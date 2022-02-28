package pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Activity;

import android.animation.Animator;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Ads.GoogleAppLovinAds;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.R;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Adapter.AdapterShareAsPicture;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.GetSet.PDFPage;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.utils.Utils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ActivityShareAsPicture extends AppCompatActivity {
    public static String ORGANIZE_SHARE_PAGES_TIP = "prefs_organize_share_pages";
    public RelativeLayout rLayTapMoreOptions;
    public SharedPreferences sharedPreferences;
    Context context;
    FloatingActionButton floatingBtnSave;
    boolean isPdfPicturePagesTip;
    List<PDFPage> listPdfPages = new ArrayList<>();
    List<PDFPage> listPdfPicFinal = new ArrayList<>();
    String pdfDirAsfileName;
    String pdfSavedFilePath;
    ProgressBar progressSharePdfPicture;
    RecyclerView recycleSharePdfPicture;
    AdapterShareAsPicture shareAsPictureAdapter;
    String strAllPdfDocuments;
    String strAllPdfPictureDir;
    ImageView imgTapClose;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_share_as_picture);

        LinearLayout ll_banner = findViewById(R.id.ll_banner);
        GoogleAppLovinAds.showBannerAds(ActivityShareAsPicture.this,ll_banner);


        strAllPdfPictureDir = Environment.getExternalStorageDirectory() + "/Pictures/AllPdf/tmp/";
        strAllPdfDocuments = Environment.getExternalStorageDirectory() + "/Documents/AllPdf/";

        setSupportActionBar(findViewById(R.id.toolbar));
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        context = this;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        isPdfPicturePagesTip = sharedPreferences.getBoolean(ORGANIZE_SHARE_PAGES_TIP, true);

        recycleSharePdfPicture = findViewById(R.id.recycleSharePdfPicture);
        progressSharePdfPicture = findViewById(R.id.progressSharePdfPicture);
        floatingBtnSave = findViewById(R.id.floatingBtnSave);
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
                    edit.putBoolean(ActivityShareAsPicture.ORGANIZE_SHARE_PAGES_TIP, false);
                    edit.apply();
                }
            });
        });

        pdfSavedFilePath = getIntent().getStringExtra("com.example.pdfreader.PDF_PATH");
        if (isPdfPicturePagesTip) {
            rLayTapMoreOptions.setVisibility(View.GONE);
        } else {
            rLayTapMoreOptions.setVisibility(View.GONE);
        }

        new LoadPdfPictureThumbAyn().execute(pdfSavedFilePath);

        floatingBtnSave.setOnClickListener(view -> {
            listPdfPicFinal = shareAsPictureAdapter.getFinalOrganizedPages();
            new saveShareImagePdfAyn(getPdfPicturePages(listPdfPicFinal)).execute();
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Utils.deletePdfFiles(strAllPdfPictureDir);
    }

    public List<Integer> getPdfPicturePages(List<PDFPage> list) {
        ArrayList<Integer> arrayList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            arrayList.add(list.get(i).getPageNumber());
        }
        return arrayList;
    }

    public int calculateLeft(int i, int i2) {
        if (i2 > i) {
            return (i2 - i) / 2;
        }
        return 0;
    }

    public class LoadPdfPictureThumbAyn extends AsyncTask<String, Void, Void> {
        public LoadPdfPictureThumbAyn() {
        }

        public void onPreExecute() {
            super.onPreExecute();
        }

        public Void doInBackground(String... strArr) {
            int i;
            String str;
            FileOutputStream fileOutputStream;
            OutOfMemoryError e;
            PdfiumCore pdfiumCore = new PdfiumCore(context);
            pdfDirAsfileName = "share/";
            Uri parse = Uri.parse(strArr[0]);

            try {
                PdfDocument newDocument = pdfiumCore.newDocument(context.getContentResolver().openFileDescriptor(parse, "r"));
                int pageCount = pdfiumCore.getPageCount(newDocument);

                File file = new File(strAllPdfPictureDir + pdfDirAsfileName);
                if (!file.exists()) {
                    file.mkdirs();
                }
                int i2 = 0;
                while (i2 < pageCount) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(strAllPdfPictureDir);
                    sb.append(pdfDirAsfileName);
                    sb.append("page-");
                    int i3 = i2 + 1;
                    sb.append(i3);
                    sb.append(".jpg");
                    String sb2 = sb.toString();

                    FileOutputStream fileOutputStream2 = new FileOutputStream(sb2);
                    pdfiumCore.openPage(newDocument, i2);
                    int pageWidthPoint = pdfiumCore.getPageWidthPoint(newDocument, i2);
                    int pageHeightPoint = pdfiumCore.getPageHeightPoint(newDocument, i2);
                    try {
                        Bitmap createBitmap = Bitmap.createBitmap(pageWidthPoint, pageHeightPoint, Bitmap.Config.ARGB_8888);
                        fileOutputStream = fileOutputStream2;
                        i = pageCount;
                        str = sb2;
                        try {
                            pdfiumCore.renderPageBitmap(newDocument, createBitmap, i2, 0, 0, pageWidthPoint, pageHeightPoint, true);
                            try {
                                createBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
                            } catch (OutOfMemoryError ignored) {
                            }
                        } catch (OutOfMemoryError e2) {
                            try {
                                Toast.makeText(context, R.string.failed_low_memory, Toast.LENGTH_LONG).show();
                                e2.printStackTrace();
                                listPdfPages.add(new PDFPage(i3, Uri.fromFile(new File(str))));
                                fileOutputStream.close();
                            } catch (OutOfMemoryError ignored) {
                            }
                        }
                    } catch (OutOfMemoryError e4) {
                        e = e4;
                        fileOutputStream = fileOutputStream2;
                        i = pageCount;
                        str = sb2;
                        Toast.makeText(context, R.string.failed_low_memory, Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                        listPdfPages.add(new PDFPage(i3, Uri.fromFile(new File(str))));
                        fileOutputStream.close();
                        listPdfPages.add(new PDFPage(i3, Uri.fromFile(new File(str))));
                        fileOutputStream.close();
                    }
                    listPdfPages.add(new PDFPage(i3, Uri.fromFile(new File(str))));
                    fileOutputStream.close();
                    i2 = i3;
                    pageCount = i;
                }
                pdfiumCore.closeDocument(newDocument);
                return null;
            } catch (Exception e5) {
                e5.printStackTrace();
                return null;
            }
        }

        public void onPostExecute(Void r6) {
            super.onPostExecute(r6);
            shareAsPictureAdapter = new AdapterShareAsPicture(context, listPdfPages);
            int i = Utils.isTablet(context) ? 6 : 3;
            recycleSharePdfPicture.setLayoutManager(new GridLayoutManager(context, i, RecyclerView.VERTICAL, false));
            progressSharePdfPicture.setVisibility(View.GONE);
            recycleSharePdfPicture.setAdapter(shareAsPictureAdapter);
            floatingBtnSave.setVisibility(View.VISIBLE);

            new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(15, 0) {

                @Override
                public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                }

                @Override
                public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder2) {
                    int adapterPosition = viewHolder.getAdapterPosition();
                    int adapterPosition2 = viewHolder2.getAdapterPosition();
                    listPdfPages.add(adapterPosition, listPdfPages.remove(adapterPosition2));
                    shareAsPictureAdapter.notifyItemMoved(adapterPosition2, adapterPosition);
                    return true;
                }

                @Override
                public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                    super.clearView(recyclerView, viewHolder);
                    StringBuilder sb = new StringBuilder();
                    sb.append("Page order after swap ");
                    ActivityShareAsPicture shareAsPictureActivity = ActivityShareAsPicture.this;
                    sb.append(shareAsPictureActivity.getPdfPicturePages(shareAsPictureActivity.listPdfPages).toString());
                }
            }).attachToRecyclerView(recycleSharePdfPicture);
        }
    }

    public class saveShareImagePdfAyn extends AsyncTask<Void, Void, Void> {
        String imageName;
        ProgressDialog progressDialog;
        List<Integer> organizedPages;

        public saveShareImagePdfAyn(List<Integer> list) {
            organizedPages = list;
        }

        public void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage(context.getString(R.string.saving_wait));
            progressDialog.show();
        }

        public Void doInBackground(Void... voidArr) {
            try {
                File file = new File(strAllPdfPictureDir + pdfDirAsfileName);
                String name = new File(pdfSavedFilePath).getName();
                if (!file.exists()) {
                    file.mkdirs();
                }
                imageName = strAllPdfPictureDir + pdfDirAsfileName + Utils.removePdfExtension(name) + ".jpg";
                ArrayList<Integer> arrayList = new ArrayList<>();
                int size = organizedPages.size();

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                int i = 0;
                int i2 = 0;
                int i3 = 0;
                while (i2 < size) {
                    BitmapFactory.decodeFile(strAllPdfPictureDir + pdfDirAsfileName + "page-" + organizedPages.get(i2) + ".jpg", options);
                    i2++;
                    i3 = i2 == size ? i3 + options.outHeight : i3 + options.outHeight + 4;
                    arrayList.add(options.outWidth);
                }
                int intValue = Collections.max(arrayList);

                try {
                    Bitmap createBitmap = Bitmap.createBitmap(intValue, i3, Bitmap.Config.ARGB_8888);
                    createBitmap.eraseColor(getResources().getColor(R.color.colorPDFViewBg));
                    Canvas canvas = new Canvas(createBitmap);
                    int i4 = 0;
                    while (i < size) {
                        String str = strAllPdfPictureDir + pdfDirAsfileName + "page-" + organizedPages.get(i) + ".jpg";
                        Bitmap decodeFile = BitmapFactory.decodeFile(str);
                        canvas.drawBitmap(decodeFile, (float) calculateLeft(decodeFile.getWidth(), intValue), (float) i4, null);
                        i++;
                        i4 = i == size ? i4 + decodeFile.getHeight() : i4 + decodeFile.getHeight() + 4;
                        decodeFile.recycle();
                    }
                    FileOutputStream fileOutputStream = new FileOutputStream(new File(imageName));
                    createBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
                    fileOutputStream.flush();
                    fileOutputStream.close();
                } catch (OutOfMemoryError e) {
                    Toast.makeText(context, R.string.failed_low_memory, Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
            return null;
        }

        public void onPostExecute(Void r4) {
            super.onPostExecute(r4);
            progressDialog.dismiss();
            Utils.sharePdfFile(context, FileProvider.getUriForFile(context, getPackageName() + ".provider", new File(imageName)));
        }
    }
}