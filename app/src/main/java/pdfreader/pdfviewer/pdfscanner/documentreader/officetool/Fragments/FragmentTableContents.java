package pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.R;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Adapter.AdapterContents;
import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FragmentTableContents extends Fragment {
    public static final String SAVED_STATE = "prefs_saved_state";
    private static final String PDF_PATH = "pdf_path";
    public LinearLayout layNoPdfTableData;
    public String mPdfPath;
    AdapterContents contentsAdapter;
    Context context;
    int lastFirstVisiblePosition = 0;
    RecyclerView recyclerPdfTable;
    SharedPreferences sharedPreferences;

    public static FragmentTableContents newInstance(String str) {
        FragmentTableContents tableContentsFragment = new FragmentTableContents();
        Bundle bundle = new Bundle();
        bundle.putString(PDF_PATH, str);
        tableContentsFragment.setArguments(bundle);
        return tableContentsFragment;
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        context = getContext();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (getArguments() != null) {
            mPdfPath = getArguments().getString(PDF_PATH);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        recyclerPdfTable = view.findViewById(R.id.recyclerPdfTable);
        layNoPdfTableData = view.findViewById(R.id.layNoPdfTableData);
        new LoadTableOfPdfContentsData().execute();
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        return layoutInflater.inflate(R.layout.fragment_table_contents, viewGroup, false);
    }

    @Override
    public void onDestroy() {
        LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerPdfTable.getLayoutManager();
        if (linearLayoutManager != null) {
            sharedPreferences.edit().putInt(SAVED_STATE, linearLayoutManager.findFirstCompletelyVisibleItemPosition()).apply();
        }
        super.onDestroy();
    }

    public class LoadTableOfPdfContentsData extends AsyncTask<Void, Void, Void> {
        List<PdfDocument.Bookmark> contents = new ArrayList<>();
        PdfDocument pdfDocument;
        PdfiumCore pdfiumCore;

        public LoadTableOfPdfContentsData() {
        }

        public void onPreExecute() {
            super.onPreExecute();
        }

        public Void doInBackground(Void... voidArr) {
            lastFirstVisiblePosition = sharedPreferences.getInt(FragmentTableContents.SAVED_STATE, 0);
            try {
                pdfiumCore = new PdfiumCore(context);
                pdfDocument = pdfiumCore.newDocument(context.getContentResolver().openFileDescriptor(Uri.fromFile(new File(mPdfPath)), "r"));
                contents = pdfiumCore.getTableOfContents(pdfDocument);
            } catch (Exception e) {
                e.printStackTrace();
            } catch (StackOverflowError e2) {
                e2.printStackTrace();
            }
            contentsAdapter = new AdapterContents(context, contents);
            return null;
        }

        public void onPostExecute(Void r5) {
            super.onPostExecute(r5);
            if (contents.size() == 0) {
                layNoPdfTableData.setVisibility(View.VISIBLE);
            } else {
                layNoPdfTableData.setVisibility(View.GONE);
            }
            recyclerPdfTable.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));
            recyclerPdfTable.setAdapter(contentsAdapter);
            Objects.requireNonNull(recyclerPdfTable.getLayoutManager()).scrollToPosition(lastFirstVisiblePosition);
        }
    }
}