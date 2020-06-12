package com.rmj.parking_place.actvities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.Menu;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.rmj.parking_place.App;
import com.rmj.parking_place.R;
import com.rmj.parking_place.actvities.login.ui.LoginActivity;
import com.rmj.parking_place.database.ZoneRepository;
import com.rmj.parking_place.dto.PaidParkingPlaceDTO;
import com.rmj.parking_place.dto.ReservationAndPaidParkingPlacesDTO;
import com.rmj.parking_place.dto.ReservationDTO;
import com.rmj.parking_place.fragments.favorite_places.FavoritePlacesFragment;
import com.rmj.parking_place.model.FavoritePlace;
import com.rmj.parking_place.model.Location;
import com.rmj.parking_place.model.PaidParkingPlace;
import com.rmj.parking_place.model.ParkingPlace;
import com.rmj.parking_place.model.Reservation;
import com.rmj.parking_place.model.TicketType;
import com.rmj.parking_place.model.Zone;
import com.rmj.parking_place.service.ParkingPlaceServerUtils;
import com.rmj.parking_place.utils.TokenUtils;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationManagerCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.widget.Toolbar;
import androidx.room.Room;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends /*AppCompatActivity*/ CheckWifiActivity
                            implements FavoritePlacesFragment.OnListFragmentInteractionListener {

    private AppBarConfiguration mAppBarConfiguration;
    private NavController navController;

    private ArrayList<FavoritePlace> favoritePlaces;

    private Reservation reservation;
    private PaidParkingPlace regularPaidParkingPlace;
    private List<PaidParkingPlace> paidParkingPlacesForFavoritePlaces;

    private static ZoneRepository zoneRepository;
    private List<Zone> zones;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            zoneRepository = new ZoneRepository(this);

            getZonesFromDB();
            downloadFavoritePlaces();
        }
        else {
            zones = savedInstanceState.getParcelableArrayList("zones");
            if (zones == null) {
                zones = new ArrayList<Zone>();
            }

            reservation = savedInstanceState.getParcelable("reservation");

            regularPaidParkingPlace = savedInstanceState.getParcelable("regularPaidParkingPlace");

            paidParkingPlacesForFavoritePlaces =
                    savedInstanceState.getParcelableArrayList("paidParkingPlacesForFavoritePlaces");
            if (paidParkingPlacesForFavoritePlaces != null) {
                paidParkingPlacesForFavoritePlaces = new ArrayList<PaidParkingPlace>();
            }

            favoritePlaces = savedInstanceState.getParcelableArrayList("favoritePlaces");
            if (favoritePlaces == null) {
                favoritePlaces = new ArrayList<FavoritePlace>();
            }
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        /*FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/
        DrawerLayout drawer = findViewById(R.id.drawer_layout);

        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_reports, R.id.nav_map_page, R.id.nav_favorite_places,
                                                                    R.id.nav_reservation_and_paid_parking_places)
                .setDrawerLayout(drawer)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }

    private void getZonesFromDB() {
        new AsyncTask<Void, Void, Object>() {

            @Override
            protected Object doInBackground(Void... voids) {
                zones = zoneRepository.getZones();
                if (zones == null || (zones != null && zones.isEmpty())) {
                    downloadZones(true);
                    // unutar downloadZones bice pozvan downloadReservationAndPaidParkingPlaces
                }
                else {
                    LatLngBounds bounds;
                    for (Zone zone : zones) {
                        bounds = makeLatLngBoundsMapboxsdk(zone.getNorthEast(), zone.getSouthWest());
                        zone.setBounds(bounds);
                    }
                    downloadReservationAndPaidParkingPlaces();
                }
                return null;
            }
        }.execute();
    }

    public void selectDrawerItem(int buttonId) {
        int fragmentId = -1;

        switch(buttonId) {
            case R.id.mapPageBtn:
                fragmentId = R.id.nav_map_page;
                break;
            case R.id.reservationAndPaidParkingPlacesBtn:
                fragmentId = R.id.nav_reservation_and_paid_parking_places;
                break;
            case R.id.favoritePlacesBtn:
                fragmentId = R.id.nav_favorite_places;
                break;
            case R.id.registerUserBtn:
                fragmentId = -1;
                break;
            default:
                Toast.makeText(this, "Not found fragment", Toast.LENGTH_SHORT).show();
                break;
        }


        if (fragmentId == -1) {
            Intent intent = new Intent(MainActivity.this, RegistrationActivity.class);
            startActivity(intent);
        }
        else {
            navController.navigate(fragmentId);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        if (zones != null) {
            outState.putParcelableArrayList("zones", (ArrayList<Zone>) zones);
        }

        if (reservation != null) {
            outState.putParcelable("reservation", reservation);
        }

        if (regularPaidParkingPlace != null) {
            outState.putParcelable("regularPaidParkingPlace", regularPaidParkingPlace);
        }

        if (paidParkingPlacesForFavoritePlaces != null) {
            outState.putParcelableArrayList("paidParkingPlacesForFavoritePlaces",
                    (ArrayList<PaidParkingPlace>) paidParkingPlacesForFavoritePlaces);
        }

        if (favoritePlaces != null) {
            outState.putParcelableArrayList("favoritePlaces", favoritePlaces);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public void downloadZones(boolean reservationAndPaidParkingPlacesDownloadOnFinish) {
        /*InputStream is = getResources().openRawResource(R.raw.zones_with_parking_places);
        List<Zone> zones = JsonLoader.getZones(is);
        -------------------------------------------------
        new RequestAsyncTask(this).execute("GET", "https://192.168.1.12:45455/api/zones");*/

        ParkingPlaceServerUtils.zoneService.getZones()
                .enqueue(new Callback<List<Zone>>() {
                    @Override
                    public void onResponse(Call<List<Zone>> call, retrofit2.Response<List<Zone>> response) {
                        if (response == null || (response != null && !response.isSuccessful())) {
                            Toast.makeText(MainActivity.this, "Problem with loading zones. We will try again.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        else {
                            zones = response.body();
                            if (zones == null) {
                                zones = new ArrayList<Zone>();
                            }

                            if (!zones.isEmpty()) {
                                LatLngBounds bounds;
                                for (Zone zone : zones) {
                                    bounds = makeLatLngBoundsMapboxsdk(zone.getNorthEast(), zone.getSouthWest());
                                    zone.setBounds(bounds);
                                }

                                new AsyncTask<Object, Void, Void>() {
                                    @Override
                                    protected Void doInBackground(Object[] objects) {
                                        zoneRepository.insertZones(zones);
                                        return null;
                                    }
                                }.execute();
                                if (reservationAndPaidParkingPlacesDownloadOnFinish){
                                    downloadReservationAndPaidParkingPlaces();
                                }
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Zone>> call, Throwable t) {
                        Toast.makeText(MainActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        /*new GetRequestAsyncTask(this).execute(App.getParkingPlaceServerUrl() + "/api/zones",
                HttpRequestAndResponseType.GET_ZONES.name(), TokenUtils.getToken());*/
    }

    private LatLngBounds makeLatLngBoundsMapboxsdk(Location northEast, Location southWest) {
        return new LatLngBounds.Builder()
                .include(new LatLng(northEast.getLatitude(), northEast.getLongitude()))
                .include(new LatLng(southWest.getLatitude(), southWest.getLongitude()))
                .build();
    }

    private void downloadFavoritePlaces() {
       ParkingPlaceServerUtils.userService.getFavoritePlaces()
               .enqueue(new Callback<ArrayList<FavoritePlace>>() {
                   @Override
                   public void onResponse(Call<ArrayList<FavoritePlace>> call, Response<ArrayList<FavoritePlace>> response) {
                       if (response.isSuccessful()) {
                           favoritePlaces = response.body();
                           if (favoritePlaces == null) {
                               favoritePlaces = new ArrayList<FavoritePlace>();
                           }
                       }
                       else if(response.code() == 401) { // Unauthorized
                            App.loginAgain();
                       }
                       else {
                           favoritePlaces = new ArrayList<FavoritePlace>();
                       }
                   }

                   @Override
                   public void onFailure(Call<ArrayList<FavoritePlace>> call, Throwable t) {
                       Toast.makeText(MainActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                       favoritePlaces = new ArrayList<FavoritePlace>();
                   }
               });
    }

    private void downloadReservationAndPaidParkingPlaces() {
        ParkingPlaceServerUtils.userService.getReservationAndPaidParkingPlaces()
                .enqueue(new Callback<ReservationAndPaidParkingPlacesDTO>() {
                    @Override
                    public void onResponse(Call<ReservationAndPaidParkingPlacesDTO> call, Response<ReservationAndPaidParkingPlacesDTO> response) {
                        if (response.isSuccessful()) {
                            ReservationAndPaidParkingPlacesDTO reservationAndPaidParkingPlacesDTO = response.body();
                            if (reservationAndPaidParkingPlacesDTO == null) {
                                reservation = null;
                                regularPaidParkingPlace = null;
                                paidParkingPlacesForFavoritePlaces = new ArrayList<PaidParkingPlace>();
                            }
                            else {
                                ParkingPlace parkingPlace;
                                ReservationDTO reservationDTO = reservationAndPaidParkingPlacesDTO.getReservation();
                                if (reservationDTO != null) {
                                    parkingPlace = reservationDTO.getParkingPlace();
                                    parkingPlace.setZone(getZone(reservationDTO.getZoneId()));
                                    reservation = new Reservation(reservationDTO.getId(), reservationDTO.getStartDateTimeAndroid(),
                                                                            reservationDTO.getStartDateTimeServer(), parkingPlace);
                                }

                                PaidParkingPlaceDTO regularPaidParkingPlaceDTO = reservationAndPaidParkingPlacesDTO
                                                                                                    .getRegularPaidParkingPlace();
                                if (regularPaidParkingPlaceDTO != null) {
                                    parkingPlace = regularPaidParkingPlaceDTO.getParkingPlace();
                                    parkingPlace.setZone(getZone(regularPaidParkingPlaceDTO.getZoneId()));
                                    regularPaidParkingPlace = new PaidParkingPlace(regularPaidParkingPlaceDTO.getId(), parkingPlace,
                                        regularPaidParkingPlaceDTO.getStartDateTimeAndroid(), regularPaidParkingPlaceDTO.getStartDateTimeServer(),
                                        regularPaidParkingPlaceDTO.getTicketType(), regularPaidParkingPlaceDTO.isArrogantUser());
                                }

                                List<PaidParkingPlaceDTO> paidParkingPlacesForFavoritePlaceDTOs = reservationAndPaidParkingPlacesDTO.getPaidParkingPlacesForFavoritePlaces();
                                paidParkingPlacesForFavoritePlaces = new ArrayList<PaidParkingPlace>();
                                if (paidParkingPlacesForFavoritePlaceDTOs != null && !paidParkingPlacesForFavoritePlaceDTOs.isEmpty()) {
                                    for(PaidParkingPlaceDTO paidParkingPlaceDTO : paidParkingPlacesForFavoritePlaceDTOs) {
                                        parkingPlace = paidParkingPlaceDTO.getParkingPlace();
                                        parkingPlace.setZone(getZone(regularPaidParkingPlaceDTO.getZoneId()));
                                        paidParkingPlacesForFavoritePlaces.add(new PaidParkingPlace(paidParkingPlaceDTO.getId(), parkingPlace,
                                                paidParkingPlaceDTO.getStartDateTimeAndroid(), paidParkingPlaceDTO.getStartDateTimeServer(),
                                                paidParkingPlaceDTO.getTicketType(), paidParkingPlaceDTO.isArrogantUser()));
                                    }
                                }
                            }
                        }
                        else if(response.code() == 401) { // Unauthorized
                            App.loginAgain();
                        }
                        else {
                            reservation = null;
                            regularPaidParkingPlace = null;
                            paidParkingPlacesForFavoritePlaces = new ArrayList<PaidParkingPlace>();
                        }
                    }

                    @Override
                    public void onFailure(Call<ReservationAndPaidParkingPlacesDTO> call, Throwable t) {
                        Toast.makeText(MainActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                        reservation = null;
                        regularPaidParkingPlace = null;
                        paidParkingPlacesForFavoritePlaces = new ArrayList<PaidParkingPlace>();
                    }
                });
    }

    public Zone getZone(Long zoneId) {
        if (zones == null || (zones != null && zones.isEmpty())) {
            return null;
        }
        else{
            for (Zone zone : zones) {
                if (zone.getId().equals(zoneId)) {
                    return zone;
                }
            }
            return null;
        }
    }


    /*public void loginAgain() {
        String currentActivity = App.getCurrentActivityName();
        if (currentActivity.endsWith("LoginActivity")) { // vec se nalazi na login activity-ju
            return;
        }

        TokenUtils.removeToken();
        startActivity(new Intent(MainActivity.this, LoginActivity.class));
        Toast.makeText(this, "Please login again.", Toast.LENGTH_SHORT).show();
        finish();
    }*/

    public void clickOnItemLogout(MenuItem item) {
        TokenUtils.removeToken();
        startActivity(new Intent(MainActivity.this, LoginActivity.class));
    }

    public void clickOnItemSettings(MenuItem item) {
        startActivity(new Intent(MainActivity.this, SettingsActivity.class));
    }

    @Override
    public void onListFragmentInteraction(FavoritePlace item) {
        Toast.makeText(this, item.getName(), Toast.LENGTH_SHORT).show();
    }

    public ArrayList<FavoritePlace> getFavoritePlaces() {
        return favoritePlaces;
    }

    public List<Zone> getZones() {
        return zones;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public PaidParkingPlace getRegularPaidParkingPlace() {
        return regularPaidParkingPlace;
    }

    public List<PaidParkingPlace> getPaidParkingPlacesForFavoritePlaces() {
        return paidParkingPlacesForFavoritePlaces;
    }

    public void savePaidParkingPlace(PaidParkingPlace paidParkingPlace) {
        if (paidParkingPlace.getTicketType() == TicketType.REGULAR) {
            regularPaidParkingPlace = paidParkingPlace;
        }
        else {
            if (paidParkingPlacesForFavoritePlaces == null) {
                paidParkingPlacesForFavoritePlaces = new ArrayList<PaidParkingPlace>();
            }
            paidParkingPlacesForFavoritePlaces.add(paidParkingPlace);
        }
    }

    public void saveReservation(Reservation reservation) {
        this.reservation = reservation;
    }

    public void resetZonesIfNeeded() {
        if (zones != null && !zones.isEmpty()) {
            for (Zone zone : zones) {
                if (zone.getVersion().longValue() != -1) {
                    zone.setVersion(-1L);
                    zone.getParkingPlaces().clear();
                }
            }
        }
    }

    public void navigateToHomeFramgent() {
        navController.navigate(R.id.nav_home);
    }

    public void leaveParkingPlace(Long parkingPlaceId) {
        if(regularPaidParkingPlace.getParkingPlace().getId().equals(parkingPlaceId)){
            regularPaidParkingPlace = null;
            return;
        }
        PaidParkingPlace placeForRemove = null;
        for (PaidParkingPlace place : paidParkingPlacesForFavoritePlaces){
            if(place.getParkingPlace().getId().equals(parkingPlaceId)){
                placeForRemove = place;
                break;
            }
        }

        if(placeForRemove != null){
            paidParkingPlacesForFavoritePlaces.remove(placeForRemove);
        }
    }

    public void resetRegularPaidParkingPlace() {
        this.regularPaidParkingPlace = null;
    }

    public void resetReservation() {
        this.reservation = null;
    }

}
