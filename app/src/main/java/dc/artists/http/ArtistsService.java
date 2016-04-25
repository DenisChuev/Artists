package dc.artists.http;

import java.util.List;

import dc.artists.model.ArtistItem;
import retrofit2.Call;
import retrofit2.http.GET;

public interface ArtistsService {
    @GET("artists.json")
    Call<List<ArtistItem>> getArtistItems();
}