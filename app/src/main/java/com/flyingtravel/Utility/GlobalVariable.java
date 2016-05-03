package com.flyingtravel.Utility;

import android.app.Application;

import com.google.android.gms.maps.model.MarkerOptions;
import com.flyingtravel.Activity.Spot.SpotData;

import java.util.ArrayList;

/**
 * Created by Tinghua on 2/29/2016.
 */
public class GlobalVariable extends Application {
    public ArrayList<SpotData> SpotDataTPE = new ArrayList<SpotData>();
    public ArrayList<SpotData> SpotDataTW = new ArrayList<SpotData>();
    public ArrayList<SpotData> SpotDataRaw = new ArrayList<SpotData>();
    public ArrayList<SpotData> SpotDataSorted = new ArrayList<SpotData>();
    public ArrayList<MarkerOptions> MarkerOptionsArray = new ArrayList<MarkerOptions>();

    //public Double Latitude;
    //public Double Longitude;

    public Boolean isAPILoaded = false;
}