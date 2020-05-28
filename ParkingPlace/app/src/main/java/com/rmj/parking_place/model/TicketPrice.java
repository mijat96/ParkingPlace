package com.rmj.parking_place.model;

import android.os.Parcel;
import android.os.Parcelable;

public class TicketPrice implements Parcelable {
    private int duration; // hours
    private TicketType ticketType;
    private float price;

    public TicketPrice() {

    }

    public TicketPrice(int duration, TicketType ticketType, float price) {
        this.duration = duration;
        this.ticketType = ticketType;
        this.price = price;
    }

    public TicketPrice(TicketPrice ticketPrice) {
        this.duration = ticketPrice.duration;
        this.ticketType = ticketPrice.ticketType;
        this.price = ticketPrice.price;
    }

    protected TicketPrice(Parcel in) {
        duration = in.readInt();
        ticketType = TicketType.valueOf(in.readString());
        price = in.readFloat();
    }

    public static final Creator<TicketPrice> CREATOR = new Creator<TicketPrice>() {
        @Override
        public TicketPrice createFromParcel(Parcel in) {
            return new TicketPrice(in);
        }

        @Override
        public TicketPrice[] newArray(int size) {
            return new TicketPrice[size];
        }
    };

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public TicketType getTicketType() {
        return ticketType;
    }

    public void setTicketType(TicketType ticketType) {
        this.ticketType = ticketType;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(duration);
        dest.writeString(ticketType.name());
        dest.writeFloat(price);
    }
}
