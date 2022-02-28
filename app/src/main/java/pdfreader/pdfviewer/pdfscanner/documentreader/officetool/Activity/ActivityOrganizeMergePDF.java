package pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Activity;

import android.animation.Animator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Ads.GoogleAppLovinAds;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.R;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Adapter.AdapterMergeOrganalPDF;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.utils.PDFTools;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ActivityOrganizeMergePDF extends AppCompatActivity {
    public static String ORGANIZE_MERGE_PAGES_TIP = "prefs_organize_merge_pages";
    public Context context;
    public List<File> mPDFFiles = new ArrayList<>();
    public AdapterMergeOrganalPDF organizePagesAdapter;
    public ConstraintLayout progressMain;
    public ProgressBar progressMergePdf;
    public RelativeLayout rLayTapMoreOptions;
    public RecyclerView recycleOrganizePages;
    public SharedPreferences sharedPreferences;
    public int REQUEST_CODE_ADD_FILE = 62;
    public FloatingActionButton floatBtnSave;
    public boolean showOrganizePagesTip;
    public String allPdfDocuments;
    public String allPdfPictureDir;
    public ImageView imgTapClose;
    public List<String> pdfFilePaths;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_organize_merge_pdf);
        setStatusBar();

        LinearLayout ll_banner = findViewById(R.id.ll_banner);
        FrameLayout fl_native = findViewById(R.id.fl_native);

        GoogleAppLovinAds.showBannerAds(ActivityOrganizeMergePDF.this,ll_banner);
        GoogleAppLovinAds.showNativeAds(ActivityOrganizeMergePDF.this,fl_native);


        String file = Environment.getExternalStorageDirectory().toString();
        allPdfPictureDir = file + "/Pictures/AllPdf/tmp/";
        allPdfDocuments = file + "/Documents/AllPdf/";

        setSupportActionBar(findViewById(R.id.toolbar));
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        context = this;

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        showOrganizePagesTip = sharedPreferences.getBoolean(ORGANIZE_MERGE_PAGES_TIP, true);
        recycleOrganizePages = findViewById(R.id.recycleOrganizePages);
        progressMergePdf = findViewById(R.id.progressMergePdf);
        progressMain = findViewById(R.id.progressMain);
        floatBtnSave = findViewById(R.id.floatBtnSave);
        rLayTapMoreOptions = findViewById(R.id.rLayTapMoreOptions);
        imgTapClose = findViewById(R.id.imgTapClose);

        imgTapClose.setOnClickListener(view -> {
            rLayTapMoreOptions.setVisibility(View.GONE);
            rLayTapMoreOptions.animate().translationY((float) (-rLayTapMoreOptions.getHeight())).alpha(0.0f).setListener(new Animator.AnimatorListener() {

                public void onAnimationCancel(Animator animator) {
                }

                public void onAnimationRepeat(Animator animator) {
                }

                public void onAnimationStart(Animator animator) {
                }

                public void onAnimationEnd(Animator animator) {
                    rLayTapMoreOptions.setVisibility(View.GONE);
                    SharedPreferences.Editor edit = sharedPreferences.edit();
                    edit.putBoolean(ActivityOrganizeMergePDF.ORGANIZE_MERGE_PAGES_TIP, false);
                    edit.apply();
                }
            });
        });

        pdfFilePaths = getIntent().getStringArrayListExtra(ActivityPDFTools.PDF_PATHS);

        if (showOrganizePagesTip) {
            rLayTapMoreOptions.setVisibility(View.GONE);
        } else {
            rLayTapMoreOptions.setVisibility(View.GONE);
        }

        new LoadMergePdfThumbAyn().execute(pdfFilePaths);

        floatBtnSave.setOnClickListener(view -> {
            organizePagesAdapter.finishActionMode();
            if (organizePagesAdapter.getPDFsToMerge().size() >= 2) {
                getMergePDFFileList(organizePagesAdapter.getPDFsToMerge());
                return;
            }
            Toast.makeText(context, R.string.at_least_two_files, Toast.LENGTH_LONG).show();
        });
    }

    @Override
    public void onBackPressed() {
        if (progressMain.findViewById(R.id.imgCloseProgress).getVisibility() == View.VISIBLE) {
            closeDownloadingProgressBar(progressMain);
        } else if (progressMain.getVisibility() != View.VISIBLE) {
            super.onBackPressed();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Utils.deletePdfFiles(allPdfPictureDir);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_organize_pages, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.action_add_file) {
            Intent intent = new Intent(this, ActivitySelectPDF.class);
            intent.putExtra(ActivityPDFTools.MULTI_SELECTION, true);
            intent.putExtra(ActivityPDFTools.CALLING_ACTIVITY, ActivityOrganizeMergePDF.class.getSimpleName());
            startActivityForResult(intent, REQUEST_CODE_ADD_FILE);
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
        if (intent != null) {
            ArrayList<String> stringArrayListExtra = intent.getStringArrayListExtra(ActivityPDFTools.PDF_PATHS);
            pdfFilePaths = stringArrayListExtra;
            new LoadMergePdfThumbAyn().execute(stringArrayListExtra);
            return;
        }
        Toast.makeText(this, "An error occured", Toast.LENGTH_SHORT).show();
    }

    public void getMergePDFFileList(List<File> list) {
        final ArrayList<String> arrayList = new ArrayList<>();
        for (File file : list) {
            arrayList.add(file.getAbsolutePath());
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        float f = context.getResources().getDisplayMetrics().density;
        final EditText editText = new EditText(context);
        editText.setText("Merged" + System.currentTimeMillis());
        editText.setSelectAllOnFocus(true);
        DialogInterface.OnClickListener onClickListener = null;
        builder.setTitle(R.string.enter_file_name).setPositiveButton(R.string.ok, onClickListener).setNegativeButton(R.string.cancel, onClickListener);
        final AlertDialog create = builder.create();
        int i = (int) (24.0f * f);
        create.setView(editText, i, (int) (8.0f * f), i, (int) (f * 5.0f));
        create.show();
        create.getButton(-1).setOnClickListener(view -> {
            String obj = editText.getText().toString();
            if (Utils.isFileNameValid(obj)) {
                create.dismiss();
                new PDFTools();
                new PDFTools.MergePDFFiles(context, arrayList, obj, progressMain).execute();
                return;
            }
            editText.setError(getString(R.string.invalid_file_name));
        });
    }

    public void closeDownloadingProgressBar(View view) {
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

    public class LoadMergePdfThumbAyn extends AsyncTask<List<String>, Void, Void> {
        public LoadMergePdfThumbAyn() {
        }

        public void onPreExecute() {
            super.onPreExecute();
        }

        public Void doInBackground(List<String>... listArr) {
            List<String> list = listArr[0];
            int size = list.size();
            for (int i = 0; i < size; i++) {
                String str = list.get(i);
                if (!Utils.isThumbnailPresent(context, str)) {
                    Utils.generatePDFThumbnail(context, str);
                }
                mPDFFiles.add(new File(str));
            }
            return null;
        }

        public void onPostExecute(Void r6) {
            super.onPostExecute(r6);
            organizePagesAdapter = new AdapterMergeOrganalPDF(context, mPDFFiles);
            recycleOrganizePages.setLayoutManager(new GridLayoutManager(context, Utils.isTablet(context) ? 6 : 2, RecyclerView.VERTICAL, false));
            progressMergePdf.setVisibility(View.GONE);
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
                    mPDFFiles.add(adapterPosition, mPDFFiles.remove(adapterPosition2));
                    organizePagesAdapter.notifyItemMoved(adapterPosition2, adapterPosition);
                    return true;
                }

                @Override
                public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                    super.clearView(recyclerView, viewHolder);
                }
            }).attachToRecyclerView(recycleOrganizePages);
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
