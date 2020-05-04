package com.rmj.parking_place.utils;

import android.content.Context;
import android.content.SharedPreferences;


public class TokenUtils {

    private SharedPreferences sharedPreferences;

    public TokenUtils(Context context) {
        sharedPreferences = context.getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
    }

    public void saveToken(String token) {
        SharedPreferences.Editor edit= sharedPreferences.edit();
        edit.putString("token", token);
        edit.commit();
    }

    public String getToken() {
        String token = sharedPreferences.getString("token","");
        return token;
    }

    public boolean isLogged() {
        return getToken() != "";
    }

    public void removeToken() {
        SharedPreferences.Editor edit= sharedPreferences.edit();
        edit.remove("token");
        edit.commit();
    }
}
