package com.example.zlatan.track.Objects;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zlatan on 08/12/15.
 */
public class POI implements java.io.Serializable {

    String name;
    int id;
    String description;
    String dateAdded;
    float latitude;
    float longitude;
    int companyId;



    public POI(int poi_id, String poi_name, String poi_description, String poi_date_added, float poi_lat, float poi_lon, int poi_company_id) {
        id = poi_id;
        name = poi_name;
        description = poi_description;
        dateAdded = poi_date_added;
        latitude = poi_lat;
        longitude = poi_lon;
        companyId = poi_company_id;
    }

    public POI(int poi_id, String poi_name, String poi_description, String poi_date_added) {
        id = poi_id;
        name = poi_name;
        description = poi_description;
        dateAdded = poi_date_added;
    }

    public int getId() {
        return id;
    }

    public String getName () {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getDateAdded() {
        return dateAdded;
    }

    public float getLatitude() {
        return latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public int getCompanyId() {
        return companyId;
    }

    public void setName (String poi_name) {
        name = poi_name;
    }

    public void setDescription(String poi_description) {
        description = poi_description;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
