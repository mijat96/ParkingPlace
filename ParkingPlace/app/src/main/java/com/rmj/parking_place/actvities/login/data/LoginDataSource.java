package com.rmj.parking_place.actvities.login.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.rmj.parking_place.R;
import com.rmj.parking_place.actvities.login.data.model.LoggedInUser;
import com.rmj.parking_place.actvities.login.ui.LoginActivity;
import com.rmj.parking_place.dto.LoginDTO;
import com.rmj.parking_place.dto.TokenDTO;
import com.rmj.parking_place.exceptions.InvalidUsernameOrPasswordException;
import com.rmj.parking_place.utils.HttpRequestAndResponseType;
import com.rmj.parking_place.utils.PostRequestTask;
import com.rmj.parking_place.utils.TokenUtils;
import com.squareup.okhttp.MediaType;

import java.io.IOException;

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
public class LoginDataSource {

    private static String LOGIN_URL;

    private SharedPreferences sharedPreferences;
    private TokenUtils tokenUtils;

    public LoginDataSource(Context context) {
        sharedPreferences = context.getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        LOGIN_URL = getParkingPlaceServerUrl(context) + "/api/authentication/login";
        this.tokenUtils = new TokenUtils(sharedPreferences);
    }

    private String getParkingPlaceServerUrl(Context context) {
        String parkingPlaceServerUrl = sharedPreferences.getString("parkingPlaceServerUrl","");
        if (parkingPlaceServerUrl.equals("")) {
            parkingPlaceServerUrl = context.getString(R.string.PARKING_PLACE_SERVER_BASE_URL);
        }

        return  parkingPlaceServerUrl;
    }

    public Result<LoggedInUser> login(String username, String password) {
        try {

            TokenDTO tokenDTO = loginOnServer(username, password);
            this.tokenUtils.saveToken(tokenDTO.getToken());

            LoggedInUser user = new LoggedInUser(java.util.UUID.randomUUID().toString(), username);
            return new Result.Success<>(user);
        } catch (Exception e) {
            return new Result.Error(new IOException("Error logging in", e));
        }
    }

    private TokenDTO loginOnServer(String username, String password) {
        LoginDTO loginDTO = new LoginDTO(username, password);
        PostRequestTask task = new PostRequestTask(loginDTO);
        TokenDTO tokenDTO = (TokenDTO) task.execute(LOGIN_URL, HttpRequestAndResponseType.LOGIN.name());
        if (tokenDTO == null) {
            throw new InvalidUsernameOrPasswordException("Invalid username or password!");
        }
        return tokenDTO;
    }

    public void logout() {
        tokenUtils.removeToken();
    }
}
