package com.rmj.parking_place.model;


import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

public class Reservation implements Parcelable {
    private Date startDateTime;
    private Date endDateTime;
    private ParkingPlace parkingPlace;

    private static final long ONE_MINUTE_IN_MILLISECONDS = 60000;
    private static final long DURATION_OF_RESERVATION = 1; // 10; // min

    public Reservation() {

    }

    public Reservation(ParkingPlace parkingPlace) {
        this.startDateTime = new Date();

        long startDateTimeInMillis = this.startDateTime.getTime();
        this.endDateTime = new Date(startDateTimeInMillis + (DURATION_OF_RESERVATION * ONE_MINUTE_IN_MILLISECONDS));
        this.parkingPlace = new ParkingPlace(parkingPlace);
    }

    protected Reservation(Parcel in) {
        startDateTime = new Date(in.readLong());
        endDateTime = new Date(in.readLong());
        parkingPlace = in.readParcelable(ParkingPlace.class.getClassLoader());
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

    public Date getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(Date startDateTime) {
        this.startDateTime = startDateTime;
    }

    public Date getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(Date endDateTime) {
        this.endDateTime = endDateTime;
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
        dest.writeLong(startDateTime.getTime());
        dest.writeLong(endDateTime.getTime());
        dest.writeParcelable(parkingPlace, flags);
    }
}
