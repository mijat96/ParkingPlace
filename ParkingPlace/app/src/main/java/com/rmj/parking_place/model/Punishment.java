package com.rmj.parking_place.model;

import java.util.Calendar;
import java.util.Date;


public class Punishment {
    private PunishmentType type;
    private String description;
    private Date dateTime;
    private int duration; // hours

    public Punishment() {

    }

    public Punishment(PunishmentType type, String description,  int duration) {
        this.type = type;
        this.description = description;
        this.dateTime = new Date();
        this.duration = duration;
    }

    public PunishmentType getType() {
        return type;
    }

    public void setType(PunishmentType type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDateTime() {
        return dateTime;
    }

    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}
