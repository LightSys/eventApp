package org.lightsys.eventApp.data;


import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Nate Gamble
 * created on 7/25/2019.
 *
 * Class represents map info
 */

public class MapInfo {

    private String name;
    private String JSONPoi;
    // <<Name of point of interest, icon used>, <Lat, Long>>
    private HashMap<ArrayList<String>, ArrayList<Double>> POIs;
    private Double topLeftLat;
    private Double topLeftLong;
    private Double botRightLat;
    private Double botRightLong;

    public MapInfo() { Log.d("MapInfo", "creating map"); }

    public String getName()                                         { return name; }
    public String getJSONPoi()                                      { return JSONPoi; }
    public HashMap<ArrayList<String>, ArrayList<Double>> getPOIs()  { return POIs; }
    public Double getTopLeftLat()                                   { return topLeftLat; }
    public Double getTopLeftLong()                                  { return topLeftLong; }
    public Double getBotRightLat()                                  { return botRightLat; }
    public Double getBotRightLong()                                 { return botRightLong; }

    public void setName(String name)                                            { this.name = name; }
    public void setPOIs(HashMap<ArrayList<String>, ArrayList<Double>> POIs)     { this.POIs = POIs; }
    public void setTopLeftLat(Double topLeftLat)                                { this.topLeftLat = topLeftLat; }
    public void setTopLeftLong(Double topLeftLong)                              { this.topLeftLong = topLeftLong; }
    public void setBotRightLat(Double botRightLat)                              { this.botRightLat = botRightLat; }
    public void setBotRightLong(Double botRightLong)                            { this.botRightLong = botRightLong; }

    public void setJSONPoi(String JSONPoi) {
        this.JSONPoi = JSONPoi;
        JSONArray pois;
        HashMap<ArrayList<String>, ArrayList<Double>> newPois = new HashMap<>();
        try {
            pois = new JSONArray(JSONPoi);
            for (int i = 0; i < pois.length(); i++) {
                JSONObject temp = pois.getJSONObject(i);
                ArrayList<String> nameIcon = new ArrayList<>();
                nameIcon.add(temp.getString("name"));
                nameIcon.add(temp.getString("icon"));
                ArrayList<Double> latLong = new ArrayList<>();
                latLong.add(temp.getDouble("lat"));
                latLong.add(temp.getDouble("long"));
                newPois.put(nameIcon, latLong);
            }
        } catch (JSONException e) { e.printStackTrace(); }
        this.POIs = newPois;
    }
}
