package com.flyingtravel.Utility;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
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
 * Created by Tinghua on 3/12/2016.
 * 旅遊日誌
 */
public class JsonMemoList extends AsyncTask<String, String, Map<String, String[][]>> {
    Context mcontext;
    //0218
    Boolean ifOK = false;
    int Count = 0;


    public JsonMemoList(Context context) {
        this.mcontext = context;
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Map<String, String[][]> doInBackground(String... params) {
        Log.e("3.9", "=========JsonMemoList======doInBackground");

        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost("http://zhiyou.lin366.com/api/diy/index.aspx");
        MultipartEntity multipartEntity = new MultipartEntity();
        Charset charset = Charset.forName("UTF-8");
        try {
            multipartEntity.addPart("json", new StringBody("{\"act\":\"list\",\"type\":\"diy\",\"page\":\"1\",\"size\":\"10\",\"key\":\"\",\"tid\":\"\"}", charset));
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
         //測試!
        if (getString != null) {
            Log.d("3/13_", "getString: " + getString);
            Log.d("3/13_", "getStringLength:" + getString.length());
        } else
            Log.d("3/13_", "getString NULL");

        try {
            state = new JSONObject(getString.substring(
                    getString.indexOf("{"), getString.lastIndexOf("}") + 1)).getString("states");
            totalcount = new JSONObject(getString).getString("totalCount");
        } catch (JSONException | NullPointerException e) {
            e.printStackTrace();
        }

        if (totalcount != null && !totalcount.equals("10")) {
            Log.d("3/13_", "updated" + totalcount);
            HttpClient client2 = new DefaultHttpClient();
            HttpPost post2 = new HttpPost("http://zhiyou.lin366.com/api/diy/index.aspx");
            MultipartEntity multipartEntity2 = new MultipartEntity();
            Charset charset2 = Charset.forName("UTF-8");
            try {
                multipartEntity2.addPart("json",
                        new StringBody("{\"act\":\"list\",\"type\":\"diy\",\"page\":\"1\",\"size\":\""
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
            } catch (JSONException e) {
                e.printStackTrace();
            }
            //BOTH states and totalCount should be upgraded
            Log.e("3/13_", "Updated: " + getString.length());
        }
        String[][] jsonObjects = null;
        if (state != null && state.equals("1") && totalcount != null) {
            JSONArray jsonArray = null;
            jsonObjects = new String[Integer.valueOf(totalcount)][6];
            try {
                jsonArray = new JSONObject(getString).getJSONArray("list");
                Log.e("3/13_", jsonArray.length() + ":jsonArray長度");
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
                    jsonObjects[i][5] = jsonArray.getJSONObject(i).getString("add_time");
                } catch (JSONException | NullPointerException e) {
                    e.printStackTrace();
                }
            }
            ifOK = true;
            Count = Integer.parseInt(totalcount);
        }
        Map<String, String[][]> fromnet = new HashMap<>();
        fromnet.put("item", jsonObjects);
        return fromnet;
    }

    @Override
    protected void onPostExecute(Map<String, String[][]> stringStringMap) {
        String[][] jsonObjects = stringStringMap.get("item");
        if (ifOK && Count != 0) {
            DataBaseHelper helper = DataBaseHelper.getmInstance(mcontext);
            SQLiteDatabase database = helper.getWritableDatabase();
//            database.beginTransaction();
            Cursor travelMemo_cursor = database.query("travelMemo", new String[]{"totalCount", "id",
                            "title", "url","zhaiyao", "click", "addtime"},
                    null, null, null, null, null);
            if (travelMemo_cursor != null && jsonObjects != null) {
                if (travelMemo_cursor.getCount() == 0) //如果還沒新增過資料->直接新增!
                    for (int i = 0; i < Count; i++) {
                        //其中一項資料不得為NULL
                        if (jsonObjects[i][0] != null && jsonObjects[i][1] != null
                                && jsonObjects[i][2] != null && jsonObjects[i][3] != null
                                && jsonObjects[i][4] != null && jsonObjects[i][5] != null) {
                            ContentValues cv = new ContentValues();
                            cv.put("id", jsonObjects[i][0]);
                            cv.put("title", jsonObjects[i][1]);
                            cv.put("url", jsonObjects[i][2]);
                            cv.put("zhaiyao", jsonObjects[i][3]);
                            cv.put("click", jsonObjects[i][4]);
                            cv.put("addtime", jsonObjects[i][5]);
                            long result = database.insert("travelMemo", null, cv);
                            Log.d("3/13_沒有重複資料", result + " = DB INSERT " + i + " title:" + jsonObjects[i][1]);
                        }
                    }
                else { //資料庫已經有資料了!
                    for (int i = 0; i < Count; i++) {
                        if (jsonObjects[i][0] != null && jsonObjects[i][1] != null
                                && jsonObjects[i][2] != null && jsonObjects[i][3] != null
                                && jsonObjects[i][4] != null && jsonObjects[i][5] != null) {
                            Cursor travelMemo_dul = database.query(true, "travelMemo", new String[]{"totalCount", "id",
                                            "title", "url","zhaiyao", "click", "addtime"},
                                    "id=" + jsonObjects[i][0], null, null, null, null, null);
                            if (travelMemo_dul != null && travelMemo_dul.getCount() > 0) {
                                //TODO 要更新click資料
                                travelMemo_dul.moveToFirst();
                                Log.e("3/13_", "有重複的資料!" + travelMemo_dul.getString(1) + "title: " + travelMemo_dul.getString(2));
                            } else {
                                ContentValues cv = new ContentValues();
                                cv.put("id", jsonObjects[i][0]);
                                cv.put("title", jsonObjects[i][1]);
                                cv.put("url", jsonObjects[i][2]);
                                cv.put("zhaiyao", jsonObjects[i][3]);
                                cv.put("click", jsonObjects[i][4]);
                                cv.put("addtime", jsonObjects[i][5]);
                                long result = database.insert("travelMemo", null, cv);
                                Log.d("3/13_新增過資料", result + " = DB INSERT " + i + "title " + jsonObjects[i][1]);
                            }
                            travelMemo_dul.close();
                        }
                    }
                }
                travelMemo_cursor.close();
            } else
                Log.d("3/13_", "something NULL!" + jsonObjects + " :jsonObjects");
//            database.endTransaction();
//            database.close();
        }
        super.onPostExecute(stringStringMap);
    }
}



