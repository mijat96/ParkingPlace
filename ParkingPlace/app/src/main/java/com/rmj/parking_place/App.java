package com.rmj.parking_place;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

public class App extends Application {

    private static Context context;
    private static SharedPreferences sharedPreferences;

    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        sharedPreferences = context.getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
    }

    public static Context getAppContext() {
        return context;
    }

    public static SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }

    public static String getParkingPlaceServerUrl() {
        String parkingPlaceServerUrl = "";

        if (sharedPreferences != null) {
            parkingPlaceServerUrl = sharedPreferences.getString("parkingPlaceServerUrl","");
        }
        if (parkingPlaceServerUrl.equals("")) {
            parkingPlaceServerUrl = context.getString(R.string.PARKING_PLACE_SERVER_BASE_URL);
        }

        return  parkingPlaceServerUrl;
    }

    public static void saveParkingPlaceServerUrl(String parkingPlaceServerUrl) {
        SharedPreferences.Editor edit= sharedPreferences.edit();
        edit.putString("parkingPlaceServerUrl", parkingPlaceServerUrl);
        edit.commit();
    }
}
