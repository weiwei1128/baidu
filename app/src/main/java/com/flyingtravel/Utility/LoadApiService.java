package com.flyingtravel.Utility;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.flyingtravel.Activity.Spot.SpotData;

/**
 * Created by Hua on 2016/3/11.
 * //要記得註冊service
 * //
 */
public class LoadApiService extends Service {
    Context context;
    GlobalVariable globalVariable;

    public Boolean isTpeApiLoaded = false;
    public Boolean isTwApiLoaded = false;

    public static final String BROADCAST_ACTION = "com.example.spotapi.status";

    public LoadApiService() {
    }

    @Override
    public void onCreate() {
//        Log.d("3/23_", "LoadApiService onCreate");
        context = getApplicationContext();
        globalVariable = (GlobalVariable) context.getApplicationContext();
        registerReceiver(broadcastReceiver_TPE, new IntentFilter(TpeApi.BROADCAST_ACTION));
        registerReceiver(broadcastReceiver_TW, new IntentFilter(TwApi.BROADCAST_ACTION));
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        if (broadcastReceiver_TPE != null)
            unregisterReceiver(broadcastReceiver_TPE);
        if (broadcastReceiver_TW != null)
            unregisterReceiver(broadcastReceiver_TW);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        Boolean DownloadTpeApi = sharedPreferences.getBoolean("DownloadTpeApi", false);
        Boolean DownloadTwApi = sharedPreferences.getBoolean("DownloadTwApi", false);
//        Log.e("3/23_", "DownloadTpeApi: "+DownloadTpeApi);
//        Log.e("3/23_", "DownloadTwApi: "+DownloadTwApi);
        TpeApi tpeApi = new TpeApi(context);
        TwApi twApi = new TwApi(context);

        if (!DownloadTpeApi) {
            if(!(tpeApi.getStatus() == AsyncTask.Status.RUNNING)) {
                tpeApi.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }

        if (!DownloadTwApi) {
            if(!(twApi.getStatus() == AsyncTask.Status.RUNNING)) {
                twApi.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }

        if (DownloadTpeApi && DownloadTwApi) {
            DataBaseHelper helper = DataBaseHelper.getmInstance(context);
            SQLiteDatabase database = helper.getWritableDatabase();
            Cursor spotDataRaw_cursor = database.query("spotDataRaw", new String[]{"spotName", "spotAdd",
                            "spotLat", "spotLng", "picture1", "picture2", "picture3",
                            "openTime", "ticketInfo", "infoDetail"}, null, null, null, null, null);
            if (spotDataRaw_cursor != null && spotDataRaw_cursor.getCount() > 0) {
                if (globalVariable.SpotDataRaw.size() < spotDataRaw_cursor.getCount()) {
                    globalVariable.SpotDataRaw.clear();
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
                        globalVariable.SpotDataRaw.add(new SpotData(Name, Latitude, Longitude, Add,
                                Picture1, Picture2, Picture3, OpenTime, TicketInfo, InfoDetail));
                    }
                    spotDataRaw_cursor.close();
                }
//                Log.e("3/23_", "API count: " + globalVariable.SpotDataRaw.size());
                globalVariable.isAPILoaded = true;
                if (globalVariable.isAPILoaded) {
//                    Log.e("3/23_", "API is Loaded Broadcast");
                    Intent APILoaded = new Intent(BROADCAST_ACTION);
                    APILoaded.putExtra("isAPILoaded", true);
                    sendBroadcast(APILoaded);
//                    Log.e("3/23_", "***Call StopLoadApiService***");
                    stopSelf();
                }
            }
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private BroadcastReceiver broadcastReceiver_TPE = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Update Your UI here..
            if (intent != null) {
                Boolean isTPELoaded = intent.getBooleanExtra("isTPEAPILoaded", false);
                if (isTPELoaded) {
                    isTpeApiLoaded = true;
                }

                if (isTpeApiLoaded && isTwApiLoaded) {
                    globalVariable.SpotDataRaw.addAll(globalVariable.SpotDataTPE);
                    globalVariable.SpotDataRaw.addAll(globalVariable.SpotDataTW);
//                    Log.e("3/23_", "API count: " + globalVariable.SpotDataRaw.size());
                    globalVariable.isAPILoaded = true;
                    if (globalVariable.isAPILoaded) {
//                        Log.e("3/23_TPE", "API is Loaded Broadcast");
                        Intent APILoaded = new Intent(BROADCAST_ACTION);
                        APILoaded.putExtra("isAPILoaded", true);
                        sendBroadcast(APILoaded);
//                        Log.e("3/23_", "***Call StopLoadApiService***");
                        stopSelf();
                    }
                }
            }
        }
    };

    private BroadcastReceiver broadcastReceiver_TW = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Update Your UI here..
            if (intent != null) {
                Boolean isTWLoaded = intent.getBooleanExtra("isTWAPILoaded", false);
                if (isTWLoaded) {
                    isTwApiLoaded = true;
                }

                if (isTpeApiLoaded && isTwApiLoaded) {
                    globalVariable.SpotDataRaw.addAll(globalVariable.SpotDataTPE);
                    globalVariable.SpotDataRaw.addAll(globalVariable.SpotDataTW);
//                    Log.e("3/23_", "API count: " + globalVariable.SpotDataRaw.size());
                    globalVariable.isAPILoaded = true;
                    if (globalVariable.isAPILoaded) {
//                        Log.e("3/23_TW", "API is Loaded Broadcast");
                        Intent APILoaded = new Intent(BROADCAST_ACTION);
                        APILoaded.putExtra("isAPILoaded", true);
                        sendBroadcast(APILoaded);
//                        Log.e("3/23_", "***Call StopLoadApiService***");
                        stopSelf();
                    }
                }
            }
        }
    };
}
