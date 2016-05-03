package com.flyingtravel.Activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.flyingtravel.HomepageActivity;
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


/**
 * 0309 LinearLayout
 **/
public class ServiceActivity extends AppCompatActivity {
    LinearLayout backImg;
    EditText commentEdt;
    LinearLayout sendLayout;
    String name, email, phone;
    TextView companyText,phoneText,timeText,lineIdText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.service_activity);
        backImg = (LinearLayout) findViewById(R.id.service_backImg);
        commentEdt = (EditText) findViewById(R.id.service_edit);
        sendLayout = (LinearLayout) findViewById(R.id.service_send_layout);
        companyText = (TextView)findViewById(R.id.service_companyText);
        phoneText = (TextView)findViewById(R.id.service_phoneText);
        timeText = (TextView)findViewById(R.id.service_timeText);
        lineIdText = (TextView)findViewById(R.id.service_lineText);


        new getData().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        DataBaseHelper helper = DataBaseHelper.getmInstance(ServiceActivity.this);
        SQLiteDatabase database = helper.getWritableDatabase();
        Cursor member_cursor = database.query("member", new String[]{"account", "password",
                "name", "phone", "email", "addr"}, null, null, null, null, null);
        if (member_cursor != null && member_cursor.getCount() > 0) {
            member_cursor.moveToFirst();
//            Log.d("2.26", "DB " + member_cursor.getString(2));
            name = (member_cursor.getString(2));
            phone = (member_cursor.getString(3));
            email = (member_cursor.getString(4));
        }
        if (member_cursor != null)
            member_cursor.close();

        sendLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Log.d("3.9", "ServiceActivity sendLayout clicked!" + commentEdt.getText().toString());
                if (commentEdt.getText().toString().equals(""))
                    Toast.makeText(ServiceActivity.this, ServiceActivity.this.getResources().getString(R.string.emptyInput_text), Toast.LENGTH_SHORT).show();
                else
                    new sendMessage(commentEdt.getText().toString()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });

        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Log.e("1/29", "SERVICE2" + commentEdt.getText().toString());
                if (!commentEdt.getText().toString().equals("")) {
                    // 創建退出對話框
                    AlertDialog isExit = new AlertDialog.Builder(ServiceActivity.this).create();
                    // 設置對話框標題
                    isExit.setTitle(ServiceActivity.this.getResources().getString(R.string.notsend_text));
                    // 設置對話框消息
                    isExit.setMessage(ServiceActivity.this.getResources().getString(R.string.LeaveMessage_text));
                    // 添加選擇按鈕並注冊監聽
                    isExit.setButton(ServiceActivity.this.getResources().getString(R.string.ok_text), listener);
                    isExit.setButton2(ServiceActivity.this.getResources().getString(R.string.cancel_text), listener);
                    // 顯示對話框
                    isExit.show();
                } else {
                    Functions.go(true, ServiceActivity.this, ServiceActivity.this, HomepageActivity.class, null);
                }
            }
        });


    }

    DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case AlertDialog.BUTTON_POSITIVE:// "確認"按鈕退出此頁
                    Functions.go(false, ServiceActivity.this, ServiceActivity.this, HomepageActivity.class, null);
                    break;
                case AlertDialog.BUTTON_NEGATIVE:// "取消"第二個按鈕取消對話框
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Functions.go(true, ServiceActivity.this, ServiceActivity.this, HomepageActivity.class, null);
        }
        return false;
    }

    class getData extends AsyncTask<String,Void,Boolean>{
        ProgressDialog dialog = new ProgressDialog(ServiceActivity.this);
        String company=null,phone=null,time=null,lineId=null;
        @Override
        protected void onPreExecute() {
            dialog.setMessage(ServiceActivity.this.getResources().getString(R.string.loading_text));
            dialog.show();
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            HttpClient client = new DefaultHttpClient();
            //http://zhiyou.lin366.com/api/feedback/index.aspx
            //http://zhiyou.lin366.com/api/feedback/content.aspx
            HttpPost post = new HttpPost("http://zhiyou.lin366.com/api/feedback/content.aspx");
            MultipartEntity entity = new MultipartEntity();
            Charset chars = Charset.forName("UTF-8");
            try {
                entity.addPart("json", new StringBody("{\"act\":\"content\"}", chars));
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
            String message = null;
            try {
                message = new JSONObject(result.substring(
                        result.indexOf("{"), result.lastIndexOf("}") + 1)).getString("states");
            } catch (JSONException | NullPointerException e2) {
                e2.printStackTrace();
            }
            if(message==null||!message.equals("1"))
                return false;
            else {
                try {
                    company = new JSONObject(result.substring(
                            result.indexOf("{"), result.lastIndexOf("}") + 1)).getString("company");
                } catch (JSONException | NullPointerException e2) {
                    e2.printStackTrace();
                }

                try {
                    phone = new JSONObject(result.substring(
                            result.indexOf("{"), result.lastIndexOf("}") + 1)).getString("tel");
                } catch (JSONException | NullPointerException e2) {
                    e2.printStackTrace();
                }
                try {
                    time = new JSONObject(result.substring(
                            result.indexOf("{"), result.lastIndexOf("}") + 1)).getString("time");
                } catch (JSONException | NullPointerException e2) {
                    e2.printStackTrace();
                }
                try {
                    lineId = new JSONObject(result.substring(
                            result.indexOf("{"), result.lastIndexOf("}") + 1)).getString("line");
                } catch (JSONException | NullPointerException e2) {
                    e2.printStackTrace();
                }
                return true;
            }
        }

        @Override
        protected void onPostExecute(Boolean s) {
            if(dialog.isShowing())
                dialog.dismiss();
            if(s){
            companyText.setText(company);
                Log.e("5.3", "company" + company + "phone:" + phone + "time: " + time + "lineID: " + lineId);
                phoneText.setText(phone);

                lineIdText.setText("LINE ID：" + time);
//                timeText.append(time);
                timeText.setText(ServiceActivity.this.getResources().getString(R.string.serviceTime_text)+time);
            }else Toast.makeText(ServiceActivity.this,ServiceActivity.this.getResources().getString(R.string.wrongData_text),Toast.LENGTH_SHORT).show();
            super.onPostExecute(s);
        }
    }

    class sendMessage extends AsyncTask<String, Void, String> {
        String m_messgae;
        ProgressDialog mDialog;

        //http://zhiyou.lin366.com/api/feedback/index.aspx
        public sendMessage(String message) {
            this.m_messgae = message;
        }

        @Override
        protected void onPreExecute() {
            //Loading Dialog
            mDialog = new ProgressDialog(ServiceActivity.this);
            mDialog.setMessage(ServiceActivity.this.getResources().getString(R.string.sending_text));
            mDialog.setCancelable(false);
            if (!mDialog.isShowing()) {
                mDialog.show();
            }
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
//            Log.e("3.9", "Service do in background");
            HttpClient client = new DefaultHttpClient();
            //http://zhiyou.lin366.com/api/feedback/index.aspx
            HttpPost post = new HttpPost("http://zhiyou.lin366.com/api/feedback/index.aspx");
            MultipartEntity entity = new MultipartEntity();
            Charset chars = Charset.forName("UTF-8");
            try {
                entity.addPart("json",
                        new StringBody("{\"act\":\"add\",\"title\":\"title\",\"name\":\"" + name + "\",\"email\":\"" + email + "\",\"content\":\"" + m_messgae + "\",\"tel\":\"" + phone + "\"}", chars));
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
            String message = null;
            try {
                message = new JSONObject(result.substring(
                        result.indexOf("{"), result.lastIndexOf("}") + 1)).getString("msg");
            } catch (JSONException | NullPointerException e2) {
                e2.printStackTrace();
            }
            return message;
        }

        @Override
        protected void onPostExecute(String s) {
            mDialog.dismiss();
            commentEdt.setText("");
            if (s == null)
                s = ServiceActivity.this.getResources().getString(R.string.connecterror_text);
            Toast.makeText(ServiceActivity.this, ServiceActivity.this.getResources().getString(R.string.reply_text)+"\n" + s, Toast.LENGTH_SHORT).show();
            super.onPostExecute(s);
        }


    }
}
