package com.rmj.parking_place.model;


import android.os.Parcel;
import android.os.Parcelable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Reservation implements Parcelable {
    private Long id;
    private Date startDateTimeAndroid;
    private Date startDateTimeServer;
    private ParkingPlace parkingPlace;

    private static final long ONE_MINUTE_IN_MILLISECONDS = 60000;
    private static final long DURATION_OF_RESERVATION = 1; // 10; // min
    private static SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    public Reservation() {

    }

    public Reservation(Long id, String startDateTimeAndroid, String startDateTimeServer, ParkingPlace parkingPlace) {
        this.id = id;
        try {
            this.startDateTimeAndroid = sdf.parse(startDateTimeAndroid);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        try {
            this.startDateTimeServer = sdf.parse(startDateTimeServer);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        this.parkingPlace = parkingPlace;
    }

    /*public Reservation(ParkingPlace parkingPlace) {
        this.startDateTime = new Date();

        long startDateTimeInMillis = this.startDateTime.getTime();
        this.endDateTime = new Date(startDateTimeInMillis + (DURATION_OF_RESERVATION * ONE_MINUTE_IN_MILLISECONDS));
        this.parkingPlace = new ParkingPlace(parkingPlace);
    }*/

    protected Reservation(Parcel in) {
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readLong();
        }
        startDateTimeAndroid = new Date(in.readLong());
        startDateTimeServer = new Date(in.readLong());
        parkingPlace = in.readParcelable(ParkingPlace.class.getClassLoader());
    }

    public String getStartAndEndDateTimeAndroid() {
        return sdf.format(startDateTimeAndroid) + " - " + sdf.format(getEndDateTimeAndroid());
    }

    public String getStartAndEndDateTimeServer() {
        return sdf.format(startDateTimeServer) + " - " + sdf.format(getEndDateTimeServer());
    }

    public static final Creator<Reservation> CREATOR = new Creator<Reservation>() {
        @Override
        public Reservation createFromParcel(Parcel in) {
            return new Reservation(in);
        }

        @Override
        public Reservation[] newArray(int size) {
            return new Reservation[size];
        }
    };

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public Date getStartDateTimeAndroid() {
        return startDateTimeAndroid;
    }

    public void setStartDateTimeAndroid(Date startDateTimeAndroid) {
        this.startDateTimeAndroid = startDateTimeAndroid;
    }

    public Date getStartDateTimeServer() {
        return startDateTimeServer;
    }

    public void setStartDateTimeServer(Date startDateTimeServer) {
        this.startDateTimeServer = startDateTimeServer;
    }

    public ParkingPlace getParkingPlace() {
        return parkingPlace;
    }

    public void setParkingPlace(ParkingPlace parkingPlace) {
        this.parkingPlace = parkingPlace;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (id == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(id);
        }
        dest.writeLong(startDateTimeAndroid.getTime());
        dest.writeLong(startDateTimeServer.getTime());
        dest.writeParcelable(parkingPlace, flags);
    }

    public Date getEndDateTimeAndroid() {
        return new Date(this.startDateTimeAndroid.getTime() + (DURATION_OF_RESERVATION * ONE_MINUTE_IN_MILLISECONDS));
    }

    public Date getEndDateTimeServer() {
        return new Date(this.startDateTimeServer.getTime() + (DURATION_OF_RESERVATION * ONE_MINUTE_IN_MILLISECONDS));
    }
}
