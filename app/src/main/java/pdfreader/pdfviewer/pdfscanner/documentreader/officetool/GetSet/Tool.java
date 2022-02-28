package pdfreader.pdfviewer.pdfscanner.documentreader.officetool.GetSet;

public class Tool {
    private int drawable;
    private int f4854id;
    private String title;

    public Tool(int i, String str, int i2) {
        this.f4854id = i;
        this.title = str;
        this.drawable = i2;
    }

    public int getId() {
        return this.f4854id;
    }

    public void setId(int i) {
        this.f4854id = i;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String str) {
        this.title = str;
    }

    public int getDrawable() {
        return this.drawable;
    }

    public void setDrawable(int i) {
        this.drawable = i;
    }
}
