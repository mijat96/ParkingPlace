package com.rmj.parking_place.actvities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.rmj.parking_place.App;
import com.rmj.parking_place.R;
import com.rmj.parking_place.actvities.login.ui.LoginActivity;
import com.rmj.parking_place.utils.TokenUtils;

public class SplashScreenActivity extends CheckWifiActivity /*AppCompatActivity*/ {

    private static int MAX_SPLASH_TIME_OUT = 3000; // splash ce biti vidljiv SPLASH_TIME_OUT milisekundi

    private AlertDialog dialogForCurrentPrivateIpAddress;
    private long splashTimeOut;
    private InitTask initTask;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        
        if (savedInstanceState == null) {
            splashTimeOut = MAX_SPLASH_TIME_OUT;
        }
        else {
            boolean dialogIsShowing = savedInstanceState.getBoolean("dialogIsShowing");
            if (dialogIsShowing) {
                splashTimeOut = 0;
            }
            else {
                splashTimeOut = savedInstanceState.getInt("splashTimeOut");
            }
        }

        // detectAnyException();

        // uradi inicijalizaciju u pozadinksom threadu
        initTask = new InitTask();
        initTask.execute();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        if (dialogForCurrentPrivateIpAddress != null) {
            outState.putBoolean("dialogIsShowing", dialogForCurrentPrivateIpAddress.isShowing());
        }

        int timeLeft = (int) (splashTimeOut - (System.currentTimeMillis() - initTask.startTime));
        if(timeLeft < 0) {
            timeLeft = 0;
        }
        outState.putInt("splashTimeOut", timeLeft);
        
        super.onSaveInstanceState(outState);
    }

    private AlertDialog getDialogForCurrentPrivateIpAddress() {
        if (dialogForCurrentPrivateIpAddress == null) {

            Context context = SplashScreenActivity.this;

            final EditText parkingPlaceServerUrlEditText = new EditText(context);
            String parkingPlaceServerUrl = App.getParkingPlaceServerUrl();
            parkingPlaceServerUrlEditText.setText(parkingPlaceServerUrl);

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage("Enter web address of  ParkingPlaceServer")
                    .setView(parkingPlaceServerUrlEditText)
                    .setCancelable(false)
                    // .setPositiveButton()
                    .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            App.saveParkingPlaceServerUrl(parkingPlaceServerUrlEditText.getText().toString());

                            // uloguj se
                            loginIfNotLoggedOrGoToMainActivity();

                            dialog.cancel();
                        }
                    });
            dialogForCurrentPrivateIpAddress = builder.create();
            dialogForCurrentPrivateIpAddress.setCanceledOnTouchOutside(false);
        }

        return dialogForCurrentPrivateIpAddress;
    }

    private void detectAnyException() {
        // final SplashScreenActivity that = this;
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(final Thread paramThread, final Throwable paramThrowable) {
                //Catch your exception
                // Without System.exit() this will not work.
                new Thread() {
                    @Override
                    public void run() {
                        Looper.prepare();
                        Log.e("PARKING_PLACE_ERROR", paramThrowable.getMessage());
                        //Toast.makeText(that,paramThrowable.getMessage(), Toast.LENGTH_LONG).show();
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
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private class InitTask extends AsyncTask<Void, Void, Void>
    {
        private long startTime;

        @Override
        protected void onPreExecute()
        {
            startTime = System.currentTimeMillis();
        }

        @Override
        protected Void doInBackground(Void... arg0)
        {
            continueLogin();
            return null;
        }

        private void continueLogin()
        {
            // sacekaj tako da splash bude vidljiv minimum SPLASH_TIME_OUT milisekundi
            long timeLeft = splashTimeOut - (System.currentTimeMillis() - startTime);
            if(timeLeft < 0) {
                timeLeft = 0;
            }
            SystemClock.sleep(timeLeft);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showDialogForCurrentPrivateIpAddress();
                }
            });

            // uloguj se
            // loginIfNotLoggedOrGoToMainActivity();
        }

        private void showDialogForCurrentPrivateIpAddress() {
            AlertDialog dialog = getDialogForCurrentPrivateIpAddress();
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            dialog.show();
        }
    }

    /**
     * Proveri da li je logovan user, ako nije registruj ga.
     */
    private void loginIfNotLoggedOrGoToMainActivity()
    {
        startNextActivity();
        finish(); // da nebi mogao da ode back na splash
    }


    private void startNextActivity()
    {
        Class classVar;
        if (TokenUtils.isLogged()) {
            classVar = MainActivity.class;
        }
        else {
            classVar = LoginActivity.class;
        }
        startActivity(new Intent(SplashScreenActivity.this, classVar));
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (dialogForCurrentPrivateIpAddress != null) {
            dialogForCurrentPrivateIpAddress.cancel();
        }
    }
}
