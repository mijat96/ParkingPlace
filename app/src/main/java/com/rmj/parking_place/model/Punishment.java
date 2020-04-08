package com.rmj.parking_place.model;

public class Punishment {
    private PunishmentType type;
    private String description;

    public Punishment() {

    }

    public Punishment(PunishmentType type, String description) {
        this.type = type;
        this.description = description;
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
}
