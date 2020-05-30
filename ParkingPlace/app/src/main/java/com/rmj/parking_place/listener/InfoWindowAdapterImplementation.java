package com.rmj.parking_place.listener;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.androidmapsextensions.GoogleMap;
import com.androidmapsextensions.Marker;
import com.rmj.parking_place.fragments.MapFragment;

public class InfoWindowAdapterImplementation implements GoogleMap.InfoWindowAdapter {

    private MapFragment mapFragment;

    public InfoWindowAdapterImplementation(MapFragment mapFragment) {
        this.mapFragment = mapFragment;
    }

    @Override
    public View getInfoContents(Marker marker) {
        Context context = mapFragment.getActivity(); // getApplicationContext(); //or getActivity(), YourActivity.this, etc.

        LinearLayout info = new LinearLayout(context);
        info.setOrientation(LinearLayout.VERTICAL);

        TextView title = new TextView(context);
        title.setTextColor(Color.BLACK);
        title.setGravity(Gravity.CENTER);
        title.setTypeface(null, Typeface.BOLD);
        title.setText(marker.getTitle());

        TextView snippet = new TextView(context);
        snippet.setTextColor(Color.GRAY);
        snippet.setText(marker.getSnippet());

        info.addView(title);
        info.addView(snippet);

        return info;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    public void setMapFragment(MapFragment mapFragment) {
        this.mapFragment = mapFragment;
    }
}
