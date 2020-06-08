package com.rmj.parking_place.service;

import com.rmj.parking_place.dto.DTO;
import com.rmj.parking_place.dto.ReportDTO;
import com.rmj.parking_place.dto.ReservingDTO;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ReportService {
    @Headers({
            "User-Agent: Mobile-Android"
            //ne salje se slika
            /*"Content-Type:application/json"*/
    })

    @Multipart
    @POST("/api/reports/sendReport")
    Call<ResponseBody> reportParkedCar(@Part MultipartBody.Part file,
                                       @Part MultipartBody.Part parkingPlaceId,
                                       @Part MultipartBody.Part zoneId);

}
