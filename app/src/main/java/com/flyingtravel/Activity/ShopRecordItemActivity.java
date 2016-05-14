package com.flyingtravel.Activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.flyingtravel.R;
import com.flyingtravel.Utility.DataBaseHelper;
import com.flyingtravel.Utility.Functions;
import com.flyingtravel.Utility.GlobalVariable;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

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

public class ShopRecordItemActivity extends AppCompatActivity {
    String OrderId;
    TextView Order_date, Order_no, Order_payment, Order_state, ship_way, ship_name, ship_tel, ship_addr,
            ship_message, money_item, money_ship, money_total;
    LinearLayout carLayout, backImg;
    Context context = ShopRecordItemActivity.this;
    LayoutInflater inflater;
    /*GA*/
    public static Tracker tracker;

    @Override
    protected void onResume() {
        super.onResume();
        /**GA**/
        if(OrderId!=null) {
            tracker.setScreenName("訂單內頁-ID:"+OrderId);
            tracker.send(new HitBuilders.ScreenViewBuilder().build());
        }
        /**GA**/
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shoprecord_item_activity);
        /**GA**/
        GlobalVariable globalVariable = (GlobalVariable) getApplication();
        tracker = globalVariable.getDefaultTracker();
        /**GA**/
        setupUI();
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        Bundle bundle = this.getIntent().getExtras();
        if (bundle.containsKey("WhichItem")) {
            OrderId = bundle.getString("WhichItem");
            new getOrder().execute();
            getDB();
        } else {
            Toast.makeText(ShopRecordItemActivity.this, this.getResources().getString(R.string.wrongData_text), Toast.LENGTH_SHORT).show();
        }

    }

    void setupUI() {
        carLayout = (LinearLayout) findViewById(R.id.shoprecord_itemlayout);
        backImg = (LinearLayout) findViewById(R.id.shoprecorditem_backImg);
        Order_date = (TextView) findViewById(R.id.shoprecord_itemdate);
        Order_no = (TextView) findViewById(R.id.shoprecord_itemno);
        Order_payment = (TextView) findViewById(R.id.shoprecord_itempay);
        Order_state = (TextView) findViewById(R.id.shoprecord_itemstate);
        ship_way = (TextView) findViewById(R.id.shoprecord_itemship);
        ship_name = (TextView) findViewById(R.id.shoprecord_itemcontact);
        ship_tel = (TextView) findViewById(R.id.shoprecord_itemphone);
        ship_addr = (TextView) findViewById(R.id.shoprecord_itemaddr);
        ship_message = (TextView) findViewById(R.id.shoprecord_itemmessage);
        money_item = (TextView) findViewById(R.id.shoprecord_itemaddmoney);
        money_ship = (TextView) findViewById(R.id.shoprecord_itemshipmoney);
        money_total = (TextView) findViewById(R.id.shoprecord_itemtotalmoney);

        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Functions.go(true, ShopRecordItemActivity.this, ShopRecordItemActivity.this,
                        null, null);
            }
        });
    }

    void getDB() {
        DataBaseHelper helper = DataBaseHelper.getmInstance(context);
        SQLiteDatabase database = helper.getWritableDatabase();
        Cursor order_cursor = database.query("shoporder", new String[]{"order_id", "order_userid", "order_no",
                "order_time", "order_name", "order_phone", "order_email",
                "order_money", "order_state", "order_schedule"}, "order_id=" + OrderId, null, null, null, null);
        if (order_cursor != null && order_cursor.getCount() > 0) {
            order_cursor.moveToFirst();
            if (order_cursor.getString(8) != null) {
                Order_state.setText(order_cursor.getString(8));
            }
        }
        if (order_cursor != null)
            order_cursor.close();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK)
            Functions.go(true, ShopRecordItemActivity.this, ShopRecordItemActivity.this,
                    null, null);
        return false;
    }

    class getOrder extends AsyncTask<String, Void, String[][]> {
        ProgressDialog mDialog;

        @Override
        protected void onPreExecute() {
            //Loading Dialog
            mDialog = new ProgressDialog(ShopRecordItemActivity.this);
            mDialog.setMessage(context.getResources().getString(R.string.loading_text));
            mDialog.setCancelable(false);
            if (!mDialog.isShowing()) {
                mDialog.show();
            }
            super.onPreExecute();
        }

        @Override
        protected String[][] doInBackground(String... params) {
//            Log.i("3.11", "*************ShopRecordITEM DO IN BACKGROUND" + OrderId);
            if (OrderId != null) {
                String returnMessage[][] = null;
                /*
                * 0 order_no
                * 1 name
                * 2 total amount
                * 3 time
                * 4 payment
                * 5 phone
                * 6 message
                * 7 itemcount
                * */

                HttpClient client = new DefaultHttpClient();
                HttpPost post = new HttpPost("http://zhiyou.lin366.com/api/order/index.aspx");
                MultipartEntity multipartEntity = new MultipartEntity();
                Charset charset = Charset.forName("UTF-8");
                try {
                    multipartEntity.addPart("json", new StringBody("{\"act\":\"show\"," +
                            "\"id\":\"" + OrderId + "\"}", charset));
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
//                try {
//                    Log.e("3.15", "getString::" + getString);
//                } catch (NullPointerException e) {
//                    e.printStackTrace();
//                }
                String state = null;
                try {
                    state = new JSONObject(getString.substring(getString.indexOf("{"),
                            getString.lastIndexOf("}") + 1)).getString("states");
                } catch (JSONException | NullPointerException e) {
                    e.printStackTrace();
                }
                if (state == null || state.equals("0"))
                    return null;

                int itemCount = 0;
                JSONArray jsonArray = null;
                try {
                    jsonArray = new JSONObject(getString).getJSONArray("goodslist");
                } catch (JSONException | NullPointerException e) {
                    e.printStackTrace();
                }
                if (jsonArray == null || jsonArray.length() == 0) {
                    returnMessage = new String[1][8];
                    returnMessage[0][7] = "0";
                    //購物車沒有東西!!
                } else {
                    itemCount = jsonArray.length();
                    returnMessage = new String[(itemCount + 1)][8];
                    returnMessage[0][7] = itemCount + "";
                    for (int i = 0; i < itemCount; i++) {
                        try {
                            returnMessage[(i + 1)][0] = jsonArray.getJSONObject(i).getString("goods_title");
                        } catch (JSONException | NullPointerException e) {
                            e.printStackTrace();
                        }
                        try {
                            String real_price = jsonArray.getJSONObject(i).getString("real_price");
                            if (real_price.contains(".")) {//有小數點!!
                                real_price = real_price.substring(0, real_price.indexOf("."));
                            }
                            returnMessage[(i + 1)][1] = real_price;
                        } catch (JSONException | NullPointerException e) {
                            e.printStackTrace();
                        }
                        try {
                            returnMessage[(i + 1)][2] = jsonArray.getJSONObject(i).getString("quantity");
                        } catch (JSONException | NullPointerException e) {
                            e.printStackTrace();
                        }
                        try {
                            returnMessage[(i + 1)][3] = jsonArray.getJSONObject(i).getString("money");
                        } catch (JSONException | NullPointerException e) {
                            e.printStackTrace();
                        }
                    }
                }
                try {
                    returnMessage[0][0] = new JSONObject(getString.substring(getString.indexOf("{"),
                            getString.lastIndexOf("}") + 1)).getString("order_no");
                } catch (JSONException | NullPointerException e) {
                    e.printStackTrace();
                }
                try {
                    returnMessage[0][1] = new JSONObject(getString.substring(getString.indexOf("{"),
                            getString.lastIndexOf("}") + 1)).getString("accept_name");
                } catch (JSONException | NullPointerException e) {
                    e.printStackTrace();
                }
                try {
                    String order_amount = new JSONObject(getString.substring(getString.indexOf("{"),
                            getString.lastIndexOf("}") + 1)).getString("order_amount");
                    if (order_amount.contains(".")) {//有小數點!!
                        order_amount = order_amount.substring(0, order_amount.indexOf("."));
                    }
                    returnMessage[0][2] = order_amount;
                } catch (JSONException | NullPointerException e) {
                    e.printStackTrace();
                }
                try {
                    returnMessage[0][3] = new JSONObject(getString.substring(getString.indexOf("{"),
                            getString.lastIndexOf("}") + 1)).getString("add_time");
                } catch (JSONException | NullPointerException e) {
                    e.printStackTrace();
                }
                try {
                    returnMessage[0][4] = new JSONObject(getString.substring(getString.indexOf("{"),
                            getString.lastIndexOf("}") + 1)).getString("payment_title");
                } catch (JSONException | NullPointerException e) {
                    e.printStackTrace();
                }
                try {
                    returnMessage[0][5] = new JSONObject(getString.substring(getString.indexOf("{"),
                            getString.lastIndexOf("}") + 1)).getString("mobile");
                } catch (JSONException | NullPointerException e) {
                    e.printStackTrace();
                }
                try {
                    returnMessage[0][6] = new JSONObject(getString.substring(getString.indexOf("{"),
                            getString.lastIndexOf("}") + 1)).getString("message");
                } catch (JSONException | NullPointerException e) {
                    e.printStackTrace();
                }
                /*
                * 0 order_no
                * 1 name
                * 2 total amount
                * 3 time
                * 4 payment
                * 5 phone
                * 6 message
                * 7 itemcount
                * */

                /**"states": "1",
                 "order_no": "B16031100073383",
                 "accept_name": "name",
                 "order_amount": "4070.00",
                 "add_time": "2016/3/11 0:07:33",
                 "payment_title": "信用 卡",
                 "email": "email",
                 "mobile": "tel",
                 "message": "content",
                 "goodslist": [{
                 "id": "38", -> 沒路用的東西ˊ口ˋ
                 "goods_title": "【福岩屋】紅毛苔 120g",
                 "real_price": "3570.00",
                 "quantity": "1",
                 "money": "3570"
                 }, {
                 "id": "39",
                 "goods_title": "周媽媽神香辣椒 醬",
                 "real_price": "250.00",
                 "quantity": "2",
                 "money": "500"
                 }],
                 * **/


                //如果讀取資料錯誤 不進行之後的動作

                return returnMessage;
            } else return null;
        }

        @Override
        protected void onPostExecute(String[][] strings) {
            mDialog.dismiss();
            if (strings != null) {
                int itemCount = Integer.parseInt(strings[0][7]);
                for (int i = 0; i < itemCount; i++) {
                    //0.1.3
                    View view = inflater.inflate(R.layout.shoprecord_item_caritem, null);
                    TextView name = (TextView) view.findViewById(R.id.shoprecord_itemname);
                    TextView money = (TextView) view.findViewById(R.id.shoprecord_itemmoney);
                    TextView count = (TextView) view.findViewById(R.id.shoprecord_itemcount);
                    if (strings[(i + 1)][0] != null)
                        name.setText(strings[(i + 1)][0]);
                    if (strings[(i + 1)][2] != null)
                        count.setText("  x" + strings[(i + 1)][2]);
                    if (strings[(i + 1)][3] != null)
                        money.setText("$" + strings[(i + 1)][3]);
                    carLayout.addView(view);
                }
                if (strings[0][0] != null)
                    Order_no.setText(strings[0][0]);
                if (strings[0][1] != null)
                    ship_name.setText(strings[0][1]);
                if (strings[0][2] != null)
                    money_total.setText("$" + strings[0][2]);
                if (strings[0][2] != null)
                    money_item.setText("$" + strings[0][2]);
                if (strings[0][3] != null)
                    Order_date.setText(strings[0][3]);
                if (strings[0][4] != null)
                    Order_payment.setText(strings[0][4]);
                if (strings[0][5] != null)
                    ship_tel.setText(strings[0][5]);
                if (strings[0][6] != null)
                    ship_message.setText(strings[0][6]);

            }

            super.onPostExecute(strings);
        }
    }

}
