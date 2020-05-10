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

import com.rmj.parking_place.R;
import com.rmj.parking_place.actvities.login.ui.LoginActivity;
import com.rmj.parking_place.utils.TokenUtils;

public class SplashScreenActivity extends CheckWifiActivity /*AppCompatActivity*/ {

    private static int SPLASH_TIME_OUT = 3000; // splash ce biti vidljiv minimum SPLASH_TIME_OUT milisekundi

    private SharedPreferences sharedPreferences;
    private TokenUtils tokenUtils;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        tokenUtils = new TokenUtils(sharedPreferences);

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
            // loginIfNotLoggedOrGoToMapActivity();
        }

        private void showDialogForCurrentPrivateIpAddress() {
            Context context = SplashScreenActivity.this;

            final EditText parkingPlaceServerUrlEditText = new EditText(context);
            String parkingPlaceServerUrl = getParkingPlaceServerUrl();
            parkingPlaceServerUrlEditText.setText(parkingPlaceServerUrl);

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage("Enter web address of  ParkingPlaceServer")
                    .setView(parkingPlaceServerUrlEditText)
                    .setCancelable(false)
                    // .setPositiveButton()
                    .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            saveParkingPlaceServerUrl(parkingPlaceServerUrlEditText.getText().toString());

                            // uloguj se
                            loginIfNotLoggedOrGoToMapActivity();

                            dialog.cancel();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.setCanceledOnTouchOutside(false);
            alert.show();
        }
    }

    private void saveParkingPlaceServerUrl(String parkingPlaceServerUrl) {
        SharedPreferences.Editor edit= sharedPreferences.edit();
        edit.putString("parkingPlaceServerUrl", parkingPlaceServerUrl);
        edit.commit();
    }

    private String getParkingPlaceServerUrl() {
        String parkingPlaceServerUrl = sharedPreferences.getString("parkingPlaceServerUrl","");
        if (parkingPlaceServerUrl.equals("")) {
            parkingPlaceServerUrl = getString(R.string.PARKING_PLACE_SERVER_BASE_URL);
        }

        return  parkingPlaceServerUrl;
    }

    /**
     * Proveri da li je logovan user, ako nije registruj ga.
     */
    private void loginIfNotLoggedOrGoToMapActivity()
    {
        startNextActivity();
        finish(); // da nebi mogao da ode back na splash
    }


    private void startNextActivity()
    {
        Class classVar;
        if (tokenUtils.isLogged()) {
            classVar = MapActivity.class;
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
