package pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.utils.DataUpdatedEvent;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Activity.ActivityPDFTools;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.R;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Activity.ActivityShareAsPicture;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.data.DbHelper;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.utils.Utils;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.Objects;

public class FragmentBottomSheetDialog extends com.google.android.material.bottomsheet.BottomSheetDialogFragment {
    public static final String FROM_RECENT = "com.example.pdfreader.FROM_RECENT";
    public static final String PDF_PATH = "com.example.pdfreader.PDF_PATH";
    Context context;
    String fileName;
    Boolean fromRecent;
    ImageView imgBookMarkPdf;
    String pdfPath;
    BottomSheetBehavior.BottomSheetCallback mBottomSheetBehaviorCallback = new BottomSheetBehavior.BottomSheetCallback() {
        @Override
        public void onSlide(@NonNull View view, float f) {
        }

        @Override
        public void onStateChanged(@NonNull View view, int i) {
            if (i == 5) {
                dismiss();
            }
        }
    };

    @Override
    public void setupDialog(Dialog dialog, int i) {
        Bundle arguments = getArguments();
        pdfPath = Objects.requireNonNull(arguments).getString(FROM_RECENT);
        fileName = new File(pdfPath).getName();
        fromRecent = arguments.getBoolean("fromRecent");
        context = getContext();
        View inflate = View.inflate(context, R.layout.fragment_bottom_sheet_dialog, null);
        dialog.setContentView(inflate);
        imgBookMarkPdf = inflate.findViewById(R.id.imgBookMarkPdf);
        ((TextView) inflate.findViewById(R.id.tvPdfFileName)).setText(fileName);

        setupPdfStared();

        CoordinatorLayout.Behavior behavior = ((CoordinatorLayout.LayoutParams) ((View) inflate.getParent()).getLayoutParams()).getBehavior();

        inflate.findViewById(R.id.laySharePdf).setOnClickListener(v -> {
            try {
                Utils.sharePdfFile(context, FileProvider.getUriForFile(context, context.getPackageName() + ".provider", new File(pdfPath)));
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(context, R.string.cant_share_file, Toast.LENGTH_LONG).show();
            }
            dialog.dismiss();
        });

        inflate.findViewById(R.id.layRenamePdf).setOnClickListener(v -> {
            renamePdf();
            dialog.dismiss();
        });

        inflate.findViewById(R.id.layPdfReader).setOnClickListener(v -> {
            openPdfReader();
            dialog.dismiss();
        });

        inflate.findViewById(R.id.layPdfPrint).setOnClickListener(v -> {
            Utils.printPdfFile(context, Uri.fromFile(new File(pdfPath)));
            dialog.dismiss();
        });

        inflate.findViewById(R.id.layPdfDelete).setOnClickListener(v -> {
            deletePdfConfirmDialog();
            dialog.dismiss();
        });

        inflate.findViewById(R.id.laySharePdfPicture).setOnClickListener(v -> {
            shareAsPicture(Uri.fromFile(new File(pdfPath)));
            dialog.dismiss();
        });

        inflate.findViewById(R.id.layPdfSaveLocation).setOnClickListener(v -> Toast.makeText(context, pdfPath, Toast.LENGTH_LONG).show());

        imgBookMarkPdf.setOnClickListener(view -> {
            dismiss();
            DbHelper instance = DbHelper.getInstance(context);
            if (instance.isStared(pdfPath)) {
                instance.removeStaredPDF(pdfPath);
            } else {
                instance.addStaredPDF(pdfPath);
            }
            EventBus.getDefault().post(new DataUpdatedEvent.RecentPDFStaredEvent());
            EventBus.getDefault().post(new DataUpdatedEvent.DevicePDFStaredEvent());
        });
        if (behavior instanceof BottomSheetBehavior) {
            ((BottomSheetBehavior) behavior).setBottomSheetCallback(mBottomSheetBehaviorCallback);
        }
    }

    public void deletePdf() {
        if (fromRecent) {
            DbHelper.getInstance(context).deleteRecentPDF(pdfPath);
        } else {
            deletePdfConfirmDialog();
        }
    }

    public void deletePdfConfirmDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.permanently_delete_file).setPositiveButton(R.string.delete, (dialogInterface, i) -> {
            File file = new File(pdfPath);
            if (file.delete()) {
                new File(context.getCacheDir() + "/Thumbnails/" + Utils.removePdfExtension(file.getName()) + ".jpg").delete();
                MediaScannerConnection.scanFile(context, new String[]{pdfPath}, null, (str, uri) -> EventBus.getDefault().post(new DataUpdatedEvent.PermanetlyDeleteEvent()));
                return;
            }
            Toast.makeText(context, "Can't delete pdf file", Toast.LENGTH_LONG).show();
        }).setNegativeButton(R.string.cancel, (dialogInterface, i) -> dialogInterface.cancel());
        builder.create().show();
    }

    public void renamePdf() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final File file = new File(pdfPath);
        final String removePdfExtension = Utils.removePdfExtension(file.getName());
        float f = context.getResources().getDisplayMetrics().density;
        final EditText editText = new EditText(context);
        editText.setText(removePdfExtension);
        editText.setSelectAllOnFocus(true);
        builder.setTitle(R.string.rename_file).setPositiveButton(R.string.ok, null).setNegativeButton(R.string.cancel, (dialogInterface, i) -> dialogInterface.dismiss());
        final AlertDialog create = builder.create();
        int i = (int) (24.0f * f);
        create.setView(editText, i, (int) (8.0f * f), i, (int) (f * 5.0f));
        create.show();
        create.getButton(-1).setOnClickListener(view -> {
            String obj = editText.getText().toString();
            if (TextUtils.equals(removePdfExtension, obj)) {
                create.dismiss();
            } else if (Utils.isFileNameValid(obj)) {
                final String replace = pdfPath.replace(removePdfExtension, obj);
                if (file.renameTo(new File(replace))) {
                    create.dismiss();
                    DbHelper instance = DbHelper.getInstance(context);
                    instance.updateHistory(pdfPath, replace);
                    instance.updateStaredPDF(pdfPath, replace);
                    instance.updateBookmarkPath(pdfPath, replace);
                    instance.updateLastOpenedPagePath(pdfPath, replace);
                    String str = context.getCacheDir() + "/Thumbnails/";
                    String str2 = str + Utils.removePdfExtension(file.getName()) + ".jpg";
                    String str3 = str + Utils.removePdfExtension(obj) + ".jpg";
                    new File(str2).renameTo(new File(str3));
                    MediaScannerConnection.scanFile(context, new String[]{replace}, null, (str1, uri) -> EventBus.getDefault().post(new DataUpdatedEvent.PdfRenameEvent()));
                    return;
                }
                Toast.makeText(context, R.string.failed_to_rename_file, Toast.LENGTH_LONG).show();
            } else {
                editText.setError(context.getString(R.string.invalid_file_name));
            }
        });
    }

    public void shareAsPicture(Uri uri) {
        Intent intent = new Intent(context, ActivityShareAsPicture.class);
        intent.putExtra("com.example.pdfreader.PDF_PATH", uri.toString());
        startActivity(intent);
    }

    public void openPdfReader() {
        Uri fromFile = Uri.fromFile(new File(pdfPath));
        Intent intent = new Intent(context, ActivityPDFTools.class);
        intent.putExtra(ActivityPDFTools.PRE_SELECTED_PDF_PATH, fromFile.toString());
        startActivity(intent);
    }

    public void setupPdfStared() {
        if (DbHelper.getInstance(context).isStared(pdfPath)) {
            imgBookMarkPdf.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_action_star_yellow));
        }
    }
}