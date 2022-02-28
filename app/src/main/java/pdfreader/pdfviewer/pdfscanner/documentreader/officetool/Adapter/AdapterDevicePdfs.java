package pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Adapter;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Ads.GoogleAppLovinAds;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.utils.DataUpdatedEvent;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Activity.ActivityMain;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.R;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.data.DbHelper;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.data.FileDiffCallback;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Fragments.FragmentBottomSheetDialog;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.GetSet.PdfDataType;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.utils.Utils;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

public class AdapterDevicePdfs extends RecyclerView.Adapter<AdapterDevicePdfs.DevicePdfFileViewHolder> {
    public Context context;
    public DbHelper dbHelper = DbHelper.getInstance(context);
    public boolean isDevicePdfGridEnabled;
    public List<PdfDataType> listDevidePdfDataTypeFiles;
    public OnPdfClickListener pdfClickListener;

    public AdapterDevicePdfs(List<PdfDataType> list, Context context2) {
        listDevidePdfDataTypeFiles = list;
        context = context2;
        isDevicePdfGridEnabled = PreferenceManager.getDefaultSharedPreferences(context2).getBoolean(ActivityMain.GRID_VIEW_ENABLED, false);
        Context context3 = context;
        if (context3 instanceof OnPdfClickListener) {
            pdfClickListener = (OnPdfClickListener) context3;
            return;
        }
        throw new RuntimeException(context.toString() + " must implement OnPdfClickListener");
    }

    @NonNull
    @Override
    public DevicePdfFileViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view;
        if (isDevicePdfGridEnabled) {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_pdf_grid, viewGroup, false);
        } else {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_pdf, viewGroup, false);
        }
        return new DevicePdfFileViewHolder(view);
    }

    public void onBindViewHolder(final DevicePdfFileViewHolder devicePdfFileViewHolder, int i) {
        PdfDataType pdfDataType = listDevidePdfDataTypeFiles.get(i);
        String name = pdfDataType.getName();
        Long length = pdfDataType.getLength();
        final String absolutePath = pdfDataType.getAbsolutePath();
        devicePdfFileViewHolder.tvPdfTitle.setText(name);
        devicePdfFileViewHolder.tvPdfSize.setText(Formatter.formatShortFileSize(context, length));
        devicePdfFileViewHolder.tvLastPdfModified.setText(Utils.formatDateToHumanReadable(pdfDataType.getLastModified()));
        if (pdfDataType.isStarred()) {
            devicePdfFileViewHolder.imgStar.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_action_star_yellow));
        } else {
            devicePdfFileViewHolder.imgStar.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_action_star));
        }

        devicePdfFileViewHolder.imgStar.setOnClickListener(view -> {
            if (GoogleAppLovinAds.adsdisplay) {
                GoogleAppLovinAds.showFullAds((Activity) context, () -> {
                    GoogleAppLovinAds.allcount60.start();
                    if (dbHelper.isStared(absolutePath)) {
                        dbHelper.removeStaredPDF(absolutePath);
                        devicePdfFileViewHolder.imgStar.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_action_star));
                    } else {
                        dbHelper.addStaredPDF(absolutePath);
                        devicePdfFileViewHolder.imgStar.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_action_star_yellow));
                    }
                });
            } else {
                if (dbHelper.isStared(absolutePath)) {
                    dbHelper.removeStaredPDF(absolutePath);
                    devicePdfFileViewHolder.imgStar.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_action_star));
                } else {
                    dbHelper.addStaredPDF(absolutePath);
                    devicePdfFileViewHolder.imgStar.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_action_star_yellow));
                }
            }
            EventBus.getDefault().post(new DataUpdatedEvent.DevicePDFStaredEvent());
        });

        devicePdfFileViewHolder.rLayPdf.setOnClickListener(view -> {
            int adapterPosition = devicePdfFileViewHolder.getAdapterPosition();
            pdfClicked(adapterPosition);
            StringBuilder sb = new StringBuilder();
            sb.append("PdfDataType ");
            sb.append(adapterPosition);
            sb.append(" clicked");
        });

        devicePdfFileViewHolder.rLayPdf.setOnLongClickListener(view -> {
            if (devicePdfFileViewHolder.getAdapterPosition() < 0) {
                return true;
            }
            showBottomPDFFile(devicePdfFileViewHolder.getAdapterPosition());
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return listDevidePdfDataTypeFiles.size();
    }

    public void pdfClicked(int i) {
        OnPdfClickListener onPdfClickListener = pdfClickListener;
        if (onPdfClickListener != null && i >= 0) {
            onPdfClickListener.onPdfClicked(listDevidePdfDataTypeFiles.get(i));
        }
    }

    public void updatePdfData(List<PdfDataType> list) {
        DiffUtil.calculateDiff(new FileDiffCallback(listDevidePdfDataTypeFiles, list)).dispatchUpdatesTo(this);
        listDevidePdfDataTypeFiles = list;
    }

    public void filter(List<PdfDataType> list) {
        listDevidePdfDataTypeFiles = list;
        notifyDataSetChanged();
    }

    public void showBottomPDFFile(int i) {
        String absolutePath = listDevidePdfDataTypeFiles.get(i).getAbsolutePath();
        Bundle bundle = new Bundle();
        bundle.putString(FragmentBottomSheetDialog.FROM_RECENT, absolutePath);
        bundle.putBoolean("fromRecent", true);
        FragmentBottomSheetDialog bottomSheetDialogFragment = new FragmentBottomSheetDialog();
        bottomSheetDialogFragment.setArguments(bundle);
        bottomSheetDialogFragment.show(((AppCompatActivity) context).getSupportFragmentManager(), bottomSheetDialogFragment.getTag());
    }

    public interface OnPdfClickListener {
        void onPdfClicked(PdfDataType pdfDataType);
    }

    public class DevicePdfFileViewHolder extends RecyclerView.ViewHolder {
        public ImageView imgPdfImage;
        public ImageView imgStar;
        public RelativeLayout rLayPdf;
        public TextView tvLastPdfModified;
        public TextView tvPdfSize;
        public TextView tvPdfTitle;

        private DevicePdfFileViewHolder(View view) {
            super(view);
            if (isDevicePdfGridEnabled) {
                imgPdfImage = view.findViewById(R.id.imgPdfImage);
            }
            tvPdfTitle = view.findViewById(R.id.tvPdfTitle);
            tvLastPdfModified = view.findViewById(R.id.tvLastPdfModified);
            tvPdfSize = view.findViewById(R.id.tvPdfSize);
            imgStar = view.findViewById(R.id.imgStar);
            rLayPdf = view.findViewById(R.id.rLayPdf);
        }
    }
}