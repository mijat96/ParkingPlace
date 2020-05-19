package com.rmj.parking_place.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.transition.Slide;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;

import android.util.DisplayMetrics;
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
import com.rmj.parking_place.App;
import com.rmj.parking_place.R;
import com.rmj.parking_place.actvities.MainActivity;
import com.rmj.parking_place.dto.DTO;
import com.rmj.parking_place.dto.ParkingPlaceChangesDTO;
import com.rmj.parking_place.dto.ParkingPlaceDTO;
import com.rmj.parking_place.dto.ParkingPlacesInitialDTO;
import com.rmj.parking_place.dto.ParkingPlacesUpdatingDTO;
import com.rmj.parking_place.dto.TakingDTO;
import com.rmj.parking_place.exceptions.AlreadyReservedParkingPlaceException;
import com.rmj.parking_place.exceptions.AlreadyTakenParkingPlaceException;
import com.rmj.parking_place.exceptions.CurrentLocationUnknownException;
import com.rmj.parking_place.exceptions.InvalidModeException;
import com.rmj.parking_place.exceptions.MaxAllowedDistanceForReservationException;
import com.rmj.parking_place.exceptions.NotFoundParkingPlaceException;
import com.rmj.parking_place.model.Location;
import com.rmj.parking_place.model.Mode;
import com.rmj.parking_place.model.ParkingPlace;
import com.rmj.parking_place.model.Zone;
import com.rmj.parking_place.utils.AsyncResponse;
import com.rmj.parking_place.utils.GetRequestAsyncTask;
import com.rmj.parking_place.utils.HttpRequestAndResponseType;
import com.rmj.parking_place.utils.JsonLoader;
import com.rmj.parking_place.utils.PostRequestAsyncTask;
import com.rmj.parking_place.utils.PutRequestAsyncTask;
import com.rmj.parking_place.utils.Response;
import com.rmj.parking_place.utils.TokenUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MapPageFragment extends Fragment implements AsyncResponse {

    private Mode currentMode;
    private Timer timerForReservationOrTakingOfParkingPlace = new Timer();
    private Timer timerForUpdatingParkingPlaces = new Timer();

    private MapFragment mapFragment;
    private ParkingPlaceInfoFragment parkingPlaceInfoFragment;
    private FindParkingFragment findParkingFragment;

    private List<Zone> zones = null;
    private List<Zone> zonesForUpdating = null;

    private MainActivity mainActivity;
    private View view;

    private com.google.android.gms.maps.model.LatLng clickedLocation;
    private String chosedSearchMethod;

    private double latitude;
    private  double longitude;

    private boolean findParkingFragmentShowed = false;

    public MapPageFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mainActivity = (MainActivity) getActivity();

        this.zonesForUpdating = new ArrayList<Zone>();

        //detectAnyException();

        Toast.makeText(getActivity(), "onCreate()",Toast.LENGTH_SHORT).show();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_map_page, container, false);

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
                clickOnBtnFindParkingPlace(v);
            }
        });

        parkingPlaceInfoFragment = ParkingPlaceInfoFragment.newInstance();
        mapFragment = new MapFragment();
        findParkingFragment = new FindParkingFragment();

        FragmentManager fm = getChildFragmentManager();
        fm.beginTransaction()
            .replace(R.id.place_info_frame, parkingPlaceInfoFragment, "parkingPlaceInfoFragment")
            .replace(R.id.mapContent, mapFragment, "mapFragment")
            .replace(R.id.find_parking_frame, findParkingFragment, "findParkinfFragment")
            .commit();

        setNoneMode();

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
            downloadZones();
        }
        /*else {
            // iscrtaj markere ako nisu iscrtani
            mapFragment.drawParkingPlaceMarkersIfCan();
        }*/

        startTimerForUpdatingParkingPlaces();
    }

    @Override
    public void onPause() {
        super.onPause();
        Toast.makeText(mainActivity, "onPause()",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStop() {
        super.onStop();
        Toast.makeText(mainActivity, "onStop()",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopTimerForUpdatingParkingPlaces();
        Toast.makeText(mainActivity, "onDestroy()",Toast.LENGTH_SHORT).show();
    }


    private void downloadZones() {
        // InputStream is = getResources().openRawResource(R.raw.zones_with_parking_places);
        // List<Zone> zones = JsonLoader.getZones(is);
        //-------------------------------------------------
        // new RequestAsyncTask(this).execute("GET", "https://192.168.1.12:45455/api/zones");
        new GetRequestAsyncTask(this).execute(App.getParkingPlaceServerUrl() + "/api/zones",
                HttpRequestAndResponseType.GET_ZONES.name(), TokenUtils.getToken());
    }

    private void updateZoneWithParkingPlaceChanges() {
        // InputStream is = getResources().openRawResource(R.raw.zones_with_parking_places);
        // List<Zone> zones = JsonLoader.getZones(is);
        //-------------------------------------------------
        // new RequestAsyncTask(this).execute("GET", "https://192.168.1.12:45455/api/zones");

        String url;

        synchronized (this.zonesForUpdating) {
            url = prepareUrlForUpdatingZones(this.zonesForUpdating);
        }

        new GetRequestAsyncTask(this)
                .execute(url, HttpRequestAndResponseType.UPDATE_ZONES_WITH_PARKING_PLACES.name(), TokenUtils.getToken());
    }

    public void selectZonesForUpdating(LatLngBounds currentCameraBounds) {
        if (this.zones == null || (this.zones != null && this.zones.isEmpty())) {
            return;
        }

        com.mapbox.mapboxsdk.geometry.LatLngBounds cameraBounds = convertLatLngBoundsToLatLngBoundsMapboxsdk(currentCameraBounds);
        com.mapbox.mapboxsdk.geometry.LatLngBounds zoneBounds;
        com.mapbox.mapboxsdk.geometry.LatLngBounds intersectBounds;

        synchronized (this.zonesForUpdating) {
            this.zonesForUpdating = new ArrayList<Zone>();
            for (Zone zone : this.zones) {
                zoneBounds = makeLatLngBoundsMapboxsdk(zone.getNorthEast(), zone.getSouthWest());
                intersectBounds = cameraBounds.intersect(zoneBounds);
                if (intersectBounds != null && !intersectBounds.isEmptySpan()) {
                    this.zonesForUpdating.add(zone);
                }
            }
        }
    }

    private com.mapbox.mapboxsdk.geometry.LatLngBounds makeLatLngBoundsMapboxsdk(Location northEast, Location southWest) {
        return new com.mapbox.mapboxsdk.geometry.LatLngBounds.Builder()
                .include(new LatLng(northEast.getLatitude(), northEast.getLongitude()))
                .include(new LatLng(southWest.getLatitude(), southWest.getLongitude()))
                .build();
    }

    private com.mapbox.mapboxsdk.geometry.LatLngBounds convertLatLngBoundsToLatLngBoundsMapboxsdk(LatLngBounds bounds) {
        return new com.mapbox.mapboxsdk.geometry.LatLngBounds.Builder()
                .include(new LatLng(bounds.northeast.latitude, bounds.northeast.longitude))
                .include(new LatLng(bounds.southwest.latitude, bounds.southwest.longitude))
                .build();
    }

    private String prepareUrlForUpdatingZones(List<Zone> zones) {
        String url = App.getParkingPlaceServerUrl() + "/api/parkingplaces/changes?";

        StringBuilder sbZoneIds = new StringBuilder();
        StringBuilder sbVersions = new StringBuilder();

        for (Zone zone : zones) {
            sbZoneIds.append(",");
            sbZoneIds.append(zone.getId());
            sbVersions.append(",");
            sbVersions.append(zone.getVersion());
        }

        url += "zoneids=" + sbZoneIds.substring(1) + "&versions=" + sbVersions.substring(1);
        return url;
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

    public void setIsReservingMode() {
        this.currentMode = Mode.IS_RESERVING;

        int color = ContextCompat.getColor(mainActivity.getApplicationContext(), R.color.colorReserve);
        TextView txtRemainingTime = (TextView) view.findViewById(R.id.txtRemainingTime);
        txtRemainingTime.setTextColor(color);
        txtRemainingTime.setVisibility(View.VISIBLE);

        color = ContextCompat.getColor(mainActivity.getApplicationContext(), R.color.colorDisabledButton);

        Button btnReserve = (Button) view.findViewById(R.id.btnReserve);
        btnReserve.setEnabled(false);
        btnReserve.setBackgroundColor(color);

        Button btnTake = (Button) view.findViewById(R.id.btnTake);
        btnReserve.setEnabled(false);
        btnReserve.setBackgroundColor(color);
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

    public void setIsTakingMode() {
        this.currentMode = Mode.IS_TAKING;
        int color = ContextCompat.getColor(mainActivity.getApplicationContext(), R.color.colorTake);
        TextView txtRemainingTime = (TextView) view.findViewById(R.id.txtRemainingTime);
        txtRemainingTime.setTextColor(color);
        txtRemainingTime.setVisibility(View.VISIBLE);

        color = ContextCompat.getColor(mainActivity.getApplicationContext(), R.color.colorDisabledButton);
        Button btnTake = (Button) view.findViewById(R.id.btnTake);
        btnTake.setEnabled(false);
        btnTake.setBackgroundColor(color);

        color = ContextCompat.getColor(mainActivity.getApplicationContext(), R.color.colorDisabledButton);
        Button btnReserve = (Button) view.findViewById(R.id.btnReserve);
        btnReserve.setEnabled(false);
        btnReserve.setBackgroundColor(color);

        Button btnLeaveParkingPlace = (Button) view.findViewById(R.id.btnLeaveParkingPlace);
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

        DTO dto = null;
        try {
            dto = mapFragment.checkAndPrepareAllForReservingOnServer();
        } catch (NotFoundParkingPlaceException | AlreadyTakenParkingPlaceException | AlreadyReservedParkingPlaceException
                | CurrentLocationUnknownException | MaxAllowedDistanceForReservationException e) {
            Toast.makeText(mainActivity, e.getMessage(),Toast.LENGTH_SHORT).show();
            return;
        }

        reserveParkingPlaceOnServer(dto);
    }

    private void reserveParkingPlace() {
        mapFragment.reserveParkingPlace();

        if (isInCanReserveMode()) {
            setIsReservingMode();
        }
        else if (isInCanReserveAndCanTakeMode()) {
            setIsReservingAndCanTakeMode();
        }
    }

    private void reserveParkingPlaceOnServer(DTO dto) {
        new PostRequestAsyncTask(this, dto).execute(App.getParkingPlaceServerUrl() + "/api/parkingplaces/reservation",
                HttpRequestAndResponseType.RESERVE_PARKING_PLACE.name(), TokenUtils.getToken());
    }

    private void takeParkingPlaceOnServer(TakingDTO dto) {
        new PostRequestAsyncTask(this, dto).execute(App.getParkingPlaceServerUrl() + "/api/parkingplaces/taking",
                HttpRequestAndResponseType.TAKE_PARKING_PLACE.name(), TokenUtils.getToken());
    }

    public void clickOnBtnTake(View view) {
        boolean isReservingAndCanTakeMode = isInIsReservingAndCanTakeMode();
        if(!isInCanTakeMode() && !isInCanReserveAndCanTakeMode() && !isReservingAndCanTakeMode) {
            Toast.makeText(mainActivity, "currentMode == " + currentMode.name()
                    + " (invalid mode) (clickOnBtnTake method)",Toast.LENGTH_SHORT).show();
            return;
        }

        if (isReservingAndCanTakeMode) {
            finishReservationOfParkingPlace(false);
        }

        TakingDTO dto = null;
        try {
            dto = mapFragment.checkAndPrepareAllForTakingOnServer();
        } catch (NotFoundParkingPlaceException | AlreadyReservedParkingPlaceException | AlreadyTakenParkingPlaceException e) {
            Toast.makeText(mainActivity, e.getMessage(),Toast.LENGTH_SHORT).show();
            return;
        }

        takeParkingPlaceOnServer(dto);
    }

    private void takeParkingPlace() {
        mapFragment.takeParkingPlace();
        setIsTakingMode();
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

    public void startTimerForReservationOrTakingOfParkingPlace(long endDateTimeInMillisParam, final boolean reservation) {
        final long endDateTimeInMillis = endDateTimeInMillisParam;

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
        timerForReservationOrTakingOfParkingPlace.cancel();
        timerForReservationOrTakingOfParkingPlace.purge();
        timerForReservationOrTakingOfParkingPlace = new Timer();
    }

    private void addViolationToUser(boolean reservation) {
        // TODO
        // reservation or taking VIOLATION
    }


    public void finishReservationOfParkingPlace(boolean timeIsUp) {
        stopTimerForReservationOrTakingOfParkingPlace();

        ((TextView) view.findViewById(R.id.txtRemainingTime)).setVisibility(View.GONE);

        if (timeIsUp) {
            addViolationToUser(true);
            String message = "Your parking place reservation has expired!";
            showDialogWithSingleButton(message, "Close");
        }

        if (isInIsReservingMode()) {
            setNoneMode();
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

        mapFragment.finishReservationOfParkingPlace();
    }

    /**
     *  ova metoda se ne poziva iz glavnog UI thread-a
     */
    public void finishTakingOfParkingPlace(boolean timeIsUp) {
        stopTimerForReservationOrTakingOfParkingPlace();

        ((TextView) view.findViewById(R.id.txtRemainingTime)).setVisibility(View.GONE);

        if (timeIsUp) {
            setNoneMode();
            mapFragment.finishTakingOfParkingPlace();
            addViolationToUser(false);
            String message = "Your parking time has expired!";
            showDialogWithSingleButton(message, "Close");
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
        new PutRequestAsyncTask(this, dto).execute(App.getParkingPlaceServerUrl() + "/api/parkingplaces/leave",
                HttpRequestAndResponseType.LEAVE_PARKING_PLACE.name(), TokenUtils.getToken());
    }

    private void showDialogWithSingleButton(String messageParam, String buttonTextParam) {
        final String message = messageParam;
        final String buttonText = buttonTextParam;

        AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
        builder.setMessage(message)
                .setCancelable(false)
                // .setPositiveButton()
                .setNeutralButton(buttonText, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.setCanceledOnTouchOutside(false);
        alert.show();
    }

    @Override
    public void processFinish(Response response) {
        if (response.getType() == HttpRequestAndResponseType.GET_ZONES) {
            if (response.getResult().equals("NOT_CONNECTED") || response.getResult().equals("FAIL")) {
                Toast.makeText(mainActivity, "[response_result = " + response.getResult() + "] Problem with loading zones. We will try again.",
                        Toast.LENGTH_SHORT).show();
                new GetRequestAsyncTask(this).execute(App.getParkingPlaceServerUrl() + "/api/zones",
                        HttpRequestAndResponseType.GET_ZONES.name(), TokenUtils.getToken());
            }
            else {
                this.zones = JsonLoader.convertJsonToZones(response.getResult());
               /* HashMap<Location, ParkingPlace> parkingPlaces = new HashMap<Location, ParkingPlace>();

                for (Zone zone : zones) {
                    for (ParkingPlace parkingPlace : zone.getParkingPlaces()) {
                        parkingPlace.setZone(zone);
                        parkingPlaces.put(parkingPlace.getLocation(), parkingPlace);
                    }
                }
                mapFragment.setParkingPlaces(parkingPlaces);
                mapFragment.drawParkingPlaceMarkersIfCan();*/
            }
        }
        else if (response.getType() == HttpRequestAndResponseType.UPDATE_ZONES_WITH_PARKING_PLACES) {
            if (response.getResult().equals("NOT_CONNECTED") || response.getResult().equals("FAIL")) {
                Toast.makeText(mainActivity, "Problem with updating zones with parking places.",
                        Toast.LENGTH_SHORT).show();
            }
            else {
                ParkingPlacesUpdatingDTO parkingPlacesUpdatingDTO =
                        JsonLoader.convertJsonToParkingPlacesUpdatingDTO(response.getResult());
                ArrayList<ParkingPlace> parkingPlacesForAdding = new ArrayList<ParkingPlace>();
                List<ParkingPlace> parkingPlacesForUpdating = new ArrayList<ParkingPlace>();

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

                for (ParkingPlaceChangesDTO parkingPlaceChangesDTO : parkingPlacesUpdatingDTO.getChanges()) {
                    for (Zone zone : zones) {
                        if (parkingPlaceChangesDTO.getZoneId().equals(zone.getId())) {
                            if (parkingPlaceChangesDTO.getVersion() > zone.getVersion()) {
                                zone.setVersion(parkingPlaceChangesDTO.getVersion());
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
                }

                if (!parkingPlacesForAdding.isEmpty() || !parkingPlacesForUpdating.isEmpty()) {
                    mapFragment.tryToFindEmptyParkingPlaceNearbyAndSetMode();
                }
            }
        }
        else if (response.getType() == HttpRequestAndResponseType.RESERVE_PARKING_PLACE) {
            if (response.getResult().equals("FAIL")) {
                Toast.makeText(mainActivity, "Problem with reservation of parking place.",
                        Toast.LENGTH_SHORT).show();
            }
            else {
                reserveParkingPlace();
            }
        }
        else if (response.getType() == HttpRequestAndResponseType.TAKE_PARKING_PLACE) {
            if (response.getResult().equals("FAIL")) {
                Toast.makeText(mainActivity, "Problem with taking of parking place.",
                        Toast.LENGTH_SHORT).show();
            }
            else {
                takeParkingPlace();
            }
        }

        else if (response.getType() == HttpRequestAndResponseType.LEAVE_PARKING_PLACE) {
            if (response.getResult().equals("NOT_CONNECTED") || response.getResult().equals("FAIL")) {
                Toast.makeText(mainActivity, "[response_result = " + response.getResult() + "] Problem with leaving parking place.",
                        Toast.LENGTH_SHORT).show();
            }
            else {
                setNoneMode();
                mapFragment.finishTakingOfParkingPlace();
                Toast.makeText(mainActivity, "Parking place is empty now (on server).",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void loginAgain() {
       mainActivity.loginAgain();
    }

    public void startTimerForUpdatingParkingPlaces() {
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
        timerForUpdatingParkingPlaces.cancel();
        timerForUpdatingParkingPlaces.purge();
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
        showOrHideParkingPlaceInfoFragmentWithTransition(false);
        clickedLocation = null;
    }

    public void showPlaceInfoFragment(ParkingPlace selectedParkingPlace, float distance) {
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
        //LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.place_info_frame);
        // linearLayout.setVisibility(View.VISIBLE);
        showOrHideParkingPlaceInfoFragmentWithTransition(true);
    }

    /*public void hidePlaceInfoFragmet() {
        ((LinearLayout) view.findViewById(R.id.place_info_frame)).setVisibility(View.GONE);
        clickedLocation = null;
    }*/

    public void clickOnBtnFindParkingPlace(View v) {
        /*DisplayMetrics displaymetrics = new DisplayMetrics();
        mainActivity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int height = displaymetrics.heightPixels;

        ViewGroup.LayoutParams paramsMap = mapFragment.getView().getLayoutParams();
        paramsMap.height = height/2;
        mapFragment.getView().setLayoutParams(paramsMap);*/

        findParkingFragmentShowed = true;

        findParkingFragment.setTextToSelected(clickedLocation == null);

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

        mapFragment.changePositionOfGoogleLogo(false);
        mapFragment.changePositionOfMyLocationButton(false);
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

    public void returnGoogleLogoOnStartPosition() {
        mapFragment.changePositionOfGoogleLogo(true);
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
        findParkingFragmentShowed = false;
    }

    public boolean isFindParkingFragmentShowed() {
        return findParkingFragmentShowed;
    }

    public int getFindParkingButtonHeight() {
        return view.findViewById(R.id.findParkingButton).getHeight();
    }

}
