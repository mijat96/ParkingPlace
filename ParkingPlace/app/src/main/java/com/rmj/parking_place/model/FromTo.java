package com.rmj.parking_place.model;

import java.util.Objects;

public class FromTo {
    private Location from;
    private Location to;

    public FromTo() {

    }

    public FromTo(Location from, Location to) {
        this.from = from;
        this.to = to;
    }

    public Location getFrom() {
        return from;
    }

    public void setFrom(Location from) {
        this.from = from;
    }

    public Location getTo() {
        return to;
    }

    public void setTo(Location to) {
        this.to = to;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FromTo fromTo = (FromTo) o;
        return from.equals(fromTo.from) &&
                to.equals(fromTo.to);
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to);
    }
}
