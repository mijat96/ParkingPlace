package com.rmj.parking_place.actvities.login.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.rmj.parking_place.App;
import com.rmj.parking_place.R;
import com.rmj.parking_place.actvities.login.data.model.LoggedInUser;
import com.rmj.parking_place.dto.LoginDTO;
import com.rmj.parking_place.dto.TokenDTO;
import com.rmj.parking_place.exceptions.InvalidUsernameOrPasswordException;
import com.rmj.parking_place.service.ParkingPlaceServiceUtils;
import com.rmj.parking_place.utils.HttpRequestAndResponseType;
import com.rmj.parking_place.utils.PostRequestTask;
import com.rmj.parking_place.utils.TokenUtils;
import com.squareup.okhttp.MediaType;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
public class LoginDataSource {

    private static String LOGIN_URL;

    private SharedPreferences sharedPreferences;
    private TokenUtils tokenUtils;

    public LoginDataSource() {
        LOGIN_URL = App.getParkingPlaceServerUrl() + "/api/authentication/login";
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
        Call<TokenDTO> call = ParkingPlaceServiceUtils.authenticationService.login(loginDTO);
        Response<TokenDTO> response = null;
        try {
            response = call.execute();
        } catch (IOException e) {
            throw new InvalidUsernameOrPasswordException("Invalid username or password!");
        }
        
        TokenDTO tokenDTO = response.body();
        if (tokenDTO == null) {
            throw new InvalidUsernameOrPasswordException("Invalid username or password!");
        }
        return tokenDTO;
    }

    public void logout() {
        tokenUtils.removeToken();
    }
}
