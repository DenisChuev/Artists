package dc.artists.http;

import android.support.v4.BuildConfig;
import android.util.Log;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RestClient {
    private static final String TAG = "RestClient";
    private static final String API_URL =
            "http://download.cdn.yandex.net/mobilization-2016/";

    private static RestClient sRestClient;
    private Retrofit mRetrofit;
    private ArtistsService mArtistsService;

    private RestClient() {
        try {
            mRetrofit = new Retrofit.Builder()
                    .baseUrl(API_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            mArtistsService = mRetrofit.create(ArtistsService.class);
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Check url", e);
            }
        }
    }

    public static RestClient get() {
        if (null == sRestClient) {
            sRestClient = new RestClient();
        }
        return sRestClient;
    }

    public ArtistsService getService() {
        return mArtistsService;
    }
}