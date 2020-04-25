package com.rmj.parking_place;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.rmj.parking_place.exceptions.AlreadyReservedParkingPlaceException;
import com.rmj.parking_place.exceptions.AlreadyTakenParkingPlaceException;
import com.rmj.parking_place.exceptions.CurrentLocationUnknownException;
import com.rmj.parking_place.exceptions.InvalidModeException;
import com.rmj.parking_place.exceptions.MaxAllowedDistanceForReservationException;
import com.rmj.parking_place.exceptions.NotFoundParkingPlaceException;
import com.rmj.parking_place.fragments.MapFragment;
import com.rmj.parking_place.model.Mode;
import com.rmj.parking_place.model.ParkingPlaceStatus;
import com.rmj.parking_place.tools.FragmentTransition;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private Mode currentMode;
    private Timer timer = new Timer();
    private MapFragment mapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //detectAnyException();

        setNoneMode();
        mapFragment = MapFragment.newInstance();
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
        Toast.makeText(this, "onDestroy()",Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Toast.makeText(this, "onRestart()",Toast.LENGTH_SHORT).show();
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
        boolean canReserveMode = isInCanReserveMode();
        boolean canReserveAndCanTakeMode = isInCanReserveAndCanTakeMode();
        if(!canReserveMode && !canReserveAndCanTakeMode) {
            Toast.makeText(this, "currentMode == " + currentMode.name()
                    + " (invalid mode) (clickOnBtnReserve method)",Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            mapFragment.reserveParkingPlace();
        } catch (NotFoundParkingPlaceException | AlreadyTakenParkingPlaceException | AlreadyReservedParkingPlaceException
                | CurrentLocationUnknownException | MaxAllowedDistanceForReservationException e) {
            Toast.makeText(this, e.getMessage(),Toast.LENGTH_SHORT).show();
            return;
        }

        if (canReserveMode) {
            setIsReservingMode();
        }
        else if (canReserveAndCanTakeMode) {
            setIsReservingAndCanTakeMode();
        }
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

        try {
            mapFragment.takeParkingPlace();
        } catch (NotFoundParkingPlaceException | AlreadyReservedParkingPlaceException | AlreadyTakenParkingPlaceException e) {
            Toast.makeText(this, e.getMessage(),Toast.LENGTH_SHORT).show();
            return;
        }

        Button btnLeaveParkingPlace = (Button) findViewById(R.id.btnLeaveParkingPlace);
        btnLeaveParkingPlace.setVisibility(View.VISIBLE);

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

        timer.scheduleAtFixedRate(new TimerTask() {
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
                            stopTimer();

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

    private void stopTimer() {
        timer.cancel();
        timer.purge();
        timer = new Timer();
    }

    private void addViolationToUser(boolean reservation) {
        // TODO
        // reservation or taking VIOLATION
    }


    public void finishReservationOfParkingPlace(boolean timeIsUp) {
        stopTimer();

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
        stopTimer();

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
        final MainActivity that = this;

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
}
