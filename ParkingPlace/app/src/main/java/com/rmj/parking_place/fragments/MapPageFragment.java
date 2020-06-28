package com.rmj.parking_place.fragments;

import android.app.Notification;
import android.content.Context;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.transition.Slide;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLngBounds;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.rmj.parking_place.R;
import com.rmj.parking_place.actvities.MainActivity;
import com.rmj.parking_place.database.NotificationDb;
import com.rmj.parking_place.database.NotificationRepository;
import com.rmj.parking_place.dialogs.DialogForMockLocation;
import com.rmj.parking_place.dialogs.DialogForSelectingTicketType;
import com.rmj.parking_place.dialogs.NotificationDialog;
import com.rmj.parking_place.dto.DTO;
import com.rmj.parking_place.dto.PaidParkingPlaceDTO;
import com.rmj.parking_place.dto.ParkingPlaceChangesDTO;
import com.rmj.parking_place.dto.ParkingPlaceDTO;
import com.rmj.parking_place.dto.ParkingPlacesInitialDTO;
import com.rmj.parking_place.dto.ParkingPlacesUpdatingDTO;
import com.rmj.parking_place.dto.ReservationDTO;
import com.rmj.parking_place.dto.ReservingDTO;
import com.rmj.parking_place.dto.TakingDTO;
import com.rmj.parking_place.exceptions.AlreadyReservedParkingPlaceException;
import com.rmj.parking_place.exceptions.AlreadyTakenParkingPlaceException;
import com.rmj.parking_place.exceptions.CurrentLocationUnknownException;
import com.rmj.parking_place.exceptions.InvalidModeException;
import com.rmj.parking_place.exceptions.MaxAllowedDistanceForReservationException;
import com.rmj.parking_place.exceptions.NotFoundParkingPlaceException;
import com.rmj.parking_place.listener.OnCreateViewFinishedListenerImplementation;
import com.rmj.parking_place.model.Location;
import com.rmj.parking_place.model.Mode;
import com.rmj.parking_place.model.PaidParkingPlace;
import com.rmj.parking_place.model.ParkingPlace;
import com.rmj.parking_place.model.ParkingPlaceStatus;
import com.rmj.parking_place.model.Reservation;
import com.rmj.parking_place.model.TicketType;
import com.rmj.parking_place.model.Zone;
import com.rmj.parking_place.service.ParkingPlaceServerUtils;
import com.rmj.parking_place.utils.NotificationUtils;
import com.rmj.parking_place.utils.ParametersForUpdatingZones;
import com.rmj.parking_place.utils.TimerWithEndDateTime;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;

public class MapPageFragment extends Fragment{

    private NotificationDialog dialogForExpiredReservationOrTakingParkingPlace;
    private NotificationDialog dialogForCurrentLocationNotFound;
    private DialogForSelectingTicketType dialogForSelectingTicketType;
    private DialogForMockLocation dialogForMockLocation;

    private Mode currentMode;
    private TimerWithEndDateTime timerForReservationOrTakingOfParkingPlace;
    private TimerWithEndDateTime timerForNotificationReservationOrTakingOfParkingPlace;
    private Timer timerForUpdatingParkingPlaces;

    private MapFragment mapFragment;
    private ParkingPlaceInfoFragment parkingPlaceInfoFragment;
    private FindParkingFragment findParkingFragment;

    private List<Zone> zones = null;
    private List<Zone> zonesForUpdating = null;

    private Reservation reservation;
    private PaidParkingPlace paidParkingPlace;

    private MainActivity mainActivity;
    private View view;

    public static final int NOTIFICATION_ID = 888;
    private NotificationManagerCompat mNotificationManagerCompat;
    private Notification notification;

    private Context mContext;
    private static OnCreateViewFinishedListenerImplementation onCreateViewFinishedListenerImplementation;
    private static NotificationRepository notificationRepository;


    public MapPageFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mainActivity = (MainActivity) getActivity();

        if (savedInstanceState == null) {
            notificationRepository = new NotificationRepository(mainActivity);
            mainActivity.resetZonesIfNeeded();
            this.zonesForUpdating = new ArrayList<Zone>();
        }
        else {
            zones = savedInstanceState.getParcelableArrayList("zones");
            zonesForUpdating = savedInstanceState.getParcelableArrayList("zonesForUpdating");
        }

        //createNotificationReservationExpire();
        //detectAnyException();
        //mNotificationManagerCompat = NotificationManagerCompat.from(mContext.getApplicationContext());//(getActivity().getApplicationContext());
        //createNotificationChannel();
        Toast.makeText(getActivity(), "onCreate()",Toast.LENGTH_SHORT).show();
    }

    /*private void createNotificationReservationExpire() {
        String notificationChannelId = "notifikacija";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext.getApplicationContext(), notificationChannelId);//(getActivity().getApplicationContext(), notificationChannelId);

        notification = builder
                .setSmallIcon(R.drawable.icon_failure)
                .setContentTitle("Reservation expire")
                .setContentText("Your reservation is expire")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build();
    }*/

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    private void setCurrentModeAgain() {
        switch (currentMode) {
            case NONE:
                setNoneMode();
                break;
            case CAN_RESERVE:
                setCanReserveMode();
                break;
            case CAN_RESERVE_AND_CAN_TAKE:
                setCanReserveAndCanTakeMode();
                break;
            case IS_RESERVING:
                setIsReservingMode();
                break;
            case IS_RESERVING_AND_CAN_TAKE:
                setIsReservingAndCanTakeMode();
                break;
            case CAN_TAKE:
                setCanTakeMode();
                break;
            case IS_TAKING:
                setIsTakingMode(false);
                break;
            default:
                setNoneMode();
                break;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_map_page, container, false);

        initializeButtons();

        FragmentManager fm = getChildFragmentManager();


        if (savedInstanceState == null) {
            reservation = mainActivity.getReservation();
            paidParkingPlace = mainActivity.getRegularPaidParkingPlace();

            mapFragment = new MapFragment();
            // mapFragment = (MapFragment) fm.findFragmentById(R.id.map_fragment_id);
            parkingPlaceInfoFragment = ParkingPlaceInfoFragment.newInstance();
            findParkingFragment = new FindParkingFragment();


            fm.beginTransaction()
                    .replace(R.id.place_info_frame, parkingPlaceInfoFragment, "parkingPlaceInfoFragment")
                    .replace(R.id.mapContent, mapFragment, "mapFragment")
                    .replace(R.id.find_parking_frame, findParkingFragment, "findParkingFragment")
                    .commit();
        }
        else {
            reservation = savedInstanceState.getParcelable("reservation");
            paidParkingPlace = savedInstanceState.getParcelable("regularPaidParkingPlace");

            String currentModeStr = savedInstanceState.getString("currentMode");
            currentMode = Mode.valueOf(currentModeStr);
            setCurrentModeAgain();

           /* if (isInIsReservingMode() || isInIsReservingAndCanTakeMode() || isInIsTakingMode()) {
                long endDateTimeInMillis = savedInstanceState.getLong("endDateTimeInMillis");
                boolean reservation = savedInstanceState.getBoolean("reservation");
                startTimerForReservationOrTakingOfParkingPlace(endDateTimeInMillis, reservation);
            }*/

            boolean dialogForExpiredReservationOrTakingParkingPlaceIsShowing =
                        savedInstanceState.getBoolean("dialogForExpiredReservationOrTakingParkingPlaceIsShowing");
            if (dialogForExpiredReservationOrTakingParkingPlaceIsShowing) {
                String dialogMessage = savedInstanceState.getString("dialogForExpiredReservationOrTakingParkingPlaceMessage");
                dialogForExpiredReservationOrTakingParkingPlace = new NotificationDialog(dialogMessage, mainActivity);
                dialogForExpiredReservationOrTakingParkingPlace.showDialog();
            }

            boolean dialogForSelectingTicketTypeIsShowing = savedInstanceState.getBoolean("dialogForSelectingTicketTypeIsShowing");
            if (dialogForSelectingTicketTypeIsShowing) {
                boolean dialogForSelectingTicketTypeOnlyRegularTicket =
                                savedInstanceState.getBoolean("dialogForSelectingTicketTypeOnlyRegularTicket");
                dialogForSelectingTicketType = new DialogForSelectingTicketType(dialogForSelectingTicketTypeOnlyRegularTicket,
                                                                                                        this);
                dialogForSelectingTicketType.showDialog();
            }

            // mapFragment = (MapFragment) fm.findFragmentById(R.id.mapContent);
            mapFragment =  (MapFragment) fm.getFragment(savedInstanceState, "mapFragment");
            // parkingPlaceInfoFragment = (ParkingPlaceInfoFragment) fm.findFragmentById(R.id.place_info_frame);

            if (onCreateViewFinishedListenerImplementation == null) {
                onCreateViewFinishedListenerImplementation = new OnCreateViewFinishedListenerImplementation(this, mapFragment);
            }
            else {
                onCreateViewFinishedListenerImplementation.setMapPageFragment(this);
                onCreateViewFinishedListenerImplementation.setMapFragment(mapFragment);
            }

            // bitno je da se ovo pozove pre ponovnog kreiranja findParkingFragment, jer se u suprotnom nece
            // dobiti dobar height za findParkingFragment
            recoverVisibleFragments(savedInstanceState);

            parkingPlaceInfoFragment =  (ParkingPlaceInfoFragment) fm.getFragment(savedInstanceState,
                                                                            "parkingPlaceInfoFragment");
            // findParkingFragment = () fm.findFragmentById(R.id.find_parking_frame);
            findParkingFragment =  (FindParkingFragment) fm.getFragment(savedInstanceState, "findParkingFragment");
            findParkingFragment.setOnCreateViewFinishedListener(onCreateViewFinishedListenerImplementation);
        }

        Date now = new Date();
        if (reservation != null && now.before(reservation.getEndDateTimeAndroid())) {
            setIsReservingMode();
            startTimerForReservationOrTakingOfParkingPlace(reservation.getEndDateTimeAndroid().getTime(), true);
        }
        else if (paidParkingPlace != null && now.before(paidParkingPlace.getEndDateTimeAndroid())) {
            setIsTakingMode(false);
            startTimerForReservationOrTakingOfParkingPlace(paidParkingPlace.getEndDateTimeAndroid().getTime(), false);
        }
        else {
            setNoneMode();
        }

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Toast.makeText(mainActivity, "onStart()",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        Toast.makeText(mainActivity, "onResume()",Toast.LENGTH_SHORT).show();
        if (zones == null || (zones != null && zones.isEmpty())) {
            zones = mainActivity.getZones();
        }
        checkTimerForReservationOrTakingOfParkingPlaceAndStartIfNeeded();

        startTimerForUpdatingParkingPlaces();
    }

    @Override
    public void onPause() {
        super.onPause();
        Toast.makeText(mainActivity, "onPause()",Toast.LENGTH_SHORT).show();
        /*if(reservation != null){
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    //TODO your background code
                    boolean end = false;
                    while(!end){
                        Date currentDateTime = new Date();
                        long currentDateTimeInMillis = currentDateTime.getTime();
                        final long remainingTimeInSeconds = (reservation.getEndDateTimeAndroid().getTime() - currentDateTimeInMillis) / 1000;

                        if(remainingTimeInSeconds < 0){
                            showNotification(true);
                            end = true;
                        }
                    }
                }
            });
        }*/
    }

    @Override
    public void onStop() {
        super.onStop();
        stopTimerForUpdatingParkingPlaces();
        stopTimerForReservationOrTakingOfParkingPlace();
        Toast.makeText(mainActivity, "onStop()",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cancelDialogForExpiredReservationOrTakingParkingPlace();
        cancelDialogForSelectingTicketType();
        cancelDialogForMockLocation();
        cancelDialogForCurrentLocationNotFound();

        Toast.makeText(mainActivity, "onDestroy()",Toast.LENGTH_SHORT).show();
    }

    private void initializeButtons() {
        Button reserveBtn = (Button) view.findViewById(R.id.btnReserve);
        reserveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickOnBtnReserve(v);
            }
        });

        Button takeBtn = (Button) view.findViewById(R.id.btnTake);
        takeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickOnBtnTake(v);
            }
        });

        Button leaveBtn = (Button) view.findViewById(R.id.btnLeaveParkingPlace);
        leaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickOnBtnLeaveParkingPlace(v);
            }
        });

        ImageButton findBtn = (ImageButton) view.findViewById(R.id.findParkingButton);
        findBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickOnBtnFindParkingPlace();
            }
        });
    }

    private void recoverVisibleFragments(Bundle savedInstanceState) {
        boolean parkingPlaceInfoFragmentVisible = savedInstanceState.getBoolean("parkingPlaceInfoFragmentVisible");
        boolean findParkingPlaceFragmentVisible = savedInstanceState.getBoolean("findParkingPlaceFragmentVisible");


        if (parkingPlaceInfoFragmentVisible) {
            // mapFragment.showPlaceInfoFragment();
            showOrHideParkingPlaceInfoFragmentWithTransition(true);
        }

        if (findParkingPlaceFragmentVisible) {
            // clickOnBtnFindParkingPlace();
            view.findViewById(R.id.findParkingButton).setVisibility(View.GONE);
            showOrHideFindParkingFragmentWithTransition(true);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString("currentMode", currentMode.name());

        // Save the fragment's instance
        FragmentManager fm = getChildFragmentManager();
        fm.putFragment(outState, "mapFragment", mapFragment);
        fm.putFragment(outState, "parkingPlaceInfoFragment", parkingPlaceInfoFragment);
        fm.putFragment(outState, "findParkingFragment", findParkingFragment);


        outState.putBoolean("parkingPlaceInfoFragmentVisible", isParkingPlaceInfoFragmentVisible());
        outState.putBoolean("findParkingPlaceFragmentVisible", isFindParkingPlaceFragmentVisible());

        boolean dialogForExpiredReservationOrTakingParkingPlaceIsShowing =
                                                dialogForExpiredReservationOrTakingParkingPlace != null
                                                && dialogForExpiredReservationOrTakingParkingPlace.isShowing();
        outState.putBoolean("dialogForExpiredReservationOrTakingParkingPlaceIsShowing",
                                dialogForExpiredReservationOrTakingParkingPlaceIsShowing);
        if (dialogForExpiredReservationOrTakingParkingPlaceIsShowing) {
            outState.putString("dialogForExpiredReservationOrTakingParkingPlaceMessage",
                    dialogForExpiredReservationOrTakingParkingPlace.getMessage());
        }

        boolean dialogForSelectingTicketTypeIsShowing = dialogForSelectingTicketType != null
                                                         && dialogForSelectingTicketType.isShowing();
        outState.putBoolean("dialogForSelectingTicketTypeIsShowing", dialogForSelectingTicketTypeIsShowing);
        if (dialogForSelectingTicketTypeIsShowing) {
            outState.putBoolean("dialogForSelectingTicketTypeOnlyRegularTicket",
                                dialogForSelectingTicketType.isOnlyRegularTicket());
        }

        if (zones != null) {
            outState.putParcelableArrayList("zones", (ArrayList<Zone>) zones);
        }

        if (zonesForUpdating != null) {
            outState.putParcelableArrayList("zonesForUpdating", (ArrayList<Zone>) zonesForUpdating);
        }

        if (reservation != null) {
            outState.putParcelable("reservation", reservation);
        }

        if (paidParkingPlace != null) {
            outState.putParcelable("paidParkingPlace", paidParkingPlace);
        }

        /*if (timerForReservationOrTakingOfParkingPlace != null && timerForReservationOrTakingOfParkingPlace.isStarted()) {
            outState.putLong("endDateTimeInMillis", timerForReservationOrTakingOfParkingPlace.getEndDateTimeInMillis());
            outState.putBoolean("reservation", timerForReservationOrTakingOfParkingPlace.isReservation());
        }*/

        super.onSaveInstanceState(outState);
    }

    public void showDialogForMockLocation() {
        dialogForMockLocation = new DialogForMockLocation(getActivity());
        dialogForMockLocation.showDialog();
    }

    public void showDialogForCurrentLocationNotFound() {
        String message = "Not found current location";
        dialogForCurrentLocationNotFound = new NotificationDialog(message, mainActivity);
        dialogForCurrentLocationNotFound.showDialog();
    }

    private void checkTimerForReservationOrTakingOfParkingPlaceAndStartIfNeeded() {
        if (isInIsReservingMode() || isInIsReservingAndCanTakeMode() || isInIsTakingMode()) {
            if (timerForReservationOrTakingOfParkingPlace == null) {
                throw new RuntimeException("timerForReservationOrTakingOfParkingPlace == null");
            }

            if (!timerForReservationOrTakingOfParkingPlace.isStarted()) {
                startTimerForReservationOrTakingOfParkingPlace(
                        timerForReservationOrTakingOfParkingPlace.getEndDateTimeInMillis(),
                        timerForReservationOrTakingOfParkingPlace.isReservation());
            }
        }
    }

    private void cancelDialogForCurrentLocationNotFound() {
        if (dialogForCurrentLocationNotFound != null
                && dialogForCurrentLocationNotFound.isShowing()) {
            dialogForCurrentLocationNotFound.cancel();
            dialogForCurrentLocationNotFound = null;
        }
    }

    private void cancelDialogForMockLocation() {
        if (dialogForMockLocation != null
                && dialogForMockLocation.isShowing()) {
            dialogForMockLocation.cancel();
            dialogForMockLocation = null;
        }
    }

    private void cancelDialogForExpiredReservationOrTakingParkingPlace() {
        if (dialogForExpiredReservationOrTakingParkingPlace != null
                && dialogForExpiredReservationOrTakingParkingPlace.isShowing()) {
            dialogForExpiredReservationOrTakingParkingPlace.cancel();
            dialogForExpiredReservationOrTakingParkingPlace = null;
        }
    }

    private void cancelDialogForSelectingTicketType() {
        if (dialogForSelectingTicketType != null
                && dialogForSelectingTicketType.isShowing()) {
            dialogForSelectingTicketType.cancel();
            dialogForSelectingTicketType = null;
        }
    }

    private void updateZoneWithParkingPlaceChanges() {
        // InputStream is = getResources().openRawResource(R.raw.zones_with_parking_places);
        // List<Zone> zones = JsonLoader.getZones(is);
        //-------------------------------------------------
        // new RequestAsyncTask(this).execute("GET", "https://192.168.1.12:45455/api/zones");

        ParametersForUpdatingZones parametersForUpdatingZones;
        synchronized (this.zonesForUpdating) {
            parametersForUpdatingZones = prepareParametersForUpdatingZones(this.zonesForUpdating);
        }

        ParkingPlaceServerUtils.zoneService.updateZonesWithParkingPlaces(parametersForUpdatingZones.getZoneids(),
                                                                        parametersForUpdatingZones.getVersions())
                .enqueue(new Callback<ParkingPlacesUpdatingDTO>() {
                    @Override
                    public void onResponse(Call<ParkingPlacesUpdatingDTO> call, retrofit2.Response<ParkingPlacesUpdatingDTO> response) {
                        if (response == null || (response != null && !response.isSuccessful())) {
                            Toast.makeText(mainActivity, "Problem with updating zones with parking places.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        else {
                            ParkingPlacesUpdatingDTO parkingPlacesUpdatingDTO = response.body();
                            if (parametersForUpdatingZones == null) {
                                Toast.makeText(mainActivity, "Problem with updating zones with parking places.",
                                                                                        Toast.LENGTH_SHORT).show();
                            }
                            else {
                                completeUpdatingOfZonesWithParkingPlaceChanges(parkingPlacesUpdatingDTO);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ParkingPlacesUpdatingDTO> call, Throwable t) {
                        Toast.makeText(mainActivity, t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        /*new GetRequestAsyncTask(this)
                .execute(url, HttpRequestAndResponseType.UPDATE_ZONES_WITH_PARKING_PLACES.name(), TokenUtils.getToken());*/
    }

    private LatLngBounds convertZoneToLatLngBounds(Zone zone) {
        Location northEast = zone.getNorthEast();
        Location southWest = zone.getSouthWest();
        return LatLngBounds.builder()
                    .include(new com.google.android.gms.maps.model.LatLng(northEast.getLatitude(), northEast.getLongitude()))
                    .include(new com.google.android.gms.maps.model.LatLng(southWest.getLatitude(), southWest.getLongitude()))
                    .build();
    }

    private void completeUpdatingOfZonesWithParkingPlaceChanges(ParkingPlacesUpdatingDTO parkingPlacesUpdatingDTO) {
        ArrayList<ParkingPlace> parkingPlacesForAdding = new ArrayList<ParkingPlace>();
        List<ParkingPlace> parkingPlacesForUpdating = new ArrayList<ParkingPlace>();
        List<LatLngBounds> zonesBoundsForClustersUpdating = new ArrayList<LatLngBounds>();

        for (ParkingPlacesInitialDTO parkingPlacesInitialDTO : parkingPlacesUpdatingDTO.getInitials()) {
            for (Zone zone : zones) {
                if (parkingPlacesInitialDTO.getZoneId().equals(zone.getId())) {
                    if (parkingPlacesInitialDTO.getVersion() > zone.getVersion()) {
                        zone.setVersion(parkingPlacesInitialDTO.getVersion());
                        zone.setParkingPlaces(parkingPlacesInitialDTO.getParkingPlaces());
                        for (ParkingPlace parkingPlace : zone.getParkingPlaces()) {
                            parkingPlace.setZone(zone);
                        }
                        parkingPlacesForAdding.addAll(zone.getParkingPlaces());
                    }
                }
            }
        }
        if (!parkingPlacesForAdding.isEmpty()) {
            mapFragment.addParkingPlacesAndMarkers(parkingPlacesForAdding);
        }

        LatLngBounds zoneBounds;
        for (ParkingPlaceChangesDTO parkingPlaceChangesDTO : parkingPlacesUpdatingDTO.getChanges()) {
            for (Zone zone : zones) {
                if (parkingPlaceChangesDTO.getZoneId().equals(zone.getId())) {
                    if (parkingPlaceChangesDTO.getVersion() > zone.getVersion()) {
                        zone.setVersion(parkingPlaceChangesDTO.getVersion());
                        zoneBounds = convertZoneToLatLngBounds(zone);
                        zonesBoundsForClustersUpdating.add(zoneBounds);
                        for (ParkingPlaceDTO parkingPlaceDTO : parkingPlaceChangesDTO.getParkingPlaceChanges()) {
                            for (ParkingPlace parkingPlace : zone.getParkingPlaces()) {
                                if (parkingPlaceDTO.getId().equals(parkingPlace.getId())) {
                                    parkingPlace.setStatus(parkingPlaceDTO.getStatus());
                                    parkingPlacesForUpdating.add(parkingPlace);
                                }
                            }
                        }
                    }
                }
            }
        }
        if (!parkingPlacesForUpdating.isEmpty()) {
            mapFragment.updateParkingPlaceMarkers(parkingPlacesForUpdating);
            mapFragment.updateDisplayedClusters(zonesBoundsForClustersUpdating);
        }

        if (!parkingPlacesForAdding.isEmpty() || !parkingPlacesForUpdating.isEmpty()) {
            mapFragment.tryToFindEmptyParkingPlaceNearbyAndSetMode();
        }
    }

    public void selectZonesForUpdating(LatLngBounds currentCameraBounds) {
        if (this.zones == null || (this.zones != null && this.zones.isEmpty())) {
            return;
        }

        com.mapbox.mapboxsdk.geometry.LatLngBounds cameraBounds = convertLatLngBoundsToLatLngBoundsMapboxsdk(currentCameraBounds);
        // com.mapbox.mapboxsdk.geometry.LatLngBounds zoneBounds;
        com.mapbox.mapboxsdk.geometry.LatLngBounds intersectBounds;

        synchronized (this.zonesForUpdating) {
            this.zonesForUpdating.clear();
            for (Zone zone : this.zones) {
                // zoneBounds = makeLatLngBoundsMapboxsdk(zone.getNorthEast(), zone.getSouthWest());
                intersectBounds = cameraBounds.intersect(zone.getBounds());
                if (intersectBounds != null && !intersectBounds.isEmptySpan()) {
                    this.zonesForUpdating.add(zone);
                }
            }
        }
    }

    /*private com.mapbox.mapboxsdk.geometry.LatLngBounds makeLatLngBoundsMapboxsdk(Location northEast, Location southWest) {
        return new com.mapbox.mapboxsdk.geometry.LatLngBounds.Builder()
                .include(new LatLng(northEast.getLatitude(), northEast.getLongitude()))
                .include(new LatLng(southWest.getLatitude(), southWest.getLongitude()))
                .build();
    }*/

    private com.mapbox.mapboxsdk.geometry.LatLngBounds convertLatLngBoundsToLatLngBoundsMapboxsdk(LatLngBounds bounds) {
        return new com.mapbox.mapboxsdk.geometry.LatLngBounds.Builder()
                .include(new LatLng(bounds.northeast.latitude, bounds.northeast.longitude))
                .include(new LatLng(bounds.southwest.latitude, bounds.southwest.longitude))
                .build();
    }

    private ParametersForUpdatingZones prepareParametersForUpdatingZones(List<Zone> zones) {
        StringBuilder sbZoneIds = new StringBuilder();
        StringBuilder sbVersions = new StringBuilder();

        for (Zone zone : zones) {
            sbZoneIds.append(",");
            sbZoneIds.append(zone.getId());
            sbVersions.append(",");
            sbVersions.append(zone.getVersion());
        }

        return new ParametersForUpdatingZones(sbZoneIds.substring(1), sbVersions.substring(1));
    }

    public void setNoneMode() {
        this.currentMode = Mode.NONE;

        int color = ContextCompat.getColor(mainActivity.getApplicationContext(), R.color.colorDisabledButton);

        Button btnReserve = (Button) view.findViewById(R.id.btnReserve);
        btnReserve.setEnabled(false);
        btnReserve.setBackgroundColor(color);

        Button btnTake = (Button) view.findViewById(R.id.btnTake);
        btnTake.setEnabled(false);
        btnTake.setBackgroundColor(color);

        // ((TextView) findViewById(R.id.txtRemainingTime)).setVisibility(View.INVISIBLE);
        // ((LinearLayout) findViewById(R.id.northPanel)).setVisibility(View.GONE);
        // GONE - This view is invisible, and it doesn't take any space for layout purposes.
    }

    public void setCanReserveMode() {
        this.currentMode = Mode.CAN_RESERVE;

        int color = ContextCompat.getColor(mainActivity.getApplicationContext(), R.color.colorReserve);
        Button btnReserve = (Button) view.findViewById(R.id.btnReserve);
        btnReserve.setEnabled(true);
        btnReserve.setBackgroundColor(color);

        color = ContextCompat.getColor(mainActivity.getApplicationContext(), R.color.colorDisabledButton);

        Button btnTake = (Button) view.findViewById(R.id.btnTake);
        btnTake.setEnabled(false);
        btnTake.setBackgroundColor(color);

        // ((LinearLayout) findViewById(R.id.northPanel)).setVisibility(View.VISIBLE);
    }

    public void setCanTakeMode() {
        this.currentMode = Mode.CAN_TAKE;

        int color = ContextCompat.getColor(mainActivity.getApplicationContext(), R.color.colorTake);
        Button btnTake = (Button) view.findViewById(R.id.btnTake);
        btnTake.setEnabled(true);
        btnTake.setBackgroundColor(color);

        color = ContextCompat.getColor(mainActivity.getApplicationContext(), R.color.colorDisabledButton);

        Button btnReserve = (Button) view.findViewById(R.id.btnReserve);
        btnReserve.setEnabled(false);
        btnReserve.setBackgroundColor(color);
        // ((LinearLayout) findViewById(R.id.northPanel)).setVisibility(View.VISIBLE);
    }

    private void showTxtRemainingTime(int colorResource) {
        int color = ContextCompat.getColor(mainActivity.getApplicationContext(), colorResource);
        TextView txtRemainingTime = (TextView) view.findViewById(R.id.txtRemainingTime);
        txtRemainingTime.setTextColor(color);
        txtRemainingTime.setVisibility(View.VISIBLE);
    }

    public void setIsReservingMode() {
        this.currentMode = Mode.IS_RESERVING;

        showTxtRemainingTime(R.color.colorReserve);

        int color = ContextCompat.getColor(mainActivity.getApplicationContext(), R.color.colorDisabledButton);

        Button btnReserve = (Button) view.findViewById(R.id.btnReserve);
        btnReserve.setEnabled(false);
        btnReserve.setBackgroundColor(color);

        Button btnTake = (Button) view.findViewById(R.id.btnTake);
        btnTake.setEnabled(false);
        btnTake.setBackgroundColor(color);
    }

    public void setIsReservingAndCanTakeMode() {
        this.currentMode = Mode.IS_RESERVING_AND_CAN_TAKE;
        int color = ContextCompat.getColor(mainActivity.getApplicationContext(), R.color.colorReserve);
        TextView txtRemainingTime = (TextView) view.findViewById(R.id.txtRemainingTime);
        txtRemainingTime.setTextColor(color);
        txtRemainingTime.setVisibility(View.VISIBLE);

        color = ContextCompat.getColor(mainActivity.getApplicationContext(), R.color.colorDisabledButton);
        Button btnReserve = (Button) view.findViewById(R.id.btnReserve);
        btnReserve.setEnabled(false);
        btnReserve.setBackgroundColor(color);

        color = ContextCompat.getColor(mainActivity.getApplicationContext(), R.color.colorTake);
        Button btnTake = (Button) view.findViewById(R.id.btnTake);
        btnTake.setEnabled(true);
        btnTake.setBackgroundColor(color);

    }

    public void setIsTakingMode(boolean activateLeaveBtn) {
        this.currentMode = Mode.IS_TAKING;

        showTxtRemainingTime(R.color.colorTake);

        int color = ContextCompat.getColor(mainActivity.getApplicationContext(), R.color.colorDisabledButton);
        int colorActivateBtn = ContextCompat.getColor(mainActivity.getApplicationContext(), R.color.colorLeaveParkingPlace);
        Button btnTake = (Button) view.findViewById(R.id.btnTake);
        btnTake.setEnabled(false);
        btnTake.setBackgroundColor(color);

        //color = ContextCompat.getColor(mainActivity.getApplicationContext(), R.color.colorDisabledButton);
        Button btnReserve = (Button) view.findViewById(R.id.btnReserve);
        btnReserve.setEnabled(false);
        btnReserve.setBackgroundColor(color);

        Button btnLeaveParkingPlace = (Button) view.findViewById(R.id.btnLeaveParkingPlace);
        if(activateLeaveBtn){
            btnLeaveParkingPlace.setEnabled(true);
            btnLeaveParkingPlace.setBackgroundColor(colorActivateBtn);
        }else {
            btnLeaveParkingPlace.setEnabled(false);
            btnLeaveParkingPlace.setBackgroundColor(color);
        }
        btnLeaveParkingPlace.setVisibility(View.VISIBLE);
    }

    public void setCanReserveAndCanTakeMode() {
        currentMode = Mode.CAN_RESERVE_AND_CAN_TAKE;

        int color = ContextCompat.getColor(mainActivity.getApplicationContext(), R.color.colorReserve);
        Button btnReserve = (Button) view.findViewById(R.id.btnReserve);
        btnReserve.setEnabled(true);
        btnReserve.setBackgroundColor(color);

        color = ContextCompat.getColor(mainActivity.getApplicationContext(), R.color.colorTake);
        Button btnTake = (Button) view.findViewById(R.id.btnTake);
        btnTake.setEnabled(true);
        btnTake.setBackgroundColor(color);
    }

    public void clickOnBtnReserve(View view) {
        if(!isInCanReserveMode() && !isInCanReserveAndCanTakeMode()) {
            Toast.makeText(mainActivity, "currentMode == " + currentMode.name()
                    + " (invalid mode) (clickOnBtnReserve method)",Toast.LENGTH_SHORT).show();
            return;
        }

        ReservingDTO dto = null;
        try {
            dto = mapFragment.checkAndPrepareAllForReservingOnServer();
        } catch (NotFoundParkingPlaceException | AlreadyTakenParkingPlaceException | AlreadyReservedParkingPlaceException
                | CurrentLocationUnknownException | MaxAllowedDistanceForReservationException e) {
            Toast.makeText(mainActivity, e.getMessage(),Toast.LENGTH_SHORT).show();
            return;
        }

        reserveParkingPlaceOnServer(dto);
    }

    private void reserveParkingPlace(Reservation reservation) {
        this.reservation = reservation;
        mainActivity.saveReservation(reservation);
        mapFragment.reserveParkingPlace(reservation);

        if (isInCanReserveMode()) {
            setIsReservingMode();
        }
        else if (isInCanReserveAndCanTakeMode()) {
            setIsReservingAndCanTakeMode();
        }

        if (isParkingPlaceInfoFragmentVisible()) {
            parkingPlaceInfoFragment.updateParkingPlaceStatus(ParkingPlaceStatus.RESERVED);
        }
    }

    private void setupNotification(boolean forReserving) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                String notificationTitle;
                String notificationText;
                String notificationType;
                long parkingPlaceId;
                long notificationId;
                long dateTime;
                if (forReserving) {
                    notificationTitle = mainActivity.getString(R.string.reserveParkingPlaceNotificationTitle);
                    notificationText = mainActivity.getString(R.string.reserveParkingPlaceNotificationText);
                    notificationType = "reseravation_notification";
                    notificationId = reservation.getId();
                    parkingPlaceId = reservation.getParkingPlace().getId();

                    dateTime = reservation.getEndDateTimeAndroid().getTime() - 15000;
                }
                else {
                    notificationTitle = mainActivity.getString(R.string.takeParkingPlaceNotificationTitle);
                    notificationText = mainActivity.getString(R.string.takeParkingPlaceNotificationText);
                    notificationType = "taking_notification";
                    notificationId = paidParkingPlace.getId();
                    parkingPlaceId = paidParkingPlace.getParkingPlace().getId();
                    //dateTime = paidParkingPlace.getEndDateTimeAndroid().getTime();
                    dateTime = System.currentTimeMillis() + 10*1000;
                }

                NotificationDb notificationDb = new NotificationDb(notificationId, notificationTitle, notificationText,
                        notificationType, dateTime, parkingPlaceId);
                NotificationUtils.scheduleNotification(notificationDb);
                notificationRepository.insertNotification(notificationDb);

                return null;
            }
        }.execute();
    }

    private void reserveParkingPlaceOnServer(ReservingDTO dto) {
        ParkingPlaceServerUtils.parkingPlaceService.reserveParkingPlace(dto)
                .enqueue(new Callback<ReservationDTO>() {
                    @Override
                    public void onResponse(Call<ReservationDTO> call, retrofit2.Response<ReservationDTO> response) {
                        if (response == null || (response != null && !response.isSuccessful())) {
                            Toast.makeText(mainActivity, "Problem with reservation of parking place.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        else {
                            ReservationDTO reservationDTO = response.body();
                            ParkingPlace parkingPlace = reservationDTO.getParkingPlace();
                            parkingPlace.setZone(mainActivity.getZone(reservationDTO.getZoneId()));
                            Reservation reservation = new Reservation(reservationDTO.getId(), reservationDTO.getStartDateTimeAndroid(),
                                    reservationDTO.getStartDateTimeServer(), parkingPlace);
                            reserveParkingPlace(reservation);
                            setupNotification(true);
                        }
                    }

                    @Override
                    public void onFailure(Call<ReservationDTO> call, Throwable t) {
                        Toast.makeText(mainActivity, t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void takeParkingPlaceOnServer(TakingDTO dto) {
        ParkingPlaceServerUtils.parkingPlaceService.takeParkingPlace(dto)
                .enqueue(new Callback<PaidParkingPlaceDTO>() {
                    @Override
                    public void onResponse(Call<PaidParkingPlaceDTO> call, retrofit2.Response<PaidParkingPlaceDTO> response) {
                        if (response == null || (response != null && !response.isSuccessful())) {
                            Toast.makeText(mainActivity, "Problem with taking of parking place.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        else {
                            if (isInIsReservingAndCanTakeMode()) {
                                finishReservationOfParkingPlace(false);
                                NotificationUtils.cancelNotification("reservation_notification",
                                                                                            reservation.getId().intValue());
                            }

                            PaidParkingPlaceDTO paidParkingPlaceDTO = response.body();
                            ParkingPlace parkingPlace = paidParkingPlaceDTO.getParkingPlace();
                            parkingPlace.setZone(mainActivity.getZone(paidParkingPlaceDTO.getZoneId()));
                            PaidParkingPlace paidParkingPlace = new PaidParkingPlace(paidParkingPlaceDTO.getId(), parkingPlace,
                                    paidParkingPlaceDTO.getStartDateTimeAndroid(), paidParkingPlaceDTO.getStartDateTimeServer(),
                                    paidParkingPlaceDTO.getTicketType(), paidParkingPlaceDTO.isArrogantUser());
                            takeParkingPlace(paidParkingPlace);
                            setupNotification(false);
                        }
                    }

                    @Override
                    public void onFailure(Call<PaidParkingPlaceDTO> call, Throwable t) {
                        Toast.makeText(mainActivity, t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void clickOnBtnTake(View view) {
        boolean isReservingAndCanTakeMode = isInIsReservingAndCanTakeMode();
        if(!isInCanTakeMode() && !isInCanReserveAndCanTakeMode() && !isReservingAndCanTakeMode) {
            Toast.makeText(mainActivity, "currentMode == " + currentMode.name()
                    + " (invalid mode) (clickOnBtnTake method)",Toast.LENGTH_SHORT).show();
            return;
        }

        boolean parkingPlaceNearByFavoritePlace = false;
        try {
            parkingPlaceNearByFavoritePlace = mapFragment.checkParkingPlaceIsNearByFavoritePlace(mainActivity.getFavoritePlaces());
        } catch (Exception e) {
            Toast.makeText(mainActivity, e.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }

        dialogForSelectingTicketType = new DialogForSelectingTicketType(!parkingPlaceNearByFavoritePlace, this);
        dialogForSelectingTicketType.showDialog();
    }

    public void continueWithTakingOfParkingPlace(TicketType ticketType) {
       /* if (isInIsReservingAndCanTakeMode()) {
            finishReservationOfParkingPlace(false);
        }*/

        TakingDTO dto = null;
        try {
            dto = mapFragment.checkAndPrepareAllForTakingOnServer();
        } catch (NotFoundParkingPlaceException | AlreadyReservedParkingPlaceException | AlreadyTakenParkingPlaceException e) {
            Toast.makeText(mainActivity, e.getMessage(),Toast.LENGTH_SHORT).show();
            return;
        }

        dto.setTicketType(ticketType);
        takeParkingPlaceOnServer(dto);
    }

    private void takeParkingPlace(PaidParkingPlace paidParkingPlace) {
        this.paidParkingPlace = paidParkingPlace;
        mainActivity.savePaidParkingPlace(paidParkingPlace);
        mapFragment.takeParkingPlace(paidParkingPlace);
        setIsTakingMode(true);

        if (isParkingPlaceInfoFragmentVisible()) {
            parkingPlaceInfoFragment.updateParkingPlaceStatus(ParkingPlaceStatus.TAKEN);
        }
    }

    public void updateRemainingTime(long remainingTimeInSeconds) {
        long hours = remainingTimeInSeconds / 3600;
        long differenceInSecondsWithoutHours = remainingTimeInSeconds - hours * 3600;
        long mins = differenceInSecondsWithoutHours / 60;
        long secs = differenceInSecondsWithoutHours % 60;

        final String remainingTimeStr = "Remaining time:  " + prepareRemainingTimeString(hours, mins, secs);
        ((TextView) view.findViewById(R.id.txtRemainingTime)).setText(remainingTimeStr);
    }

    private String prepareRemainingTimeString(long hours, long mins, long secs) {
        String remainingTimeString = "";

        if (hours / 10 < 1) {
            remainingTimeString += "0" + hours;
        }
        else {
            remainingTimeString += hours;
        }
        remainingTimeString += ":";

        if (mins / 10 < 1) {
            remainingTimeString += "0" + mins;
        }
        else {
            remainingTimeString += mins;
        }
        remainingTimeString += ":";

        if (secs / 10 < 1) {
            remainingTimeString += "0" + secs;
        }
        else {
            remainingTimeString += secs;
        }

        return remainingTimeString;
    }

    public void clickOnBtnLeaveParkingPlace(View view) {
        Button btnLeaveParkingPlace = (Button) view.findViewById(R.id.btnLeaveParkingPlace);
        btnLeaveParkingPlace.setVisibility(View.GONE);

        finishTakingOfParkingPlace(false);

        mapFragment.checkSelectedParkingPlaceAndSetMode();
        mapFragment.tryToFindEmptyParkingPlaceNearbyAndSetMode();
    }

    public boolean isInIsReservingMode() {
        return currentMode == Mode.IS_RESERVING;
    }

    public boolean isInIsTakingMode() {
        return currentMode == Mode.IS_TAKING;
    }

    public boolean isInNoneMode() { return currentMode == Mode.NONE; }

    public boolean isInIsReservingAndCanTakeMode() { return currentMode == Mode.IS_RESERVING_AND_CAN_TAKE; }

    public boolean isInCanReserveMode() { return currentMode == Mode.CAN_RESERVE; }

    public boolean isInCanTakeMode() { return currentMode == Mode.CAN_TAKE; }

    public boolean isInCanReserveAndCanTakeMode() { return currentMode == Mode.CAN_RESERVE_AND_CAN_TAKE; }

    public void startTimerForReservationOrTakingOfParkingPlace(long endDateTimeInMillis, boolean reservation) {
        timerForReservationOrTakingOfParkingPlace = new TimerWithEndDateTime(endDateTimeInMillis, reservation);
        timerForReservationOrTakingOfParkingPlace.setStarted(true);
        timerForReservationOrTakingOfParkingPlace.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Date currentDateTime = new Date();
                long currentDateTimeInMillis = currentDateTime.getTime();
                final long remainingTimeInSeconds = (endDateTimeInMillis - currentDateTimeInMillis) / 1000;

                // Dok nisam koristio mainActivity.runOnUiThread metodu (thread),
                // dobijao sam exception - android.view.ViewRootImpl$CalledFromWrongThreadException:
                // only the original thread that created a view hierarchy can touch its views.
                // Valjda je i asistent nesto pricao o tome na vezbama
                mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateRemainingTime(remainingTimeInSeconds);

                        if (remainingTimeInSeconds < 0) {
                            stopTimerForReservationOrTakingOfParkingPlace();

                            if (reservation) {
                                finishReservationOfParkingPlace(true);
                            } else {
                                finishTakingOfParkingPlace(true);
                            }
                        }
                    }
                });
            }
        }, 0, 1000);//put here time 1000 milliseconds=1 second
    }

    private void stopTimerForReservationOrTakingOfParkingPlace() {

        if (timerForReservationOrTakingOfParkingPlace != null) {
            timerForReservationOrTakingOfParkingPlace.setStarted(false);
            timerForReservationOrTakingOfParkingPlace.cancel();
            timerForReservationOrTakingOfParkingPlace.purge();
            // timerForReservationOrTakingOfParkingPlace = new Timer();
        }
    }

    public void startTimerForNotificationReservationOrTakingOfParkingPlace(long endDateTimeInMillis, boolean reservation){
        timerForNotificationReservationOrTakingOfParkingPlace = new TimerWithEndDateTime(endDateTimeInMillis, reservation);
        timerForNotificationReservationOrTakingOfParkingPlace.setStarted(true);
        timerForNotificationReservationOrTakingOfParkingPlace.scheduleAtFixedRate(new TimerTask() {
            Date currentDateTime = new Date();
            long currentDateTimeInMillis = currentDateTime.getTime();
            final long remainingTimeInSeconds = (endDateTimeInMillis - currentDateTimeInMillis) / 1000;

            @Override
            public void run() {
                if (remainingTimeInSeconds < 0) {
                    Toast.makeText(mContext, "Prikaz notifikacije", Toast.LENGTH_SHORT).show();
                    stopTimerForNotificationReservationOrTakingOfParkingPlace();
                    showNotification(reservation);
                }
            }
        }, 0, 1000);
    }

    private void stopTimerForNotificationReservationOrTakingOfParkingPlace() {
        if (timerForNotificationReservationOrTakingOfParkingPlace != null) {
            timerForNotificationReservationOrTakingOfParkingPlace.setStarted(false);
            timerForNotificationReservationOrTakingOfParkingPlace.cancel();
            timerForNotificationReservationOrTakingOfParkingPlace.purge();
            // timerForReservationOrTakingOfParkingPlace = new Timer();
        }
    }


    /*private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getActivity().getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
            //notificationManager.createNotificationChannel(channel);
       }
    }*/

    private void showNotification(boolean reservation) {
        if(reservation){
            mNotificationManagerCompat.notify(NOTIFICATION_ID, notification);
        }

    }

    private void addViolationToUser(boolean reservation) {
        // TODO
        // reservation or taking VIOLATION
    }

    public void finishReservationOfParkingPlace(boolean timeIsUp) {
        stopTimerForReservationOrTakingOfParkingPlace();

        ((TextView) view.findViewById(R.id.txtRemainingTime)).setVisibility(View.GONE);

        if (timeIsUp) {
            mainActivity.resetReservation();
            addViolationToUser(true);
            String message = "Your parking place reservation has expired!";
            dialogForExpiredReservationOrTakingParkingPlace = new NotificationDialog(message, mainActivity);
            dialogForExpiredReservationOrTakingParkingPlace.showDialog();
        }

        if (isInIsReservingMode()) {
            if (mapFragment.getSelectedParkingPlace() == null) {
                setNoneMode();
            }
            else {
                setCanReserveMode();
            }

            mapFragment.tryToFindEmptyParkingPlaceNearbyAndSetMode();
            if (!timeIsUp) {
                throw new InvalidModeException("timeIsUp == false && currentMode == IS_RESERVING"
                        + " (finishReservationOfParkingPlace method)");
                // Vreme jos nije isteklo, a hocemo da zavrsimo rezervaciju.
                // Zauzimajuci parking mesto, rezervacija se zavrsava.
                // Ne radi se ni o ovom slucaju, jer currentMode nije IS_RESERVING_AND_CAN_TAKE.
                // Zakljucak: Nalazimo se u nenormalnom stanju.
            }

        }
        else if (isInIsReservingAndCanTakeMode()) {
            setCanTakeMode();
        }
        else {
            Toast.makeText(mainActivity, "Reservation is expired!", Toast.LENGTH_SHORT).show();
            return;
        }

        mapFragment.finishReservationOfParkingPlace();

        //PRIKAZ NOTIFIKACIJA RADI
        //showNotification(true);
    }

    /**
     *  ova metoda se ne poziva iz glavnog UI thread-a
     */
    public void finishTakingOfParkingPlace(boolean timeIsUp) {
        stopTimerForReservationOrTakingOfParkingPlace();

        ((TextView) view.findViewById(R.id.txtRemainingTime)).setVisibility(View.GONE);

        if (timeIsUp) {
            mainActivity.resetRegularPaidParkingPlace();
            setNoneMode();
            mapFragment.finishTakingOfParkingPlace();
            addViolationToUser(false);
            String message = "Your parking time has expired!";
            dialogForExpiredReservationOrTakingParkingPlace = new NotificationDialog(message, mainActivity);
            dialogForExpiredReservationOrTakingParkingPlace.showDialog();
        }
        else {
            DTO dto = null;
            try {
                dto = mapFragment.checkAndPrepareDtoForLeavingParkingPlaceOnServer();
            } catch (NotFoundParkingPlaceException | AlreadyTakenParkingPlaceException | AlreadyReservedParkingPlaceException
                    | CurrentLocationUnknownException | MaxAllowedDistanceForReservationException e) {
                Toast.makeText(mainActivity, e.getMessage(),Toast.LENGTH_SHORT).show();
                return;
            }

            leaveParkingPlaceOnServer(dto);
        }
    }

    private void leaveParkingPlaceOnServer(DTO dto) {
        ParkingPlaceServerUtils.parkingPlaceService.leaveParkingPlace(dto)
                    .enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                            if (response == null || (response != null && !response.isSuccessful())) {
                                Toast.makeText(mainActivity, "Problem with reservation of parking place.",
                                        Toast.LENGTH_SHORT).show();
                            }
                            else {
                                //Kada se pokrene ponovo app a pa se stisne leave nema te rezervacije vise
                                NotificationUtils.cancelNotification( "taking_notification", paidParkingPlace.getId().intValue());
                                setNoneMode();
                                paidParkingPlace = null;
                                mainActivity.leaveParkingPlace(dto.getParkingPlaceId());
                                mapFragment.finishTakingOfParkingPlace();
                                Toast.makeText(mainActivity, "Parking place is empty now (on server).",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            Toast.makeText(mainActivity, t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
    }

    public void startTimerForUpdatingParkingPlaces() {
        timerForUpdatingParkingPlaces = new Timer();
        timerForUpdatingParkingPlaces.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (zonesForUpdating != null && !zonesForUpdating.isEmpty()) {
                    updateZoneWithParkingPlaceChanges();
                }
            }
        }, 0, 2000);//put here time 2000 milliseconds=2 second
    }

    private void stopTimerForUpdatingParkingPlaces() {
        if (timerForUpdatingParkingPlaces != null) {

            timerForUpdatingParkingPlaces.cancel();
            timerForUpdatingParkingPlaces.purge();
        }
    }

    /*public void showPlaceInfoFragment(ParkingPlace selectedParkingPlace, float distance) {
        TextView textStatus = (TextView) view.findViewById(R.id.status);
        textStatus.setText("Status: " + selectedParkingPlace.getStatus().toString());

        TextView textAddress = (TextView) view.findViewById(R.id.address);
        textAddress.setText("Address: " + selectedParkingPlace.getLocation().getAddress());

        TextView textZone = (TextView) view.findViewById(R.id.zone);
        textZone.setText("Zone: " + selectedParkingPlace.getZone().getName());

        float distanceKm = distance / 1000;
        TextView textDistance = (TextView) view.findViewById(R.id.distance);
        textDistance.setText("Distance: " + distanceKm + "km");

        //((LinearLayout) findViewById(R.id.place_info_linear_layout)).setVisibility(View.VISIBLE);
        LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.place_info_frame);
        linearLayout.setVisibility(View.VISIBLE);
    }*/

    public void hidePlaceIndoFragmet() {
        // view.findViewById(R.id.place_info_frame).setVisibility(View.GONE);

        parkingPlaceInfoFragment.setRealDistance("Real Distance: Computing...");

        showOrHideParkingPlaceInfoFragmentWithTransition(false);
    }

    public void showPlaceInfoFragment(ParkingPlace selectedParkingPlace, float distance) {
        mapFragment.setRealDistanceInParkingPlaceInfo();

        parkingPlaceInfoFragment.setData(selectedParkingPlace, distance);

        //((LinearLayout) findViewById(R.id.place_info_linear_layout)).setVisibility(View.VISIBLE);
        //LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.place_info_frame);
        // linearLayout.setVisibility(View.VISIBLE);
        showOrHideParkingPlaceInfoFragmentWithTransition(true);
    }

    /*public void hidePlaceInfoFragmet() {
        ((LinearLayout) view.findViewById(R.id.place_info_frame)).setVisibility(View.GONE);
        clickedLocation = null;
    }*/

    public void clickOnBtnFindParkingPlace() {
        /*DisplayMetrics displaymetrics = new DisplayMetrics();
        mainActivity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int height = displaymetrics.heightPixels;

        ViewGroup.LayoutParams paramsMap = mapFragment.getView().getLayoutParams();
        paramsMap.height = height/2;
        mapFragment.getView().setLayoutParams(paramsMap);*/

        // findParkingFragment.setTextToSelected(false);

        //LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        //layoutParams.setMargins(0, height/2, 0, 0);
        //linearLayout.setLayoutParams(layoutParams);

        view.findViewById(R.id.findParkingButton).setVisibility(View.GONE);

        //view.findViewById(R.id.find_parking_frame).setVisibility(View.VISIBLE);
        /*View findParkingFrame = view.findViewById(R.id.find_parking_frame);
        findParkingFrame.animate()
                .translationY(findParkingFrame.getHeight())
                .alpha(1.0f)
                .setDuration(500)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        findParkingFrame.setVisibility(View.VISIBLE);
                    }
                })
                .start();*/
        showOrHideFindParkingFragmentWithTransition(true);

        int orientation = this.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            mapFragment.changePaddingOfGoogleMap(false);
            mapFragment.resetMarginsOfMyLocationButton();
        }
        else {
            // landscape mode
            mapFragment.changeMarginsOfMyLocationButton(false, false);
        }
    }

    private void showOrHideFindParkingFragmentWithTransition(boolean show) {
        Transition transition = new Slide(Gravity.BOTTOM);
        transition.setDuration(800);
        transition.addTarget(R.id.find_parking_frame);

        ViewGroup parent = view.findViewById(R.id.mapPageFrameLayout);
        TransitionManager.beginDelayedTransition(parent, transition);
        view.findViewById(R.id.find_parking_frame).setVisibility(show ? View.VISIBLE : View.GONE );
    }

    private void showOrHideParkingPlaceInfoFragmentWithTransition(boolean show) {
        Transition transition = new Slide(Gravity.BOTTOM);
        transition.setDuration(800);
        transition.addTarget(R.id.place_info_frame);

        ViewGroup parent = view.findViewById(R.id.northPanel);
        TransitionManager.beginDelayedTransition(parent, transition);
        view.findViewById(R.id.place_info_frame).setVisibility(show ? View.VISIBLE : View.GONE );
    }

    public boolean isParkingPlaceInfoFragmentVisible() {
        return view.findViewById(R.id.place_info_frame).getVisibility() == View.VISIBLE;
    }

    public boolean isFindParkingPlaceFragmentVisible() {
        return view.findViewById(R.id.find_parking_frame).getVisibility() == View.VISIBLE;
    }

    public void returnGoogleLogoOnStartPosition() {
        mapFragment.changePaddingOfGoogleMap(true);
    }

    /*public void onClickHideFindFragmentButton(View view){
        ((LinearLayout) view.findViewById(R.id.find_parking_frame)).setVisibility(View.GONE);
        DisplayMetrics displaymetrics = new DisplayMetrics();
        mainActivity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int height = displaymetrics.heightPixels;

        ViewGroup.LayoutParams paramsMap = mapFragment.getView().getLayoutParams();
        paramsMap.height = height;
        mapFragment.getView().setLayoutParams(paramsMap);

        ImageButton findParkingButton = (ImageButton) view.findViewById(R.id.findParkingButton);
        findParkingButton.setVisibility(View.VISIBLE);
    }*/

    /*public void clickOnBtnSearchParkingPlace(View view){
        EditText editTextAddress = (EditText) view.findViewById(R.id.address_text_input);
        String addressTextInput = editTextAddress.getText().toString();
        EditText editTextZone = (EditText) view.findViewById(R.id.zone_text_input);
        String zoneTextInput = editTextZone.getText().toString();
        EditText editTextLocation = (EditText) view.findViewById(R.id.location_text_input);
        EditText editTextDistance = (EditText) view.findViewById(R.id.location_distance_text_input);
        float distance = Float.parseFloat(editTextDistance.getText().toString());

        HashMap<Location, ParkingPlace> places = mapFragment.getParkingPlaces();
        ArrayList<ParkingPlace> parkingPlaces = new ArrayList<ParkingPlace>();
        zones = getZones();
        for (ParkingPlace parkingPlace: places.values()) {
            if(!addressTextInput.matches("") && addressTextInput.equals(parkingPlace.getLocation().getAddress()) && chosedSearchMethod.matches("address")){
                Toast.makeText(mainActivity, "search address",Toast.LENGTH_SHORT).show();
                parkingPlaces.add(parkingPlace);
            } else if(!zoneTextInput.matches("") && zoneTextInput.equals(parkingPlace.getZone().getName()) && chosedSearchMethod.matches("zone")){
                Toast.makeText(mainActivity, "search address",Toast.LENGTH_SHORT).show();
                parkingPlaces.add(parkingPlace);
            } else if(!editTextLocation.getText().toString().matches("") && editTextLocation.getText().toString().matches("selected")
                    && chosedSearchMethod.matches("marker")){
                float distanceMarkerCurrentLocation = computeDistanceBetweenTwoPoints(latitude, longitude,
                        parkingPlace.getLocation().getLatitude(), parkingPlace.getLocation().getLongitude());
                if(distanceMarkerCurrentLocation <= distance*1000){
                    parkingPlaces.add(parkingPlace);
                }
                //Toast.makeText(this, "search location",Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(mainActivity, "select search method and fill in the field",Toast.LENGTH_SHORT).show();
            }
        }

        if (!parkingPlaces.isEmpty()) {
            FragmentManager fm = getFragmentManager();
            MapFragment fragm = (MapFragment) fm.findFragmentById(R.id.mapContent);
            double latitude = parkingPlaces.get(0).getLocation().getLatitude();
            double longitude = parkingPlaces.get(0).getLocation().getLongitude();
            com.google.android.gms.maps.model.LatLng lng = new com.google.android.gms.maps.model.LatLng(latitude, longitude);
            fragm.updateCameraPosition(lng, true);
        }
    }*/

    public List<Zone> getZones() {
        /*InputStream is = getResources().openRawResource(R.raw.zones_with_parking_places);
        List<Zone> zones = JsonLoader.getZones(is);*/
        return zones;
    }

    public void setInvisibilityOfMapPageFragmenView() {
        ((LinearLayout) view.findViewById(R.id.find_parking_frame)).setVisibility(View.GONE);
    }

    public void setVisibilityOfFindParkingButton() {
        ImageButton findParkingButton = (ImageButton) view.findViewById(R.id.findParkingButton);
        findParkingButton.setVisibility(View.VISIBLE);
    }

    public int getFindParkingFragmentHeight() {
        return findParkingFragment.getHeight();
    }

    public void setClickedLocation(com.google.android.gms.maps.model.LatLng latLng) {
        findParkingFragment.setClickedLocation(latLng);
    }

    public HashMap<Location, ParkingPlace> getParkingPlaces() {
        return mapFragment.getParkingPlaces();
    }

    public void setInvisibilityOfFindParkingFragment() {
        /*View findParkingFrame = view.findViewById(R.id.find_parking_frame);
        findParkingFrame.animate()
                .translationY(findParkingFrame.getHeight())
                .alpha(0.0f)
                .setDuration(500)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        findParkingFrame.setVisibility(View.GONE);
                    }
                })
                .start();
         */
        showOrHideFindParkingFragmentWithTransition(false);
    }

    public int getFindParkingButtonHeight() {
        View findParkingButton = view.findViewById(R.id.findParkingButton);
        findParkingButton.measure(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        int height = findParkingButton.getMeasuredHeight();
        // na ovaj nacin uzimamo height zato sto kad se prvi put menja visibility sa GONE na VISIBLE,
        // moze se desiti da ocitamo vrednost fragmenta (view) pre nego sto se on resize
        // i onda dobijamo height = 0
        return height;
        // return view.findViewById(R.id.findParkingButton).getHeight();
    }

    public void setRealDistanceInParkingPlaceInfo(double distance) {
        distance = Math.round(distance * 100.0) / 100.0; // zaokruzivanje na dve decimale
        parkingPlaceInfoFragment.setRealDistance("Real Distance: " + distance + "km");
    }

    public int getFindParkingFragmentWidth() {
        return findParkingFragment.getWidth();
    }

    public ParkingPlace getSelectedParkingPlace() {
        return mapFragment.getSelectedParkingPlace();
    }

    public void activateBtnLeaveParkingPlace() {
        int color = ContextCompat.getColor(mainActivity.getApplicationContext(), R.color.colorLeaveParkingPlace);

        Button btnLeaveParkingPlace = (Button) view.findViewById(R.id.btnLeaveParkingPlace);
        btnLeaveParkingPlace.setEnabled(true);
        btnLeaveParkingPlace.setBackgroundColor(color);
    }

    public void updateCameraPosition(com.google.android.gms.maps.model.LatLng latLng) {
        mapFragment.updateCameraPosition(latLng, 18, true);
    }

    public void updateCameraBounds(LatLngBounds latLngBounds) {
        mapFragment.updateCameraBounds(latLngBounds, true);
    }

}
