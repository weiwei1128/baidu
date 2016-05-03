package com.flyingtravel.Utility;

import android.content.Context;
import android.os.AsyncTask;

import com.flyingtravel.Utility.Functions.TaskCallBack;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

/**
 * Created by wei on 2016/3/22.
 */
public class OrderUpdate extends AsyncTask<String, Boolean, Boolean> {
    Boolean updated = false;
    String userId = null;
    int OldCount = 0;
    Context mContext;
    TaskCallBack taskCallBack;


    public OrderUpdate(String UserId, int oldCount, Context context, TaskCallBack taskCallBack) {
        this.userId = UserId;
        this.OldCount = oldCount;
        this.mContext = context;
        this.taskCallBack = taskCallBack;
    }

    @Override
    protected Boolean doInBackground(String... params) {
        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost("http://zhiyou.lin366.com/api/order/index.aspx");
        MultipartEntity multipartEntity = new MultipartEntity();
        Charset charset = Charset.forName("UTF-8");
        try {
            multipartEntity.addPart("json", new StringBody("{" +
                    "    \"act\": \"list\"," +
                    "    \"type\": \"\"," +
                    "    \"page\": \"1\"," +
                    "    \"size\": \"100\"," +
                    "    \"key\": \"\"," +
                    "    \"uid\": \"" + userId + "\"" +
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
            return false;

        try {
            totalcount = new JSONObject(getString.substring(getString.indexOf("{"), getString.lastIndexOf("}") + 1)).getString("totalCount");
        } catch (JSONException | NullPointerException e) {
            e.printStackTrace();
        }
        if (totalcount == null || Integer.valueOf(totalcount) <= OldCount)

            return false;
        else
            return true;

    }

    @Override
    protected void onPostExecute(Boolean s) {
        super.onPostExecute(s);
        taskCallBack.TaskDone(s);
    }
}

