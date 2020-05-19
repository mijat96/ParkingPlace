package com.rmj.parking_place.service;

import com.rmj.parking_place.dto.LoginDTO;
import com.rmj.parking_place.dto.RegistrationDTO;
import com.rmj.parking_place.dto.TokenDTO;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;


public interface AuthenticationService {

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type:application/json"
    })
    @POST("/api/authentication/login")
    Call<TokenDTO> login(@Body LoginDTO loginDTO);

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type:application/json"
    })
    @POST("/api/authentication/register")
    Call<ResponseBody> register(@Body RegistrationDTO registrationDTO);
}
