package com.rmj.parking_place.fragments;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
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

// import com.google.maps.android.SphericalUtil;
import com.rmj.parking_place.MainActivity;
import com.rmj.parking_place.R;
import com.rmj.parking_place.dialogs.LocationDialog;
import com.rmj.parking_place.exceptions.AlreadyReservedParkingPlaceException;
import com.rmj.parking_place.exceptions.AlreadyTakenParkingPlaceException;
import com.rmj.parking_place.exceptions.CurrentLocationUnknownException;
import com.rmj.parking_place.exceptions.MaxAllowedDistanceForReservationException;
import com.rmj.parking_place.exceptions.NotFoundParkingPlaceException;
import com.rmj.parking_place.model.Mode;
import com.rmj.parking_place.model.PaidParkingPlace;
import com.rmj.parking_place.model.ParkingPlace;
import com.rmj.parking_place.model.ParkingPlaceStatus;
import com.rmj.parking_place.model.Reservation;
import com.rmj.parking_place.model.Zone;
import com.rmj.parking_place.utils.JsonLoader;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class MapFragment extends Fragment implements LocationListener, OnMapReadyCallback {

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    private LocationManager locationManager;
    private String provider;
    private SupportMapFragment mMapFragment;
    private AlertDialog dialog;
    private Location currentLocation;
    private Marker currentLocationMarker;
    private GoogleMap map;
    private List<Marker> parkingPlaceMarkers;
    private List<Zone> zones;

    private ParkingPlace foundedParkingPlaceNearby;

    private ParkingPlace selectedParkingPlace;
    private Marker selectedParkingPlaceMarker;

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

    public static MapFragment newInstance() {

        MapFragment mpf = new MapFragment();

        return mpf;
    }

    /**
     * Prilikom kreiranja fragmenta preuzimamo sistemski servis za rad sa lokacijama
     * */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
    }

    /**
     * Kada zelmo da dobijamo informacije o lokaciji potrebno je da specificiramo
     * po kom kriterijumu zelimo da dobijamo informacije GSP, MOBILNO(WIFI, MObilni internet), GPS+MOBILNO
     * **/
    private void createMapFragmentAndInflate() {
        //specificiramo krijterijum da dobijamo informacije sa svih izvora
        //ako korisnik to dopusti
        Criteria criteria = new Criteria();

        //sistemskom servisu prosledjujemo taj kriterijum da bi
        //mogli da dobijamo informacje sa tog izvora
        provider = locationManager.getBestProvider(criteria, true);

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

        boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean wifi = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!gps && !wifi) {
            showLocatonDialog();
        } else {
            if (checkLocationPermission()) {
                if (ContextCompat.checkSelfPermission(getContext(),
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
            }
        }

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

        if (currentLocation != null) {
            if (currentLocation.getLatitude() == location.getLatitude()
                    && currentLocation.getLongitude() == location.getLongitude()) {
                return;
            }
        }

        currentLocation = location;

        /*if (currentLocation.hasSpeed()) {
            ((MainActivity) this.getActivity()).setNoneMode();
        }*/

        // Toast.makeText(getActivity(), "NEW LOCATION", Toast.LENGTH_SHORT).show();
        if (map != null) {
            if (currentLocationMarker != null) {
                currentLocationMarker.setPosition(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));
            }
            else {
                currentLocationMarker = addMarker(currentLocation, "CURRENT_LOCATION");
            }
        }

        tryToFindEmptyParkingPlaceNearbyAndSetMode();
    }

    public void tryToFindEmptyParkingPlaceNearbyAndSetMode() {
        MainActivity mainActivity = (MainActivity) this.getActivity();

        if (!mainActivity.isInIsTakingMode()) {
            foundedParkingPlaceNearby = tryToFindEmptyParkingPlaceNearby(currentLocation);

            if (foundedParkingPlaceNearby != null) {
                if (mainActivity.isInCanReserveMode()) {
                    if (selectedParkingPlace == null) {
                        throw new NotFoundParkingPlaceException("selectedParkingPlace == null");
                    }
                    else {
                        mainActivity.setCanReserveAndCanTakeMode();
                    }
                }
                else if (mainActivity.isInIsReservingMode()) {
                    if (selectedParkingPlace == null) {
                        throw new NotFoundParkingPlaceException("selectedParkingPlace == null");
                    }
                    else if (foundedParkingPlaceNearby.equals(selectedParkingPlace)) {
                        mainActivity.setIsReservingAndCanTakeMode();
                    }
                    else {
                        Toast.makeText(getActivity(), "As long as you have a parking place reserved,"
                                + " you cannot take another parking place.", Toast.LENGTH_SHORT).show();
                    }
                }
                else if (mainActivity.isInIsReservingAndCanTakeMode()) {
                    if (!foundedParkingPlaceNearby.equals(selectedParkingPlace)) {
                        mainActivity.setIsReservingMode();
                    }
                }
                else {
                    mainActivity.setCanTakeMode();
                }
            }
            else {
                if (mainActivity.isInIsReservingAndCanTakeMode()) {
                    mainActivity.setIsReservingMode();
                }
                else if (mainActivity.isInCanReserveAndCanTakeMode()) {
                    mainActivity.setCanReserveMode();
                }
                else if (mainActivity.isInCanTakeMode()) {
                    mainActivity.setNoneMode();
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
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

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
                                ActivityCompat.requestPermissions(getActivity(),
                                        new String[]{
                                                Manifest.permission.ACCESS_FINE_LOCATION,
                                                Manifest.permission.ACCESS_COARSE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
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

                        //Request location updates:
                        locationManager.requestLocationUpdates(provider, 0, 0, this);
                    }

                }
                return;
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
            if (ContextCompat.checkSelfPermission(getContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                //Request location updates:
                currentLocation = locationManager.getLastKnownLocation(provider);
            }else if(ContextCompat.checkSelfPermission(getContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){

                //Request location updates:
                currentLocation = locationManager.getLastKnownLocation(provider);
            }

            /*if (currentLocation != null) {
                double homeLatitude = 45.293034;
                double homeLongitude = 19.819853;
                currentLocation.setLatitude(homeLatitude);
                currentLocation.setLongitude(homeLongitude);
            }*/
        }

        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                MainActivity mainActivity = (MainActivity) getActivity();
                boolean canReserveMode = mainActivity.isInCanReserveMode();
                boolean canReserveAndCanTakeMode = mainActivity.isInCanReserveAndCanTakeMode();

                if (canReserveMode || canReserveAndCanTakeMode) {
                    if (canReserveMode) {
                        mainActivity.setNoneMode();
                    }
                    else if (canReserveAndCanTakeMode) {
                        mainActivity.setCanTakeMode();
                    }
                }

                if (selectedParkingPlace != null) {
                    String markerIcon = selectedParkingPlace.getStatus().name();
                    updateParkingPlaceMarker(selectedParkingPlaceMarker, markerIcon);
                    selectedParkingPlace = null;
                    selectedParkingPlaceMarker = null;
                }
            }
        });

        //ako zelmo da reagujemo na klik markera koristimo marker click listener
        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if (currentLocationMarker != null && marker.equals(currentLocationMarker)) {
                    return  true;
                }

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

                MainActivity mainActivity = (MainActivity) getActivity();
                if (mainActivity.isInNoneMode()) {
                    mainActivity.setCanReserveMode();
                }
                else if (mainActivity.isInCanTakeMode()) {
                    if (foundedParkingPlaceNearby.equals(selectedParkingPlace)) {
                        Toast.makeText(getActivity(), "Ovo mesto ne mozete rezervisati, ali mozete zauzeti!",
                                Toast.LENGTH_SHORT).show();
                    }
                    else {
                        mainActivity.setCanReserveAndCanTakeMode();
                    }
                }

                Toast.makeText(getActivity(), marker.getTitle(), Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        if (currentLocation != null) {
            currentLocationMarker = addMarker(currentLocation, "CURRENT_LOCATION");
            updateCameraPosition(currentLocationMarker.getPosition());
        }

        ArrayList<ParkingPlace> parkingPlaces = new ArrayList<ParkingPlace>();
        zones = getZones();
        for (Zone zone : zones) {
            for (ParkingPlace parkingPlace : zone.getParkingPlaces()) {
                parkingPlace.setZone(zone);
            }
            parkingPlaces.addAll(zone.getParkingPlaces());
        }

        parkingPlaceMarkers = addParkingPlaceMarkers(parkingPlaces);

        double homeLatitude = 45.293034;
        double homeLongitude = 19.819853;
        double centerLatitude = 45.254879;
        double centerLongitude = 19.842181;
        float distance = computeDistanceBetweenTwoPoints(homeLatitude, homeLongitude, centerLatitude, centerLongitude);
        Toast.makeText(this.getActivity(), "distance = " + distance,Toast.LENGTH_SHORT).show();
        System.out.println("distance = " + distance);

        //Getting both the coordinates
//        LatLng from = new LatLng(homeLatitude,homeLongitude);
//        LatLng to = new LatLng(centerLatitude,centerLongitude);

        //Calculating the distance in meters
//        double realDistance = SphericalUtil.computeDistanceBetween(from, to);
//        System.out.println("realDistance = " + realDistance);
    }

    private void updateCameraPosition(LatLng position) {
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(position).zoom(20).build();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    private ParkingPlace getParkingPlace(double latitude, double longitude) {
        for (Zone zone : zones) {
            for (ParkingPlace parkingPlace : zone.getParkingPlaces()) {
                if (parkingPlace.hasCoordinates(latitude, longitude)) {
                    return parkingPlace;
                }
            }
        }

        return null;
    }

    private List<Marker> addParkingPlaceMarkers(List<ParkingPlace> parkingPlaces) {
        List<Marker> markers = new ArrayList<Marker>();

        Marker marker;
        for (ParkingPlace parkingPlace : parkingPlaces) {
            marker = addMarker(parkingPlace.getLocation(), parkingPlace.getStatus().name());
            markers.add(marker);
        }
        return markers;
    }

    private List<Zone> getZones() {
        InputStream is = getResources().openRawResource(R.raw.zones_with_parking_places);
        List<Zone> zones = JsonLoader.getZones(is);
        return zones;
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

        Marker marker = map.addMarker(new MarkerOptions()
                .title("YOUR_POSITON")
                .icon(BitmapDescriptorFactory.fromResource(resourceId))
                .position(loc));
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

    private ParkingPlace tryToFindEmptyParkingPlaceNearby(Location location)
            /*throws AlreadyReservedParkingPlaceException, AlreadyTakenParkingPlaceException*/ {
        if (zones == null) {
            return null;
        }

        for (Zone zone : zones) {
            for (ParkingPlace parkingPlace : zone.getParkingPlaces()) {
                if (parkingPlace.getStatus() == ParkingPlaceStatus.EMPTY && parkingPlaceIsNearby(parkingPlace, location)) {
                    /*if (parkingPlace.getStatus() == ParkingPlaceStatus.RESERVED) {
                        throw new AlreadyReservedParkingPlaceException("Parking place (latitude=" + latitude
                                + ", longitude=" + longitude + ") is already reserved");
                    }
                    else if (parkingPlace.getStatus() == ParkingPlaceStatus.TAKEN) {
                        throw new AlreadyTakenParkingPlaceException("Parking place (latitude=" + latitude
                                + ", longitude=" + longitude + ") is already taken");
                    }*/
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
        for (Marker marker : parkingPlaceMarkers) {
            if (marker.getPosition().latitude == latitude && marker.getPosition().longitude == longitude) {
                return marker;
            }
        }

        throw new NotFoundParkingPlaceException("Not found parking place marker (latitude=" + latitude
                + ", longitude=" + longitude + ")");
    }

    public void reserveParkingPlace() throws AlreadyTakenParkingPlaceException, AlreadyReservedParkingPlaceException, NotFoundParkingPlaceException, CurrentLocationUnknownException, MaxAllowedDistanceForReservationException {
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

        ParkingPlaceStatus newParkingPlaceStatus = ParkingPlaceStatus.RESERVED;
        selectedParkingPlace.setStatus(newParkingPlaceStatus);
        reservation = new Reservation(selectedParkingPlace);
        updateParkingPlaceMarker(selectedParkingPlaceMarker, newParkingPlaceStatus.name());

        final MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.startTimerForReservationOrTakingOfParkingPlace(reservation.getEndDateTime().getTime(), true);
    }

    public void takeParkingPlace() throws NotFoundParkingPlaceException, AlreadyReservedParkingPlaceException, AlreadyTakenParkingPlaceException {
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

        ParkingPlaceStatus newParkingPlaceStatus = ParkingPlaceStatus.TAKEN;
        foundedParkingPlaceNearby.setStatus(newParkingPlaceStatus);
        paidParkingPlace = new PaidParkingPlace(foundedParkingPlaceNearby);
        Marker marker = getParkingPlaceMarker(foundedParkingPlaceNearby.getLocation().getLatitude(),
                                            foundedParkingPlaceNearby.getLocation().getLongitude());
        updateParkingPlaceMarker(marker, newParkingPlaceStatus.name());

        Date endDateTime = paidParkingPlace.getEndDateTime();
        long endDateTimeInMillis = endDateTime.getTime();

        final MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.startTimerForReservationOrTakingOfParkingPlace(endDateTimeInMillis, false);
    }

    private void addViolationToUser(boolean reservation) {
        // TODO
        // reservation or taking VIOLATION
    }

    public void finishReservationOfParkingPlace() {
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
            ((MainActivity) getActivity()).setCanReserveMode();
        }
    }
}