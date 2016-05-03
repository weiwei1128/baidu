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
 * Created by wei on 2016/3/22.
 */
public class OrderGet extends AsyncTask<String, Void, String> {
    String UserId;
    Context context;
    Functions.TaskCallBack taskCallBack;

    public OrderGet(Context context, String UserId, Functions.TaskCallBack callBack) {
        this.context = context;
        this.UserId = UserId;
        this.taskCallBack = callBack;
    }

    @Override
    protected String doInBackground(String... params) {
        String returnMessage = null;
        if (UserId != null) {
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
                            "    \"type\": \"\"," +
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
                } catch (JSONException| NullPointerException e) {
                    e.printStackTrace();
                }
                if (state == null || state.equals("0"))
                    return null;
                try {
                    totalcount = new JSONObject(getString).getString("totalCount");
                } catch (JSONException| NullPointerException e) {
                    e.printStackTrace();
                }
            }
            //如果總數錯誤就不繼續進行了!!
            if (totalcount == null || Integer.valueOf(totalcount) <= 0)
                return null;

            //正式處理資料
            String[][] jsonObjects = null;
            JSONArray jsonArray = null;
            jsonObjects = new String[Integer.valueOf(totalcount)][8];
            try {
                jsonArray = new JSONObject(getString).getJSONArray("list");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            //如果資料長度錯誤就不繼續進行了!!
            if (jsonArray == null || jsonArray.length() <= 0)
                return null;
            for (int i = 0; i < Integer.valueOf(totalcount); i++) {
                try {
                    jsonObjects[i][0] = jsonArray.getJSONObject(i).getString("id");
                } catch (JSONException | NullPointerException e) {
                    e.printStackTrace();
                }
                try {
                    jsonObjects[i][1] = jsonArray.getJSONObject(i).getString("order_no");
                } catch (JSONException | NullPointerException e) {
                    e.printStackTrace();
                }
                try {
                    jsonObjects[i][2] = jsonArray.getJSONObject(i).getString("add_time");
                } catch (JSONException | NullPointerException e) {
                    e.printStackTrace();
                }
                try {
                    jsonObjects[i][3] = jsonArray.getJSONObject(i).getString("accept_name");
                } catch (JSONException | NullPointerException e) {
                    e.printStackTrace();
                }
                try {
                    jsonObjects[i][4] = jsonArray.getJSONObject(i).getString("mobile");
                } catch (JSONException | NullPointerException e) {
                    e.printStackTrace();
                }
                try {
                    jsonObjects[i][5] = jsonArray.getJSONObject(i).getString("email");
                } catch (JSONException | NullPointerException e) {
                    e.printStackTrace();
                }
                try {
                    String order_amount = jsonArray.getJSONObject(i).getString("order_amount");
                    if (order_amount.contains(".")) {//有小數點!!
                        order_amount = order_amount.substring(0, order_amount.indexOf("."));
                    }
                    jsonObjects[i][6] = order_amount;
                } catch (JSONException | NullPointerException e) {
                    e.printStackTrace();
                }
                try {
                    jsonObjects[i][7] = jsonArray.getJSONObject(i).getString("status");
                } catch (JSONException | NullPointerException e) {
                    e.printStackTrace();
                }
            }
            DataBaseHelper helper = DataBaseHelper.getmInstance(context);
            SQLiteDatabase database = helper.getWritableDatabase();
            Cursor order_cursor = database.query("shoporder", new String[]{"order_id","order_userid ", "order_no",
                    "order_time", "order_name", "order_phone", "order_email",
                    "order_money", "order_state","order_schedule"}, null, null, null, null, null);
            if (order_cursor != null) {
                ContentValues cv = new ContentValues();
                if (order_cursor.getCount() == 0) {//是新的資料庫 -> 新增資料
                    for (String[] string : jsonObjects) {//會跑[H][]次
                        cv.clear();
                        cv.put("order_userid", UserId);
                        cv.put("order_id", string[0]);
                        cv.put("order_no", string[1]);
                        cv.put("order_time", string[2]);
                        cv.put("order_name", string[3]);
                        cv.put("order_phone", string[4]);
                        cv.put("order_email", string[5]);
                        cv.put("order_money", string[6]);
                        cv.put("order_state", string[7]);
                        long result = database.insert("shoporder", null, cv);
                        returnMessage = returnMessage + "新的資料庫新增資料:" + string[0] + " result:" + result;
                    }

                } else { //已經有資料庫了->確認是否有重複資料 ->確認是否要更新狀態 // -> 確認是否有新的資料
                    for (String[] string : jsonObjects) {
                        Cursor order_cursor_dul = database.query("shoporder", new String[]{"order_id","order_userid ", "order_no",
                                        "order_time", "order_name", "order_phone",
                                        "order_email", "order_money", "order_state","order_schedule"},
                                "order_id=" + string[0], null, null, null, null);
                        if (order_cursor_dul != null && order_cursor_dul.getCount() > 0) {
                            //有重複的資料 ->確認是否更新狀態!
                            order_cursor_dul.moveToFirst();
                            while (order_cursor_dul.isAfterLast()) {
                                if (!order_cursor_dul.getString(7).equals(string[7])) {//資料不相同
                                    cv.clear();
                                    cv.put("order_state", string[7]);
                                    long result = database.update("shoporder", cv, "order_id=?", new String[]{string[0]});
                                    returnMessage = returnMessage + "新的資料庫更新資料:" + string[0] + " result:" + result;
                                }
                                order_cursor_dul.moveToNext();
                            }
                        } else {
                            cv.clear();
                            cv.put("order_userid", UserId);
                            cv.put("order_id", string[0]);
                            cv.put("order_no", string[1]);
                            cv.put("order_time", string[2]);
                            cv.put("order_name", string[3]);
                            cv.put("order_phone", string[4]);
                            cv.put("order_email", string[5]);
                            cv.put("order_money", string[6]);
                            cv.put("order_state", string[7]);
                            long result = database.insert("shoporder", null, cv);
                            returnMessage = returnMessage + "舊的資料庫新增資料:" + string[0] + " result:" + result;
                        }
                        if (order_cursor_dul != null)
                            order_cursor_dul.close();
                    }
                }
                order_cursor.close();
            }
//            database.close();
        }
        return returnMessage;
    }

    @Override
    protected void onPostExecute(String s) {
        if (s == null)
            taskCallBack.TaskDone(false);
        else {
            new OrderOk(UserId,context).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            taskCallBack.TaskDone(true);
        }
        super.onPostExecute(s);
    }
}

