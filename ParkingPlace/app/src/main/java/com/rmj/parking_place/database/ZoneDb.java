package com.rmj.parking_place.database;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.rmj.parking_place.model.Location;
import com.rmj.parking_place.model.Zone;

@Entity(tableName = "zone")
public class ZoneDb {
    @PrimaryKey
    @NonNull
    public long zoneId;
    public String name;
    public long version;

    public ZoneDb() {

    }

    public ZoneDb(Zone zone) {
        this.zoneId = zone.getId();
        this.name = zone.getName();
        this.version = zone.getVersion();
    }
}
