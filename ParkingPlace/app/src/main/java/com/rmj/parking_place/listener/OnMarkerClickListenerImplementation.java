package com.rmj.parking_place.listener;

import android.widget.Toast;

import com.androidmapsextensions.GoogleMap;
import com.androidmapsextensions.Marker;
import com.google.android.gms.maps.model.LatLng;
import com.rmj.parking_place.fragments.MapFragment;
import com.rmj.parking_place.fragments.MapPageFragment;
import com.rmj.parking_place.model.ParkingPlace;

public class OnMarkerClickListenerImplementation implements GoogleMap.OnMarkerClickListener {

    private MapFragment mapFragment;
    private MapPageFragment mapPageFragment;

    public OnMarkerClickListenerImplementation(MapFragment mapFragment, MapPageFragment mapPageFragment) {
        this.mapFragment = mapFragment;
        this.mapPageFragment = mapPageFragment;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (mapFragment.isFavoritePlaceMarker(marker)) {
            marker.showInfoWindow();
            return true;
        }

        ParkingPlace oldSelectedParkingPlace = mapFragment.getSelectedParkingPlace();
        Marker oldSelectedParkingPlaceMarker = mapFragment.getSelectedParkingPlaceMarker();

        LatLng position = marker.getPosition();
        ParkingPlace selectedParkingPlace = mapFragment.getParkingPlace(position.latitude, position.longitude);
        mapFragment.setSelectedParkingPlace(selectedParkingPlace);
        if (selectedParkingPlace == null) {
            return true;
        }


        if (oldSelectedParkingPlace != null) {
            String markerIcon = oldSelectedParkingPlace.getStatus().name();
            mapFragment.updateParkingPlaceMarker(oldSelectedParkingPlaceMarker, markerIcon);
        }

        // String markerIcon = "SELECTED_" + selectedParkingPlace.getStatus().name();
        String markerIcon = selectedParkingPlace.getStatus().name() + "_SELECTED";
        mapFragment.updateParkingPlaceMarker(marker, markerIcon);
        mapFragment.setSelectedParkingPlaceMarker(marker);

        // izracunati razdaljinu od trenutne lokacije do izabranog markera
        // MainActivity mainActivity = (MainActivity) getActivity();

        mapFragment.showPlaceInfoFragment();

        if (mapPageFragment.isInNoneMode()) {
            mapPageFragment.setCanReserveMode();
        }
        else if (mapPageFragment.isInCanTakeMode()) {
            if (mapFragment.getFoundedParkingPlaceNearby().equals(selectedParkingPlace)) {
                Toast.makeText(mapFragment.getActivity(), "Ovo mesto ne mozete rezervisati, ali mozete zauzeti!",
                        Toast.LENGTH_SHORT).show();
            }
            else {
                mapPageFragment.setCanReserveAndCanTakeMode();
            }
        }

        Toast.makeText(mapFragment.getActivity(), marker.getTitle(), Toast.LENGTH_SHORT).show();
        return true;
    }
}
