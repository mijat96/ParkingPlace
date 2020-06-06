package com.rmj.parking_place.listener;

import android.content.res.Configuration;

import com.androidmapsextensions.ClusterGroup;
import com.androidmapsextensions.GoogleMap;
import com.androidmapsextensions.Marker;
import com.google.android.gms.maps.model.LatLng;
import com.rmj.parking_place.fragments.MapFragment;
import com.rmj.parking_place.fragments.MapPageFragment;
import com.rmj.parking_place.model.ParkingPlace;

public class OnMapClickListenerImplementation implements GoogleMap.OnMapClickListener {
    private MapFragment mapFragment;
    private MapPageFragment mapPageFragment;

    public OnMapClickListenerImplementation(MapFragment mapFragment, MapPageFragment mapPageFragment) {
        this.mapFragment = mapFragment;
        this.mapPageFragment = mapPageFragment;
    }

    @Override
    public void onMapClick(LatLng position) {
        boolean canReserveMode = mapPageFragment.isInCanReserveMode();
        boolean canReserveAndCanTakeMode = mapPageFragment.isInCanReserveAndCanTakeMode();

        if (canReserveMode || canReserveAndCanTakeMode) {
            if (canReserveMode) {
                mapPageFragment.setNoneMode();
            }
            else if (canReserveAndCanTakeMode) {
                mapPageFragment.setCanTakeMode();
            }
        }

        ParkingPlace selectedParkingPlace = mapFragment.getSelectedParkingPlace();

        if (selectedParkingPlace != null) {
            String markerIcon = selectedParkingPlace.getStatus().name();
            Marker selectedParkingPlaceMarker = mapFragment.getSelectedParkingPlaceMarker();
            mapFragment.updateParkingPlaceMarker(selectedParkingPlaceMarker, markerIcon);
            selectedParkingPlaceMarker.setClusterGroup(ClusterGroup.DEFAULT);

            selectedParkingPlace = null;
            mapFragment.setSelectedParkingPlace(selectedParkingPlace);

            selectedParkingPlaceMarker = null;
            mapFragment.setSelectedParkingPlaceMarker(selectedParkingPlaceMarker);

            mapPageFragment.hidePlaceIndoFragmet();
        }

        // mapPageFragment.setClickedLocation(latLng);

        if (mapPageFragment.isFindParkingPlaceFragmentVisible()) {
            int orientation = mapFragment.getResources().getConfiguration().orientation;
            boolean orientationPortrait;
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                orientationPortrait = true;
            } else {
                // landscape mode
                orientationPortrait = false;
            }
            mapPageFragment.setVisibilityOfFindParkingButton();
            mapFragment.changeMarginsOfMyLocationButton(true, orientationPortrait);
            mapPageFragment.returnGoogleLogoOnStartPosition();
            mapPageFragment.setInvisibilityOfFindParkingFragment();
        }
    }

    public void setMapFragment(MapFragment mapFragment) {
        this.mapFragment = mapFragment;
    }

    public void setMapPageFragment(MapPageFragment mapPageFragment) {
        this.mapPageFragment = mapPageFragment;
    }
}
