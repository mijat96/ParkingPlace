package com.rmj.parking_place.utils;

public class ParametersForUpdatingZones {
    private String zoneids;
    private String versions;


    public ParametersForUpdatingZones() {

    }

    public ParametersForUpdatingZones(String zoneids, String versions) {
        this.zoneids = zoneids;
        this.versions = versions;
    }

    public String getZoneids() {
        return zoneids;
    }

    public void setZoneids(String zoneids) {
        this.zoneids = zoneids;
    }

    public String getVersions() {
        return versions;
    }

    public void setVersions(String versions) {
        this.versions = versions;
    }
}
