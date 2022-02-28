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
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.utils.Utils;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AdapterMergeOrganalPDF extends RecyclerView.Adapter<AdapterMergeOrganalPDF.MergePdfOrignalPagesViewHolder> {
    static String thmubImagePath;
    public ActionMode actionMode;
    public ActionModeCallback actionModeCallback;
    public Context context;
    public List<File> listMergePDFFile;
    public SparseBooleanArray mergeOrgSelectedPages = new SparseBooleanArray();

    public AdapterMergeOrganalPDF(Context context2, List<File> list) {
        listMergePDFFile = list;
        context = context2;
        actionModeCallback = new ActionModeCallback();
        thmubImagePath = context2.getCacheDir() + "/Thumbnails/";
        StringBuilder sb = new StringBuilder();
        sb.append("number of thumbs ");
        sb.append(list.size());
    }

    @NonNull
    @Override
    public MergePdfOrignalPagesViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new MergePdfOrignalPagesViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_organize_pdfs_merge_grid, viewGroup, false));
    }

    public void onBindViewHolder(final MergePdfOrignalPagesViewHolder mergePdfOrignalPagesViewHolder, int i) {
        File file = listMergePDFFile.get(i);
        Picasso.get().load(Utils.getImageUriFromPath(thmubImagePath + Utils.removePdfExtension(file.getName()) + ".jpg")).placeholder(R.drawable.ic_pdf_img).into(mergePdfOrignalPagesViewHolder.imgPdfImage);
        mergePdfOrignalPagesViewHolder.tvPdfFileNamw.setText(file.getName());
        selectChangeBackground(mergePdfOrignalPagesViewHolder, i);
        mergePdfOrignalPagesViewHolder.rLayMain.setOnClickListener(view -> {
            int adapterPosition = mergePdfOrignalPagesViewHolder.getAdapterPosition();
            if (actionMode == null) {
                actionMode = ((AppCompatActivity) context).startSupportActionMode(actionModeCallback);
            }
            adpPosSelection(adapterPosition);
            StringBuilder sb = new StringBuilder();
            sb.append("Clicked position ");
            sb.append(adapterPosition);
        });
    }

    public List<File> getPDFsToMerge() {
        return listMergePDFFile;
    }

    public void adpPosSelection(int i) {
        if (mergeOrgSelectedPages.get(i, false)) {
            mergeOrgSelectedPages.delete(i);
        } else {
            mergeOrgSelectedPages.put(i, true);
        }
        notifyItemChanged(i);
        int size = mergeOrgSelectedPages.size();
        if (size == 0) {
            actionMode.finish();
            return;
        }
        ActionMode actionMode2 = actionMode;
        actionMode2.setTitle(size + " " + context.getString(R.string.selected));
        actionMode.invalidate();
    }

    private void selectChangeBackground(MergePdfOrignalPagesViewHolder mergePdfOrignalPagesViewHolder, int i) {
        if (isSelected(i)) {
            mergePdfOrignalPagesViewHolder.layOrgnalPdfPage.setVisibility(View.VISIBLE);
        } else {
            mergePdfOrignalPagesViewHolder.layOrgnalPdfPage.setVisibility(View.GONE);
        }
    }

    private boolean isSelected(int i) {
        return getMergeOrgSelectedPages().contains(i);
    }

    @Override
    public int getItemCount() {
        return listMergePDFFile.size();
    }

    public void DeleteSelection() {
        List<Integer> mergeOrgSelectedPages2 = getMergeOrgSelectedPages();
        mergeOrgSelectedPages.clear();
        for (Integer num : mergeOrgSelectedPages2) {
            notifyItemChanged(num);
        }
    }

    public List<Integer> getMergeOrgSelectedPages() {
        int size = mergeOrgSelectedPages.size();
        ArrayList<Integer> arrayList = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            arrayList.add(mergeOrgSelectedPages.keyAt(i));
        }
        return arrayList;
    }

    private void deleteMergePdf(int i) {
        listMergePDFFile.remove(i);
        notifyItemRemoved(i);
    }

    public void deleteMergePdfItems(List<Integer> list) {
        Collections.sort(list, (num, num2) -> num2 - num);
        while (!list.isEmpty()) {
            if (list.size() == 1) {
                deleteMergePdf(list.get(0));
                list.remove(0);
            } else {
                int i = 1;
                while (list.size() > i && list.get(i).equals(list.get(i - 1) - 1)) {
                    i++;
                }
                if (i == 1) {
                    deleteMergePdf(list.get(0));
                } else {
                    deleteMergePdfRange(list.get(i - 1), i);
                }
                for (int i2 = 0; i2 < i; i2++) {
                    list.remove(0);
                }
            }
        }
    }

    private void deleteMergePdfRange(int i, int i2) {
        for (int i3 = 0; i3 < i2; i3++) {
            listMergePDFFile.remove(i);
        }
        notifyItemRangeRemoved(i, i2);
    }

    public void finishActionMode() {
        ActionMode actionMode2 = actionMode;
        if (actionMode2 != null) {
            actionMode2.finish();
        }
    }

    public static class MergePdfOrignalPagesViewHolder extends RecyclerView.ViewHolder {
        public RelativeLayout rLayMain;
        public TextView tvPdfFileNamw;
        ImageView imgPdfImage;
        LinearLayout layOrgnalPdfPage;

        public MergePdfOrignalPagesViewHolder(View view) {
            super(view);
            rLayMain = (RelativeLayout) view.findViewById(R.id.rLayMain);
            tvPdfFileNamw = (TextView) view.findViewById(R.id.tvPdfFileNamw);
            imgPdfImage = (ImageView) view.findViewById(R.id.imgPdfImage);
            layOrgnalPdfPage = (LinearLayout) view.findViewById(R.id.layOrgnalPdfPage);
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
            AdapterMergeOrganalPDF mergeOrganalPDFAdp = AdapterMergeOrganalPDF.this;
            mergeOrganalPDFAdp.deleteMergePdfItems(mergeOrganalPDFAdp.getMergeOrgSelectedPages());
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
