package com.rmj.parking_place.service;

import com.rmj.parking_place.dto.DTO;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;


public interface ReviewerService {

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type:application/json"
    })
    @POST(ServiceUtils.ADD)
    Call<ResponseBody> add(@Body DTO dto);
}
