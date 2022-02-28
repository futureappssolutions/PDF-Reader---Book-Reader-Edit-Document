package pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Ads;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.appopen.AppOpenAd;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Activity.ActivityLaunchScreen;

import java.util.Date;

import static androidx.lifecycle.Lifecycle.Event.ON_START;


public class AppOpenManager implements Application.ActivityLifecycleCallbacks, LifecycleObserver {
    public static int adCount = 0;
    private static boolean isShowingAd = false;
    private final Application myApplication;
    private AppOpenAd appOpenAd = null;
    private long loadTime = 0;
    private Activity currentActivity;

    public AppOpenManager(Application nameApplication) {
        this.myApplication = nameApplication;
        this.myApplication.registerActivityLifecycleCallbacks(this);
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
    }

    public void fetchAd() {
        if (isAdAvailable()) {
            return;
        }
        AppOpenAd.AppOpenAdLoadCallback loadCallback = new AppOpenAd.AppOpenAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull AppOpenAd ad) {
                AppOpenManager.this.appOpenAd = ad;
                AppOpenManager.this.loadTime = (new Date()).getTime();
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {

            }
        };
        AdRequest adRequest = getAdRequest();
        AppOpenAd.load(myApplication,Preference.getGoogle_open(), adRequest, AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT, loadCallback);
    }

    public void showAdIfAvailable() {
        if (!isShowingAd && isAdAvailable()) {
            adCount++;
            FullScreenContentCallback fullScreenContentCallback =
                    new FullScreenContentCallback() {
                        @Override
                        public void onAdDismissedFullScreenContent() {
                            AppOpenManager.this.appOpenAd = null;
                            isShowingAd = false;
                            fetchAd();
                        }

                        @Override
                        public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                        }

                        @Override
                        public void onAdShowedFullScreenContent() {
                            isShowingAd = true;
                        }
                    };
            appOpenAd.setFullScreenContentCallback(fullScreenContentCallback);
            appOpenAd.show(currentActivity);
        } else {
            fetchAd();
        }
    }

    private AdRequest getAdRequest() {
        return new AdRequest.Builder().build();
    }

    private boolean wasLoadTimeLessThanNHoursAgo() {
        long dateDifference = (new Date()).getTime() - this.loadTime;
        long numMilliSecondsPerHour = 3600000;
        return (dateDifference < (numMilliSecondsPerHour * (long) 4));
    }

    public boolean isAdAvailable() {
        return appOpenAd != null && wasLoadTimeLessThanNHoursAgo();
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        currentActivity = activity;
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        currentActivity = activity;
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {

    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        currentActivity = activity;
        if (activity instanceof ActivityLaunchScreen) {
            if (!(Preference.getActive_Weekly().equals("true") || Preference.getActive_Monthly().equals("true") || Preference.getActive_Yearly().equals("true"))) {
                showAdIfAvailable();
            }
        }
    }

    @OnLifecycleEvent(ON_START)
    public void onStart() {
        if (currentActivity instanceof ActivityLaunchScreen) {
            fetchAd();
        } else {
            if (!(Preference.getActive_Weekly().equals("true") || Preference.getActive_Monthly().equals("true") || Preference.getActive_Yearly().equals("true"))) {
                showAdIfAvailable();
            }
        }
    }
}