package pdfreader.pdfviewer.pdfscanner.documentreader.officetool.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.preference.PreferenceManager;

import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Fragments.FragmentSettings;

import java.util.Locale;

public class LocaleUtils {
    public static void setUpLanguage(Context context) {
        String string = PreferenceManager.getDefaultSharedPreferences(context).getString(FragmentSettings.KEY_PREFS_LANGUAGE, "en");
        Configuration configuration = context.getResources().getConfiguration();
        Locale locale = new Locale(string);
        Locale.setDefault(locale);
        configuration.locale = locale;
        context.getResources().updateConfiguration(configuration, context.getResources().getDisplayMetrics());
    }

    public static String keyToLanguage(Context context, String str) {
        SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(context).edit();
        edit.putString(FragmentSettings.KEY_PREFS_LANGUAGE, str);
        edit.apply();
        return null;
    }
}
