package pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Adapter;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.SparseBooleanArray;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.R;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.data.DbHelper;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.GetSet.BookmarkData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AdapterBookmarks extends RecyclerView.Adapter<AdapterBookmarks.PDFBookViewHolder> {
    public ActionMode actionMode;
    public ActionModeCallback actionModeCallback;
    public LinearLayout layEmptyData;
    public List<BookmarkData> listBookMarkPDF;
    public Context mContext;
    public OnBookmarkClickedListener onBookmarkClickedListener;
    public SparseBooleanArray selectedBookmarks = new SparseBooleanArray();

    public AdapterBookmarks(Context context, List<BookmarkData> list, LinearLayout linearLayout) {
        listBookMarkPDF = list;
        mContext = context;
        layEmptyData = linearLayout;
        actionModeCallback = new ActionModeCallback();
        if (mContext instanceof OnBookmarkClickedListener) {
            onBookmarkClickedListener = (OnBookmarkClickedListener) mContext;
            return;
        }
        throw new RuntimeException(mContext.toString() + " must implement OnBookmarkClickedListener");
    }

    @NonNull
    @Override
    public PDFBookViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new PDFBookViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_bookmark, viewGroup, false));
    }

    public void onBindViewHolder(final PDFBookViewHolder pDFBookViewHolder, int i) {
        final BookmarkData bookmarkData = listBookMarkPDF.get(i);
        pDFBookViewHolder.tvPDFBookMarkTitle.setText(bookmarkData.getTitle());
        pDFBookViewHolder.tvBookmarkPageNumber.setText(mContext.getString(R.string.page) + " " + bookmarkData.getPageNumber());
        togglePDFSelectedBGColor(pDFBookViewHolder, i);
        pDFBookViewHolder.rLayBookMark.setOnClickListener(view -> {
            if (actionMode != null) {
                getTogglePDFSelected(pDFBookViewHolder.getAdapterPosition());
            } else {
                bookmarkPDFSelected(bookmarkData);
            }
        });

        pDFBookViewHolder.rLayBookMark.setOnLongClickListener(view -> {
            if (actionMode == null) {
                actionMode = ((AppCompatActivity) mContext).startSupportActionMode(actionModeCallback);
            }
            getTogglePDFSelected(pDFBookViewHolder.getAdapterPosition());
            return false;
        });
    }

    public void getTogglePDFSelected(int i) {
        if (selectedBookmarks.get(i, false)) {
            selectedBookmarks.delete(i);
        } else {
            selectedBookmarks.put(i, true);
        }
        notifyItemChanged(i);
        int selectedPDFCount = getSelectedPDFCount();
        if (selectedPDFCount == 0) {
            actionMode.finish();
            return;
        }
        actionMode.setTitle(selectedPDFCount + " " + mContext.getString(R.string.selected));
        actionMode.invalidate();
    }

    private int getSelectedPDFCount() {
        return selectedBookmarks.size();
    }

    private List<Integer> getSelectedPDFPhotos() {
        int size = selectedBookmarks.size();
        ArrayList<Integer> arrayList = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            arrayList.add(selectedBookmarks.keyAt(i));
        }
        return arrayList;
    }

    public void clearSelectedPDF() {
        List<Integer> selectedPDFPhotos = getSelectedPDFPhotos();
        selectedBookmarks.clear();
        for (Integer num : selectedPDFPhotos) {
            notifyItemChanged(num);
        }
    }

    private boolean isSelectedPdf(int i) {
        return getSelectedPDFPhotos().contains(i);
    }

    private void togglePDFSelectedBGColor(PDFBookViewHolder pDFBookViewHolder, int i) {
        if (isSelectedPdf(i)) {
            pDFBookViewHolder.laySelectedItem.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorSelectedPDFs));
            return;
        }
        TypedValue typedValue = new TypedValue();
        mContext.getTheme().resolveAttribute(16843534, typedValue, true);
        pDFBookViewHolder.laySelectedItem.setBackgroundResource(typedValue.resourceId);
    }

    @Override
    public int getItemCount() {
        return listBookMarkPDF.size();
    }

    public void bookmarkPDFSelected(BookmarkData bookmarkData) {
        onBookmarkClickedListener.onBookmarkClicked(bookmarkData);
    }

    public void deleteSelectedPDF(ActionMode actionMode2) {
        DbHelper instance = DbHelper.getInstance(mContext);
        List<Integer> selectedBookmarks2 = getSelectedBookmarks();
        int selectedPDFCount = getSelectedPDFCount();
        ArrayList<BookmarkData> arrayList = new ArrayList<>();
        for (int i = 0; i < selectedPDFCount; i++) {
            arrayList.add(listBookMarkPDF.get(selectedBookmarks2.get(i)));
        }
        removeSelectedPdf(selectedBookmarks2);
        actionMode2.finish();
        instance.deleteBookmarks(arrayList);
    }

    private void removeItem(int i) {
        listBookMarkPDF.remove(i);
        setLayEmptyData();
        notifyItemRemoved(i);
    }

    private void removeSelectedPdf(List<Integer> list) {
        Collections.sort(list, (num, num2) -> num2 - num);
        while (!list.isEmpty()) {
            if (list.size() == 1) {
                removeItem(list.get(0));
                list.remove(0);
            } else {
                int i = 1;
                while (list.size() > i && list.get(i).equals(list.get(i - 1) - 1)) {
                    i++;
                }
                if (i == 1) {
                    removeItem(list.get(0));
                } else {
                    removePDF(list.get(i - 1), i);
                }
                for (int i2 = 0; i2 < i; i2++) {
                    list.remove(0);
                }
            }
        }
    }

    private void removePDF(int i, int i2) {
        for (int i3 = 0; i3 < i2; i3++) {
            listBookMarkPDF.remove(i);
        }
        setLayEmptyData();
        notifyItemRangeRemoved(i, i2);
    }

    private List<Integer> getSelectedBookmarks() {
        int size = selectedBookmarks.size();
        ArrayList<Integer> arrayList = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            arrayList.add(selectedBookmarks.keyAt(i));
        }
        return arrayList;
    }

    private void setLayEmptyData() {
        if (listBookMarkPDF.size() > 0) {
            layEmptyData.setVisibility(View.GONE);
        } else {
            layEmptyData.setVisibility(View.VISIBLE);
        }
    }

    public interface OnBookmarkClickedListener {
        void onBookmarkClicked(BookmarkData bookmarkData);
    }

    public static class PDFBookViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout laySelectedItem;
        public RelativeLayout rLayBookMark;
        public TextView tvBookmarkPageNumber;
        public TextView tvPDFBookMarkTitle;

        public PDFBookViewHolder(View view) {
            super(view);
            tvPDFBookMarkTitle = view.findViewById(R.id.tvPDFBookMarkTitle);
            tvBookmarkPageNumber = view.findViewById(R.id.tvBookmarkPageNumber);
            rLayBookMark = view.findViewById(R.id.rLayBookMark);
            laySelectedItem = view.findViewById(R.id.laySelectedItem);
        }
    }

    private class ActionModeCallback implements ActionMode.Callback {
        View actionView;
        int colorFrom;
        int colorTo;
        int flags;

        private ActionModeCallback() {
            View decorView = ((Activity) mContext).getWindow().getDecorView();
            actionView = decorView;
            flags = decorView.getSystemUiVisibility();
            colorFrom = mContext.getResources().getColor(R.color.white);
            colorTo = mContext.getResources().getColor(R.color.colorDarkerGray);
        }

        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            actionMode.getMenuInflater().inflate(R.menu.fragment_bookmarks, menu);
            int i = Build.VERSION.SDK_INT;
            if (i >= 21) {
                if (i >= 23) {
                    int i2 = flags & -8193;
                    flags = i2;
                    actionView.setSystemUiVisibility(i2);
                }
                ValueAnimator ofObject = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
                ofObject.setDuration(300L);
                ofObject.addUpdateListener(valueAnimator -> ((Activity) mContext).getWindow().setStatusBarColor((Integer) valueAnimator.getAnimatedValue()));
                ofObject.start();
            }
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            if (menuItem.getItemId() != R.id.action_delete) {
                return true;
            }
            deleteSelectedPDF(actionMode);
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {
            clearSelectedPDF();
            int i = Build.VERSION.SDK_INT;
            if (i >= 21) {
                if (i >= 23) {
                    int i2 = flags | 8192;
                    flags = i2;
                    actionView.setSystemUiVisibility(i2);
                }
                ValueAnimator ofObject = ValueAnimator.ofObject(new ArgbEvaluator(), colorTo, colorFrom);
                ofObject.setDuration(300L);
                ofObject.addUpdateListener(valueAnimator -> ((Activity) mContext).getWindow().setStatusBarColor((Integer) valueAnimator.getAnimatedValue()));
                ofObject.start();
            }
        }
    }
}
