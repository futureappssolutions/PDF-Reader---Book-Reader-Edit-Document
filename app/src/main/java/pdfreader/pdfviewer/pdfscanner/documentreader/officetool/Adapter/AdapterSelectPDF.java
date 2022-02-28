package pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Adapter;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.preference.PreferenceManager;
import android.text.format.Formatter;
import android.util.SparseBooleanArray;
import android.util.TypedValue;
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
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Activity.ActivityMain;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.R;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.GetSet.PdfDataType;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.utils.Utils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class AdapterSelectPDF extends RecyclerView.Adapter<AdapterSelectPDF.SelectPDFViewHolder> {
    public ActionMode actionMode;
    public ActionModeCallback actionModeCallback;
    public Context context;
    public boolean isGridDataEnabled;
    public Boolean isSelectMultiple;
    public List<PdfDataType> listPdfTypeFiles;
    public OnMultiSelectedPDFListener onMultiSelectedPDFListener;
    public OnSelectedPdfClickListener pdfStaredClickListener;
    public SparseBooleanArray selectedPDFs = new SparseBooleanArray();

    public interface OnMultiSelectedPDFListener {
        void onMultiSelectedPDF(ArrayList<String> arrayList);
    }

    public interface OnSelectedPdfClickListener {
        void onSelectedPdfClicked(PdfDataType pdfDataType);
    }

    public class SelectPDFViewHolder extends RecyclerView.ViewHolder {
        public ImageView imgPdfSelectedPhoto;
        public LinearLayout layPdfItemMain;
        public LinearLayout laySelectedItemHyLight;
        public RelativeLayout rlayPdfSelected;
        public TextView tvPdfModifiedTime;
        public TextView tvPdfSize;
        public TextView tvPdfTitle;

        public SelectPDFViewHolder(View view) {
            super(view);
            if (isGridDataEnabled) {
                imgPdfSelectedPhoto = (ImageView) view.findViewById(R.id.imgPdfSelectedPhoto);
                laySelectedItemHyLight = (LinearLayout) view.findViewById(R.id.laySelectedItemHyLight);
            }
            tvPdfTitle = (TextView) view.findViewById(R.id.tvPdfTitle);
            tvPdfModifiedTime = (TextView) view.findViewById(R.id.tvPdfModifiedTime);
            tvPdfSize = (TextView) view.findViewById(R.id.tvPdfSize);
            rlayPdfSelected = (RelativeLayout) view.findViewById(R.id.rlayPdfSelected);
            layPdfItemMain = (LinearLayout) view.findViewById(R.id.layPdfItemMain);
        }
    }

    public AdapterSelectPDF(List<PdfDataType> list, Context context2, Boolean bool) {
        listPdfTypeFiles = list;
        context = context2;
        isSelectMultiple = bool;
        actionModeCallback = new ActionModeCallback();
        isGridDataEnabled = PreferenceManager.getDefaultSharedPreferences(context2).getBoolean(ActivityMain.GRID_VIEW_ENABLED, false);
        Context context3 = context;
        if (context3 instanceof OnSelectedPdfClickListener) {
            pdfStaredClickListener = (OnSelectedPdfClickListener) context3;
            if (context3 instanceof OnMultiSelectedPDFListener) {
                onMultiSelectedPDFListener = (OnMultiSelectedPDFListener) context3;
                return;
            }
            throw new RuntimeException(context.toString() + " must implement OnMultiSelectedPDFListener");
        }
        throw new RuntimeException(context.toString() + " must implement OnSelectedPdfClickListener");
    }

    @NonNull
    @Override
    public SelectPDFViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view;
        if (isGridDataEnabled) {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_pdf_select_grid, viewGroup, false);
        } else {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_pdf_select, viewGroup, false);
        }
        return new SelectPDFViewHolder(view);
    }

    public void onBindViewHolder(SelectPDFViewHolder selectPDFViewHolder, final int i) {
        PdfDataType pdfDataType = listPdfTypeFiles.get(i);
        String name = pdfDataType.getName();
        Long length = pdfDataType.getLength();
        selectPDFViewHolder.tvPdfTitle.setText(name);
        selectPDFViewHolder.tvPdfSize.setText(Formatter.formatShortFileSize(context, length));
        selectPDFViewHolder.tvPdfModifiedTime.setText(Utils.formatDateToHumanReadable(pdfDataType.getLastModified()));
        changePDFSelectedBGColor(selectPDFViewHolder, i);
        if (isGridDataEnabled) {
            Picasso.get().load(pdfDataType.getThumbUri()).into(selectPDFViewHolder.imgPdfSelectedPhoto);
        }

        selectPDFViewHolder.rlayPdfSelected.setOnClickListener(view -> {
            if (actionMode == null && isSelectMultiple) {
                actionMode = ((AppCompatActivity) context).startSupportActionMode(actionModeCallback);
            }
            if (isSelectMultiple) {
                getPDFSelection(i);
            } else {
                clickedSelectedPdf(i);
            }
        });
    }

    public void filter(List<PdfDataType> list) {
        listPdfTypeFiles = list;
        notifyDataSetChanged();
    }

    public void pdfDataUpdate(List<PdfDataType> list) {
        listPdfTypeFiles = list;
        notifyDataSetChanged();
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
            View decorView = ((Activity) context).getWindow().getDecorView();
            view = decorView;
            flags = decorView.getSystemUiVisibility();
            colorFrom = context.getResources().getColor(R.color.white);
            colorTo = context.getResources().getColor(R.color.colorDarkerGray);
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
            selectedMultiplePDF(selectedListPdfFiles());
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
                    view.setSystemUiVisibility(i2);
                }
                ValueAnimator ofObject = ValueAnimator.ofObject(new ArgbEvaluator(), colorTo, colorFrom);
                ofObject.setDuration(300L);
                ofObject.addUpdateListener(valueAnimator -> ((Activity) context).getWindow().setStatusBarColor((Integer) valueAnimator.getAnimatedValue()));
                ofObject.start();
            }
        }
    }

    public void getPDFSelection(int i) {
        if (selectedPDFs.get(i, false)) {
            selectedPDFs.delete(i);
        } else {
            selectedPDFs.put(i, true);
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
        return selectedPDFs.size();
    }

    private List<Integer> getSelectedPdfFiles() {
        int size = selectedPDFs.size();
        ArrayList<Integer> arrayList = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            arrayList.add(selectedPDFs.keyAt(i));
        }
        return arrayList;
    }

    public void clearSelectedPDF() {
        List<Integer> selectedPdfFiles = getSelectedPdfFiles();
        selectedPDFs.clear();
        for (Integer num : selectedPdfFiles) {
            notifyItemChanged(num);
        }
    }

    private boolean isSelected(int i) {
        return getSelectedPdfFiles().contains(i);
    }

    private void changePDFSelectedBGColor(SelectPDFViewHolder selectPDFViewHolder, int i) {
        if (isSelected(i)) {
            if (isGridDataEnabled) {
                selectPDFViewHolder.laySelectedItemHyLight.setVisibility(View.VISIBLE);
            } else {
                selectPDFViewHolder.layPdfItemMain.setBackgroundColor(ContextCompat.getColor(context, R.color.colorSelectedPDFs));
            }
        } else if (isGridDataEnabled) {
            selectPDFViewHolder.laySelectedItemHyLight.setVisibility(View.GONE);
        } else {
            TypedValue typedValue = new TypedValue();
            context.getTheme().resolveAttribute(16843534, typedValue, true);
            selectPDFViewHolder.layPdfItemMain.setBackgroundResource(typedValue.resourceId);
        }
    }

    public ArrayList<String> selectedListPdfFiles() {
        List<Integer> selectedPdfFiles = getSelectedPdfFiles();
        ArrayList<String> arrayList = new ArrayList<>();
        for (Integer num : selectedPdfFiles) {
            arrayList.add(listPdfTypeFiles.get(num).getAbsolutePath());
        }
        return arrayList;
    }

    @Override
    public int getItemCount() {
        return listPdfTypeFiles.size();
    }

    public void clickedSelectedPdf(int i) {
        OnSelectedPdfClickListener onSelectedPdfClickListener = pdfStaredClickListener;
        if (onSelectedPdfClickListener != null) {
            onSelectedPdfClickListener.onSelectedPdfClicked(listPdfTypeFiles.get(i));
        }
    }

    public void selectedMultiplePDF(ArrayList<String> arrayList) {
        OnMultiSelectedPDFListener onMultiSelectedPDFListener2 = onMultiSelectedPDFListener;
        if (onMultiSelectedPDFListener2 != null) {
            onMultiSelectedPDFListener2.onMultiSelectedPDF(arrayList);
        }
    }
}
