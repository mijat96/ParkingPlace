package com.rmj.parking_place.actvities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;

import com.rmj.parking_place.App;
import com.rmj.parking_place.R;
import com.rmj.parking_place.actvities.login.ui.LoginActivity;
import com.rmj.parking_place.utils.TokenUtils;

public class SplashScreenActivity extends CheckWifiActivity /*AppCompatActivity*/ {

    private static int SPLASH_TIME_OUT = 3000; // splash ce biti vidljiv minimum SPLASH_TIME_OUT milisekundi


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        // uradi inicijalizaciju u pozadinksom threadu
        new InitTask().execute();
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
            long timeLeft = SPLASH_TIME_OUT - (System.currentTimeMillis() - startTime);
            if(timeLeft < 0) timeLeft = 0;
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
            AlertDialog alert = builder.create();
            alert.setCanceledOnTouchOutside(false);
            alert.show();
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
}
