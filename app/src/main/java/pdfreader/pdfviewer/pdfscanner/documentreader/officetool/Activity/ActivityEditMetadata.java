package pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Ads.GoogleAppLovinAds;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.R;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.utils.Utils;
import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDDocumentInformation;
import com.tom_roush.pdfbox.util.PDFBoxResourceLoader;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class ActivityEditMetadata extends AppCompatActivity {
    EditText etAuthor;
    EditText etCreatedDate;
    EditText etCreatorName;
    EditText etKeywords;
    EditText etModifiedDate;
    EditText etProducerName;
    EditText etSubject;
    EditText etTitle;
    Context mContext;
    String pdfPath;

    public void banner() {
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_edit_metadata);

        LinearLayout ll_banner = findViewById(R.id.ll_banner);
        GoogleAppLovinAds.showBannerAds(ActivityEditMetadata.this,ll_banner);



        if (Utils.isTablet(this)) {
            Utils.setLightStatusBar(this);
        } else {
            Utils.clearLightStatusBar(this);
        }

        setSupportActionBar(findViewById(R.id.toolBar));
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        mContext = this;
        etTitle = findViewById(R.id.etTitle);
        etAuthor = findViewById(R.id.etAuthor);
        etCreatorName = findViewById(R.id.etCreatorName);
        etProducerName = findViewById(R.id.etProducerName);
        etSubject = findViewById(R.id.etSubject);
        etKeywords = findViewById(R.id.etKeywords);
        etCreatedDate = findViewById(R.id.etCreatedDate);
        etModifiedDate = findViewById(R.id.etModifiedDate);

        pdfPath = getIntent().getStringExtra("com.example.pdfreader.PDF_PATH");
        new LoadMetadata().execute();
        banner();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_edit_metadata, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.menuSave) {
            new SaveMetadata().execute();
        }
        return super.onOptionsItemSelected(menuItem);
    }

    public void showFileProtectedDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.file_protected).setMessage(R.string.file_protected_unprotect).setPositiveButton(R.string.ok, null);
        builder.create().show();
    }

    public class LoadMetadata extends AsyncTask<Void, Void, Void> {
        PdfDocument.Meta meta;
        ProgressDialog progressDialog;

        public LoadMetadata() {
        }

        public void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(mContext);
            progressDialog.setMessage(getString(R.string.loading_wait));
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
        }

        public Void doInBackground(Void... voidArr) {
            try {
                PdfiumCore pdfiumCore = new PdfiumCore(mContext);
                meta = pdfiumCore.getDocumentMeta(pdfiumCore.newDocument(mContext.getContentResolver().openFileDescriptor(Uri.fromFile(new File(pdfPath)), "r")));
                return null;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        public void onPostExecute(Void r3) {
            super.onPostExecute(r3);
            progressDialog.dismiss();
            if (meta != null) {
                etTitle.setText(meta.getTitle());
                etAuthor.setText(meta.getAuthor());
                etCreatorName.setText(meta.getCreator());
                etProducerName.setText(meta.getProducer());
                etSubject.setText(meta.getSubject());
                etKeywords.setText(meta.getKeywords());
                etCreatedDate.setText(Utils.formatMetadataDate(getApplicationContext(), meta.getCreationDate()));
                etModifiedDate.setText(Utils.formatMetadataDate(getApplicationContext(), meta.getModDate()));
                return;
            }
            Toast.makeText(mContext, R.string.cant_load_metadata, Toast.LENGTH_LONG).show();
        }
    }

    public class SaveMetadata extends AsyncTask<Void, Void, Void> {
        boolean isSaved = false;
        ProgressDialog progressDialog;

        public SaveMetadata() {
        }

        public void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(mContext);
            progressDialog.setMessage(mContext.getResources().getString(R.string.saving_wait));
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
        }

        public Void doInBackground(Void... voidArr) {
            PDFBoxResourceLoader.init(mContext);
            try {
                PDDocument load = PDDocument.load(new File(pdfPath));
                if (!load.isEncrypted()) {
                    PDDocumentInformation documentInformation = load.getDocumentInformation();
                    documentInformation.setTitle(etTitle.getText().toString());
                    documentInformation.setAuthor(etAuthor.getText().toString());
                    documentInformation.setCreator(etCreatorName.getText().toString());
                    documentInformation.setProducer(etProducerName.getText().toString());
                    documentInformation.setSubject(etSubject.getText().toString());
                    documentInformation.setKeywords(etKeywords.getText().toString());
                    load.setDocumentInformation(documentInformation);
                    load.save(new File(pdfPath));
                    isSaved = true;
                    MediaScannerConnection.scanFile(mContext, new String[]{pdfPath}, new String[]{"application/pdf"}, null);
                } else {
                    runOnUiThread(() -> showFileProtectedDialog());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        public void onPostExecute(Void r3) {
            super.onPostExecute(r3);
            progressDialog.dismiss();
            if (isSaved) {
                Toast.makeText(mContext, R.string.saved, Toast.LENGTH_LONG).show();
            }
        }
    }
}
