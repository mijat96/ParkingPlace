package com.rmj.parking_place.model;


import android.os.Parcel;
import android.os.Parcelable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PaidParkingPlace implements Parcelable {
    private Long id;
    private ParkingPlace parkingPlace;
    private Date startDateTimeAndroid;
    private Date startDateTimeServer;
    private TicketType ticketType;
    private boolean arrogantUser;

    private static SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    public PaidParkingPlace() {

    }

    public PaidParkingPlace(Long id, ParkingPlace parkingPlace, String startDateTimeAndroid, String startDateTimeServer,
                                TicketType ticketType, boolean arrogantUser) {
        this.id = id;
        this.parkingPlace = parkingPlace;
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
        this.ticketType = ticketType;
        this.arrogantUser = arrogantUser;
    }

    /*public PaidParkingPlace(ParkingPlace parkingPlace, TicketType ticketType) {
        this.parkingPlace = new ParkingPlace(parkingPlace);
        this.startDateTime = new Date();
        this.ticketType = ticketType;
        this.arrogantUser = false;
    }*/

    /*public PaidParkingPlace(ParkingPlace parkingPlace, Date startDateTime,
                            TicketType ticketType, boolean arrogantUser) {
        this.parkingPlace = new ParkingPlace(parkingPlace);
        this.startDateTime = startDateTime;
        this.ticketType = ticketType;
        this.arrogantUser = arrogantUser;
    }*/

    public String getStartAndEndDateTimeAndroid() {
        return sdf.format(startDateTimeAndroid) + " - " + sdf.format(getEndDateTimeAndroid());
    }

    public String getStartAndEndDateTimeServer() {
        return sdf.format(startDateTimeServer) + " - " + sdf.format(getEndDateTimeServer());
    }

    protected PaidParkingPlace(Parcel in) {
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readLong();
        }
        parkingPlace = in.readParcelable(ParkingPlace.class.getClassLoader());
        startDateTimeAndroid = new Date(in.readLong());
        startDateTimeServer = new Date(in.readLong());
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

    public Date getEndDateTimeAndroid() {
        Zone zone = this.parkingPlace.getZone();
        TicketPrice ticketPrice = zone.getTicketPrice(this.ticketType);
        long duration = ticketPrice.getDuration() * 3600000; // form hours to milliseconds
        return new Date(this.startDateTimeAndroid.getTime() + duration);
    }

    public Date getEndDateTimeServer() {
        Zone zone = this.parkingPlace.getZone();
        TicketPrice ticketPrice = zone.getTicketPrice(this.ticketType);
        long duration = ticketPrice.getDuration() * 3600000; // form hours to milliseconds
        return new Date(this.startDateTimeServer.getTime() + duration);
    }

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public ParkingPlace getParkingPlace() {
        return parkingPlace;
    }

    public void setParkingPlace(ParkingPlace parkingPlace) {
        this.parkingPlace = parkingPlace;
    }

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
        if (id == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(id);
        }
        dest.writeParcelable(parkingPlace, flags);
        dest.writeLong(startDateTimeAndroid.getTime());
        dest.writeLong(startDateTimeServer.getTime());
        dest.writeString(ticketType.name());
        dest.writeByte((byte) (arrogantUser ? 1 : 0));
    }
}
