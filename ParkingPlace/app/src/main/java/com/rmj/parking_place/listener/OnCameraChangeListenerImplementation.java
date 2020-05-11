package com.rmj.parking_place.listener;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLngBounds;
import com.rmj.parking_place.actvities.MapActivity;
import com.rmj.parking_place.fragments.MapPageFragment;

public class OnCameraChangeListenerImplementation implements GoogleMap.OnCameraChangeListener {
    private MapPageFragment mapPageFragment;
    private GoogleMap map;

    private LatLngBounds currentCameraBounds;
    private long lastCallMs = Long.MIN_VALUE;

    private static int CAMERA_MOVE_REACT_THRESHOLD_MS = 500;

    public OnCameraChangeListenerImplementation(GoogleMap map, MapPageFragment mapPageFragment) {
        this.map = map;
        this.mapPageFragment = mapPageFragment;
    }


    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        LatLngBounds bounds = map.getProjection().getVisibleRegion().latLngBounds;
        // Check whether the camera changes report the same boundaries (?!), yes, it happens
        if (currentCameraBounds != null
                && currentCameraBounds.northeast.latitude == bounds.northeast.latitude
                && currentCameraBounds.northeast.longitude == bounds.northeast.longitude
                && currentCameraBounds.southwest.latitude == bounds.southwest.latitude
                && currentCameraBounds.southwest.longitude == bounds.southwest.longitude) {
            return;
        }

        final long snap = System.currentTimeMillis();
        if (lastCallMs + CAMERA_MOVE_REACT_THRESHOLD_MS > snap) {
            lastCallMs = snap;
            return;
        }

        mapPageFragment.selectZonesForUpdating(bounds);

        lastCallMs = snap;
        currentCameraBounds = bounds;
    }
}
