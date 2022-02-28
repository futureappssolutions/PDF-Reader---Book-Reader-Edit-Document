package pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Activity;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ShareCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Ads.GoogleAppLovinAds;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.R;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Adapter.AdapterExtractTextsPages;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.GetSet.PDFPage;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.utils.Utils;

import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.text.PDFTextStripper;
import com.tom_roush.pdfbox.util.PDFBoxResourceLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class ActivityExtractTextsPages extends AppCompatActivity {
    public static String ORGANIZE_PAGES_TIP = "prefs_organize_pages";
    public String allPdfDocuments;
    public String allPdfPictureDir;
    public Button btnCancelProgress;
    public Button btnOpenPdfFile;
    public Context context;
    public AdapterExtractTextsPages extractTextsPagesAdapter;
    public FloatingActionButton floatBtnSave;
    public ImageView imgCloseProgress;
    public ConstraintLayout mProgressView;
    public String pdfFilePath;
    public List<PDFPage> pdfPages = new ArrayList<>();
    public ProgressBar progressDownloading;
    public ConstraintLayout progressMain;
    public ProgressBar progressOrganizePages;
    public RelativeLayout rLayInfoTapMoreOptions;
    public RecyclerView recycleOrganizePages;
    public SharedPreferences sharedPreferences;
    public TextView tvCurrentAction;
    public TextView tvDownloadPercent;
    public TextView tvSavedPdfPath;
    public ImageView imgPdfSuccess;
    public ImageView imgTapClose;
    boolean showOrganizePagesTip;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_extract_texts_pages);

        LinearLayout ll_banner = findViewById(R.id.ll_banner);
        FrameLayout fl_native = findViewById(R.id.fl_native);

        GoogleAppLovinAds.showBannerAds(ActivityExtractTextsPages.this,ll_banner);
        GoogleAppLovinAds.showNativeAds(ActivityExtractTextsPages.this,fl_native);


        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        context = this;

        allPdfPictureDir = Environment.getExternalStorageDirectory() + "/Pictures/AllPdf/tmp/";
        allPdfDocuments = Environment.getExternalStorageDirectory() + "/Documents/AllPdf/";

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        showOrganizePagesTip = sharedPreferences.getBoolean(ORGANIZE_PAGES_TIP, true);

        recycleOrganizePages = findViewById(R.id.recycleOrganizePages);
        progressOrganizePages = findViewById(R.id.progressOrganizePages);
        floatBtnSave = findViewById(R.id.floatBtnSave);
        progressMain = findViewById(R.id.progressMain);
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
                    edit.putBoolean(ActivityExtractTextsPages.ORGANIZE_PAGES_TIP, false);
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
        new LoadPdfPageThumbPhotos().execute(pdfFilePath);

        floatBtnSave.setOnClickListener(view -> {
            extractTextsPagesAdapter.finishActionBarMode(false);
            if (extractTextsPagesAdapter.getSelectedPages().size() > 0) {
                new ExtractPdfTextPageAyn(context, pdfFilePath, extractTextsPagesAdapter.getSelectedPages(), progressMain).execute();
                return;
            }
            Toast.makeText(context, R.string.select_at_least_one_page, Toast.LENGTH_LONG).show();
        });
    }

    public void initProgressView() {
        tvDownloadPercent = mProgressView.findViewById(R.id.tvDownloadPercent);
        tvCurrentAction = mProgressView.findViewById(R.id.tvCurrentAction);
        progressDownloading = mProgressView.findViewById(R.id.progressDownloading);
        tvSavedPdfPath = mProgressView.findViewById(R.id.tvSavedPdfPath);
        imgPdfSuccess = mProgressView.findViewById(R.id.imgPdfSuccess);
        btnOpenPdfFile = mProgressView.findViewById(R.id.btnOpenPdfFile);
        btnCancelProgress = mProgressView.findViewById(R.id.btnCancelProgress);
        imgCloseProgress = mProgressView.findViewById(R.id.imgCloseProgress);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Utils.deletePdfFiles(allPdfPictureDir);
    }

    public void finishProcessBar(Context context2, String str) {
        tvDownloadPercent.setVisibility(View.GONE);
        progressDownloading.setVisibility(View.GONE);
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
        progressDownloading.setVisibility(View.VISIBLE);
        tvDownloadPercent.setVisibility(View.VISIBLE);
        btnCancelProgress.setVisibility(View.VISIBLE);
        progressDownloading.setProgress(0);
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

    public void updateProgressBarPercentage(int i, int i2) {
        int i3 = ((int) (((float) i) * 100.0f)) / i2;
        tvDownloadPercent.setText(i3 + "%");
        progressDownloading.setProgress(i);
    }

    public void showInterstialAd(Context context2, String str) {
        finishProcessBar(context2, str);
    }

    @SuppressLint("WrongConstant")
    public void openPdfPath(final Context context2, String str, final String str2) {
        btnOpenPdfFile.setText(str);
        btnOpenPdfFile.setOnClickListener(view -> {
            Uri uriForFile = FileProvider.getUriForFile(context2, getPackageName() + ".provider", new File(str2));
            Intent intent = ShareCompat.IntentBuilder.from((Activity) context2).setType(context2.getContentResolver().getType(uriForFile)).setStream(uriForFile).getIntent();
            intent.setData(uriForFile);
            intent.addFlags(1);
            intent.setAction("android.intent.action.VIEW");
            intent.addFlags(268435456);
            if (intent.resolveActivity(context2.getPackageManager()) != null) {
                context2.startActivity(intent);
            } else {
                Toast.makeText(context2, R.string.no_proper_app_for_opening_text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void removeProgresIndeterminate(Context context2, final ProgressBar progressBar) {
        ((Activity) context2).runOnUiThread(() -> progressBar.setIndeterminate(false));
    }

    public class ExtractPdfTextPageAyn extends AsyncTask<Void, Integer, Void> {
        String errorMessage;
        String extractedTextDir;
        String extractedTextFilePath;
        Context mContext;
        int mNumPages;
        String pdfPath;
        List<Integer> selectedPages;
        boolean textExtractSuccess = true;

        public ExtractPdfTextPageAyn(Context context, String str, List<Integer> list, ConstraintLayout constraintLayout) {
            mContext = context;
            pdfPath = str;
            selectedPages = list;
            mNumPages = list.size();
            mProgressView = constraintLayout;
            initProgressView();
            Utils.setLightStatusBar(context);
            btnCancelProgress.setOnClickListener(view -> {
                cancel(true);
                closeProgressBar(mContext);
            });
        }

        public void onPreExecute() {
            super.onPreExecute();
            progressDownloading.setIndeterminate(true);
            progressDownloading.setMax(mNumPages);
            tvCurrentAction.setText(R.string.extracting);
            mProgressView.setVisibility(View.VISIBLE);
        }

        public Void doInBackground(Void... voidArr) {
            extractedTextDir = allPdfDocuments + "Texts/";
            File file = new File(extractedTextDir);
            if (!file.exists()) {
                file.mkdirs();
            }
            try {
                File file2 = new File(pdfPath);
                String name = file2.getName();
                extractedTextFilePath = extractedTextDir + Utils.removePdfExtension(name) + ".txt";
                PDFBoxResourceLoader.init(mContext);
                PDDocument load = PDDocument.load(file2);
                if (!load.isEncrypted()) {
                    PDFTextStripper pDFTextStripper = new PDFTextStripper();
                    StringBuilder sb = new StringBuilder();
                    String pageEnd = pDFTextStripper.getPageEnd();
                    removeProgresIndeterminate(mContext, progressDownloading);
                    int i = 0;
                    while (true) {
                        if (i >= mNumPages) {
                            break;
                        } else if (isCancelled()) {
                            break;
                        } else {
                            int intValue = selectedPages.get(i) + 1;
                            pDFTextStripper.setStartPage(intValue);
                            pDFTextStripper.setEndPage(intValue);
                            sb.append(pDFTextStripper.getText(load) + pageEnd);
                            i++;
                            publishProgress(i);
                        }
                    }
                    FileOutputStream fileOutputStream = new FileOutputStream(new File(extractedTextFilePath));
                    fileOutputStream.write(sb.toString().getBytes());
                    load.close();
                    fileOutputStream.close();
                    return null;
                }
                errorMessage = mContext.getString(R.string.file_protected_unprotect);
                textExtractSuccess = false;
                return null;
            } catch (Exception e) {
                e.printStackTrace();
                errorMessage = mContext.getString(R.string.extraction_failed);
                textExtractSuccess = false;
                return null;
            }
        }

        public void onProgressUpdate(Integer... numArr) {
            super.onProgressUpdate(numArr);
            updateProgressBarPercentage(numArr[0], mNumPages);
        }

        public void onPostExecute(Void r4) {
            super.onPostExecute(r4);
            if (GoogleAppLovinAds.adsdisplay) {
                GoogleAppLovinAds.showFullAds((Activity) context, () -> {
                    GoogleAppLovinAds.allcount60.start();
                    tvCurrentAction.setText(R.string.done);
                    btnCancelProgress.setOnClickListener(null);
                    showInterstialAd(mContext, extractedTextDir);
                    openPdfPath(mContext, mContext.getString(R.string.open_file), extractedTextFilePath);
                    if (!textExtractSuccess) {
                        Toast.makeText(mContext, errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                tvCurrentAction.setText(R.string.done);
                btnCancelProgress.setOnClickListener(null);
                showInterstialAd(mContext, extractedTextDir);
                openPdfPath(mContext, mContext.getString(R.string.open_file), extractedTextFilePath);
                if (!textExtractSuccess) {
                    Toast.makeText(mContext, errorMessage, Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    public class LoadPdfPageThumbPhotos extends AsyncTask<String, Void, Void> {
        public LoadPdfPageThumbPhotos() {
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
                        Toast.makeText(context, R.string.failed_low_memory, Toast.LENGTH_LONG).show();
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
            extractTextsPagesAdapter = new AdapterExtractTextsPages(context, pdfPages);
            recycleOrganizePages.setLayoutManager(new GridLayoutManager(context, Utils.isTablet(context) ? 6 : 3, RecyclerView.VERTICAL, false));
            progressOrganizePages.setVisibility(View.GONE);
            recycleOrganizePages.setAdapter(extractTextsPagesAdapter);
            floatBtnSave.setVisibility(View.VISIBLE);
        }
    }
}
