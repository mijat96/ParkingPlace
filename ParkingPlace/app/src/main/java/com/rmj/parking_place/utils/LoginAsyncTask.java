package com.rmj.parking_place.utils;

import android.os.AsyncTask;

import com.rmj.parking_place.actvities.login.ui.LoginActivity;
import com.rmj.parking_place.actvities.login.ui.LoginViewModel;
import com.rmj.parking_place.exceptions.InvalidNumberOfParamsException;

public class LoginAsyncTask extends AsyncTask<String,Void,String> {
    private LoginActivity loginActivity;
    private LoginViewModel loginViewModel;

    public LoginAsyncTask(LoginViewModel loginViewModel, LoginActivity loginActivity) {
        this.loginViewModel = loginViewModel;
        this.loginActivity = loginActivity;
    }

    @Override
    protected String doInBackground(String... params) {
        if (params.length != 2) {
            throw new InvalidNumberOfParamsException("params.length != 2");
        }

        String username = params[0];
        String password = params[1];

        loginViewModel.login(loginActivity, username, password);
        return "DONE";
    }

    @Override
    protected void onPostExecute(String s) {

        //here s is the response string, do what ever you want
        super.onPostExecute(s);
    }
}
