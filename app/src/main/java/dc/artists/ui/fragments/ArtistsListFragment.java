package dc.artists.ui.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.BuildConfig;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.github.siyamed.shapeimageview.RoundedImageView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

import dc.artists.R;
import dc.artists.http.ArtistsService;
import dc.artists.http.RestClient;
import dc.artists.model.ArtistItem;
import dc.artists.model.ArtistsStore;
import dc.artists.model.QueryPreferences;
import dc.artists.model.Sort;
import dc.artists.ui.activities.ArtistPagerActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.widget.Toast.LENGTH_SHORT;
import static android.widget.Toast.makeText;

public class ArtistsListFragment extends Fragment
        implements SwipeRefreshLayout.OnRefreshListener {
    private static final String TAG = "ArtistsListFragment";
    private static final String URL =
            "http://download.cdn.yandex.net/mobilization-2016/artists.json";

    private ArtistsStore mArtistsStore;
    private RecyclerView mArtistsRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ArtistsAdapter mArtistsAdapter;
    private Toast mToast;

    public static ArtistsListFragment newInstance() {
        return new ArtistsListFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
        mArtistsStore = ArtistsStore.get();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_artists_list, container, false);

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.refresh_list);
        mSwipeRefreshLayout.setColorSchemeColors(Color.RED, Color.GREEN, Color.BLUE, Color.CYAN);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        mArtistsRecyclerView = (RecyclerView) view.findViewById(R.id.fragment_artists_list_recycler_view);
        mArtistsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mArtistsRecyclerView.setHasFixedSize(true);

        updateArtistItems(URL);

        return view;
    }

    private void updateArtistItems(String url) {
        if (mArtistsStore.isEmpty()) {
            if (isFirstStartup()) {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "First start the application");
                }
                try {
                    downloadArtistItems(url);
                } catch (Exception e) {
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "download artists error", e);
                    }
                }
            } else {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "Download cache");
                }
                new DownloadCacheTask(url).execute();
            }
        } else {
            setupAdapter();
        }
    }

    private boolean isFirstStartup() {
        return QueryPreferences.isFirstStartup(getActivity());
    }

    private void downloadArtistItems(final String url) throws Exception {
        mSwipeRefreshLayout.setRefreshing(true);

        final ArtistsService artistsService = RestClient.get().getService();

        final Call<List<ArtistItem>> call = artistsService.getArtistItems();

        call.enqueue(new Callback<List<ArtistItem>>() {
            @Override
            public void onResponse(Call<List<ArtistItem>> call, Response<List<ArtistItem>> response) {
                List<ArtistItem> artistItems = response.body();
                if (!artistItems.isEmpty()) {
                    mArtistsStore.addArtistItems(response.body());

                    QueryPreferences.updateVisit(getActivity());
                    saveArtistItems(url, parseToJson(artistItems));

                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "download successful");
                    }
                } else {
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "download failed");
                    }
                    showErrorMessage();
                }
                setupAdapter();
            }

            @Override
            public void onFailure(Call<List<ArtistItem>> call, Throwable t) {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "download failed", t);
                }
                setupAdapter();
                showErrorMessage();
            }
        });
    }

    private void showErrorMessage() {
        mSwipeRefreshLayout.setRefreshing(false);
        showToast(R.string.network_error);
    }

    private void showToast(int message) {
        if (null != mToast) {
            mToast.cancel();
        }
        mToast = makeText(getActivity(), message, LENGTH_SHORT);
        mToast.show();
    }

    private void setupAdapter() {
        if (isAdded() && (null != mArtistsRecyclerView)) {
            mArtistsAdapter = new ArtistsAdapter(mArtistsStore.getArtistItems());
            mSwipeRefreshLayout.setRefreshing(false);
            mArtistsRecyclerView.setAdapter(mArtistsAdapter);
        }
    }

    private void setupAdapter(List<ArtistItem> artistItems) {
        if (isAdded() && (null != mArtistsRecyclerView)) {
            mSwipeRefreshLayout.setRefreshing(false);
            mArtistsRecyclerView.setAdapter(new ArtistsAdapter(artistItems));
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_artist_list_menu, menu);

        final MenuItem searchMenuItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) searchMenuItem.getActionView();

        searchView.setMaxWidth(Integer.MAX_VALUE);

        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String query = QueryPreferences.getStoredQuery(getActivity());
                searchView.setQuery(query, false);
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, query);
                }
                QueryPreferences.setStoredQuery(getActivity(), query);
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, newText);
                }
                mArtistsAdapter.getFilter().filter(newText);
                return true;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sort_by_name_ascending:
                new SortTask(Sort.BY_NAME_ASCENDING).execute();
                return true;
            case R.id.action_sort_by_albums_ascending:
                new SortTask(Sort.BY_ALBUMS_ASCENDING).execute();
                return true;
            case R.id.action_sort_by_tracks_ascending:
                new SortTask(Sort.BY_TRACKS_ASCENDING).execute();
                return true;
            case R.id.action_sort_by_name_descending:
                new SortTask(Sort.BY_NAME_DESCENDING).execute();
                return true;
            case R.id.action_sort_by_albums_descending:
                new SortTask(Sort.BY_ALBUMS_DESCENDING).execute();
                return true;
            case R.id.action_sort_by_tracks_descending:
                new SortTask(Sort.BY_TRACKS_DESCENDING).execute();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRefresh() {
        mSwipeRefreshLayout.setRefreshing(true);
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                updateArtistItems(URL);
            }
        });
        mSwipeRefreshLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(false);
            }
        }, 3000);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        saveArtistItems(URL, parseToJson(mArtistsStore.getArtistItems()));
    }

    private void saveArtistItems(String url, String response) {
        QueryPreferences.setStoredResponse(getActivity(), url,
                response);
    }

    private String parseToJson(List<ArtistItem> artistItems) {
        return new Gson().toJson(artistItems);
    }

    private List<ArtistItem> parseFromJson(String json) {
        return new Gson().fromJson(json, new TypeToken<List<ArtistItem>>() {
        }.getType());
    }

    private class DownloadCacheTask extends AsyncTask<Void, Void, Void> {
        private final String mUrl;

        public DownloadCacheTask(String url) {
            mUrl = url;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            String json = QueryPreferences.getStoredResponse(getActivity(), mUrl);
            if (json.isEmpty()) {
                try {
                    downloadArtistItems(mUrl);
                } catch (Exception e) {
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "download artists error", e);
                    }
                }
            } else {
                mArtistsStore.addArtistItems(parseFromJson(json));
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            setupAdapter();
        }
    }

    private class SortTask extends AsyncTask<Void, Void, Void> {
        private final Sort mSort;

        public SortTask(Sort sort) {
            mSort = sort;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            mArtistsStore.sortArtists(mSort);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            setupAdapter();
        }
    }

    private class ArtistsHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final RoundedImageView mArtistSmallCover;
        private final TextView mArtistName;
        private final TextView mArtistAlbumsAndTracks;
        private final TextView mArtistGenres;
        private ArtistItem mArtistItem;

        public ArtistsHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            mArtistName = (TextView) itemView.findViewById(R.id.artist_name);
            mArtistSmallCover = (RoundedImageView) itemView.findViewById(R.id.artist_small_cover);
            mArtistGenres = (TextView) itemView.findViewById(R.id.artist_genres);
            mArtistAlbumsAndTracks = (TextView) itemView.findViewById(R.id.artist_albums_and_tracks);
        }

        public void bindArtistItem(final ArtistItem artistItem) {
            mArtistItem = artistItem;
            mArtistName.setText(artistItem.getName());
            mArtistGenres.setText(artistItem.getGenres());
            mArtistAlbumsAndTracks.setText(String.format("%s, %s",
                    artistItem.getAlbums(getActivity()),
                    artistItem.getTracks(getActivity())));

            Glide.with(getActivity())
                    .load(artistItem.getSmallCover())
                    .fitCenter()
                    .error(R.drawable.placeholder)
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(mArtistSmallCover);
        }

        @Override
        public void onClick(View view) {
            Intent intent = ArtistPagerActivity.newIntent(getActivity(), mArtistItem);
            startActivity(intent);
        }
    }

    private class ArtistsAdapter extends RecyclerView.Adapter<ArtistsHolder> implements Filterable {
        private final List<ArtistItem> mArtistItems;

        public ArtistsAdapter(List<ArtistItem> artistItems) {
            mArtistItems = artistItems;
        }

        @Override
        public ArtistsHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.list_item_artist, parent, false);
            return new ArtistsHolder(view);
        }

        @Override
        public void onBindViewHolder(ArtistsHolder holder, int position) {
            ArtistItem artistItem = mArtistItems.get(position);
            holder.bindArtistItem(artistItem);
        }

        @Override
        public int getItemCount() {
            return mArtistItems.size();
        }

        @Override
        public Filter getFilter() {
            return new ArtistsFilter();
        }

        private class ArtistsFilter extends Filter {
            @Override
            protected FilterResults performFiltering(CharSequence query) {
                List<ArtistItem> resultList = query.length() == 0 ?
                        mArtistsStore.getArtistItems() :
                        mArtistsStore.searchArtists(query);
                FilterResults results = new FilterResults();
                results.values = resultList;
                results.count = resultList.size();
                return results;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                setupAdapter((List<ArtistItem>) filterResults.values);
            }
        }
    }
}