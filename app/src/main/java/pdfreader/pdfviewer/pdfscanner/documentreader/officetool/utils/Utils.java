package pdfreader.pdfviewer.pdfscanner.documentreader.officetool.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.print.PrintManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.core.app.ShareCompat;
import androidx.print.PrintHelper;

import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.R;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Adapter.AdapterPrintDocument;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.data.DbHelper;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.GetSet.PdfDataType;
import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfPasswordException;
import com.shockwave.pdfium.PdfiumCore;
import com.tom_roush.pdfbox.pdmodel.interactive.action.PDWindowsLaunchParams;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Utils {
    private static Dialog processDialog;

    public static class BackgroundGenerateThumbnails extends AsyncTask<Void, Void, Void> {
        private Context mContext;

        public BackgroundGenerateThumbnails(Context context) {
            this.mContext = context;
        }

        public Void doInBackground(Void... voidArr) {
            List<PdfDataType> allPdfs = DbHelper.getInstance(this.mContext).getAllPdfs();
            int size = allPdfs.size();
            for (int i = 0; i < size; i++) {
                String absolutePath = allPdfs.get(i).getAbsolutePath();
                if (!Utils.isThumbnailPresent(this.mContext, absolutePath)) {
                    Utils.generatePDFThumbnail(this.mContext, absolutePath);
                }
            }
            return null;
        }
    }

    public static boolean isTablet(Context context) {
        return context.getResources().getBoolean(R.bool.isTablet);
    }

    public static String formatDateToHumanReadable(Long l) {
        return new SimpleDateFormat("MMM dd yyyy", Locale.getDefault()).format(new Date(l.longValue()));
    }

    public static String formatMetadataDate(Context context, String str) {
        try {
            return new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).parse(str.split("\\+")[0].split(":")[1]));
        } catch (Exception e) {
            e.printStackTrace();
            return context.getString(R.string.unknown);
        }
    }

    public static String removePdfExtension(String str) {
        int lastIndexOf = str.lastIndexOf(System.getProperty("file.separator"));
        if (lastIndexOf != -1) {
            str = str.substring(lastIndexOf + 1);
        }
        int lastIndexOf2 = str.lastIndexOf(".");
        if (lastIndexOf2 == -1) {
            return str;
        }
        return str.substring(0, lastIndexOf2);
    }

    public static void sharePdfFile(Context context, Uri uri) {
        try {
            Intent intent = ShareCompat.IntentBuilder.from((Activity) context).setType(context.getContentResolver().getType(uri)).setStream(uri).getIntent();
            intent.addFlags(1);
            Intent createChooser = Intent.createChooser(intent, context.getResources().getString(R.string.share_this_file_via));
            createChooser.setFlags(268435456);
            if (createChooser.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(createChooser);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, (int) R.string.cant_share_file, 1).show();
        }
    }

    public static boolean isFileNameValid(String str) {
        String trim = str.trim();
        return !TextUtils.isEmpty(trim) && trim.matches("[a-zA-Z0-9-_ ]*");
    }

    public static void deletePdfFiles(String str) {
        File file = new File(str);
        if (file.exists() && file.isDirectory()) {
            try {
                Runtime.getRuntime().exec("find " + str + " -xdev -mindepth 1 -delete");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static Uri getImageUriFromPath(String str) {
        return Uri.fromFile(new File(str.replace(".pdf", ".jpg")));
    }

    public static boolean isThumbnailPresent(Context context, String str) {
        String name = new File(str).getName();
        return new File((context.getCacheDir() + "/Thumbnails/") + removePdfExtension(name) + ".jpg").exists();
    }

    public static void generatePDFThumbnail(Context context, String str) {
        PdfiumCore pdfiumCore = new PdfiumCore(context);
        File file = new File(str);
        String name = file.getName();
        try {
            PdfDocument newDocument = pdfiumCore.newDocument(context.getContentResolver().openFileDescriptor(Uri.fromFile(file), "r"));
            String str2 = context.getCacheDir() + "/Thumbnails/";
            File file2 = new File(str2);
            if (!file2.exists()) {
                file2.mkdirs();
            }
            String str3 = str2 + removePdfExtension(name) + ".jpg";
            StringBuilder sb = new StringBuilder();
            sb.append("Generating thumb img ");
            sb.append(str3);
            FileOutputStream fileOutputStream = new FileOutputStream(str3);
            pdfiumCore.openPage(newDocument, 0);
            int pageWidthPoint = pdfiumCore.getPageWidthPoint(newDocument, 0) / 2;
            int pageHeightPoint = pdfiumCore.getPageHeightPoint(newDocument, 0) / 2;
            try {
                Bitmap createBitmap = Bitmap.createBitmap(pageWidthPoint, pageHeightPoint, Bitmap.Config.RGB_565);
                pdfiumCore.renderPageBitmap(newDocument, createBitmap, 0, 0, 0, pageWidthPoint, pageHeightPoint, true);
                createBitmap.compress(Bitmap.CompressFormat.JPEG, 50, fileOutputStream);
            } catch (OutOfMemoryError e) {
                Toast.makeText(context, (int) R.string.failed_low_memory, 1).show();
                e.printStackTrace();
            }
            pdfiumCore.closeDocument(newDocument);
        } catch (Exception e2) {
            e2.printStackTrace();
        }
    }

    public static void printPdfFile(Context context, Uri uri) {
        try {
            new PdfiumCore(context).newDocument(context.getContentResolver().openFileDescriptor(uri, "r"));
            if (PrintHelper.systemSupportsPrint()) {
                PrintManager printManager = (PrintManager) context.getSystemService(PDWindowsLaunchParams.OPERATION_PRINT);
                String str = context.getString(R.string.app_name) + " Document";
                if (printManager != null) {
                    printManager.print(str, new AdapterPrintDocument(context, uri), null);
                    return;
                }
                return;
            }
            Toast.makeText(context, (int) R.string.device_does_not_support_printing, 1).show();
        } catch (PdfPasswordException e) {
            Toast.makeText(context, (int) R.string.cant_print_password_protected_pdf, 1).show();
            e.printStackTrace();
        } catch (IOException e2) {
            Toast.makeText(context, (int) R.string.cannot_print_malformed_pdf, 1).show();
            e2.printStackTrace();
        } catch (Exception e3) {
            Toast.makeText(context, (int) R.string.cannot_print_unknown_error, 1).show();
            e3.printStackTrace();
        }
    }

    public static void startShareActivity(Context context) {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.SEND");
        intent.putExtra("android.intent.extra.TEXT", "Hi! 'am using this nice app 'PDF Reader' for reading and manipulating PDF files. You can find it on Google Play or at this link https://play.google.com/store/apps/details?id=com.pdftools.pdfreader.pdfviewer");
        intent.setType("text/plain");
        Intent createChooser = Intent.createChooser(intent, context.getResources().getString(R.string.chooser_title));
        createChooser.setFlags(268435456);
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(new Intent(createChooser));
        }
    }

    public static void setLightStatusBar(Context context) {
        Activity activity = (Activity) context;
        View decorView = activity.getWindow().getDecorView();
        int systemUiVisibility = decorView.getSystemUiVisibility();
        int color = context.getResources().getColor(R.color.light_color);
        int i = Build.VERSION.SDK_INT;
        if (i >= 21 && i >= 23) {
            decorView.setSystemUiVisibility(systemUiVisibility & -8193);
            activity.getWindow().setStatusBarColor(color);
        }
    }

    public static void clearLightStatusBar(Context context) {
        Activity activity = (Activity) context;
        View decorView = activity.getWindow().getDecorView();
        int systemUiVisibility = decorView.getSystemUiVisibility();
        int color = context.getResources().getColor(R.color.light_color);
        int i = Build.VERSION.SDK_INT;
        if (i >= 21 && i >= 23) {
            decorView.setSystemUiVisibility(systemUiVisibility | 8192);
            activity.getWindow().setStatusBarColor(color);
        }
    }

    public static ActivityManager.MemoryInfo getAvailableMemory(Context context) {
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        ((ActivityManager) context.getSystemService("activity")).getMemoryInfo(memoryInfo);
        return memoryInfo;
    }

    public static void IntProgress(Context context) {
        processDialog = new Dialog(context);
        processDialog.requestWindowFeature(1);
        processDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        processDialog.setContentView(R.layout.progress_loading);
        processDialog.setCancelable(false);
    }

    public static void ShowProgress() {
        try {
            processDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void DismissProgress() {
        try {
            if (processDialog != null && processDialog.isShowing()) {
                processDialog.dismiss();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}