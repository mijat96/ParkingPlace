package com.rmj.parking_place.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Relation;

import com.google.android.gms.maps.model.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.rmj.parking_place.database.TicketPriceDb;
import com.rmj.parking_place.database.ZoneWithLocationsAndTicketPrices;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Zone implements Parcelable {
    private Long id;
    private String name;
    private Long version;
    private Location northEast;
    private Location southWest;
    private com.mapbox.mapboxsdk.geometry.LatLngBounds bounds;
    private List<ParkingPlace> parkingPlaces;
    private List<TicketPrice> ticketPrices;

    public Zone() {

    }

    public Zone(Long id, String name, Long version, Location northEast, Location southWest,
                    com.mapbox.mapboxsdk.geometry.LatLngBounds bounds,
                    List<ParkingPlace> parkingPlaces, List<TicketPrice> ticketPrices) {
        this.id = id;
        this.name = name;
        this.version = version;
        this.northEast = northEast;
        this.southWest = southWest;
        this.bounds = bounds;
        this.parkingPlaces = parkingPlaces;
        this.ticketPrices = ticketPrices;
    }

    public Zone(Zone zone) {
        this.id = zone.id;
        this.name = zone.name;
        this.parkingPlaces = null;
        this.version = null;
        this.northEast = null;
        this.southWest = null;
        this.bounds = null;
        this.ticketPrices = new ArrayList<TicketPrice>();
        for (TicketPrice ticketPrice : zone.ticketPrices) {
            this.ticketPrices.add(ticketPrice);
        }
    }

    protected Zone(Parcel in) {
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readLong();
        }
        name = in.readString();
        if (in.readByte() == 0) {
            version = null;
        } else {
            version = in.readLong();
        }
        northEast = in.readParcelable(Location.class.getClassLoader());
        southWest = in.readParcelable(Location.class.getClassLoader());
        bounds = in.readParcelable(LatLngBounds.class.getClassLoader());
        parkingPlaces = readParkingPlaces(in);
        ticketPrices = in.createTypedArrayList(TicketPrice.CREATOR);
    }

    public Zone(ZoneWithLocationsAndTicketPrices zoneWithLocationsAndTicketPrices) {
        this.id = zoneWithLocationsAndTicketPrices.zone.zoneId;
        this.name = zoneWithLocationsAndTicketPrices.zone.name;
        this.version = zoneWithLocationsAndTicketPrices.zone.version;
        this.northEast = new Location(zoneWithLocationsAndTicketPrices.northEast);
        this.southWest = new Location(zoneWithLocationsAndTicketPrices.southWest);
        this.parkingPlaces = new ArrayList<ParkingPlace>();
        this.ticketPrices = restoreTicketPrices(zoneWithLocationsAndTicketPrices.ticketPrices);
    }

    private List<TicketPrice> restoreTicketPrices(List<TicketPriceDb> ticketPriceDbs) {
        ArrayList<TicketPrice> ticketPrices = new ArrayList<TicketPrice>();
        for (TicketPriceDb ticketPriceDb : ticketPriceDbs) {
            ticketPrices.add(new TicketPrice(ticketPriceDb));
        }
        return ticketPrices;
    }

    public void writeParkingPlaces(Parcel dest, int flags, List<ParkingPlace> parkingPlaces) {
        if (parkingPlaces == null) {
            dest.writeInt(-1);
            return;
        }
        int N = parkingPlaces.size();
        int i=0;
        dest.writeInt(N);
        while (i < N) {
            ParkingPlace.writeToParcelWithoutZone(dest, flags, parkingPlaces.get(i));
            i++;
        }
    }

    public ArrayList<ParkingPlace> readParkingPlaces(Parcel in) {
        int N = in.readInt();
        if (N < 0) {
            return null;
        }
        ArrayList<ParkingPlace> l = new ArrayList<ParkingPlace>(N);
        ParkingPlace parkingPlace;
        while (N > 0) {
            parkingPlace = ParkingPlace.readToParcelWithoutZone(in);
            parkingPlace.setZone(this);
            l.add(parkingPlace);
            N--;
        }
        return l;
    }

    public static final Creator<Zone> CREATOR = new Creator<Zone>() {
        @Override
        public Zone createFromParcel(Parcel in) {
            return new Zone(in);
        }

        @Override
        public Zone[] newArray(int size) {
            return new Zone[size];
        }
    };

    public TicketPrice getTicketPrice(TicketType ticketType) {
        for (TicketPrice ticketPrice : ticketPrices) {
            if (ticketPrice.getTicketType() == ticketType) {
                return ticketPrice;
            }
        }

        return null;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Location getNorthEast() {
        return northEast;
    }

    public void setNorthEast(Location northEast) {
        this.northEast = northEast;
    }

    public Location getSouthWest() {
        return southWest;
    }

    public void setSouthWest(Location southWest) {
        this.southWest = southWest;
    }

    public LatLngBounds getBounds() { return bounds; }

    public void setBounds(LatLngBounds bounds) { this.bounds = bounds; }

    public List<ParkingPlace> getParkingPlaces() {
        return parkingPlaces;
    }

    public void setParkingPlaces(List<ParkingPlace> parkingPlaces) { this.parkingPlaces = parkingPlaces; }

    public List<TicketPrice> getTicketPrices() {
        return ticketPrices;
    }

    public void setTicketPrices(List<TicketPrice> ticketPrices) { this.ticketPrices = ticketPrices; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Zone zone = (Zone) o;
        return name.equals(zone.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
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
        dest.writeString(name);
        if (version == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(version);
        }
        dest.writeParcelable(northEast, flags);
        dest.writeParcelable(southWest, flags);
        dest.writeParcelable(bounds, flags);
        writeParkingPlaces(dest, flags, parkingPlaces);
        dest.writeTypedList(ticketPrices);
    }

    public com.google.android.gms.maps.model.LatLngBounds getLatLngBounds() {
        return com.google.android.gms.maps.model.LatLngBounds.builder()
                .include(new LatLng(northEast.getLatitude(), northEast.getLongitude()))
                .include(new LatLng(southWest.getLatitude(), southWest.getLongitude()))
                .build();
    }
}
