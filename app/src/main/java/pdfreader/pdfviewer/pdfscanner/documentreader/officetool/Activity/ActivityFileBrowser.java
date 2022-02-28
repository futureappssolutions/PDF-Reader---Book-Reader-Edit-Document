package pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Ads.GoogleAppLovinAds;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.R;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Adapter.AdapterFileBrowser;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Fragments.FragmentFileList;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.GetSet.PdfDataType;

import java.io.File;

public class ActivityFileBrowser extends AppCompatActivity implements AdapterFileBrowser.OnPdfClickListener {
    String rootPath;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_file_browser);

        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        LinearLayout ll_banner = findViewById(R.id.ll_banner);
        GoogleAppLovinAds.showBannerAds(ActivityFileBrowser.this,ll_banner);


        rootPath = Environment.getExternalStorageDirectory() + "/";
        browseFileDirPathlList(this.rootPath);
    }

    public void browseFileDirPathlList(String str) {
        if (new File(str).isDirectory()) {
            FragmentFileList newInstance = FragmentFileList.newInstance(str);
            if (TextUtils.equals(str, rootPath)) {
                getSupportFragmentManager().beginTransaction().replace(R.id.framePdfFileList, newInstance).commit();
            } else {
                getSupportFragmentManager().beginTransaction().replace(R.id.framePdfFileList, newInstance).addToBackStack(null).commit();
            }
        } else {
            Intent intent = new Intent(this, ActivityPDFViewer.class);
            intent.putExtra(ActivityMain.PDF_LOCATION, str);
            startActivity(intent);
        }
    }

    @Override
    public void onPdfClicked(PdfDataType pdfDataType) {
        browseFileDirPathlList(pdfDataType.getAbsolutePath());
    }
}