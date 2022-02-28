package pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Fragments;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.R;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Adapter.AdapterBookmarks;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.data.DbHelper;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.GetSet.BookmarkData;

public class FragmentBookmarks extends Fragment {
    private static final String PDF_PATH = "pdf_path";
    public LinearLayout layNoBookmark;
    public String pdfFilesPath;
    public AdapterBookmarks bookmarksAdapter;
    public Context context;
    public RecyclerView recyclerBookmarksPdf;

    public static FragmentBookmarks newInstance(String str) {
        FragmentBookmarks bookmarksFragment = new FragmentBookmarks();
        Bundle bundle = new Bundle();
        bundle.putString(PDF_PATH, str);
        bookmarksFragment.setArguments(bundle);
        return bookmarksFragment;
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        return layoutInflater.inflate(R.layout.fragment_bookmarks, viewGroup, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        recyclerBookmarksPdf = view.findViewById(R.id.recyclerBookmarksPdf);
        layNoBookmark = view.findViewById(R.id.layNoBookmark);
        new BookmarksPdfLoad().execute();
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
        List<BookmarkData> listBookmarkData = new ArrayList<>();

        public BookmarksPdfLoad() {
        }

        public void onPreExecute() {
            super.onPreExecute();
        }

        public Void doInBackground(Void... voidArr) {
            listBookmarkData = DbHelper.getInstance(context).getBookmarks(pdfFilesPath);
            bookmarksAdapter = new AdapterBookmarks(context, listBookmarkData, layNoBookmark);
            return null;
        }

        public void onPostExecute(Void r5) {
            super.onPostExecute(r5);
            if (listBookmarkData.size() == 0) {
                layNoBookmark.setVisibility(View.VISIBLE);
                return;
            }
            recyclerBookmarksPdf.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));
            recyclerBookmarksPdf.setAdapter(bookmarksAdapter);
            layNoBookmark.setVisibility(View.GONE);
        }
    }
}