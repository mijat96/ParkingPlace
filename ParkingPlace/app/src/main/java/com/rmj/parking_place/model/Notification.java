package com.rmj.parking_place.model;

import java.util.Calendar;
import java.util.Date;

public class Notification {
    private String title;
    private String text;
    private Date dateTime;

    public Notification() {

    }

    public Notification(String title, String text) {
        this.title = title;
        this.text = text;
        this.dateTime = Calendar.getInstance().getTime();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Date getDateTime() {
        return dateTime;
    }

    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
    }
}
