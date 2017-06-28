package com.brgk.placetomeet.contants;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.brgk.placetomeet.R;

public class UsefulFunctions {

    public static Bitmap buildMarkerIcon(Resources res, Bitmap bmp) {
        final int size = 512;
        int radius = 192;
        Bitmap scaled = Bitmap.createScaledBitmap(bmp, 2 * radius, 2 * radius, false);
        Bitmap result = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);

        int color = 0xffaa0000;
        Paint paint = new Paint();
        Rect srcRect = new Rect(0, 0, 2 * radius, 2 * radius);
        Rect dstRect = new Rect(64, 20, size - 64, 20 + 2 * radius);

        paint.setAntiAlias(true);
        paint.setColor(color);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawCircle(size / 2, radius + 20, radius, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(scaled, srcRect, dstRect, paint);
        Bitmap cover = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.marker_cover), size, size, false);
        canvas.drawBitmap(cover, 0, 0, null);

        result = Bitmap.createScaledBitmap(result, 128, 128, false);
        return result;
    }

    public static boolean isOnline(Context c) {
        ConnectivityManager cm =
                (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}