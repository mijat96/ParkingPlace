package com.rmj.parking_place.listener;

import android.location.Location;
import android.widget.Toast;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.model.LatLng;
import com.rmj.parking_place.App;
import com.rmj.parking_place.fragments.MapFragment;

public class LocationCallbackImplementation extends LocationCallback {
    private MapFragment mapFragment;

    public LocationCallbackImplementation(MapFragment mapFragment) {
        this.mapFragment = mapFragment;
    }

    @Override
    public void onLocationResult(LocationResult locationResult) {
        Location location;
        if (locationResult == null) {
            location = null;
        }
        else {
            // locationResult.getLocations();
            location = locationResult.getLastLocation();
        }

        Location oldLocation = mapFragment.getCurrentLocation();
        if (oldLocation != null && location != null) {
            if (oldLocation.getLatitude() == location.getLatitude()
                    && oldLocation.getLongitude() == location.getLongitude()) {
                return;
            }
        }

        Location currentLocation = location;
        mapFragment.setCurrentLocation(currentLocation);
        if (currentLocation == null) {
            Toast.makeText(mapFragment.getActivity(), "Izgubili smo vasu lokaciju!", Toast.LENGTH_SHORT).show();
            return;
        }
        else if (oldLocation == null) {
            // ako pre nismo imali prikazanu lokaciju, sada kad smo dobili
            mapFragment.updateCameraPosition(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), true);

            Toast.makeText(mapFragment.getActivity(), "Ponovo imamo vasu lokaciju!", Toast.LENGTH_SHORT).show();

            if (!App.mockLocationAllowed() && mapFragment.checkMockLocation()) {
                return;
            }
        }

        mapFragment.tryToFindEmptyParkingPlaceNearbyAndSetMode();
    }

    public void setMapFragment(MapFragment mapFragment) {
        this.mapFragment = mapFragment;
    }
}
