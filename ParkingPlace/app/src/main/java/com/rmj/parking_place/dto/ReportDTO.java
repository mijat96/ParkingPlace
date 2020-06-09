package com.rmj.parking_place.dto;

import android.graphics.Bitmap;

import java.util.Date;

public class ReportDTO{

    public String reason;
    public int parkingPlaceId;
    public int zoneId;
    public String dateTime;
    public String address;
    public String status;

    public ReportDTO(String reason, int parkingPlaceId, int zoneId, Date dateTime, String address, String status)
    {
        this.dateTime = dateTime.toString();
        this.address = address;
        this.zoneId = zoneId;
        this.reason = reason;
        this.parkingPlaceId = parkingPlaceId;
        this.status = status;
    }

    public ReportDTO(){}
}
