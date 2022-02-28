package pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ActionMenuView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.preference.PreferenceManager;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.util.Constants;
import com.github.barteksc.pdfviewer.util.FitPolicy;

import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Ads.GoogleAppLovinAds;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.R;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.data.DbHelper;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Fragments.FragmentSettings;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Fragments.FragmentTableContents;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.utils.Utils;
import com.shockwave.pdfium.PdfPasswordException;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.encryption.InvalidPasswordException;

import java.io.File;

public class ActivityPDFViewer extends AppCompatActivity implements ActionMenuView.OnMenuItemClickListener {
    static final String CONTENTS_PDF_PATH = "com.example.pdfreader.CONTENTS_PDF_PATH";
    static final String PAGE_NUMBER = "com.pdftools.pdfreader.pdfviewer.PAGE_NUMBER";
    private final Handler mHideHandler = new Handler(Looper.myLooper());
    public TextView tvPdfPageNumbers;
    ActionMenuView bottomMenuAction;
    int colorPrimaryDark;
    int colorPrimaryDarkNight;
    Context context;
    DbHelper dbHelper;
    View divider;
    String filePath;
    FitPolicy fitPolicy;
    int flags;
    LinearLayout layBottomMenuBar;
    private final Runnable mShowPart2Runnable = new Runnable() {
        public void run() {
            ActionBar supportActionBar = getSupportActionBar();
            if (supportActionBar != null) {
                supportActionBar.show();
            }
            layBottomMenuBar.setVisibility(View.VISIBLE);
        }
    };
    ActionBar mActionBar;
    String mPassword = "";
    boolean nightModeEnabled;
    OnPageChangeListener onPageChangeListener = new OnPageChangeListener() {
        @Override
        public void onPageChanged(int i, int i2) {
            tvPdfPageNumbers.setText((i + 1) + " / " + i2);
        }
    };
    int pageNumber;
    String pdfFileLocation;
    PDFView pdfView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        public void run() {
            pdfView.setSystemUiVisibility(4615);
        }
    };
    ProgressBar progressOpenPdf;
    OnLoadCompleteListener onLoadCompleteListener = new OnLoadCompleteListener() {
        @Override
        public void loadComplete(int i) {
            progressOpenPdf.setVisibility(View.GONE);
            tvPdfPageNumbers.setVisibility(View.VISIBLE);
        }
    };
    SharedPreferences sharedPreferences;
    boolean swipeHorizontalEnabled;
    Toolbar toolbar;
    Toolbar toolbarBottom;
    Uri uri;
    OnErrorListener onErrorListener = new OnErrorListener() {
        @Override
        public void onError(Throwable th) {
            if (th instanceof PdfPasswordException) {
                enterPasswordDialog();
                return;
            }
            Toast.makeText(ActivityPDFViewer.this, th.getMessage(), Toast.LENGTH_LONG).show();
            progressOpenPdf.setVisibility(View.GONE);
        }
    };
    View view;
    private boolean AUTO_HIDE;
    private Menu mMenu;
    private boolean mVisible;
    private final Runnable mHideRunnable = this::hide;
    private boolean rememberLastPage;
    private boolean showRemoveAds;
    private boolean stayAwake;
    private Menu topMenu;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_pdf_viewer);

        LinearLayout ll_banner = findViewById(R.id.ll_banner);
        GoogleAppLovinAds.showBannerAds(ActivityPDFViewer.this,ll_banner);

        toolbar = findViewById(R.id.toolbar);
        toolbarBottom = findViewById(R.id.toolbarBottom);
        progressOpenPdf = findViewById(R.id.progressOpenPdf);
        tvPdfPageNumbers = findViewById(R.id.tvPdfPageNumbers);
        pdfView = findViewById(R.id.pdfView);
        bottomMenuAction = findViewById(R.id.bottomMenuAction);
        divider = findViewById(R.id.divider);
        layBottomMenuBar = findViewById(R.id.layBottomMenuBar);

        bottomMenuAction.setOnMenuItemClickListener(this);
        context = this;
        setSupportActionBar(toolbar);
        mActionBar = getSupportActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        stayAwake = sharedPreferences.getBoolean(FragmentSettings.KEY_PREFS_STAY_AWAKE, true);
        rememberLastPage = sharedPreferences.getBoolean(FragmentSettings.KEY_PREFS_REMEMBER_LAST_PAGE, true);
        int i = 0;
        AUTO_HIDE = sharedPreferences.getBoolean("prefs_auto_full_screen", false);
        swipeHorizontalEnabled = sharedPreferences.getBoolean("prefs_swipe_horizontal_enabled", false);
        nightModeEnabled = sharedPreferences.getBoolean("prefs_night_mode_enabled", false);

        View decorView = ((Activity) context).getWindow().getDecorView();
        view = decorView;
        flags = decorView.getSystemUiVisibility();
        colorPrimaryDark = context.getResources().getColor(R.color.white);
        colorPrimaryDarkNight = context.getResources().getColor(R.color.colorPrimaryDarkNight);
        Constants.THUMBNAIL_RATIO = 0.7f;
        Intent intent = getIntent();
        pdfFileLocation = intent.getStringExtra(ActivityMain.PDF_LOCATION);
        showRemoveAds = intent.getBooleanExtra(ActivityMain.SHOW_REMOVE_ADS, false);
        uri = intent.getData();
        dbHelper = DbHelper.getInstance(this);
        pdfView.setKeepScreenOn(stayAwake);
        if (rememberLastPage) {
            i = dbHelper.getLastOpenedPage(pdfFileLocation);
        }
        pageNumber = i;
        fitPolicy = (Utils.isTablet(this) || swipeHorizontalEnabled) ? FitPolicy.HEIGHT : FitPolicy.WIDTH;
        loadSelectedPdfFile(mPassword, pageNumber, swipeHorizontalEnabled, nightModeEnabled, fitPolicy);
        pdfView.setOnClickListener(view -> toggle());
    }

    @Override
    public void onPostCreate(Bundle bundle) {
        super.onPostCreate(bundle);
        if (showRemoveAds) {
            delayedHide(9000);
        } else {
            delayedHide(6000);
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_pdf_viewer, menu);
        topMenu = menu;
        mMenu = bottomMenuAction.getMenu();
        getMenuInflater().inflate(R.menu.activity_pdf_viewer_bottom, mMenu);
        MenuItem findItem = mMenu.findItem(R.id.itemBookmarkView);
        MenuItem findItem2 = mMenu.findItem(R.id.itemThemeNightMode);
        setupPdfListSwipeIcons(findItem, swipeHorizontalEnabled);
        setNightModeThemeIcons(findItem2, nightModeEnabled);
        return true;
    }

    @Override
    public void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
        if (i == 7 && i2 == -1) {
            pdfView.jumpTo(intent.getIntExtra(PAGE_NUMBER, pdfView.getCurrentPage()) - 1, true);
        }
    }

    @SuppressLint("NonConstantResourceId")
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        int itemId = menuItem.getItemId();
        if (itemId != R.id.itemPrint) {
            switch (itemId) {
                case R.id.itemShare:
                    sharePdf();
                    break;
                case R.id.itemSharePicture:
                    sharePdfAsPicture();
                    break;
            }
        } else {
            printPdf();
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onDestroy() {
        sharedPreferences.edit().putInt(FragmentTableContents.SAVED_STATE, 0).apply();
        if (rememberLastPage && !TextUtils.isEmpty(pdfFileLocation)) {
            dbHelper.addLastOpenedPage(filePath, pdfView.getCurrentPage());
        }
        super.onDestroy();
    }

    public void loadSelectedPdfFile(String str, int i, boolean z, boolean z2, FitPolicy fitPolicy2) {
        if (uri != null) {
            try {
                filePath = uri.getPath();
                mActionBar.setTitle(new File(filePath).getName());
            } catch (Exception e) {
                mActionBar.setTitle("View PDF");
                e.printStackTrace();
            }
            pdfView.fromUri(uri).password(str).enableAnnotationRendering(true).pageFitPolicy(fitPolicy2).spacing(6).defaultPage(i).swipeHorizontal(z).autoSpacing(z).pageFling(z).pageSnap(z).nightMode(z2).onPageChange(onPageChangeListener).onLoad(onLoadCompleteListener).onError(onErrorListener).load();
        } else if (!TextUtils.isEmpty(pdfFileLocation)) {
            String str2 = pdfFileLocation;
            filePath = str2;
            File file = new File(str2);
            mActionBar.setTitle(file.getName());
            pdfView.fromFile(file).password(str).enableAnnotationRendering(true).pageFitPolicy(fitPolicy2).spacing(6).defaultPage(i).swipeHorizontal(z).autoSpacing(z).pageFling(z).pageSnap(z).nightMode(z2).onPageChange(onPageChangeListener).onLoad(onLoadCompleteListener).onError(onErrorListener).load();
            dbHelper.addRecentPDF(file.getAbsolutePath());
        }
    }

    public String getName(Uri uri2) {
        Cursor query = getContentResolver().query(uri2, null, null, null, null);
        int columnIndex = query.getColumnIndex("_display_name");
        query.moveToFirst();
        String string = query.getString(columnIndex);
        query.getColumnNames();
        query.close();
        return string;
    }

    public void showShareAsPicture(Uri uri2) {
        Intent intent = new Intent(this, ActivityShareAsPicture.class);
        intent.putExtra("com.example.pdfreader.PDF_PATH", uri2.toString());
        startActivity(intent);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        int itemId = menuItem.getItemId();
        if (itemId != R.id.itemThemeNightMode) {
            switch (itemId) {
                case R.id.itemBookmark:
                    addPageBookmark(this, filePath, pdfView.getCurrentPage() + 1);
                    return false;
                case R.id.itemBookmarkView:
                    bookMarkPDFView(menuItem);
                    return false;
                case R.id.itemPageToJump:
                    jumpToPageOfPdf();
                    return false;
                case R.id.itemPdfContents:
                    showPdfContents(filePath);
                    return false;
                case R.id.itemPdfToolsSetting:
                    showPdfTools();
                    return false;
                default:
                    return false;
            }
        } else {
            changeThemeNightMode(menuItem);
            return false;
        }
    }

    public void bookMarkPDFView(MenuItem menuItem) {
        swipeHorizontalEnabled = sharedPreferences.getBoolean("prefs_swipe_horizontal_enabled", false);
        boolean z = sharedPreferences.getBoolean("prefs_night_mode_enabled", false);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        setupPdfListSwipeIcons(menuItem, !swipeHorizontalEnabled);
        if (swipeHorizontalEnabled) {
            loadSelectedPdfFile(mPassword, pdfView.getCurrentPage(), !swipeHorizontalEnabled, z, FitPolicy.WIDTH);
            edit.putBoolean("prefs_swipe_horizontal_enabled", !swipeHorizontalEnabled).apply();
            Toast.makeText(context, "Vertical swipe enabled", Toast.LENGTH_SHORT).show();
            return;
        }
        loadSelectedPdfFile(mPassword, pdfView.getCurrentPage(), !swipeHorizontalEnabled, z, FitPolicy.HEIGHT);
        edit.putBoolean("prefs_swipe_horizontal_enabled", !swipeHorizontalEnabled).apply();
        Toast.makeText(context, "Horizontal swipe enabled", Toast.LENGTH_SHORT).show();
    }

    public void addPageBookmark(final Context context2, final String str, final int i) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context2);
        final EditText editText = new EditText(context2);
        editText.setHint(R.string.enter_title);
        float f = context2.getResources().getDisplayMetrics().density;
        builder.setTitle(R.string.add_bookmark).setPositiveButton(R.string.ok, (dialogInterface, i1) -> {
            DbHelper.getInstance(context2).addBookmark(str, TextUtils.isEmpty(editText.getText().toString()) ? getString(R.string.bookmark) : editText.getText().toString(), i1);
            Toast.makeText(context2, getString(R.string.page) + " " + i1 + " " + getString(R.string.bookmark_added), Toast.LENGTH_SHORT).show();
        }).setNegativeButton(R.string.cancel, null);
        AlertDialog create = builder.create();
        int i2 = (int) (24.0f * f);
        create.setView(editText, i2, (int) (8.0f * f), i2, (int) (f * 5.0f));
        create.show();
    }

    public void showPdfContents(String str) {
        Intent intent = new Intent(this, ActivityContents.class);
        intent.putExtra(CONTENTS_PDF_PATH, str);
        startActivityForResult(intent, 7);
    }

    public void jumpToPageOfPdf() {
        float f = context.getResources().getDisplayMetrics().density;
        final EditText editText = new EditText(context);
        editText.setHint(R.string.enter_page_number);
        editText.setInputType(3);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.jump_to_page).setPositiveButton(R.string.ok, null).setNegativeButton(R.string.cancel, null);
        final AlertDialog create = builder.create();
        int i = (int) (24.0f * f);
        create.setView(editText, i, (int) (8.0f * f), i, (int) (f * 5.0f));
        create.show();
        create.getButton(-1).setOnClickListener(view -> {
            String obj = editText.getText().toString();
            if (isValidPageNumber(obj)) {
                create.dismiss();
                pdfView.jumpTo(Integer.parseInt(obj) - 1, true);
                return;
            }
            editText.setError(getString(R.string.invalid_page_number));
        });
    }

    public void changeThemeNightMode(MenuItem menuItem) {
        boolean z = sharedPreferences.getBoolean("prefs_night_mode_enabled", false);
        nightModeEnabled = z;
        setNightModeThemeIcons(menuItem, !z);
        pdfView.setNightMode(!nightModeEnabled);
        pdfView.invalidate();
        sharedPreferences.edit().putBoolean("prefs_night_mode_enabled", !nightModeEnabled).apply();
        setupPdfListSwipeIcons(mMenu.findItem(R.id.itemBookmarkView), sharedPreferences.getBoolean("prefs_swipe_horizontal_enabled", false));
    }

    public void showPdfTools() {
        try {
            Uri fromFile = uri != null ? uri : Uri.fromFile(new File(filePath));
            Intent intent = new Intent(this, ActivityPDFTools.class);
            intent.putExtra(ActivityPDFTools.PRE_SELECTED_PDF_PATH, fromFile.toString());
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, R.string.cannot_show_tools, Toast.LENGTH_LONG).show();
        }
    }

    public void setupPdfListSwipeIcons(MenuItem menuItem, boolean z) {
        boolean z2 = sharedPreferences.getBoolean("prefs_night_mode_enabled", false);
        if (z) {
            if (z2) {
                menuItem.setIcon(R.drawable.ic_action_swipe_vertical_night);
                return;
            }
            menuItem.setIcon(R.drawable.ic_action_swipe_vertical);
            menuItem.setTitle(R.string.swipe_vertical);
        } else if (z2) {
            menuItem.setIcon(R.drawable.ic_action_swipe_horizontal_night);
        } else {
            menuItem.setIcon(R.drawable.ic_action_swipe_horizontal);
            menuItem.setTitle(R.string.swipe_horizontal);
        }
    }

    public void setNightModeThemeIcons(MenuItem menuItem, boolean z) {
        Resources resources = context.getResources();
        if (z) {
            menuItem.setIcon(R.drawable.ic_action_light_mode_night);
            menuItem.setTitle(R.string.light_mode);
            toolbar.setBackgroundColor(resources.getColor(R.color.colorPrimaryNight));
            toolbar.setTitleTextColor(resources.getColor(R.color.colorTitleTextNight));
            toolbar.setNavigationIcon(R.drawable.ic_round_arrow_back_ios_24_night);
            toolbarBottom.setBackgroundColor(resources.getColor(R.color.colorPrimaryNight));
            pdfView.setBackgroundColor(resources.getColor(R.color.colorPrimaryDarkNight));
            divider.setBackgroundColor(resources.getColor(R.color.colorPrimaryDarkNight));
            topMenu.findItem(R.id.itemShare).setIcon(R.drawable.ic_round_ios_share_24_night);
            topMenu.findItem(R.id.itemSharePicture).setIcon(R.drawable.ic_round_picture_as_pdf_24_night);
            topMenu.findItem(R.id.itemPrint).setIcon(R.drawable.ic_round_print_24_night);
            mMenu.findItem(R.id.itemBookmark).setIcon(R.drawable.ic_action_bookmark_night);
            mMenu.findItem(R.id.itemPdfContents).setIcon(R.drawable.ic_round_view_list_24_night);
            mMenu.findItem(R.id.itemPageToJump).setIcon(R.drawable.ic_action_jump_to_page_night);
            mMenu.findItem(R.id.itemPdfToolsSetting).setIcon(R.drawable.ic_round_build_24_night);
            if (Build.VERSION.SDK_INT >= 23) {
                flags &= -8193;
                ((Activity) context).getWindow().setStatusBarColor(colorPrimaryDarkNight);
                return;
            }
            return;
        }
        menuItem.setIcon(R.drawable.ic_round_dark_mode_24);
        menuItem.setTitle(R.string.night_mode);
        toolbar.setBackgroundColor(-1);
        toolbar.setTitleTextColor(context.getResources().getColor(R.color.colorTitleTextLight));
        toolbar.setNavigationIcon(R.drawable.ic_round_arrow_back_ios_24);
        toolbarBottom.setBackgroundColor(-1);
        pdfView.setBackgroundColor(context.getResources().getColor(R.color.colorPDFViewBg));
        divider.setBackgroundColor(resources.getColor(R.color.colorDividerLight));
        topMenu.findItem(R.id.itemShare).setIcon(R.drawable.ic_round_ios_share_24);
        topMenu.findItem(R.id.itemSharePicture).setIcon(R.drawable.ic_round_picture_as_pdf_24);
        topMenu.findItem(R.id.itemPrint).setIcon(R.drawable.ic_round_print_24);
        mMenu.findItem(R.id.itemBookmark).setIcon(R.drawable.ic_action_bookmark);
        mMenu.findItem(R.id.itemPdfContents).setIcon(R.drawable.ic_round_view_list_24);
        mMenu.findItem(R.id.itemPageToJump).setIcon(R.drawable.ic_action_jump_to_page);
        mMenu.findItem(R.id.itemPdfToolsSetting).setIcon(R.drawable.ic_round_build_24);
        if (Build.VERSION.SDK_INT >= 23) {
            flags |= 8192;
            ((Activity) context).getWindow().setStatusBarColor(colorPrimaryDark);
        }
    }

    public void enterPasswordDialog() {
        float f = context.getResources().getDisplayMetrics().density;
        final EditText editText = new EditText(context);
        editText.setHint(R.string.enter_password);
        editText.setInputType(129);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.password_protected).setPositiveButton(R.string.ok, null).setCancelable(false).setNegativeButton(R.string.cancel, (dialogInterface, i) -> finish());
        final AlertDialog create = builder.create();
        int i = (int) (24.0f * f);
        create.setView(editText, i, (int) (8.0f * f), i, (int) (f * 5.0f));
        create.show();
        create.getButton(-1).setOnClickListener(view -> {
            PDDocument pDDocument;
            mPassword = editText.getText().toString();
            if (!TextUtils.isEmpty(mPassword)) {
                try {
                    if (uri != null) {
                        pDDocument = PDDocument.load(getContentResolver().openInputStream(uri), mPassword);
                    } else {
                        pDDocument = PDDocument.load(new File(filePath), mPassword);
                    }
                    pDDocument.close();
                    loadSelectedPdfFile(mPassword, pageNumber, swipeHorizontalEnabled, nightModeEnabled, fitPolicy);
                    create.dismiss();
                } catch (Exception e) {
                    if (e instanceof InvalidPasswordException) {
                        editText.setError(context.getString(R.string.invalid_password));
                        return;
                    }
                    e.printStackTrace();
                }
            } else {
                editText.setError(context.getString(R.string.invalid_password));
            }
        });
    }

    public boolean isValidPageNumber(String str) {
        if (!TextUtils.isEmpty(str) && TextUtils.isDigitsOnly(str)) {
            int pageCount = pdfView.getPageCount();
            try {
                int intValue = Integer.valueOf(str);
                if (intValue <= 0 || intValue > pageCount) {
                    return false;
                }
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public void toggle() {
        if (mVisible) {
            hide();
            return;
        }
        show();
        if (AUTO_HIDE) {
            delayedHide(10000);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void hide() {
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.hide();
        }
        layBottomMenuBar.setVisibility(View.GONE);
        mVisible = false;
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, 1);
    }

    private void show() {
        pdfView.setSystemUiVisibility(1536);
        mVisible = true;
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, 1);
    }

    private void delayedHide(int i) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, i);
    }

    private void printPdf() {
        Uri uri2 = uri;
        if (uri2 != null) {
            Utils.printPdfFile(this, uri2);
        } else {
            Utils.printPdfFile(this, Uri.fromFile(new File(filePath)));
        }
    }

    private void sharePdf() {
        Uri uri2 = uri;
        if (uri2 != null) {
            Utils.sharePdfFile(this, uri2);
            return;
        }
        try {
            Utils.sharePdfFile(this, FileProvider.getUriForFile(context, getPackageName() + ".provider", new File(filePath)));
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, R.string.cant_share_file, Toast.LENGTH_LONG).show();
        }
    }

    private void sharePdfAsPicture() {
        Uri uri2 = uri;
        if (uri2 != null) {
            showShareAsPicture(uri2);
            return;
        }
        try {
            showShareAsPicture(Uri.fromFile(new File(filePath)));
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, R.string.cant_share_file, Toast.LENGTH_LONG).show();
        }
    }
}
