package pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Ads.GoogleAppLovinAds;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.R;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Adapter.AdapterSelectImages;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.data.DbHelper;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.utils.Utils;

import java.util.ArrayList;
import java.util.Objects;

public class ActivitySelectImages extends AppCompatActivity implements AdapterSelectImages.OnImageSelectedListener {
    public Context context;
    public DbHelper dbHelper;
    public int numberOfColumns;
    public ProgressBar progressImgSelect;
    public RecyclerView recyclerSelectImgFromGallery;
    public SharedPreferences sharedPreferences;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_select_images);
        setStatusBar();


        setSupportActionBar(findViewById(R.id.toolbar));
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        Spinner spinner = findViewById(R.id.spinnerGalleryDirectory);
        recyclerSelectImgFromGallery = findViewById(R.id.recyclerSelectImgFromGallery);
        progressImgSelect = findViewById(R.id.progressImgSelect);
        dbHelper = DbHelper.getInstance(this);
        context = this;
        final int i = Utils.isTablet(this) ? 3 : 3;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        numberOfColumns = sharedPreferences.getInt(ActivityMain.GRID_VIEW_NUM_OF_COLUMNS, i);
        spinner.setSelection(3);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long j) {
                int i2 = i;
                if (i2 == 0) {
                    new LoadImages("/").execute();
                } else if (i2 == 1) {
                    new LoadImages("/DCIM/").execute();
                } else if (i2 == 2) {
                    new LoadImages("/Download/").execute();
                } else if (i2 == 3) {
                    new LoadImages("/Pictures/").execute();
                } else if (i2 == 4) {
                    new LoadImages("/WhatsApp/Media/WhatsApp Images/").execute();
                }
            }
        });
    }

    @Override
    public void onMultiSelectedPDF(ArrayList<String> arrayList) {
        if (GoogleAppLovinAds.adsdisplay) {
            GoogleAppLovinAds.showFullAds(ActivitySelectImages.this, () -> {
                GoogleAppLovinAds.allcount60.start();
                Intent intent = new Intent(this, ActivityOrganizeImages.class);
                intent.putStringArrayListExtra(ActivityOrganizeImages.IMAGE_URIS, arrayList);
                startActivity(intent);
            });
        } else {
            Intent intent = new Intent(this, ActivityOrganizeImages.class);
            intent.putStringArrayListExtra(ActivityOrganizeImages.IMAGE_URIS, arrayList);
            startActivity(intent);
        }
    }

    public class LoadImages extends AsyncTask<Void, Void, Void> {
        public AdapterSelectImages adapter;
        public String imageDir;

        public LoadImages(String str) {
            imageDir = str;
        }

        public void onPreExecute() {
            super.onPreExecute();
            progressImgSelect.setVisibility(View.VISIBLE);
        }

        public Void doInBackground(Void... voidArr) {
            adapter = new AdapterSelectImages(context, dbHelper.getAllImages(Environment.getExternalStorageDirectory() + imageDir));
            return null;
        }

        public void onPostExecute(Void r6) {
            super.onPostExecute(r6);
            progressImgSelect.setVisibility(View.GONE);
            recyclerSelectImgFromGallery.setLayoutManager(new GridLayoutManager(context, 3, RecyclerView.VERTICAL, false));
            recyclerSelectImgFromGallery.setAdapter(adapter);
        }
    }
    public void setStatusBar() {
        Window window = getWindow();
        if (Build.VERSION.SDK_INT >= 23) {
            window.getDecorView().setSystemUiVisibility(window.getDecorView().getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.light_color));
            window.setNavigationBarColor(ContextCompat.getColor(this, R.color.white));
        } else if (Build.VERSION.SDK_INT == 21 || Build.VERSION.SDK_INT == 22) {
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.light_color));
            window.setNavigationBarColor(ContextCompat.getColor(this, R.color.white));
        } else {
            window.clearFlags(0);
        }
    }
}