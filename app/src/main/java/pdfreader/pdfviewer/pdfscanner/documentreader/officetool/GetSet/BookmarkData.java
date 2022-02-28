package pdfreader.pdfviewer.pdfscanner.documentreader.officetool.GetSet;

public class BookmarkData {
    private int pageNumber;
    private String path;
    private String title;

    public String getPath() {
        return this.path;
    }

    public void setPath(String str) {
        this.path = str;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String str) {
        this.title = str;
    }

    public int getPageNumber() {
        return this.pageNumber;
    }

    public void setPageNumber(int i) {
        this.pageNumber = i;
    }
}
