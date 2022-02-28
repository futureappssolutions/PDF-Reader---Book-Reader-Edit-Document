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

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.recyclerview.widget.RecyclerView;

import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.R;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.GetSet.PDFPage;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AdapterShareAsPicture extends RecyclerView.Adapter<AdapterShareAsPicture.OrganizePagesViewHolder> {
    public ActionMode actionMode;
    public ActionModeCallback actionModeCallback;
    public Context context;
    private List<PDFPage> listPdfImagePages;
    private SparseBooleanArray selectedPages = new SparseBooleanArray();

    public class OrganizePagesViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPdfImage;
        LinearLayout layOrgnizePage;
        public RelativeLayout rLayMain;
        public TextView tvPageNumber;

        private OrganizePagesViewHolder(View view) {
            super(view);
            this.rLayMain = (RelativeLayout) view.findViewById(R.id.rLayMain);
            this.tvPageNumber = (TextView) view.findViewById(R.id.tvPageNumber);
            this.imgPdfImage = (ImageView) view.findViewById(R.id.imgPdfImage);
            this.layOrgnizePage = (LinearLayout) view.findViewById(R.id.layOrgnizePage);
        }
    }

    public AdapterShareAsPicture(Context context2, List<PDFPage> list) {
        this.listPdfImagePages = list;
        this.context = context2;
        this.actionModeCallback = new ActionModeCallback();
        StringBuilder sb = new StringBuilder();
        sb.append("number of thumbs ");
        sb.append(list.size());
    }

    @Override
    public OrganizePagesViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new OrganizePagesViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_organize_pages_grid, viewGroup, false));
    }

    public void onBindViewHolder(final OrganizePagesViewHolder organizePagesViewHolder, int i) {
        PDFPage pDFPage = this.listPdfImagePages.get(i);
        Picasso.get().load(pDFPage.getThumbnailUri()).fit().into(organizePagesViewHolder.imgPdfImage);
        organizePagesViewHolder.tvPageNumber.setText(String.valueOf(pDFPage.getPageNumber()));
        changePDFSelectedBGColor(organizePagesViewHolder, i);
        organizePagesViewHolder.rLayMain.setOnClickListener(new View.OnClickListener() {
            /* class pdfreader.pdfviewer.pdfscanner.documentreader.officetool.adapters.ShareAsPictureAdapter.AnonymousClass1 */

            public void onClick(View view) {
                int adapterPosition = organizePagesViewHolder.getAdapterPosition();
                if (AdapterShareAsPicture.this.actionMode == null) {
                    AdapterShareAsPicture shareAsPictureAdapter = AdapterShareAsPicture.this;
                    shareAsPictureAdapter.actionMode = ((AppCompatActivity) shareAsPictureAdapter.context).startSupportActionMode(AdapterShareAsPicture.this.actionModeCallback);
                }
                AdapterShareAsPicture.this.getSelectedImagePdf(adapterPosition);
                StringBuilder sb = new StringBuilder();
                sb.append("Clicked position ");
                sb.append(adapterPosition);
            }
        });
    }

    public void getSelectedImagePdf(int i) {
        if (this.selectedPages.get(i, false)) {
            this.selectedPages.delete(i);
        } else {
            this.selectedPages.put(i, true);
        }
        notifyItemChanged(i);
        int size = this.selectedPages.size();
        if (size == 0) {
            this.actionMode.finish();
            return;
        }
        ActionMode actionMode2 = this.actionMode;
        actionMode2.setTitle(size + " " + this.context.getString(R.string.selected));
        this.actionMode.invalidate();
    }

    private void changePDFSelectedBGColor(OrganizePagesViewHolder organizePagesViewHolder, int i) {
        if (isSelected(i)) {
            organizePagesViewHolder.layOrgnizePage.setVisibility(View.VISIBLE);
        } else {
            organizePagesViewHolder.layOrgnizePage.setVisibility(View.GONE);
        }
    }

    private boolean isSelected(int i) {
        return getSelectedPdfImagePages().contains(Integer.valueOf(i));
    }

    @Override
    public int getItemCount() {
        return this.listPdfImagePages.size();
    }

    public void clearSelectedPDF() {
        List<Integer> selectedPdfImagePages = getSelectedPdfImagePages();
        this.selectedPages.clear();
        for (Integer num : selectedPdfImagePages) {
            notifyItemChanged(num.intValue());
        }
    }

    public List<Integer> getSelectedPdfImagePages() {
        int size = this.selectedPages.size();
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < size; i++) {
            arrayList.add(Integer.valueOf(this.selectedPages.keyAt(i)));
        }
        return arrayList;
    }

    private void deletePdfItem(int i) {
        this.listPdfImagePages.remove(i);
        notifyItemRemoved(i);
    }

    public List<PDFPage> getFinalOrganizedPages() {
        return this.listPdfImagePages;
    }

    public void deletePhotoPdfItem(List<Integer> list) {
        Collections.sort(list, new Comparator<Integer>() {
            /* class pdfreader.pdfviewer.pdfscanner.documentreader.officetool.adapters.ShareAsPictureAdapter.AnonymousClass2 */

            public int compare(Integer num, Integer num2) {
                return num2.intValue() - num.intValue();
            }
        });
        while (!list.isEmpty()) {
            if (list.size() == 1) {
                deletePdfItem(list.get(0).intValue());
                list.remove(0);
            } else {
                int i = 1;
                while (list.size() > i && list.get(i).equals(Integer.valueOf(list.get(i - 1).intValue() - 1))) {
                    i++;
                }
                if (i == 1) {
                    deletePdfItem(list.get(0).intValue());
                } else {
                    removeSeletedPdfRange(list.get(i - 1).intValue(), i);
                }
                for (int i2 = 0; i2 < i; i2++) {
                    list.remove(0);
                }
            }
        }
    }

    private void removeSeletedPdfRange(int i, int i2) {
        for (int i3 = 0; i3 < i2; i3++) {
            this.listPdfImagePages.remove(i);
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
            View decorView = ((Activity) AdapterShareAsPicture.this.context).getWindow().getDecorView();
            this.view = decorView;
            this.flags = decorView.getSystemUiVisibility();
            this.colorFrom = AdapterShareAsPicture.this.context.getResources().getColor(R.color.white);
            this.colorTo = AdapterShareAsPicture.this.context.getResources().getColor(R.color.colorDarkerGray);
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
                    /* class pdfreader.pdfviewer.pdfscanner.documentreader.officetool.adapters.ShareAsPictureAdapter.ActionModeCallback.AnonymousClass1 */

                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        ((Activity) AdapterShareAsPicture.this.context).getWindow().setStatusBarColor(((Integer) valueAnimator.getAnimatedValue()).intValue());
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
            AdapterShareAsPicture shareAsPictureAdapter = AdapterShareAsPicture.this;
            shareAsPictureAdapter.deletePhotoPdfItem(shareAsPictureAdapter.getSelectedPdfImagePages());
            actionMode.finish();
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {
            AdapterShareAsPicture.this.clearSelectedPDF();
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
                    /* class pdfreader.pdfviewer.pdfscanner.documentreader.officetool.adapters.ShareAsPictureAdapter.ActionModeCallback.AnonymousClass2 */

                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        ((Activity) AdapterShareAsPicture.this.context).getWindow().setStatusBarColor(((Integer) valueAnimator.getAnimatedValue()).intValue());
                    }
                });
                ofObject.start();
            }
            AdapterShareAsPicture.this.actionMode = null;
        }
    }
}
