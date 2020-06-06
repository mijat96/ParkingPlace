package com.rmj.parking_place.listener;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.rmj.parking_place.App;
import com.rmj.parking_place.fragments.MapFragment;

public class LocationListenerImplementation implements LocationListener {

    private MapFragment mapFragment;

    public LocationListenerImplementation(MapFragment mapFragment) {
        this.mapFragment = mapFragment;
    }

    /**
     * Svaki put kada uredjaj dobijee novu informaciju o lokaciji ova metoda se poziva
     * i prosledjuje joj se nova informacija o kordinatamad
     * */
    @Override
    public void onLocationChanged(Location location) {
        Location oldLocation = mapFragment.getCurrentLocation();
        if (oldLocation != null && location != null) {
            if (oldLocation.getLatitude() == location.getLatitude()
                    && oldLocation.getLongitude() == location.getLongitude()) {
                return;
            }
        }

        Location currentLocation = location;
        LatLng currentLocationLatLng;
        mapFragment.setCurrentLocation(currentLocation);
        if (currentLocation == null) {
            Toast.makeText(mapFragment.getActivity(), "Izgubili smo vasu lokaciju!", Toast.LENGTH_SHORT).show();
            return;
        }
        else {
            currentLocationLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            mapFragment.updateCurrentLocationMarkerPosition(currentLocationLatLng);
        }

        if (oldLocation == null) {
            // ako pre nismo imali prikazanu lokaciju, sada kad smo dobili
            if (currentLocationLatLng == null) {
                currentLocationLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            }
            mapFragment.updateCameraPosition(currentLocationLatLng, true);

            Toast.makeText(mapFragment.getActivity(), "Ponovo imamo vasu lokaciju!", Toast.LENGTH_SHORT).show();

            if (!App.mockLocationAllowed() && mapFragment.checkMockLocation()) {
                return;
            }
        }

        mapFragment.tryToFindEmptyParkingPlaceNearbyAndSetMode();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    public void setMapFragment(MapFragment mapFragment) {
        this.mapFragment = mapFragment;
    }
}
