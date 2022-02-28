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

import androidx.appcompat.view.ActionMode;
import androidx.recyclerview.widget.RecyclerView;

import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AdapterViewImages extends RecyclerView.Adapter<AdapterViewImages.ViewImagesViewHolder> {
    public ActionMode actionMode;
    private ActionModeCallback actionModeCallback;
    public Context context;
    private List<Uri> listImageUris;
    private SparseBooleanArray selectedPages = new SparseBooleanArray();

    public class ViewImagesViewHolder extends RecyclerView.ViewHolder {
        ImageView imgMainPic;

        private ViewImagesViewHolder(View view) {
            super(view);
            this.imgMainPic = (ImageView) view.findViewById(R.id.imgMainPic);
        }
    }

    public AdapterViewImages(Context context2, List<Uri> list) {
        this.listImageUris = list;
        this.context = context2;
        this.actionModeCallback = new ActionModeCallback();
    }

    @Override
    public ViewImagesViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewImagesViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_view_image, viewGroup, false));
    }

    public void onBindViewHolder(ViewImagesViewHolder viewImagesViewHolder, int i) {
        Picasso.get().load(this.listImageUris.get(i)).into(viewImagesViewHolder.imgMainPic);
        viewImagesViewHolder.imgMainPic.setOnClickListener(new View.OnClickListener() {
            /* class pdfreader.pdfviewer.pdfscanner.documentreader.officetool.adapters.ViewImagesAdapter.AnonymousClass1 */

            public void onClick(View view) {
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.listImageUris.size();
    }

    public void removeImagePdf() {
        List<Integer> selectedPdfPage = selectedPdfPage();
        this.selectedPages.clear();
        for (Integer num : selectedPdfPage) {
            notifyItemChanged(num.intValue());
        }
    }

    public List<Integer> selectedPdfPage() {
        int size = this.selectedPages.size();
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < size; i++) {
            arrayList.add(Integer.valueOf(this.selectedPages.keyAt(i)));
        }
        return arrayList;
    }

    private void removeImageItem(int i) {
        this.listImageUris.remove(i);
        notifyItemRemoved(i);
    }

    public void removeImagePdfItems(List<Integer> list) {
        Collections.sort(list, new Comparator<Integer>() {
            /* class pdfreader.pdfviewer.pdfscanner.documentreader.officetool.adapters.ViewImagesAdapter.AnonymousClass2 */

            public int compare(Integer num, Integer num2) {
                return num2.intValue() - num.intValue();
            }
        });
        while (!list.isEmpty()) {
            if (list.size() == 1) {
                removeImageItem(list.get(0).intValue());
                list.remove(0);
            } else {
                int i = 1;
                while (list.size() > i && list.get(i).equals(Integer.valueOf(list.get(i - 1).intValue() - 1))) {
                    i++;
                }
                if (i == 1) {
                    removeImageItem(list.get(0).intValue());
                } else {
                    removePdfImageRange(list.get(i - 1).intValue(), i);
                }
                for (int i2 = 0; i2 < i; i2++) {
                    list.remove(0);
                }
            }
        }
    }

    private void removePdfImageRange(int i, int i2) {
        for (int i3 = 0; i3 < i2; i3++) {
            this.listImageUris.remove(i);
        }
        notifyItemRangeRemoved(i, i2);
    }

    private class ActionModeCallback implements ActionMode.Callback {
        int colorFrom;
        int colorTo;
        int flags;
        View view;

        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        private ActionModeCallback() {
            View decorView = ((Activity) AdapterViewImages.this.context).getWindow().getDecorView();
            this.view = decorView;
            this.flags = decorView.getSystemUiVisibility();
            this.colorFrom = AdapterViewImages.this.context.getResources().getColor(R.color.white);
            this.colorTo = AdapterViewImages.this.context.getResources().getColor(R.color.colorDarkerGray);
        }

        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            actionMode.getMenuInflater().inflate(R.menu.activity_organize_pages_action_mode, menu);
            int i = Build.VERSION.SDK_INT;
            if (i >= 21) {
                if (i >= 23) {
                    int i2 = this.flags & -8193;
                    this.flags = i2;
                    this.view.setSystemUiVisibility(i2);
                }
                ValueAnimator ofObject = ValueAnimator.ofObject(new ArgbEvaluator(), Integer.valueOf(this.colorFrom), Integer.valueOf(this.colorTo));
                ofObject.setDuration(300L);
                ofObject.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    /* class pdfreader.pdfviewer.pdfscanner.documentreader.officetool.adapters.ViewImagesAdapter.ActionModeCallback.AnonymousClass1 */

                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        ((Activity) AdapterViewImages.this.context).getWindow().setStatusBarColor(((Integer) valueAnimator.getAnimatedValue()).intValue());
                    }
                });
                ofObject.start();
            }
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            if (menuItem.getItemId() != R.id.action_delete) {
                return true;
            }
            AdapterViewImages viewImagesAdapter = AdapterViewImages.this;
            viewImagesAdapter.removeImagePdfItems(viewImagesAdapter.selectedPdfPage());
            actionMode.finish();
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {
            AdapterViewImages.this.removeImagePdf();
            int i = Build.VERSION.SDK_INT;
            if (i >= 21) {
                if (i >= 23) {
                    int i2 = this.flags | 8192;
                    this.flags = i2;
                    this.view.setSystemUiVisibility(i2);
                }
                ValueAnimator ofObject = ValueAnimator.ofObject(new ArgbEvaluator(), Integer.valueOf(this.colorTo), Integer.valueOf(this.colorFrom));
                ofObject.setDuration(300L);
                ofObject.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    /* class pdfreader.pdfviewer.pdfscanner.documentreader.officetool.adapters.ViewImagesAdapter.ActionModeCallback.AnonymousClass2 */

                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        ((Activity) AdapterViewImages.this.context).getWindow().setStatusBarColor(((Integer) valueAnimator.getAnimatedValue()).intValue());
                    }
                });
                ofObject.start();
            }
            AdapterViewImages.this.actionMode = null;
        }
    }
}
