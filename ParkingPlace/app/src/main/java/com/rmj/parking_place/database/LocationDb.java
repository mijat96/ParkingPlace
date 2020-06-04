package com.rmj.parking_place.database;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.rmj.parking_place.model.Location;

@Entity(tableName = "location")
public class LocationDb {
    @PrimaryKey(autoGenerate = true)
    @NonNull
    public long locationId;
    public double latitude;
    public double longitude;
    public String address;

    public LocationDb() {

    }

    public LocationDb(Location location) {
        this.latitude = location.getLatitude();
        this.longitude = location.getLongitude();
        this.address = location.getAddress();
    }
}
