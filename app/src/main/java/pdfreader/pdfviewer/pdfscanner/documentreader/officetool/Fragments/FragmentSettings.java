package pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Fragments;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.R;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.utils.LocaleUtils;

import java.util.Objects;

public class FragmentSettings extends PreferenceFragmentCompat {
    public static final String KEY_PREFS_LANGUAGE = "prefs_language";
    public static final String KEY_PREFS_REMEMBER_LAST_PAGE = "prefs_remember_last_page";
    public static final String KEY_PREFS_STAY_AWAKE = "prefs_stay_awake";
    public Context context;
    SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener = (sharedPreferences, str) -> {
        if (((str.hashCode() != -976476153 || !str.equals(FragmentSettings.KEY_PREFS_LANGUAGE)) ? (char) 65535 : 0) == 0) {
            FragmentSettings settingsFragment = FragmentSettings.this;
            settingsFragment.bindLanguagePreferenceSummaryToValue(Objects.requireNonNull(settingsFragment.findPreference(FragmentSettings.KEY_PREFS_LANGUAGE)));
            LocaleUtils.setUpLanguage(FragmentSettings.this.context);
            ((Activity) FragmentSettings.this.context).recreate();
        }
    };

    @Override
    public void onCreatePreferences(Bundle bundle, String str) {
        addPreferencesFromResource(R.xml.preferences);
        this.context = getContext();
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this.sharedPreferenceChangeListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this.sharedPreferenceChangeListener);
    }

    public void bindLanguagePreferenceSummaryToValue(Preference preference) {
        preference.setSummary(LocaleUtils.keyToLanguage(this.context, getPreferenceScreen().getSharedPreferences().getString(preference.getKey(), "en")));
    }
}