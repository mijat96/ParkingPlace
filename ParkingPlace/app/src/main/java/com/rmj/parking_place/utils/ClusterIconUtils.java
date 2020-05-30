package com.rmj.parking_place.utils;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import androidx.collection.LruCache;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.rmj.parking_place.App;
import com.rmj.parking_place.R;

public class ClusterIconUtils {
    private static LruCache<KeyForIconsCache, BitmapDescriptor> cache;
    private static Paint paint;
    private static Bitmap bitmap;
    static {
        cache = new LruCache<KeyForIconsCache, BitmapDescriptor>(128);

        Resources resources = App.getAppContext().getResources();

        bitmap = BitmapFactory.decodeResource(resources, R.drawable.cluster_48);

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.WHITE);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(resources.getDimension(R.dimen.text_size_cluster));
    }

    public static BitmapDescriptor makeIcon(int totalNumber, int numberOfEmpties) {
        KeyForIconsCache keyForIconsCache = new KeyForIconsCache(totalNumber, numberOfEmpties);
        BitmapDescriptor cachedIcon = cache.get(keyForIconsCache);
        if (cachedIcon != null) {
            return cachedIcon;
        }

        String textEmptyParkings = String.valueOf(numberOfEmpties);
        String textAllParkings = String.valueOf(totalNumber);
        String text = textAllParkings + " " + textEmptyParkings;

        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);

        Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        float x = mutableBitmap.getWidth() / 2.0f;
        float y = (mutableBitmap.getHeight() - bounds.height()) / 2.0f - bounds.top;

        Canvas canvas = new Canvas(mutableBitmap);
        canvas.drawText(text, x, y, paint);
        BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(mutableBitmap);

        cache.put(keyForIconsCache, icon);

        return icon;
    }
}
