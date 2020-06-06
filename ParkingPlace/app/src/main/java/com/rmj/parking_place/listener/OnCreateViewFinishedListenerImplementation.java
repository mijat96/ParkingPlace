package com.rmj.parking_place.listener;

import android.content.res.Configuration;

import com.rmj.parking_place.fragments.MapFragment;
import com.rmj.parking_place.fragments.MapPageFragment;

public class OnCreateViewFinishedListenerImplementation implements OnCreateViewFinishedListener {

    private MapFragment mapFragment;
    private MapPageFragment mapPageFragment;

    public OnCreateViewFinishedListenerImplementation(MapPageFragment mapPageFragment, MapFragment mapFragment) {
        this.mapFragment = mapFragment;
        this.mapPageFragment = mapPageFragment;
    }

    @Override
    public void OnCreateViewFinished() {
        int orientation = mapFragment.getResources().getConfiguration().orientation;
        /*boolean bottom, orientationPortrait;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            bottom = false;
            orientationPortrait = true;
        } else {
            // landscape mode
            bottom = true;
            orientationPortrait = false;
        }*/

        boolean showOnStartPosition = !mapPageFragment.isFindParkingPlaceFragmentVisible();
        boolean bottom = showOnStartPosition;

        mapFragment.changePaddingOfGoogleMap(bottom);

        boolean orientationPortrait = orientation == Configuration.ORIENTATION_PORTRAIT;

        if (orientationPortrait && !showOnStartPosition) {
            mapFragment.resetMarginsOfMyLocationButton();
        }
        else {
            mapFragment.changeMarginsOfMyLocationButton(showOnStartPosition, orientationPortrait);
        }
    }

    public void setMapFragment(MapFragment mapFragment) {
        this.mapFragment = mapFragment;
    }

    public void setMapPageFragment(MapPageFragment mapPageFragment) {
        this.mapPageFragment = mapPageFragment;
    }
}
