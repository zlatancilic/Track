package com.example.zlatan.track.Objects;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zlatan on 08/12/15.
 */
public class POI {

    String name;
    int id;
    String description;
    String date_added;
    float latitude;
    float longitude;



    public POI(int poi_id, String poi_name, String poi_description, String poi_date_added) {
        id = poi_id;
        name = poi_name;
        description = poi_description;
        date_added = poi_date_added;
    }

    public int getId() {
        return id;
    }

    public String getName () {
        return name;
    }


    @Override
    public String toString() {
        return this.name;
    }
}
