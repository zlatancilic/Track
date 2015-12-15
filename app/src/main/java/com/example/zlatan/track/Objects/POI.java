package com.example.zlatan.track.Objects;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zlatan on 08/12/15.
 */
public class POI {

    String name;
    String id;
    List<String> activeCommands;

    public POI(String poi_id, String poi_name) {
        activeCommands = new ArrayList<String>();
        id = poi_id;
        name = poi_name;
    }

    public void setCommand(String command) {
        activeCommands.add(command);
    }


}
