package pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Activity.ActivityMain;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.R;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.data.FileDiffCallback;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.GetSet.PdfDataType;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.utils.Utils;
import com.squareup.picasso.Picasso;

import java.util.List;

public class AdapterFileBrowser extends RecyclerView.Adapter<AdapterFileBrowser.BrowsePdfFileViewHolder> {
    public int intFileFolderColor;
    public boolean isBrowserGridEnabled;
    public Context context;
    public List<PdfDataType> listPdfDataTypeFiles;
    public OnPdfClickListener pdfClickListener;

    public AdapterFileBrowser(Context context2, List<PdfDataType> list) {
        listPdfDataTypeFiles = list;
        context = context2;
        isBrowserGridEnabled = PreferenceManager.getDefaultSharedPreferences(context2).getBoolean(ActivityMain.GRID_VIEW_ENABLED, false);
        Context context3 = context;
        if (context3 instanceof OnPdfClickListener) {
            pdfClickListener = (OnPdfClickListener) context3;
            return;
        }
        throw new RuntimeException(context.toString() + " must implement OnPdfClickListener");
    }

    @NonNull
    @Override
    public BrowsePdfFileViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view;
        if (isBrowserGridEnabled) {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_browse_pdf_grid, viewGroup, false);
        } else {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_browse_pdf, viewGroup, false);
        }
        return new BrowsePdfFileViewHolder(view);
    }

    public void onBindViewHolder(final BrowsePdfFileViewHolder browsePdfFileViewHolder, int i) {
        PdfDataType pdfDataType = listPdfDataTypeFiles.get(i);
        String name = pdfDataType.getName();
        Long length = pdfDataType.getLength();
        browsePdfFileViewHolder.tvPdfFileName.setText(name);
        if (isBrowserGridEnabled) {
            if (pdfDataType.isDirectory()) {
                browsePdfFileViewHolder.imgPdfBrowse.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_folder_stacked));
                browsePdfFileViewHolder.imgPdfBrowse.setScaleType(ImageView.ScaleType.FIT_XY);
                browsePdfFileViewHolder.tvPdfSize.setText(pdfDataType.getNumItems() + " " + context.getString(R.string.items));
            } else {
                Picasso.get().load(pdfDataType.getThumbUri()).into(browsePdfFileViewHolder.imgPdfBrowse);
                browsePdfFileViewHolder.tvPdfSize.setText(Formatter.formatShortFileSize(context, length));
            }
        } else if (pdfDataType.isDirectory()) {
            browsePdfFileViewHolder.imgPdfImage.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_folder_closed));
            browsePdfFileViewHolder.tvPdfFilModified.setText(pdfDataType.getNumItems() + " " + context.getString(R.string.items));
        } else {
            browsePdfFileViewHolder.imgPdfImage.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_pdf_img));
            browsePdfFileViewHolder.tvPdfFilModified.setText(Utils.formatDateToHumanReadable(pdfDataType.getLastModified()));
            browsePdfFileViewHolder.tvPdfSize.setText(Formatter.formatShortFileSize(context, length));
        }
        browsePdfFileViewHolder.rLayBrowsePdf.setOnClickListener(view -> browsePdfClicked(browsePdfFileViewHolder.getAdapterPosition()));
    }

    @Override
    public int getItemCount() {
        return listPdfDataTypeFiles.size();
    }

    public void browsePdfClicked(int i) {
        OnPdfClickListener onPdfClickListener = pdfClickListener;
        if (onPdfClickListener != null) {
            onPdfClickListener.onPdfClicked(listPdfDataTypeFiles.get(i));
        }
    }

    public void updateData(List<PdfDataType> list) {
        DiffUtil.calculateDiff(new FileDiffCallback(listPdfDataTypeFiles, list)).dispatchUpdatesTo(this);
        listPdfDataTypeFiles = list;
    }

    public interface OnPdfClickListener {
        void onPdfClicked(PdfDataType pdfDataType);
    }

    public class BrowsePdfFileViewHolder extends RecyclerView.ViewHolder {
        public ImageView imgPdfBrowse;
        public ImageView imgPdfImage;
        public RelativeLayout rLayBrowsePdf;
        public TextView tvPdfFilModified;
        public TextView tvPdfFileName;
        public TextView tvPdfSize;

        private BrowsePdfFileViewHolder(View view) {
            super(view);
            if (isBrowserGridEnabled) {
                imgPdfBrowse = view.findViewById(R.id.imgPdfBrowse);
                intFileFolderColor = Color.parseColor("#FFED8B28");
            } else {
                imgPdfImage = view.findViewById(R.id.imgPdfImage);
            }
            tvPdfFileName = view.findViewById(R.id.tvPdfFileName);
            tvPdfFilModified = view.findViewById(R.id.tvPdfFilModified);
            tvPdfSize = view.findViewById(R.id.tvPdfSize);
            rLayBrowsePdf = view.findViewById(R.id.rLayBrowsePdf);
        }
    }
}
