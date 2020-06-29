package com.rmj.parking_place.listener;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationManager;
import android.util.DisplayMetrics;
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
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.rmj.parking_place.App;
import com.rmj.parking_place.R;
import com.rmj.parking_place.actvities.MainActivity;
import com.rmj.parking_place.fragments.MapFragment;
import com.rmj.parking_place.fragments.MapPageFragment;
import com.rmj.parking_place.model.PaidParkingPlace;
import com.rmj.parking_place.model.ParkingPlaceStatus;
import com.rmj.parking_place.model.Reservation;
import com.rmj.parking_place.utils.ClusterIconUtils;

import java.util.List;

public class  OnMapReadyCallbackImplementation implements OnMapReadyCallback {
    private MapFragment mapFragment;
    private MapPageFragment mapPageFragment;
    private MainActivity mainActivity;

    private OnMapClickListenerImplementation onMapClickListenerImplementation;
    private OnMarkerClickListenerImplementation onMarkerClickListenerImplementation;
    private OnCameraChangeListenerImplementation onCameraChangeListenerImplementation;
    private InfoWindowAdapterImplementation infoWindowAdapterImplementation;

    public OnMapReadyCallbackImplementation(MapFragment mapFragment, MapPageFragment mapPageFragment) {
        this.mapFragment = mapFragment;
        this.mapPageFragment = mapPageFragment;
        this.mainActivity = (MainActivity) mapFragment.getActivity();
    }

    /**
     * KAda je mapa spremna mozemo da radimo sa njom.
     * Mozemo reagovati na razne dogadjaje dodavanje markera, pomeranje markera,klik na mapu,...
     * */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mapFragment.setMap(googleMap);
        mapFragment.setMapTheme();
        /*if (mapFragment.isRecoveredFragment()) {
            mapFragment.restoreParkingPlaceMarkers();
            mapFragment.restoreNavigationPathPolyline();
        }*/
        mapFragment.setCurrentLocation(null);

        int screenHeight = getScreenHeight();

        ClusteringSettings clusteringSettings = new ClusteringSettings();
        clusteringSettings.addMarkersDynamically(true);
        //POPRAVITI JOS DIMENZIJE U ODNOSU NA MAPU
        if(screenHeight < 2340){
            clusteringSettings.clusterSize(50);
        }else{
            clusteringSettings.clusterSize(110);
        }
        clusteringSettings.clusterOptionsProvider(new ClusterOptionsProvider() {
            @Override
            public ClusterOptions getClusterOptions(List<Marker> markers) {
                int numberOfEmpties = 0;

                for (Marker m: markers) {
                    if(m.getData() == ParkingPlaceStatus.EMPTY) {
                        numberOfEmpties++;
                    }
                }

                BitmapDescriptor icon = ClusterIconUtils.makeIcon(markers.size(), numberOfEmpties);

                return new ClusterOptions().icon(icon);
            }
        });
        googleMap.setClustering(clusteringSettings);

        if (mapFragment.checkLocationPermission()) {
            if (ContextCompat.checkSelfPermission(mapFragment.getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(mapFragment.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                googleMap.setMyLocationEnabled(true);
                googleMap.setBuildingsEnabled(true);
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

        Reservation reservation = mainActivity.getReservation();
        PaidParkingPlace paidParkingPlace = mainActivity.getRegularPaidParkingPlace();
        Location currentLocation = mapFragment.getCurrentLocation();

        Double latitude = null, longitude = null;
        com.rmj.parking_place.model.Location location;

        // ukoliko imas zapocetu rezervaciju ili zauzeto mesto, podesi camera position tamo
        // u suprotnom, i ako imas currentLocation, onda podesi camera position na currentLocation
        if (reservation != null) {
            location = reservation.getParkingPlace().getLocation();
            latitude = location.getLatitude();
            longitude = location.getLongitude();
        }
        else if (paidParkingPlace != null) {
            location = paidParkingPlace.getParkingPlace().getLocation();
            latitude = location.getLatitude();
            longitude = location.getLongitude();
        }
        else if (currentLocation != null) {
            latitude = currentLocation.getLatitude();
            longitude = currentLocation.getLongitude();
        }

        if (latitude != null && longitude != null) {
            mapFragment.updateCameraPosition(new LatLng(latitude, longitude),true);
        }

        mapFragment.drawParkingPlaceMarkersIfCan();

        if (mapPageFragment.isInIsReservingMode() || mapPageFragment.isInCanReserveAndCanTakeMode()) {
            mapFragment.redrawNavigationPath();
        }

        mapFragment.drawFavoritePlaceMarkers();

        int orientation = mapFragment.getResources().getConfiguration().orientation;
        boolean orientationPortrait;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            orientationPortrait = true;
        } else {
            // landscape mode
            orientationPortrait = false;
        }

        boolean showOnStartPosition = !mapPageFragment.isFindParkingPlaceFragmentVisible();

        mapFragment.changeMarginsOfMyLocationButton(showOnStartPosition, orientationPortrait);
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

    public int getScreenHeight(){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        mapFragment.getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        //int width = displayMetrics.widthPixels;
        //int resolution[] = {height, width};
        return height;
    }
}
