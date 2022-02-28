package pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Activity.ActivityMain;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.R;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Adapter.AdapterFileBrowser;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.GetSet.PdfDataType;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.utils.Utils;
import com.itextpdf.text.pdf.PdfObject;
import com.itextpdf.text.xml.xmp.PdfSchema;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FragmentFileList extends Fragment implements AdapterFileBrowser.OnPdfClickListener {
    private static final String FILE_PATH = "file_path";
    public String filePath;
    Context context;
    AdapterFileBrowser fileBrowserAdapter;
    boolean isGridViewEnabled;
    LinearLayout layNoBrowserData;
    List<PdfDataType> listPdfdir = new ArrayList<>();
    int numberOfColumns;
    ProgressBar progressBrowsePdf;
    RecyclerView recyclerBrowsePdf;
    String thumbleDir;

    public static FragmentFileList newInstance(String str) {
        FragmentFileList fileListFragment = new FragmentFileList();
        Bundle bundle = new Bundle();
        bundle.putString(FILE_PATH, str);
        fileListFragment.setArguments(bundle);
        return fileListFragment;
    }

    @Override
    public void onPdfClicked(PdfDataType pdfDataType) {
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        context = getContext();
        if (getArguments() != null) {
            filePath = getArguments().getString(FILE_PATH);
            thumbleDir = context.getCacheDir() + "/Thumbnails/";
            int i = Utils.isTablet(context) ? 6 : 3;
            SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            numberOfColumns = defaultSharedPreferences.getInt(ActivityMain.GRID_VIEW_NUM_OF_COLUMNS, i);
            isGridViewEnabled = defaultSharedPreferences.getBoolean(ActivityMain.GRID_VIEW_ENABLED, false);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        recyclerBrowsePdf = view.findViewById(R.id.recyclerBrowsePdf);
        progressBrowsePdf = view.findViewById(R.id.progressBrowsePdf);
        layNoBrowserData = view.findViewById(R.id.layNoBrowserData);
        new listPdfFileDirectory().execute();
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        return layoutInflater.inflate(R.layout.fragment_file_list, viewGroup, false);
    }

    @Override
    public void onAttach(@NonNull Context context2) {
        super.onAttach(context2);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public List<PdfDataType> getAllFilesList(String str) {
        File[] listFiles;
        final File file = new File(str);
        ArrayList<PdfDataType> arrayList = new ArrayList<>();
        if (file.isDirectory() && (listFiles = file.listFiles(file1 -> {
            String fileExtensionFromUrl = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(file1).toString());
            return (file1.isDirectory() && !file1.isHidden()) || TextUtils.equals(fileExtensionFromUrl, PdfSchema.DEFAULT_XPATH_ID) || TextUtils.equals(fileExtensionFromUrl, PdfObject.TEXT_PDFDOCENCODING);
        })) != null) {
            for (File file2 : listFiles) {
                Uri imageUriFromPath = !file2.isDirectory() ? Utils.getImageUriFromPath(thumbleDir + Utils.removePdfExtension(file2.getName()) + ".jpg") : null;
                int size = file2.isDirectory() ? getAllFilesList(file2.getAbsolutePath()).size() : 0;
                PdfDataType pdfDataType = new PdfDataType();
                pdfDataType.setName(file2.getName());
                pdfDataType.setAbsolutePath(file2.getAbsolutePath());
                pdfDataType.setPdfUri(Uri.fromFile(file2));
                pdfDataType.setLength(file2.length());
                pdfDataType.setLastModified(file2.lastModified());
                pdfDataType.setThumbUri(imageUriFromPath);
                pdfDataType.setDirectory(file2.isDirectory());
                pdfDataType.setNumItems(size);
                arrayList.add(pdfDataType);
            }
        }

        Collections.sort(arrayList, (pdfDataType, pdfDataType2) -> {
            if (pdfDataType.isDirectory() && !pdfDataType2.isDirectory()) {
                return -1;
            }
            if (pdfDataType.isDirectory() || !pdfDataType2.isDirectory()) {
                return pdfDataType.getName().compareToIgnoreCase(pdfDataType2.getName());
            }
            return 1;
        });
        return arrayList;
    }

    public class listPdfFileDirectory extends AsyncTask<Void, Void, Void> {
        public listPdfFileDirectory() {
        }

        @SuppressLint("ResourceType")
        public void onPreExecute() {
            super.onPreExecute();
            progressBrowsePdf.setVisibility(View.VISIBLE);
            if (isGridViewEnabled) {
                recyclerBrowsePdf.setBackgroundColor(getResources().getColor(R.color.light_color));
                recyclerBrowsePdf.setLayoutManager(new GridLayoutManager(context, numberOfColumns, RecyclerView.VERTICAL, false));
                return;
            }
            recyclerBrowsePdf.setBackgroundColor(getResources().getColor(R.color.light_color));
            recyclerBrowsePdf.setLayoutManager(new LinearLayoutManager(context, 1, false));
        }

        public Void doInBackground(Void... voidArr) {
            listPdfdir = getAllFilesList(filePath);
            fileBrowserAdapter = new AdapterFileBrowser(context, listPdfdir);
            return null;
        }

        public void onPostExecute(Void r3) {
            super.onPostExecute(r3);
            progressBrowsePdf.setVisibility(View.GONE);
            recyclerBrowsePdf.setAdapter(fileBrowserAdapter);
            if (listPdfdir.size() == 0) {
                layNoBrowserData.setVisibility(View.VISIBLE);
            } else {
                layNoBrowserData.setVisibility(View.GONE);
            }
        }
    }
}