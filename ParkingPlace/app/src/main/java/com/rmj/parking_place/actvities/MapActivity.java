package com.rmj.parking_place.actvities;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLngBounds;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.rmj.parking_place.R;
import com.rmj.parking_place.actvities.login.ui.LoginActivity;
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
import com.rmj.parking_place.fragments.MapFragment;
import com.rmj.parking_place.model.Location;
import com.rmj.parking_place.model.Mode;
import com.rmj.parking_place.model.ParkingPlace;
import com.rmj.parking_place.model.Zone;
import com.rmj.parking_place.tools.FragmentTransition;
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
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MapActivity extends CheckWifiActivity /*AppCompatActivity*/ implements AsyncResponse {

    private Mode currentMode;
    private Timer timerForReservationOrTakingOfParkingPlace = new Timer();
    private Timer timerForUpdatingParkingPlaces = new Timer();
    private MapFragment mapFragment;

    private List<Zone> zones = null;
    private List<Zone> zonesForUpdating = null;

    private SharedPreferences sharedPreferences;
    private TokenUtils tokenUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        tokenUtils = new TokenUtils(sharedPreferences);

        this.zonesForUpdating = new ArrayList<Zone>();

        //detectAnyException();

        setNoneMode();
        mapFragment = new MapFragment();
        FragmentTransition.to(mapFragment, this, false);

        Toast.makeText(this, "onCreate()",Toast.LENGTH_SHORT).show();
    }

    private String getParkingPlaceServerUrl() {
        String parkingPlaceServerUrl = sharedPreferences.getString("parkingPlaceServerUrl","");
        if (parkingPlaceServerUrl.equals("")) {
            parkingPlaceServerUrl = getString(R.string.PARKING_PLACE_SERVER_BASE_URL);
        }

        return  parkingPlaceServerUrl;
    }

    /*private void detectAnyException() {
        final MainActivity that = this;
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(final Thread paramThread, final Throwable paramThrowable) {
                //Catch your exception
                // Without System.exit() this will not work.
                new Thread() {
                    @Override
                    public void run() {
                        Looper.prepare();
                        Toast.makeText(that,paramThrowable.getMessage(), Toast.LENGTH_LONG).show();
                        Looper.loop();
                    }
                }.start();
                try
                {
                    Thread.sleep(4000); // Let the Toast display before app will get shutdown
                }
                catch (InterruptedException e) {    }
                System.exit(2);
            }
        });
    }*/

    @Override
    protected void onStart() {
        super.onStart();
        Toast.makeText(this, "onStart()",Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Toast.makeText(this, "onResume()",Toast.LENGTH_SHORT).show();
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
    protected void onPause() {
        super.onPause();
        Toast.makeText(this, "onPause()",Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Toast.makeText(this, "onStop()",Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTimerForUpdatingParkingPlaces();
        Toast.makeText(this, "onDestroy()",Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Toast.makeText(this, "onRestart()",Toast.LENGTH_SHORT).show();
    }

    private void downloadZones() {
        // InputStream is = getResources().openRawResource(R.raw.zones_with_parking_places);
        // List<Zone> zones = JsonLoader.getZones(is);
        //-------------------------------------------------
        // new RequestAsyncTask(this).execute("GET", "https://192.168.1.12:45455/api/zones");
        new GetRequestAsyncTask(this).execute(getParkingPlaceServerUrl() + "/api/zones",
                HttpRequestAndResponseType.GET_ZONES.name(), tokenUtils.getToken());
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
                .execute(url, HttpRequestAndResponseType.UPDATE_ZONES_WITH_PARKING_PLACES.name(), tokenUtils.getToken());
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
        String url = getParkingPlaceServerUrl() + "/api/parkingplaces/changes?";

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

        int color = ContextCompat.getColor(getApplicationContext(), R.color.colorDisabledButton);

        Button btnReserve = (Button) findViewById(R.id.btnReserve);
        btnReserve.setEnabled(false);
        btnReserve.setBackgroundColor(color);

        Button btnTake = (Button) findViewById(R.id.btnTake);
        btnTake.setEnabled(false);
        btnTake.setBackgroundColor(color);

        // ((TextView) findViewById(R.id.txtRemainingTime)).setVisibility(View.INVISIBLE);
        // ((LinearLayout) findViewById(R.id.northPanel)).setVisibility(View.GONE);
        // GONE - This view is invisible, and it doesn't take any space for layout purposes.
    }

    public void setCanReserveMode() {
        this.currentMode = Mode.CAN_RESERVE;

        int color = ContextCompat.getColor(getApplicationContext(), R.color.colorReserve);
        Button btnReserve = (Button) findViewById(R.id.btnReserve);
        btnReserve.setEnabled(true);
        btnReserve.setBackgroundColor(color);

        color = ContextCompat.getColor(getApplicationContext(), R.color.colorDisabledButton);

        Button btnTake = (Button) findViewById(R.id.btnTake);
        btnTake.setEnabled(false);
        btnTake.setBackgroundColor(color);

        // ((LinearLayout) findViewById(R.id.northPanel)).setVisibility(View.VISIBLE);
    }

    public void setCanTakeMode() {
        this.currentMode = Mode.CAN_TAKE;

        int color = ContextCompat.getColor(getApplicationContext(), R.color.colorTake);
        Button btnTake = (Button) findViewById(R.id.btnTake);
        btnTake.setEnabled(true);
        btnTake.setBackgroundColor(color);

        color = ContextCompat.getColor(getApplicationContext(), R.color.colorDisabledButton);

        Button btnReserve = (Button) findViewById(R.id.btnReserve);
        btnReserve.setEnabled(false);
        btnReserve.setBackgroundColor(color);
        // ((LinearLayout) findViewById(R.id.northPanel)).setVisibility(View.VISIBLE);
    }

    public void setIsReservingMode() {
        this.currentMode = Mode.IS_RESERVING;

        int color = ContextCompat.getColor(getApplicationContext(), R.color.colorReserve);
        TextView txtRemainingTime = (TextView) findViewById(R.id.txtRemainingTime);
        txtRemainingTime.setTextColor(color);
        txtRemainingTime.setVisibility(View.VISIBLE);

        color = ContextCompat.getColor(getApplicationContext(), R.color.colorDisabledButton);

        Button btnReserve = (Button) findViewById(R.id.btnReserve);
        btnReserve.setEnabled(false);
        btnReserve.setBackgroundColor(color);

        Button btnTake = (Button) findViewById(R.id.btnTake);
        btnReserve.setEnabled(false);
        btnReserve.setBackgroundColor(color);
    }

    public void setIsReservingAndCanTakeMode() {
        this.currentMode = Mode.IS_RESERVING_AND_CAN_TAKE;
        int color = ContextCompat.getColor(getApplicationContext(), R.color.colorReserve);
        TextView txtRemainingTime = (TextView) findViewById(R.id.txtRemainingTime);
        txtRemainingTime.setTextColor(color);
        txtRemainingTime.setVisibility(View.VISIBLE);

        color = ContextCompat.getColor(getApplicationContext(), R.color.colorDisabledButton);
        Button btnReserve = (Button) findViewById(R.id.btnReserve);
        btnReserve.setEnabled(false);
        btnReserve.setBackgroundColor(color);

        color = ContextCompat.getColor(getApplicationContext(), R.color.colorTake);
        Button btnTake = (Button) findViewById(R.id.btnTake);
        btnTake.setEnabled(true);
        btnTake.setBackgroundColor(color);

    }

    public void setIsTakingMode() {
        this.currentMode = Mode.IS_TAKING;
        int color = ContextCompat.getColor(getApplicationContext(), R.color.colorTake);
        TextView txtRemainingTime = (TextView) findViewById(R.id.txtRemainingTime);
        txtRemainingTime.setTextColor(color);
        txtRemainingTime.setVisibility(View.VISIBLE);

        color = ContextCompat.getColor(getApplicationContext(), R.color.colorDisabledButton);
        Button btnTake = (Button) findViewById(R.id.btnTake);
        btnTake.setEnabled(false);
        btnTake.setBackgroundColor(color);

        color = ContextCompat.getColor(getApplicationContext(), R.color.colorDisabledButton);
        Button btnReserve = (Button) findViewById(R.id.btnReserve);
        btnReserve.setEnabled(false);
        btnReserve.setBackgroundColor(color);

        Button btnLeaveParkingPlace = (Button) findViewById(R.id.btnLeaveParkingPlace);
        btnLeaveParkingPlace.setVisibility(View.VISIBLE);
    }

    public void setCanReserveAndCanTakeMode() {
        currentMode = Mode.CAN_RESERVE_AND_CAN_TAKE;

        int color = ContextCompat.getColor(getApplicationContext(), R.color.colorReserve);
        Button btnReserve = (Button) findViewById(R.id.btnReserve);
        btnReserve.setEnabled(true);
        btnReserve.setBackgroundColor(color);

        color = ContextCompat.getColor(getApplicationContext(), R.color.colorTake);
        Button btnTake = (Button) findViewById(R.id.btnTake);
        btnTake.setEnabled(true);
        btnTake.setBackgroundColor(color);
    }

    public void clickOnBtnReserve(View view) {
        if(!isInCanReserveMode() && !isInCanReserveAndCanTakeMode()) {
            Toast.makeText(this, "currentMode == " + currentMode.name()
                    + " (invalid mode) (clickOnBtnReserve method)",Toast.LENGTH_SHORT).show();
            return;
        }

        DTO dto = null;
        try {
            dto = mapFragment.checkAndPrepareAllForReservingOnServer();
        } catch (NotFoundParkingPlaceException | AlreadyTakenParkingPlaceException | AlreadyReservedParkingPlaceException
                | CurrentLocationUnknownException | MaxAllowedDistanceForReservationException e) {
            Toast.makeText(this, e.getMessage(),Toast.LENGTH_SHORT).show();
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
        new PostRequestAsyncTask(this, dto).execute(getParkingPlaceServerUrl() + "/api/parkingplaces/reservation",
                HttpRequestAndResponseType.RESERVE_PARKING_PLACE.name(), tokenUtils.getToken());
    }

    private void takeParkingPlaceOnServer(TakingDTO dto) {
        new PostRequestAsyncTask(this, dto).execute(getParkingPlaceServerUrl() + "/api/parkingplaces/taking",
                HttpRequestAndResponseType.TAKE_PARKING_PLACE.name(), tokenUtils.getToken());
    }

    public void clickOnBtnTake(View view) {
        boolean isReservingAndCanTakeMode = isInIsReservingAndCanTakeMode();
        if(!isInCanTakeMode() && !isInCanReserveAndCanTakeMode() && !isReservingAndCanTakeMode) {
            Toast.makeText(this, "currentMode == " + currentMode.name()
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
            Toast.makeText(this, e.getMessage(),Toast.LENGTH_SHORT).show();
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
        ((TextView) findViewById(R.id.txtRemainingTime)).setText(remainingTimeStr);
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
        Button btnLeaveParkingPlace = (Button) findViewById(R.id.btnLeaveParkingPlace);
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
                runOnUiThread(new Runnable() {
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

        ((TextView) findViewById(R.id.txtRemainingTime)).setVisibility(View.GONE);

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

        ((TextView) findViewById(R.id.txtRemainingTime)).setVisibility(View.GONE);

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
                Toast.makeText(this, e.getMessage(),Toast.LENGTH_SHORT).show();
                return;
            }

            leaveParkingPlaceOnServer(dto);
        }
    }

    private void leaveParkingPlaceOnServer(DTO dto) {
        new PutRequestAsyncTask(this, dto).execute(getParkingPlaceServerUrl() + "/api/parkingplaces/leave",
                HttpRequestAndResponseType.LEAVE_PARKING_PLACE.name(), tokenUtils.getToken());
    }

    private void showDialogWithSingleButton(String messageParam, String buttonTextParam) {
        final String message = messageParam;
        final String buttonText = buttonTextParam;
        final MapActivity that = this;

        AlertDialog.Builder builder = new AlertDialog.Builder(that);
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
                Toast.makeText(this, "[response_result = " + response.getResult() + "] Problem with loading zones. We will try again.",
                        Toast.LENGTH_SHORT).show();
                new GetRequestAsyncTask(this).execute(getParkingPlaceServerUrl() + "/api/zones",
                        HttpRequestAndResponseType.GET_ZONES.name(), tokenUtils.getToken());
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
                Toast.makeText(this, "Problem with updating zones with parking places.",
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
                Toast.makeText(this, "Problem with reservation of parking place.",
                        Toast.LENGTH_SHORT).show();
            }
            else {
                reserveParkingPlace();
            }
        }
        else if (response.getType() == HttpRequestAndResponseType.TAKE_PARKING_PLACE) {
            if (response.getResult().equals("FAIL")) {
                Toast.makeText(this, "Problem with taking of parking place.",
                        Toast.LENGTH_SHORT).show();
            }
            else {
                takeParkingPlace();
            }
        }

        else if (response.getType() == HttpRequestAndResponseType.LEAVE_PARKING_PLACE) {
            if (response.getResult().equals("NOT_CONNECTED") || response.getResult().equals("FAIL")) {
                Toast.makeText(this, "[response_result = " + response.getResult() + "] Problem with leaving parking place.",
                        Toast.LENGTH_SHORT).show();
            }
            else {
                setNoneMode();
                mapFragment.finishTakingOfParkingPlace();
                Toast.makeText(this, "Parking place is empty now (on server).",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String getCurrentActivity() {
        ComponentName cn;
        ActivityManager am = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            cn = am.getAppTasks().get(0).getTaskInfo().topActivity;
        } else {
            //noinspection deprecation
            cn = am.getRunningTasks(1).get(0).topActivity;
        }

        return cn.getClassName();
    }

    @Override
    public void loginAgain() {
        String currentActivity = getCurrentActivity();
        if (currentActivity.endsWith("LoginActivity")) { // vec se nalazi na login activity-ju
            return;
        }

        tokenUtils.removeToken();
        startActivity(new Intent(MapActivity.this, LoginActivity.class));
        Toast.makeText(this, "Please login again.", Toast.LENGTH_SHORT).show();
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

}
