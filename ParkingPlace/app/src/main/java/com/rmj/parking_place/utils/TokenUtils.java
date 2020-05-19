package com.rmj.parking_place.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.rmj.parking_place.App;


public class TokenUtils {

    public static void saveToken(String token) {
        SharedPreferences.Editor edit = App.getSharedPreferences().edit();
        edit.putString("token", token);
        edit.commit();
    }

    public static String getToken() {
        String token = App.getSharedPreferences().getString("token","");
        return token;
    }

    public static boolean isLogged() {
        return getToken() != "";
    }

    public static void removeToken() {
        SharedPreferences.Editor edit= App.getSharedPreferences().edit();
        edit.remove("token");
        edit.commit();
    }
}
