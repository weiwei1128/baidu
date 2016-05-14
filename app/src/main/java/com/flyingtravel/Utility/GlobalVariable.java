package com.flyingtravel.Utility;

import android.app.Application;

import com.flyingtravel.Activity.Spot.SpotData;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.maps.model.MarkerOptions;

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

    //----GA----//
    private Tracker mTracker;

    /**
     * Gets the default {@link Tracker} for this {@link Application}.
     *
     * @return tracker
     */
    synchronized public Tracker getDefaultTracker() {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
            mTracker = analytics.newTracker("UA-77414748-1");
            mTracker.enableExceptionReporting(true);
            mTracker.enableAdvertisingIdCollection(true);
//            mTracker.enableAutoActivityTracking(true);
        }
        return mTracker;
    }
}