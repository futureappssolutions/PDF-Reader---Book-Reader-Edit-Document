package pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.R;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.GetSet.Tool;

import java.util.List;

public class ToolsAdapterHome extends RecyclerView.Adapter<ToolsAdapterHome.ToolsViewHolder> {
    public Context mContext;
    public OnSelectedMenuClickListener toolClickListener;
    public List<Tool> tools;

    public ToolsAdapterHome(Context context, List<Tool> list, OnSelectedMenuClickListener toolClick) {
        this.tools = list;
        this.mContext = context;
        this.toolClickListener = toolClick;
    }

    @NonNull
    @Override
    public ToolsViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ToolsViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_tool, viewGroup, false));
    }

    public void onBindViewHolder(final ToolsViewHolder toolsViewHolder, int i) {
        Tool tool = this.tools.get(i);
        toolsViewHolder.tvMenuToolTitle.setText(tool.getTitle());
        toolsViewHolder.imgIconTool.setImageDrawable(ContextCompat.getDrawable(this.mContext, tool.getDrawable()));
        toolsViewHolder.layToolItem.setOnClickListener(view -> ToolsAdapterHome.this.menuToolClick(toolsViewHolder.getAdapterPosition()));
    }

    @Override
    public int getItemCount() {
        return this.tools.size();
    }

    public void menuToolClick(int i) {
        OnSelectedMenuClickListener onSelectedMenuClickListener = this.toolClickListener;
        if (onSelectedMenuClickListener != null) {
            onSelectedMenuClickListener.onToolClicked(i);
        }
    }

    public interface OnSelectedMenuClickListener {
        void onToolClicked(int i);
    }

    public static class ToolsViewHolder extends RecyclerView.ViewHolder {
        public ImageView imgIconTool;
        public LinearLayout layToolItem;
        public TextView tvMenuToolTitle;

        public ToolsViewHolder(View view) {
            super(view);
            this.tvMenuToolTitle = (TextView) view.findViewById(R.id.tvMenuToolTitle);
            this.imgIconTool = (ImageView) view.findViewById(R.id.imgIconTool);
            this.layToolItem = (LinearLayout) view.findViewById(R.id.layToolItem);
        }
    }
}
