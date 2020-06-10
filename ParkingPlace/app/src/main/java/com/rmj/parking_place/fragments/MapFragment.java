package com.rmj.parking_place.fragments;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Rect;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.androidmapsextensions.ClusterGroup;
import com.androidmapsextensions.MarkerOptions;
import com.androidmapsextensions.Polyline;
import com.androidmapsextensions.PolylineOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.rmj.parking_place.App;
import com.rmj.parking_place.R;
import com.rmj.parking_place.actvities.MainActivity;
import com.rmj.parking_place.dialogs.DialogForMockLocation;
import com.rmj.parking_place.dialogs.LocationDialog;
import com.rmj.parking_place.dialogs.NotificationDialog;
import com.rmj.parking_place.dto.DTO;
import com.rmj.parking_place.dto.ReservingDTO;
import com.rmj.parking_place.dto.TakingDTO;
import com.rmj.parking_place.dto.navigation.NavigationDTO;
import com.rmj.parking_place.exceptions.AlreadyReservedParkingPlaceException;
import com.rmj.parking_place.exceptions.AlreadyTakenParkingPlaceException;
import com.rmj.parking_place.exceptions.CurrentLocationUnknownException;
import com.rmj.parking_place.exceptions.MaxAllowedDistanceForReservationException;
import com.rmj.parking_place.exceptions.NotFoundParkingPlaceException;
import com.rmj.parking_place.listener.LocationCallbackImplementation;
import com.rmj.parking_place.listener.LocationListenerImplementation;
import com.rmj.parking_place.listener.OnCameraChangeListenerImplementation;
import com.rmj.parking_place.listener.OnMapClickListenerImplementation;
import com.rmj.parking_place.listener.OnMapReadyCallbackImplementation;
import com.rmj.parking_place.listener.OnMarkerClickListenerImplementation;
import com.rmj.parking_place.model.FavoritePlace;
import com.rmj.parking_place.model.FromTo;
import com.rmj.parking_place.model.PaidParkingPlace;
import com.rmj.parking_place.model.ParkingPlace;
import com.rmj.parking_place.model.ParkingPlaceStatus;
import com.rmj.parking_place.model.Reservation;
import com.rmj.parking_place.model.TicketType;
import com.rmj.parking_place.service.NavigationServerUtils;
import com.rmj.parking_place.utils.AfterGetNavigation;
import com.rmj.parking_place.utils.ClusterIconUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MapFragment extends Fragment {

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    private static HashMap<FromTo, NavigationDTO> navigationCash = new HashMap<FromTo, NavigationDTO>();

    //-----------------------------------------------------------------
    private static LocationManager locationManager;
    // private static LocationListenerImplementation locationListenerImpl;
    // private String provider;
    //-----------------------------------------------------------------
    private FusedLocationProviderClient fusedLocationProviderClient;
    private static LocationRequest locationRequest;
    private static LocationCallbackImplementation locationCallbackImplementation;
    //-----------------------------------------------------------------


    private com.androidmapsextensions.SupportMapFragment mMapFragment;
    //private SupportMapFragment mMapFragment;

    private AlertDialog locationDialog;
    private Location currentLocation;
    //private com.androidmapsextensions.Marker currentLocationMarker;

    // private GoogleMap map;
    private com.androidmapsextensions.GoogleMap map;

    private HashMap<com.rmj.parking_place.model.Location, com.androidmapsextensions.Marker> parkingPlaceMarkers;
    private HashMap<com.rmj.parking_place.model.Location, ParkingPlace> parkingPlaces;
    // ----------------------------------------------------
    private ArrayList<com.androidmapsextensions.Marker> favoritePlaceMarkers;
    // ----------------------------------------------------
    private ParkingPlace selectedParkingPlace;
    private ParkingPlace foundedParkingPlaceNearby;

    private ParkingPlace potentiallyReservedParkingPlace;
    private ParkingPlace reservedParkingPlace;

    private ParkingPlace potentiallyTakenParkingPlace;
    private ParkingPlace takenParkingPlace;
    // ----------------------------------------------------
    private com.androidmapsextensions.Marker selectedParkingPlaceMarker;
    private com.androidmapsextensions.Marker foundedParkingPlaceNearbyMarker;

    private com.androidmapsextensions.Marker potentiallyReservedParkingPlaceMarker;
    private com.androidmapsextensions.Marker reservedParkingPlaceMarker;

    private com.androidmapsextensions.Marker potentiallyTakenParkingPlaceMarker;
    private com.androidmapsextensions.Marker takenParkingPlaceMarker;
    // ----------------------------------------------------
    private Polyline navigationPathPolyline;
    // ----------------------------------------------------
    private Reservation reservation;
    private PaidParkingPlace paidParkingPlace;
    // ----------------------------------------------------

    private MainActivity mainActivity;
    private MapPageFragment mapPageFragment;

    private static OnMapReadyCallbackImplementation onMapReadyCallbackImplementation;

    private static final int MAX_ALLOWED_DISTANCE_FOR_RESERVATION = 5000; // meters
    private static final float MAX_DISTANCE_PARKING_PLACE_LOCATION_TO_FAVORITE_PLACE = 100.0f; // meters
    private static final float MAX_ALLOWED_DISTANCE_FOR_TAKING = 1.0f; // meters

    public static HashMap<String, Integer> markerIcons;
    static {
        markerIcons = new HashMap<String, Integer>();
        markerIcons.put("CURRENT_LOCATION", R.drawable.round_my_location_blue_24);
        markerIcons.put("EMPTY", R.drawable.round_directions_car_green_36);
        markerIcons.put("RESERVED", R.drawable.round_directions_car_orange_36);
        markerIcons.put("TAKEN", R.drawable.round_directions_car_red_36);
        // markerIcons.put("SELECTED", R.drawable.round_directions_car_purple_36);
        markerIcons.put("EMPTY_SELECTED", R.drawable.round_directions_car_green_selected_36);
        markerIcons.put("RESERVED_SELECTED", R.drawable.round_directions_car_orange_selected_36);
        markerIcons.put("TAKEN_SELECTED", R.drawable.round_directions_car_red_selected_36);

        markerIcons.put("HOME", R.drawable.baseline_home_blue_36);
        markerIcons.put("WORK", R.drawable.baseline_work_blue_36);
        markerIcons.put("OTHER", R.drawable.baseline_favorite_blue_36);

        markerIcons.put("CLUSTER", R.drawable.cluster_48);
    }

    private boolean dialogAllowUserLocationWasDisplayed;
    private boolean recoveredFragment;


    /*public static MapFragment newInstance() {
        MapFragment mpf = new MapFragment();
        return mpf;
    }*/

    public MapFragment() {
        this.dialogAllowUserLocationWasDisplayed = false;
        parkingPlaces = new HashMap<com.rmj.parking_place.model.Location, ParkingPlace>();
        parkingPlaceMarkers = new HashMap<com.rmj.parking_place.model.Location, com.androidmapsextensions.Marker>();
    }

    /**
     * Prilikom kreiranja fragmenta preuzimamo sistemski servis za rad sa lokacijama
     * */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mainActivity = (MainActivity) getActivity();
        mapPageFragment = (MapPageFragment) getParentFragment();

        if (savedInstanceState == null) {
            reservation = mainActivity.getReservation();
            paidParkingPlace = mainActivity.getRegularPaidParkingPlace();
            recoveredFragment = false;

            setupLocationCallbackAndLocationRequest();
            setupFusedLocationProviderClient();
            locationManager = (LocationManager) mainActivity.getSystemService(Context.LOCATION_SERVICE);
        }
        else {
            recoveredFragment = true;
            recoverSavedInstance(savedInstanceState);
        }

    }

//    private void setupLocationListener() {
//        locationListenerImpl = new LocationListenerImplementation(this);
//    }

    private void setupLocationCallbackAndLocationRequest() {
        locationCallbackImplementation = new LocationCallbackImplementation(this);
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(0);
        locationRequest.setFastestInterval(0);
    }

    /*private void setupLocationManager() {
        if (locationManager == null) {
            locationManager = (LocationManager) mainActivity.getSystemService(Context.LOCATION_SERVICE);
        }

        if (!App.useFusedLocation() && !App.mockLocationAllowed()) {
            if (currentLocation == null) {
                currentLocation = getCurrentLocationFromLocationManager();
            }

            if (checkMockLocation()) {
                mainActivity.navigateToHomeFramgent();
                return;
            }
        }
    }*/

    private void setupFusedLocationProviderClient() {
        if (checkLocationPermission()) {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(mainActivity);
                fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        currentLocation = location;
                        if (!App.mockLocationAllowed()) {
                            if (checkMockLocation()) {
                                //mainActivity.navigateToHomeFramgent();
                                return;
                            }
                        }

                        if (checkLocationPermission()) {
                            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                                    == PackageManager.PERMISSION_GRANTED ||
                                    ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                                            == PackageManager.PERMISSION_GRANTED) {
                                fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallbackImplementation, null);
                            }
                        }
                    }
                })
                .addOnFailureListener(mainActivity, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(mainActivity, e.getMessage(), Toast.LENGTH_SHORT).show();
                        mainActivity.navigateToHomeFramgent();
                    }
                });
            }
        }
    }

    private void recoverSavedInstance(Bundle savedInstanceState) {
        ArrayList<ParkingPlace> parkingPlacesArrayList = savedInstanceState.getParcelableArrayList("parkingPlaces");
        for(ParkingPlace parkingPlace : parkingPlacesArrayList) {
            parkingPlaces.put(parkingPlace.getLocation(), parkingPlace);
        }

        selectedParkingPlace = savedInstanceState.getParcelable("selectedParkingPlace");
        potentiallyReservedParkingPlace = savedInstanceState.getParcelable("potentiallyReservedParkingPlace");
        // reservedParkingPlace = savedInstanceState.getParcelable("reservedParkingPlace");
        potentiallyTakenParkingPlace = savedInstanceState.getParcelable("potentiallyTakenParkingPlace");
        // takenParkingPlace = savedInstanceState.getParcelable("takenParkingPlace");

        reservation = savedInstanceState.getParcelable("reservation");
        paidParkingPlace = savedInstanceState.getParcelable("paidParkingPlace");
        /*if (!parkingPlaces.isEmpty()) {
            restoreReservedAndTakenParkingPlace();
        }*/

        currentLocation = savedInstanceState.getParcelable("currentLocation");
        // provider = savedInstanceState.getString("provider");
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putParcelableArrayList("parkingPlaces", new ArrayList<ParkingPlace>(parkingPlaces.values()));

        if (selectedParkingPlace != null) {
            outState.putParcelable("selectedParkingPlace", selectedParkingPlace);
        }

        if (potentiallyReservedParkingPlace != null) {
            outState.putParcelable("potentiallyReservedParkingPlace", potentiallyReservedParkingPlace);
        }

        /*if (reservedParkingPlace != null) {
            outState.putParcelable("reservedParkingPlace", reservedParkingPlace);
        }*/

        if (potentiallyTakenParkingPlace != null) {
            outState.putParcelable("potentiallyTakenParkingPlace", potentiallyTakenParkingPlace);
        }

        /*if (takenParkingPlace != null) {
            outState.putParcelable("takenParkingPlace", takenParkingPlace);
        }*/

        if (reservation != null) {
            outState.putParcelable("reservation", reservation);
        }

        if (paidParkingPlace != null) {
            outState.putParcelable("paidParkingPlace", paidParkingPlace);
        }

        if (currentLocation != null) {
            outState.putParcelable("currentLocation", currentLocation);
        }

        /*if (provider != null) {
            outState.putString("provider", provider);
        }*/

       /* if (map != null) {
            outState.putParcelable("cameraPosition", map.getCameraPosition());
        }*/

        FragmentManager fm = getChildFragmentManager();
        fm.putFragment(outState, "mMapFragment", mMapFragment);

        super.onSaveInstanceState(outState);
    }

    /**
     * Kada zelmo da dobijamo informacije o lokaciji potrebno je da specificiramo
     * po kom kriterijumu zelimo da dobijamo informacije GSP, MOBILNO(WIFI, MObilni internet), GPS+MOBILNO
     * **/
    private void createMapFragmentAndInflate() {
        /*if (!App.useFusedLocation()) {
            setProvider();
        }*/

        //kreiramo novu instancu fragmenta
        // mMapFragment = SupportMapFragment.newInstance();
        mMapFragment = (com.androidmapsextensions.SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_container);
        mMapFragment.setRetainInstance(true);

        //i vrsimo zamenu trenutnog prikaza sa prikazom mape
        // FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        // transaction.replace(R.id.map_container, mMapFragment).commit();

        //pozivamo ucitavnje mape.
        //VODITI RACUNA OVO JE ASINHRONA OPERACIJA
        //LOKACIJE MOGU DA SE DOBIJU PRE MAPE I OBRATNO
        // mMapFragment.getMapAsync(this);
        onMapReadyCallbackImplementation = new OnMapReadyCallbackImplementation(this, mapPageFragment);
        mMapFragment.getExtendedMapAsync(onMapReadyCallbackImplementation);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
    }

    private void showLocatonDialog() {
        if (locationDialog == null) {
            locationDialog = new LocationDialog(mainActivity).prepareDialog();
        } else {
            if (locationDialog.isShowing()) {
                locationDialog.dismiss();
            }
        }

        locationDialog.show();
    }

    /**
     * returns true if mock location enabled, false if not enabled
     */
    private int mockLocationEnabled() {
        boolean mock;
        if (currentLocation == null) {
            return -1;
        }
        /*else if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            mock = currentLocation.isFromMockProvider();
        }
        else {*/
        String mockLocation = "0";
        try {
            mockLocation = Settings.Secure.getString(mainActivity.getContentResolver(), Settings.Secure.ALLOW_MOCK_LOCATION);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mock = !mockLocation.equals("0");
        //}

        if (mock) {
            return 1;
        }
        else {
            return 0;
        }
    }


    public boolean checkMockLocation() {
        int mock = mockLocationEnabled();
        if (mock == -1) {
            mapPageFragment.showDialogForCurrentLocationNotFound();
            return true;
        }
        else if (mock == 1) {
            mapPageFragment.showDialogForMockLocation();
            currentLocation = null;
            return true;
        }

        return false;
    }


    @Override
    public void onResume() {
        super.onResume();

        // createMapFragmentAndInflate();

        if (checkLocationPermission()) {
            boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean wifi = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (!gps && !wifi) {
                showLocatonDialog();
            }
            else if (ContextCompat.checkSelfPermission(getContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                //Request location updates:
                if (fusedLocationProviderClient == null) {
                    setupFusedLocationProviderClient();
                }
                else {
                    fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallbackImplementation, null);
                }
                Toast.makeText(getContext(), "ACCESS_FINE_LOCATION", Toast.LENGTH_SHORT).show();
            }
            else if(ContextCompat.checkSelfPermission(getContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){

                //Request location updates:
                if (fusedLocationProviderClient == null) {
                    setupFusedLocationProviderClient();
                }
                else {
                    fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallbackImplementation, null);
                }
                Toast.makeText(getContext(), "ACCESS_COARSE_LOCATION", Toast.LENGTH_SHORT).show();
            }

            if (currentLocation != null) {
                // updateCameraPosition(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), false);
                tryToFindEmptyParkingPlaceNearbyAndSetMode();
            }
           /* if (!parkingPlaces.isEmpty()) {
                drawParkingPlaceMarkersIfCan();
            }*/
        }

    }

    /**
     * ova metoda se mora pozivati unutar if bloka u kome je uslov da korisnik ima permisiju za lokaciju
     * (ACCESS_FINE_LOCATION or ACCESS_COARSE_LOCATION)
     */
    /*private void useMyLocationOnMap(boolean use) {
        if (map != null) {
            map.setMyLocationEnabled(use);
        }
    }*/

    /*private void switchLocationManagerAndFusedLocationProvider(boolean useFusedLocation) {
        if (useFusedLocation) {
            if (locationManager != null) {
                locationManager.removeUpdates(locationListenerImpl);
            }
            // locationManager = null;
            locationListenerImpl = null;
            removeCurrentLocationMarker();
            setupLocationCallbackAndLocationRequest();
            setupFusedLocationProviderClient();
        }
        else {
            if (fusedLocationProviderClient != null) {
                fusedLocationProviderClient.removeLocationUpdates(locationCallbackImplementation);
            }
            fusedLocationProviderClient = null;
            locationCallbackImplementation = null;
            locationRequest = null;
            useMyLocationOnMap(false);
            setupLocationListener();
            setupLocationManager();
        }
    }*/

    /*private Location getCurrentLocationFromLocationManager() {
        if (checkLocationPermission()) {
            if (ContextCompat.checkSelfPermission(mainActivity, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(mainActivity, Manifest.permission.ACCESS_COARSE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                if (provider == null) {
                    setProvider();
                }

                //Request location updates:
                Location location = locationManager.getLastKnownLocation(provider);
                return  location;
            }
        }

        return null;
    }*/

    /*public String getProvider() {
        return provider;
    }*/

    /*public void setProvider() {
        //specificiramo krijterijum da dobijamo informacije sa svih izvora
        //ako korisnik to dopusti
        Criteria criteria = new Criteria();

        //sistemskom servisu prosledjujemo taj kriterijum da bi
        //mogli da dobijamo informacje sa tog izvora
        provider = locationManager.getBestProvider(criteria, true);
    }*/

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup vg, Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        View view = inflater.inflate(R.layout.map_layout, vg, false);

        if (savedInstanceState == null) {
            createMapFragmentAndInflate();
        }
        else {
            FragmentManager fm = getChildFragmentManager();
            mMapFragment = (com.androidmapsextensions.SupportMapFragment) fm.getFragment(savedInstanceState, "mMapFragment");
            map = mMapFragment.getExtendedMap();

            updateReferencesCascadingInListeners();

            /*map.setOnMapClickListener(new OnMapClickListenerImplementation(this, mapPageFragment));
            map.setOnMarkerClickListener(new OnMarkerClickListenerImplementation(this, mapPageFragment));
            map.setOnCameraChangeListener(new OnCameraChangeListenerImplementation(map, mapPageFragment));*/
            restoreMarkers();
            restoreReservedAndTakenParkingPlace();
            restoreNavigationPathPolyline();
            if (selectedParkingPlace != null) {
                selectedParkingPlaceMarker = getParkingPlaceMarker(selectedParkingPlace.getLocation());
            }
            if (potentiallyReservedParkingPlace != null) {
                potentiallyReservedParkingPlaceMarker = getParkingPlaceMarker(potentiallyReservedParkingPlace.getLocation());
            }
           /* if (reservedParkingPlace != null) {
                reservedParkingPlaceMarker = getParkingPlaceMarker(reservedParkingPlace.getLocation());
            }*/
            if (potentiallyTakenParkingPlace != null) {
                potentiallyTakenParkingPlaceMarker = getParkingPlaceMarker(potentiallyTakenParkingPlace.getLocation());
            }
            /*if (takenParkingPlace != null) {
                takenParkingPlaceMarker = getParkingPlaceMarker(takenParkingPlace.getLocation());
            }*/
            /*CameraPosition oldCameraPosition = map.getCameraPosition();
            CameraPosition cameraPosition = savedInstanceState.getParcelable("cameraPosition");
            updateCameraPosition(cameraPosition, false);*/
        }

        return view;
    }

    private void updateReferencesCascadingInListeners() {
        onMapReadyCallbackImplementation.setMapFragment(this);
        onMapReadyCallbackImplementation.setMapPageFragment(mapPageFragment);
        //if (App.useFusedLocation()) {
        locationCallbackImplementation.setMapFragment(this);
        /*}
        else {
            locationListenerImpl.setMapFragment(this);
        }*/
    }

    /**
     * Svaki put kada uredjaj dobijee novu informaciju o lokaciji ova metoda se poziva
     * i prosledjuje joj se nova informacija o kordinatamad
     * */
    /*@Override
    public void onLocationChanged(Location location) {
        Location oldLocation = currentLocation;
        if (currentLocation != null) {
            if (currentLocation.getLatitude() == location.getLatitude()
                    && currentLocation.getLongitude() == location.getLongitude()) {
                return;
            }
        }

        currentLocation = location;
        if (currentLocation == null) {
            Toast.makeText(getContext(), "Izgubili smo vasu lokaciju!", Toast.LENGTH_SHORT).show();
            return;
        }
        else {
            if (oldLocation == null) {
                // ako pre nismo imali prikazanu lokaciju, sada kad smo dobili
                updateCameraPosition(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), true);

                Toast.makeText(getContext(), "Ponovo imamo vasu lokaciju!", Toast.LENGTH_SHORT).show();
            }
        }

//        if (currentLocation.hasSpeed()) {
//            ((MainActivity) this.getActivity()).setNoneMode();
//        }
//
//        // Toast.makeText(getActivity(), "NEW LOCATION", Toast.LENGTH_SHORT).show();
//        if (map != null) {
//            if (currentLocationMarker != null) {
//                currentLocationMarker.setPosition(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));
//            }
//            else {
//                currentLocationMarker = addMarker(currentLocation, "CURRENT_LOCATION");
//                updateCameraPosition(currentLocationMarker.getPosition());
//            }
//        }

        tryToFindEmptyParkingPlaceNearbyAndSetMode();
    }*/

    public void tryToFindEmptyParkingPlaceNearbyAndSetMode() {
        // MainActivity mainActivity = (MainActivity) this.getActivity();

        /*if(mapPageFragment == null)
        {
            mapPageFragment = (MapPageFragment) getParentFragment();
        }*/

        if (currentLocation == null) {
            return;
        }

        if (!mapPageFragment.isInIsTakingMode()) {
            foundedParkingPlaceNearby = tryToFindEmptyParkingPlaceNearby(currentLocation);

            if (foundedParkingPlaceNearby != null) {
                foundedParkingPlaceNearbyMarker = getParkingPlaceMarker(foundedParkingPlaceNearby.getLocation().getLatitude(),
                        foundedParkingPlaceNearby.getLocation().getLongitude());

                if (mapPageFragment.isInCanReserveMode()) {
                    if (selectedParkingPlace == null) {
                        throw new NotFoundParkingPlaceException("selectedParkingPlace == null");
                    }
                    else {
                        mapPageFragment.setCanReserveAndCanTakeMode();
                    }
                }
                else if (mapPageFragment.isInIsReservingMode()) {
                    if (reservedParkingPlace == null) {
                        throw new NotFoundParkingPlaceException("reservedParkingPlace == null");
                    }
                    else if (foundedParkingPlaceNearby.equals(reservedParkingPlace)) {
                        mapPageFragment.setIsReservingAndCanTakeMode();
                    }
                    else {
                        Toast.makeText(mainActivity, "As long as you have a parking place reserved,"
                                + " you cannot take another parking place.", Toast.LENGTH_SHORT).show();
                    }
                }
                else if (mapPageFragment.isInIsReservingAndCanTakeMode()) {
                    if (!foundedParkingPlaceNearby.equals(reservedParkingPlace)) {
                        mapPageFragment.setIsReservingMode();
                    }
                }
                else if (!mapPageFragment.isInCanReserveAndCanTakeMode()) {
                    mapPageFragment.setCanTakeMode();
                }
            }
            else {
                if (mapPageFragment.isInIsReservingAndCanTakeMode()) {
                    mapPageFragment.setIsReservingMode();
                }
                else if (mapPageFragment.isInCanReserveAndCanTakeMode()) {
                    mapPageFragment.setCanReserveMode();
                }
                else if (mapPageFragment.isInCanTakeMode()) {
                    mapPageFragment.setNoneMode();
                }
            }
        }
    }

    /*@Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }*/

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(mainActivity, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?

            // if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(mainActivity)
                        .setTitle("Allow user location")
                        .setMessage("To continue working we need your locations....Allow now?")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                // https://stackoverflow.com/questions/49490276/onrequestpermissionsresult-doesnt-work
                                /*ActivityCompat.requestPermissions(getActivity(),
                                        new String[]{
                                                Manifest.permission.ACCESS_FINE_LOCATION,
                                                Manifest.permission.ACCESS_COARSE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);*/
                                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();
                dialogAllowUserLocationWasDisplayed = true;
            } else {
                // No explanation needed, we can request the permission.
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
                // https://stackoverflow.com/questions/49490276/onrequestpermissionsresult-doesnt-work
                /*ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);*/
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(mainActivity,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        //Request location updates:
                        if (fusedLocationProviderClient == null) {
                            setupFusedLocationProviderClient();
                        }
                        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallbackImplementation, null);
                    }

                } else if (grantResults.length > 0
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED){

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    if (ContextCompat.checkSelfPermission(mainActivity,
                            Manifest.permission.ACCESS_COARSE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        //Request location updates:
                        if (fusedLocationProviderClient == null) {
                            setupFusedLocationProviderClient();
                        }
                        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallbackImplementation, null);
                    }

                }
                else if (dialogAllowUserLocationWasDisplayed
                        && !shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    mainActivity.finish();
                }
            }

        }
    }


    /**
     * KAda je mapa spremna mozemo da radimo sa njom.
     * Mozemo reagovati na razne dogadjaje dodavanje markera, pomeranje markera,klik na mapu,...
     * */
    /*@Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        currentLocation = null;

        if (checkLocationPermission()) {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                if (provider == null) {
                    setProvider();
                }
                //Request location updates:
                currentLocation = locationManager.getLastKnownLocation(provider);

                map.setMyLocationEnabled(true);
                map.setBuildingsEnabled(true);
                //map.getUiSettings().
            }
        }

        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
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

                if (selectedParkingPlace != null) {
                    String markerIcon = selectedParkingPlace.getStatus().name();
                    updateParkingPlaceMarker(selectedParkingPlaceMarker, markerIcon);
                    selectedParkingPlace = null;
                    selectedParkingPlaceMarker = null;

                    mapPageFragment.hidePlaceIndoFragmet();
                }

                // mapPageFragment.setClickedLocation(latLng);

                if (mapPageFragment.isFindParkingPlaceFragmentVisible()) {
                    mapPageFragment.returnGoogleLogoOnStartPosition();
                    changeMarginsOfMyLocationButton(true);
                    mapPageFragment.setVisibilityOfFindParkingButton();
                    mapPageFragment.setInvisibilityOfFindParkingFragment();
                }
            }
        });

        //ako zelmo da reagujemo na klik markera koristimo marker click listener
        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                ParkingPlace oldSelectedParkingPlace = selectedParkingPlace;
                Marker oldSelectedParkingPlaceMarker = selectedParkingPlaceMarker;

                LatLng position = marker.getPosition();
                selectedParkingPlace = getParkingPlace(position.latitude, position.longitude);
                if (selectedParkingPlace == null) {
                    return true;
                }


                if (oldSelectedParkingPlace != null) {
                    String markerIcon = oldSelectedParkingPlace.getStatus().name();
                    updateParkingPlaceMarker(oldSelectedParkingPlaceMarker, markerIcon);
                }

                // String markerIcon = "SELECTED_" + selectedParkingPlace.getStatus().name();
                String markerIcon = selectedParkingPlace.getStatus().name() + "_SELECTED";
                updateParkingPlaceMarker(marker, markerIcon);
                selectedParkingPlaceMarker = marker;

                // izracunati razdaljinu od trenutne lokacije do izabranog markera
                // MainActivity mainActivity = (MainActivity) getActivity();

                showPlaceInfoFragment();

                if (mapPageFragment.isInNoneMode()) {
                    mapPageFragment.setCanReserveMode();
                }
                else if (mapPageFragment.isInCanTakeMode()) {
                    if (foundedParkingPlaceNearby.equals(selectedParkingPlace)) {
                        Toast.makeText(getActivity(), "Ovo mesto ne mozete rezervisati, ali mozete zauzeti!",
                                Toast.LENGTH_SHORT).show();
                    }
                    else {
                        mapPageFragment.setCanReserveAndCanTakeMode();
                    }
                }

                Toast.makeText(getActivity(), marker.getTitle(), Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        map.setOnCameraChangeListener(new OnCameraChangeListenerImplementation(map, mapPageFragment));

        if (currentLocation != null) {
            // currentLocationMarker = addMarker(currentLocation, "CURRENT_LOCATION");
            // updateCameraPosition(currentLocationMarker.getPosition());
            // updateCameraPosition(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), true);
        }


        if (!recoveredFragment) {
            drawParkingPlaceMarkersIfCan();
        }
        if (mapPageFragment.isInIsReservingMode() || mapPageFragment.isInCanReserveAndCanTakeMode()) {
            redrawNavigationPath();
        }

        changeMarginsOfMyLocationButton(true);
    }*/

    public void showPlaceInfoFragment() {
        float distanceMarkerCurrentLocation = computeDistanceBetweenTwoPoints(selectedParkingPlace.getLocation().getLatitude(),
                selectedParkingPlace.getLocation().getLongitude(), currentLocation.getLatitude(), currentLocation.getLongitude());

        mapPageFragment.showPlaceInfoFragment(selectedParkingPlace, distanceMarkerCurrentLocation);
    }

    public void changeMarginsOfMyLocationButton(boolean showOnStartPosition, boolean orientationPortrait) {
        View mapView = mMapFragment.getView();
        if (mapView != null &&
                mapView.findViewById(Integer.parseInt("1")) != null) {
            // Get the button view
            View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
            // and next place it, on bottom right (as Google Maps app)
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)
                    locationButton.getLayoutParams();
            // position on right bottom
            // layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            // layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            int topMargin, rightPadding;
            // mapView.measure(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            int mapViewHeight = mapView.getHeight();
            // int mapViewHeight = mapView.getMeasuredHeight();
            if (showOnStartPosition) {
                /*if (orientationPortrait) {
                    topMargin = mapPageFragment.getFindParkingFragmentHeight(); // mapPageFragment.getFindParkingButtonHeight(); // mapViewHeight - mapPageFragment.getFindParkingButtonHeight();
                }
                else {
                    topMargin = mapPageFragment.getFindParkingButtonHeight();
                }*/
                topMargin = mapPageFragment.getFindParkingButtonHeight();
                rightPadding = 0;
            }
            else {
                if (orientationPortrait) {
                    topMargin = mapPageFragment.getFindParkingFragmentHeight();// mapViewHeight - mapPageFragment.getFindParkingFragmentHeight();
                    rightPadding = 0;
                }
                else {
                    topMargin = 5;// mapViewHeight;
                    rightPadding = mapPageFragment.getFindParkingFragmentWidth();
                }
            }

            if (orientationPortrait) {
                topMargin += 40;
            }

            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP,0);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM,RelativeLayout.TRUE);
            layoutParams.setMargins(0, 0, 0, topMargin);
            map.setPadding(0, 0, rightPadding, 0);
        }
    }

    public void updateCameraPosition(LatLng position, boolean withAnimation) {
        if(map != null) {

            updateCameraPosition(position, 15, withAnimation);
        }
    }

    public void updateCameraPosition(LatLng position, float zoom, boolean withAnimation) {
        if(map != null) {
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(position).zoom(zoom).build();
            updateCameraPosition(cameraPosition, withAnimation);
        }
    }

    public void updateCameraPosition(CameraPosition cameraPosition, boolean withAnimation) {
        if(map != null) {
            CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
            if (withAnimation) {
                map.animateCamera(cameraUpdate);
            }
            else {
                // map.getCameraPosition().zoom
                map.moveCamera(cameraUpdate);
            }
        }
    }

    public void updateCameraBounds(LatLngBounds bounds, boolean withAnimation) {
        if(map != null) {
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 0);
            if (withAnimation) {
                map.animateCamera(cameraUpdate);
            }
            else {
                map.moveCamera(cameraUpdate);
            }
        }
    }

    public ParkingPlace getParkingPlace(double latitude, double longitude) {
        com.rmj.parking_place.model.Location location = new com.rmj.parking_place.model.Location(latitude, longitude);
        return parkingPlaces.get(location);
    }

    public HashMap<com.rmj.parking_place.model.Location, com.androidmapsextensions.Marker> createParkingPlaceMarkers(
            Collection<ParkingPlace> parkingPlaces) {
        HashMap<com.rmj.parking_place.model.Location, com.androidmapsextensions.Marker> markers =
                new HashMap<com.rmj.parking_place.model.Location, com.androidmapsextensions.Marker>();

        com.androidmapsextensions.Marker marker;
        String markerIcon;
        for (ParkingPlace parkingPlace : parkingPlaces) {
            markerIcon = parkingPlace.getStatus().name();
            if (selectedParkingPlace != null) {
                if (parkingPlace.equals(selectedParkingPlace)) {
                    markerIcon += "_SELECTED";
                }
            }
            marker = addMarker(parkingPlace.getLocation(), markerIcon, parkingPlace.getStatus());
            markers.put(parkingPlace.getLocation(), marker);
        }
        return markers;
    }

    public void addParkingPlacesAndMarkers(Collection<ParkingPlace> newParkingPlaces) {
        boolean empty = parkingPlaces.isEmpty();
        for(ParkingPlace newParkingPlace : newParkingPlaces) {
            parkingPlaces.put(newParkingPlace.getLocation(), newParkingPlace);
        }

        if (map != null) {
            HashMap<com.rmj.parking_place.model.Location, com.androidmapsextensions.Marker> newMarkers =
                                                                            createParkingPlaceMarkers(newParkingPlaces);
            parkingPlaceMarkers.putAll(newMarkers);
        }

        if (empty) {
            restoreReservedAndTakenParkingPlace();
        }
    }

    private com.androidmapsextensions.Marker addMarker(Location location, String markerIcon, ParkingPlaceStatus status) {
        LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());

        com.androidmapsextensions.Marker marker = addMarker(loc, markerIcon, status);
        return marker;
    }

    private com.androidmapsextensions.Marker addMarker(com.rmj.parking_place.model.Location location, String markerIcon,
                                                       ParkingPlaceStatus status) {
        LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());

        com.androidmapsextensions.Marker marker = addMarker(loc, markerIcon, status);
        return marker;
    }

    private com.androidmapsextensions.Marker addMarker(LatLng loc, String markerIcon, ParkingPlaceStatus status) {
        int resourceId = markerIcons.get(markerIcon);

        com.androidmapsextensions.Marker marker = map.addMarker(
                new MarkerOptions()
                        .title("YOUR_POSITON")
                        .anchor(0.5f,0.5f) // centriramo marker na odgovarajuce koordinate
                        .icon(BitmapDescriptorFactory.fromResource(resourceId))
                        .position(loc)
                        .data(status) // ovo setujemo zbog cluster-a
        );
        marker.setFlat(true);

        return marker;
    }

    /*public void addCurrentLocationMarker(LatLng loc) {
        int resourceId = markerIcons.get("CURRENT_LOCATION");

        currentLocationMarker = map.addMarker(
                new MarkerOptions()
                        .anchor(0.5f,0.5f) // centriramo marker na odgovarajuce koordinate
                        .icon(BitmapDescriptorFactory.fromResource(resourceId))
                        .position(loc)
                        .clusterGroup(ClusterGroup.NOT_CLUSTERED)
        );
        currentLocationMarker.setFlat(true);
    }*/

    private void removeMarkers(List<Marker> markers) {
        for (Marker marker : markers) {
            marker.remove();
        }
    }

    /**
     *
     * Rad sa lokacjom izuzetno trosi bateriju.Obavezno osloboditi kada vise ne koristmo
     * */
    @Override
    public void onPause() {
        super.onPause();

        /*if (locationManager != null) {
            locationManager.removeUpdates(locationListenerImpl);
        }*/

        if (fusedLocationProviderClient != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallbackImplementation);
        }
    }

    private float computeDistanceBetweenTwoPoints(com.rmj.parking_place.model.Location pointA,
                                                  com.rmj.parking_place.model.Location pointB) {
        return computeDistanceBetweenTwoPoints(pointA.getLatitude(), pointA.getLongitude(),
                pointB.getLatitude(), pointB.getLongitude());
    }

    private float computeDistanceBetweenTwoPoints(double latitudeA, double longitudeA, double latitudeB, double longitudeB) {
        float[] results = new float[1];
        Location.distanceBetween(latitudeA, longitudeA, latitudeB, longitudeB, results);
        return results[0];
    }

    private ParkingPlace tryToFindEmptyParkingPlaceNearby(Location location) {
        if (parkingPlaces == null || (parkingPlaces != null && parkingPlaces.isEmpty())) {
            return null;
        }

        // MainActivity mainActivity = (MainActivity) getActivity();
        boolean reservingMode = mapPageFragment.isInIsReservingMode() || mapPageFragment.isInIsReservingAndCanTakeMode();
        if (reservingMode) {
            if (reservedParkingPlace == null) {
                throw  new NotFoundParkingPlaceException("reservedParkingPlace == null");
            }
        }

        boolean nearBy;
        for (ParkingPlace parkingPlace : parkingPlaces.values()) {
            if (parkingPlace.getStatus() == ParkingPlaceStatus.EMPTY
                    || (reservingMode && parkingPlace.equals(reservedParkingPlace))) {
                nearBy = parkingPlaceIsNearby(parkingPlace, location);
                if (nearBy) {
                    return  parkingPlace;
                }
            }
        }

        return null;
    }

    private boolean parkingPlaceIsNearby(ParkingPlace parkingPlace, Location location) {
        float distance = computeDistanceBetweenTwoPoints(parkingPlace.getLocation().getLatitude(),
                parkingPlace.getLocation().getLongitude(), location.getLatitude(), location.getLongitude());
        // distance (meteres)
        // return distance < 0.5;
        return distance <= MAX_ALLOWED_DISTANCE_FOR_TAKING;
    }

    public void updateParkingPlaceMarker(com.androidmapsextensions.Marker marker, String newMarkerIcon)
            throws NotFoundParkingPlaceException {
        // Marker marker = getParkingPlaceMarker(latitude, longitude);

        if (marker == null) {
            //throw new NotFoundParkingPlaceException("marker == null (updateParkingPlaceMarker method)");
            return;
        }

        int resourceId = markerIcons.get(newMarkerIcon);
        marker.setIcon(BitmapDescriptorFactory.fromResource(resourceId));



        /*marker.remove();
        Marker newMarker = addMarker(marker.getPosition(), newMarkerIcon);
        parkingPlaceMarkers.put(new com.rmj.parking_place.model.Location(newMarker.getPosition().latitude,
                                                                        newMarker.getPosition().longitude), newMarker);
        return newMarker;*/
    }

    private com.androidmapsextensions.Marker getParkingPlaceMarker(double latitude, double longitude) throws NotFoundParkingPlaceException {
        com.rmj.parking_place.model.Location location = new com.rmj.parking_place.model.Location(latitude, longitude);
        com.androidmapsextensions.Marker marker = getParkingPlaceMarker(location);
        return marker;
    }

    private com.androidmapsextensions.Marker getParkingPlaceMarker(com.rmj.parking_place.model.Location location) throws NotFoundParkingPlaceException {
        com.androidmapsextensions.Marker marker = parkingPlaceMarkers.get(location);

        if (marker == null) {
            throw new NotFoundParkingPlaceException("Not found parking place marker (latitude=" + location.getLatitude()
                    + ", longitude=" + location.getLongitude() + ")");
        }

        return marker;
    }

    public ReservingDTO checkAndPrepareAllForReservingOnServer() throws AlreadyTakenParkingPlaceException,
            AlreadyReservedParkingPlaceException, NotFoundParkingPlaceException, CurrentLocationUnknownException,
            MaxAllowedDistanceForReservationException {
        float distance;
        if (selectedParkingPlace == null) {
            throw new NotFoundParkingPlaceException("selectedParkingPlace == null");
        }
        else if (currentLocation == null) {
            throw new CurrentLocationUnknownException("Current location unknown!");
        }
        else if ((distance = computeDistanceBetweenTwoPoints(currentLocation.getLatitude(), currentLocation.getLongitude(),
                selectedParkingPlace.getLocation().getLatitude(), selectedParkingPlace.getLocation().getLongitude()))
                > MAX_ALLOWED_DISTANCE_FOR_RESERVATION) {
            throw new MaxAllowedDistanceForReservationException("Max allowed distance for reservation is "
                    + MAX_ALLOWED_DISTANCE_FOR_RESERVATION + " meters, but your distance is " + distance + " meters.");
        }
        else if (selectedParkingPlace.getStatus() == ParkingPlaceStatus.RESERVED) {
            throw new AlreadyReservedParkingPlaceException("Parking place (latitude="
                    + selectedParkingPlace.getLocation().getLatitude()
                    + ", longitude=" + selectedParkingPlace.getLocation().getLongitude()
                    + ") is already reserved");
        }
        else if (selectedParkingPlace.getStatus() == ParkingPlaceStatus.TAKEN) {
            throw new AlreadyTakenParkingPlaceException("Parking place (latitude="
                    + selectedParkingPlace.getLocation().getLatitude()
                    + ", longitude=" + selectedParkingPlace.getLocation().getLongitude()
                    + ") is already taken");
        }

        potentiallyReservedParkingPlace = selectedParkingPlace;
        if (selectedParkingPlaceMarker == null) {
            selectedParkingPlaceMarker = getParkingPlaceMarker(selectedParkingPlace.getLocation());
        }
        else {
            potentiallyReservedParkingPlaceMarker = selectedParkingPlaceMarker;
        }

        return new ReservingDTO(potentiallyReservedParkingPlace.getZone().getId(), potentiallyReservedParkingPlace.getId(),
                                currentLocation.getLatitude(), currentLocation.getLongitude());
    }

    public void reserveParkingPlace(Reservation reservation) {
        reservedParkingPlace = potentiallyReservedParkingPlace;
        potentiallyReservedParkingPlace = null;

        reservedParkingPlaceMarker = potentiallyReservedParkingPlaceMarker;
        potentiallyReservedParkingPlaceMarker = null;

        ParkingPlaceStatus newParkingPlaceStatus = ParkingPlaceStatus.RESERVED;
        reservedParkingPlace.setStatus(newParkingPlaceStatus);
        //reservation = new Reservation(reservedParkingPlace);
        this.reservation = reservation;

        updateParkingPlaceMarker(reservedParkingPlaceMarker, newParkingPlaceStatus.name());

        AfterGetNavigation afterGetNavigation = navigationDTO -> drawNavigationPath(navigationDTO.getCoordinates());
        getNavigation(reservedParkingPlace, afterGetNavigation);

        // final MainActivity mainActivity = (MainActivity) getActivity();
        mapPageFragment.startTimerForReservationOrTakingOfParkingPlace(this.reservation.getEndDateTimeAndroid().getTime(), true);
    }

    public void setRealDistanceInParkingPlaceInfo() {
        AfterGetNavigation afterGetNavigation = navigationDTO -> mapPageFragment.setRealDistanceInParkingPlaceInfo(
                                                                        navigationDTO.getProperties().getDistance());
        getNavigation(selectedParkingPlace, afterGetNavigation);
    }

    private void getNavigation(ParkingPlace parkingPlace, AfterGetNavigation afterGetNavigation) {
        FromTo fromTo = new FromTo(new com.rmj.parking_place.model.Location(currentLocation.getLatitude(),
                currentLocation.getLongitude()),
                parkingPlace.getLocation());

        NavigationDTO navigationDTO = navigationCash.get(fromTo);
        if (navigationDTO != null) {
            afterGetNavigation.doSomething(navigationDTO);
            // drawNavigationPath(navigationDTO.getCoordinates());
        }
        else {

            NavigationServerUtils.navigationService.getNavigation(fromTo.getFrom().getLatitude(), fromTo.getFrom().getLongitude(),
                    fromTo.getTo().getLatitude(), fromTo.getTo().getLongitude(),"geojson")
                    .enqueue(new Callback<NavigationDTO>() {
                        @Override
                        public void onResponse(Call<NavigationDTO> call, Response<NavigationDTO> response) {
                            if (response == null || (response != null && !response.isSuccessful())) {
                                Toast.makeText(mainActivity, "Problem with loading navigation path. We will try again.",
                                        Toast.LENGTH_SHORT).show();
                            }
                            else {
                                NavigationDTO navigationDTO = response.body();
                                if (navigationDTO.getProperties().getDistance() > 0) {
                                    navigationCash.put(fromTo, navigationDTO);
                                }

                                afterGetNavigation.doSomething(navigationDTO);
                                // drawNavigationPath(navigationDTO.getCoordinates());
                            }
                        }

                        @Override
                        public void onFailure(Call<NavigationDTO> call, Throwable t) {
                            // Toast.makeText(getActivity(), t.getMessage(), Toast.LENGTH_SHORT).show();
                            // Toast.makeText(mapPageFragment.getActivity(), t.getMessage(), Toast.LENGTH_SHORT).show();
                            // dobija se sledeci error
                            // java.lang.NullPointerException: Attempt to invoke virtual method 'android.content.res.Resources android.content.Context.getResources()' on a null object reference

                            Toast.makeText(App.getAppContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
                            // call.enqueue(this); // pokusaj ponovo
                        }
                    });
        }


        // String url = getNavigationUrl(fromTo);
        // new NavigationTask(this, fromTo).execute(url, HttpRequestAndResponseType.NAVIGATION.name());
    }

    /*private String getNavigationUrl(FromTo fromTo) {
        String url = getString(R.string.NAVIGATION_SERVICE_BASE_URL)
                + "?flat=" + fromTo.getFrom().getLatitude() + "&flon=" + fromTo.getFrom().getLongitude()
                + "&tlat=" + fromTo.getTo().getLatitude() + "&tlon=" + fromTo.getTo().getLongitude()
                + "&format=geojson";
        return url;
    }*/

    public void takeParkingPlace(PaidParkingPlace paidParkingPlace) {
        takenParkingPlace = potentiallyTakenParkingPlace;
        potentiallyTakenParkingPlace = null;

        takenParkingPlaceMarker = potentiallyTakenParkingPlaceMarker;
        potentiallyTakenParkingPlaceMarker = null;

        ParkingPlaceStatus newParkingPlaceStatus = ParkingPlaceStatus.TAKEN;
        takenParkingPlace.setStatus(newParkingPlaceStatus);
        //paidParkingPlace = new PaidParkingPlace(takenParkingPlace, ticketType);
        this.paidParkingPlace = paidParkingPlace;
        updateParkingPlaceMarker(takenParkingPlaceMarker, newParkingPlaceStatus.name());

        Date endDateTime = this.paidParkingPlace.getEndDateTimeAndroid();
        long endDateTimeInMillis = endDateTime.getTime();

        if (this.paidParkingPlace.getTicketType() == TicketType.REGULAR) {
            mapPageFragment.startTimerForReservationOrTakingOfParkingPlace(endDateTimeInMillis, false);
        }
        else {
            mapPageFragment.setNoneMode();
        }
    }

    public TakingDTO checkAndPrepareAllForTakingOnServer() throws NotFoundParkingPlaceException, AlreadyReservedParkingPlaceException, AlreadyTakenParkingPlaceException {
        if (foundedParkingPlaceNearby == null) {
            throw new NotFoundParkingPlaceException("foundedParkingPlaceNearby == null");
        }
        else if (foundedParkingPlaceNearby.getStatus() == ParkingPlaceStatus.RESERVED) {
            throw new AlreadyReservedParkingPlaceException("Parking place (latitude="
                    + foundedParkingPlaceNearby.getLocation().getLatitude()
                    + ", longitude=" + foundedParkingPlaceNearby.getLocation().getLongitude()
                    + ") is already reserved");
        }
        else if (foundedParkingPlaceNearby.getStatus() == ParkingPlaceStatus.TAKEN) {
            throw new AlreadyTakenParkingPlaceException("Parking place (latitude="
                    + foundedParkingPlaceNearby.getLocation().getLatitude()
                    + ", longitude=" + foundedParkingPlaceNearby.getLocation().getLongitude()
                    + ") is already taken");
        }

        potentiallyTakenParkingPlace = foundedParkingPlaceNearby;
        if (foundedParkingPlaceNearby == null) {
            foundedParkingPlaceNearbyMarker = getParkingPlaceMarker(foundedParkingPlaceNearby.getLocation().getLatitude(),
                    foundedParkingPlaceNearby.getLocation().getLongitude());
        }
        else {
            potentiallyTakenParkingPlaceMarker = foundedParkingPlaceNearbyMarker;
        }

        return new TakingDTO(potentiallyTakenParkingPlace.getZone().getId(), potentiallyTakenParkingPlace.getId(),
                            currentLocation.getLatitude(), currentLocation.getLongitude());
    }

    public void finishReservationOfParkingPlace() {
        removeNavigationPathPolyline();

        if (reservedParkingPlace == null) {
            throw new NotFoundParkingPlaceException("reservedParkingPlace == null");
        }

        reservation = null;

        ParkingPlaceStatus newParkingPlaceStatus = ParkingPlaceStatus.EMPTY;
        reservedParkingPlace.setStatus(newParkingPlaceStatus);
//        Marker marker = getParkingPlaceMarker(reservedParkingPlace.getLocation().getLatitude(),
//                reservedParkingPlace.getLocation().getLongitude());
        reservedParkingPlace = null;

        String markerIcon = newParkingPlaceStatus.name();
        updateParkingPlaceMarker(reservedParkingPlaceMarker, markerIcon);
        reservedParkingPlaceMarker = null;
    }

    public void finishTakingOfParkingPlace() {
        if (takenParkingPlace == null) {
            throw new NotFoundParkingPlaceException("takenParkingPlace == null");
        }

        paidParkingPlace = null;

        ParkingPlaceStatus newParkingPlaceStatus = ParkingPlaceStatus.EMPTY;
        takenParkingPlace.setStatus(newParkingPlaceStatus);
        /*Marker marker = getParkingPlaceMarker(takenParkingPlace.getLocation().getLatitude(),
                takenParkingPlace.getLocation().getLongitude());*/
        takenParkingPlace = null;

        String markerIcon = newParkingPlaceStatus.name();
        updateParkingPlaceMarker(takenParkingPlaceMarker, markerIcon);
        takenParkingPlaceMarker = null;
    }

    public void checkSelectedParkingPlaceAndSetMode() {
        if (selectedParkingPlace != null) {
            // ((MainActivity) getActivity()).setCanReserveMode();
            mapPageFragment.setCanReserveMode();
        }
    }

    /*
     * Za ovu metodu se koristi 'synchronized' jer moze biti pozvana ili kad se downloaduju podaci ili kad je mapa spremna
     * */
    public void drawParkingPlaceMarkersIfCan() {
        if (map != null && parkingPlaces != null && !parkingPlaces.isEmpty()) {
            map.clear();
            parkingPlaceMarkers = createParkingPlaceMarkers(parkingPlaces.values());
        }
    }

    public void setParkingPlaces(HashMap<com.rmj.parking_place.model.Location, ParkingPlace> parkingPlaces) {
        this.parkingPlaces = parkingPlaces;
    }


    public void redrawNavigationPath() {
        if (navigationPathPolyline != null) {
            List<LatLng> points = navigationPathPolyline.getPoints();
            drawNavigationPathUsingPoints(points);

        }
    }
    private void drawNavigationPath(List<double[]> coordinates) {
        ArrayList<LatLng> points = new ArrayList<LatLng>();
        LatLng point;
        double lat, lng;
        double[] lngLat;

        for (int i = 0; i < coordinates.size(); i++) {
            lngLat = coordinates.get(i);
            lng = lngLat[0];
            lat = lngLat[1];
            point = new LatLng(lat, lng);
            points.add(point);
        }

        drawNavigationPathUsingPoints(points);
    }

    private void drawNavigationPathUsingPoints(List<LatLng> points) {
        com.androidmapsextensions.PolylineOptions lineOptions = new PolylineOptions();
        lineOptions.addAll(points);
        lineOptions.width(12);
        lineOptions.color(Color.rgb(0, 102, 204)); // blue
        lineOptions.geodesic(true);

        // Drawing polyline in the Google Map
        navigationPathPolyline = map.addPolyline(lineOptions);
        updateCameraPosition(points.get(points.size() / 2), true);
    }

    private void removeNavigationPathPolyline() {
        if (navigationPathPolyline != null) {
            navigationPathPolyline.remove();
            navigationPathPolyline = null;
        }
    }

    public void updateParkingPlaceMarkers(List<ParkingPlace> parkingPlacesForUpdating) {
        com.androidmapsextensions.Marker marker;
        String markerIcon;

        /*ArrayList<com.androidmapsextensions.Marker> markersForUpdating =
                new ArrayList<com.androidmapsextensions.Marker>(parkingPlacesForUpdating.size());*/

        for (ParkingPlace parkingPlace : parkingPlacesForUpdating) {
            marker = parkingPlaceMarkers.get(parkingPlace.getLocation());
            marker.setData(parkingPlace.getStatus()); // ovo setujemo zbog cluster-a
            //marker.setClusterGroup(ClusterGroup.FIRST_USER);
            //marker.setClusterGroup(ClusterGroup.DEFAULT);

            markerIcon = parkingPlace.getStatus().name();
            if (selectedParkingPlace != null) {
                if (parkingPlace.equals(selectedParkingPlace)) {
                    markerIcon += "_SELECTED";
                }
            }
            updateParkingPlaceMarker(marker, markerIcon);
            //markersForUpdating.add(marker);
        }

        /*for (com.androidmapsextensions.Marker m : markersForUpdating) {
            m.setClusterGroup(ClusterGroup.DEFAULT);
        }*/
    }

    public DTO checkAndPrepareDtoForLeavingParkingPlaceOnServer() {
        if (paidParkingPlace == null) {
            throw new NotFoundParkingPlaceException("foundedParkingPlaceNearby == null");
        }

        return new DTO(paidParkingPlace.getParkingPlace().getZone().getId(), paidParkingPlace.getParkingPlace().getId());
    }

    public HashMap<com.rmj.parking_place.model.Location, ParkingPlace> getParkingPlaces(){
        return parkingPlaces;
    }

    public void changePaddingOfGoogleMap(boolean bottom) {
        if (map != null) {
            int bottomPadding;
            if (bottom) {
                bottomPadding = 0;
            }
            else {
                bottomPadding = mapPageFragment.getFindParkingFragmentHeight();
            }
            map.setPadding(0, 0, 0, bottomPadding);
        }
    }

    public boolean checkParkingPlaceIsNearByFavoritePlace(ArrayList<FavoritePlace> favoritePlaces) {
        if (foundedParkingPlaceNearby == null) {
            throw new NotFoundParkingPlaceException("foundedParkingPlaceNearby == null");
        }

        if (favoritePlaces == null || (favoritePlaces != null && favoritePlaces.isEmpty())) {
            return false;
        }

        com.rmj.parking_place.model.Location foundedParkingPlaceNearbyLocation = foundedParkingPlaceNearby.getLocation();
        com.rmj.parking_place.model.Location favoritePlacelocation;
        double distance;

        for (FavoritePlace favoritePlace : favoritePlaces) {
            favoritePlacelocation = favoritePlace.getLocation();
            distance = computeDistanceBetweenTwoPoints(foundedParkingPlaceNearbyLocation.getLatitude(),
                    foundedParkingPlaceNearbyLocation.getLongitude(),
                                                        favoritePlacelocation.getLatitude(), favoritePlacelocation.getLongitude());
            if (distance <= MAX_DISTANCE_PARKING_PLACE_LOCATION_TO_FAVORITE_PLACE) {
                return true;
            }
        }

        return false;
    }

    public void setMap(com.androidmapsextensions.GoogleMap googleMap) {
        map = googleMap;
    }

    public ParkingPlace getSelectedParkingPlace() {
        return selectedParkingPlace;
    }

    public com.androidmapsextensions.Marker getSelectedParkingPlaceMarker() {
        return selectedParkingPlaceMarker;
    }

    public void setSelectedParkingPlace(ParkingPlace selectedParkingPlace) {
        this.selectedParkingPlace = selectedParkingPlace;
    }

    public void setSelectedParkingPlaceMarker(com.androidmapsextensions.Marker marker) {
        selectedParkingPlaceMarker = marker;
    }

    public ParkingPlace getFoundedParkingPlaceNearby() {
        return foundedParkingPlaceNearby;
    }

    public Location getCurrentLocation() {
        return  currentLocation;
    }

    public void setCurrentLocation(Location currentLocation) {
        this.currentLocation = currentLocation;
    }

    public LocationManager getLocationManager() {
        return locationManager;
    }

    public boolean isRecoveredFragment() {
        return recoveredFragment;
    }

    public void restoreMarkers() {
        /*List<com.androidmapsextensions.Marker> markers1 = map.getDisplayedMarkers();
        for (com.androidmapsextensions.Marker marker : markers) {
            if (marker.isCluster()) {
                System.out.println();
            }
            else {
                marker.setClusterGroup(1);
                marker.setIcon(null);
                marker.setPosition(new LatLng(40,50));

                System.out.println();
            }
        }*/

        List<com.androidmapsextensions.Marker> markers = map.getMarkers();
        // ovo vraca obicne markere (medju njima nece bit cluster markera)

        if (markers.isEmpty()) {
            return;
        }

        parkingPlaceMarkers = new HashMap<com.rmj.parking_place.model.Location, com.androidmapsextensions.Marker>();
        favoritePlaceMarkers = new ArrayList<com.androidmapsextensions.Marker>(3);
        LatLng position;

        for (com.androidmapsextensions.Marker marker : markers) {
            if (marker.getSnippet() == null) {
                position = marker.getPosition();
                parkingPlaceMarkers.put(new com.rmj.parking_place.model.Location(position.latitude,position.longitude), marker);
            }
            else {
                favoritePlaceMarkers.add(marker);
            }
        }
    }

    public void restoreNavigationPathPolyline() {
        List<Polyline> polylines = map.getPolylines();
        if (polylines.isEmpty()) {
            return;
        }
        navigationPathPolyline = polylines.get(0);
    }

    public void drawFavoritePlaceMarkers() {
        ArrayList<FavoritePlace> favoritePlaces = mainActivity.getFavoritePlaces();
        if (favoritePlaces == null || (favoritePlaces != null && favoritePlaces.isEmpty())) {
            return;
        }

        favoritePlaceMarkers = new ArrayList<com.androidmapsextensions.Marker>(3);
        com.androidmapsextensions.Marker marker;

        for (FavoritePlace favoritePlace : favoritePlaces) {
            marker = addFavoritePlaceMarker(favoritePlace);
            favoritePlaceMarkers.add(marker);
        }
    }

    private com.androidmapsextensions.Marker addFavoritePlaceMarker(FavoritePlace favoritePlace) {
        LatLng postion = new LatLng(favoritePlace.getLocation().getLatitude(), favoritePlace.getLocation().getLongitude());
        int resourceId = markerIcons.get(favoritePlace.getType().name());

        com.androidmapsextensions.Marker marker = map.addMarker(
                new MarkerOptions()
                        .title(favoritePlace.getName())
                        .anchor(0.5f,0.5f) // centriramo marker na odgovarajuce koordinate
                        .icon(BitmapDescriptorFactory.fromResource(resourceId))
                        .position(postion)
                        .snippet(favoritePlace.getName() + " (" + favoritePlace.getType().name() + ")")
                        .clusterGroup(ClusterGroup.NOT_CLUSTERED)
        );
        marker.setFlat(true);
        return marker;
    }

    public boolean isFavoritePlaceMarker(com.androidmapsextensions.Marker marker) {
        if (favoritePlaceMarkers == null || (favoritePlaceMarkers != null && favoritePlaceMarkers.isEmpty())) {
            return false;
        }

        return favoritePlaceMarkers.contains(marker);
    }

    public void updateDisplayedClusters(List<LatLngBounds> zonesBoundsForClustersUpdating) {
        List<com.androidmapsextensions.Marker> displayedMarkers = map.getDisplayedMarkers();
        int emptyMarkersCount;
        BitmapDescriptor icon;

        for (com.androidmapsextensions.Marker marker : displayedMarkers) {
            if (marker.isCluster()) {
                if (pointInsideZonesBounds(zonesBoundsForClustersUpdating, marker.getPosition())) {
                    emptyMarkersCount = countEmptyMarkersInClusterMarker(marker);
                    icon = ClusterIconUtils.makeIcon(marker.getMarkers().size(), emptyMarkersCount);
                    marker.setIcon(icon);
                }
            }
        }
    }

    private boolean pointInsideZonesBounds(List<LatLngBounds> zonesBoundsForClustersUpdating, LatLng point) {
        for (LatLngBounds latLngBounds : zonesBoundsForClustersUpdating) {
            if (latLngBounds.contains(point)) {
                return true;
            }
        }

        return false;
    }

    private int countEmptyMarkersInClusterMarker(com.androidmapsextensions.Marker clusterMarker) {
        List<com.androidmapsextensions.Marker> markers = clusterMarker.getMarkers();
        if (markers == null) {
            return 0;
        }

        int numberOfEmpties = 0;
        for (com.androidmapsextensions.Marker m: markers) {
            if(m.getData() == ParkingPlaceStatus.EMPTY){
                numberOfEmpties++;
            }
        }

        return numberOfEmpties;
    }

    public void resetMarginsOfMyLocationButton() {
        View mapView = mMapFragment.getView();
        if (mapView != null &&
            mapView.findViewById(Integer.parseInt("1")) != null) {
            // Get the button view
            View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
            // and next place it, on bottom right (as Google Maps app)
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)
                    locationButton.getLayoutParams();

            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP,0);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM,RelativeLayout.TRUE);
            layoutParams.setMargins(0, 0, 0, 70);
        }
    }

    public void restoreReservedParkingPlace(com.rmj.parking_place.model.Location location) {
        reservedParkingPlace = parkingPlaces.get(location);
        if (reservedParkingPlace != null) {
            reservedParkingPlaceMarker = getParkingPlaceMarker(reservedParkingPlace.getLocation());
        }
        else {
            // nisu jos pristigla parking mesta
            reservedParkingPlaceMarker = getParkingPlaceMarker(location);
        }
    }

    public void restoreTakenParkingPlace(com.rmj.parking_place.model.Location location) {
        takenParkingPlace = parkingPlaces.get(location);
        if (takenParkingPlace != null) {
            takenParkingPlaceMarker = getParkingPlaceMarker(takenParkingPlace.getLocation());
        }
        else {
            // nisu jos pristigla parking mesta
            takenParkingPlaceMarker = getParkingPlaceMarker(location);
        }
        mapPageFragment.activateBtnLeaveParkingPlace();
    }

    public void restoreReservedAndTakenParkingPlace() {
        Date now = new Date();
        if (reservation != null && now.before(reservation.getEndDateTimeAndroid())) {
            restoreReservedParkingPlace(reservation.getParkingPlace().getLocation());
        }
        if (paidParkingPlace != null && now.before(paidParkingPlace.getEndDateTimeAndroid())) {
            restoreTakenParkingPlace(paidParkingPlace.getParkingPlace().getLocation());
        }
    }

    public FusedLocationProviderClient getFusedLocationProviderClient() {
        return fusedLocationProviderClient;
    }

    /*public void updateCurrentLocationMarkerPosition(LatLng currentLocationLatLng) {
        if (currentLocationMarker == null) {
            addCurrentLocationMarker(currentLocationLatLng);
        }
        else {
            currentLocationMarker.setPosition(currentLocationLatLng);
        }
    }*/

    /*public void removeCurrentLocationMarker() {
        if (currentLocationMarker != null) {
            currentLocationMarker.remove();
            currentLocationMarker = null;
        }
    }*/

    /*public boolean isCurrentLocationMarker(com.androidmapsextensions.Marker marker) {
        if (currentLocationMarker == null) {
            return false;
        }

        return marker.equals(currentLocationMarker);
    }*/
}