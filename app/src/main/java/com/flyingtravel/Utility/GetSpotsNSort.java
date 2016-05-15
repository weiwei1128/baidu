package com.flyingtravel.Utility;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import com.flyingtravel.Activity.Spot.SpotData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by Tinghua on 3/8/2016.
 */
public class GetSpotsNSort extends AsyncTask<Void, Void, ArrayList<SpotData>> {
    private static final String TAG = "GetSpotsNSort";
    private GlobalVariable globalVariable;
    private Double Latitude;
    private Double Longitude;

    Context mcontext;

    public static final String BROADCAST_ACTION = "com.example.spotsort.status";

    public GetSpotsNSort(Context context, Double lat, Double lng) {
        this.mcontext = context;
        this.Latitude = lat;
        this.Longitude = lng;
        globalVariable = (GlobalVariable) mcontext.getApplicationContext();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected ArrayList<SpotData> doInBackground(Void... param) {
//        Log.e("5/15_", "=========GetSpotsNSort======doInBackground");
        ArrayList<SpotData> mSpotData = new ArrayList<SpotData>();
        if (globalVariable.isAPILoaded) {
            Integer SpotCount = globalVariable.SpotDataRaw.size();
            for (int i = 0; i < SpotCount; i++) {
                String Name = globalVariable.SpotDataRaw.get(i).getName();
                String Add = globalVariable.SpotDataRaw.get(i).getAdd();
                Double Latitude = globalVariable.SpotDataRaw.get(i).getLatitude();
                Double Longitude = globalVariable.SpotDataRaw.get(i).getLongitude();
                String Picture1 = globalVariable.SpotDataRaw.get(i).getPicture1();
                String Picture2 = globalVariable.SpotDataRaw.get(i).getPicture2();
                String Picture3 = globalVariable.SpotDataRaw.get(i).getPicture3();
                String OpenTime = globalVariable.SpotDataRaw.get(i).getOpenTime();
                String TicketInfo = globalVariable.SpotDataRaw.get(i).getTicketInfo();
                String InfoDetail = globalVariable.SpotDataRaw.get(i).getInfoDetail();
                mSpotData.add(new SpotData(Name, Latitude, Longitude, Add,
                        Picture1, Picture2, Picture3, OpenTime, TicketInfo, InfoDetail));
            }
        } else {
            // retrieve Spots from DB
            DataBaseHelper helper = DataBaseHelper.getmInstance(mcontext);
            SQLiteDatabase database = helper.getWritableDatabase();
            Cursor spotDataRaw_cursor = database.query("spotDataRaw", new String[]{"spotName", "spotAdd",
                            "spotLat", "spotLng", "picture1", "picture2","picture3",
                            "openTime", "ticketInfo", "infoDetail"},
                    null, null, null, null, null);
            if (spotDataRaw_cursor != null) {
                while (spotDataRaw_cursor.moveToNext()) {
                    String Name = spotDataRaw_cursor.getString(0);
                    String Add = spotDataRaw_cursor.getString(1);
                    Double Latitude = spotDataRaw_cursor.getDouble(2);
                    Double Longitude = spotDataRaw_cursor.getDouble(3);
                    String Picture1 = spotDataRaw_cursor.getString(4);
                    String Picture2 = spotDataRaw_cursor.getString(5);
                    String Picture3 = spotDataRaw_cursor.getString(6);
                    String OpenTime = spotDataRaw_cursor.getString(7);
                    String TicketInfo = spotDataRaw_cursor.getString(8);
                    String InfoDetail = spotDataRaw_cursor.getString(9);
                    mSpotData.add(new SpotData(Name, Latitude, Longitude, Add,
                            Picture1, Picture2, Picture3, OpenTime, TicketInfo, InfoDetail));
                }
                spotDataRaw_cursor.close();
            }
        }

        Log.e("3/23_排序", "景點開始排序");
        for (SpotData mSpot : mSpotData) {
            //for迴圈將距離帶入，判斷距離為Distance function
            //需帶入使用者取得定位後的緯度、經度、景點店家緯度、經度。
            mSpot.setDistance(Distance(Latitude, Longitude,
                    mSpot.getLatitude(), mSpot.getLongitude()));
        }

        //依照距離遠近進行List重新排列
        DistanceSort(mSpotData);

        globalVariable.SpotDataSorted = mSpotData;
        if (!globalVariable.SpotDataSorted.isEmpty()) {
            Intent intent = new Intent(BROADCAST_ACTION);
            intent.putExtra("isSorted", true);
            mcontext.sendBroadcast(intent);
        }

        Log.e("3/23_", "=========GetSpotsNSort======Write to DB");
        DataBaseHelper helper = DataBaseHelper.getmInstance(mcontext);
        SQLiteDatabase database = helper.getWritableDatabase();
        Cursor spotDataSorted_cursor = database.query("spotDataSorted", new String[]{"spotName", "spotAdd",
                        "spotLat", "spotLng", "picture1", "picture2", "picture3",
                        "openTime", "ticketInfo", "infoDetail"},
                null, null, null, null, null);
        Integer SpotDataSize = mSpotData.size();
        if (spotDataSorted_cursor != null && SpotDataSize > 0) {
            if (spotDataSorted_cursor.getCount() == 0) {
/*
                String sql = "INSERT INTO spotDataSorted (spotName, spotAdd, spotLat, spotLng, " +
                        "picture1, picture2, picture3, openTime, ticketInfo, infoDetail) VALUES (?,?,?,?,?,?,?,?,?,?)";
                database.beginTransactionNonExclusive();
                // db.beginTransaction();
                SQLiteStatement stmt = database.compileStatement(sql);
*/
                for (int i = 0; i < SpotDataSize; i++) {
/*                    stmt.bindString(1, mSpotData.get(i).getName());
                    stmt.bindString(2, mSpotData.get(i).getAdd());
                    stmt.bindDouble(3, mSpotData.get(i).getLatitude());
                    stmt.bindDouble(4, mSpotData.get(i).getLongitude());
                    stmt.bindString(5, mSpotData.get(i).getPicture1());
                    stmt.bindString(6, mSpotData.get(i).getPicture2());
                    stmt.bindString(7, mSpotData.get(i).getPicture3());
                    stmt.bindString(8, mSpotData.get(i).getOpenTime());
                    stmt.bindString(9, mSpotData.get(i).getTicketInfo());
                    stmt.bindString(10, mSpotData.get(i).getInfoDetail());
                    stmt.execute();
                    stmt.clearBindings();
*/
                    ContentValues cv = new ContentValues();
                    cv.put("spotName", mSpotData.get(i).getName());
                    cv.put("spotAdd", mSpotData.get(i).getAdd());
                    cv.put("spotLat", mSpotData.get(i).getLatitude());
                    cv.put("spotLng", mSpotData.get(i).getLongitude());
                    cv.put("picture1", mSpotData.get(i).getPicture1());
                    cv.put("picture2", mSpotData.get(i).getPicture2());
                    cv.put("picture3", mSpotData.get(i).getPicture3());
                    cv.put("openTime", mSpotData.get(i).getOpenTime());
                    cv.put("ticketInfo", mSpotData.get(i).getTicketInfo());
                    cv.put("infoDetail", mSpotData.get(i).getInfoDetail());
                    long result = database.insert("spotDataSorted", null, cv);
                    //Log.d("3/8_新增排序", result + " = DB INSERT " + i + " spotName " + globalVariable.SpotData.get(i).getName());*/
                }
                //database.setTransactionSuccessful();
                //database.endTransaction();
            } else {
                database.delete("spotDataSorted", null, null);
/*
                String sql = "INSERT INTO spotDataSorted (spotName, spotAdd, spotLat, spotLng, " +
                        "picture1, picture2, picture3, openTime, ticketInfo, infoDetail) VALUES (?,?,?,?,?,?,?,?,?,?)";
                database.beginTransactionNonExclusive();
                // db.beginTransaction();
                SQLiteStatement stmt = database.compileStatement(sql);
*/
                for (int i = 0; i < SpotDataSize; i++) {
/*                    stmt.bindString(1, mSpotData.get(i).getName());
                    stmt.bindString(2, mSpotData.get(i).getAdd());
                    stmt.bindDouble(3, mSpotData.get(i).getLatitude());
                    stmt.bindDouble(4, mSpotData.get(i).getLongitude());
                    stmt.bindString(5, mSpotData.get(i).getPicture1());
                    stmt.bindString(6, mSpotData.get(i).getPicture2());
                    stmt.bindString(7, mSpotData.get(i).getPicture3());
                    stmt.bindString(8, mSpotData.get(i).getOpenTime());
                    stmt.bindString(9, mSpotData.get(i).getTicketInfo());
                    stmt.bindString(10, mSpotData.get(i).getInfoDetail());
                    stmt.execute();
                    stmt.clearBindings();
*/
                    ContentValues cv = new ContentValues();
                    cv.put("spotName", mSpotData.get(i).getName());
                    cv.put("spotAdd", mSpotData.get(i).getAdd());
                    cv.put("spotLat", mSpotData.get(i).getLatitude());
                    cv.put("spotLng", mSpotData.get(i).getLongitude());
                    cv.put("picture1", mSpotData.get(i).getPicture1());
                    cv.put("picture2", mSpotData.get(i).getPicture2());
                    cv.put("picture3", mSpotData.get(i).getPicture3());
                    cv.put("openTime", mSpotData.get(i).getOpenTime());
                    cv.put("ticketInfo", mSpotData.get(i).getTicketInfo());
                    cv.put("infoDetail", mSpotData.get(i).getInfoDetail());
                    long result = database.insert("spotDataSorted", null, cv);
                    //Log.d("3/8_更新排序", result + " = DB INSERT " + i + " spotName " + globalVariable.SpotData.get(i).getName());*/
                }
                //database.setTransactionSuccessful();
                //database.endTransaction();
            }
            spotDataSorted_cursor.close();
        }
        return globalVariable.SpotDataSorted;
    }

    protected void onPostExecute(ArrayList<SpotData> SpotData) {
        if (!SpotData.isEmpty()) {
            Log.e("3/23_GetSpotsNSort", "DONE");
        }
        super.onPostExecute(SpotData);
    }

    //List排序，依照距離由近開始排列，第一筆為最近，最後一筆為最遠
    private void DistanceSort(ArrayList<SpotData> spot) {
        Collections.sort(spot, new Comparator<SpotData>() {
            @Override
            public int compare(SpotData spot1, SpotData spot2) {
                //return spot1.getDistance() < spot2.getDistance() ? -1 : 1;
                return spot1.getDistance() < spot2.getDistance() ? -1 : spot1.getDistance() == spot2.getDistance() ? 0 : 1;
            }
        });
    }

    //帶入使用者及景點店家經緯度可計算出距離
    public double Distance(double longitude1, double latitude1, double longitude2, double latitude2) {
        double radLatitude1 = latitude1 * Math.PI / 180;
        double radLatitude2 = latitude2 * Math.PI / 180;
        double l = radLatitude1 - radLatitude2;
        double p = longitude1 * Math.PI / 180 - longitude2 * Math.PI / 180;
        double distance = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(l / 2), 2)
                + Math.cos(radLatitude1) * Math.cos(radLatitude2)
                * Math.pow(Math.sin(p / 2), 2)));
        distance = distance * 6378137.0;
        distance = Math.round(distance * 10000) / 10000;

        return distance;
    }
}
