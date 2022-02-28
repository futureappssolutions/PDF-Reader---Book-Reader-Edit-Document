package pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Ads.GoogleAppLovinAds;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.utils.DataUpdatedEvent;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Activity.ActivityMain;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.R;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Adapter.AdapterRecentPdfs;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.customview.MaterialSearchView;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.data.DbHelper;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.GetSet.PdfDataType;

public class FragmentRecentPdf extends Fragment implements MaterialSearchView.OnQueryTextListener {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    public Context context;
    public DbHelper dbHelper;
    public boolean isGridEnabled;
    public LinearLayout layNoRecentPdf;
    public List<PdfDataType> listRecentPdfHistory = new ArrayList<>();
    public ProgressBar progressRecentPdfHistory;
    LinearLayout ll_progress;
    public AdapterRecentPdfs recentPdfsAdapter;
    public RecyclerView recyclerRecentPdfHistory;
    public SwipeRefreshLayout swipeRecentPdfRefresh;
    public boolean isFragVisibleToUser;
    public String mParam1;
    public String mParam2;
    public MaterialSearchView materialSearchView;
    public int numberOfColumnsRecent;
    public OnRecentPdfClickListener onRecentPdfClickListener;
    public SharedPreferences sharedPreferences;
    public FrameLayout fl_native;

    public static FragmentRecentPdf newInstance(String str, String str2) {
        FragmentRecentPdf recentPdfFragment = new FragmentRecentPdf();
        Bundle bundle = new Bundle();
        bundle.putString(ARG_PARAM1, str);
        bundle.putString(ARG_PARAM2, str2);
        recentPdfFragment.setArguments(bundle);
        return recentPdfFragment;
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        context = getContext();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        isGridEnabled = sharedPreferences.getBoolean(ActivityMain.GRID_VIEW_ENABLED, false);
        numberOfColumnsRecent = sharedPreferences.getInt(ActivityMain.GRID_VIEW_NUM_OF_COLUMNS, 2);
        recentPdfsAdapter = new AdapterRecentPdfs(listRecentPdfHistory, context);
        dbHelper = DbHelper.getInstance(context);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        fl_native = view.findViewById(R.id.fl_native);
        GoogleAppLovinAds.showNativeAds(getActivity(),fl_native);

        recyclerRecentPdfHistory = view.findViewById(R.id.recyclerRecentPdfHistory);
        layNoRecentPdf = view.findViewById(R.id.layNoRecentPdf);
        materialSearchView = requireActivity().findViewById(R.id.searchBarPdf);
        swipeRecentPdfRefresh = view.findViewById(R.id.swipeRecentPdfRefresh);
        progressRecentPdfHistory = view.findViewById(R.id.progressRecentPdfHistory);
        ll_progress = view.findViewById(R.id.ll_progress);

        materialSearchView.setOnQueryTextListener(this);

        if (isGridEnabled) {
            setRecentPdfGrid(context, recyclerRecentPdfHistory, numberOfColumnsRecent);
        } else {
            setRecentPdfListView(context, recyclerRecentPdfHistory);
        }

        new LoadRecentPdfHistory().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        swipeRecentPdfRefresh.setOnRefreshListener(() -> new UpdateRecentPdf().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]));
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        return layoutInflater.inflate(R.layout.fragment_recents_pdf, viewGroup, false);
    }

    @Override
    public void onAttach(@NonNull Context context2) {
        super.onAttach(context2);
        if (context2 instanceof OnRecentPdfClickListener) {
            onRecentPdfClickListener = (OnRecentPdfClickListener) context2;
            return;
        }
        throw new RuntimeException(context2.toString() + " must implement OnRecentPdfClickListener");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onRecentPdfClickListener = null;
    }

    @Override
    public void setUserVisibleHint(boolean z) {
        super.setUserVisibleHint(z);
        if (z) {
            isFragVisibleToUser = true;
            if (materialSearchView != null) {
                materialSearchView.setOnQueryTextListener(this);
                return;
            }
            return;
        }
        isFragVisibleToUser = false;
        if (materialSearchView != null) {
            materialSearchView.setOnQueryTextListener(null);
        }
    }

    @Override
    public boolean onQueryTextSubmit(String str) {
        if (!isFragVisibleToUser) {
            return true;
        }
        searchRecentOpenPDF(str);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String str) {
        if (!isFragVisibleToUser) {
            return true;
        }
        searchRecentOpenPDF(str);
        return true;
    }

    public void setRecentPdfGrid(Context context2, RecyclerView recyclerView, int i) {
        float valueOf = getResources().getDisplayMetrics().density;
        GridLayoutManager gridLayoutManager = new GridLayoutManager(context2, i, RecyclerView.VERTICAL, false);
        recyclerView.setBackgroundColor(getResources().getColor(R.color.light_color));
      //  recyclerView.setPadding((int) (valueOf * 4.0f), (int) (valueOf * 4.0f), (int) (valueOf * 6.0f), (int) (valueOf * 80.0f));
        recyclerView.setLayoutManager(gridLayoutManager);
    }

    @SuppressLint("ResourceType")
    public void setRecentPdfListView(Context context2, RecyclerView recyclerView) {
        float valueOf = getResources().getDisplayMetrics().density;
        recyclerView.setBackgroundColor(getResources().getColor(R.color.light_color));
       // recyclerView.setPadding(0, 0, (int) (valueOf * 4.0f), (int) (valueOf * 80.0f));
        recyclerView.setLayoutManager(new LinearLayoutManager(context2));
    }

    public void searchRecentOpenPDF(String str) {
        ArrayList<PdfDataType> arrayList = new ArrayList<>();
        for (PdfDataType pdfDataType : listRecentPdfHistory) {
            if (pdfDataType.getName().toLowerCase().contains(str.toLowerCase())) {
                arrayList.add(pdfDataType);
            }
            recentPdfsAdapter.filter(arrayList);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        new UpdateRecentPdf().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void onRecentPdfInsert(DataUpdatedEvent.RecentPdfInsert recentPdfInsert) {
        new UpdateHistoryPdfFiles().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Subscribe
    public void onRecentPdfDeleteEvent(DataUpdatedEvent.RecentPdfDeleteEvent recentPdfDeleteEvent) {
        new UpdateHistoryPdfFiles().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Subscribe
    public void onPermanetlyDeleteEvent(DataUpdatedEvent.PermanetlyDeleteEvent permanetlyDeleteEvent) {
        new UpdateHistoryPdfFiles().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Subscribe
    public void onRecentPdfClearEvent(DataUpdatedEvent.RecentPdfClearEvent recentPdfClearEvent) {
        new UpdateHistoryPdfFiles().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Subscribe
    public void onPdfRenameEvent(DataUpdatedEvent.PdfRenameEvent pdfRenameEvent) {
        new UpdateHistoryPdfFiles().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Subscribe
    public void onDevicePDFStaredEvent(DataUpdatedEvent.DevicePDFStaredEvent devicePDFStaredEvent) {
        recyclerRecentPdfHistory.setAdapter(recentPdfsAdapter);
    }

    @Subscribe
    public void onToggleGridViewEvent(DataUpdatedEvent.ToggleGridViewEvent toggleGridViewEvent) {
        isGridEnabled = sharedPreferences.getBoolean(ActivityMain.GRID_VIEW_ENABLED, false);
        int i = sharedPreferences.getInt(ActivityMain.GRID_VIEW_NUM_OF_COLUMNS, 2);
        numberOfColumnsRecent = i;
        if (isGridEnabled) {
            setRecentPdfGrid(context, recyclerRecentPdfHistory, i);
        } else {
            setRecentPdfListView(context, recyclerRecentPdfHistory);
        }

        recentPdfsAdapter = new AdapterRecentPdfs(listRecentPdfHistory, context);
        recyclerRecentPdfHistory.setAdapter(recentPdfsAdapter);
        recentPdfsAdapter.notifyDataSetChanged();
    }

    public interface OnRecentPdfClickListener {
        void onRecentPdfClick(Uri uri);
    }

    public class LoadRecentPdfHistory extends AsyncTask<Void, Void, Void> {
        public LoadRecentPdfHistory() {
        }

        public void onPreExecute() {
            super.onPreExecute();
            progressRecentPdfHistory.setVisibility(View.VISIBLE);
            ll_progress.setVisibility(View.VISIBLE);
        }

        public Void doInBackground(Void... voidArr) {
            DbHelper instance = DbHelper.getInstance(context);
            listRecentPdfHistory.clear();
            listRecentPdfHistory = instance.getRecentPDFs();
            recentPdfsAdapter = new AdapterRecentPdfs(listRecentPdfHistory, context);
            return null;
        }

        public void onPostExecute(Void r2) {
            super.onPostExecute(r2);
            progressRecentPdfHistory.setVisibility(View.GONE);
            ll_progress.setVisibility(View.GONE);
            if (listRecentPdfHistory.isEmpty()) {
                layNoRecentPdf.setVisibility(View.VISIBLE);
                fl_native.setVisibility(View.GONE);
            } else {
                layNoRecentPdf.setVisibility(View.GONE);
                fl_native.setVisibility(View.VISIBLE);
            }
            recyclerRecentPdfHistory.setAdapter(recentPdfsAdapter);
        }
    }

    public class UpdateRecentPdf extends AsyncTask<Void, Void, Void> {
        public UpdateRecentPdf() {
        }

        public Void doInBackground(Void... voidArr) {
            if (recyclerRecentPdfHistory == null) {
                return null;
            }
            listRecentPdfHistory = dbHelper.getRecentPDFs();
            return null;
        }

        public void onPostExecute(Void r3) {
            super.onPostExecute(r3);
            if (listRecentPdfHistory.isEmpty()) {
                layNoRecentPdf.setVisibility(View.VISIBLE);
                fl_native.setVisibility(View.GONE);
            } else {
                layNoRecentPdf.setVisibility(View.GONE);
                fl_native.setVisibility(View.VISIBLE);
            }
            swipeRecentPdfRefresh.setRefreshing(false);
            recentPdfsAdapter.updateData(listRecentPdfHistory);
        }
    }

    public class UpdateHistoryPdfFiles extends AsyncTask<Void, Void, Void> {
        public UpdateHistoryPdfFiles() {
        }

        public Void doInBackground(Void... voidArr) {
            if (recyclerRecentPdfHistory == null) {
                return null;
            }
            listRecentPdfHistory = dbHelper.getRecentPDFs();
            return null;
        }

        public void onPostExecute(Void r3) {
            super.onPostExecute(r3);
            if (listRecentPdfHistory.isEmpty()) {
                layNoRecentPdf.setVisibility(View.VISIBLE);
                fl_native.setVisibility(View.GONE);
            } else {
                layNoRecentPdf.setVisibility(View.GONE);
                fl_native.setVisibility(View.VISIBLE);
            }
            swipeRecentPdfRefresh.setRefreshing(false);
            recentPdfsAdapter.updateData(listRecentPdfHistory);
        }
    }
}