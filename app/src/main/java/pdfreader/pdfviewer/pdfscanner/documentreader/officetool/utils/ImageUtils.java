package pdfreader.pdfviewer.pdfscanner.documentreader.officetool.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

import java.io.ByteArrayOutputStream;

public class ImageUtils {
    public static ImageUtils imageUtils;

    public static ImageUtils getInstant() {
        if (imageUtils == null) {
            imageUtils = new ImageUtils();
        }
        return imageUtils;
    }

    public Bitmap getPdfCompressedBitmap(String str) {
        Bitmap bitmap;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap decodeFile = BitmapFactory.decodeFile(str, options);
        int i = options.outHeight;
        int i2 = options.outWidth;
        float f = (float) i2;
        float f2 = (float) i;
        float f3 = f / f2;
        if (f2 > 1920.0f || f > 1080.0f) {
            if (f3 < 0.5625f) {
                i2 = (int) ((1920.0f / f2) * f);
                i = 1920;
            } else {
                i = f3 > 0.5625f ? (int) ((1080.0f / f) * f2) : 1920;
                i2 = 1080;
            }
        }
        options.inSampleSize = calculatePdfSampleSize(options, i2, i);
        options.inJustDecodeBounds = false;
        options.inDither = false;
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inTempStorage = new byte[16384];
        try {
            decodeFile = BitmapFactory.decodeFile(str, options);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }
        try {
            bitmap = Bitmap.createBitmap(i2, i, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError e2) {
            e2.printStackTrace();
            bitmap = null;
        }
        float f4 = (float) i2;
        float f5 = f4 / ((float) options.outWidth);
        float f6 = (float) i;
        float f7 = f6 / ((float) options.outHeight);
        float f8 = f4 / 2.0f;
        float f9 = f6 / 2.0f;
        Matrix matrix = new Matrix();
        matrix.setScale(f5, f7, f8, f9);
        Canvas canvas = new Canvas(bitmap);
        canvas.setMatrix(matrix);
        canvas.drawBitmap(decodeFile, f8 - ((float) (decodeFile.getWidth() / 2)), f9 - ((float) (decodeFile.getHeight() / 2)), new Paint(2));
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
    }

    public Bitmap getPdfCompressedBitmap(byte[] bArr) {
        Bitmap bitmap;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap decodeByteArray = BitmapFactory.decodeByteArray(bArr, 0, bArr.length, options);
        int i = options.outHeight;
        int i2 = options.outWidth;
        float f = (float) i2;
        float f2 = (float) i;
        float f3 = f / f2;
        if (f2 > 1920.0f || f > 1080.0f) {
            if (f3 < 0.5625f) {
                i2 = (int) ((1920.0f / f2) * f);
                i = 1920;
            } else {
                i = f3 > 0.5625f ? (int) ((1080.0f / f) * f2) : 1920;
                i2 = 1080;
            }
        }
        options.inSampleSize = calculatePdfSampleSize(options, i2, i);
        options.inJustDecodeBounds = false;
        options.inDither = false;
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inTempStorage = new byte[16384];
        try {
            decodeByteArray = BitmapFactory.decodeByteArray(bArr, 0, bArr.length, options);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }
        try {
            bitmap = Bitmap.createBitmap(i2, i, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError e2) {
            e2.printStackTrace();
            bitmap = null;
        }
        float f4 = (float) i2;
        float f5 = f4 / ((float) options.outWidth);
        float f6 = (float) i;
        float f7 = f6 / ((float) options.outHeight);
        float f8 = f4 / 2.0f;
        float f9 = f6 / 2.0f;
        Matrix matrix = new Matrix();
        matrix.setScale(f5, f7, f8, f9);
        Canvas canvas = new Canvas(bitmap);
        canvas.setMatrix(matrix);
        canvas.drawBitmap(decodeByteArray, f8 - ((float) (decodeByteArray.getWidth() / 2)), f9 - ((float) (decodeByteArray.getHeight() / 2)), new Paint(2));
        return bitmap;
    }

    private int calculatePdfSampleSize(BitmapFactory.Options options, int i, int i2) {
        int i3;
        int i4 = options.outHeight;
        int i5 = options.outWidth;
        if (i4 > i2 || i5 > i) {
            i3 = Math.round(((float) i4) / ((float) i2));
            int round = Math.round(((float) i5) / ((float) i));
            if (i3 >= round) {
                i3 = round;
            }
        } else {
            i3 = 1;
        }
        while (((float) (i5 * i4)) / ((float) (i3 * i3)) > ((float) (i * i2 * 2))) {
            i3++;
        }
        return i3;
    }
}
