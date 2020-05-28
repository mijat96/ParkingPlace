package com.rmj.parking_place.listener;

import com.rmj.parking_place.fragments.MapFragment;

public class OnCreateViewFinishedListenerImplementation implements OnCreateViewFinishedListener {

    private MapFragment mapFragment;


    public OnCreateViewFinishedListenerImplementation(MapFragment mapFragment) {
        this.mapFragment = mapFragment;
    }

    @Override
    public void OnCreateViewFinished() {
        mapFragment.changePositionOfGoogleLogo(false);
        mapFragment.changePositionOfMyLocationButton(false);
    }
}
