package pdfreader.pdfviewer.pdfscanner.documentreader.officetool.data;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.utils.DataUpdatedEvent;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.R;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.GetSet.BookmarkData;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.GetSet.PdfDataType;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.utils.Utils;
import com.itextpdf.text.html.HtmlTags;
import com.itextpdf.text.xml.xmp.PdfSchema;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "pdf_history.db";
    private static final int DATABASE_VERSION = 2;
    public static final String SORT_BY = "prefs_sort_by";
    public static final String SORT_ORDER = "prefs_sort_order";
    private static DbHelper sInstance;
    private final String SQL_CREATE_BOOKMARK = "CREATE TABLE IF NOT EXISTS bookmarks ( _id INTEGER PRIMARY KEY AUTOINCREMENT, tvMenuToolTitle TEXT, path TEXT, page_number INTEGER UNIQUE, created_at DATETIME DEFAULT (DATETIME('now','localtime')))";
    private final String SQL_CREATE_HISTORY_PDFS_TABLE = "CREATE TABLE IF NOT EXISTS history_pdfs ( _id INTEGER PRIMARY KEY AUTOINCREMENT, path TEXT UNIQUE, last_accessed_at DATETIME DEFAULT (DATETIME('now','localtime')))";
    private final String SQL_CREATE_LAST_OPENED_PAGE = "CREATE TABLE IF NOT EXISTS last_opened_page ( _id INTEGER PRIMARY KEY AUTOINCREMENT, path TEXT UNIQUE, page_number INTEGER)";
    private final String SQL_CREATE_STARED_PDFS_TABLE = "CREATE TABLE IF NOT EXISTS stared_pdfs ( _id INTEGER PRIMARY KEY AUTOINCREMENT, path TEXT UNIQUE, created_at DATETIME DEFAULT (DATETIME('now','localtime')))";
    private final String TAG = DbHelper.class.getSimpleName();
    private String THUMBNAILS_DIR;
    public Context context;
    private SQLiteDatabase mDatabase;
    private int mOpenCounter;

    public DbHelper(Context context2) {
        super(context2, DATABASE_NAME, (SQLiteDatabase.CursorFactory) null, 2);
        this.context = context2;
        this.THUMBNAILS_DIR = context2.getCacheDir() + "/Thumbnails/";
    }

    public static synchronized DbHelper getInstance(Context context2) {
        DbHelper dbHelper;
        synchronized (DbHelper.class) {
            synchronized (DbHelper.class) {
                if (sInstance == null) {
                    sInstance = new DbHelper(context2.getApplicationContext());
                }
                dbHelper = sInstance;
            }
            return dbHelper;
        }
    }

    public synchronized SQLiteDatabase getReadableDb() {
        int i = this.mOpenCounter + 1;
        this.mOpenCounter = i;
        if (i == 1) {
            this.mDatabase = getWritableDatabase();
        }
        return this.mDatabase;
    }

    public synchronized void closeDb() {
        int i = this.mOpenCounter - 1;
        this.mOpenCounter = i;
        if (i == 0) {
            this.mDatabase.close();
        }
    }

    public void onCreate(SQLiteDatabase sQLiteDatabase) {
        sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS history_pdfs ( _id INTEGER PRIMARY KEY AUTOINCREMENT, path TEXT UNIQUE, last_accessed_at DATETIME DEFAULT (DATETIME('now','localtime')))");
        sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS stared_pdfs ( _id INTEGER PRIMARY KEY AUTOINCREMENT, path TEXT UNIQUE, created_at DATETIME DEFAULT (DATETIME('now','localtime')))");
        sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS last_opened_page ( _id INTEGER PRIMARY KEY AUTOINCREMENT, path TEXT UNIQUE, page_number INTEGER)");
        sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS bookmarks ( _id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, path TEXT, page_number INTEGER UNIQUE, created_at DATETIME DEFAULT (DATETIME('now','localtime')))");
    }

    public void onUpgrade(SQLiteDatabase sQLiteDatabase, int i, int i2) {
        if (i == 1) {
            sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS last_opened_page ( _id INTEGER PRIMARY KEY AUTOINCREMENT, path TEXT UNIQUE, page_number INTEGER)");
            sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS bookmarks ( _id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, path TEXT, page_number INTEGER UNIQUE, created_at DATETIME DEFAULT (DATETIME('now','localtime')))");
        }
    }

    public List<PdfDataType> getAllPdfFromDirectory(String str) {
        String string = PreferenceManager.getDefaultSharedPreferences(this.context).getString(SORT_BY, "name");
        ArrayList arrayList = new ArrayList();
        try {
            ContentResolver contentResolver = this.context.getContentResolver();
            Uri contentUri = MediaStore.Files.getContentUri("external");
            char c = 65535;
            int hashCode = string.hashCode();
            if (hashCode != 3373707) {
                if (hashCode != 3530753) {
                    if (hashCode == 1375123195 && string.equals("date modified")) {
                        c = 0;
                    }
                } else if (string.equals(HtmlTags.SIZE)) {
                    c = 1;
                }
            } else if (string.equals("name")) {
                c = 2;
            }
            String str2 = c != 0 ? c != 1 ? "tvMenuToolTitle  COLLATE NOCASE ASC" : "_size  COLLATE NOCASE ASC" : "date_modified  COLLATE NOCASE ASC";
            Cursor query = contentResolver.query(contentUri, new String[]{"_data"}, "mime_type=? AND _data LIKE ?", new String[]{MimeTypeMap.getSingleton().getMimeTypeFromExtension(PdfSchema.DEFAULT_XPATH_ID), "%" + str + "%"}, str2);
            if (query != null && query.moveToFirst()) {
                do {
                    File file = new File(query.getString(query.getColumnIndex("_data")));
                    if (file.length() != 0) {
                        Uri imageUriFromPath = Utils.getImageUriFromPath(this.THUMBNAILS_DIR + Utils.removePdfExtension(file.getName()) + ".jpg");
                        PdfDataType pdfDataType = new PdfDataType();
                        pdfDataType.setName(file.getName());
                        pdfDataType.setAbsolutePath(file.getAbsolutePath());
                        pdfDataType.setPdfUri(Uri.fromFile(file));
                        pdfDataType.setLength(Long.valueOf(file.length()));
                        pdfDataType.setLastModified(Long.valueOf(file.lastModified()));
                        pdfDataType.setThumbUri(imageUriFromPath);
                        pdfDataType.setStarred(isStared(file.getAbsolutePath()));
                        arrayList.add(pdfDataType);
                    }
                } while (query.moveToNext());
                query.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        String str3 = this.TAG;
        Log.d(str3, "no of files in db " + arrayList.size());
        return arrayList;
    }

    public List<PdfDataType> getAllPdfs() {
        String str;
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.context);
        String string = defaultSharedPreferences.getString(SORT_BY, "name");
        String string2 = defaultSharedPreferences.getString(SORT_ORDER, "ascending");
        ArrayList arrayList = new ArrayList();
        ContentResolver contentResolver = this.context.getContentResolver();
        Uri contentUri = MediaStore.Files.getContentUri("external");
        String str2 = TextUtils.equals(string2, "descending") ? "DESC" : "ASC";
        char c = 65535;
        int hashCode = string.hashCode();
        if (hashCode != 3373707) {
            if (hashCode != 3530753) {
                if (hashCode == 1375123195 && string.equals("date modified")) {
                    c = 0;
                }
            } else if (string.equals(HtmlTags.SIZE)) {
                c = 1;
            }
        } else if (string.equals("name")) {
            c = 2;
        }
        if (c == 0) {
            str = "date_modified  COLLATE NOCASE " + str2;
        } else if (c != 1) {
            str = "title  COLLATE NOCASE " + str2;
        } else {
            str = "_size  COLLATE NOCASE " + str2;
        }
        try {
            Cursor query = contentResolver.query(contentUri, new String[]{"_data"}, "mime_type=?", new String[]{MimeTypeMap.getSingleton().getMimeTypeFromExtension(PdfSchema.DEFAULT_XPATH_ID)}, str);
            if (query != null && query.moveToFirst()) {
                do {
                    File file = new File(query.getString(query.getColumnIndex("_data")));
                    if (file.length() != 0) {
                        Uri imageUriFromPath = Utils.getImageUriFromPath(this.THUMBNAILS_DIR + Utils.removePdfExtension(file.getName()) + ".jpg");
                        PdfDataType pdfDataType = new PdfDataType();
                        pdfDataType.setName(file.getName());
                        pdfDataType.setAbsolutePath(file.getAbsolutePath());
                        pdfDataType.setPdfUri(Uri.fromFile(file));
                        pdfDataType.setLength(Long.valueOf(file.length()));
                        pdfDataType.setLastModified(Long.valueOf(file.lastModified()));
                        pdfDataType.setThumbUri(imageUriFromPath);
                        pdfDataType.setStarred(isStared(file.getAbsolutePath()));
                        arrayList.add(pdfDataType);
                    }
                } while (query.moveToNext());
                query.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return arrayList;
    }

    public void addRecentPDF(String str) {
        SQLiteDatabase readableDb = getReadableDb();
        ContentValues contentValues = new ContentValues();
        contentValues.put("path", str);
        readableDb.replace(DbContract.RecentPDFEntry.TABLE_NAME, null, contentValues);
        closeDb();
    }

    public List<PdfDataType> getRecentPDFs() {
        ArrayList arrayList = new ArrayList();
        SQLiteDatabase readableDb = getReadableDb();
        Cursor rawQuery = readableDb.rawQuery("SELECT * FROM history_pdfs ORDER BY last_accessed_at DESC", null);
        if (rawQuery.moveToFirst()) {
            do {
                String string = rawQuery.getString(rawQuery.getColumnIndex("path"));
                File file = new File(string);
                if (file.exists()) {
                    Uri imageUriFromPath = Utils.getImageUriFromPath(this.THUMBNAILS_DIR + Utils.removePdfExtension(file.getName()) + ".jpg");
                    PdfDataType pdfDataType = new PdfDataType();
                    pdfDataType.setName(file.getName());
                    pdfDataType.setAbsolutePath(file.getAbsolutePath());
                    pdfDataType.setPdfUri(Uri.fromFile(file));
                    pdfDataType.setLength(Long.valueOf(file.length()));
                    pdfDataType.setLastModified(Long.valueOf(file.lastModified()));
                    pdfDataType.setThumbUri(imageUriFromPath);
                    pdfDataType.setStarred(isStared(readableDb, file.getAbsolutePath()));
                    arrayList.add(pdfDataType);
                } else {
                    deleteRecentPDF(string);
                }
            } while (rawQuery.moveToNext());
        }
        rawQuery.close();
        closeDb();
        return arrayList;
    }

    public void deleteRecentPDF(String str) {
        getReadableDb().delete(DbContract.RecentPDFEntry.TABLE_NAME, "path =?", new String[]{str});
        closeDb();
        EventBus.getDefault().post(new DataUpdatedEvent.RecentPdfDeleteEvent());
    }

    public void updateHistory(String str, String str2) {
        try {
            SQLiteDatabase readableDb = getReadableDb();
            ContentValues contentValues = new ContentValues();
            contentValues.put("path", str2);
            readableDb.update(DbContract.RecentPDFEntry.TABLE_NAME, contentValues, "path=?", new String[]{str});
            closeDb();
        } catch (Exception e) {
            Toast.makeText(this.context, (int) R.string.failed, 1).show();
            e.printStackTrace();
        }
    }

    public void updateStarred(String str, String str2) {
        SQLiteDatabase readableDb = getReadableDb();
        ContentValues contentValues = new ContentValues();
        contentValues.put("path", str2);
        readableDb.update(DbContract.StarredPDFEntry.TABLE_NAME, contentValues, "path=?", new String[]{str});
        closeDb();
    }

    public void clearRecentPDFs() {
        getReadableDb().delete(DbContract.RecentPDFEntry.TABLE_NAME, null, null);
        closeDb();
        EventBus.getDefault().post(new DataUpdatedEvent.RecentPdfClearEvent());
    }

    public List<PdfDataType> getStarredPdfs() {
        ArrayList arrayList = new ArrayList();
        Cursor rawQuery = getReadableDb().rawQuery("SELECT * FROM stared_pdfs ORDER BY created_at DESC", null);
        if (rawQuery.moveToFirst()) {
            do {
                String string = rawQuery.getString(rawQuery.getColumnIndex("path"));
                File file = new File(string);
                if (file.exists()) {
                    Uri imageUriFromPath = Utils.getImageUriFromPath(this.THUMBNAILS_DIR + Utils.removePdfExtension(file.getName()) + ".jpg");
                    PdfDataType pdfDataType = new PdfDataType();
                    pdfDataType.setName(file.getName());
                    pdfDataType.setAbsolutePath(file.getAbsolutePath());
                    pdfDataType.setPdfUri(Uri.fromFile(file));
                    pdfDataType.setLength(Long.valueOf(file.length()));
                    pdfDataType.setLastModified(Long.valueOf(file.lastModified()));
                    pdfDataType.setThumbUri(imageUriFromPath);
                    pdfDataType.setStarred(true);
                    arrayList.add(pdfDataType);
                } else {
                    removeStaredPDF(string);
                }
            } while (rawQuery.moveToNext());
        }
        rawQuery.close();
        closeDb();
        return arrayList;
    }

    public void addStaredPDF(String str) {
        SQLiteDatabase readableDb = getReadableDb();
        ContentValues contentValues = new ContentValues();
        contentValues.put("path", str);
        readableDb.replace(DbContract.StarredPDFEntry.TABLE_NAME, null, contentValues);
        closeDb();
    }

    public void removeStaredPDF(String str) {
        getReadableDb().delete(DbContract.StarredPDFEntry.TABLE_NAME, "path =?", new String[]{str});
        closeDb();
    }

    public void updateStaredPDF(String str, String str2) {
        try {
            SQLiteDatabase readableDb = getReadableDb();
            ContentValues contentValues = new ContentValues();
            contentValues.put("path", str2);
            readableDb.update(DbContract.StarredPDFEntry.TABLE_NAME, contentValues, "path =?", new String[]{str});
            closeDb();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isStared(String str) {
        Cursor query = getReadableDb().query(DbContract.StarredPDFEntry.TABLE_NAME, new String[]{"path"}, "path =?", new String[]{str}, null, null, null);
        Boolean valueOf = Boolean.valueOf(query.moveToFirst());
        query.close();
        closeDb();
        return valueOf.booleanValue();
    }

    public boolean isStared(SQLiteDatabase sQLiteDatabase, String str) {
        Cursor query = sQLiteDatabase.query(DbContract.StarredPDFEntry.TABLE_NAME, new String[]{"path"}, "path =?", new String[]{str}, null, null, null);
        Boolean valueOf = Boolean.valueOf(query.moveToFirst());
        query.close();
        return valueOf.booleanValue();
    }

    public List<Uri> getAllImages(String str) {
        ArrayList arrayList = new ArrayList();
        try {
            Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            Cursor query = this.context.getContentResolver().query(uri, null, "_data LIKE ? AND mime_type LIKE ? ", new String[]{"%" + str + "%", "%image/%"}, null);
            if (query != null && query.moveToFirst()) {
                do {
                    String string = query.getString(query.getColumnIndex("_data"));
                    Log.d(this.TAG, string);
                    arrayList.add(Uri.fromFile(new File(string)));
                } while (query.moveToNext());
                query.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return arrayList;
    }

    public int getLastOpenedPage(String str) {
        try {
            Cursor query = getReadableDb().query(DbContract.LastOpenedPageEntry.TABLE_NAME, new String[]{"page_number"}, "path = ? ", new String[]{str}, null, null, null);
            if (query != null) {
                if (query.moveToFirst()) {
                    int i = query.getInt(query.getColumnIndex("page_number"));
                    try {
                        query.close();
                    } catch (Exception unused) {
                    }
                    closeDb();
                    return i;
                }
            }
            closeDb();
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            closeDb();
            return 0;
        }
    }

    public void addLastOpenedPage(String str, int i) {
        try {
            SQLiteDatabase readableDb = getReadableDb();
            ContentValues contentValues = new ContentValues();
            contentValues.put("path", str);
            contentValues.put("page_number", Integer.valueOf(i));
            readableDb.replace(DbContract.LastOpenedPageEntry.TABLE_NAME, null, contentValues);
            closeDb();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateLastOpenedPagePath(String str, String str2) {
        try {
            SQLiteDatabase readableDb = getReadableDb();
            ContentValues contentValues = new ContentValues();
            contentValues.put("path", str2);
            readableDb.update(DbContract.LastOpenedPageEntry.TABLE_NAME, contentValues, "path =?", new String[]{str});
            closeDb();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addBookmark(String str, String str2, int i) {
        try {
            SQLiteDatabase readableDb = getReadableDb();
            ContentValues contentValues = new ContentValues();
            contentValues.put("path", str);
            contentValues.put(DbContract.BookmarkEntry.COLUMN_TITLE, str2);
            contentValues.put("page_number", Integer.valueOf(i));
            readableDb.replace(DbContract.BookmarkEntry.TABLE_NAME, null, contentValues);
            closeDb();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<BookmarkData> getBookmarks(String str) {
        ArrayList arrayList = new ArrayList();
        try {
            Cursor query = getReadableDb().query(DbContract.BookmarkEntry.TABLE_NAME, new String[]{DbContract.BookmarkEntry.COLUMN_TITLE, "path", "page_number"}, "path = ? ", new String[]{str}, null, null, "created_at DESC");
            if (query != null && query.moveToFirst()) {
                do {
                    BookmarkData bookmarkData = new BookmarkData();
                    bookmarkData.setTitle(query.getString(query.getColumnIndex(DbContract.BookmarkEntry.COLUMN_TITLE)));
                    bookmarkData.setPageNumber(query.getInt(query.getColumnIndex("page_number")));
                    bookmarkData.setPath(query.getString(query.getColumnIndex("path")));
                    arrayList.add(bookmarkData);
                } while (query.moveToNext());
                query.close();
                closeDb();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return arrayList;
    }

    public void updateBookmarkPath(String str, String str2) {
        try {
            SQLiteDatabase readableDb = getReadableDb();
            ContentValues contentValues = new ContentValues();
            contentValues.put("path", str2);
            readableDb.update(DbContract.BookmarkEntry.TABLE_NAME, contentValues, "path =?", new String[]{str});
            closeDb();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteBookmarks(List<BookmarkData> list) {
        try {
            SQLiteDatabase readableDb = getReadableDb();
            int size = list.size();
            readableDb.beginTransaction();
            for (int i = 0; i < size; i++) {
                readableDb.delete(DbContract.BookmarkEntry.TABLE_NAME, "path = ? AND page_number = ? ", new String[]{list.get(i).getPath(), String.valueOf(list.get(i).getPageNumber())});
            }
            readableDb.setTransactionSuccessful();
            readableDb.endTransaction();
            closeDb();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
