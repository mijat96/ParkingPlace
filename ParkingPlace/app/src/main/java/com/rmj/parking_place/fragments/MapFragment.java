package com.rmj.parking_place.fragments;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.rmj.parking_place.actvities.MapActivity;
import com.rmj.parking_place.R;
import com.rmj.parking_place.dialogs.LocationDialog;
import com.rmj.parking_place.dto.TakingDTO;
import com.rmj.parking_place.dto.navigation.NavigationDTO;
import com.rmj.parking_place.exceptions.AlreadyReservedParkingPlaceException;
import com.rmj.parking_place.exceptions.AlreadyTakenParkingPlaceException;
import com.rmj.parking_place.exceptions.CurrentLocationUnknownException;
import com.rmj.parking_place.exceptions.MaxAllowedDistanceForReservationException;
import com.rmj.parking_place.exceptions.NotFoundParkingPlaceException;
import com.rmj.parking_place.model.FromTo;
import com.rmj.parking_place.model.PaidParkingPlace;
import com.rmj.parking_place.model.ParkingPlace;
import com.rmj.parking_place.model.ParkingPlaceStatus;
import com.rmj.parking_place.model.Reservation;
import com.rmj.parking_place.utils.AsyncResponse;
import com.rmj.parking_place.utils.HttpRequestAndResponseType;
import com.rmj.parking_place.utils.JsonLoader;
import com.rmj.parking_place.utils.NavigationResponse;
import com.rmj.parking_place.utils.NavigationTask;
import com.rmj.parking_place.utils.Response;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.rmj.parking_place.dto.ReservationDTO;


public class MapFragment extends Fragment implements LocationListener, OnMapReadyCallback, AsyncResponse {

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    private static final String NAVIGATION_SERVICE_BASE_URL = "http://www.yournavigation.org/api/1.0/gosmore.php";
    private static HashMap<FromTo, NavigationDTO> navigationCash = new HashMap<FromTo, NavigationDTO>();

    private LocationManager locationManager;
    private String provider;
    private SupportMapFragment mMapFragment;
    private AlertDialog dialog;
    private Location currentLocation;
    // private Marker currentLocationMarker;
    private GoogleMap map;

    private HashMap<com.rmj.parking_place.model.Location, Marker> parkingPlaceMarkers;
    private HashMap<com.rmj.parking_place.model.Location, ParkingPlace> parkingPlaces;

    private ParkingPlace foundedParkingPlaceNearby;

    private ParkingPlace selectedParkingPlace;
    private Marker selectedParkingPlaceMarker;
    private Polyline navigationPathPolyline;

    private PaidParkingPlace paidParkingPlace;
    private Reservation reservation;


    private static final int MAX_ALLOWED_DISTANCE_FOR_RESERVATION = 5000; // meters

    private static HashMap<String, Integer> markerIcons;
    static {
        markerIcons = new HashMap<String, Integer>();
        markerIcons.put("CURRENT_LOCATION", R.drawable.round_my_location_blue_24);
        markerIcons.put("EMPTY", R.drawable.round_directions_car_green_36);
        markerIcons.put("RESERVED", R.drawable.round_directions_car_orange_36);
        markerIcons.put("TAKEN", R.drawable.round_directions_car_red_36);
        markerIcons.put("SELECTED", R.drawable.round_directions_car_purple_36);
    }

    private boolean drawingFinished;
    private boolean dialogAllowUserLocationWasDisplayed;

    /*public static MapFragment newInstance() {

        MapFragment mpf = new MapFragment();

        return mpf;
    }*/

    public MapFragment() {
        this.drawingFinished = false;
        this.dialogAllowUserLocationWasDisplayed = false;
    }

    /**
     * Prilikom kreiranja fragmenta preuzimamo sistemski servis za rad sa lokacijama
     * */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        ParkingPlaceInfoFragment plf = ParkingPlaceInfoFragment.newInstance();
        FindParkingFragment fpf = FindParkingFragment.newInstance();

        FragmentManager fm = getFragmentManager();
        fm.beginTransaction().replace(R.id.place_info_frame, plf, "parkingPlaceInfoFragment").commit();
        fm.beginTransaction().replace(R.id.find_parking_frame, fpf, "findParkinfFragment").commit();
    }

    /**
     * Kada zelmo da dobijamo informacije o lokaciji potrebno je da specificiramo
     * po kom kriterijumu zelimo da dobijamo informacije GSP, MOBILNO(WIFI, MObilni internet), GPS+MOBILNO
     * **/
    private void createMapFragmentAndInflate() {
        setProvider();

        //kreiramo novu instancu fragmenta
        mMapFragment = SupportMapFragment.newInstance();

        //i vrsimo zamenu trenutnog prikaza sa prikazom mape
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.map_container, mMapFragment).commit();

        //pozivamo ucitavnje mape.
        //VODITI RACUNA OVO JE ASINHRONA OPERACIJA
        //LOKACIJE MOGU DA SE DOBIJU PRE MAPE I OBRATNO
        mMapFragment.getMapAsync(this);
    }


    private void showLocatonDialog() {
        if (dialog == null) {
            dialog = new LocationDialog(getActivity()).prepareDialog();
        } else {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }

        dialog.show();
    }

    @Override
    public void onResume() {
        super.onResume();

        createMapFragmentAndInflate();

        if (checkLocationPermission()) {
            boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean wifi = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (!gps && !wifi) {
                showLocatonDialog();
            }
            else if (ContextCompat.checkSelfPermission(getContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                //Request location updates:
                locationManager.requestLocationUpdates(provider, 0, 0, this);
                Toast.makeText(getContext(), "ACCESS_FINE_LOCATION", Toast.LENGTH_SHORT).show();
            }else if(ContextCompat.checkSelfPermission(getContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){

                //Request location updates:
                locationManager.requestLocationUpdates(provider, 0, 0, this);
                Toast.makeText(getContext(), "ACCESS_COARSE_LOCATION", Toast.LENGTH_SHORT).show();
            }

            if (currentLocation != null) {
                updateCameraPosition(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));
            }
        }

    }

    private void setProvider() {
        //specificiramo krijterijum da dobijamo informacije sa svih izvora
        //ako korisnik to dopusti
        Criteria criteria = new Criteria();

        //sistemskom servisu prosledjujemo taj kriterijum da bi
        //mogli da dobijamo informacje sa tog izvora
        provider = locationManager.getBestProvider(criteria, true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup vg, Bundle data) {
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.map_layout, vg, false);

        return view;
    }

    /**
     * Svaki put kada uredjaj dobijee novu informaciju o lokaciji ova metoda se poziva
     * i prosledjuje joj se nova informacija o kordinatamad
     * */
    @Override
    public void onLocationChanged(Location location) {
        //----------------------------
        //double homeLatitude =  45.292943; // 45.293034;
        //double homeLongitude = 19.819809; // 19.819853;
        //location.setLatitude(homeLatitude);
        //location.setLongitude(homeLongitude);
        //-----------------------------

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
                updateCameraPosition(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));

                Toast.makeText(getContext(), "Ponovo imamo vasu lokaciju!", Toast.LENGTH_SHORT).show();
            }
        }

        /*if (currentLocation.hasSpeed()) {
            ((MapActivity) this.getActivity()).setNoneMode();
        }*/

        // Toast.makeText(getActivity(), "NEW LOCATION", Toast.LENGTH_SHORT).show();
        /*if (map != null) {
            if (currentLocationMarker != null) {
                currentLocationMarker.setPosition(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));
            }
            else {
                currentLocationMarker = addMarker(currentLocation, "CURRENT_LOCATION");
                updateCameraPosition(currentLocationMarker.getPosition());
            }
        }*/

        tryToFindEmptyParkingPlaceNearbyAndSetMode();
    }

    public void tryToFindEmptyParkingPlaceNearbyAndSetMode() {
        MapActivity mapActivity = (MapActivity) this.getActivity();

        if (!mapActivity.isInIsTakingMode()) {
            foundedParkingPlaceNearby = tryToFindEmptyParkingPlaceNearby(currentLocation);

            if (foundedParkingPlaceNearby != null) {
                if (mapActivity.isInCanReserveMode()) {
                    if (selectedParkingPlace == null) {
                        throw new NotFoundParkingPlaceException("selectedParkingPlace == null");
                    }
                    else {
                        mapActivity.setCanReserveAndCanTakeMode();
                    }
                }
                else if (mapActivity.isInIsReservingMode()) {
                    if (selectedParkingPlace == null) {
                        throw new NotFoundParkingPlaceException("selectedParkingPlace == null");
                    }
                    else if (foundedParkingPlaceNearby.equals(selectedParkingPlace)) {
                        mapActivity.setIsReservingAndCanTakeMode();
                    }
                    else {
                        Toast.makeText(getActivity(), "As long as you have a parking place reserved,"
                                + " you cannot take another parking place.", Toast.LENGTH_SHORT).show();
                    }
                }
                else if (mapActivity.isInIsReservingAndCanTakeMode()) {
                    if (!foundedParkingPlaceNearby.equals(selectedParkingPlace)) {
                        mapActivity.setIsReservingMode();
                    }
                }
                else {
                    mapActivity.setCanTakeMode();
                }
            }
            else {
                if (mapActivity.isInIsReservingAndCanTakeMode()) {
                    mapActivity.setIsReservingMode();
                }
                else if (mapActivity.isInCanReserveAndCanTakeMode()) {
                    mapActivity.setCanReserveMode();
                }
                else if (mapActivity.isInCanTakeMode()) {
                    mapActivity.setNoneMode();
                }
            }
        }
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

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?

            // if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
               if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(getActivity())
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
                    if (ContextCompat.checkSelfPermission(getActivity(),
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (provider == null) {
                            setProvider();
                        }
                        //Request location updates:
                        locationManager.requestLocationUpdates(provider, 0, 0, this);
                    }

                } else if (grantResults.length > 0
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED){

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    if (ContextCompat.checkSelfPermission(getActivity(),
                            Manifest.permission.ACCESS_COARSE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (provider == null) {
                            setProvider();
                        }
                        //Request location updates:
                        locationManager.requestLocationUpdates(provider, 0, 0, this);
                    }

                }
                else if (dialogAllowUserLocationWasDisplayed
                        && !shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    getActivity().finish();
                }
            }

        }
    }


    /**
     * KAda je mapa spremna mozemo da radimo sa njom.
     * Mozemo reagovati na razne dogadjaje dodavanje markera, pomeranje markera,klik na mapu,...
     * */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        currentLocation = null;

        if (checkLocationPermission()) {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {

                //Request location updates:
                currentLocation = locationManager.getLastKnownLocation(provider);

                map.setMyLocationEnabled(true);
            }

            /*if (currentLocation != null) {
                double homeLatitude = 45.293034;
                double homeLongitude = 19.819853;
                currentLocation.setLatitude(homeLatitude);
                currentLocation.setLongitude(homeLongitude);
            }*/
        }

        final MapActivity mapActivity = (MapActivity) getActivity();

        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                //Toast.makeText(getActivity(), latLng.toString(), Toast.LENGTH_SHORT).show();
                mapActivity.setClickedLocation(latLng);
                boolean canReserveMode = mapActivity.isInCanReserveMode();
                boolean canReserveAndCanTakeMode = mapActivity.isInCanReserveAndCanTakeMode();

                if (canReserveMode || canReserveAndCanTakeMode) {
                    if (canReserveMode) {
                        mapActivity.setNoneMode();
                    }
                    else if (canReserveAndCanTakeMode) {
                        mapActivity.setCanTakeMode();
                    }
                }

                if (selectedParkingPlace != null) {
                    String markerIcon = selectedParkingPlace.getStatus().name();
                    updateParkingPlaceMarker(selectedParkingPlaceMarker, markerIcon);
                    selectedParkingPlace = null;
                    selectedParkingPlaceMarker = null;
                }

                if(selectedParkingPlace == null){
                    mapActivity.hidePlaceInfoFragmet();
                }
            }
        });

        //ako zelmo da reagujemo na klik markera koristimo marker click listener
        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                /*if (currentLocationMarker != null && marker.equals(currentLocationMarker)) {
                    return  true;
                }*/

                ParkingPlace oldSelectedParkingPlace = selectedParkingPlace;
                Marker oldSelectedParkingPlaceMarker = selectedParkingPlaceMarker;

                LatLng position = marker.getPosition();
                selectedParkingPlace = getParkingPlace(position.latitude, position.longitude);
                if (selectedParkingPlace == null) {
                    return true;
                }


                if (oldSelectedParkingPlace != null) {
                    String markerIcon = oldSelectedParkingPlace.getStatus().name();
                    int resourceId = markerIcons.get(markerIcon);
                    oldSelectedParkingPlaceMarker.setIcon(BitmapDescriptorFactory.fromResource(resourceId));
                }

                // String markerIcon = "SELECTED_" + selectedParkingPlace.getStatus().name();
                String markerIcon = "SELECTED";
                updateParkingPlaceMarker(marker, markerIcon);
                selectedParkingPlaceMarker = marker;

                MapActivity mapActivity = (MapActivity) getActivity();

                //izracunata je razdaljina vazdusne linije
                float distanceMarkerCurrentLocation = computeDistanceBetweenTwoPoints(selectedParkingPlace.getLocation().getLatitude(),
                        selectedParkingPlace.getLocation().getLongitude(), currentLocation.getLatitude(), currentLocation.getLongitude());

                mapActivity.showPlaceInfoFragment(selectedParkingPlace, distanceMarkerCurrentLocation);

                if (mapActivity.isInNoneMode()) {
                    mapActivity.setCanReserveMode();
                }
                else if (mapActivity.isInCanTakeMode()) {
                    if (foundedParkingPlaceNearby.equals(selectedParkingPlace)) {
                        Toast.makeText(getActivity(), "Ovo mesto ne mozete rezervisati, ali mozete zauzeti!",
                                Toast.LENGTH_SHORT).show();
                    }
                    else {
                        mapActivity.setCanReserveAndCanTakeMode();
                    }
                }

                Toast.makeText(getActivity(), marker.getTitle(), Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        if (currentLocation != null) {
            // currentLocationMarker = addMarker(currentLocation, "CURRENT_LOCATION");
            // updateCameraPosition(currentLocationMarker.getPosition());
            updateCameraPosition(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));
        }

        drawParkingPlaceMarkersIfCan();
        if (mapActivity.isInIsReservingMode() || mapActivity.isInCanReserveAndCanTakeMode()) {
            redrawNavigationPath();
        }

    }

    public void updateCameraPosition(LatLng position) {
        if(map != null) {
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(position).zoom(15).build();
            map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
    }

    private ParkingPlace getParkingPlace(double latitude, double longitude) {
        com.rmj.parking_place.model.Location location = new com.rmj.parking_place.model.Location(latitude, longitude);
        return parkingPlaces.get(location);
    }

    private HashMap<com.rmj.parking_place.model.Location, Marker> addParkingPlaceMarkers(Collection<ParkingPlace> parkingPlaces) {
        HashMap<com.rmj.parking_place.model.Location, Marker> markers =
                new HashMap<com.rmj.parking_place.model.Location, Marker>();

        Marker marker;
        for (ParkingPlace parkingPlace : parkingPlaces) {
            marker = addMarker(parkingPlace.getLocation(), parkingPlace.getStatus().name());
            markers.put(parkingPlace.getLocation(), marker);
        }
        return markers;
    }

    private Marker addMarker(Location location, String markerIcon) {
        LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());

        Marker marker = addMarker(loc, markerIcon);
        return marker;
    }

    private Marker addMarker(com.rmj.parking_place.model.Location location, String markerIcon) {
        LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());

        Marker marker = addMarker(loc, markerIcon);
        return marker;
    }

    private Marker addMarker(LatLng loc, String markerIcon) {
        int resourceId = markerIcons.get(markerIcon);

        Marker marker = map.addMarker(
                new MarkerOptions()
                    .title("YOUR_POSITON")
                    .anchor(0.5f,0.5f) // centriramo marker na odgovarajuce koordinate
                    .icon(BitmapDescriptorFactory.fromResource(resourceId))
                    .position(loc)
        );
        marker.setFlat(true);

        return marker;
    }

    private void removeMarkers(List<Marker> markers) {
        for (Marker marker : markers) {
            marker.remove();
        }
    }

    /**
     *
     * Rad sa lokacja izuzetno trosi bateriju.Obavezno osloboditi kada vise ne koristmo
     * */
    @Override
    public void onPause() {
        super.onPause();

        locationManager.removeUpdates(this);
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
        if (parkingPlaces == null) {
            return null;
        }

        for (ParkingPlace parkingPlace : parkingPlaces.values()) {
            if (parkingPlace.getStatus() == ParkingPlaceStatus.EMPTY && parkingPlaceIsNearby(parkingPlace, location)) {
                return  parkingPlace;
            }
        }

        return null;
    }

    private boolean parkingPlaceIsNearby(ParkingPlace parkingPlace, Location location) {
       float distance = computeDistanceBetweenTwoPoints(parkingPlace.getLocation().getLatitude(),
                        parkingPlace.getLocation().getLongitude(), location.getLatitude(), location.getLongitude());
       // distance (meteres)
       return distance < 0.5;
    }

    private void updateParkingPlaceMarker(Marker marker, String newMarkerIcon)
            throws NotFoundParkingPlaceException {
        // Marker marker = getParkingPlaceMarker(latitude, longitude);

        if (marker == null) {
            throw new NotFoundParkingPlaceException("marker == null (updateParkingPlaceMarker method)");
        }

        int resourceId = markerIcons.get(newMarkerIcon);
        marker.setIcon(BitmapDescriptorFactory.fromResource(resourceId));

    }

    private Marker getParkingPlaceMarker(double latitude, double longitude) throws NotFoundParkingPlaceException {
        com.rmj.parking_place.model.Location location = new com.rmj.parking_place.model.Location(latitude, longitude);
        Marker marker = parkingPlaceMarkers.get(location);

        if (marker == null) {
            throw new NotFoundParkingPlaceException("Not found parking place marker (latitude=" + latitude
                    + ", longitude=" + longitude + ")");
        }

        return marker;
    }

    public ReservationDTO checkAndPrepareDtoForReservingOnServer() throws AlreadyTakenParkingPlaceException, AlreadyReservedParkingPlaceException, NotFoundParkingPlaceException, CurrentLocationUnknownException, MaxAllowedDistanceForReservationException {
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

        return new ReservationDTO(selectedParkingPlace.getZone().getId(), selectedParkingPlace.getId());
    }

    public void reserveParkingPlace() {
        ParkingPlaceStatus newParkingPlaceStatus = ParkingPlaceStatus.RESERVED;
        selectedParkingPlace.setStatus(newParkingPlaceStatus);
        reservation = new Reservation(selectedParkingPlace);
        updateParkingPlaceMarker(selectedParkingPlaceMarker, newParkingPlaceStatus.name());

        FromTo fromTo = new FromTo(new com.rmj.parking_place.model.Location(currentLocation.getLatitude(),
                                                                            currentLocation.getLongitude()),
                                                                            selectedParkingPlace.getLocation());
        String url = getNavigationUrl(fromTo);
        new NavigationTask(this, fromTo).execute(url, HttpRequestAndResponseType.NAVIGATION.name());

        final MapActivity mapActivity = (MapActivity) getActivity();
        mapActivity.startTimerForReservationOrTakingOfParkingPlace(reservation.getEndDateTime().getTime(), true);
    }

    private String getNavigationUrl(FromTo fromTo) {
        String url = NAVIGATION_SERVICE_BASE_URL
                + "?flat=" + fromTo.getFrom().getLatitude() + "&flon=" + fromTo.getFrom().getLongitude()
                + "&tlat=" + + fromTo.getTo().getLatitude() + "&tlon=" + fromTo.getTo().getLongitude()
                + "&format=geojson";
        return url;
    }

    public void takeParkingPlace() {
        ParkingPlaceStatus newParkingPlaceStatus = ParkingPlaceStatus.TAKEN;
        foundedParkingPlaceNearby.setStatus(newParkingPlaceStatus);
        paidParkingPlace = new PaidParkingPlace(foundedParkingPlaceNearby);
        Marker marker = getParkingPlaceMarker(foundedParkingPlaceNearby.getLocation().getLatitude(),
                foundedParkingPlaceNearby.getLocation().getLongitude());
        updateParkingPlaceMarker(marker, newParkingPlaceStatus.name());

        Date endDateTime = paidParkingPlace.getEndDateTime();
        long endDateTimeInMillis = endDateTime.getTime();

        final MapActivity mapActivity = (MapActivity) getActivity();
        mapActivity.startTimerForReservationOrTakingOfParkingPlace(endDateTimeInMillis, false);
    }

    public TakingDTO checkAndPrepareDtoForTakingOnServer() throws NotFoundParkingPlaceException, AlreadyReservedParkingPlaceException, AlreadyTakenParkingPlaceException {
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

        return new TakingDTO(selectedParkingPlace.getZone().getId(), selectedParkingPlace.getId());
    }

    private void addViolationToUser(boolean reservation) {
        // TODO
        // reservation or taking VIOLATION
    }

    public void finishReservationOfParkingPlace() {
        removeNavigationPathPolyline();

        if (selectedParkingPlace == null) {
            throw new NotFoundParkingPlaceException("selectedParkingPlace == null");
        }

        ParkingPlaceStatus newParkingPlaceStatus = ParkingPlaceStatus.EMPTY;
        selectedParkingPlace.setStatus(newParkingPlaceStatus);
//        Marker marker = getParkingPlaceMarker(selectedParkingPlace.getLocation().getLatitude(),
//                selectedParkingPlace.getLocation().getLongitude());
        selectedParkingPlace = null;

        String markerIcon = newParkingPlaceStatus.name();
        updateParkingPlaceMarker(selectedParkingPlaceMarker, markerIcon);
        selectedParkingPlaceMarker = null;
    }

    public void finishTakingOfParkingPlace() {
        if (foundedParkingPlaceNearby == null) {
            throw new NotFoundParkingPlaceException("foundedParkingPlaceNearby == null");
        }

        ParkingPlaceStatus newParkingPlaceStatus = ParkingPlaceStatus.EMPTY;
        foundedParkingPlaceNearby.setStatus(newParkingPlaceStatus);
        Marker marker = getParkingPlaceMarker(foundedParkingPlaceNearby.getLocation().getLatitude(),
                foundedParkingPlaceNearby.getLocation().getLongitude());
        foundedParkingPlaceNearby = null;

        String markerIcon = newParkingPlaceStatus.name();
        int resourceId = markerIcons.get(markerIcon);
        marker.setIcon(BitmapDescriptorFactory.fromResource(resourceId));
    }

    public void checkSelectedParkingPlaceAndSetMode() {
        if (selectedParkingPlace != null) {
            ((MapActivity) getActivity()).setCanReserveMode();
        }
    }

    /*
    * Za ovu metodu se koristi 'synchronized' jer moze biti pozvana ili kad se downloaduju podaci ili kad je mapa spremna
    * */
    public synchronized void drawParkingPlaceMarkersIfCan() {
        if (map != null && parkingPlaces != null && !drawingFinished) {
            parkingPlaceMarkers = addParkingPlaceMarkers(parkingPlaces.values());
            drawingFinished = true;
        }

        /*double homeLatitude = 45.293034;
        double homeLongitude = 19.819853;
        double centerLatitude = 45.254879;
        double centerLongitude = 19.842181;
        float distance = computeDistanceBetweenTwoPoints(homeLatitude, homeLongitude, centerLatitude, centerLongitude);
        Toast.makeText(this.getActivity(), "distance = " + distance,Toast.LENGTH_SHORT).show();
        System.out.println("distance = " + distance);*/

        //Getting both the coordinates
//        LatLng from = new LatLng(homeLatitude,homeLongitude);
//        LatLng to = new LatLng(centerLatitude,centerLongitude);

        //Calculating the distance in meters
//        double realDistance = SphericalUtil.computeDistanceBetween(from, to);
//        System.out.println("realDistance = " + realDistance);
    }

    public void setParkingPlaces(HashMap<com.rmj.parking_place.model.Location, ParkingPlace> parkingPlaces) {
        this.parkingPlaces = parkingPlaces;
    }

    public HashMap<com.rmj.parking_place.model.Location, ParkingPlace> getParkingPlaces() {
        return this.parkingPlaces;
    }

    @Override
    public void processFinish(Response response) {
        if (response.getType() == HttpRequestAndResponseType.NAVIGATION) {
            NavigationResponse navigationResponse = (NavigationResponse) response;
            FromTo fromTo = navigationResponse.getFromTo();
            NavigationDTO navigationDTO = JsonLoader.convertJsonToNavigationDTO(navigationResponse.getResult());
            navigationCash.put(fromTo, navigationDTO);

            drawNavigationPath(navigationDTO.getCoordinates());
        }
    }

    @Override
    public void loginAgain() {
        ((MapActivity) getActivity()).loginAgain();
    }

    private void redrawNavigationPath() {
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
        PolylineOptions lineOptions = new PolylineOptions();
        lineOptions.addAll(points);
        lineOptions.width(12);
        lineOptions.color(Color.rgb(0, 102, 204)); // blue
        lineOptions.geodesic(true);

        // Drawing polyline in the Google Map
        navigationPathPolyline = map.addPolyline(lineOptions);
        updateCameraPosition(points.get(points.size() / 2));
    }

    private void removeNavigationPathPolyline() {
        if (navigationPathPolyline != null) {
            navigationPathPolyline.remove();
            navigationPathPolyline = null;
        }
    }

    public void updateParkingPlaceMarkers(List<ParkingPlace> parkingPlacesForUpdating) {
        Marker marker;
        String markerIcon;

        for (ParkingPlace parkingPlace : parkingPlacesForUpdating) {
            marker = parkingPlaceMarkers.get(parkingPlace.getLocation());
            markerIcon = parkingPlace.getStatus().name();
            updateParkingPlaceMarker(marker, markerIcon);
        }
    }

    public void resetDrawingFinished() {
        this.drawingFinished = false;
    }
}