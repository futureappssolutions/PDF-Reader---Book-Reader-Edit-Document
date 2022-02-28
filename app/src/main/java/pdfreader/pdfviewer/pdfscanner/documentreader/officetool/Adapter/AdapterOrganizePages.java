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

public class AdapterOrganizePages extends RecyclerView.Adapter<AdapterOrganizePages.OrganalPdfPagesViewHolder> {
    public ActionMode actionMode;
    public ActionModeCallback actionModeCallback;
    public Context context;
    public List<PDFPage> listPdfFilePages;
    public SparseBooleanArray selectedPages = new SparseBooleanArray();

    public AdapterOrganizePages(Context context2, List<PDFPage> list) {
        listPdfFilePages = list;
        context = context2;
        actionModeCallback = new ActionModeCallback();
        StringBuilder sb = new StringBuilder();
        sb.append("number of thumbs ");
        sb.append(list.size());
    }

    @NonNull
    @Override
    public OrganalPdfPagesViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new OrganalPdfPagesViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_organize_pages_grid, viewGroup, false));
    }

    public void onBindViewHolder(final OrganalPdfPagesViewHolder organalPdfPagesViewHolder, int i) {
        PDFPage pDFPage = listPdfFilePages.get(i);
        Picasso.get().load(pDFPage.getThumbnailUri()).fit().into(organalPdfPagesViewHolder.imgPdfImage);
        organalPdfPagesViewHolder.tvPageNumber.setText(String.valueOf(pDFPage.getPageNumber()));
        selectChangeBackground(organalPdfPagesViewHolder, i);
        organalPdfPagesViewHolder.rLayMain.setOnClickListener(view -> {
            int adapterPosition = organalPdfPagesViewHolder.getAdapterPosition();
            if (actionMode == null) {
                AdapterOrganizePages organizePagesAdapter = AdapterOrganizePages.this;
                organizePagesAdapter.actionMode = ((AppCompatActivity) organizePagesAdapter.context).startSupportActionMode(actionModeCallback);
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

    private void selectChangeBackground(OrganalPdfPagesViewHolder organalPdfPagesViewHolder, int i) {
        if (isSelected(i)) {
            organalPdfPagesViewHolder.layOrgnizePage.setVisibility(View.VISIBLE);
        } else {
            organalPdfPagesViewHolder.layOrgnizePage.setVisibility(View.GONE);
        }
    }

    private boolean isSelected(int i) {
        return getSelectedPages().contains(i);
    }

    @Override
    public int getItemCount() {
        return listPdfFilePages.size();
    }

    public void DeleteSelection() {
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

    private void deleteOriganlPdfItem(int i) {
        listPdfFilePages.remove(i);
        notifyItemRemoved(i);
    }

    public void deleteOrigPageData(List<Integer> list) {
        Collections.sort(list, (num, num2) -> num2 - num);
        while (!list.isEmpty()) {
            if (list.size() == 1) {
                deleteOriganlPdfItem(list.get(0));
                list.remove(0);
            } else {
                int i = 1;
                while (list.size() > i && list.get(i).equals(list.get(i - 1) - 1)) {
                    i++;
                }
                if (i == 1) {
                    deleteOriganlPdfItem(list.get(0));
                } else {
                    deleteOrigPdfPage(list.get(i - 1), i);
                }
                for (int i2 = 0; i2 < i; i2++) {
                    list.remove(0);
                }
            }
        }
    }

    private void deleteOrigPdfPage(int i, int i2) {
        for (int i3 = 0; i3 < i2; i3++) {
            listPdfFilePages.remove(i);
        }
        notifyItemRangeRemoved(i, i2);
    }

    public static class OrganalPdfPagesViewHolder extends RecyclerView.ViewHolder {
        public RelativeLayout rLayMain;
        public TextView tvPageNumber;
        ImageView imgPdfImage;
        LinearLayout layOrgnizePage;

        private OrganalPdfPagesViewHolder(View view) {
            super(view);
            rLayMain = (RelativeLayout) view.findViewById(R.id.rLayMain);
            tvPageNumber = (TextView) view.findViewById(R.id.tvPageNumber);
            imgPdfImage = (ImageView) view.findViewById(R.id.imgPdfImage);
            layOrgnizePage = (LinearLayout) view.findViewById(R.id.layOrgnizePage);
        }
    }

    private class ActionModeCallback implements ActionMode.Callback {
        int colorFrom;
        int colorTo;
        int flags;
        View view;

        private ActionModeCallback() {
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
            actionMode.getMenuInflater().inflate(R.menu.activity_organize_pages_action_mode, menu);
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
            if (menuItem.getItemId() != R.id.action_delete) {
                return true;
            }
            AdapterOrganizePages organizePagesAdapter = AdapterOrganizePages.this;
            organizePagesAdapter.deleteOrigPageData(organizePagesAdapter.getSelectedPages());
            actionMode.finish();
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {
            DeleteSelection();
            int i = Build.VERSION.SDK_INT;
            if (i >= 21) {
                if (i >= 23) {
                    int i2 = flags | 8192;
                    flags = i2;
                    view.setSystemUiVisibility(i2);
                }
                ValueAnimator ofObject = ValueAnimator.ofObject(new ArgbEvaluator(), colorTo, colorFrom);
                ofObject.setDuration(300L);
                ofObject.addUpdateListener(valueAnimator -> ((Activity) context).getWindow().setStatusBarColor((Integer) valueAnimator.getAnimatedValue()));
                ofObject.start();
            }
        }
    }
}