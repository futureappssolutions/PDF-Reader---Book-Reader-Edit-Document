package pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Adapter;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.recyclerview.widget.RecyclerView;

import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.R;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.GetSet.PDFPage;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AdapterExtractTextsPages extends RecyclerView.Adapter<AdapterExtractTextsPages.PdfOrgnalPagesViewHolder> {
    public ActionMode actionMode;
    public Context context;
    public SetActionBarThemeCallback setActionBarThemeCallback;
    public List<PDFPage> listPdfFilePages;
    public SparseBooleanArray selectedPages = new SparseBooleanArray();
    boolean clearSelection = true;

    public AdapterExtractTextsPages(Context context2, List<PDFPage> list) {
        listPdfFilePages = list;
        context = context2;
        setActionBarThemeCallback = new SetActionBarThemeCallback();
        StringBuilder sb = new StringBuilder();
        sb.append("number of thumbs ");
        sb.append(list.size());
    }

    @NonNull
    @Override
    public PdfOrgnalPagesViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new PdfOrgnalPagesViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_organize_pages_grid, viewGroup, false));
    }

    public void onBindViewHolder(final PdfOrgnalPagesViewHolder pdfOrgnalPagesViewHolder, int i) {
        PDFPage pDFPage = listPdfFilePages.get(i);
        Picasso.get().load(pDFPage.getThumbnailUri()).fit().into(pdfOrgnalPagesViewHolder.imgPdfImage);
        pdfOrgnalPagesViewHolder.tvPageNumber.setText(String.valueOf(pDFPage.getPageNumber()));
        selectChangeBackground(pdfOrgnalPagesViewHolder, i);
        pdfOrgnalPagesViewHolder.rLayMain.setOnClickListener(view -> {
            int adapterPosition = pdfOrgnalPagesViewHolder.getAdapterPosition();
            if (actionMode == null) {
                AdapterExtractTextsPages extractTextsPagesAdapter = AdapterExtractTextsPages.this;
                extractTextsPagesAdapter.actionMode = ((AppCompatActivity) extractTextsPagesAdapter.context).startSupportActionMode(setActionBarThemeCallback);
            }
            adpPosSelection(adapterPosition);
            StringBuilder sb = new StringBuilder();
            sb.append("Clicked position ");
            sb.append(adapterPosition);
        });
    }

    public void adpPosSelection(int i) {
        if (selectedPages.get(i, false)) {
            selectedPages.delete(i);
        } else {
            selectedPages.put(i, true);
        }
        notifyItemChanged(i);
        int size = selectedPages.size();
        if (size == 0) {
            actionMode.finish();
            return;
        }
        ActionMode actionMode2 = actionMode;
        actionMode2.setTitle(size + " " + context.getString(R.string.selected));
        actionMode.invalidate();
    }

    private void selectChangeBackground(PdfOrgnalPagesViewHolder pdfOrgnalPagesViewHolder, int i) {
        if (isPAgeSelected(i)) {
            pdfOrgnalPagesViewHolder.layOrgnizePage.setVisibility(View.VISIBLE);
        } else {
            pdfOrgnalPagesViewHolder.layOrgnizePage.setVisibility(View.GONE);
        }
    }

    private boolean isPAgeSelected(int i) {
        return getSelectedPages().contains(i);
    }

    @Override
    public int getItemCount() {
        return listPdfFilePages.size();
    }

    public void deleteSelectedPAge() {
        List<Integer> selectedPages2 = getSelectedPages();
        selectedPages.clear();
        for (Integer num : selectedPages2) {
            notifyItemChanged(num);
        }
    }

    public List<Integer> getSelectedPages() {
        int size = selectedPages.size();
        ArrayList<Integer> arrayList = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            arrayList.add(selectedPages.keyAt(i));
        }
        return arrayList;
    }

    private void deletePdfFilePage(int i) {
        listPdfFilePages.remove(i);
        notifyItemRemoved(i);
    }

    public void deletePdfPage(List<Integer> list) {
        Collections.sort(list, (num, num2) -> num2 - num);
        while (!list.isEmpty()) {
            if (list.size() == 1) {
                deletePdfFilePage(list.get(0));
                list.remove(0);
            } else {
                int i = 1;
                while (list.size() > i && list.get(i).equals(list.get(i - 1) - 1)) {
                    i++;
                }
                if (i == 1) {
                    deletePdfFilePage(list.get(0));
                } else {
                    deletePdfRange(list.get(i - 1), i);
                }
                for (int i2 = 0; i2 < i; i2++) {
                    list.remove(0);
                }
            }
        }
    }

    private void deletePdfRange(int i, int i2) {
        for (int i3 = 0; i3 < i2; i3++) {
            listPdfFilePages.remove(i);
        }
        notifyItemRangeRemoved(i, i2);
    }

    public void selectAllPdf() {
        int size = listPdfFilePages.size();
        for (int i = 0; i < size; i++) {
            selectedPages.put(i, true);
        }
        ActionMode actionMode2 = actionMode;
        actionMode2.setTitle(size + " " + context.getString(R.string.selected));
        actionMode.invalidate();
        notifyItemRangeChanged(0, size);
    }

    public void unselectAllPdf() {
        selectedPages.clear();
        ActionMode actionMode2 = actionMode;
        actionMode2.setTitle("0 " + context.getString(R.string.selected));
        actionMode.invalidate();
        notifyItemRangeChanged(0, listPdfFilePages.size());
    }

    public void finishActionBarMode(boolean z) {
        clearSelection = z;
        ActionMode actionMode2 = actionMode;
        if (actionMode2 != null) {
            actionMode2.finish();
        }
    }

    public static class PdfOrgnalPagesViewHolder extends RecyclerView.ViewHolder {
        public RelativeLayout rLayMain;
        public TextView tvPageNumber;
        ImageView imgPdfImage;
        LinearLayout layOrgnizePage;

        private PdfOrgnalPagesViewHolder(View view) {
            super(view);
            rLayMain = view.findViewById(R.id.rLayMain);
            tvPageNumber = view.findViewById(R.id.tvPageNumber);
            imgPdfImage = view.findViewById(R.id.imgPdfImage);
            layOrgnizePage = view.findViewById(R.id.layOrgnizePage);
        }
    }

    private class SetActionBarThemeCallback implements ActionMode.Callback {
        int colorFrom;
        int colorTo;
        int flags;
        View view;

        private SetActionBarThemeCallback() {
            View decorView = ((Activity) context).getWindow().getDecorView();
            view = decorView;
            flags = decorView.getSystemUiVisibility();
            colorFrom = context.getResources().getColor(R.color.white);
            colorTo = context.getResources().getColor(R.color.colorDarkerGray);
        }

        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            actionMode.getMenuInflater().inflate(R.menu.activity_extract_texts_pages, menu);
            int i = Build.VERSION.SDK_INT;
            if (i >= 21) {
                if (i >= 23) {
                    int i2 = flags & -8193;
                    flags = i2;
                    view.setSystemUiVisibility(i2);
                }
                ValueAnimator ofObject = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
                ofObject.setDuration(300L);
                ofObject.addUpdateListener(valueAnimator -> ((Activity) context).getWindow().setStatusBarColor((Integer) valueAnimator.getAnimatedValue()));
                ofObject.start();
            }
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            int itemId = menuItem.getItemId();
            if (itemId == R.id.action_delete) {
                AdapterExtractTextsPages extractTextsPagesAdapter = AdapterExtractTextsPages.this;
                extractTextsPagesAdapter.deletePdfPage(extractTextsPagesAdapter.getSelectedPages());
                actionMode.finish();
                return true;
            } else if (itemId == R.id.action_deselect_all) {
                unselectAllPdf();
                return true;
            } else if (itemId != R.id.action_select_all) {
                return true;
            } else {
                selectAllPdf();
                return true;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {
            ValueAnimator valueAnimator;
            AdapterExtractTextsPages extractTextsPagesAdapter = AdapterExtractTextsPages.this;
            if (extractTextsPagesAdapter.clearSelection) {
                extractTextsPagesAdapter.deleteSelectedPAge();
            }
            int i = Build.VERSION.SDK_INT;
            if (i >= 21) {
                if (i >= 23) {
                    int i2 = flags | 8192;
                    flags = i2;
                    view.setSystemUiVisibility(i2);
                }
                if (!clearSelection) {
                    valueAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), colorTo, context.getResources().getColor(R.color.light_color));
                } else {
                    valueAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), colorTo, colorFrom);
                }
                valueAnimator.setDuration(300L);
                valueAnimator.addUpdateListener(valueAnimator1 -> ((Activity) context).getWindow().setStatusBarColor((Integer) valueAnimator1.getAnimatedValue()));
                valueAnimator.start();
            }
            clearSelection = true;
        }
    }
}
