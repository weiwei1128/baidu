package com.flyingtravel.Utility;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.JsonToken;
import android.util.Log;

import com.flyingtravel.Activity.Spot.SpotData;
import com.google.gson.stream.JsonReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by Tinghua on 2016/3/23.
 */
public class TpeApi extends AsyncTask<String, Void, ArrayList<SpotData>> {
    public static final String TAG = "TpeApi";
    public static final String SERVER_URL = "http://data.taipei/opendata/datalist/apiAccess?scope=resourceAquire&rid=36847f3f-deff-4183-a5bb-800737591de5";

    public static Boolean isTPEAPILoaded = false;

    Context mcontext;
    GlobalVariable globalVariable;

    public static final String BROADCAST_ACTION = "com.example.tpeapi.status";

    public TpeApi(Context context) {
        this.mcontext = context;
        globalVariable = (GlobalVariable) mcontext.getApplicationContext();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected ArrayList<SpotData> doInBackground(String... params) {
//        Log.e("3/23_", "=========TpeApi======doInBackground");
        try {
            //Create an HTTP client
            HttpClient client = new DefaultHttpClient();
            HttpGet get = new HttpGet(SERVER_URL);

            //Perform the request and check the status code
            HttpResponse response = client.execute(get);
            StatusLine statusLine = response.getStatusLine();

            if (statusLine.getStatusCode() == 200) {
                HttpEntity entity = response.getEntity();
                InputStream content = entity.getContent();

//                Log.e("3/23_TpeSpotJson", "start to JsonParse");
                JsonReader reader = new JsonReader(new InputStreamReader(content, "UTF-8"));

                reader.beginObject();
                while (reader.hasNext()) {
                    String Result = reader.nextName();
                    if (Result.equals("result")) {

                        reader.beginObject();
                        while (reader.hasNext()) {
                            String Results = reader.nextName();
                            if (Results.equals("results")) {

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
                                                case "stitle":
                                                    Name = reader.nextString();
                                                    break;
                                                case "latitude":
                                                    Latitude = reader.nextDouble();
                                                    break;
                                                case "longitude":
                                                    Longitude = reader.nextDouble();
                                                    break;
                                                case "address":
                                                    Add = reader.nextString();
                                                    break;
                                                case "file":
                                                    String ImgString = reader.nextString();
                                                    int StringPosition1 = ImgString.indexOf("http", 2);
                                                    int StringPosition2 = ImgString.indexOf("http", StringPosition1+1);
                                                    int StringPosition3 = ImgString.indexOf("http", StringPosition2+1);

                                                    Picture1 = ImgString;
                                                    if (StringPosition1 > 0) {
                                                        Picture1 = ImgString.substring(0, StringPosition1);
                                                        if (StringPosition2 > 0 && StringPosition2 > StringPosition1) {
                                                            Picture2 = ImgString.substring(StringPosition1, StringPosition2);
                                                            if (StringPosition3 > 0 && StringPosition3 > StringPosition2) {
                                                                Picture3 = ImgString.substring(StringPosition2, StringPosition3);
                                                            }
                                                        }

                                                    }
                                                    break;
                                                case "MEMO_TIME":
                                                    OpenTime = reader.nextString();
                                                    break;
                                                case "xbody":
                                                    InfoDetail = reader.nextString();
                                                    break;
                                                default:
                                                    //Log.e("3/23_TpeSpotJson", "in SkipValue:" +key);
                                                    reader.skipValue();
                                                    break;
                                            }
                                        }
                                    }
                                    globalVariable.SpotDataTPE.add(new SpotData(Name, Latitude, Longitude,
                                            Add, Picture1, Picture2, Picture3, OpenTime, TicketInfo, InfoDetail));
                                    reader.endObject();
                                }
                                reader.endArray();
                            } else {
                                //Log.e("3/23_TpeSpotJson", "in Results");
                                reader.skipValue();
                            }
                        }
                        reader.endObject();
                    } else {
                        //Log.e("3/23_TpeSpotJson", "in Result");
                        reader.skipValue();
                    }
                }
                reader.endObject();
                reader.close();
                content.close();
            }/* else if (statusLine.getStatusCode() == 500) {
                Toast.makeText(mcontext, "台北市政府資料開放平台暫時無法提供服務！", Toast.LENGTH_LONG).show();
            }*/ else {
                Log.e(TAG, "Server responded with status code: " + statusLine.getStatusCode());
            }
        } catch (Exception ex) {
            Log.e(TAG, "Failed to send HTTP POST request due to: " + ex);
        }

        isTPEAPILoaded = true;
        if (isTPEAPILoaded) {
            Intent intent = new Intent(BROADCAST_ACTION);
            intent.putExtra("isTPEAPILoaded", true);
            mcontext.sendBroadcast(intent);
        }
//        Log.e("3/23_TPESpotJson", "Loaded to globalVariable");

//        Log.e("3/23_", "=========TPESpotJson======Write to DB");
        DataBaseHelper helper = DataBaseHelper.getmInstance(mcontext);
        SQLiteDatabase database = helper.getWritableDatabase();
        Cursor spotDataRaw_cursor = database.query("spotDataRaw", new String[]{"spotName", "spotAdd",
                        "spotLat", "spotLng", "picture1", "picture2", "picture3",
                        "openTime", "ticketInfo", "infoDetail"},
                null, null, null, null, null);
        Integer ResultsLength = globalVariable.SpotDataTPE.size();
//        Log.e("3/23_TpeApi", ResultsLength.toString());
        if (spotDataRaw_cursor != null && ResultsLength > 0) {
            if (spotDataRaw_cursor.getCount() == 0) {

                String sql = "INSERT INTO spotDataRaw (spotName, spotAdd, spotLat, spotLng, " +
                        "picture1, picture2, picture3, openTime, ticketInfo, infoDetail) VALUES (?,?,?,?,?,?,?,?,?,?)";
                database.beginTransactionNonExclusive();
                SQLiteStatement stmt = database.compileStatement(sql);

                for (Integer i = 0; i < ResultsLength; i++) {
                    stmt.bindString(1, globalVariable.SpotDataTPE.get(i).getName());
                    stmt.bindString(2, globalVariable.SpotDataTPE.get(i).getAdd());
                    stmt.bindDouble(3, globalVariable.SpotDataTPE.get(i).getLatitude());
                    stmt.bindDouble(4, globalVariable.SpotDataTPE.get(i).getLongitude());
                    stmt.bindString(5, globalVariable.SpotDataTPE.get(i).getPicture1());
                    stmt.bindString(6, globalVariable.SpotDataTPE.get(i).getPicture2());
                    stmt.bindString(7, globalVariable.SpotDataTPE.get(i).getPicture3());
                    stmt.bindString(8, globalVariable.SpotDataTPE.get(i).getOpenTime());
                    stmt.bindString(9, globalVariable.SpotDataTPE.get(i).getTicketInfo());
                    stmt.bindString(10, globalVariable.SpotDataTPE.get(i).getInfoDetail());
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

                for (Integer i = 0; i < ResultsLength; i++) {
                    Cursor spotDataRaw_dul = database.query(true, "spotDataRaw", new String[]{"spotName", "spotAdd",
                                    "spotLat", "spotLng", "picture1", "picture2","picture3",
                                    "openTime", "ticketInfo", "infoDetail"},
                            "spotName=\"" + globalVariable.SpotDataTPE.get(i).getName()+ "\"", null, null, null, null, null);
                    if (spotDataRaw_dul != null && spotDataRaw_dul.getCount() != 0) {
                        spotDataRaw_dul.moveToFirst();
                        //Log.e("3/23", "有重複的資料! " + i + " spotName: " + spotDataRaw_dul.getString(0));
                    }else {
                        stmt.bindString(1, globalVariable.SpotDataTPE.get(i).getName());
                        stmt.bindString(2, globalVariable.SpotDataTPE.get(i).getAdd());
                        stmt.bindDouble(3, globalVariable.SpotDataTPE.get(i).getLatitude());
                        stmt.bindDouble(4, globalVariable.SpotDataTPE.get(i).getLongitude());
                        stmt.bindString(5, globalVariable.SpotDataTPE.get(i).getPicture1());
                        stmt.bindString(6, globalVariable.SpotDataTPE.get(i).getPicture2());
                        stmt.bindString(7, globalVariable.SpotDataTPE.get(i).getPicture3());
                        stmt.bindString(8, globalVariable.SpotDataTPE.get(i).getOpenTime());
                        stmt.bindString(9, globalVariable.SpotDataTPE.get(i).getTicketInfo());
                        stmt.bindString(10, globalVariable.SpotDataTPE.get(i).getInfoDetail());
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
        return globalVariable.SpotDataTPE;
    }

    protected void onPostExecute(ArrayList<SpotData> s) {
//        Log.e("3/23_TpeApi", "DONE");
        if (s.size() != 0) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mcontext);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("DownloadTpeApi", true);
            editor.apply();
        }
        super.onPostExecute(s);
    }
}
