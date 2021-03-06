package pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.InstallState;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Ads.GoogleAppLovinAds;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Ads.Preference;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.R;

@SuppressLint("CustomSplashScreen")
public class ActivityLaunchScreen extends AppCompatActivity implements OnSuccessListener<AppUpdateInfo> {
    public static final int REQUEST_CODE = 1234;
    public final int RC_APP_UPDATE = 100;
    public AppUpdateManager appUpdateManager;
    public boolean mNeedsFlexibleUpdate;
    public boolean UpdateAvailable = false;

    InstallStateUpdatedListener installStateUpdatedListener = new InstallStateUpdatedListener() {
        @Override
        public void onStateUpdate(InstallState state) {
            if (state.installStatus() == InstallStatus.DOWNLOADED) {
                popupSnackBarForCompleteUpdate();
            } else if (state.installStatus() == InstallStatus.INSTALLED) {
                if (appUpdateManager != null) {
                    appUpdateManager.unregisterListener(installStateUpdatedListener);
                }
            }
        }
    };

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_splash);
        setStatusBar();

        appUpdateManager = AppUpdateManagerFactory.create(ActivityLaunchScreen.this);
        mNeedsFlexibleUpdate = false;


        FirebaseDatabase.getInstance().getReference().child("app_data").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Preference.setGoogle_banner(dataSnapshot.child("banner_id").getValue(String.class));
                Preference.setGoogle_full(dataSnapshot.child("full_id").getValue(String.class));
                Preference.setGoogle_native(dataSnapshot.child("native_id").getValue(String.class));
                Preference.setGoogle_open(dataSnapshot.child("app_open_id").getValue(String.class));
                Preference.setAds_time(dataSnapshot.child("ads_time").getValue(String.class));
                Preference.setAds_name(dataSnapshot.child("ads_name").getValue(String.class));

                Preference.setAppLovin_banner(dataSnapshot.child("al_banner").getValue(String.class));
                Preference.setAppLovin_native(dataSnapshot.child("al_native").getValue(String.class));
                Preference.setAppLovin_full(dataSnapshot.child("al_full").getValue(String.class));

                Preference.setActive_AdsWeek(dataSnapshot.child("weekly_key").getValue(String.class));
                Preference.setActive_AdsMonth(dataSnapshot.child("monthly_key").getValue(String.class));
                Preference.setActive_AdsYear(dataSnapshot.child("yearly_key").getValue(String.class));
                Preference.setBase_key(dataSnapshot.child("base_key").getValue(String.class));

                try {
                    GoogleAppLovinAds.allcount60 = new android.os.CountDownTimer(Integer.parseInt(Preference.getAds_time()) * 1000L, 1000) {
                        public void onTick(long millisUntilFinished) {
                            GoogleAppLovinAds.adsdisplay = false;
                        }

                        public void onFinish() {
                            GoogleAppLovinAds.adsdisplay = true;
                        }
                    };
                    GoogleAppLovinAds.allcount60.start();
                } catch (NumberFormatException ex) {
                    ex.printStackTrace();
                }

                GoogleAppLovinAds.preLoadAds(ActivityLaunchScreen.this);

                new Handler(Looper.myLooper()).postDelayed(() -> {
                    startActivity(new Intent(ActivityLaunchScreen.this, ActivityMain.class));
                    finish();
                }, 4000);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("databaseError", databaseError.toString());
                Log.e("databaseError", databaseError.getMessage());
            }
        });

    }

    @Override
    public void onSuccess(AppUpdateInfo appUpdateInfo) {
        if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
            startUpdate(appUpdateInfo);
        } else if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
            popupSnackBarForCompleteUpdate();
        } else if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
            if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                startUpdate(appUpdateInfo);
            } else if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                mNeedsFlexibleUpdate = true;
                showFlexibleUpdateNotification();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        appUpdateManager = AppUpdateManagerFactory.create(this);
        appUpdateManager.registerListener(installStateUpdatedListener);
        appUpdateManager.getAppUpdateInfo().addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                UpdateAvailable = true;
                try {
                    appUpdateManager.startUpdateFlowForResult(appUpdateInfo, AppUpdateType.IMMEDIATE, ActivityLaunchScreen.this, RC_APP_UPDATE);
                } catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();
                }
            } else if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                popupSnackBarForCompleteUpdate();
            } else {
                UpdateAvailable = false;
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (appUpdateManager != null) {
            appUpdateManager.unregisterListener(installStateUpdatedListener);
        }
    }

    private void startUpdate(final AppUpdateInfo appUpdateInfo) {
        final Activity activity = this;
        new Thread(() -> {
            try {
                appUpdateManager.startUpdateFlowForResult(appUpdateInfo, AppUpdateType.IMMEDIATE, activity, REQUEST_CODE);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void popupSnackBarForCompleteUpdate() {
        Snackbar snackbar = Snackbar.make(findViewById(R.id.rl_splash), "An update has just been downloaded.", Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("INSTALL", view -> {
            if (appUpdateManager != null) {
                appUpdateManager.completeUpdate();
            }
        });
        snackbar.setActionTextColor(getResources().getColor(R.color.appColor));
        snackbar.show();
    }

    private void showFlexibleUpdateNotification() {
        Snackbar snackbar = Snackbar.make(findViewById(R.id.rl_splash), "An update is available and accessible in More.", Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_APP_UPDATE) {
            if (resultCode != RESULT_OK) {
                UpdateAvailable = false;
            }
        }
    }

    public void setStatusBar() {
        Window window = getWindow();
        if (Build.VERSION.SDK_INT >= 23) {
            window.getDecorView().setSystemUiVisibility(window.getDecorView().getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.white));
            window.setNavigationBarColor(ContextCompat.getColor(this, R.color.white));
        } else if (Build.VERSION.SDK_INT == 21 || Build.VERSION.SDK_INT == 22) {
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.white));
            window.setNavigationBarColor(ContextCompat.getColor(this, R.color.white));
        } else {
            window.clearFlags(0);
        }
    }
}

