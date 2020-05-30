package com.rmj.parking_place.listener;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationManager;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.androidmapsextensions.ClusterGroup;
import com.androidmapsextensions.ClusterOptions;
import com.androidmapsextensions.ClusterOptionsProvider;
import com.androidmapsextensions.ClusteringSettings;
import com.androidmapsextensions.GoogleMap;
import com.androidmapsextensions.Marker;
import com.androidmapsextensions.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.rmj.parking_place.R;
import com.rmj.parking_place.fragments.MapFragment;
import com.rmj.parking_place.fragments.MapPageFragment;
import com.rmj.parking_place.model.ParkingPlaceStatus;
import com.rmj.parking_place.utils.ClusterIconUtils;

import java.util.List;

public class  OnMapReadyCallbackImplementation implements OnMapReadyCallback {
    private MapFragment mapFragment;
    private MapPageFragment mapPageFragment;

    private OnMapClickListenerImplementation onMapClickListenerImplementation;
    private OnMarkerClickListenerImplementation onMarkerClickListenerImplementation;
    private OnCameraChangeListenerImplementation onCameraChangeListenerImplementation;
    private InfoWindowAdapterImplementation infoWindowAdapterImplementation;

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

        googleMap.setClustering(new ClusteringSettings().clusterOptionsProvider(new ClusterOptionsProvider() {
            @Override
            public ClusterOptions getClusterOptions(List<Marker> markers) {
                int numberOfEmpties = 0;

                for (Marker m: markers) {
                    if(m.getData() == ParkingPlaceStatus.EMPTY){
                        numberOfEmpties++;
                    }
                }

                BitmapDescriptor icon = ClusterIconUtils.makeIcon(markers.size(), numberOfEmpties);

                return new ClusterOptions().icon(icon);
            }
        }));

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

        onMapClickListenerImplementation = new OnMapClickListenerImplementation(mapFragment, mapPageFragment);
        onMarkerClickListenerImplementation = new OnMarkerClickListenerImplementation(mapFragment, mapPageFragment);
        onCameraChangeListenerImplementation = new OnCameraChangeListenerImplementation(googleMap, mapPageFragment);
        infoWindowAdapterImplementation = new InfoWindowAdapterImplementation(mapFragment);

        googleMap.setOnMapClickListener(onMapClickListenerImplementation);
        googleMap.setOnMarkerClickListener(onMarkerClickListenerImplementation);
        googleMap.setOnCameraChangeListener(onCameraChangeListenerImplementation);
        googleMap.setInfoWindowAdapter(infoWindowAdapterImplementation);

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


    public void setMapFragment(MapFragment mapFragment) {
        this.mapFragment = mapFragment;

        onMapClickListenerImplementation.setMapFragment(mapFragment);
        onMarkerClickListenerImplementation.setMapFragment(mapFragment);
        infoWindowAdapterImplementation.setMapFragment(mapFragment);
    }

    public void setMapPageFragment(MapPageFragment mapPageFragment) {
        this.mapPageFragment = mapPageFragment;

        onMapClickListenerImplementation.setMapPageFragment(mapPageFragment);
        onMarkerClickListenerImplementation.setMapPageFragment(mapPageFragment);
        onCameraChangeListenerImplementation.setMapPageFragment(mapPageFragment);
    }
}
