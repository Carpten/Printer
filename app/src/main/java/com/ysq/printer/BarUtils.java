package com.ysq.printer;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;


/**
 * @author: yangshuiqiang
 * Time:2017/12/5 14:01
 */

public class BarUtils {

    public static Bitmap encodeAsBitmap(String contents, BarcodeFormat format
            , int desiredWidth, int desiredHeight) {
        try {
            final int WHITE = 0xFFFFFFFF;
            final int BLACK = 0xFF000000;
            MultiFormatWriter writer = new MultiFormatWriter();
            BitMatrix result = writer.encode(contents, format, desiredWidth,
                    desiredHeight, null);
            int width = result.getWidth();
            int height = result.getHeight();
            int[] pixels = new int[width * height];
            // All are 0, or black, by default
            for (int y = 0; y < height; y++) {
                int offset = y * width;
                for (int x = 0; x < width; x++) {
                    pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
                }
            }

            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
            return bitmap;
        } catch (WriterException e) {
            return null;
        }
    }

    public static Bitmap encodeAsBitmapOffset(String contents, BarcodeFormat format
            , int desiredWidth, int desiredHeight, int offsetX) {
        try {
            final int WHITE = 0xFFFFFFFF;
            final int BLACK = 0xFF000000;
            MultiFormatWriter writer = new MultiFormatWriter();
            BitMatrix result = writer.encode(contents, format, desiredWidth,
                    desiredHeight, null);


            int width = result.getWidth();
            int height = result.getHeight();
            Bitmap bitmap = Bitmap.createBitmap(width + offsetX, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            canvas.drawColor(0xffffffff);
            int[] pixels = new int[width * height];
            // All are 0, or black, by default
            for (int y = 0; y < height; y++) {
                int offset = y * width;
                for (int x = 0; x < width; x++) {
                    pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
                }
            }

            bitmap.setPixels(pixels, 0, width, offsetX, 0, width, height);
            return bitmap;
        } catch (WriterException e) {
            return null;
        }
    }
}
