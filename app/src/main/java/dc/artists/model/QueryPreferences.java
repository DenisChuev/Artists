package dc.artists.model;

import android.content.Context;
import android.preference.PreferenceManager;

public class QueryPreferences {

    private static final String PREF_HAS_VISITED = "hasVisited";
    private static final String PREF_SEARCH_QUERY = "searchQuery";

    public static String getStoredQuery(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PREF_SEARCH_QUERY, "");
    }

    public static void setStoredQuery(Context context, String query) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(PREF_SEARCH_QUERY, query)
                .apply();
    }

    public static boolean isFirstStartup(Context context) {
        return !PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(PREF_HAS_VISITED, false);
    }

    public static void updateVisit(Context context) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(PREF_HAS_VISITED, true)
                .apply();
    }

    public static void setStoredResponse(Context context, String url, String response) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(url, response).apply();
    }

    public static String getStoredResponse(Context context, String url) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(url, "");
    }
}