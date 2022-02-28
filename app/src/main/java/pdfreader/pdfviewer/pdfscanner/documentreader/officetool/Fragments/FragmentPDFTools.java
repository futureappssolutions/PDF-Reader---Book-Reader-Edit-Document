package pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Fragments;

import static pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Activity.ActivityPDFTools.MULTI_SELECTION;
import static pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Activity.ActivityPDFTools.PDF_PATHS;
import static pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Activity.ActivityPDFTools.PRE_SELECTED_PDF_PATH;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Activity.ActivityEditMetadata;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Activity.ActivityExtractTextsPages;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Activity.ActivityOrganizeMergePDF;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Activity.ActivityOrganizePages;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Ads.GoogleAppLovinAds;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.R;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Activity.ActivitySelectImages;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Activity.ActivitySelectPDF;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Adapter.ToolsAdapterHome;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.data.ToolsData;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.utils.PDFTools;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.utils.Utils;

import com.shockwave.pdfium.PdfiumCore;

import java.io.File;
import java.util.ArrayList;

public class FragmentPDFTools extends Fragment implements ToolsAdapterHome.OnSelectedMenuClickListener {
    public Context context;
    public Uri preSelectedPdfUri;
    public ConstraintLayout progressMain;
    public ImageView imgCloseProgress;
    public int toolPosition;

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        return layoutInflater.inflate(R.layout.fragment_pdf_tools, viewGroup, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        context = getContext();


        imgCloseProgress = view.findViewById(R.id.imgCloseProgress);
        progressMain = view.findViewById(R.id.progressMain);
        RecyclerView recyclerView = view.findViewById(R.id.recyclePdfTools);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(context, 3, RecyclerView.VERTICAL, false);
        ToolsAdapterHome toolsAdapter = new ToolsAdapterHome(context, ToolsData.getTools(requireActivity()), this);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(toolsAdapter);
        preSelectedPdfUri = getPreSelectedPdfUri();

        new RemoveTempFolderData().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        imgCloseProgress.setOnClickListener(view1 -> {
            closeProgressBar(context);
        });
    }

    public void closeProgressBar(Context context2) {
        imgCloseProgress.setVisibility(View.GONE);
        PDFTools.progressDownloading.setVisibility(View.VISIBLE);
        PDFTools.progressDownloading.setProgress(0);
        Utils.clearLightStatusBar(context2);
        progressMain.setVisibility(View.GONE);
    }

    @Override
    public void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
        if (i == 4842 && i2 == -1) {
            startPdfTool(toolPosition, intent.getStringArrayListExtra(PDF_PATHS));
        }
    }

    public void startPdfTool(int i, ArrayList<String> arrayList) {
        PDFTools pDFTools = new PDFTools();
        switch (i) {
            case 0:
                return;
            case 1:
                new LoadSplittingPdfTools(arrayList.get(0)).execute();
                return;
            case 2:
                imgQualityDialog(arrayList.get(0));
                return;
            case 3:
                pDFTools.getClass();
                new PDFTools.ConvertPDFToPdfPictures(context, arrayList.get(0), progressMain).execute();
                return;
            case 4:
                pdfOrganizePages(arrayList.get(0));
                return;
            case 5:
                pdfDocumentMetadata(arrayList.get(0));
                return;
            case 6:
                pdfCompressionOptions(arrayList.get(0));
                return;
            case 7:
                extractPdfTextsPages(arrayList.get(0));
                return;
            default:
                Toast.makeText(context, "Clicked", Toast.LENGTH_LONG).show();
        }
    }

    public Uri getPreSelectedPdfUri() {
        String stringExtra = requireActivity().getIntent().getStringExtra(PRE_SELECTED_PDF_PATH);
        if (!TextUtils.isEmpty(stringExtra)) {
            return Uri.parse(stringExtra);
        }
        return null;
    }

    public void pdfDocumentMetadata(String str) {
        Intent intent = new Intent(context, ActivityEditMetadata.class);
        intent.putExtra("com.example.pdfreader.PDF_PATH", str);
        startActivity(intent);
    }

    public void pdfOrganizePages(String str) {
        Intent intent = new Intent(context, ActivityOrganizePages.class);
        intent.putExtra("com.example.pdfreader.PDF_PATH", str);
        startActivity(intent);
    }

    public void extractPdfTextsPages(String str) {
        Intent intent = new Intent(context, ActivityExtractTextsPages.class);
        intent.putExtra("com.example.pdfreader.PDF_PATH", str);
        startActivity(intent);
    }

    public void StartIntent() {
        Intent intent2 = new Intent(context, ActivityOrganizeMergePDF.class);
        ArrayList<String> arrayList2 = new ArrayList<>();
        arrayList2.add(preSelectedPdfUri.getPath());
        intent2.putStringArrayListExtra(PDF_PATHS, arrayList2);
        startActivity(intent2);
    }

    public void pdfCompressionLevelDialog(final String str) {
        final int[] iArr = {50};
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        final SharedPreferences.Editor edit = defaultSharedPreferences.edit();
        builder.setTitle(R.string.compression_level).setSingleChoiceItems(R.array.compression_level, defaultSharedPreferences.getInt("prefs_checked_compression_quality", 1), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                edit.putInt("prefs_checked_compression_quality", i);
                if (i == 0) {
                    iArr[0] = 70;
                } else if (i == 1) {
                    iArr[0] = 50;
                } else if (i == 2) {
                    iArr[0] = 20;
                }
            }
        }).setPositiveButton(R.string.compress, (dialogInterface, i) -> {
            edit.apply();
            new PDFTools().getClass();
            new PDFTools.CompressPDFImproved(context, str, iArr[0], progressMain).execute();
        }).setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
        });
        builder.create().show();
    }

    public void pdfSplittingDialog(final String str, final int i) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.split_pdf).setView(getLayoutInflater().inflate(R.layout.dialog_split_options, null)).setPositiveButton(R.string.ok, (DialogInterface.OnClickListener) null).setNegativeButton(R.string.cancel, (DialogInterface.OnClickListener) null);
        final AlertDialog create = builder.create();
        create.show();
        final EditText editText = create.findViewById(R.id.etSplitFrom);
        final EditText editText2 = create.findViewById(R.id.etSplitTo);
        ((TextView) create.findViewById(R.id.tvNumberOfPages)).setText(getString(R.string.number_of_pages) + " " + i);
        final int[] iArr = {0};
        ((Spinner) create.findViewById(R.id.spinnerSplitting)).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long j) {
                if (i == 0) {
                    iArr[0] = 0;
                    editText.setVisibility(View.INVISIBLE);
                    editText2.setVisibility(View.INVISIBLE);
                    editText.setText("");
                    editText2.setText("");
                } else if (i == 1) {
                    iArr[0] = 1;
                    editText.setVisibility(View.VISIBLE);
                    editText2.setVisibility(View.INVISIBLE);
                    editText.setHint(R.string.at);
                    editText.setText("");
                    editText2.setText("");
                } else if (i == 2) {
                    iArr[0] = 2;
                    editText.setVisibility(View.VISIBLE);
                    editText2.setVisibility(View.VISIBLE);
                    editText.setHint(R.string.from);
                    editText.setText("");
                    editText2.setText("");
                }
            }
        });

        create.getButton(-1).setOnClickListener(view -> {
            int i1 = 0;
            PDFTools pDFTools = new PDFTools();
            int i2 = iArr[0];
            if (i2 == 0) {
                pDFTools.getClass();
                new PDFTools.SplitPDF(context, str, progressMain).execute();
                create.cancel();
            } else if (i2 == 1) {
                int intValue = TextUtils.isEmpty(editText.getText().toString()) ? 0 : Integer.parseInt(editText.getText().toString());
                if (intValue <= 0 || intValue > i1) {
                    editText.setError(getString(R.string.invalid_value));
                    return;
                }
                pDFTools.getClass();
                new PDFTools.SplitPDF(context, str, progressMain, intValue).execute();
                create.cancel();
            } else if (i2 == 2) {
                int intValue2 = TextUtils.isEmpty(editText.getText().toString()) ? 0 : Integer.parseInt(editText.getText().toString());
                int intValue3 = TextUtils.isEmpty(editText2.getText().toString()) ? 0 : Integer.parseInt(editText2.getText().toString());
                if (intValue2 <= 0 || intValue2 > (i1 = i1)) {
                    editText.setError(getString(R.string.invalid_value));
                } else if (intValue3 <= 0 || intValue3 > i1 || intValue3 <= intValue2) {
                    editText2.setError(getString(R.string.invalid_value));
                } else {
                    pDFTools.getClass();
                    new PDFTools.SplitPDF(context, str, progressMain, intValue2, intValue3).execute();
                    create.cancel();
                }
            }
        });
    }

    public void imgQualityDialog(final String str) {
        final int[] iArr = {50};
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        final SharedPreferences.Editor edit = defaultSharedPreferences.edit();
        builder.setTitle(R.string.image_quality).setSingleChoiceItems(R.array.compression_level, defaultSharedPreferences.getInt("prefs_checked_img_quality", 1), (dialogInterface, i) -> {
            edit.putInt("prefs_checked_img_quality", i);
            if (i == 0) {
                iArr[0] = 30;
            } else if (i == 1) {
                iArr[0] = 65;
            } else if (i == 2) {
                iArr[0] = 100;
            }
        }).setPositiveButton(R.string.extract, (dialogInterface, i) -> {
            edit.apply();
            new PDFTools().getClass();
            new PDFTools.ExtractPdfImages(context, str, iArr[0], progressMain).execute();
        }).setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
        });
        builder.create().show();
    }

    public void pdfCompressionOptions(String str) {
        if (!Utils.getAvailableMemory(context).lowMemory) {
            pdfCompressionLevelDialog(str);
        } else {
            Toast.makeText(context, R.string.cant_compress_low_memory, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onToolClicked(int i) {
        toolPosition = i;
        switch (i) {
            case 8:
                if (GoogleAppLovinAds.adsdisplay) {
                    GoogleAppLovinAds.showFullAds((Activity) context, () -> {
                        GoogleAppLovinAds.allcount60.start();
                        startActivityForResult(new Intent(context, ActivitySelectImages.class), 4842);
                    });
                } else {
                    startActivityForResult(new Intent(context, ActivitySelectImages.class), 4842);
                }

                return;
            default:
                Intent intent = new Intent(context, ActivitySelectPDF.class);
                if (i != 0 || preSelectedPdfUri == null) {
                    if (i == 0) {
                        intent.putExtra(MULTI_SELECTION, true);
                    }
                    if (preSelectedPdfUri != null) {
                        ArrayList<String> arrayList = new ArrayList<>();
                        arrayList.add(preSelectedPdfUri.getPath());
                        startPdfTool(i, arrayList);
                        return;
                    }
                    if (GoogleAppLovinAds.adsdisplay) {
                        GoogleAppLovinAds.showFullAds((Activity) context, () -> {
                            GoogleAppLovinAds.allcount60.start();
                            startActivityForResult(intent, 4842);
                        });
                    } else {
                        startActivityForResult(intent, 4842);
                    }
                    return;
                }

                if (GoogleAppLovinAds.adsdisplay) {
                    GoogleAppLovinAds.showFullAds((Activity) context, () -> {
                        GoogleAppLovinAds.allcount60.start();
                        StartIntent();
                    });
                } else {
                    StartIntent();
                }
        }
    }

    public class RemoveTempFolderData extends AsyncTask<Void, Void, Void> {
        public RemoveTempFolderData() {
        }

        public Void doInBackground(Void... voidArr) {
            String str = Environment.getExternalStorageDirectory() + "/Pictures/AllPdf/tmp/";
            if (!new File(str).exists()) {
                return null;
            }
            Utils.deletePdfFiles(str);
            return null;
        }

        public void onPostExecute(Void r2) {
            super.onPostExecute(r2);
        }
    }

    public class LoadSplittingPdfTools extends AsyncTask<Void, Void, Void> {
        String mPdfPath;
        int numPages;
        ProgressDialog progressDialog;

        public LoadSplittingPdfTools(String str) {
            mPdfPath = str;
        }

        public void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage(getString(R.string.loading_wait));
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
        }

        public Void doInBackground(Void... voidArr) {
            PdfiumCore pdfiumCore = new PdfiumCore(context);
            try {
                numPages = pdfiumCore.getPageCount(pdfiumCore.newDocument(context.getContentResolver().openFileDescriptor(Uri.fromFile(new File(mPdfPath)), "r")));
                return null;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        public void onPostExecute(Void r3) {
            super.onPostExecute(r3);
            progressDialog.dismiss();
            pdfSplittingDialog(mPdfPath, numPages);
        }
    }
}
