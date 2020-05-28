package com.rmj.parking_place.model;

import android.os.Parcel;
import android.os.Parcelable;

public class FavoritePlace implements Parcelable {
    private Long id;
    private String name;
    private FavoritePlaceType type;
    private Location location;

    public FavoritePlace() {

    }

    public FavoritePlace(String name, FavoritePlaceType type, Location location) {
        this.id = null;
        this.name = name;
        this.type = type;
        this.location = location;
    }


    protected FavoritePlace(Parcel in) {
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readLong();
        }
        name = in.readString();
        String typeStr = in.readString();
        type = FavoritePlaceType.valueOf(typeStr);
        location = in.readParcelable(Location.class.getClassLoader());
    }

    public static final Creator<FavoritePlace> CREATOR = new Creator<FavoritePlace>() {
        @Override
        public FavoritePlace createFromParcel(Parcel in) {
            return new FavoritePlace(in);
        }

        @Override
        public FavoritePlace[] newArray(int size) {
            return new FavoritePlace[size];
        }
    };

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public FavoritePlaceType getType() { return type; }

    public void setType(FavoritePlaceType type) { this.type = type; }

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
        dest.writeString(type.name());
        dest.writeParcelable(location, flags);
    }
}
