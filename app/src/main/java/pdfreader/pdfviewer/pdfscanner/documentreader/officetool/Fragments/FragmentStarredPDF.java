package pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Fragments;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.List;

import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Ads.GoogleAppLovinAds;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.R;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Adapter.AdapterRecentPdfs;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.data.DbHelper;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.GetSet.PdfDataType;

public class FragmentStarredPDF extends Fragment {
    private static final String PDF_PATH = "pdf_path";
    public LinearLayout layNoBookmark;
    public List<PdfDataType> listBookmarkPdfDataTypes = new ArrayList<>();
    public String pdfFilesPath;
    public AdapterRecentPdfs bookmarksAdapter;
    public Context context;
    public RecyclerView recyclerBookmarksPdf;
    public FrameLayout fl_native;
    private SwipeRefreshLayout swipeRecentPdfRefresh;

    public static FragmentStarredPDF newInstance(String str) {
        FragmentStarredPDF starredPDFFragment = new FragmentStarredPDF();
        Bundle bundle = new Bundle();
        bundle.putString(PDF_PATH, str);
        starredPDFFragment.setArguments(bundle);
        return starredPDFFragment;
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        return layoutInflater.inflate(R.layout.fragment_starred_pdf, viewGroup, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        fl_native = view.findViewById(R.id.fl_native);
        GoogleAppLovinAds.showNativeAds(getActivity(),fl_native);


        recyclerBookmarksPdf = view.findViewById(R.id.recyclerBookmarksPdf);
        layNoBookmark = view.findViewById(R.id.layNoBookmark);
        swipeRecentPdfRefresh = view.findViewById(R.id.swipeRecentPdfRefresh);

        new BookmarksPdfLoad().execute();

        swipeRecentPdfRefresh.setOnRefreshListener(() -> new BookmarksPdfLoad().execute(new Void[0]));
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        context = getContext();
        if (getArguments() != null) {
            pdfFilesPath = getArguments().getString(PDF_PATH);
        }
    }

    public class BookmarksPdfLoad extends AsyncTask<Void, Void, Void> {
        public BookmarksPdfLoad() {
        }

        public void onPreExecute() {
            super.onPreExecute();
        }

        public Void doInBackground(Void... voidArr) {
            listBookmarkPdfDataTypes = DbHelper.getInstance(context).getStarredPdfs();
            bookmarksAdapter = new AdapterRecentPdfs(listBookmarkPdfDataTypes, context);
            return null;
        }

        public void onPostExecute(Void r5) {
            super.onPostExecute(r5);
            swipeRecentPdfRefresh.setRefreshing(false);
            if (listBookmarkPdfDataTypes.size() == 0) {
                layNoBookmark.setVisibility(View.VISIBLE);
                fl_native.setVisibility(View.GONE);
                return;
            }
            recyclerBookmarksPdf.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));
            recyclerBookmarksPdf.setAdapter(bookmarksAdapter);
            layNoBookmark.setVisibility(View.GONE);
            fl_native.setVisibility(View.VISIBLE);
        }
    }
}