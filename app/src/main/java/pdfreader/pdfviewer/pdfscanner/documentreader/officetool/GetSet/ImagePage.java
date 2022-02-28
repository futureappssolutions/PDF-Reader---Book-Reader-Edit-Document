package pdfreader.pdfviewer.pdfscanner.documentreader.officetool.GetSet;

import android.net.Uri;

public class ImagePage {
    private Uri imageUri;
    private int pageNumber;

    public ImagePage(int i, Uri uri) {
        this.pageNumber = i;
        this.imageUri = uri;
    }

    public int getPageNumber() {
        return this.pageNumber;
    }

    public void setPageNumber(int i) {
        this.pageNumber = i;
    }

    public Uri getImageUri() {
        return this.imageUri;
    }

    public void setImageUri(Uri uri) {
        this.imageUri = uri;
    }
}
