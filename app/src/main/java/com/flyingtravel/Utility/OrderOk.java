package com.flyingtravel.Utility;

import android.content.Context;
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

/**
 * Created by wei on 2016/3/23.
 * 取得status ok的訂單 並且另外呼叫OrderList判斷是否符合行程查詢所需的資料要求
 */
public class OrderOk extends AsyncTask<String, Void, String[]> {
    String UserId;
    Context context;

    public OrderOk(String userId, Context context) {
        this.UserId = userId;
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String[] doInBackground(String... params) {
        if (UserId != null) {
            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost("http://zhiyou.lin366.com/api/order/index.aspx");
            MultipartEntity multipartEntity = new MultipartEntity();
            Charset charset = Charset.forName("UTF-8");
            try {
                multipartEntity.addPart("json", new StringBody("{" +
                        "    \"act\": \"list\"," +
                        "    \"type\": \"ok\"," +
                        "    \"page\": \"1\"," +
                        "    \"size\": \"100\"," +
                        "    \"key\": \"\"," +
                        "    \"uid\": \"" + UserId + "\"" +
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
            String totalcount = null;
            try {
                state = new JSONObject(getString.substring(getString.indexOf("{"), getString.lastIndexOf("}") + 1)).getString("states");
            } catch (JSONException | NullPointerException e) {
                e.printStackTrace();
            }
            //如果讀取資料錯誤 不進行之後的動作
            if (state == null || state.equals("0"))
                return null;

            try {
                totalcount = new JSONObject(getString.substring(getString.indexOf("{"), getString.lastIndexOf("}") + 1)).getString("totalCount");
            } catch (JSONException | NullPointerException e) {
                e.printStackTrace();
            }

            if (totalcount != null && Integer.valueOf(totalcount) > 100) {
                HttpClient client2 = new DefaultHttpClient();
                HttpPost post2 = new HttpPost("http://zhiyou.lin366.com/api/order/index.aspx");
                MultipartEntity multipartEntity2 = new MultipartEntity();
                Charset charset2 = Charset.forName("UTF-8");
                try {
                    multipartEntity2.addPart("json", new StringBody("{" +
                            "    \"act\": \"list\"," +
                            "    \"type\": \"ok\"," +
                            "    \"page\": \"1\"," +
                            "    \"size\": \"" + totalcount + "\"," +
                            "    \"key\": \"\"," +
                            "    \"uid\": \"" + UserId + "\"" +
                            "}", charset2));
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
                } catch (JSONException | NullPointerException e) {
                    e.printStackTrace();
                }
                if (state == null || state.equals("0"))
                    return null;
                try {
                    totalcount = new JSONObject(getString).getString("totalCount");
                } catch (JSONException | NullPointerException e) {
                    e.printStackTrace();
                }
            }
            //如果總數錯誤就不繼續進行了!!
            if (totalcount == null || Integer.valueOf(totalcount) <= 0)
                return null;
            //正式處理資料
            String getId[] = null;
            JSONArray jsonArray = null;
            getId = new String[Integer.valueOf(totalcount)];
            try {
                jsonArray = new JSONObject(getString).getJSONArray("list");
            } catch (JSONException | NullPointerException e) {
                e.printStackTrace();
            }
            //如果資料長度錯誤就不繼續進行了!!
            if (jsonArray == null || jsonArray.length() <= 0)
                return null;
            for (int i = 0; i < Integer.valueOf(totalcount); i++) {
                try {
                    getId[i] = jsonArray.getJSONObject(i).getString("id");
                } catch (JSONException | NullPointerException e) {
                    e.printStackTrace();
                }
            }
            return getId;
        } else
            return null;
    }

    @Override
    protected void onPostExecute(String[] s) {
        if (s != null)
            for (int i = 0; i < s.length; i++) {
                new OrderList(s[i], context).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
//        else Log.e("3.23", "OrderOK NULL");
        super.onPostExecute(s);
    }
}
