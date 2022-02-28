package pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.R;
import com.shockwave.pdfium.PdfDocument;

import java.util.List;

public class AdapterContents extends RecyclerView.Adapter<AdapterContents.PdfFileContentsViewHolder> {
    public Context context;
    public List<PdfDocument.Bookmark> listBookmarksFiles;
    public OnContentClickedListener onContentClickedListener;

    public AdapterContents(Context context2, List<PdfDocument.Bookmark> list) {
        listBookmarksFiles = list;
        context = context2;
        if (context2 instanceof OnContentClickedListener) {
            onContentClickedListener = (OnContentClickedListener) context2;
            return;
        }
        throw new RuntimeException(context.toString() + " must implement OnContentClickedListener");
    }

    @NonNull
    @Override
    public PdfFileContentsViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new PdfFileContentsViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_contents, viewGroup, false));
    }

    public void onBindViewHolder(PdfFileContentsViewHolder pdfFileContentsViewHolder, int i) {
        final PdfDocument.Bookmark bookmark = listBookmarksFiles.get(i);
        pdfFileContentsViewHolder.tvContentFileTitle.setText(bookmark.getTitle());
        pdfFileContentsViewHolder.tvContentFilePage.setText(context.getString(R.string.page) + " " + (bookmark.getPageIdx() + 1));
        pdfFileContentsViewHolder.rlayContents.setOnClickListener(view -> contentClicked(bookmark));
    }

    @Override
    public int getItemCount() {
        return listBookmarksFiles.size();
    }

    public void contentClicked(PdfDocument.Bookmark bookmark) {
        onContentClickedListener.onContentClicked(bookmark);
    }

    public interface OnContentClickedListener {
        void onContentClicked(PdfDocument.Bookmark bookmark);
    }

    public static class PdfFileContentsViewHolder extends RecyclerView.ViewHolder {
        public RelativeLayout rlayContents;
        public TextView tvContentFilePage;
        public TextView tvContentFileTitle;

        public PdfFileContentsViewHolder(View view) {
            super(view);
            tvContentFileTitle = view.findViewById(R.id.tvContentFileTitle);
            tvContentFilePage = view.findViewById(R.id.tvContentFilePage);
            rlayContents = view.findViewById(R.id.rlayContents);
        }
    }
}
