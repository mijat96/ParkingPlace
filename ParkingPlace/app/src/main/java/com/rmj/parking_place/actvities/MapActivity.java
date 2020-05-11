package com.rmj.parking_place.actvities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.rmj.parking_place.R;
import com.rmj.parking_place.actvities.login.ui.LoginActivity;
import com.rmj.parking_place.dto.ParkingPlaceChangesDTO;
import com.rmj.parking_place.dto.ParkingPlaceDTO;
import com.rmj.parking_place.dto.ReservationDTO;
import com.rmj.parking_place.dto.TakingDTO;
import com.rmj.parking_place.exceptions.AlreadyReservedParkingPlaceException;
import com.rmj.parking_place.exceptions.AlreadyTakenParkingPlaceException;
import com.rmj.parking_place.exceptions.CurrentLocationUnknownException;
import com.rmj.parking_place.exceptions.InvalidModeException;
import com.rmj.parking_place.exceptions.MaxAllowedDistanceForReservationException;
import com.rmj.parking_place.exceptions.NotFoundParkingPlaceException;
import com.rmj.parking_place.fragments.FindParkingFragment;
import com.rmj.parking_place.fragments.MapFragment;
import com.rmj.parking_place.fragments.ParkingPlaceInfoFragment;
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
import com.rmj.parking_place.utils.Response;
import com.rmj.parking_place.utils.TokenUtils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MapActivity extends AppCompatActivity implements AsyncResponse {

    //private static final String PARKING_PLACE_SERVER_BASE_URL = "https://parkingplaceserver.conveyor.cloud";
    private static final String PARKING_PLACE_SERVER_BASE_URL = "https://parkingplaceserver-tm8.conveyor.cloud";

    private Mode currentMode;
    private Timer timerForReservationOrTakingOfParkingPlace = new Timer();
    private Timer timerForUpdatingParkingPlaces = new Timer();
    private MapFragment mapFragment;
    private LatLng clickedLocation;
    private String chosedSearchMethod;
    private List<Zone> zones = null;
    private List<Zone> zonesForUpdating = null;

    private double latitude;
    private  double longitude;
    private TokenUtils tokenUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tokenUtils = new TokenUtils(this);

        //detectAnyException();

        setNoneMode();
        mapFragment = new MapFragment();
        FragmentTransition.to(mapFragment, this, false);

        Toast.makeText(this, "onCreate()",Toast.LENGTH_SHORT).show();
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
        if (zones == null) {
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
        mapFragment.resetDrawingFinished();
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
        new GetRequestAsyncTask(this).execute(PARKING_PLACE_SERVER_BASE_URL + "/api/zones",
                HttpRequestAndResponseType.GET_ZONES_WITH_PARKING_PLACES.name(), tokenUtils.getToken());
    }

    private void updateZoneWithParkingPlaceChanges() {
        // InputStream is = getResources().openRawResource(R.raw.zones_with_parking_places);
        // List<Zone> zones = JsonLoader.getZones(is);
        //-------------------------------------------------
        // new RequestAsyncTask(this).execute("GET", "https://192.168.1.12:45455/api/zones");
        String url = prepareUrlForUpdating(this.zonesForUpdating);
        new GetRequestAsyncTask(this)
                .execute(url, HttpRequestAndResponseType.UPDATE_ZONES_WITH_PARKING_PLACES.name(), tokenUtils.getToken());
    }

    private String prepareUrlForUpdating(List<Zone> zones) {
        String url = PARKING_PLACE_SERVER_BASE_URL + "/api/parkingplaces/changes?";

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

        // ((LinearLayout) findViewById(R.id.northPanel)).setVisibility(View.VISIBLE);
    }

    public void setCanTakeMode() {
        this.currentMode = Mode.CAN_TAKE;

        int color = ContextCompat.getColor(getApplicationContext(), R.color.colorTake);
        Button btnTake = (Button) findViewById(R.id.btnTake);
        btnTake.setEnabled(true);
        btnTake.setBackgroundColor(color);

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

        ReservationDTO dto = null;
        try {
            dto = mapFragment.checkAndPrepareDtoForReservingOnServer();
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

    private void reserveParkingPlaceOnServer( ReservationDTO dto) {
        new PostRequestAsyncTask(this, dto).execute(PARKING_PLACE_SERVER_BASE_URL + "/api/parkingplaces/reservation",
                HttpRequestAndResponseType.RESERVE_PARKING_PLACE.name(), tokenUtils.getToken());
    }

    private void takeParkingPlaceOnServer(TakingDTO dto) {
        new PostRequestAsyncTask(this, dto).execute(PARKING_PLACE_SERVER_BASE_URL + "/api/parkingplaces/taking",
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
            dto = mapFragment.checkAndPrepareDtoForTakingOnServer();
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
            addViolationToUser(false);
            String message = "Your parking time has expired!";
            showDialogWithSingleButton(message, "Close");
        }

        setNoneMode();
        mapFragment.finishTakingOfParkingPlace();
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
        if (response.getType() == HttpRequestAndResponseType.GET_ZONES_WITH_PARKING_PLACES) {
            if (response.getResult().equals("FAIL")) {
                Toast.makeText(this, "Problem with loading zones with parking places. We will try again.",
                        Toast.LENGTH_SHORT).show();
                new GetRequestAsyncTask(this).execute(PARKING_PLACE_SERVER_BASE_URL + "/api/zones",
                        HttpRequestAndResponseType.GET_ZONES_WITH_PARKING_PLACES.name(), tokenUtils.getToken());
            }
            else {
                this.zones = JsonLoader.convertJsonToZones(response.getResult());
                HashMap<Location, ParkingPlace> parkingPlaces = new HashMap<Location, ParkingPlace>();

                for (Zone zone : zones) {
                    for (ParkingPlace parkingPlace : zone.getParkingPlaces()) {
                        parkingPlace.setZone(zone);
                        parkingPlaces.put(parkingPlace.getLocation(), parkingPlace);
                    }
                }
                mapFragment.setParkingPlaces(parkingPlaces);
                mapFragment.drawParkingPlaceMarkersIfCan();
            }
        }
        else if (response.getType() == HttpRequestAndResponseType.UPDATE_ZONES_WITH_PARKING_PLACES) {
            if (response.getResult().equals("FAIL")) {
                Toast.makeText(this, "Problem with updating zones with parking places.",
                        Toast.LENGTH_SHORT).show();
            }
            else {
                List<ParkingPlaceChangesDTO> parkingPlaceChangesDTOs =
                            JsonLoader.convertJsonToParkingPlaceChangesDTOs(response.getResult());
                List<ParkingPlace> parkingPlacesForUpdating = new ArrayList<ParkingPlace>();

                for (ParkingPlaceChangesDTO parkingPlaceChangesDTO : parkingPlaceChangesDTOs) {
                    for (Zone zone : zones) {
                        if (parkingPlaceChangesDTO.getZoneId().equals(zone.getId())) {
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
                mapFragment.updateParkingPlaceMarkers(parkingPlacesForUpdating);
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
    }

    @Override
    public void loginAgain() {
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

    public void showPlaceInfoFragment(ParkingPlace selectedParkingPlace, float distance) {
        TextView textStatus = (TextView) findViewById(R.id.status);
        textStatus.setText("Status: " + selectedParkingPlace.getStatus().toString());

        TextView textAddress = (TextView) findViewById(R.id.address);
        textAddress.setText("Address: " + selectedParkingPlace.getLocation().getAddress());

        TextView textZone = (TextView) findViewById(R.id.zone);
        textZone.setText("Zone: " + selectedParkingPlace.getZone().getName());

        float distanceKm = distance / 1000;
        TextView textDistance = (TextView) findViewById(R.id.distance);
        textDistance.setText("Distance: " + distanceKm + "km");

        //((LinearLayout) findViewById(R.id.place_info_linear_layout)).setVisibility(View.VISIBLE);
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.place_info_frame);
        linearLayout.setVisibility(View.VISIBLE);
    }

    public void hidePlaceInfoFragmet() {
        ((LinearLayout) findViewById(R.id.place_info_frame)).setVisibility(View.GONE);
        clickedLocation = null;
    }

    public void clickOnBtnFindParkingPlace(View view) {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int height = displaymetrics.heightPixels;

        ViewGroup.LayoutParams paramsMap = mapFragment.getView().getLayoutParams();
        paramsMap.height = height/2;
        mapFragment.getView().setLayoutParams(paramsMap);

        EditText editTextLocation = (EditText) findViewById(R.id.location_text_input);

        if(clickedLocation == null){
            editTextLocation.setText("not selected");
        }else
        {
            editTextLocation.setText("selected");
        }

        //LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        //layoutParams.setMargins(0, height/2, 0, 0);
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.find_parking_frame);
        //linearLayout.setLayoutParams(layoutParams);
        linearLayout.setVisibility(View.VISIBLE);

        ImageButton findParkingButton = (ImageButton) findViewById(R.id.finParkingButton);
        findParkingButton.setVisibility(View.GONE);
    }

    public void onCheckboxSearchClicked(View view){
        boolean checked = ((CheckBox) view).isChecked();
        CheckBox markerCheckBox =(CheckBox) findViewById(R.id.markerCheckBox);
        CheckBox zoneCheckBox =(CheckBox) findViewById(R.id.zoneCheckBox);
        CheckBox addressCheckBox =(CheckBox) findViewById(R.id.addressCheckBox);
        EditText editTextAddress = (EditText) findViewById(R.id.address_text_input);
        EditText editTextZone = (EditText) findViewById(R.id.zone_text_input);
        EditText editTextMarker = (EditText) findViewById(R.id.location_text_input);
        EditText editTextlocationDistance = (EditText) findViewById(R.id.location_distance_text_input);

        switch(view.getId()) {
            case R.id.addressCheckBox:
                if (checked){
                    chosedSearchMethod = "address";
                    markerCheckBox.setChecked(false);
                    zoneCheckBox.setChecked(false);
                    editTextZone.setFocusable(false);
                    editTextMarker.setFocusable(false);
                    editTextlocationDistance.setFocusable(false);
                    editTextAddress.setFocusable(true);
                    editTextAddress.setFocusableInTouchMode(true);
                    editTextAddress.requestFocus();
                }
                else{
                    chosedSearchMethod = "";
                    editTextAddress.setFocusable(false);
                }
                break;
            case R.id.zoneCheckBox:
                if (checked){
                    chosedSearchMethod = "zone";
                    markerCheckBox.setChecked(false);
                    addressCheckBox.setChecked(false);
                    editTextAddress.setFocusable(false);
                    editTextMarker.setFocusable(false);
                    editTextlocationDistance.setFocusable(false);
                    editTextZone.setFocusable(true);
                    editTextZone.setFocusableInTouchMode(true);
                    editTextZone.requestFocus();
                }
                else{
                    chosedSearchMethod = "";
                    editTextZone.setFocusable(false);
                }
                break;
            case R.id.markerCheckBox:
                if (checked){
                    chosedSearchMethod = "marker";
                    addressCheckBox.setChecked(false);
                    zoneCheckBox.setChecked(false);
                    editTextZone.setFocusable(false);
                    editTextAddress.setFocusable(false);
                    editTextlocationDistance.setFocusable(true);
                    editTextlocationDistance.setFocusableInTouchMode(true);
                    editTextlocationDistance.requestFocus();
                }
                else{
                    chosedSearchMethod = "";
                    editTextMarker.setFocusable(false);
                    editTextlocationDistance.setFocusable(false);
                }
                break;
            // TODO: Veggie sandwich
        }
    }

    public void onClickHideFindFragmentButton(View view){
        ((LinearLayout) findViewById(R.id.find_parking_frame)).setVisibility(View.GONE);
        DisplayMetrics displaymetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int height = displaymetrics.heightPixels;

        ViewGroup.LayoutParams paramsMap = mapFragment.getView().getLayoutParams();
        paramsMap.height = height;
        mapFragment.getView().setLayoutParams(paramsMap);

        ImageButton findParkingButton = (ImageButton) findViewById(R.id.finParkingButton);
        findParkingButton.setVisibility(View.VISIBLE);
    }

    public void clickOnBtnSearchParkingPlace(View view){
        EditText editTextAddress = (EditText) findViewById(R.id.address_text_input);
        String addressTextInput = editTextAddress.getText().toString();
        EditText editTextZone = (EditText) findViewById(R.id.zone_text_input);
        String zoneTextInput = editTextZone.getText().toString();
        EditText editTextLocation = (EditText) findViewById(R.id.location_text_input);
        EditText editTextDistance = (EditText) findViewById(R.id.location_distance_text_input);
        float distance = Float.parseFloat(editTextDistance.getText().toString());

        HashMap<com.rmj.parking_place.model.Location, ParkingPlace> places = mapFragment.getParkingPlaces();
        ArrayList<ParkingPlace> parkingPlaces = new ArrayList<ParkingPlace>();
        zones = getZones();
        for (ParkingPlace parkingPlace: places.values()) {
            if(!addressTextInput.matches("") && addressTextInput.equals(parkingPlace.getLocation().getAddress()) && chosedSearchMethod.matches("address")){
                Toast.makeText(this, "search address",Toast.LENGTH_SHORT).show();
                parkingPlaces.add(parkingPlace);
            } else if(!zoneTextInput.matches("") && zoneTextInput.equals(parkingPlace.getZone().getName()) && chosedSearchMethod.matches("zone")){
                Toast.makeText(this, "search address",Toast.LENGTH_SHORT).show();
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
                Toast.makeText(this, "select search method and fill in the field",Toast.LENGTH_SHORT).show();
            }
        }

        if (!parkingPlaces.isEmpty()) {
            FragmentManager fm = getSupportFragmentManager();
            MapFragment fragm = (MapFragment) fm.findFragmentById(R.id.mainContent);
            double latitude = parkingPlaces.get(0).getLocation().getLatitude();
            double longitude = parkingPlaces.get(0).getLocation().getLongitude();
            LatLng lng = new LatLng(latitude, longitude);
            fragm.updateCameraPosition(lng);
        }
    }

    private List<Zone> getZones() {
        InputStream is = getResources().openRawResource(R.raw.zones_with_parking_places);
        List<Zone> zones = JsonLoader.getZones(is);
        return zones;
    }

    float computeDistanceBetweenTwoPoints(double latitudeA, double longitudeA, double latitudeB, double longitudeB) {
        float[] results = new float[1];
        android.location.Location.distanceBetween(latitudeA, longitudeA, latitudeB, longitudeB, results);
        return results[0];
    }

    public void setClickedLocation(LatLng latLng){
        clickedLocation = latLng;
        latitude = latLng.latitude;
        longitude = latLng.longitude;
        EditText editTextLocation = (EditText) findViewById(R.id.location_text_input);
        editTextLocation.setText("selected");
    }
}
