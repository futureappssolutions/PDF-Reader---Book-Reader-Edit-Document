package pdfreader.pdfviewer.pdfscanner.documentreader.officetool.GetSet;

import android.net.Uri;

public class PdfDataType {
    private String absolutePath;
    private String createdAt;
    private boolean isDirectory;
    private boolean isStarred;
    private Long lastModified;
    private Long length;
    private String name;
    private int numItems;
    private Uri pdfUri;
    private Uri thumbUri;

    public PdfDataType() {
    }

    public PdfDataType(String str, Long l, String str2) {
        this.name = str;
        this.length = l;
        this.createdAt = str2;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String str) {
        this.name = str;
    }

    public Long getLength() {
        return this.length;
    }

    public void setLength(Long l) {
        this.length = l;
    }

    public String getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(String str) {
        this.createdAt = str;
    }

    public String getAbsolutePath() {
        return this.absolutePath;
    }

    public void setAbsolutePath(String str) {
        this.absolutePath = str;
    }

    public Long getLastModified() {
        return this.lastModified;
    }

    public void setLastModified(Long l) {
        this.lastModified = l;
    }

    public Uri getPdfUri() {
        return this.pdfUri;
    }

    public void setPdfUri(Uri uri) {
        this.pdfUri = uri;
    }

    public Uri getThumbUri() {
        return this.thumbUri;
    }

    public void setThumbUri(Uri uri) {
        this.thumbUri = uri;
    }

    public boolean isStarred() {
        return this.isStarred;
    }

    public void setStarred(boolean z) {
        this.isStarred = z;
    }

    public boolean isDirectory() {
        return this.isDirectory;
    }

    public void setDirectory(boolean z) {
        this.isDirectory = z;
    }

    public int getNumItems() {
        return this.numItems;
    }

    public void setNumItems(int i) {
        this.numItems = i;
    }
}
