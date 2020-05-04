package com.rmj.parking_place.actvities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;

import com.rmj.parking_place.R;
import com.rmj.parking_place.actvities.login.ui.LoginActivity;
import com.rmj.parking_place.utils.TokenUtils;

public class SplashScreenActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_EMAIL = 1;
    private static int SPLASH_TIME_OUT = 3000; // splash ce biti vidljiv minimum SPLASH_TIME_OUT milisekundi

    private TokenUtils tokenUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        tokenUtils = new TokenUtils(this);

        // uradi inicijalizaciju u pozadinksom threadu
        new InitTask().execute();
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

            // uloguj se
            loginIfNotLoggedOrGoToMapActivity();
        }
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
}
