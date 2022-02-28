package pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Ads;

import android.content.Context;
import android.content.SharedPreferences;

public class Preference {
    public SharedPreferences.Editor editor;
    private static final String active_Weekly = "weekly_key";
    private static final String active_Monthly = "monthly_key";
    private static final String active_Yearly = "yearly_key";
    private static final String base_key = "base_key";
    private static final String Google_full = "g_full";
    private static final String Google_banner = "g_banner";
    private static final String Google_native = "g_native";
    private static final String Google_open = "g_app_open";
    private static final String Ads_time = "ads_time";
    private static final String ads_name = "ads_name";
    private static final String AppLovin_full = "AppLovin_full";
    private static final String AppLovin_banner = "AppLovin_banner";
    private static final String AppLovin_native = "AppLovin_native";


    private static final String active_AkeyY = "active_AkeyY";
    private static final String active_AkeyM = "active_AkeyM";
    private static final String active_AkeyG = "active_AkeyG";


    private static SharedPreferences get() {
        return MyApplication.getApp().getSharedPreferences("AppController", Context.MODE_PRIVATE);
    }


    public static boolean getBoolean(String str, boolean z) {
        return get().getBoolean(str, z);
    }

    public static void setBoolean(String str, boolean z) {
        get().edit().putBoolean(str, z).apply();
    }

    public static String getGoogle_banner() {
        return get().getString(Google_banner, "");
    }

    public static void setGoogle_banner(String value) {
        get().edit().putString(Google_banner, value).apply();
    }

    public static String getAds_name() {
        return get().getString(ads_name, "");
    }

    public static void setAds_name(String value) {
        get().edit().putString(ads_name, value).apply();
    }


    public static String getGoogle_full() {
        return get().getString(Google_full, "");
    }

    public static void setGoogle_full(String value) {
        get().edit().putString(Google_full, value).apply();
    }

    public static String getAppLovin_full() {
        return get().getString(AppLovin_full, "");
    }

    public static void setAppLovin_full(String value) {
        get().edit().putString(AppLovin_full, value).apply();
    }

    public static String getAppLovin_banner() {
        return get().getString(AppLovin_banner, "");
    }

    public static void setAppLovin_banner(String value) {
        get().edit().putString(AppLovin_banner, value).apply();
    }

    public static String getAppLovin_native() {
        return get().getString(AppLovin_native, "");
    }

    public static void setAppLovin_native(String value) {
        get().edit().putString(AppLovin_native, value).apply();
    }


    public static String getGoogle_native() {
        return get().getString(Google_native, "");
    }

    public static void setGoogle_native(String value) {
        get().edit().putString(Google_native, value).apply();
    }

    public static String getGoogle_open() {
        return get().getString(Google_open, "");
    }

    public static void setGoogle_open(String value) {
        get().edit().putString(Google_open, value).apply();
    }

    public static String getAds_time() {
        return get().getString(Ads_time, "");
    }

    public static void setAds_time(String value) {
        get().edit().putString(Ads_time, value).apply();
    }

    public static String getActive_Weekly() {
        return get().getString(active_Weekly, "");
    }

    public static void setActive_Weekly(String value) {
        get().edit().putString(active_Weekly, value).apply();
    }

    public static String getActive_Monthly() {
        return get().getString(active_Monthly, "");
    }

    public static void setActive_Monthly(String value) {
        get().edit().putString(active_Monthly, value).apply();
    }

    public static String getActive_Yearly() {
        return get().getString(active_Yearly, "");
    }

    public static void setActive_Yearly(String value) {
        get().edit().putString(active_Yearly, value).apply();
    }

    public static String getBase_key() {
        return get().getString(base_key, "");
    }

    public static void setBase_key(String value) {
        get().edit().putString(base_key, value).apply();
    }

    public static String getActive_AdsWeek() {
        return get().getString(active_AkeyG, "");
    }

    public static void setActive_AdsWeek(String value) {
        get().edit().putString(active_AkeyG, value).apply();
    }

    public static String getActive_AdsMonth() {
        return get().getString(active_AkeyM, "");
    }

    public static void setActive_AdsMonth(String value) {
        get().edit().putString(active_AkeyM, value).apply();
    }

    public static String getActive_AdsYear() {
        return get().getString(active_AkeyY, "");
    }

    public static void setActive_AdsYear(String value) {
        get().edit().putString(active_AkeyY, value).apply();
    }
}
