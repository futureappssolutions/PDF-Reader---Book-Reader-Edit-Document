package pdfreader.pdfviewer.pdfscanner.documentreader.officetool.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;

import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Activity.ActivityMain;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Activity.ActivityPDFTools;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Activity.ActivityPDFViewer;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.R;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Activity.ActivitySelectPDF;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Activity.ActivityViewImages;
import com.itextpdf.text.Annotation;
import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PRStream;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfNumber;
import com.itextpdf.text.pdf.PdfObject;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.parser.PdfImageObject;
import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;
import com.tom_roush.pdfbox.multipdf.PDFMergerUtility;
import com.tom_roush.pdfbox.util.PDFBoxResourceLoader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class PDFTools {
    public static final String TAG = "PDFTools";
    @SuppressLint("StaticFieldLeak")
    public static Button btnCancelProgress;
    @SuppressLint("StaticFieldLeak")
    public static ImageView imgCloseProgress;
    public static ConstraintLayout mProgressView;
    @SuppressLint("StaticFieldLeak")
    public static ProgressBar progressDownloading;
    @SuppressLint("StaticFieldLeak")
    public static TextView tvCurrentAction;
    @SuppressLint("StaticFieldLeak")
    public static TextView tvDownloadPercent;
    @SuppressLint("StaticFieldLeak")
    public static Button btnOpenPdfFile;
    @SuppressLint("StaticFieldLeak")
    public static ImageView imgPdfSuccess;
    @SuppressLint("StaticFieldLeak")
    public static TextView tvDescription;
    @SuppressLint("StaticFieldLeak")
    public static TextView tvSavedPdfPath;

    public static void initializeProgressView() {
        tvDownloadPercent = mProgressView.findViewById(R.id.tvDownloadPercent);
        tvCurrentAction = mProgressView.findViewById(R.id.tvCurrentAction);
        progressDownloading = mProgressView.findViewById(R.id.progressDownloading);
        tvDescription = mProgressView.findViewById(R.id.tvDescription);
        tvSavedPdfPath = mProgressView.findViewById(R.id.tvSavedPdfPath);
        imgPdfSuccess = mProgressView.findViewById(R.id.imgPdfSuccess);
        btnOpenPdfFile = mProgressView.findViewById(R.id.btnOpenPdfFile);
        btnCancelProgress = mProgressView.findViewById(R.id.btnCancelProgress);
        imgCloseProgress = mProgressView.findViewById(R.id.imgCloseProgress);
    }

    @SuppressLint("WrongConstant")
    public static void processingFinished(Context context, String str, String str2, String str3) {
        tvDownloadPercent.setVisibility(View.GONE);
        progressDownloading.setVisibility(View.GONE);
        imgPdfSuccess.setVisibility(View.VISIBLE);
        imgCloseProgress.setVisibility(View.VISIBLE);
        btnOpenPdfFile.setVisibility(View.VISIBLE);
        btnCancelProgress.setVisibility(View.GONE);
        String str4 = context.getString(R.string.saved_to) + " " + str3;
        if (!TextUtils.isEmpty(str)) {
            tvCurrentAction.setText(str);
        }
        if (!TextUtils.isEmpty(str2)) {
            tvDescription.setText(str2);
            tvDescription.setVisibility(View.VISIBLE);
        }
        tvSavedPdfPath.setText(str4);
    }

    @SuppressLint("WrongConstant")
    public static void closeProgressView(Context context) {
        mProgressView.setVisibility(View.GONE);
        imgPdfSuccess.setVisibility(View.GONE);
        btnOpenPdfFile.setVisibility(View.GONE);
        imgCloseProgress.setVisibility(View.GONE);
        tvDescription.setVisibility(View.GONE);
        progressDownloading.setVisibility(View.VISIBLE);
        tvDownloadPercent.setVisibility(View.VISIBLE);
        btnCancelProgress.setVisibility(View.VISIBLE);
        progressDownloading.setProgress(0);
        tvDownloadPercent.setText("0%");
        tvDescription.setText("");
        tvSavedPdfPath.setText("");
        Utils.clearLightStatusBar(context);
    }

    public static void showInterstialAd(Context context, String str, String str2, String str3) {
        processingFinished(context, str, str2, str3);
    }

    public static void updateProgressPercent(int i, int i2) {
        int i3 = ((int) (((float) i) * 100.0f)) / i2;
        tvDownloadPercent.setText(i3 + "%");
        progressDownloading.setProgress(i);
    }

    public static void setupOpenPath(final Context context, String str, final String str2, final boolean z) {
        btnOpenPdfFile.setText(str);
        btnOpenPdfFile.setOnClickListener(view -> {
            if (z) {
                File file = new File(str2);
                Intent intent = new Intent(context, ActivityPDFViewer.class);
                intent.putExtra(ActivityMain.PDF_LOCATION, file.getAbsolutePath());
                Log.d(PDFTools.TAG, "Open PDF from location " + file.getAbsolutePath());
                context.startActivity(intent);
                return;
            }
            Intent intent2 = new Intent(context, ActivitySelectPDF.class);
            intent2.putExtra(ActivityPDFTools.IS_DIRECTORY, true);
            context.startActivity(intent2.putExtra(ActivityPDFTools.DIRECTORY_PATH, str2));
        });
    }

    public static void openImageDirectory(final Context context, String str, final String str2) {
        btnOpenPdfFile.setText(str);
        btnOpenPdfFile.setOnClickListener(view -> {
            Intent intent = new Intent(context, ActivityViewImages.class);
            intent.putExtra(ActivityViewImages.GENERATED_IMAGES_PATH, str2);
            context.startActivity(intent);
        });
    }

    public static void removeProgressBarIndeterminate(Context context, final ProgressBar progressBar) {
        ((Activity) context).runOnUiThread(() -> progressBar.setIndeterminate(false));
    }

    public static class CompressPDFImproved extends AsyncTask<Void, Integer, Void> {
        String allPdfDocumentDir;
        Long compressedFileLength;
        String compressedFileSize;
        String compressedPDF;
        int compressionQuality;
        boolean isEcrypted = false;
        Context mContext;
        String pdfPath;
        String reducedPercent;
        Long uncompressedFileLength;
        String uncompressedFileSize;
        int xrefSize;

        public CompressPDFImproved(Context context, String str, int i, ConstraintLayout constraintLayout) {
            this.mContext = context;
            this.pdfPath = str;
            PDFTools.mProgressView = constraintLayout;
            PDFTools.initializeProgressView();
            Utils.setLightStatusBar(context);
            this.compressionQuality = i;
            PDFTools.btnCancelProgress.setOnClickListener(view -> {
                CompressPDFImproved.this.cancel(true);
                PDFTools.closeProgressView(CompressPDFImproved.this.mContext);
            });
        }

        public void onPreExecute() {
            super.onPreExecute();
            PDFTools.tvCurrentAction.setText(R.string.compressing);
            PDFTools.mProgressView.setVisibility(View.VISIBLE);
        }

        public Void doInBackground(Void... voidArr) {
            boolean z = PreferenceManager.getDefaultSharedPreferences(this.mContext).getBoolean(ActivityMain.GRID_VIEW_ENABLED, false);
            File file = new File(this.pdfPath);
            String name = file.getName();
            Long valueOf = file.length();
            this.uncompressedFileLength = valueOf;
            this.uncompressedFileSize = Formatter.formatShortFileSize(this.mContext, valueOf);
            this.allPdfDocumentDir = Environment.getExternalStorageDirectory() + "/Documents/AllPdf/";
            this.compressedPDF = this.allPdfDocumentDir + Utils.removePdfExtension(name) + "-Compressed.pdf";
            File file2 = new File(this.allPdfDocumentDir);
            if (!file2.exists()) {
                file2.mkdirs();
            }
            try {
                PdfReader pdfReader = new PdfReader(this.pdfPath);
                if (pdfReader.isEncrypted()) {
                    this.isEcrypted = true;
                    return null;
                }
                this.xrefSize = pdfReader.getXrefSize();
                PDFTools.progressDownloading.setMax(this.xrefSize);
                for (int i = 0; i < this.xrefSize && !isCancelled(); i++) {
                    PdfObject pdfObject = pdfReader.getPdfObject(i);
                    if (pdfObject != null && pdfObject.isStream()) {
                        PRStream pRStream = (PRStream) pdfObject;
                        PdfObject pdfObject2 = pRStream.get(PdfName.SUBTYPE);
                        if (pdfObject2 != null && pdfObject2.toString().equals(PdfName.IMAGE.toString())) {
                            try {
                                Bitmap pdfCompressedBitmap = ImageUtils.getInstant().getPdfCompressedBitmap(new PdfImageObject(pRStream).getImageAsBytes());
                                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                                pdfCompressedBitmap.compress(Bitmap.CompressFormat.JPEG, this.compressionQuality, byteArrayOutputStream);
                                pRStream.setData(byteArrayOutputStream.toByteArray(), false, 9);
                                pRStream.put(PdfName.FILTER, PdfName.DCTDECODE);
                                byteArrayOutputStream.close();
                                pRStream.clear();
                                pRStream.setData(byteArrayOutputStream.toByteArray(), false, 0);
                                pRStream.put(PdfName.TYPE, PdfName.XOBJECT);
                                pRStream.put(PdfName.SUBTYPE, PdfName.IMAGE);
                                pRStream.put(PdfName.FILTER, PdfName.DCTDECODE);
                                pRStream.put(PdfName.WIDTH, new PdfNumber(pdfCompressedBitmap.getWidth()));
                                pRStream.put(PdfName.HEIGHT, new PdfNumber(pdfCompressedBitmap.getHeight()));
                                pRStream.put(PdfName.BITSPERCOMPONENT, new PdfNumber(8));
                                pRStream.put(PdfName.COLORSPACE, PdfName.DEVICERGB);
                                if (!pdfCompressedBitmap.isRecycled()) {
                                    pdfCompressedBitmap.recycle();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        publishProgress(i + 1);
                    }
                }
                pdfReader.removeUnusedObjects();
                PdfStamper pdfStamper = new PdfStamper(pdfReader, new FileOutputStream(this.compressedPDF));
                pdfStamper.setFullCompression();
                pdfStamper.close();
                Long valueOf2 = new File(this.compressedPDF).length();
                this.compressedFileLength = valueOf2;
                this.compressedFileSize = Formatter.formatShortFileSize(this.mContext, valueOf2);
                this.reducedPercent = (100 - ((int) ((this.compressedFileLength * 100) / this.uncompressedFileLength))) + "%";
                MediaScannerConnection.scanFile(this.mContext, new String[]{this.compressedPDF}, new String[]{"application/pdf"}, null);
                if (z) {
                    Utils.generatePDFThumbnail(this.mContext, this.compressedPDF);
                }
                return null;
            } catch (Exception e2) {
                e2.printStackTrace();
                return null;
            }
        }

        public void onProgressUpdate(Integer... numArr) {
            super.onProgressUpdate(numArr);
            PDFTools.updateProgressPercent(numArr[0], this.xrefSize);
        }

        public void onPostExecute(Void r5) {
            super.onPostExecute(r5);
            if (!this.isEcrypted) {
                PDFTools.tvDownloadPercent.setText(R.string.hundred_percent);
                PDFTools.progressDownloading.setProgress(this.xrefSize);
                PDFTools.tvCurrentAction.setText(R.string.done);
                PDFTools.btnCancelProgress.setOnClickListener(null);
                PDFTools.showInterstialAd(this.mContext, this.reducedPercent, this.mContext.getString(R.string.reduced_from) + " " + this.uncompressedFileSize + " " + this.mContext.getString(R.string.to) + " " + this.compressedFileSize, this.allPdfDocumentDir);
                Context context = this.mContext;
                PDFTools.setupOpenPath(context, context.getString(R.string.open_file), this.compressedPDF, true);
                return;
            }
            PDFTools.closeProgressView(this.mContext);
            Toast.makeText(this.mContext, R.string.file_protected_unprotect, Toast.LENGTH_LONG).show();
        }
    }

    public static class ConvertPDFToPdfPictures extends AsyncTask<Void, Integer, Void> {
        String allPdfPictureDir;
        String fileName;
        Context mContext;
        int numPages;
        PdfDocument pdfDocument;
        String pdfPath;
        PdfiumCore pdfiumCore;

        public ConvertPDFToPdfPictures(Context context, String str, ConstraintLayout constraintLayout) {
            this.mContext = context;
            this.pdfPath = str;
            PDFTools.mProgressView = constraintLayout;
            PDFTools.initializeProgressView();
            Utils.setLightStatusBar(context);
            PDFTools.btnCancelProgress.setOnClickListener(view -> {
                ConvertPDFToPdfPictures.this.cancel(true);
                PDFTools.closeProgressView(ConvertPDFToPdfPictures.this.mContext);
            });
        }

        public void onPreExecute() {
            super.onPreExecute();
            PDFTools.tvCurrentAction.setText(R.string.converting);
            PDFTools.mProgressView.setVisibility(View.VISIBLE);
        }

        public Void doInBackground(Void... voidArr) {
            int i;
            String str;
            int i2;
            this.fileName = Utils.removePdfExtension(new File(this.pdfPath).getName());
            String name = new File(this.pdfPath).getName();
            ArrayList<String> arrayList = new ArrayList<>();
            ArrayList<String> arrayList2 = new ArrayList<>();
            this.allPdfPictureDir = Environment.getExternalStorageDirectory() + "/Pictures/AllPdf/" + Utils.removePdfExtension(name) + "/";
            File file = new File(this.allPdfPictureDir);
            if (!file.exists()) {
                file.mkdirs();
            }
            PdfiumCore pdfiumCore2 = new PdfiumCore(this.mContext);
            this.pdfiumCore = pdfiumCore2;
            try {
                PdfDocument newDocument = pdfiumCore2.newDocument(this.mContext.getContentResolver().openFileDescriptor(Uri.fromFile(new File(this.pdfPath)), "r"));
                this.pdfDocument = newDocument;
                this.numPages = this.pdfiumCore.getPageCount(newDocument);
                PDFTools.progressDownloading.setMax(this.numPages);
                int i3 = 0;
                while (i3 < this.numPages && !isCancelled()) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(this.allPdfPictureDir);
                    sb.append(Utils.removePdfExtension(name));
                    sb.append("-Page");
                    int i4 = i3 + 1;
                    sb.append(i4);
                    sb.append(".jpg");
                    String sb2 = sb.toString();
                    FileOutputStream fileOutputStream = new FileOutputStream(sb2);
                    Log.d(PDFTools.TAG, "Creating page image " + sb2);
                    this.pdfiumCore.openPage(this.pdfDocument, i3);
                    int pageWidthPoint = this.pdfiumCore.getPageWidthPoint(this.pdfDocument, i3) * 2;
                    int pageHeightPoint = this.pdfiumCore.getPageHeightPoint(this.pdfDocument, i3) * 2;
                    try {
                        Bitmap createBitmap = Bitmap.createBitmap(pageWidthPoint, pageHeightPoint, Bitmap.Config.ARGB_8888);
                        try {
                            i2 = 1;
                            str = sb2;
                            i = i4;
                            try {
                                this.pdfiumCore.renderPageBitmap(this.pdfDocument, createBitmap, i3, 0, 0, pageWidthPoint, pageHeightPoint, true);
                                createBitmap.compress(Bitmap.CompressFormat.JPEG, 60, fileOutputStream);
                            } catch (OutOfMemoryError ignored) {
                            }
                        } catch (OutOfMemoryError unused2) {
                            str = sb2;
                            i = i4;
                            i2 = 1;
                        }
                    } catch (OutOfMemoryError e) {
                        str = sb2;
                        i = i4;
                        i2 = 1;
                        Toast.makeText(this.mContext, R.string.failed_low_memory, Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                        arrayList.add(str);
                        arrayList2.add("image/jpg");
                        publishProgress(i);
                    }
                    arrayList.add(str);
                    arrayList2.add("image/jpg");
                    Integer[] numArr = new Integer[i2];
                    numArr[0] = i;
                    publishProgress(numArr);
                    i3 = i;
                }
                this.pdfiumCore.closeDocument(this.pdfDocument);
                try {
                    try {
                        MediaScannerConnection.scanFile(this.mContext, arrayList.toArray(new String[arrayList.size()]), arrayList2.toArray(new String[arrayList2.size()]), null);
                        return null;
                    } catch (Exception unused3) {
                        return null;
                    }
                } catch (Exception unused4) {
                    return null;
                }
            } catch (Exception unused5) {
                return null;
            }
        }

        public void onProgressUpdate(Integer... numArr) {
            super.onProgressUpdate(numArr);
            PDFTools.updateProgressPercent(numArr[0], this.numPages);
        }

        public void onPostExecute(Void r3) {
            super.onPostExecute(r3);
            PDFTools.tvCurrentAction.setText(R.string.done);
            PDFTools.btnCancelProgress.setOnClickListener(null);
            PDFTools.showInterstialAd(this.mContext, "", "", this.allPdfPictureDir);
            Context context = this.mContext;
            PDFTools.openImageDirectory(context, context.getString(R.string.open_directory), this.allPdfPictureDir);
        }
    }

    public static class ExtractPdfImages extends AsyncTask<Void, Integer, Void> {
        String allPdfPictureDir;
        int compressionQuality;
        Context mContext;
        String pdfPath;
        int xrefSize;

        public ExtractPdfImages(Context context, String str, int i, ConstraintLayout constraintLayout) {
            this.mContext = context;
            this.pdfPath = str;
            PDFTools.mProgressView = constraintLayout;
            PDFTools.initializeProgressView();
            Utils.setLightStatusBar(context);
            this.compressionQuality = i;
            PDFTools.btnCancelProgress.setOnClickListener(view -> {
                ExtractPdfImages.this.cancel(true);
                PDFTools.closeProgressView(ExtractPdfImages.this.mContext);
            });
        }

        public void onPreExecute() {
            super.onPreExecute();
            PDFTools.tvCurrentAction.setText(R.string.extracting);
            PDFTools.mProgressView.setVisibility(View.VISIBLE);
        }

        public Void doInBackground(Void... voidArr) {
            ArrayList<String> arrayList = new ArrayList<>();
            ArrayList<String> arrayList2 = new ArrayList<>();
            String name = new File(this.pdfPath).getName();
            this.allPdfPictureDir = Environment.getExternalStorageDirectory() + "/Pictures/AllPdf/" + Utils.removePdfExtension(name) + "/";
            File file = new File(this.allPdfPictureDir);
            if (!file.exists()) {
                file.mkdirs();
            }
            try {
                PdfReader pdfReader = new PdfReader(this.pdfPath);
                this.xrefSize = pdfReader.getXrefSize();
                PDFTools.progressDownloading.setMax(this.xrefSize);
                int i = 0;
                String str = "";
                String str2 = null;
                int i2 = 0;
                int i3 = 1;
                while (i2 < this.xrefSize && !isCancelled()) {
                    PdfObject pdfObject = pdfReader.getPdfObject(i2);
                    if (pdfObject != null && pdfObject.isStream()) {
                        PRStream pRStream = (PRStream) pdfObject;
                        PdfObject pdfObject2 = pRStream.get(PdfName.SUBTYPE);
                        if (pdfObject2 != null && pdfObject2.toString().equals(PdfName.IMAGE.toString())) {
                            try {
                                byte[] imageAsBytes = new PdfImageObject(pRStream).getImageAsBytes();
                                Bitmap decodeByteArray = BitmapFactory.decodeByteArray(imageAsBytes, i, imageAsBytes.length);
                                if (decodeByteArray != null) {
                                    String str3 = this.allPdfPictureDir + "image-" + i3 + ".jpg";
                                    try {
                                        decodeByteArray.compress(Bitmap.CompressFormat.JPEG, this.compressionQuality, new FileOutputStream(str3));
                                        Log.d(PDFTools.TAG, "Image extracted " + this.allPdfPictureDir + "image-" + i3 + ".jpg");
                                        pRStream.clear();
                                        if (!decodeByteArray.isRecycled()) {
                                            decodeByteArray.recycle();
                                        }
                                        i3++;
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        arrayList.add(str3);
                                        arrayList2.add("image/jpg");
                                        publishProgress(i2 + 1);
                                    }
                                    str = str3;
                                } else {
                                    str = str2;
                                }
                            } catch (Exception e2) {
                                e2.printStackTrace();
                                arrayList.add(str);
                                arrayList2.add("image/jpg");
                                publishProgress(i2 + 1);
                            }
                        }
                        arrayList.add(str);
                        arrayList2.add("image/jpg");
                        publishProgress(i2 + 1);
                        str2 = str;
                    }
                    i2++;
                    i = 0;
                }
                MediaScannerConnection.scanFile(this.mContext, arrayList.toArray(new String[arrayList.size()]), arrayList2.toArray(new String[arrayList2.size()]), null);
                return null;
            } catch (Exception e3) {
                e3.printStackTrace();
                return null;
            }
        }

        public void onProgressUpdate(Integer... numArr) {
            super.onProgressUpdate(numArr);
            PDFTools.updateProgressPercent(numArr[0], this.xrefSize);
        }

        public void onPostExecute(Void r3) {
            super.onPostExecute(r3);
            PDFTools.tvDownloadPercent.setText(R.string.hundred_percent);
            PDFTools.progressDownloading.setProgress(this.xrefSize);
            PDFTools.tvCurrentAction.setText(R.string.done);
            PDFTools.btnCancelProgress.setOnClickListener(null);
            PDFTools.showInterstialAd(this.mContext, "", "", this.allPdfPictureDir);
            Context context = this.mContext;
            PDFTools.openImageDirectory(context, context.getString(R.string.open_directory), this.allPdfPictureDir);
        }
    }

    public static class MergePDFFiles extends AsyncTask<Void, Integer, Void> {
        String allPdfMergedDir;
        Context mContext;
        boolean mergeSuccess = true;
        String mergedFileName;
        String mergedFilePath;
        int numFiles;
        ArrayList<String> pdfPaths;

        public MergePDFFiles(Context context, ArrayList<String> arrayList, String str, ConstraintLayout constraintLayout) {
            this.mContext = context;
            this.pdfPaths = arrayList;
            this.mergedFileName = str;
            PDFTools.mProgressView = constraintLayout;
            PDFTools.initializeProgressView();
            Utils.setLightStatusBar(context);
            PDFTools.btnCancelProgress.setOnClickListener(view -> {
                MergePDFFiles.this.cancel(true);
                PDFTools.closeProgressView(MergePDFFiles.this.mContext);
            });
        }

        public void onPreExecute() {
            super.onPreExecute();
            PDFTools.progressDownloading.setIndeterminate(true);
            PDFTools.tvCurrentAction.setText(R.string.merging);
            PDFTools.mProgressView.setVisibility(View.VISIBLE);
        }

        public Void doInBackground(Void... voidArr) {
            boolean z = PreferenceManager.getDefaultSharedPreferences(this.mContext).getBoolean(ActivityMain.GRID_VIEW_ENABLED, false);
            this.allPdfMergedDir = Environment.getExternalStorageDirectory() + "/Documents/AllPdf/Merged/";
            File file = new File(this.allPdfMergedDir);
            if (!file.exists()) {
                file.mkdirs();
            }
            this.mergedFilePath = this.allPdfMergedDir + this.mergedFileName + ".pdf";
            this.numFiles = this.pdfPaths.size();
            PDFTools.progressDownloading.setMax(this.numFiles + 1);
            PDFBoxResourceLoader.init(this.mContext);
            PDFMergerUtility pDFMergerUtility = new PDFMergerUtility();
            pDFMergerUtility.setDestinationFileName(this.mergedFilePath);
            PDFTools.removeProgressBarIndeterminate(this.mContext, PDFTools.progressDownloading);
            int i = 0;
            while (i < this.numFiles && !isCancelled()) {
                try {
                    pDFMergerUtility.addSource(new File(this.pdfPaths.get(i)));
                    i++;
                    publishProgress(i);
                } catch (Exception e) {
                    e.printStackTrace();
                    this.mergeSuccess = false;
                }
            }
            try {
                pDFMergerUtility.mergeDocuments(true);
            } catch (IOException e2) {
                e2.printStackTrace();
            }
            publishProgress(this.numFiles + 1);
            if (isCancelled()) {
                new File(this.mergedFilePath).delete();
            }
            MediaScannerConnection.scanFile(this.mContext, new String[]{this.mergedFilePath}, new String[]{"application/pdf"}, null);
            if (z) {
                Utils.generatePDFThumbnail(this.mContext, this.mergedFilePath);
            }
            return null;
        }

        public void onProgressUpdate(Integer... numArr) {
            super.onProgressUpdate(numArr);
            PDFTools.updateProgressPercent(numArr[0], this.numFiles + 1);
        }

        public void onPostExecute(Void r4) {
            super.onPostExecute(r4);
            PDFTools.tvCurrentAction.setText(R.string.done);
            PDFTools.btnCancelProgress.setOnClickListener(null);
            PDFTools.showInterstialAd(this.mContext, "", "", this.allPdfMergedDir);
            Context context = this.mContext;
            PDFTools.setupOpenPath(context, context.getString(R.string.open_file), this.mergedFilePath, true);
            if (!this.mergeSuccess) {
                Toast.makeText(this.mContext, R.string.merge_failed, Toast.LENGTH_LONG).show();
            }
        }
    }

    public static class SplitPDF extends AsyncTask<Void, Integer, Void> {
        public Context mContext;
        public int numPages = 0;
        public String pdfPath;
        public int splitAt = 0;
        public int splitFrom = 0;
        public int splitTo = 0;
        public String splittedPdfDocumentDir;

        public SplitPDF(Context context, String str, ConstraintLayout constraintLayout) {
            this.mContext = context;
            this.pdfPath = str;
            PDFTools.mProgressView = constraintLayout;
            PDFTools.initializeProgressView();
            Utils.setLightStatusBar(context);
            PDFTools.btnCancelProgress.setOnClickListener(view -> {
                SplitPDF.this.cancel(true);
                PDFTools.closeProgressView(SplitPDF.this.mContext);
            });
        }

        public SplitPDF(Context context, String str, ConstraintLayout constraintLayout, int i, int i2) {
            this.mContext = context;
            this.pdfPath = str;
            PDFTools.mProgressView = constraintLayout;
            PDFTools.initializeProgressView();
            Utils.setLightStatusBar(context);
            this.splitFrom = i;
            this.splitTo = i2;
            PDFTools.btnCancelProgress.setOnClickListener(view -> {
                SplitPDF.this.cancel(true);
                PDFTools.closeProgressView(SplitPDF.this.mContext);
            });
        }

        public SplitPDF(Context context, String str, ConstraintLayout constraintLayout, int i) {
            this.mContext = context;
            this.pdfPath = str;
            PDFTools.mProgressView = constraintLayout;
            PDFTools.initializeProgressView();
            Utils.setLightStatusBar(context);
            this.splitAt = i;
            PDFTools.btnCancelProgress.setOnClickListener(view -> {
                SplitPDF.this.cancel(true);
                PDFTools.closeProgressView(SplitPDF.this.mContext);
            });
        }

        public void onPreExecute() {
            super.onPreExecute();
            PDFTools.progressDownloading.setIndeterminate(true);
            PDFTools.tvCurrentAction.setText(R.string.splitting);
            PDFTools.mProgressView.setVisibility(View.VISIBLE);
        }

        public Void doInBackground(Void... voidArr) {
            ArrayList<String> arrayList = new ArrayList<>();
            ArrayList<String> arrayList2 = new ArrayList<>();
            try {
                PdfReader pdfReader = new PdfReader(this.pdfPath);
                File file = new File(this.pdfPath);
                for (int i = 1; i <= pdfReader.getNumberOfPages(); i++) {
                    Document document = new Document();
                    StringBuilder sb = new StringBuilder();
                    sb.append(Environment.getExternalStorageDirectory());
                    sb.append("/Documents/AllPdf/Split/");
                    this.splittedPdfDocumentDir = sb.toString();
                    File file2 = new File(this.splittedPdfDocumentDir);
                    if (!file2.exists()) {
                        file2.mkdirs();
                    }
                    PdfCopy pdfCopy = new PdfCopy(document, new FileOutputStream(sb + file.getName() + Annotation.PAGE + i + ".pdf"));
                    document.open();
                    pdfCopy.addPage(pdfCopy.getImportedPage(pdfReader, i));
                    Context context = this.mContext;
                    Utils.generatePDFThumbnail(context, sb + file.getName() + Annotation.PAGE + i + ".pdf");
                    arrayList.add(sb + file.getName() + Annotation.PAGE + i + ".pdf");
                    arrayList2.add("application/pdf");
                    document.close();
                }
                MediaScannerConnection.scanFile(this.mContext, arrayList.toArray(new String[arrayList.size()]), arrayList2.toArray(new String[arrayList2.size()]), null);
                Log.e("done-->>", "The pdf is split successfully");
            } catch (Exception e) {
                Log.e("error-->>", e.toString());
            }
            return null;
        }

        public void onProgressUpdate(Integer... numArr) {
            super.onProgressUpdate(numArr);
            PDFTools.updateProgressPercent(numArr[0], this.numPages);
        }

        public void onPostExecute(Void r4) {
            super.onPostExecute(r4);
            PDFTools.tvCurrentAction.setText(R.string.done);
            PDFTools.btnCancelProgress.setOnClickListener(null);
            PDFTools.showInterstialAd(this.mContext, "", "", this.splittedPdfDocumentDir);
            Context context = this.mContext;
            PDFTools.setupOpenPath(context, context.getString(R.string.open_directory), this.splittedPdfDocumentDir, false);
        }
    }
}