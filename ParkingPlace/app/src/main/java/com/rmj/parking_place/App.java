package com.rmj.parking_place;

import android.app.ActivityManager;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;

import com.rmj.parking_place.actvities.MainActivity;
import com.rmj.parking_place.actvities.login.ui.LoginActivity;
import com.rmj.parking_place.utils.TokenUtils;

public class App extends Application {

    private static Context context;
    private static SharedPreferences sharedPreferences;

    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        // sharedPreferences = context.getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
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

    public static void loginAgain() {
        /*String currentActivity = getCurrentActivity();
        if (currentActivity.endsWith("LoginActivity")) { // vec se nalazi na login activity-ju
            return;
        }*/

        TokenUtils.removeToken();
        Intent intent = new Intent(context, LoginActivity.class);
        intent.putExtra("toastMessage", "Please login again.");
        // set the new task and clear flags
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
        //finish();
    }

    public static String getCurrentActivityName() {
        ComponentName cn;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            cn = am.getAppTasks().get(0).getTaskInfo().topActivity;
        } else {
            //noinspection deprecation
            cn = am.getRunningTasks(1).get(0).topActivity;
        }

        return cn.getClassName();
    }

    public static boolean mockLocationAllowed() {
        return sharedPreferences.getBoolean("mock_location_allowed",false);
    }

    public static boolean useFusedLocation() {
        String locationType = sharedPreferences.getString("location_types","fused_location");
        return locationType.equals("fused_location");
    }

}
