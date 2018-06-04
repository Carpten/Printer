package com.ysq.printer;

import android.graphics.Bitmap;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;


/**
 * @author: yangshuiqiang
 * Time:2017/12/5 14:01
 */

public class BarUtils {

    public static Bitmap encodeAsBitmap(String contents, BarcodeFormat format, int desiredWidth, int desiredHeight) {
        try {
            final int WHITE = 0xFFFFFFFF;
            final int BLACK = 0xFF000000;
            MultiFormatWriter writer = new MultiFormatWriter();
//            Hashtable<EncodeHintType, Object> hints = new Hashtable<>();
//            hints.put(EncodeHintType.CHARACTER_SET, "utf-8"); //编码
//            hints.put(EncodeHintType.ERROR_CORRECTION, level); //容错率
//            hints.put(EncodeHintType.MARGIN, 0);  //二维码边框宽度，这里文档说设置0-4，但是设置后没有效果，不知原因，

            BitMatrix result = writer.encode(contents, format, desiredWidth, desiredHeight, null);
            int width = result.getWidth();
            int height = result.getHeight();
            int[] pixels = new int[width * height];
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
}
