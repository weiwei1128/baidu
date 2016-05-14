package com.flyingtravel.Utility;

import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by wei on 2016/2/23.
 * //要記得註冊service
 * //0310 -> NEWS OK
 */
public class HttpService extends Service {
    Context context;

    //GlobalVariable globalVariable;
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        Log.d("3.7", "Service onStartCommand");

        //利用 executeOnExecutor 確切執行非同步作業
        new Banner().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        new Goods(context).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        new News().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        new Special().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        return 1;
    }

    @Override
    public void onCreate() {
        context = this.getBaseContext();
        //globalVariable = (GlobalVariable) context.getApplicationContext();
        super.onCreate();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    /**
     * 各種JSON
     **/
    private class Banner extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }


        @Override
        protected String doInBackground(String... params) {
//            Log.e("3.9", "=========Banner======doInBackground");

            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost("http://zhiyou.lin366.com/api/adv/index.aspx");
            MultipartEntity entity = new MultipartEntity();
            Charset chars = Charset.forName("UTF-8");
            try {
                entity.addPart("json", new StringBody("{\"act\":\"top\",\"type\":\"1\",\"size\":\"20\"}}", chars));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            post.setEntity(entity);
            HttpResponse resp = null;
            String result = null;
            try {
                resp = client.execute(post);
                result = EntityUtils.toString(resp.getEntity());
            } catch (IOException e) {
                e.printStackTrace();
            }
            JSONArray jsonArray = null;
            try {
                jsonArray = new JSONObject(result).getJSONArray("list");
            } catch (JSONException | NullPointerException e2) {
                e2.printStackTrace();
            }

//            Log.d("3.7", "BannerService result:" + result);
            if (jsonArray != null) {
//                Log.d("3.7", "BannerService result:" + jsonArray.length());
                DataBaseHelper helper = DataBaseHelper.getmInstance(context);
                SQLiteDatabase database = helper.getWritableDatabase();
                Cursor cursor = database.query("banner", new String[]{"img_url", "link","bannerid"}, null, null, null, null, null);
                if (cursor != null && cursor.getCount() > 0)
                    database.delete("banner", null, null);
                if (cursor != null)
                    cursor.close();
                ContentValues contentValues = new ContentValues();

                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("count", jsonArray.length());
                final int anInt = jsonArray.length();
                for (int i = 0; i < jsonArray.length(); i++) {
                    try {
                        editor.putString("img" + i,
                                "http://zhiyou.lin366.com" + jsonArray.getJSONObject(i).getString("img_url"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    //link_url"
                    try {
                        contentValues.clear();
                        contentValues.put("img_url", "http://zhiyou.lin366.com" + jsonArray.getJSONObject(i).getString("img_url"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    try {
                        contentValues.put("link", jsonArray.getJSONObject(i).getString("link_url"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    //id
                    try {
                        contentValues.put("bannerid", jsonArray.getJSONObject(i).getString("id"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    database.insert("banner", null, contentValues);
                }
                editor.apply();
            }
            return null;
        }


        @Override
        protected void onPostExecute(String s) {
            Intent intent = new Intent("banner");
            intent.putExtra("banner", true);
            sendBroadcast(intent);
            super.onPostExecute(s);

        }
    }

    private class News extends AsyncTask<String, Void, String[]> {

        //{"act":"top","type":"tophot","size":"10"}
        //http://zhiyou.lin366.com/api/news/index.aspx
        int count = 0;
        String link[];

        @Override
        protected String[] doInBackground(String... params) {
//            Log.e("3.9", "=========News======doInBackground");

            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost("http://zhiyou.lin366.com/api/news/index.aspx");
            MultipartEntity entity = new MultipartEntity();
            Charset chars = Charset.forName("UTF-8");
            try {
                entity.addPart("json", new StringBody("{\"act\":\"top\",\"type\":\"tophot\",\"size\":\"100\"}", chars));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            post.setEntity(entity);
            HttpResponse resp = null;
            String result = null;
            String states = null;
            String message[] = new String[0];

            try {
                resp = client.execute(post);
                result = EntityUtils.toString(resp.getEntity());
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                states = new JSONObject(result.substring(
                        result.indexOf("{"), result.lastIndexOf("}") + 1)).getString("states");
            } catch (JSONException | NullPointerException e2) {
                e2.printStackTrace();
            }
//            Log.e("3.10","result:"+result); //OK
            if (states == null || states.equals("0"))
                return null;
            else {
                JSONArray jsonArray = null;
                try {
                    jsonArray = new JSONObject(result).getJSONArray("list");
//                    Log.e("3.9", jsonArray.length() + ":jsonArray長度"); //3.9 OK
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (jsonArray != null) {
                    count = jsonArray.length();
                    message = new String[count];
                    link = new String[count];
                    for (int i = 0; i < jsonArray.length(); i++) {
                        try {
                            message[i] = jsonArray.getJSONObject(i).getString("title");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        try {
                            link[i] = jsonArray.getJSONObject(i).getString("link_url");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
//                message="";//test
//                Log.e("3.10","news: "+message); //3.10 OK
                DataBaseHelper helper = DataBaseHelper.getmInstance(context);
                SQLiteDatabase database = helper.getWritableDatabase();
                Cursor news_cursor = database.query("news", new String[]{"title", "link"}, null, null, null, null, null);
                if (news_cursor != null && message != null && message.length > 0) {
                    ContentValues cv = new ContentValues();
//                Log.e("3.10","news cursor count: "+news_cursor.getCount());
                    news_cursor.moveToFirst();
                    if (news_cursor.getCount() == 0) {
                        for (int i = 0; i < count; i++) {
                            cv.clear();
                            cv.put("title", message[i]);
                            cv.put("link", link[i]);
                            long result2 = database.insert("news", null, cv);
                        }

//                    Log.e("3.10","news insert DB result: "+result);
                    } else //資料不相同 -> 更新
                        for (int i = 0; i < count; i++) {
                            news_cursor.moveToPosition(i);
                            if (!news_cursor.getString(0).equals(message[i])) {
                                cv.clear();
                                cv.put("title", message[i]);
                                cv.put("link", link[i]);
                                //.update("special_activity", cv, "special_id=?", new String[]{jsonObjects[i][0]});
                                long result2 = database.update("news", cv, "title=?", new String[]{news_cursor.getString(i)});
                            }
                        }
//                    Log.e("3.10","news update DB result: "+result);

                    news_cursor.close();
                }
                return message;
            }

        }

        @Override
        protected void onPostExecute(String[] s) {

//            else Log.e("3.10","news: cursor=NULL? message:"+s);
            super.onPostExecute(s);
        }
    }

    private class Special extends AsyncTask<String, Void, Map<String, String[][]>> {
        /*
        * {"act":"list","type":"jindian","page":"1","size":"10","key":"","tid":""}**/

        @Override
        protected Map<String, String[][]> doInBackground(String... params) {
//            Log.e("3.9", "=========Special======doInBackground");
            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost("http://zhiyou.lin366.com/api/article/index.aspx");
            MultipartEntity multipartEntity = new MultipartEntity();
            Charset charset = Charset.forName("UTF-8");
            try {
                multipartEntity.addPart("json", new StringBody("{\"act\":\"list\",\"type\":\"jindian\",\"page\":\"1\",\"size\":\"1000\",\"key\":\"\",\"tid\":\"\"}", charset));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            post.setEntity(multipartEntity);
            HttpResponse response = null;
            String getString = null;
            try {
                response = client.execute(post);
                getString = EntityUtils.toString(response.getEntity());
            } catch (IOException e) {
                e.printStackTrace();
            }
            String state = null;
            String totalcount = null;
            try {
                state = new JSONObject(getString.substring(
                        getString.indexOf("{"), getString.lastIndexOf("}") + 1)).getString("states");
                totalcount = new JSONObject(getString).getString("totalCount");
            } catch (JSONException | NullPointerException e) {
                e.printStackTrace();
            }
            /*
            *{
        "id": "618",
        "title": "雲林-北港春生活博物館",
        "img_url": "http://www.abic.com.tw/photoDB/post/1429406074.jpg",
        "zhaiyao": "北港春生活博物館，位於雲林，是一個以木工為特色的博物館，是由超過70年的木工傢具店「盛椿木業」所轉型成立的，裡面的木工文化別有一番特色。館區最特別的就是有一個捷克藝術家海大海的進駐，使得館區內中西文化交錯，呈現一個兼容並蓄的藝文空間。裡面除了有木工文物的展示…",
        "click": "0",
        "add_time": "2016/3/9 17:49:57",
        "sell_price": "100.00"
    }
            * */
            String[][] jsonObjects = null;
            if (state != null && state.equals("1") && totalcount != null) {
                JSONArray jsonArray = null;
                jsonObjects = new String[Integer.valueOf(totalcount)][6];
                try {
                    jsonArray = new JSONObject(getString).getJSONArray("list");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                for (int i = 0; i < Integer.valueOf(totalcount); i++) {
                    try {
                        jsonObjects[i][0] = jsonArray.getJSONObject(i).getString("id");
                    } catch (JSONException | NullPointerException e) {
                        e.printStackTrace();
                    }
                    try {
                        jsonObjects[i][1] = jsonArray.getJSONObject(i).getString("title");
                    } catch (JSONException | NullPointerException e) {
                        e.printStackTrace();
                    }
//                    Log.e("3.10","price**title"+jsonObjects[i][1]);
                    try {
                        jsonObjects[i][2] = jsonArray.getJSONObject(i).getString("img_url");
                    } catch (JSONException | NullPointerException e) {
                        e.printStackTrace();
                    }
                    try {
                        jsonObjects[i][3] = jsonArray.getJSONObject(i).getString("zhaiyao");
                    } catch (JSONException | NullPointerException e) {
                        e.printStackTrace();
                    }
                    try {
                        jsonObjects[i][4] = jsonArray.getJSONObject(i).getString("click");
                    } catch (JSONException | NullPointerException e) {
                        e.printStackTrace();
                    }
                    try {
                        String sellprice = jsonArray.getJSONObject(i).getString("sell_price");
                        if (sellprice.contains(".")) {
                            //有小數點!!
                            sellprice = sellprice.substring(0, sellprice.indexOf("."));
                        }
                        jsonObjects[i][5] = sellprice;
                    } catch (JSONException | NullPointerException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (jsonObjects != null) {
                Intent intent = new Intent("news");
                intent.putExtra("news", true);
                sendBroadcast(intent);

                Log.e("3.10", "special_activity item size:" + jsonObjects.length);
                DataBaseHelper helper = DataBaseHelper.getmInstance(context);
                SQLiteDatabase database = helper.getWritableDatabase();
//                database.beginTransaction();
                Cursor special = database.query("special_activity", new String[]{"special_id", "title", "img", "content", "price", "click"},
                        null, null, null, null, null);
                if (special != null) {
//                    Log.e("4.19","special != null"+jsonObjects.length);

                    if (special.getCount() == 0) //如果還沒新增過資料->直接新增!
                        for (int i = 0; i < jsonObjects.length; i++) {
                            ContentValues cv = new ContentValues();
                            cv.put("special_id", jsonObjects[i][0]);
                            cv.put("title", jsonObjects[i][1]);
                            cv.put("img", jsonObjects[i][2]);
                            cv.put("content", jsonObjects[i][3]);
                            cv.put("price", jsonObjects[i][5]);
                            cv.put("click", jsonObjects[i][4]);
                            long result = database.insert("special_activity", null, cv);
                            Log.e("4.19", "3 price:" + jsonObjects[i][5] + " title" + jsonObjects[i][1]);
//                            Log.d("3.10", "special_activity: " + result + " = DB INSERT" + i + "title " + jsonObjects[i][1]);
                        }
                    else { //資料庫已經有資料了!
                        for (int i = 0; i < jsonObjects.length; i++) {
                            Cursor special_dul = database.query(true, "special_activity", new String[]{"special_id",
                                            "title", "img", "content", "price", "click"},
                                    "special_id=" + jsonObjects[i][0], null, null, null, null, null);
                            if (special_dul != null && special_dul.getCount() > 0) {
                                //有重複的資料
                                special_dul.moveToFirst();
                                ContentValues cv = new ContentValues();
                                //若資料不一樣 則更新 ! (besides ID)
                                if (!special_dul.getString(1).equals(jsonObjects[i][1]))
                                    cv.put("title", jsonObjects[i][1]);
                                if (!special_dul.getString(2).equals(jsonObjects[i][2]))
                                    cv.put("img", jsonObjects[i][2]);
                                if (!special_dul.getString(3).equals(jsonObjects[i][3]))
                                    cv.put("content", jsonObjects[i][3]);
                                if (!special_dul.getString(4).equals(jsonObjects[i][5]))
                                    cv.put("price", jsonObjects[i][5]);
                                if (!special_dul.getString(5).equals(jsonObjects[i][4]))
                                    cv.put("click", jsonObjects[i][4]);
                                if (!special_dul.getString(1).equals(jsonObjects[i][1]) ||
                                        !special_dul.getString(2).equals(jsonObjects[i][2]) ||
                                        !special_dul.getString(3).equals(jsonObjects[i][3]) ||
                                        !special_dul.getString(4).equals(jsonObjects[i][5]) ||
                                        !special_dul.getString(5).equals(jsonObjects[i][4])) {
                                    long result = database.update("special_activity", cv, "special_id=?", new String[]{jsonObjects[i][0]});
//                                    Log.e("3.10", "special_activity updated: " + result + " title: " + jsonObjects[i][1]+" price "+jsonObjects[i][5]);
//                                    Log.e("4.19", "3 price:" + jsonObjects[i][5]+" title"+jsonObjects[i][1]);
                                }
//                                else Log.e("4.19", "3 price:" + jsonObjects[i][5]+" title"+jsonObjects[i][1]);
                            } else {
                                //資料庫存在 但資料不存在
                                ContentValues cv = new ContentValues();
                                cv.put("special_id", jsonObjects[i][0]);
                                cv.put("title", jsonObjects[i][1]);
                                cv.put("img", jsonObjects[i][2]);
                                cv.put("content", jsonObjects[i][3]);
                                cv.put("price", jsonObjects[i][5]);
                                cv.put("click", jsonObjects[i][4]);
                                long result = database.insert("special_activity", null, cv);
//                                Log.e("4.19", "3 price:" + jsonObjects[i][5]+" title"+jsonObjects[i][1]);
//                                Log.d("3.10", "special_activity insert: " + result + " = DB INSERT" + i + "title " + jsonObjects[i][1]);
                            }
                            if (special_dul != null)
                                special_dul.close();
                        }
                    }
                    special.close();
                }
                return null;
            } else
                return null;
        }

        @Override
        protected void onPostExecute(Map<String, String[][]> s) {

            super.onPostExecute(s);
        }
    }

    private class Goods extends AsyncTask<String, String, Map<String, String[][]>> {

        Context mcontext;
        //0218
        Boolean ifOK = false;
        int Count = 0;


        public Goods(Context context) {
            this.mcontext = context;
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Map<String, String[][]> doInBackground(String... params) {
//        Log.e("3.9", "=========Goods======doInBackground");

            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost("http://zhiyou.lin366.com/api/article/index.aspx");
            MultipartEntity multipartEntity = new MultipartEntity();
            Charset charset = Charset.forName("UTF-8");
            try {
                multipartEntity.addPart("json", new StringBody("{\"act\":\"list\",\"type\":\"goods\",\"page\":\"1\",\"size\":\"10\",\"key\":\"\",\"tid\":\"\"}", charset));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            post.setEntity(multipartEntity);
            HttpResponse response = null;
            String getString = null;
            try {
                response = client.execute(post);
                getString = EntityUtils.toString(response.getEntity());
            } catch (IOException e) {
                e.printStackTrace();
            }
            String state = null;
            String totalcount = null;
        /* //測試!
        if (getString != null) {
            Log.d("2.23", "getString: " + getString);
            Log.d("2.25", "getStringLength:" + getString.length());
        } else
            Log.d("2.23", "getString NULL");
            */
            try {
                state = new JSONObject(getString.substring(
                        getString.indexOf("{"), getString.lastIndexOf("}") + 1)).getString("states");

            } catch (JSONException | NullPointerException e) {
                e.printStackTrace();
            }
            try {

                totalcount = new JSONObject(getString).getString("totalCount");
            } catch (JSONException | NullPointerException e) {
                e.printStackTrace();
            }


            if (totalcount != null && !totalcount.equals("10")) {
//            Log.d("2.25", "updated" + totalcount);
                HttpClient client2 = new DefaultHttpClient();
                HttpPost post2 = new HttpPost("http://zhiyou.lin366.com/api/article/index.aspx");
                MultipartEntity multipartEntity2 = new MultipartEntity();
                Charset charset2 = Charset.forName("UTF-8");
                try {
                    multipartEntity2.addPart("json",
                            new StringBody("{\"act\":\"list\",\"type\":\"goods\",\"page\":\"1\",\"size\":\" "
                                    + totalcount + "\",\"key\":\"\",\"tid\":\"\"}", charset2));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                post2.setEntity(multipartEntity2);
                HttpResponse response2 = null;
                getString = null;
                try {
                    response2 = client2.execute(post2);
                    getString = EntityUtils.toString(response2.getEntity());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    state = new JSONObject(getString.substring(
                            getString.indexOf("{"), getString.lastIndexOf("}") + 1)).getString("states");
                    totalcount = new JSONObject(getString).getString("totalCount");
                } catch (JSONException | NullPointerException e) {
                    e.printStackTrace();
                }
                //BOTH states and totalCount should be upgraded
//            Log.e("2.25", "Updated: " + getString.length());
            }
            String[][] jsonObjects = null;
            if (state != null && state.equals("1") && totalcount != null) {
                JSONArray jsonArray = null;
                jsonObjects = new String[Integer.valueOf(totalcount)][7];
                try {
                    jsonArray = new JSONObject(getString).getJSONArray("list");
//                Log.e("2.25", jsonArray.length() + ":jsonArray長度");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                for (int i = 0; i < Integer.valueOf(totalcount); i++) {
                    try {
                        jsonObjects[i][0] = jsonArray.getJSONObject(i).getString("title");
                    } catch (JSONException | NullPointerException e) {
                        e.printStackTrace();
                    }
                    try {
                        jsonObjects[i][1] = jsonArray.getJSONObject(i).getString("img_url");
                    } catch (JSONException | NullPointerException e) {
                        e.printStackTrace();
                    }
                    try {
                        jsonObjects[i][2] = jsonArray.getJSONObject(i).getString("zhaiyao");
                    } catch (JSONException | NullPointerException e) {
                        e.printStackTrace();
                    }
                    try {
                        jsonObjects[i][3] = jsonArray.getJSONObject(i).getString("add_time");
                    } catch (JSONException | NullPointerException e) {
                        e.printStackTrace();
                    }
                    try {
                        jsonObjects[i][4] = jsonArray.getJSONObject(i).getString("id");
                    } catch (JSONException | NullPointerException e) {
                        e.printStackTrace();
                    }
                    try {
                        jsonObjects[i][5] = jsonArray.getJSONObject(i).getString("click");
                    } catch (JSONException | NullPointerException e) {
                        e.printStackTrace();
                    }
                    try {
                        String sellprice = jsonArray.getJSONObject(i).getString("sell_price");
                        if (sellprice.contains(".")) {
                            //有小數點!!
                            sellprice = sellprice.substring(0, sellprice.indexOf("."));
                        }
//                        Log.e("3.10","special_activity 去除小數點前: "+jsonArray.getJSONObject(i).getString("sell_price")+"後: "+sellprice);
                        jsonObjects[i][6] = sellprice;

//                    jsonObjects[i][6] = jsonArray.getJSONObject(i).getString("sell_price");
                        //sell_price
                    } catch (JSONException | NullPointerException e) {
                        e.printStackTrace();
                    }
                }
                ifOK = true;
                Count = Integer.parseInt(totalcount);
            } else return null;
            //===start writing to DB==//
            if (ifOK && Count != 0) {
                DataBaseHelper helper = DataBaseHelper.getmInstance(context);
                SQLiteDatabase database = helper.getWritableDatabase();
//            database.beginTransaction();
                Cursor goods_cursor = database.query("goods", new String[]{"totalCount", "goods_id",
                                "goods_title", "goods_url", "goods_money", "goods_content", "goods_click", "goods_addtime"},
                        null, null, null, null, null);
                if (goods_cursor != null && jsonObjects != null) {
                    if (goods_cursor.getCount() == 0) //如果還沒新增過資料->直接新增!
                        for (int i = 0; i < Count; i++) {
                            //其中一項資料不得為NULL
                            if (jsonObjects[i][0] != null && jsonObjects[i][1] != null
                                    && jsonObjects[i][2] != null && jsonObjects[i][3] != null
                                    && jsonObjects[i][4] != null && jsonObjects[i][5] != null
                                    && jsonObjects[i][6] != null) {
                                ContentValues cv = new ContentValues();
                                cv.put("goods_title", jsonObjects[i][0]);
                                cv.put("goods_url", jsonObjects[i][1]);
                                cv.put("goods_content", jsonObjects[i][2]);
                                cv.put("goods_addtime", jsonObjects[i][3]);
                                cv.put("goods_id", jsonObjects[i][4]);
                                cv.put("goods_click", jsonObjects[i][5]);
                                cv.put("goods_money", jsonObjects[i][6]);
                                long result = database.insert("goods", null, cv);
//                            Log.d("2.19＿沒有重複資料", result + " = DB INSERT" + i + "title " + jsonObjects[i][0]);
                            }
                        }
                    else { //資料庫已經有資料了!
                        for (int i = 0; i < Count; i++) {
                            if (jsonObjects[i][0] != null && jsonObjects[i][1] != null
                                    && jsonObjects[i][2] != null && jsonObjects[i][3] != null
                                    && jsonObjects[i][4] != null && jsonObjects[i][5] != null
                                    && jsonObjects[i][6] != null) {
                                Cursor goods_dul = database.query(true, "goods", new String[]{"totalCount", "goods_id",
                                                "goods_title", "goods_url", "goods_money", "goods_content", "goods_click", "goods_addtime"},
                                        "goods_id=" + jsonObjects[i][4], null, null, null, null, null);
                                if (goods_dul != null && goods_dul.getCount() > 0) {
                                    //TODO 要更新click資料？
                                    goods_dul.moveToFirst();
                                } else {
                                    ContentValues cv = new ContentValues();
                                    cv.put("goods_title", jsonObjects[i][0]);
                                    cv.put("goods_url", jsonObjects[i][1]);
                                    cv.put("goods_content", jsonObjects[i][2]);
                                    cv.put("goods_addtime", jsonObjects[i][3]);
                                    cv.put("goods_id", jsonObjects[i][4]);
                                    cv.put("goods_click", jsonObjects[i][5]);
                                    cv.put("goods_money", jsonObjects[i][6]);
                                    long result = database.insert("goods", null, cv);
//                                Log.d("2.25_新增過資料", result + " = DB INSERT" + i + "title " + jsonObjects[i][0]);
                                }
                                if (goods_dul != null)
                                    goods_dul.close();
                            }
                        }

                    }
                }
//            else
//                Log.d("2.19", "something NULL!" + jsonObjects + " :jsonObjects");
                if (goods_cursor != null)
                    goods_cursor.close();
            }

            Map<String, String[][]> fromnet = new HashMap<>();
            fromnet.put("item", jsonObjects);
            return fromnet;
        }

        @Override
        protected void onPostExecute(Map<String, String[][]> stringStringMap) {

            super.onPostExecute(stringStringMap);
        }
    }


}
