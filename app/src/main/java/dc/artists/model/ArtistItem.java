package dc.artists.model;

import android.content.Context;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

import dc.artists.R;

@SuppressWarnings("ALL")
public class ArtistItem implements Serializable {
    @SerializedName("id")
    private int mId;
    @SerializedName("name")
    private String mName;
    @SerializedName("tracks")
    private int mTracks;
    @SerializedName("genres")
    private List<String> mGenres;
    @SerializedName("albums")
    private int mAlbums;
    @SerializedName("link")
    private String mLink;
    @SerializedName("description")
    private String mDescription;
    @SerializedName("cover")
    private Cover mCover;

    public int getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    int getTracks() {
        return mTracks;
    }

    public String getTracks(Context context) {
        int tracksCount = getTracks();
        return context.getResources()
                .getQuantityString(R.plurals.tracks, tracksCount, tracksCount);
    }

    int getAlbums() {
        return mAlbums;
    }

    public String getAlbums(Context context) {
        int albumsCount = getAlbums();
        return context.getResources()
                .getQuantityString(R.plurals.albums, albumsCount, albumsCount);
    }

    public String getLink() {
        return mLink;
    }

    public String getDescription() {
        return Character.toUpperCase(mDescription.charAt(0)) + mDescription.substring(1);
    }

    public String getGenres() {
        String genresArray = String.valueOf(mGenres);
        return genresArray.substring(1, genresArray.length() - 1);
    }

    public String getBigCover() {
        return mCover.mBigCover;
    }

    public String getSmallCover() {
        return mCover.mSmallCover;
    }

    private class Cover implements Serializable {
        @SerializedName("small")
        private String mSmallCover;

        @SerializedName("big")
        private String mBigCover;
    }
}