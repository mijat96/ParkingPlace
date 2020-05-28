package com.rmj.parking_place.model;


import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

public class PaidParkingPlace implements Parcelable {
    private ParkingPlace parkingPlace;
    private Date startDateTime;
    private TicketType ticketType;
    private boolean arrogantUser;

    public PaidParkingPlace() {

    }

    public PaidParkingPlace(ParkingPlace parkingPlace, TicketType ticketType) {
        this.parkingPlace = new ParkingPlace(parkingPlace);
        this.startDateTime = new Date();
        this.ticketType = ticketType;
        this.arrogantUser = false;
    }

    public PaidParkingPlace(ParkingPlace parkingPlace, Date startDateTime,
                            TicketType ticketType, boolean arrogantUser) {
        this.parkingPlace = new ParkingPlace(parkingPlace);
        this.startDateTime = startDateTime;
        this.ticketType = ticketType;
        this.arrogantUser = arrogantUser;
    }

    protected PaidParkingPlace(Parcel in) {
        parkingPlace = in.readParcelable(ParkingPlace.class.getClassLoader());
        startDateTime = new Date(in.readLong());
        ticketType = TicketType.valueOf(in.readString());
        arrogantUser = in.readByte() != 0;
    }

    public static final Creator<PaidParkingPlace> CREATOR = new Creator<PaidParkingPlace>() {
        @Override
        public PaidParkingPlace createFromParcel(Parcel in) {
            return new PaidParkingPlace(in);
        }

        @Override
        public PaidParkingPlace[] newArray(int size) {
            return new PaidParkingPlace[size];
        }
    };

    public Date getEndDateTime() {
        Zone zone = this.parkingPlace.getZone();
        TicketPrice ticketPrice = zone.getTicketPrice(this.ticketType);
        long duration = ticketPrice.getDuration() * 3600000; // form hours to milliseconds
        return new Date(this.startDateTime.getTime() + duration);
    }

    public ParkingPlace getParkingPlace() {
        return parkingPlace;
    }

    public void setParkingPlace(ParkingPlace parkingPlace) {
        this.parkingPlace = parkingPlace;
    }

    public Date getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(Date startDateTime) {
        this.startDateTime = startDateTime;
    }

    public TicketType getTicketType() {
        return ticketType;
    }

    public void setTicketType(TicketType ticketType) {
        this.ticketType = ticketType;
    }

    public boolean isArrogantUser() {
        return arrogantUser;
    }

    public void setArrogantUser(boolean arrogantUser) {
        this.arrogantUser = arrogantUser;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(parkingPlace, flags);
        dest.writeLong(startDateTime.getTime());
        dest.writeString(ticketType.name());
        dest.writeByte((byte) (arrogantUser ? 1 : 0));
    }
}
