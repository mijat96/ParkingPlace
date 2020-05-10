package com.rmj.parking_place.listener;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLngBounds;
import com.rmj.parking_place.actvities.MapActivity;

public class OnCameraChangeListenerImplementation implements GoogleMap.OnCameraChangeListener {
    private MapActivity mapActivity;
    private GoogleMap map;

    private LatLngBounds currentCameraBounds;
    private long lastCallMs = Long.MIN_VALUE;

    private static int CAMERA_MOVE_REACT_THRESHOLD_MS = 500;

    public OnCameraChangeListenerImplementation(GoogleMap map, MapActivity mapActivity) {
        this.map = map;
        this.mapActivity = mapActivity;
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

        mapActivity.selectZonesForUpdating(bounds);

        lastCallMs = snap;
        currentCameraBounds = bounds;
    }
}
