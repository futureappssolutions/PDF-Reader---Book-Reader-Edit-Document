package pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Ads.GoogleAppLovinAds;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.R;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Adapter.AdapterSelectPDF;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.customview.MaterialSearchView;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.data.DbHelper;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.GetSet.PdfDataType;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.utils.Utils;
import com.itextpdf.text.html.HtmlTags;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ActivitySelectPDF extends AppCompatActivity implements AdapterSelectPDF.OnSelectedPdfClickListener, AdapterSelectPDF.OnMultiSelectedPDFListener, MaterialSearchView.OnQueryTextListener {
    public String directoryPath;
    private boolean gridViewEnabled;
    private Boolean isDirectory;
    private Boolean isMultiSelect;
    private List<PdfDataType> mPdfDataTypeFiles;
    private MenuItem menuPdfGrid;
    private MenuItem menuPdfListView;
    private int numberOfColumns;
    private RecyclerView recycleSelectPdfFile;
    private MaterialSearchView searchForSelectPdf;
    private AdapterSelectPDF selectPDFAdapter;
    private SharedPreferences sharedPreferences;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_select_pdf);

        LinearLayout ll_banner = findViewById(R.id.ll_banner);
        GoogleAppLovinAds.showBannerAds(ActivitySelectPDF.this,ll_banner);



        searchForSelectPdf = findViewById(R.id.searchBarPdf);
        searchForSelectPdf.setOnQueryTextListener(this);

        setSupportActionBar(findViewById(R.id.toolbar));
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        isMultiSelect = intent.getBooleanExtra(ActivityPDFTools.MULTI_SELECTION, false);
        isDirectory = intent.getBooleanExtra(ActivityPDFTools.IS_DIRECTORY, false);
        directoryPath = intent.getStringExtra(ActivityPDFTools.DIRECTORY_PATH);
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences = defaultSharedPreferences;
        gridViewEnabled = defaultSharedPreferences.getBoolean(ActivityMain.GRID_VIEW_ENABLED, false);
        numberOfColumns = sharedPreferences.getInt(ActivityMain.GRID_VIEW_NUM_OF_COLUMNS, 2);
        if (!isDirectory) {
            loadSelectedPDFFiles();
        } else {
            loadPDFsFromDirectory(directoryPath);
        }
    }

    @Override
    public void onBackPressed() {
        if (searchForSelectPdf.isSearchOpen()) {
            searchForSelectPdf.closeSearchingPdfData();
        } else {
            super.onBackPressed();
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_select_pdf, menu);
        menuPdfListView = menu.findItem(R.id.menuPdfListView);
        MenuItem findItem = menu.findItem(R.id.menuPdfGrid);
        menuPdfGrid = findItem;
        findItem.getSubMenu().clearHeader();
        if (gridViewEnabled) {
            menuPdfListView.setVisible(true);
            menuPdfGrid.setVisible(false);
        } else {
            menuPdfListView.setVisible(false);
            menuPdfGrid.setVisible(true);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NonConstantResourceId")
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.action_by_date_modified:
                pdfSortByDateModified();
                break;
            case R.id.action_by_name:
                pdfSortByName();
                break;
            case R.id.action_by_size:
                pdfSortBySize();
                break;
            case R.id.menuGridFiveColumns:
                selectPdfShowGridView(5);
                break;
            case R.id.menuGridFourColumns:
                selectPdfShowGridView(4);
                break;
            case R.id.menuGridSixColumns:
                selectPdfShowGridView(6);
                break;
            case R.id.menuGridThreeColumns:
                selectPdfShowGridView(3);
                break;
            case R.id.menuGridTwoColumns:
                selectPdfShowGridView(2);
                break;
            case R.id.menuPdfListView:
                selectPdfShowListView();
                break;
            case R.id.menuSearchPdfBookmark:
                searchForSelectPdf.openPdfSearchData();
                break;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onSelectedPdfClicked(PdfDataType pdfDataType) {
        if (!isDirectory) {
            ArrayList<String> arrayList = new ArrayList<>();
            arrayList.add(pdfDataType.getAbsolutePath());
            sendPdfResults(arrayList);
            return;
        }
        Intent intent = new Intent(this, ActivityPDFViewer.class);
        intent.putExtra(ActivityMain.PDF_LOCATION, pdfDataType.getAbsolutePath());
        startActivity(intent);
    }

    @Override
    public void onMultiSelectedPDF(ArrayList<String> arrayList) {
        Intent intent = new Intent(this, ActivityOrganizeMergePDF.class);
        intent.putStringArrayListExtra(ActivityPDFTools.PDF_PATHS, arrayList);
        if (!TextUtils.isEmpty(getIntent().getStringExtra(ActivityPDFTools.CALLING_ACTIVITY))) {
            sendPdfResults(arrayList);
        } else {
            if (GoogleAppLovinAds.adsdisplay) {
                GoogleAppLovinAds.showFullAds(ActivitySelectPDF.this, () -> {
                    GoogleAppLovinAds.allcount60.start();
                    startActivity(intent);
                });
            } else {
                startActivity(intent);
            }
        }
    }

    public void loadSelectedPDFFiles() {
        mPdfDataTypeFiles = DbHelper.getInstance(this).getAllPdfs();
        selectPDFAdapter = new AdapterSelectPDF(mPdfDataTypeFiles, this, isMultiSelect);
        recycleSelectPdfFile = findViewById(R.id.recycleSelectPdfFile);
        if (gridViewEnabled) {
            setPdfInGridView(this, recycleSelectPdfFile, numberOfColumns);
        } else {
            setPdfInListView(this, recycleSelectPdfFile);
        }
        recycleSelectPdfFile.setAdapter(selectPDFAdapter);
    }

    public void loadPDFsFromDirectory(String str) {
        mPdfDataTypeFiles = DbHelper.getInstance(this).getAllPdfFromDirectory(str);
        selectPDFAdapter = new AdapterSelectPDF(mPdfDataTypeFiles, this, isMultiSelect);
        recycleSelectPdfFile = findViewById(R.id.recycleSelectPdfFile);
        if (gridViewEnabled) {
            setPdfInGridView(this, recycleSelectPdfFile, numberOfColumns);
        } else {
            setPdfInListView(this, recycleSelectPdfFile);
        }
        recycleSelectPdfFile.setAdapter(selectPDFAdapter);
    }

    public void sendPdfResults(ArrayList<String> arrayList) {
        Intent intent = new Intent();
        intent.putStringArrayListExtra(ActivityPDFTools.PDF_PATHS, arrayList);
        setResult(-1, intent);
        finish();
    }

    public void searchPDFFiles(String str) {
        ArrayList<PdfDataType> arrayList = new ArrayList<>();
        for (PdfDataType pdfDataType : mPdfDataTypeFiles) {
            if (pdfDataType.getName().toLowerCase().contains(str.toLowerCase())) {
                arrayList.add(pdfDataType);
            }
            selectPDFAdapter.filter(arrayList);
        }
    }

    @Override
    public boolean onQueryTextSubmit(String str) {
        searchPDFFiles(str);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String str) {
        searchPDFFiles(str);
        return true;
    }

    public void selectPdfShowListView() {
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putBoolean(ActivityMain.GRID_VIEW_ENABLED, false);
        edit.apply();
        setPdfInListView(this, recycleSelectPdfFile);
        selectPDFAdapter = new AdapterSelectPDF(mPdfDataTypeFiles, this, isMultiSelect);
        recycleSelectPdfFile.setAdapter(selectPDFAdapter);
        menuPdfListView.setVisible(false);
        menuPdfGrid.setVisible(true);
    }

    public void selectPdfShowGridView(int i) {
        new Utils.BackgroundGenerateThumbnails(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putBoolean(ActivityMain.GRID_VIEW_ENABLED, true);
        edit.putInt(ActivityMain.GRID_VIEW_NUM_OF_COLUMNS, i);
        edit.apply();
        setPdfInGridView(this, recycleSelectPdfFile, i);
        selectPDFAdapter = new AdapterSelectPDF(mPdfDataTypeFiles, this, isMultiSelect);
        recycleSelectPdfFile.setAdapter(selectPDFAdapter);
        menuPdfListView.setVisible(true);
        menuPdfGrid.setVisible(false);
    }

    public void setPdfInGridView(Context context, RecyclerView recyclerView, int i) {
        float valueOf = getResources().getDisplayMetrics().density;
        GridLayoutManager gridLayoutManager = new GridLayoutManager(context, i, RecyclerView.VERTICAL, false);
        recyclerView.setPadding((int) (valueOf * 4.0f), (int) (valueOf * 4.0f), (int) (valueOf * 6.0f), (int) (valueOf * 5.0f));
        recyclerView.setLayoutManager(gridLayoutManager);
    }

    @SuppressLint("ResourceType")
    public void setPdfInListView(Context context, RecyclerView recyclerView) {
        recyclerView.setPadding(0, 0, 0, 0);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
    }

    public void pdfSortByName() {
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putString(DbHelper.SORT_BY, "name");
        edit.apply();
        mPdfDataTypeFiles = DbHelper.getInstance(this).getAllPdfs();
        selectPDFAdapter.pdfDataUpdate(mPdfDataTypeFiles);
    }

    public void pdfSortByDateModified() {
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putString(DbHelper.SORT_BY, "date modified");
        edit.apply();
        mPdfDataTypeFiles = DbHelper.getInstance(this).getAllPdfs();
        selectPDFAdapter.pdfDataUpdate(mPdfDataTypeFiles);
    }

    public void pdfSortBySize() {
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putString(DbHelper.SORT_BY, HtmlTags.SIZE);
        edit.apply();
        mPdfDataTypeFiles = DbHelper.getInstance(this).getAllPdfs();
        selectPDFAdapter.pdfDataUpdate(mPdfDataTypeFiles);
    }
}