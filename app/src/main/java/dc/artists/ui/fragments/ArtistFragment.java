package dc.artists.ui.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import dc.artists.R;
import dc.artists.model.ArtistItem;
import dc.artists.ui.ArtistsSwipeRefreshLayout;

public class ArtistFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private static final String ARG_ARTIST_ITEM = "artist_item";

    private ArtistItem mArtistItem;
    private ImageView mArtistBigCover;
    private TextView mArtistGenres;
    private TextView mArtistAlbumsAndTracks;
    private TextView mArtistDescription;
    private ArtistsSwipeRefreshLayout mSwipeRefreshLayout;
    private ProgressBar mProgressBar;

    public static ArtistFragment newInstance(ArtistItem artistItem) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_ARTIST_ITEM, artistItem);

        ArtistFragment fragment = new ArtistFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mArtistItem = (ArtistItem) getArguments().getSerializable(ARG_ARTIST_ITEM);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_artist, container, false);

        ScrollView mScrollView = (ScrollView) view.findViewById(R.id.scroll_view);

        mSwipeRefreshLayout = (ArtistsSwipeRefreshLayout) view.findViewById(R.id.refresh_detail);
        mSwipeRefreshLayout.setColorSchemeColors(Color.RED, Color.GREEN, Color.BLUE, Color.CYAN);
        mSwipeRefreshLayout.setMyScrollableView(mScrollView);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        mProgressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
        mArtistBigCover = (ImageView) view.findViewById(R.id.artist_big_cover);
        mArtistGenres = (TextView) view.findViewById(R.id.artist_genres);
        mArtistAlbumsAndTracks = (TextView) view.findViewById(R.id.artist_albums_and_tracks);
        mArtistDescription = (TextView) view.findViewById(R.id.artist_description);

        updateUI();

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.artist_menu, menu);
        MenuItem linkMenuItem = menu.findItem(R.id.action_link);

        String artistLink = mArtistItem.getLink();
        linkMenuItem.setVisible(null != artistLink && !artistLink.isEmpty());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_link:
                String url = mArtistItem.getLink();
                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    url = "http://" + url;
                }
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
                return true;
            case android.R.id.home:
                getActivity().finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateUI() {
        mArtistDescription.setText(mArtistItem.getDescription());
        mArtistGenres.setText(mArtistItem.getGenres());
        mArtistAlbumsAndTracks.setText(String.format("%s | %s",
                mArtistItem.getAlbums(getActivity()), mArtistItem.getTracks(getActivity())));


        mProgressBar.setVisibility(View.VISIBLE);
        Glide.with(getActivity())
                .load(mArtistItem.getBigCover())
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        mProgressBar.setVisibility(View.GONE);
                        mSwipeRefreshLayout.setRefreshing(false);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        mProgressBar.setVisibility(View.GONE);
                        mSwipeRefreshLayout.setRefreshing(false);
                        return false;
                    }
                })
                .error(R.drawable.placeholder)
                .into(mArtistBigCover);
    }

    @Override
    public void onRefresh() {
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
                updateUI();
            }
        });
    }
}