package com.flyingtravel.Utility;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

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

/**
 * Created by wei on 2016/3/23.
 * 確認status ok的訂單是否包含行程查詢所需要的兩個list
 * jindianlist
 * jindianlist
 */
public class OrderList extends AsyncTask<String,Void,Boolean> {
    String OrderId;
    Context context;
    public OrderList(String id,Context context){
        this.OrderId = id;
        this.context = context;
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Boolean doInBackground(String... params) {
        if(OrderId!=null){
            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost("http://zhiyou.lin366.com/api/order/index.aspx");
            MultipartEntity multipartEntity = new MultipartEntity();
            Charset charset = Charset.forName("UTF-8");
            try {
                multipartEntity.addPart("json", new StringBody("{" +
                        "    \"act\": \"show\"," +
                        "    \"id\": \"" + OrderId + "\"" +
                        "}", charset));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            post.setEntity(multipartEntity);
            HttpResponse response = null;
            String getString = null;
            try {
                response = client.execute(post);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                getString = EntityUtils.toString(response.getEntity());
            } catch (IOException | NullPointerException e) {
                e.printStackTrace();
            }
            String state = null;
            try {
                state = new JSONObject(getString.substring(getString.indexOf("{"), getString.lastIndexOf("}") + 1)).getString("states");
            } catch (JSONException | NullPointerException e) {
                e.printStackTrace();
            }
            //如果讀取資料錯誤 不進行之後的動作
            if (state == null || state.equals("0"))
                return null;
            //正式處理資料
            JSONArray foodlist = null;
            try {
                foodlist = new JSONObject(getString).getJSONArray("foodlist");
            } catch (JSONException | NullPointerException e) {
                e.printStackTrace();
            }
            JSONArray jindianlist = null;
            try {
                jindianlist = new JSONObject(getString).getJSONArray("jindianlist");
            } catch (JSONException | NullPointerException e) {
                e.printStackTrace();
            }
            if((foodlist==null||foodlist.length()==0)&&(jindianlist==null||jindianlist.length()==0)){
                DataBaseHelper helper = DataBaseHelper.getmInstance(context);
                SQLiteDatabase database = helper.getWritableDatabase();
                Cursor order_cursor = database.query("shoporder", new String[]{"order_id","order_userid ", "order_no",
                        "order_time", "order_name", "order_phone", "order_email",
                        "order_money", "order_state","order_schedule"},
                        "order_id=" + OrderId, null, null, null, null);
                if(order_cursor!=null&&order_cursor.getCount()>0){
                    ContentValues cv = new ContentValues();
                    cv.put("order_schedule", 1);
                    long result = database.update("shoporder", cv, "order_id=?", new String[]{OrderId});
//                    Log.i("3.23","><><><><><> OrderList count should be 1"+order_cursor.getCount()+"||||result:"+result);
                }
//                else Log.e("3.23","!!!!OrderList should not happened!!!!");

                if(order_cursor!=null)
                    order_cursor.close();
//                if(database.isOpen())
//                    database.close();
            }

        }
        return null;
    }

    @Override
    protected void onPostExecute(Boolean s) {
        super.onPostExecute(s);
    }
}
