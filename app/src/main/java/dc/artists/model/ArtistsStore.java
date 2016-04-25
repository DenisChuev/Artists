package dc.artists.model;

import android.support.v4.BuildConfig;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ArtistsStore {
    private final static String TAG = "ArtistsStore";
    private static ArtistsStore sArtistsStore;
    private final List<ArtistItem> mArtistItems = new ArrayList<>();

    private ArtistsStore() {
    }

    public static ArtistsStore get() {
        if (null == sArtistsStore) {
            sArtistsStore = new ArtistsStore();
        }
        return sArtistsStore;
    }

    public List<ArtistItem> getArtistItems() {
        return mArtistItems;
    }

    public boolean isEmpty() {
        return mArtistItems.size() == 0;
    }

    public void addArtistItems(List<ArtistItem> artistItems) {
        mArtistItems.addAll(artistItems);

        if (BuildConfig.DEBUG) {
            Log.d(TAG, String.format("Add %d artists", artistItems.size()));
        }
    }

    public void sortArtists(final Sort type) {
        Collections.sort(mArtistItems, new Comparator<ArtistItem>() {
            @Override
            public int compare(ArtistItem a, ArtistItem b) {
                switch (type) {
                    case BY_NAME_ASCENDING:
                        return a.getName().compareTo(b.getName());
                    case BY_NAME_DESCENDING:
                        return b.getName().compareTo(a.getName());
                    case BY_ALBUMS_ASCENDING:
                        int aAlbums = a.getAlbums();
                        int bAlbums = b.getAlbums();
                        return aAlbums > bAlbums ? 1 : aAlbums < bAlbums ? -1 : 0;
                    case BY_ALBUMS_DESCENDING:
                        aAlbums = a.getAlbums();
                        bAlbums = b.getAlbums();
                        return aAlbums < bAlbums ? 1 : aAlbums > bAlbums ? -1 : 0;
                    case BY_TRACKS_ASCENDING:
                        int aTracks = a.getTracks();
                        int bTracks = b.getTracks();
                        return aTracks > bTracks ? 1 : aTracks < bTracks ? -1 : 0;
                    case BY_TRACKS_DESCENDING:
                        aTracks = a.getTracks();
                        bTracks = b.getTracks();
                        return aTracks < bTracks ? 1 : aTracks > bTracks ? -1 : 0;
                    default:
                        return 0;
                }
            }
        });
    }

    public List<ArtistItem> searchArtists(CharSequence query) {
        List<ArtistItem> resultSearchList = new ArrayList<>();
        for (ArtistItem artistItem : mArtistItems) {
            String artistName = artistItem.getName().toLowerCase().trim();
            String searchPattern = query.toString().toLowerCase().trim();

            if (artistName.contains(searchPattern)) {
                resultSearchList.add(artistItem);
            }
        }
        return resultSearchList;
    }
}