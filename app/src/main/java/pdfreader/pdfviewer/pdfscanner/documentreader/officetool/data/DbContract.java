package pdfreader.pdfviewer.pdfscanner.documentreader.officetool.data;

import android.provider.BaseColumns;

public class DbContract {

    public static class BookmarkEntry implements BaseColumns {
        public static final String COLUMN_CREATED_AT = "created_at";
        public static final String COLUMN_PAGE_NUMBER = "page_number";
        public static final String COLUMN_PATH = "path";
        public static final String COLUMN_TITLE = "tvMenuToolTitle";
        public static final String TABLE_NAME = "bookmarks";
    }

    public static class LastOpenedPageEntry implements BaseColumns {
        public static final String COLUMN_PAGE_NUMBER = "page_number";
        public static final String COLUMN_PATH = "path";
        public static final String TABLE_NAME = "last_opened_page";
    }

    public static class RecentPDFEntry implements BaseColumns {
        public static final String COLUMN_LAST_ACCESSED_AT = "last_accessed_at";
        public static final String COLUMN_PATH = "path";
        public static final String TABLE_NAME = "history_pdfs";
    }

    public static class StarredPDFEntry implements BaseColumns {
        public static final String COLUMN_CREATED_AT = "created_at";
        public static final String COLUMN_PATH = "path";
        public static final String TABLE_NAME = "stared_pdfs";
    }
}
