package pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Activity;

import android.animation.Animator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Ads.GoogleAppLovinAds;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.R;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Adapter.AdapterOrganizeImages;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.GetSet.ImagePage;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.utils.ImageUtils;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.utils.Utils;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDPage;
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream;
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle;
import com.tom_roush.pdfbox.pdmodel.graphics.image.JPEGFactory;
import com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject;
import com.tom_roush.pdfbox.util.PDFBoxResourceLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ActivityOrganizeImages extends AppCompatActivity {
    public static final String IMAGE_URIS = "com.example.pdfreader.IMAGE_URIS";
    public static String ORGANIZE_PAGES_TIP = "prefs_organize_pages";
    public String allPdfDocuments;
    public ArrayList<String> arrayImageUris;
    public Button btnCancelProgress;
    public Context context;
    public ImageView imgCloseProgress;
    public List<ImagePage> listImagePages = new ArrayList<>();
    public ConstraintLayout mProgressView;
    public AdapterOrganizeImages organizeImagesAdapter;
    public ProgressBar progressDownloading;
    public ConstraintLayout progressMain;
    public ProgressBar progressOrganizePages;
    public RelativeLayout rLayTapMoreOptions;
    public RecyclerView recycleOrganizePages;
    public SharedPreferences sharedPreferences;
    public TextView tvCurrentAction;
    public TextView tvSavedPdfPath;
    public FloatingActionButton floatBtnSave;
    public boolean showOrganizePagesTip;
    public Button btnOpenPdfFile;
    public ImageView imgPdfSuccess;
    public ImageView imgTapClose;
    public TextView tvDownloadPercent;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_organize_images);

        setSupportActionBar(findViewById(R.id.toolbar));
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        LinearLayout ll_banner = findViewById(R.id.ll_banner);
        FrameLayout fl_native = findViewById(R.id.fl_native);

        GoogleAppLovinAds.showBannerAds(ActivityOrganizeImages.this,ll_banner);
        GoogleAppLovinAds.showNativeAds(ActivityOrganizeImages.this,fl_native);



        allPdfDocuments = Environment.getExternalStorageDirectory() + "/Documents/AllPdf/";

        context = this;

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        showOrganizePagesTip = sharedPreferences.getBoolean(ORGANIZE_PAGES_TIP, true);

        recycleOrganizePages = findViewById(R.id.recycleOrganizePages);
        progressOrganizePages = findViewById(R.id.progressOrganizePages);
        floatBtnSave = findViewById(R.id.floatBtnSave);
        progressMain = findViewById(R.id.progressMain);
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
                    edit.putBoolean(ActivityOrganizeImages.ORGANIZE_PAGES_TIP, false);
                    edit.apply();
                }
            });
        });

        arrayImageUris = getIntent().getStringArrayListExtra(IMAGE_URIS);
        if (showOrganizePagesTip) {
            rLayTapMoreOptions.setVisibility(View.GONE);
        } else {
            rLayTapMoreOptions.setVisibility(View.GONE);
        }

        new LoadImagePageThumbAyn(arrayImageUris).execute();

        floatBtnSave.setOnClickListener(view -> {
            if (listImagePages.size() >= 1) {
                showImagePdfFileNameDialog();
            } else {
                Toast.makeText(context, R.string.select_at_least_one_image, Toast.LENGTH_LONG).show();
            }
        });
    }

    public Bitmap rotateImageBitmap(Bitmap bitmap, int i) {
        Matrix matrix = new Matrix();
        switch (i) {
            case 2:
                matrix.setScale(-1.0f, 1.0f);
                break;
            case 3:
                matrix.setRotate(180.0f);
                break;
            case 4:
                matrix.setRotate(180.0f);
                matrix.postScale(-1.0f, 1.0f);
                break;
            case 5:
                matrix.setRotate(90.0f);
                matrix.postScale(-1.0f, 1.0f);
                break;
            case 6:
                matrix.setRotate(90.0f);
                break;
            case 7:
                matrix.setRotate(-90.0f);
                matrix.postScale(-1.0f, 1.0f);
                break;
            case 8:
                matrix.setRotate(-90.0f);
                break;
            default:
                return bitmap;
        }
        try {
            Bitmap createBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            return createBitmap;
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Integer> getImageOrganizedPages(List<ImagePage> list) {
        ArrayList<Integer> arrayList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            arrayList.add(list.get(i).getPageNumber());
        }
        return arrayList;
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

    public void updateDownloadingProgressPercent(int i, int i2) {
        int i3 = ((int) (((float) i) * 100.0f)) / i2;
        tvDownloadPercent.setText(i3 + "%");
        progressDownloading.setProgress(i);
    }

    public void OpenImagePathSet(final Context context2, String str, final String str2, final boolean z) {
        btnOpenPdfFile.setText(str);
        btnOpenPdfFile.setOnClickListener(view -> {
            if (z) {
                if (GoogleAppLovinAds.adsdisplay) {
                    GoogleAppLovinAds.showFullAds(ActivityOrganizeImages.this, () -> {
                        GoogleAppLovinAds.allcount60.start();
                        File file = new File(str2);
                        Intent intent = new Intent(context2, ActivityPDFViewer.class);
                        intent.putExtra(ActivityMain.PDF_LOCATION, file.getAbsolutePath());
                        context2.startActivity(intent);
                    });
                } else {
                    File file = new File(str2);
                    Intent intent = new Intent(context2, ActivityPDFViewer.class);
                    intent.putExtra(ActivityMain.PDF_LOCATION, file.getAbsolutePath());
                    context2.startActivity(intent);
                }
                return;
            }
            Intent intent2 = new Intent(context2, ActivitySelectPDF.class);
            intent2.putExtra(ActivityPDFTools.IS_DIRECTORY, true);
            context2.startActivity(intent2.putExtra(ActivityPDFTools.DIRECTORY_PATH, str2));
        });
    }

    public void showImagePdfFileNameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        float f = context.getResources().getDisplayMetrics().density;
        final EditText editText = new EditText(context);
        editText.setText("Image_PDF_" + System.currentTimeMillis());
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
                new SavePhotosPdfOrganizedPagesAyn(getImageOrganizedPages(listImagePages), obj, progressMain).execute();
                return;
            }
            editText.setError(getString(R.string.invalid_file_name));
        });
    }

    public void showInterstialAd(Context context2, String str) {
        finishProcessBar(context2, str);
    }

    public class LoadImagePageThumbAyn extends AsyncTask<Void, Void, Void> {
        public LoadImagePageThumbAyn(ArrayList<String> arrayList) {
            arrayImageUris = arrayList;
        }

        public void onPreExecute() {
            super.onPreExecute();
        }

        public Void doInBackground(Void... voidArr) {
            int i = 0;
            while (i < arrayImageUris.size()) {
                int i2 = i + 1;
                listImagePages.add(new ImagePage(i2, Uri.parse(arrayImageUris.get(i))));
                i = i2;
            }
            organizeImagesAdapter = new AdapterOrganizeImages(context, listImagePages);
            return null;
        }

        public void onPostExecute(Void r6) {
            super.onPostExecute(r6);
            recycleOrganizePages.setLayoutManager(new GridLayoutManager(context, Utils.isTablet(context) ? 6 : 3, RecyclerView.VERTICAL, false));
            progressOrganizePages.setVisibility(View.GONE);
            recycleOrganizePages.setAdapter(organizeImagesAdapter);
            floatBtnSave.setVisibility(View.VISIBLE);

            new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(15, 0) {

                @Override
                public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                }

                @Override
                public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder2) {
                    int adapterPosition = viewHolder.getAdapterPosition();
                    int adapterPosition2 = viewHolder2.getAdapterPosition();
                    listImagePages.add(adapterPosition, listImagePages.remove(adapterPosition2));
                    organizeImagesAdapter.notifyItemMoved(adapterPosition2, adapterPosition);
                    return true;
                }

                @Override
                public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                    super.clearView(recyclerView, viewHolder);
                    StringBuilder sb = new StringBuilder();
                    sb.append("Page order after swap ");
                    ActivityOrganizeImages organizeImagesActivity = ActivityOrganizeImages.this;
                    sb.append(organizeImagesActivity.getImageOrganizedPages(organizeImagesActivity.listImagePages).toString());
                }
            }).attachToRecyclerView(recycleOrganizePages);
        }
    }

    public class SavePhotosPdfOrganizedPagesAyn extends AsyncTask<Void, Integer, Void> {
        public String generatedPDFPath;
        public String newFileName;
        public int numPages;
        public List<Integer> organizedPages;

        public SavePhotosPdfOrganizedPagesAyn(List<Integer> list, String str, ConstraintLayout constraintLayout) {
            organizedPages = new ArrayList<>();
            numPages = organizedPages.size();
            organizedPages = list;
            numPages = list.size();
            newFileName = str;
            mProgressView = constraintLayout;
            initProgressView();
            Utils.setLightStatusBar(context);
            btnCancelProgress.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    cancel(true);
                    closeProgressBar(context);
                    closeProgressBar(context);
                }
            });
        }

        public void onPreExecute() {
            super.onPreExecute();
            progressDownloading.setMax(numPages);
            tvCurrentAction.setText(R.string.converting);
            mProgressView.setVisibility(View.VISIBLE);
        }

        public Void doInBackground(Void... voidArr) {
            boolean z = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(ActivityMain.GRID_VIEW_ENABLED, false);
            try {
                File file = new File(allPdfDocuments);
                generatedPDFPath = allPdfDocuments + newFileName + ".pdf";
                if (!file.exists()) {
                    file.mkdirs();
                }
                PDFBoxResourceLoader.init(context);
                PDDocument pDDocument = new PDDocument();
                int i = 0;
                while (i < numPages && !isCancelled()) {
                    String path = listImagePages.get(i).getImageUri().getPath();
                    Bitmap rotateImageBitmap = rotateImageBitmap(ImageUtils.getInstant().getPdfCompressedBitmap(path), new ExifInterface(path).getAttributeInt("Orientation", 0));
                    float width = (float) rotateImageBitmap.getWidth();
                    float height = (float) rotateImageBitmap.getHeight();
                    PDPage pDPage = new PDPage(new PDRectangle(width, height));
                    pDDocument.addPage(pDPage);
                    PDImageXObject createFromImage = JPEGFactory.createFromImage(pDDocument, rotateImageBitmap);
                    PDPageContentStream pDPageContentStream = new PDPageContentStream(pDDocument, pDPage, true, true, true);
                    pDPageContentStream.drawImage(createFromImage, 0.0f, 0.0f, width, height);
                    pDPageContentStream.close();
                    i++;
                    publishProgress(i);
                }
                pDDocument.save(generatedPDFPath);
                pDDocument.close();
                if (z) {
                    Utils.generatePDFThumbnail(context, generatedPDFPath);
                }
                MediaScannerConnection.scanFile(context, new String[]{generatedPDFPath}, new String[]{"application/pdf"}, null);
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
                GoogleAppLovinAds.showFullAds(ActivityOrganizeImages.this, () -> {
                    GoogleAppLovinAds.allcount60.start();
                    tvCurrentAction.setText(R.string.done);
                    btnCancelProgress.setOnClickListener(null);
                    showInterstialAd(context, allPdfDocuments);
                    OpenImagePathSet(context, context.getString(R.string.open_file), generatedPDFPath, true);
                });
            } else {
                tvCurrentAction.setText(R.string.done);
                btnCancelProgress.setOnClickListener(null);
                showInterstialAd(context, allPdfDocuments);
                OpenImagePathSet(context, context.getString(R.string.open_file), generatedPDFPath, true);
            }
        }
    }
}