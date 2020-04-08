package com.rmj.parking_place.model;

public class TicketPrice {
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
}
