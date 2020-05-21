package com.rmj.parking_place.model;

public class FavoritePlace {
    private static Long idGenerator = 0L;

    private Long id;
    private String name;
    private Location location;

    public FavoritePlace() {

    }

    public FavoritePlace(String name, Location location) {
        this.id = idGenerator++;
        this.name = name;
        this.location = location;
    }

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
}
