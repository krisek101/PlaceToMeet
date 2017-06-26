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
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.brgk.placetomeet.R;
import com.brgk.placetomeet.activities.MapActivity;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class UsefulFunctions {

    public static String getAddressFromLatLng(Context context, LatLng position) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        List<Address> addressArray = new ArrayList<>();
        String addressBuilder = null;
        int failed = 0;
        while (addressBuilder == null && failed < 2) {
            addressBuilder = "";
            try {
                addressArray = geocoder.getFromLocation(position.latitude, position.longitude, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (addressArray != null) {
                if(!addressArray.isEmpty()) {
                    if (addressArray.get(0) != null) {
                        addressBuilder += addressArray.get(0).getAddressLine(0);
                        if (addressArray.get(0).getLocality() != null) {
                            addressBuilder += ", " + addressArray.get(0).getLocality();
                        }
                    }
                }
            } else {
                addressBuilder = null;
                failed++;
            }
        }
        if (failed >= 2) {
            return "Adres nieznany";
        } else {
            return addressBuilder;
        }
    }

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

}