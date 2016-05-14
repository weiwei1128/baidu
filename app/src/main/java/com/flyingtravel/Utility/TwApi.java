package com.flyingtravel.Utility;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.stream.JsonReader;
import com.flyingtravel.Activity.Spot.SpotData;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by Tinghua on 2016/3/23.
 */
public class TwApi extends AsyncTask<String, Void, ArrayList<SpotData>> {
    public static final String TAG = "TwApi";
    public static final String SERVER_URL = "http://data.gov.tw/iisi/logaccess/2205?dataUrl=http://gis.taiwan.net.tw/XMLReleaseALL_public/scenic_spot_C_f.json&ndctype=JSON&ndcnid=7777";

    public Boolean isTWAPILoaded = false;

    Context mcontext;
    GlobalVariable globalVariable;

    public static final String BROADCAST_ACTION = "com.example.twapi.status";

    public TwApi(Context context) {
        this.mcontext = context;
        globalVariable = (GlobalVariable) mcontext.getApplicationContext();

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected ArrayList<SpotData> doInBackground(String... params) {
//        Log.e("3/23_", "=========TwApi======doInBackground");
        try {
            //Create an HTTP client
            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost(SERVER_URL);

            //Perform the request and check the status code
            HttpResponse response = client.execute(post);
            StatusLine statusLine = response.getStatusLine();

            if (statusLine.getStatusCode() == 200) {
                HttpEntity entity = response.getEntity();
                InputStream content = entity.getContent();

//                Log.e("3/23_TWSpotJson", "start to JsonParse");
                JsonReader reader = new JsonReader(new InputStreamReader(content, "UTF-8"));

                reader.beginObject();
                while (reader.hasNext()) {
                    String Info = reader.nextName();
                    if (Info.equals("Infos")) {

                        reader.beginObject();
                        while (reader.hasNext()) {
                            String Infos = reader.nextName();
                            if (Infos.equals("Info")) {

                                reader.beginArray();
                                while (reader.hasNext()) {

                                    reader.beginObject();
                                    String Name = "";
                                    double Latitude = 0.0;
                                    double Longitude = 0.0;
                                    String Add = "";
                                    String Picture1 = "";
                                    String Picture2 = "";
                                    String Picture3 = "";
                                    String OpenTime = "";
                                    String TicketInfo = "";
                                    String InfoDetail = "";
                                    while (reader.hasNext()) {
                                        String key = reader.nextName();
                                        boolean isNull = reader.peek() == com.google.gson.stream.JsonToken.NULL;
                                        if (isNull) {
                                            reader.skipValue();
                                        } else {
                                            switch (key) {
                                                case "Name":
                                                    Name = reader.nextString();
                                                    break;
                                                case "Py":
                                                    Latitude = reader.nextDouble();
                                                    break;
                                                case "Px":
                                                    Longitude = reader.nextDouble();
                                                    break;
                                                case "Add":
                                                    Add = reader.nextString();
                                                    break;
                                                case "Picture1":
                                                    Picture1 = reader.nextString();
                                                    break;
                                                case "Picture2":
                                                    Picture2 = reader.nextString();
                                                    break;
                                                case "Picture3":
                                                    Picture3 = reader.nextString();
                                                    break;
                                                case "Opentime":
                                                    OpenTime = reader.nextString();
                                                    break;
                                                case "Ticketinfo":
                                                    TicketInfo = reader.nextString();
                                                    break;
                                                case "Toldescribe":
                                                    InfoDetail = reader.nextString();
                                                    break;
                                                default:
                                                    //Log.e("3/23_TWSpotJson", "in SpotData");
                                                    reader.skipValue();
                                                    break;
                                            }
                                        }
                                    }
                                    globalVariable.SpotDataTW.add(new SpotData(Name, Latitude, Longitude,
                                            Add, Picture1, Picture2, Picture3, OpenTime, TicketInfo, InfoDetail));
                                    reader.endObject();
                                }
                                reader.endArray();
                            } else {
                                //Log.e("3/23_TWSpotJson", "in Info");
                                reader.skipValue();
                            }
                        }
                        reader.endObject();
                    } else {
                        //Log.e("3/23_TWSpotJson", "in Infos");
                        reader.skipValue();
                    }
                }
                reader.endObject();
                reader.close();
                content.close();
            }
//            else {
//                Log.e(TAG, "Server responded with status code: " + statusLine.getStatusCode());
//            }
        } catch (Exception ex) {
            ex.printStackTrace();
//            Log.e(TAG, "Failed to send HTTP POST request due to: " + ex);
        }

        isTWAPILoaded = true;
        if (isTWAPILoaded) {
            Intent intent = new Intent(BROADCAST_ACTION);
            intent.putExtra("isTWAPILoaded", true);
            mcontext.sendBroadcast(intent);
        }
//        Log.e("3/23_TWSpotJson", "Loaded to globalVariable");


//        Log.e("3/23_", "=========TWSpotJson======Write to DB");
        DataBaseHelper helper = DataBaseHelper.getmInstance(mcontext);
        SQLiteDatabase database = helper.getWritableDatabase();
        Cursor spotDataRaw_cursor = database.query("spotDataRaw", new String[]{"spotName", "spotAdd",
                        "spotLat", "spotLng", "picture1", "picture2", "picture3",
                        "openTime", "ticketInfo", "infoDetail"},
                null, null, null, null, null);
        Integer InfoLength = globalVariable.SpotDataTW.size();
//        Log.e("3/23_TwApi", InfoLength.toString());
        if (spotDataRaw_cursor != null && InfoLength > 0) {
            if (spotDataRaw_cursor.getCount() == 0) {

                String sql = "INSERT INTO spotDataRaw (spotName, spotAdd, spotLat, spotLng, " +
                        "picture1, picture2, picture3, openTime, ticketInfo, infoDetail) VALUES (?,?,?,?,?,?,?,?,?,?)";
                database.beginTransactionNonExclusive();
                SQLiteStatement stmt = database.compileStatement(sql);

                for (Integer i = 0; i < InfoLength; i++) {
                    stmt.bindString(1, globalVariable.SpotDataTW.get(i).getName());
                    stmt.bindString(2, globalVariable.SpotDataTW.get(i).getAdd());
                    stmt.bindDouble(3, globalVariable.SpotDataTW.get(i).getLatitude());
                    stmt.bindDouble(4, globalVariable.SpotDataTW.get(i).getLongitude());
                    stmt.bindString(5, globalVariable.SpotDataTW.get(i).getPicture1());
                    stmt.bindString(6, globalVariable.SpotDataTW.get(i).getPicture2());
                    stmt.bindString(7, globalVariable.SpotDataTW.get(i).getPicture3());
                    stmt.bindString(8, globalVariable.SpotDataTW.get(i).getOpenTime());
                    stmt.bindString(9, globalVariable.SpotDataTW.get(i).getTicketInfo());
                    stmt.bindString(10, globalVariable.SpotDataTW.get(i).getInfoDetail());
                    stmt.execute();
                    stmt.clearBindings();
                }
                database.setTransactionSuccessful();
                database.endTransaction();
            } else {
                String sql = "INSERT INTO spotDataRaw (spotName, spotAdd, spotLat, spotLng, " +
                        "picture1, picture2, picture3, openTime, ticketInfo, infoDetail) VALUES (?,?,?,?,?,?,?,?,?,?)";
                database.beginTransactionNonExclusive();
                SQLiteStatement stmt = database.compileStatement(sql);

                for (Integer i = 0; i < InfoLength; i++) {
                    Cursor spotDataRaw_dul = database.query(true, "spotDataRaw", new String[]{"spotName", "spotAdd",
                                    "spotLat", "spotLng", "picture1", "picture2","picture3",
                                    "openTime", "ticketInfo", "infoDetail"},
                            "spotName=\"" + globalVariable.SpotDataTW.get(i).getName()+ "\"", null, null, null, null, null);
                    if (spotDataRaw_dul != null && spotDataRaw_dul.getCount() != 0) {
                        spotDataRaw_dul.moveToFirst();
                        //Log.e("3/23", "有重複的資料! " + i + " spotName: " + spotDataRaw_dul.getString(0));
                    } else {
                        stmt.bindString(1, globalVariable.SpotDataTW.get(i).getName());
                        stmt.bindString(2, globalVariable.SpotDataTW.get(i).getAdd());
                        stmt.bindDouble(3, globalVariable.SpotDataTW.get(i).getLatitude());
                        stmt.bindDouble(4, globalVariable.SpotDataTW.get(i).getLongitude());
                        stmt.bindString(5, globalVariable.SpotDataTW.get(i).getPicture1());
                        stmt.bindString(6, globalVariable.SpotDataTW.get(i).getPicture2());
                        stmt.bindString(7, globalVariable.SpotDataTW.get(i).getPicture3());
                        stmt.bindString(8, globalVariable.SpotDataTW.get(i).getOpenTime());
                        stmt.bindString(9, globalVariable.SpotDataTW.get(i).getTicketInfo());
                        stmt.bindString(10, globalVariable.SpotDataTW.get(i).getInfoDetail());
                        stmt.execute();
                        stmt.clearBindings();
                    }
                    if(spotDataRaw_dul!=null)
                        spotDataRaw_dul.close();
                }
                database.setTransactionSuccessful();
                database.endTransaction();
            }
        }
        if (spotDataRaw_cursor != null) {
            spotDataRaw_cursor.close();
        }
        return globalVariable.SpotDataTW;
    }

    protected void onPostExecute(ArrayList<SpotData> s) {
//        Log.e("3/23_TwApi", "DONE");
        if (s.size() != 0) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mcontext);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("DownloadTwApi", true);
            editor.apply();
        }
        super.onPostExecute(s);
    }


}
