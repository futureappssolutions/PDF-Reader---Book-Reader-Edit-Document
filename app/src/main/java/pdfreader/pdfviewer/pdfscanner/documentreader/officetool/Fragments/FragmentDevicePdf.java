package pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Fragments;

import android.Manifest;
import android.animation.Animator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.utils.DataUpdatedEvent;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Activity.ActivityMain;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.R;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Adapter.AdapterDevicePdfs;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.customview.MaterialSearchView;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.data.DbHelper;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.GetSet.PdfDataType;

public class FragmentDevicePdf extends Fragment implements MaterialSearchView.OnQueryTextListener {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    public static String MORE_OPTIONS_TIP = "prefs_more_options_tip";
    public FragmentActivity activityCompat;
    public DbHelper dbHelper;
    public AdapterDevicePdfs devicePdfsAdapter;
    public boolean isGridViewEnabled;
    public LinearLayout layNoDevicePdf;
    public ProgressBar progressDevicePdf;
    LinearLayout ll_progress;
    public RelativeLayout rLayTapMore;
    public RecyclerView recycleDevicePdf;
    public boolean showMoreOptionsTip;
    public SwipeRefreshLayout swipePdfRecycle;
    public ImageView imgTapClose;
    public String mParam1;
    public String mParam2;
    List<PdfDataType> myPdfDataTypes = new ArrayList<>();
    int numberOfColumns;
    SharedPreferences sharedPreferences;
    private boolean isFragmentVisibleToUser;
    private MaterialSearchView searchBrowsePdf;

    public static FragmentDevicePdf newInstance(String str, String str2) {
        FragmentDevicePdf devicePdfFragment = new FragmentDevicePdf();
        Bundle bundle = new Bundle();
        bundle.putString(ARG_PARAM1, str);
        bundle.putString(ARG_PARAM2, str2);
        devicePdfFragment.setArguments(bundle);
        return devicePdfFragment;
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        activityCompat = getActivity();
        dbHelper = DbHelper.getInstance(activityCompat);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        isGridViewEnabled = sharedPreferences.getBoolean(ActivityMain.GRID_VIEW_ENABLED, false);
        numberOfColumns = sharedPreferences.getInt(ActivityMain.GRID_VIEW_NUM_OF_COLUMNS, 2);
        showMoreOptionsTip = sharedPreferences.getBoolean(MORE_OPTIONS_TIP, true);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        searchBrowsePdf = activityCompat.findViewById(R.id.searchBarPdf);
        layNoDevicePdf = view.findViewById(R.id.layNoDevicePdf);
        recycleDevicePdf = view.findViewById(R.id.recycleDevicePdf);
        progressDevicePdf = view.findViewById(R.id.progressDevicePdf);
        ll_progress = view.findViewById(R.id.ll_progress);
        rLayTapMore = view.findViewById(R.id.rLayTapMore);
        imgTapClose = view.findViewById(R.id.imgTapClose);
        swipePdfRecycle = view.findViewById(R.id.swipePdfRecycle);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {

            } else {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                Uri uri = Uri.fromParts("package", activityCompat.getPackageName(), null);
                intent.setData(uri);
                startActivityForResult(intent, 100);
            }
        } else {
            ActivityCompat.requestPermissions(activityCompat, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        imgTapClose.setOnClickListener(view1 -> {
            rLayTapMore.setVisibility(View.GONE);
            rLayTapMore.animate().translationY((float) (-rLayTapMore.getHeight())).alpha(0.0f).setListener(new Animator.AnimatorListener() {

                public void onAnimationCancel(Animator animator) {
                }

                public void onAnimationRepeat(Animator animator) {
                }

                public void onAnimationStart(Animator animator) {
                }

                public void onAnimationEnd(Animator animator) {
                    rLayTapMore.setVisibility(View.GONE);
                    SharedPreferences.Editor edit = sharedPreferences.edit();
                    edit.putBoolean(FragmentDevicePdf.MORE_OPTIONS_TIP, false);
                    edit.apply();
                }
            });
        });

        if (showMoreOptionsTip) {
            rLayTapMore.setVisibility(View.GONE);
        } else {
            rLayTapMore.setVisibility(View.GONE);
        }

        if (isGridViewEnabled) {
            setPdfForGridView(activityCompat, recycleDevicePdf, numberOfColumns);
        } else {
            setPdfForListView(activityCompat, recycleDevicePdf);
        }

        swipePdfRecycle.setOnRefreshListener(() -> new refreshDevicePdfFiles().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR));
    }

    @Override
    public void onRequestPermissionsResult(int i, @NonNull String[] strArr, @NonNull int[] iArr) {
        super.onRequestPermissionsResult(i, strArr, iArr);
        if (i == 1 && iArr.length >= 1 && iArr[0] == 0) {
            new DevicePdfLoad().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            return;
        }
        new AlertDialog.Builder(activityCompat).setTitle(R.string.app_name).setMessage(R.string.exit_app_has_no_permission).setCancelable(false).setPositiveButton(R.string.ok, (dialogInterface, i1) -> activityCompat.finish()).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            new DevicePdfLoad().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        new DevicePdfLoad().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        return layoutInflater.inflate(R.layout.fragment_device_pdf, viewGroup, false);
    }

    @Override
    public void setUserVisibleHint(boolean z) {
        super.setUserVisibleHint(z);
        if (z) {
            isFragmentVisibleToUser = true;
            if (searchBrowsePdf != null) {
                searchBrowsePdf.setOnQueryTextListener(this);
                return;
            }
            return;
        }
        isFragmentVisibleToUser = false;
        if (searchBrowsePdf != null) {
            searchBrowsePdf.setOnQueryTextListener(null);
        }
    }

    @Override
    public boolean onQueryTextSubmit(String str) {
        if (!isFragmentVisibleToUser) {
            return true;
        }
        searchPDFFiles(str);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String str) {
        if (!isFragmentVisibleToUser) {
            return true;
        }
        searchPDFFiles(str);
        return true;
    }

    public void setPdfForGridView(Context context, RecyclerView recyclerView, int i) {
        float valueOf = getResources().getDisplayMetrics().density;
        GridLayoutManager gridLayoutManager = new GridLayoutManager(context, i, RecyclerView.VERTICAL, false);
        recyclerView.setBackgroundColor(getResources().getColor(R.color.colorLightGray));
        recyclerView.setPadding((int) (valueOf * 4.0f), (int) (valueOf * 4.0f), (int) (valueOf * 6.0f), (int) (valueOf * 80.0f));
        recyclerView.setLayoutManager(gridLayoutManager);
    }

    @SuppressLint("ResourceType")
    public void setPdfForListView(Context context, RecyclerView recyclerView) {
        float valueOf = getResources().getDisplayMetrics().density;
        recyclerView.setBackgroundColor(getResources().getColor(17170443));
        recyclerView.setPadding(0, 0, (int) (valueOf * 4.0f), (int) (valueOf * 80.0f));
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
    }

    public void searchPDFFiles(String str) {
        ArrayList<PdfDataType> arrayList = new ArrayList<>();
        for (PdfDataType pdfDataType : myPdfDataTypes) {
            if (pdfDataType.getName().toLowerCase().contains(str.toLowerCase())) {
                arrayList.add(pdfDataType);
            }
            devicePdfsAdapter.filter(arrayList);
        }
    }

    @Subscribe
    public void onPermanetlyDeleteEvent(DataUpdatedEvent.PermanetlyDeleteEvent permanetlyDeleteEvent) {
        new refreshDevicePdfFiles().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Subscribe
    public void onPdfRenameEvent(DataUpdatedEvent.PdfRenameEvent pdfRenameEvent) {
        new refreshDevicePdfFiles().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Subscribe
    public void onRecentPDFStaredEvent(DataUpdatedEvent.RecentPDFStaredEvent recentPDFStaredEvent) {
        recycleDevicePdf.setAdapter(devicePdfsAdapter);
    }

    @Subscribe
    public void onToggleGridViewEvent(DataUpdatedEvent.ToggleGridViewEvent toggleGridViewEvent) {
        boolean z = sharedPreferences.getBoolean(ActivityMain.GRID_VIEW_ENABLED, false);
        isGridViewEnabled = z;
        if (z) {
            int i = sharedPreferences.getInt(ActivityMain.GRID_VIEW_NUM_OF_COLUMNS, 2);
            numberOfColumns = i;
            setPdfForGridView(activityCompat, recycleDevicePdf, i);
        } else {
            setPdfForListView(activityCompat, recycleDevicePdf);
        }

        devicePdfsAdapter = new AdapterDevicePdfs(myPdfDataTypes, activityCompat);
        recycleDevicePdf.setAdapter(devicePdfsAdapter);
    }

    @Subscribe
    public void onSortListEvent(DataUpdatedEvent.SortListEvent sortListEvent) {
        new refreshDevicePdfFiles().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public class DevicePdfLoad extends AsyncTask<Void, Void, Void> {
        public DevicePdfLoad() {
        }

        public void onPreExecute() {
            super.onPreExecute();
            myPdfDataTypes.clear();

            progressDevicePdf.setVisibility(View.VISIBLE);
            ll_progress.setVisibility(View.VISIBLE);
        }

        public Void doInBackground(Void... voidArr) {
//            myPdfDataTypes = dbHelper.getAllPdfs();
            walkDir(Environment.getExternalStorageDirectory());
            devicePdfsAdapter = new AdapterDevicePdfs(myPdfDataTypes, activityCompat);
            return null;
        }

        public void onPostExecute(Void r3) {
            super.onPostExecute(r3);
            progressDevicePdf.setVisibility(View.GONE);
            ll_progress.setVisibility(View.GONE);
            recycleDevicePdf.setAdapter(devicePdfsAdapter);
            if (myPdfDataTypes.isEmpty()) {
                layNoDevicePdf.setVisibility(View.VISIBLE);
            } else {
                layNoDevicePdf.setVisibility(View.GONE);
            }
            devicePdfsAdapter.updatePdfData(myPdfDataTypes);
        }
    }

    public class refreshDevicePdfFiles extends AsyncTask<Void, Void, Void> {
        public refreshDevicePdfFiles() {
        }

        public Void doInBackground(Void... voidArr) {
//            myPdfDataTypes = dbHelper.getAllPdfs();
            walkDir(Environment.getExternalStorageDirectory());
            return null;
        }

        public void onPostExecute(Void r3) {
            super.onPostExecute(r3);
            if (myPdfDataTypes.isEmpty()) {
                layNoDevicePdf.setVisibility(View.VISIBLE);
            } else {
                layNoDevicePdf.setVisibility(View.GONE);
            }
            swipePdfRecycle.setRefreshing(false);
            devicePdfsAdapter.updatePdfData(myPdfDataTypes);
        }
    }

    private void walkDir(File file) {
        File[] listFiles = file.listFiles();
        if (listFiles != null) {
            for (int i = 0; i < listFiles.length; i++) {
                if (listFiles[i].isDirectory()) {
                    walkDir(listFiles[i]);
                } else {
                    String name = listFiles[i].getName();
                    if (name != null && (name.endsWith(".pdf") || name.endsWith(".PDF"))) {
                        File file2 = listFiles[i];
                        PdfDataType pdfDataType = new PdfDataType();
                        pdfDataType.setName(file2.getName());
                        pdfDataType.setAbsolutePath(file2.getAbsolutePath());
                        pdfDataType.setPdfUri(Uri.fromFile(file2));
                        pdfDataType.setLength(file2.length());
                        pdfDataType.setLastModified(file2.lastModified());
                        myPdfDataTypes.add(pdfDataType);
                    }
                }
            }
        }
    }
}