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
import androidx.recyclerview.widget.RecyclerView;

import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Ads.GoogleAppLovinAds;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.utils.DataUpdatedEvent;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Activity.ActivityMain;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.R;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.data.DbHelper;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Fragments.FragmentBottomSheetDialog;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.GetSet.PdfDataType;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.utils.Utils;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

public class AdapterStarredPDF extends RecyclerView.Adapter<AdapterStarredPDF.SharedPDFViewHolder> {
    public boolean isGridViewEnabled;
    public Context mContext;
    public String THUMBNAILS_DIR;
    public List<PdfDataType> pdfDataTypeFiles;
    public OnStaredPdfClickListener staredPdfClickListener;

    public AdapterStarredPDF(Context context, List<PdfDataType> list) {
        pdfDataTypeFiles = list;
        mContext = context;
        THUMBNAILS_DIR = context.getCacheDir() + "/Thumbnails/";
        isGridViewEnabled = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(ActivityMain.GRID_VIEW_ENABLED, false);
        Context context2 = mContext;
        if (context2 instanceof OnStaredPdfClickListener) {
            staredPdfClickListener = (OnStaredPdfClickListener) context2;
            return;
        }
        throw new RuntimeException(mContext.toString() + " must implement OnStaredPdfClickListener");
    }

    @NonNull
    @Override
    public SharedPDFViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view;
        if (isGridViewEnabled) {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_pdf_grid, viewGroup, false);
        } else {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_pdf, viewGroup, false);
        }
        return new SharedPDFViewHolder(view);
    }

    public void onBindViewHolder(final SharedPDFViewHolder sharedPDFViewHolder, int i) {
        PdfDataType pdfDataType = pdfDataTypeFiles.get(i);
        final String absolutePath = pdfDataType.getAbsolutePath();
        String name = pdfDataType.getName();
        Long length = pdfDataType.getLength();
        final DbHelper instance = DbHelper.getInstance(mContext);
        sharedPDFViewHolder.tvPdfTitle.setText(name);
        sharedPDFViewHolder.tvPdfSize.setText(Formatter.formatShortFileSize(mContext, length));
        sharedPDFViewHolder.tvLastPdfModified.setText(Utils.formatDateToHumanReadable(pdfDataType.getLastModified()));
        if (pdfDataType.isStarred()) {
            sharedPDFViewHolder.imgStar.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_action_star_yellow));
        }
        if (isGridViewEnabled) {
            Picasso.get().load(pdfDataType.getThumbUri()).into(sharedPDFViewHolder.imgPdfImage);
        }

        sharedPDFViewHolder.imgStar.setOnClickListener(view -> {
            if (GoogleAppLovinAds.adsdisplay) {
                GoogleAppLovinAds.showFullAds((Activity) mContext, () -> {
                    GoogleAppLovinAds.allcount60.start();
                    if (instance.isStared(absolutePath)) {
                        instance.removeStaredPDF(absolutePath);
                        sharedPDFViewHolder.imgStar.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_action_star));
                    } else {
                        instance.addStaredPDF(absolutePath);
                        sharedPDFViewHolder.imgStar.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_action_star_yellow));
                    }
                });
            } else {
                if (instance.isStared(absolutePath)) {
                    instance.removeStaredPDF(absolutePath);
                    sharedPDFViewHolder.imgStar.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_action_star));
                } else {
                    instance.addStaredPDF(absolutePath);
                    sharedPDFViewHolder.imgStar.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_action_star_yellow));
                }
            }
            EventBus.getDefault().post(new DataUpdatedEvent.RecentPDFStaredEvent());
        });


        sharedPDFViewHolder.rLayPdf.setOnClickListener(view -> {
            int adapterPosition = sharedPDFViewHolder.getAdapterPosition();
            staredPdfClicked(adapterPosition);
        });

        sharedPDFViewHolder.rLayPdf.setOnLongClickListener(view -> {
            showBottomSheet(sharedPDFViewHolder.getAdapterPosition());
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return pdfDataTypeFiles.size();
    }

    public void filter(List<PdfDataType> list) {
        pdfDataTypeFiles = list;
        notifyDataSetChanged();
    }

    public void staredPdfClicked(int i) {
        OnStaredPdfClickListener onStaredPdfClickListener = staredPdfClickListener;
        if (onStaredPdfClickListener != null) {
            onStaredPdfClickListener.onStaredPdfClicked(pdfDataTypeFiles.get(i));
        }
    }

    public void showBottomSheet(int i) {
        String absolutePath = pdfDataTypeFiles.get(i).getAbsolutePath();
        Bundle bundle = new Bundle();
        bundle.putString(FragmentBottomSheetDialog.FROM_RECENT, absolutePath);
        bundle.putBoolean("fromRecent", true);
        FragmentBottomSheetDialog bottomSheetDialogFragment = new FragmentBottomSheetDialog();
        bottomSheetDialogFragment.setArguments(bundle);
        bottomSheetDialogFragment.show(((AppCompatActivity) mContext).getSupportFragmentManager(), bottomSheetDialogFragment.getTag());
    }

    public interface OnStaredPdfClickListener {
        void onStaredPdfClicked(PdfDataType pdfDataType);
    }

    public class SharedPDFViewHolder extends RecyclerView.ViewHolder {
        public ImageView imgPdfImage;
        public ImageView imgStar;
        public RelativeLayout rLayPdf;
        public TextView tvLastPdfModified;
        public TextView tvPdfSize;
        public TextView tvPdfTitle;

        private SharedPDFViewHolder(View view) {
            super(view);
            if (isGridViewEnabled) {
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