package com.flyingtravel.Activity.Buy;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.flyingtravel.R;
import com.flyingtravel.Utility.DataBaseHelper;
import com.flyingtravel.Utility.Functions;

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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;

public class BuyItemListConfirmActivity extends AppCompatActivity {
    TextView buylistText, totalText;
    EditText nameEdit, telEdit, emailEdit, addrEdit, messageEdit;
    String idS, nameS = null, phoneS = null, emailS = null, addrS = null, messageS = null;
    DataBaseHelper helper;
    SQLiteDatabase database;
    LinearLayout confrimLayout, backImg;
    //get shop list item
    final HashMap<String, Integer> cartList = new HashMap<>();
    //get remove list
    final HashSet<String> removeList = new HashSet<>();
    int removeCount = 0;
    final HashMap<Integer, String> remove = new HashMap<>();


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Functions.go(true, BuyItemListConfirmActivity.this, BuyItemListConfirmActivity.this,
                    BuyItemListActivity.class, null);
        }

        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.buyitem_list_confirm_activity);
        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null && bundle.containsKey("AfterPay") && bundle.getBoolean("AfterPay"))
            finish();



        helper =DataBaseHelper.getmInstance(BuyItemListConfirmActivity.this);
        database = helper.getWritableDatabase();

        UI();
        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Functions.go(true, BuyItemListConfirmActivity.this, BuyItemListConfirmActivity.this,
                        BuyItemListActivity.class, null);
            }
        });

        confrimLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(nameEdit.getText().toString().equals("")||telEdit.getText().toString().equals("")||
                        emailEdit.getText().toString().equals("")||addrEdit.getText().toString().equals(""))
                    Toast.makeText(BuyItemListConfirmActivity.this,
                            BuyItemListConfirmActivity.this.getResources().getString(R.string.InputData_text),Toast.LENGTH_SHORT).show();
                else {
                    Cursor member_cursor = database.query("member", new String[]{"account", "password",
                            "name", "phone", "email", "addr"}, null, null, null, null, null);
                    if (member_cursor != null) {
                        if (member_cursor.getCount() > 0) {
                            member_cursor.moveToFirst();
                            idS = member_cursor.getString(0);
                        }
                        member_cursor.close();//old85->102
                    }
                    if (!nameEdit.getText().toString().equals(""))
                        nameS = nameEdit.getText().toString();
                    if (!telEdit.getText().toString().equals(""))
                        phoneS = telEdit.getText().toString();
                    if (!emailEdit.getText().toString().equals(""))
                        emailS = emailEdit.getText().toString();
                    if (!addrEdit.getText().toString().equals(""))
                        addrS = addrEdit.getText().toString();
                    if (!messageEdit.getText().toString().equals(""))
                        messageS = messageEdit.getText().toString();
                    new SendOrder().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
//                for (Object key : cartList.keySet()) {
//                    System.out.println(key + " : " + cartList.get(key));
//                }


//                Toast.makeText(BuyItemListConfirmActivity.this, "建構中!", Toast.LENGTH_SHORT).show();
//                Log.i("3.24", "要remove的東西:" + removeList.size());
            }
        });
//        removeList.add(sharedPreferences.getString("InBuyList", null) + "");
//        Log.i("3.24", "INBUYLIST:" + sharedPreferences.getInt("InBuyList", 0));

    }

    void UI() {
        buylistText = (TextView) findViewById(R.id.buyitemlistconfirm_listText);
        backImg = (LinearLayout) findViewById(R.id.buyitemlistconfirm_backImg);
        confrimLayout = (LinearLayout) findViewById(R.id.buyitemlistconfirm_confirmLay);
        nameEdit = (EditText) findViewById(R.id.buyitemlistconfirm_nameEdit);
        telEdit = (EditText) findViewById(R.id.buyitemlistconfirm_telEdit);
        emailEdit = (EditText) findViewById(R.id.buyitemlistconfirm_emailEdit);
        addrEdit = (EditText) findViewById(R.id.buyitemlistconfirm_addrEdit);
        messageEdit = (EditText) findViewById(R.id.buyitemlistconfirm_messageEdit);
        totalText = (TextView) findViewById(R.id.buyitemlistconfirm_totalText);
        int totalnumber = 0, getitemPosition = 0, BiginCart = 0, totalmoney = 0;
        String BigitemID = null, SmallitemID = null, itemName = null;
        Cursor goods_cursor = database.query("goods", new String[]{"totalCount", "goods_id", "goods_title",
                "goods_url", "goods_money", "goods_content", "goods_addtime"}, null, null, null, null, null);
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (goods_cursor != null && goods_cursor.getCount() != 0) {
            while (goods_cursor.moveToNext()) {
                BiginCart = sharedPreferences.getInt("InBuyListg" + goods_cursor.getString(1), 0);
                removeList.add("InBuyListg" + goods_cursor.getString(1));
                removeCount++;
                remove.put(removeCount, "InBuyListg" + goods_cursor.getString(1));
                if (BiginCart > 0) {
                    for (int k = 0; k < BiginCart; k++) {
                        String a = sharedPreferences.getString("InBuyListg" + goods_cursor.getString(1) + "id" + (k + 1), null);
                        int smallItemCount = sharedPreferences.getInt("InBuyListgC" + goods_cursor.getString(1) + "id" + (k + 1), 0);
                        removeList.add("InBuyListg" + goods_cursor.getString(1) + "id" + (k + 1));
                        removeList.add("InBuyListgC" + goods_cursor.getString(1) + "id" + (k + 1));
                        removeCount++;
                        remove.put(removeCount, "InBuyListg" + goods_cursor.getString(1) + "id" + (k + 1));
                        removeCount++;
                        remove.put(removeCount, "InBuyListgC" + goods_cursor.getString(1) + "id" + (k + 1));
                        if (a != null && smallItemCount != 0) {

                            BigitemID = goods_cursor.getString(1);
                            getitemPosition = k + 1;
                            SmallitemID = a;

                            if (BigitemID != null) {
                                Cursor goods_cursor_big = database.query("goodsitem", new String[]{"goods_bigid",
                                                "goods_itemid", "goods_title", "goods_money", "goods_url"},
                                        "goods_bigid=? and goods_itemid=?", new String[]{BigitemID, SmallitemID}, null, null, null);
                                goods_cursor_big.moveToFirst();
                                int money = Integer.valueOf(goods_cursor_big.getString(3)) * smallItemCount;
                                totalmoney = totalmoney + money;
                                buylistText.append(goods_cursor.getString(2) + " " + goods_cursor_big.getString(2) + " : "
                                        + smallItemCount +BuyItemListConfirmActivity.this.getResources().getString(R.string.a_text)
                                        +" $" + money + "\n");
                                cartList.put(SmallitemID, smallItemCount);
                            }
                        }
                    }
                }
//                else {//這個大項目沒有小項目在購物車裡面
//                    Log.e("3.24", "這不是我要的!!!!" + getPosition + "." + position+"///"+goods_cursor.getString(1));
//                }
//                Log.i("3.24","我在while裡面!!!要執行下一輪");

            }
        }

        //////////^^^^^
        if (goods_cursor != null)
            goods_cursor.close();

        totalText.setText(totalmoney + "");
    }


    /**
     * http://zhiyou.lin366.com/api/order/index.aspx
     * {"act":"add","uid":"ljd110@qq.com","name":"name","tel":"tel",
     * "email":"email","content":"content","express":"1","payment":"3",
     * "sname":"sname","stel":"stel","semail":"semail","sstate":"sstate",
     * "scity":"scity","saddress":"saddress","carlist":[{"gid":"123","num":"1"},
     * {"gid":"123","num":"2"}]}
     * <p/>
     * express!=null
     * payment!=null
     * <p/>
     * <p/>
     * 回傳資料
     * {"states":"1","msg":"加入成功","id":"45"}
     */
    class SendOrder extends AsyncTask<String, Void, String> {
        //String idS, nameS = null, phoneS = null, emailS = null, addrS = null;
        final ProgressDialog progressDialog = new ProgressDialog(BuyItemListConfirmActivity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setMessage(BuyItemListConfirmActivity.this.getResources().getString(R.string.sending_text));
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost("http://zhiyou.lin366.com/api/order/index.aspx");
            MultipartEntity multipartEntity = new MultipartEntity();
            Charset charset = Charset.forName("UTF-8");
            String carlist = "";
            Boolean first = true;
            for (Object key : cartList.keySet()) {
                if (first) {
                    first = false;
                    carlist = "{\"gid\":\"" + key + "\",\"num\":\"" + cartList.get(key) + "\"}";
                } else
                    carlist = carlist + ",{\"gid\":\"" + key + "\",\"num\":\"" + cartList.get(key) + "\"}";
//                    System.out.println(key + " : " + cartList.get(key));
            }
//            Log.i("3.24", "carList:" + carlist);
            try {
                multipartEntity.addPart("json",
                        new StringBody("{\"act\":\"add\"," +
                                "\"uid\":\"" + idS + "\",\"name\":\"" + nameS
                                + "\",\"tel\":\"" + phoneS + "\",\"email\":\"" + emailS
                                + "\",\"content\":\"" + messageS + "\",\"express\":\"" + 1
                                + "\",\"payment\":\"" + 3 + "\",\"sname\":\"" + nameS
                                + "\",\"stel\":\"" + phoneS + "\",\"semail\":\"" + emailS
                                + "\",\"sstate\":\"" + "" + "\",\"scity\":\"" + ""
                                + "\",\"saddress\":\"" + addrS + "\",\"carlist\":[" + carlist + "]}", charset));
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
            //{"states":"1","msg":"加入成功","id":"60"}
            try {
                getString = EntityUtils.toString(response.getEntity());
//                Log.d("4.22","response:"+getString);
            } catch (IOException | NullPointerException e) {
                e.printStackTrace();
//                Log.e("3.10", e.toString() + "error");
            }
//            Log.d("4.22","response:"+getString);
            String state = null;
            try {
                state = new JSONObject(getString.substring(getString.indexOf("{"), getString.lastIndexOf("}") + 1)).getString("states");
            } catch (JSONException | NullPointerException e) {
                e.printStackTrace();
            }
            if (state == null || state.equals("0"))
                return null;
            else {
                String id = null;
                try {
                    id = new JSONObject(getString.substring(getString.indexOf("{"), getString.lastIndexOf("}") + 1)).getString("id");
                } catch (JSONException | NullPointerException e) {
                    e.printStackTrace();
                }
                return id;
            }
        }

        @Override
        protected void onPostExecute(final String s) {
            super.onPostExecute(s);
            if (s != null) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(BuyItemListConfirmActivity.this);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear();
                editor.apply();
//                for (Object key : remove.keySet()) {
//                    editor.remove(remove.get(key));
//                    System.out.println(key + " : " + remove.get(key));
//                }
                if (progressDialog.isShowing())
                    progressDialog.dismiss();
                progressDialog.setCancelable(false);
                progressDialog.setMessage(BuyItemListConfirmActivity.this.getResources().getString(R.string.sendingOk_text)
                        + "\n" + BuyItemListConfirmActivity.this.getResources().getString(R.string.gotoPay_text));
                progressDialog.show();
                Timer a = new Timer();
                a.schedule(new TimerTask() {
                               @Override
                               public void run() {
                                   Bundle bundle = new Bundle();
                                   bundle.putString("confirmId", s);
                                   Functions.go(false, BuyItemListConfirmActivity.this, BuyItemListConfirmActivity.this,
                                           BuyItemListConfirmWebview.class, bundle);
                                   if (progressDialog.isShowing())
                                       progressDialog.dismiss();
                                   finish();
                               }
                           },
                        1500
                );

            } else {
                if (progressDialog.isShowing())
                    progressDialog.dismiss();
                Toast.makeText(BuyItemListConfirmActivity.this, BuyItemListConfirmActivity.this.getResources().getString(R.string.sendingError_text), Toast.LENGTH_SHORT).show();
            }
        }
    }


}
