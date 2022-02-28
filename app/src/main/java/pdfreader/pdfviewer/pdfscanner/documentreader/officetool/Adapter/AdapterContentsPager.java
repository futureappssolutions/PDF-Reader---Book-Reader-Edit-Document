package pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Fragments.FragmentBookmarks;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Fragments.FragmentTableContents;

public class AdapterContentsPager extends FragmentPagerAdapter {
    String pdfPath;

    @Override
    public int getCount() {
        return 2;
    }

    public AdapterContentsPager(FragmentManager fragmentManager, String str) {
        super(fragmentManager);
        this.pdfPath = str;
    }

    @NonNull
    @Override
    public Fragment getItem(int i) {
        if (i == 0) {
            return FragmentTableContents.newInstance(this.pdfPath);
        }
        if (i != 1) {
            return null;
        }
        return FragmentBookmarks.newInstance(this.pdfPath);
    }
}
