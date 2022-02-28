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

public class AdapterRecentPdfs extends RecyclerView.Adapter<AdapterRecentPdfs.RecentPdfViewHolder> {
    public Context context;
    public OnHistoryPdfClickListener historyPdfClickListener;
    public boolean isReccentPdfGridEnabled;
    public List<PdfDataType> listRecentPdfFile;

    public AdapterRecentPdfs(List<PdfDataType> list, Context context2) {
        listRecentPdfFile = list;
        context = context2;
        isReccentPdfGridEnabled = PreferenceManager.getDefaultSharedPreferences(context2).getBoolean(ActivityMain.GRID_VIEW_ENABLED, false);
        Context context3 = context;
        if (context3 instanceof OnHistoryPdfClickListener) {
            historyPdfClickListener = (OnHistoryPdfClickListener) context3;
            return;
        }
        throw new RuntimeException(context.toString() + " must implement OnHistoryPdfClickListener");
    }

    @NonNull
    @Override
    public RecentPdfViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view;
        if (isReccentPdfGridEnabled) {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_pdf_grid, viewGroup, false);
        } else {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_pdf, viewGroup, false);
        }
        return new RecentPdfViewHolder(view);
    }

    public void onBindViewHolder(final RecentPdfViewHolder recentPdfViewHolder, int i) {
        PdfDataType pdfDataType = listRecentPdfFile.get(i);
        final String absolutePath = pdfDataType.getAbsolutePath();
        String name = pdfDataType.getName();
        Long length = pdfDataType.getLength();
        final DbHelper instance = DbHelper.getInstance(context);
        recentPdfViewHolder.tvPdfTitle.setText(name);
        recentPdfViewHolder.tvPdfSize.setText(Formatter.formatShortFileSize(context, length));
        recentPdfViewHolder.tvLastPdfModified.setText(Utils.formatDateToHumanReadable(pdfDataType.getLastModified()));
        if (pdfDataType.isStarred()) {
            recentPdfViewHolder.imgStar.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_action_star_yellow));
        } else {
            recentPdfViewHolder.imgStar.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_action_star));
        }

        recentPdfViewHolder.imgStar.setOnClickListener(view -> {
            if (GoogleAppLovinAds.adsdisplay) {
                GoogleAppLovinAds.showFullAds((Activity) context, () -> {
                    GoogleAppLovinAds.allcount60.start();
                    if (instance.isStared(absolutePath)) {
                        instance.removeStaredPDF(absolutePath);
                        recentPdfViewHolder.imgStar.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_action_star));
                    } else {
                        instance.addStaredPDF(absolutePath);
                        recentPdfViewHolder.imgStar.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_action_star_yellow));
                    }
                });
            } else {
                if (instance.isStared(absolutePath)) {
                    instance.removeStaredPDF(absolutePath);
                    recentPdfViewHolder.imgStar.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_action_star));
                } else {
                    instance.addStaredPDF(absolutePath);
                    recentPdfViewHolder.imgStar.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_action_star_yellow));
                }
            }
            EventBus.getDefault().post(new DataUpdatedEvent.RecentPDFStaredEvent());
        });

        recentPdfViewHolder.rLayPdf.setOnClickListener(view -> {
            int adapterPosition = recentPdfViewHolder.getAdapterPosition();
            pdfClickedHistory(adapterPosition);
            StringBuilder sb = new StringBuilder();
            sb.append("PdfDataType ");
            sb.append(adapterPosition);
            sb.append(" clicked");
        });

        recentPdfViewHolder.rLayPdf.setOnLongClickListener(view -> {
            showBottomRecentPDF(recentPdfViewHolder.getAdapterPosition());
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return listRecentPdfFile.size();
    }

    public void filter(List<PdfDataType> list) {
        listRecentPdfFile = list;
        notifyDataSetChanged();
    }

    public void pdfClickedHistory(int i) {
        OnHistoryPdfClickListener onHistoryPdfClickListener = historyPdfClickListener;
        if (onHistoryPdfClickListener != null && i >= 0) {
            onHistoryPdfClickListener.onHistoryPdfClicked(listRecentPdfFile.get(i));
        }
    }

    public void updateData(List<PdfDataType> list) {
        DiffUtil.calculateDiff(new FileDiffCallback(listRecentPdfFile, list)).dispatchUpdatesTo(this);
        listRecentPdfFile = list;
    }

    public void showBottomRecentPDF(int i) {
        String absolutePath = listRecentPdfFile.get(i).getAbsolutePath();
        Bundle bundle = new Bundle();
        bundle.putString(FragmentBottomSheetDialog.FROM_RECENT, absolutePath);
        bundle.putBoolean("fromRecent", true);
        FragmentBottomSheetDialog bottomSheetDialogFragment = new FragmentBottomSheetDialog();
        bottomSheetDialogFragment.setArguments(bundle);
        bottomSheetDialogFragment.show(((AppCompatActivity) context).getSupportFragmentManager(), bottomSheetDialogFragment.getTag());
    }

    public interface OnHistoryPdfClickListener {
        void onHistoryPdfClicked(PdfDataType pdfDataType);
    }

    public class RecentPdfViewHolder extends RecyclerView.ViewHolder {
        public ImageView imgPdfImage;
        public ImageView imgStar;
        public RelativeLayout rLayPdf;
        public TextView tvLastPdfModified;
        public TextView tvPdfSize;
        public TextView tvPdfTitle;

        private RecentPdfViewHolder(View view) {
            super(view);
            if (isReccentPdfGridEnabled) {
                imgPdfImage = (ImageView) view.findViewById(R.id.imgPdfImage);
            }
            tvPdfTitle = (TextView) view.findViewById(R.id.tvPdfTitle);
            tvLastPdfModified = (TextView) view.findViewById(R.id.tvLastPdfModified);
            tvPdfSize = (TextView) view.findViewById(R.id.tvPdfSize);
            imgStar = (ImageView) view.findViewById(R.id.imgStar);
            rLayPdf = (RelativeLayout) view.findViewById(R.id.rLayPdf);
        }
    }
}
