package pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Adapter;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.recyclerview.widget.RecyclerView;

import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class AdapterSelectImages extends RecyclerView.Adapter<AdapterSelectImages.SelectPDFViewHolder> {
    public ActionMode actionMode;
    public ActionModeCallback actionModeCallback;
    public Context context;
    public List<Uri> listImageUris;
    public OnImageSelectedListener onImageSelectedListener;
    public SparseBooleanArray selectedImages = new SparseBooleanArray();

    public AdapterSelectImages(Context context2, List<Uri> list) {
        listImageUris = list;
        context = context2;
        actionModeCallback = new ActionModeCallback();
        Context context3 = context;
        if (context3 instanceof OnImageSelectedListener) {
            onImageSelectedListener = (OnImageSelectedListener) context3;
            return;
        }
        throw new RuntimeException(context.toString() + " must implement OnImageSelectedListener");
    }

    @NonNull
    @Override
    public SelectPDFViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new SelectPDFViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_select_images_grid, viewGroup, false));
    }

    public void onBindViewHolder(@NonNull final SelectPDFViewHolder selectPDFViewHolder, int i) {
        backgroundSelection(selectPDFViewHolder, i);
        Picasso.get().load(listImageUris.get(i)).fit().centerCrop().into(selectPDFViewHolder.imgThumbPdf);
        selectPDFViewHolder.imgThumbPdf.setOnClickListener(view -> {
            if (actionMode == null) {
                actionMode = ((AppCompatActivity) context).startSupportActionMode(actionModeCallback);
            }
            selectImageAdpPosition(selectPDFViewHolder.getAdapterPosition());
        });
    }

    public void selectImageAdpPosition(int i) {
        if (selectedImages.get(i, false)) {
            selectedImages.delete(i);
        } else {
            selectedImages.put(i, true);
        }
        notifyItemChanged(i);
        int selectedItemCount = getSelectedItemCount();
        if (selectedItemCount == 0) {
            actionMode.finish();
            return;
        }
        ActionMode actionMode2 = actionMode;
        actionMode2.setTitle(selectedItemCount + " " + context.getString(R.string.selected));
        actionMode.invalidate();
    }

    private int getSelectedItemCount() {
        return selectedImages.size();
    }

    private List<Integer> selectedImagesSize() {
        int size = selectedImages.size();
        ArrayList<Integer> arrayList = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            arrayList.add(selectedImages.keyAt(i));
        }
        return arrayList;
    }

    public void imageUnselect() {
        List<Integer> selectedImagesSize = selectedImagesSize();
        selectedImages.clear();
        for (Integer num : selectedImagesSize) {
            notifyItemChanged(num);
        }
    }

    private boolean isSelected(int i) {
        return selectedImagesSize().contains(i);
    }

    private void backgroundSelection(SelectPDFViewHolder selectPDFViewHolder, int i) {
        if (isSelected(i)) {
            selectPDFViewHolder.laySelectedPhoto.setVisibility(View.VISIBLE);
        } else {
            selectPDFViewHolder.laySelectedPhoto.setVisibility(View.INVISIBLE);
        }
    }

    public ArrayList<String> selectedImages() {
        List<Integer> selectedImagesSize = selectedImagesSize();
        ArrayList<String> arrayList = new ArrayList<>();
        for (Integer num : selectedImagesSize) {
            arrayList.add(listImageUris.get(num).toString());
        }
        return arrayList;
    }

    @Override
    public int getItemCount() {
        return listImageUris.size();
    }

    public void multiSelectedPDF(ArrayList<String> arrayList) {
        OnImageSelectedListener onImageSelectedListener2 = onImageSelectedListener;
        if (onImageSelectedListener2 != null) {
            onImageSelectedListener2.onMultiSelectedPDF(arrayList);
        }
    }

    public interface OnImageSelectedListener {
        void onMultiSelectedPDF(ArrayList<String> arrayList);
    }

    public static class SelectPDFViewHolder extends RecyclerView.ViewHolder {
        public ImageView imgThumbPdf;
        public LinearLayout laySelectedPhoto;

        public SelectPDFViewHolder(View view) {
            super(view);
            imgThumbPdf = (ImageView) view.findViewById(R.id.imgThumbPdf);
            laySelectedPhoto = (LinearLayout) view.findViewById(R.id.laySelectedPhoto);
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
            actionMode.getMenuInflater().inflate(R.menu.selected_pdfs, menu);
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
            if (menuItem.getItemId() != R.id.action_select) {
                return true;
            }
            multiSelectedPDF(selectedImages());
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {
            imageUnselect();
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
