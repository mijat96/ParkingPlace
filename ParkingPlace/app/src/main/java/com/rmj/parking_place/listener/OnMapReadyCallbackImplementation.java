package com.rmj.parking_place.listener;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationManager;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.androidmapsextensions.GoogleMap;
import com.androidmapsextensions.Marker;
import com.androidmapsextensions.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.rmj.parking_place.fragments.MapFragment;
import com.rmj.parking_place.fragments.MapPageFragment;

public class  OnMapReadyCallbackImplementation implements OnMapReadyCallback {
    private MapFragment mapFragment;
    private MapPageFragment mapPageFragment;

    public OnMapReadyCallbackImplementation(MapFragment mapFragment, MapPageFragment mapPageFragment) {
        this.mapFragment = mapFragment;
        this.mapPageFragment = mapPageFragment;
    }

    /**
     * KAda je mapa spremna mozemo da radimo sa njom.
     * Mozemo reagovati na razne dogadjaje dodavanje markera, pomeranje markera,klik na mapu,...
     * */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mapFragment.setMap(googleMap);
        /*if (mapFragment.isRecoveredFragment()) {
            mapFragment.restoreParkingPlaceMarkers();
            mapFragment.restoreNavigationPathPolyline();
        }*/
        mapFragment.setCurrentLocation(null);

        if (mapFragment.checkLocationPermission()) {
            if (ContextCompat.checkSelfPermission(mapFragment.getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(mapFragment.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                if (mapFragment.getProvider() == null) {
                    mapFragment.setProvider();
                }
                //Request location updates:
                Location currentLocation = mapFragment.getLocationManager().getLastKnownLocation(mapFragment.getProvider());
                mapFragment.setCurrentLocation(currentLocation);

                googleMap.setMyLocationEnabled(true);
                googleMap.setBuildingsEnabled(true);
                //map.getUiSettings().
            }
        }

        googleMap.setOnMapClickListener(new OnMapClickListenerImplementation(mapFragment, mapPageFragment));
        googleMap.setOnMarkerClickListener(new OnMarkerClickListenerImplementation(mapFragment, mapPageFragment));
        googleMap.setOnCameraChangeListener(new OnCameraChangeListenerImplementation(googleMap, mapPageFragment));
        googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
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
        });

        Location currentLocation = mapFragment.getCurrentLocation();
        if (currentLocation != null) {
            mapFragment.updateCameraPosition(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),
                                                                                                    true);
        }

        mapFragment.drawParkingPlaceMarkersIfCan();

        if (mapPageFragment.isInIsReservingMode() || mapPageFragment.isInCanReserveAndCanTakeMode()) {
            mapFragment.redrawNavigationPath();
        }

        mapFragment.drawFavoritePlaceMarkers();

        mapFragment.changePositionOfMyLocationButton(true);
    }

}
