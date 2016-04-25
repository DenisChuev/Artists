package dc.artists;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import java.util.Locale;

@SuppressWarnings("ALL")
public class ArtistsApplication extends Application {

    public static void updateLanguage(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        String lang = prefs.getString("locale_override", "");
        updateLanguage(ctx, lang);
    }

    public static void updateLanguage(Context context, String lang) {
        Configuration configuration = new Configuration();
        if (!TextUtils.isEmpty(lang))
            configuration.locale = new Locale(lang);
        else
            configuration.locale = Locale.getDefault();

        context.getResources().updateConfiguration(configuration, null);
    }

    @Override
    public void onCreate() {
        updateLanguage(this, "ru");
        super.onCreate();
    }
}