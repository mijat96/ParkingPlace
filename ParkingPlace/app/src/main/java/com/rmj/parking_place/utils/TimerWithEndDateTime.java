package com.rmj.parking_place.utils;

import java.util.Timer;

public class TimerWithEndDateTime extends Timer {
    private long endDateTimeInMillis;
    private boolean reservation;
    private boolean started;

    public TimerWithEndDateTime(long endDateTimeInMillis, boolean reservation) {
        super();
        this.endDateTimeInMillis = endDateTimeInMillis;
        this.reservation = reservation;
    }

    public long getEndDateTimeInMillis() {
        return endDateTimeInMillis;
    }

    public boolean isReservation() {
        return reservation;
    }

    public boolean isStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }
}
