package pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Activity;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Ads.GoogleAppLovinAds;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.R;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Adapter.AdapterOrganizePages;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.GetSet.PDFPage;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.utils.Utils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.util.PDFBoxResourceLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ActivityOrganizePages extends AppCompatActivity {
    public static String ORGANIZE_PAGES_TIP = "prefs_organize_pages";
    public String allPdfDocuments;
    public String allPdfPictureDir;
    public Button btnCancelProgress;
    public Context context;
    public ImageView imgCloseProgress;
    public ConstraintLayout mProgressView;
    public String pdfFilePath;
    public List<PDFPage> pdfPages = new ArrayList<>();
    public ProgressBar progressBar;
    public ConstraintLayout progressMain;
    public ProgressBar progressOrganizePages;
    public RelativeLayout rLayInfoTapMoreOptions;
    public RecyclerView recycleOrganizePages;
    public SharedPreferences sharedPreferences;
    public TextView tvCurrentAction;
    public FloatingActionButton floatBtnSave;
    public boolean showOrganizePagesTip;
    public Button btnOpenPdfFile;
    public ImageView imgPdfSuccess;
    public ImageView imgTapClose;
    public TextView tvDownloadPercent;
    public TextView tvSavedPdfPath;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_organize_pages);
        setStatusBar();
        LinearLayout ll_banner = findViewById(R.id.ll_banner);
        FrameLayout fl_native = findViewById(R.id.fl_native);

        GoogleAppLovinAds.showBannerAds(ActivityOrganizePages.this,ll_banner);
        GoogleAppLovinAds.showNativeAds(ActivityOrganizePages.this,fl_native);


        allPdfPictureDir = Environment.getExternalStorageDirectory() + "/Pictures/AllPdf/tmp/";
        allPdfDocuments = Environment.getExternalStorageDirectory() + "/Documents/AllPdf/";

        setSupportActionBar(findViewById(R.id.toolbar));
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        context = this;

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        showOrganizePagesTip = sharedPreferences.getBoolean(ORGANIZE_PAGES_TIP, true);

        progressMain = findViewById(R.id.progressMain);
        recycleOrganizePages = findViewById(R.id.recycleOrganizePages);
        progressOrganizePages = findViewById(R.id.progressOrganizePages);
        floatBtnSave = findViewById(R.id.floatBtnSave);
        rLayInfoTapMoreOptions = findViewById(R.id.rLayInfoTapMoreOptions);
        imgTapClose = findViewById(R.id.imgTapClose);

        imgTapClose.setOnClickListener(view -> {
            rLayInfoTapMoreOptions.setVisibility(View.GONE);
            rLayInfoTapMoreOptions.animate().translationY((float) (-rLayInfoTapMoreOptions.getHeight())).alpha(0.0f).setListener(new Animator.AnimatorListener() {

                public void onAnimationCancel(Animator animator) {
                }

                public void onAnimationRepeat(Animator animator) {
                }

                public void onAnimationStart(Animator animator) {
                }

                public void onAnimationEnd(Animator animator) {
                    rLayInfoTapMoreOptions.setVisibility(View.GONE);
                    SharedPreferences.Editor edit = sharedPreferences.edit();
                    edit.putBoolean(ActivityOrganizePages.ORGANIZE_PAGES_TIP, false);
                    edit.apply();
                }
            });
        });

        pdfFilePath = getIntent().getStringExtra("com.example.pdfreader.PDF_PATH");
        if (showOrganizePagesTip) {
            rLayInfoTapMoreOptions.setVisibility(View.GONE);
        } else {
            rLayInfoTapMoreOptions.setVisibility(View.GONE);
        }

        new LoadOriginePageThumbnails().execute(pdfFilePath);

        floatBtnSave.setOnClickListener(view -> new SaveOrganizedPages(getOrganizedPages(pdfPages), progressMain).execute());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Utils.deletePdfFiles(allPdfPictureDir);
    }

    public List<Integer> getOrganizedPages(List<PDFPage> list) {
        ArrayList<Integer> arrayList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            arrayList.add(list.get(i).getPageNumber());
        }
        return arrayList;
    }

    public void initProgressView() {
        tvDownloadPercent = mProgressView.findViewById(R.id.tvDownloadPercent);
        tvCurrentAction = mProgressView.findViewById(R.id.tvCurrentAction);
        progressBar = mProgressView.findViewById(R.id.progressDownloading);
        tvSavedPdfPath = mProgressView.findViewById(R.id.tvSavedPdfPath);
        imgPdfSuccess = mProgressView.findViewById(R.id.imgPdfSuccess);
        btnOpenPdfFile = mProgressView.findViewById(R.id.btnOpenPdfFile);
        btnCancelProgress = mProgressView.findViewById(R.id.btnCancelProgress);
        imgCloseProgress = mProgressView.findViewById(R.id.imgCloseProgress);
    }

    public void finishProcessBar(Context context2, String str) {
        tvDownloadPercent.setVisibility(View.GONE);
        progressBar.setVisibility(View.INVISIBLE);
        imgPdfSuccess.setVisibility(View.VISIBLE);
        imgCloseProgress.setVisibility(View.VISIBLE);
        btnOpenPdfFile.setVisibility(View.VISIBLE);
        btnCancelProgress.setVisibility(View.GONE);
        tvSavedPdfPath.setText(context2.getString(R.string.saved_to) + " " + str);
    }

    public void closeProgressBar(Context context2) {
        mProgressView.setVisibility(View.GONE);
        imgPdfSuccess.setVisibility(View.GONE);
        btnOpenPdfFile.setVisibility(View.GONE);
        imgCloseProgress.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        tvDownloadPercent.setVisibility(View.VISIBLE);
        btnCancelProgress.setVisibility(View.VISIBLE);
        progressBar.setProgress(0);
        tvDownloadPercent.setText("0%");
        tvSavedPdfPath.setText("");
        Utils.clearLightStatusBar(context2);
    }

    public void closeProgressBar(View view) {
        progressMain.setVisibility(View.GONE);
        progressMain.findViewById(R.id.imgPdfSuccess).setVisibility(View.GONE);
        progressMain.findViewById(R.id.btnOpenPdfFile).setVisibility(View.GONE);
        progressMain.findViewById(R.id.imgCloseProgress).setVisibility(View.GONE);
        progressMain.findViewById(R.id.progressDownloading).setVisibility(View.VISIBLE);
        progressMain.findViewById(R.id.tvDownloadPercent).setVisibility(View.VISIBLE);
        progressMain.findViewById(R.id.btnCancelProgress).setVisibility(View.VISIBLE);
        ((ProgressBar) progressMain.findViewById(R.id.progressDownloading)).setProgress(0);
        ((TextView) progressMain.findViewById(R.id.tvDownloadPercent)).setText("0%");
        ((TextView) progressMain.findViewById(R.id.tvSavedPdfPath)).setText("");
        Utils.clearLightStatusBar(this);
    }

    public void updateDownloadingProgressPercent(int i, int i2) {
        int i3 = ((int) (((float) i) * 100.0f)) / i2;
        tvDownloadPercent.setText(i3 + "%");
        progressBar.setProgress(i);
    }

    public void openPdfPath(final Context context2, String str, final String str2, final boolean z) {
        btnOpenPdfFile.setText(str);
        btnOpenPdfFile.setOnClickListener(view -> {
            if (z) {
                File file = new File(str2);
                Intent intent = new Intent(context2, ActivityPDFViewer.class);
                intent.putExtra(ActivityMain.PDF_LOCATION, file.getAbsolutePath());
                context2.startActivity(intent);
                return;
            }
            Intent intent2 = new Intent(context2, ActivitySelectPDF.class);
            intent2.putExtra(ActivityPDFTools.IS_DIRECTORY, true);
            context2.startActivity(intent2.putExtra(ActivityPDFTools.DIRECTORY_PATH, str2));
        });
    }

    public void showInterstialAd(Context context2, String str) {
        finishProcessBar(context2, str);
    }

    public class LoadOriginePageThumbnails extends AsyncTask<String, Void, Void> {
        AdapterOrganizePages organizePagesAdapter;

        public LoadOriginePageThumbnails() {
        }

        public void onPreExecute() {
            super.onPreExecute();
        }

        public Void doInBackground(String... strArr) {
            int i;
            String str;
            int i2;
            PdfiumCore pdfiumCore = new PdfiumCore(context);
            Uri fromFile = Uri.fromFile(new File(strArr[0]));

            try {
                PdfDocument newDocument = pdfiumCore.newDocument(context.getContentResolver().openFileDescriptor(fromFile, "r"));
                int pageCount = pdfiumCore.getPageCount(newDocument);

                File file = new File(allPdfPictureDir);
                if (!file.exists()) {
                    file.mkdirs();
                }
                int i3 = 0;
                while (i3 < pageCount) {
                    String str2 = allPdfPictureDir + System.currentTimeMillis() + ".jpg";
                    FileOutputStream fileOutputStream = new FileOutputStream(str2);
                    pdfiumCore.openPage(newDocument, i3);
                    int pageWidthPoint = pdfiumCore.getPageWidthPoint(newDocument, i3) / 2;
                    int pageHeightPoint = pdfiumCore.getPageHeightPoint(newDocument, i3) / 2;
                    try {
                        Bitmap createBitmap = Bitmap.createBitmap(pageWidthPoint, pageHeightPoint, Bitmap.Config.RGB_565);
                        i = pageCount;
                        str = str2;
                        try {
                            pdfiumCore.renderPageBitmap(newDocument, createBitmap, i3, 0, 0, pageWidthPoint, pageHeightPoint, true);
                            createBitmap.compress(Bitmap.CompressFormat.JPEG, 50, fileOutputStream);
                        } catch (OutOfMemoryError ignored) {
                        }
                        i2 = 1;
                    } catch (OutOfMemoryError e) {
                        i = pageCount;
                        str = str2;
                        i2 = 1;
                        Toast.makeText(context, (int) R.string.failed_low_memory, Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                        i3++;
                        pdfPages.add(new PDFPage(i3, Uri.fromFile(new File(str))));
                    }
                    i3 += i2;
                    pdfPages.add(new PDFPage(i3, Uri.fromFile(new File(str))));
                    pageCount = i;
                }
                pdfiumCore.closeDocument(newDocument);
                return null;
            } catch (Exception e2) {
                e2.printStackTrace();
                return null;
            }
        }

        public void onPostExecute(Void r6) {
            super.onPostExecute(r6);
            organizePagesAdapter = new AdapterOrganizePages(context, pdfPages);
            recycleOrganizePages.setLayoutManager(new GridLayoutManager(context, Utils.isTablet(context) ? 6 : 3, RecyclerView.VERTICAL, false));
            progressOrganizePages.setVisibility(View.GONE);
            recycleOrganizePages.setAdapter(organizePagesAdapter);
            floatBtnSave.setVisibility(View.VISIBLE);

            new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(15, 0) {

                @Override
                public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                }

                @Override
                public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder2) {
                    int adapterPosition = viewHolder.getAdapterPosition();
                    int adapterPosition2 = viewHolder2.getAdapterPosition();
                    pdfPages.add(adapterPosition, pdfPages.remove(adapterPosition2));
                    organizePagesAdapter.notifyItemMoved(adapterPosition2, adapterPosition);
                    return true;
                }

                @Override
                public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                    super.clearView(recyclerView, viewHolder);
                    StringBuilder sb = new StringBuilder();
                    sb.append("Page order after swap ");
                    ActivityOrganizePages organizePagesActivity = ActivityOrganizePages.this;
                    sb.append(organizePagesActivity.getOrganizedPages(organizePagesActivity.pdfPages).toString());
                }
            }).attachToRecyclerView(recycleOrganizePages);
        }
    }

    public class SaveOrganizedPages extends AsyncTask<Void, Integer, Void> {
        public int numPages = 0;
        public List<Integer> organizedPages;
        String organizedFilePath;

        public SaveOrganizedPages(List<Integer> list, ConstraintLayout constraintLayout) {
            organizedPages = list;
            numPages = list.size();
            mProgressView = constraintLayout;
            initProgressView();
            Utils.setLightStatusBar(context);
            btnCancelProgress.setOnClickListener(view -> {
                cancel(true);
                closeProgressBar(context);
            });
        }

        public void onPreExecute() {
            super.onPreExecute();
            progressBar.setMax(numPages);
            tvCurrentAction.setText(R.string.organizing);
            mProgressView.setVisibility(View.VISIBLE);
        }

        public Void doInBackground(Void... voidArr) {
            boolean z = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(ActivityMain.GRID_VIEW_ENABLED, false);
            try {
                File file = new File(allPdfDocuments);
                File file2 = new File(pdfFilePath);
                String name = file2.getName();
                organizedFilePath = allPdfDocuments + Utils.removePdfExtension(name) + "-Organized.pdf";
                if (!file.exists()) {
                    file.mkdirs();
                }
                PDFBoxResourceLoader.init(context);
                PDDocument load = PDDocument.load(file2);
                PDDocument pDDocument = new PDDocument();
                int i = 0;
                while (i < numPages && !isCancelled()) {
                    pDDocument.addPage(load.getPage(organizedPages.get(i) - 1));
                    i++;
                    publishProgress(i);
                }
                pDDocument.save(new File(organizedFilePath));
                load.close();
                pDDocument.close();
                if (z) {
                    Utils.generatePDFThumbnail(context, organizedFilePath);
                }
                MediaScannerConnection.scanFile(context, new String[]{organizedFilePath}, new String[]{"application/pdf"}, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        public void onProgressUpdate(Integer... numArr) {
            super.onProgressUpdate(numArr);
            updateDownloadingProgressPercent(numArr[0], numPages);
        }

        public void onPostExecute(Void r5) {
            super.onPostExecute(r5);
            if (GoogleAppLovinAds.adsdisplay) {
                GoogleAppLovinAds.showFullAds(ActivityOrganizePages.this, () -> {
                    GoogleAppLovinAds.allcount60.start();
                    tvCurrentAction.setText(R.string.done);
                    btnCancelProgress.setOnClickListener(null);
                    showInterstialAd(context, allPdfDocuments);
                    openPdfPath(context, context.getString(R.string.open_file), organizedFilePath, true);
                });
            } else {
                tvCurrentAction.setText(R.string.done);
                btnCancelProgress.setOnClickListener(null);
                showInterstialAd(context, allPdfDocuments);
                openPdfPath(context, context.getString(R.string.open_file), organizedFilePath, true);
            }

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