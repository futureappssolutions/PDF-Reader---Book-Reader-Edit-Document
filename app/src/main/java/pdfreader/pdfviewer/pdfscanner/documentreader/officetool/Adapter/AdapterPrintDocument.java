package pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Adapter;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentInfo;

import java.io.FileOutputStream;
import java.io.InputStream;

public class AdapterPrintDocument extends android.print.PrintDocumentAdapter {
    Context context;
    String fileName = "AllPDF";
    private Uri uriFileToPrint;

    public AdapterPrintDocument(Context context2, Uri uri) {
        this.uriFileToPrint = uri;
        this.context = context2;
    }

    public void onWrite(PageRange[] pageRangeArr, ParcelFileDescriptor parcelFileDescriptor, CancellationSignal cancellationSignal, AdapterPrintDocument.WriteResultCallback writeResultCallback) {
        try {
            InputStream openInputStream = this.context.getContentResolver().openInputStream(this.uriFileToPrint);
            FileOutputStream fileOutputStream = new FileOutputStream(parcelFileDescriptor.getFileDescriptor());
            byte[] bArr = new byte[1024];
            while (true) {
                int read = openInputStream.read(bArr);
                if (read > 0) {
                    fileOutputStream.write(bArr, 0, read);
                } else {
                    writeResultCallback.onWriteFinished(new PageRange[]{PageRange.ALL_PAGES});
                    openInputStream.close();
                    fileOutputStream.close();
                    return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onLayout(PrintAttributes printAttributes, PrintAttributes printAttributes2, CancellationSignal cancellationSignal, AdapterPrintDocument.LayoutResultCallback layoutResultCallback, Bundle bundle) {
        if (cancellationSignal.isCanceled()) {
            layoutResultCallback.onLayoutCancelled();
        } else {
            layoutResultCallback.onLayoutFinished(new PrintDocumentInfo.Builder(this.fileName).setContentType(0).build(), true);
        }
    }
}
