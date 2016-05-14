package com.flyingtravel.Activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.flyingtravel.HomepageActivity;
import com.flyingtravel.R;
import com.flyingtravel.Utility.DataBaseHelper;
import com.flyingtravel.Utility.GlobalVariable;
import com.flyingtravel.Utility.HttpService;
import com.flyingtravel.Utility.LoadApiService;
import com.flyingtravel.Utility.View.MyAnimation;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
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
import java.util.Timer;
import java.util.TimerTask;

import io.fabric.sdk.android.Fabric;

public class LoginActivity extends AppCompatActivity {
    TextView accountText, passText,
            loginText, signupText,
            forgetText;
    EditText accountEdit, passEdit;
    ProgressDialog mDialog;

    /**
     * GA
     **/
    public static Tracker tracker;

    @Override
    protected void onResume() {
        super.onResume();
        /**GA**/
        tracker.setScreenName("登入");
        tracker.send(new HitBuilders.ScreenViewBuilder().build());
        /**GA**/
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.login_activity);
        /**GA**/
        GlobalVariable globalVariable = (GlobalVariable) getApplication();
        tracker = globalVariable.getDefaultTracker();

        Intent intent_LoadApi = new Intent(LoginActivity.this, LoadApiService.class);
        startService(intent_LoadApi);

        Intent intent = new Intent(LoginActivity.this, HttpService.class);
        startService(intent);

        checkLogin();


        accountText = (TextView) findViewById(R.id.home_account_text);
        passText = (TextView) findViewById(R.id.home_pass_text);
        accountEdit = (EditText) findViewById(R.id.home_account_edit);
        passEdit = (EditText) findViewById(R.id.home_pass_edit);
        loginText = (TextView) findViewById(R.id.home_login_text);
        signupText = (TextView) findViewById(R.id.home_sighup_text);
        signupText = (TextView) findViewById(R.id.home_sighup_text);
        forgetText = (TextView) findViewById(R.id.home_forgetpa_text);
        accountEdit.setVisibility(View.INVISIBLE);
        passEdit.setVisibility(View.INVISIBLE);

        accountText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Log.d("11.18", "textView clicked");
                View view = accountText;
                MyAnimation test = new MyAnimation(view, 150, true);
                accountText.startAnimation(test);
                accountEdit.setVisibility(View.VISIBLE);
                //make the keyboard show
                accountEdit.requestFocus();
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            }
        });
        passText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Log.d("11.18", "pass clicked");
                View view = passText;
                MyAnimation myAnimation = new MyAnimation(view, 150, true);
                passText.startAnimation(myAnimation);
                passEdit.setVisibility(View.VISIBLE);
                //make the keyboard show
                passEdit.requestFocus();
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);


            }
        });
        //11.18 按下textview的動畫
        loginText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (accountEdit.getText().toString().equals("") || !accountEdit.isShown()
                        || passEdit.getText().toString().equals("") || !passEdit.isShown()) {
                    Toast.makeText(LoginActivity.this,
                            LoginActivity.this.getResources().getString(R.string.noaccountAndpassword_text), Toast.LENGTH_SHORT).show();
                } else {
                    /***GA**/
                    tracker.send(new HitBuilders.EventBuilder().setCategory("登入")
//                .setAction("click")
//                .setLabel("submit")
                            .build());
                    /***GA**/
//                    Log.d("1/4", "account:" + accountEdit.getText() + "_ \n password:" + passEdit.getText() + "_");
                    login_Data loginData = new login_Data(accountEdit.getText().toString(),
                            passEdit.getText().toString());
                    loginData.execute();
                }


            }
        });
        forgetText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog findpwdDialog = new Dialog(LoginActivity.this);
                findpwdDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                findpwdDialog.setContentView(R.layout.dialog_findpwd);
                Button OK = (Button) findpwdDialog.findViewById(R.id.findpwd_OkButt);
                Button Cancel = (Button) findpwdDialog.findViewById(R.id.findpwd_CancelButt);
                final EditText accountEdit = (EditText) findpwdDialog.findViewById(R.id.findpwd_accountEdit);
                final EditText emailEdit = (EditText) findpwdDialog.findViewById(R.id.findpwd_emailEdit);
                Cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (findpwdDialog.isShowing())
                            findpwdDialog.dismiss();
                    }
                });
                OK.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (accountEdit.getText().toString().equals("")
                                || emailEdit.getText().toString().equals("")) {
                            Toast.makeText(LoginActivity.this,
                                    LoginActivity.this.getResources().getString(R.string.InputData_text), Toast.LENGTH_SHORT).show();
                        } else {
                            /***GA**/
                            tracker.send(new HitBuilders.EventBuilder().setCategory("忘記密碼")
//                .setAction("click")
//                .setLabel("submit")
                                    .build());
                            /***GA**/
//                    Log.d("1/4
                            findPwd findPwd = new findPwd(findpwdDialog, accountEdit.getText().toString(),
                                    emailEdit.getText().toString());
                            findPwd.execute();
                        }
                    }
                });
                findpwdDialog.show();
//                Toast.makeText(LoginActivity.this, "建構中", Toast.LENGTH_SHORT).show();
            }
        });
        signupText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**GA**/
                tracker.setScreenName("註冊");
                tracker.send(new HitBuilders.ScreenViewBuilder().build());
                /**GA**/
                final Dialog signDialog = new Dialog(LoginActivity.this);
                signDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                signDialog.setContentView(R.layout.dialog_reg);
                Button OK = (Button) signDialog.findViewById(R.id.reg_ok);
                Button cancel = (Button) signDialog.findViewById(R.id.reg_cancel);
                final EditText account = (EditText) signDialog.findViewById(R.id.reg_account);
                final EditText password = (EditText) signDialog.findViewById(R.id.reg_password);
                final EditText name = (EditText) signDialog.findViewById(R.id.reg_name);
                final EditText phone = (EditText) signDialog.findViewById(R.id.reg_phone);
                final EditText email = (EditText) signDialog.findViewById(R.id.reg_email);
                final EditText addr = (EditText) signDialog.findViewById(R.id.reg_addr);
                OK.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (account.getText().toString().equals("")
                                || password.getText().toString().equals("")
                                || name.getText().toString().equals("")
                                || phone.getText().toString().equals("")
                                || email.getText().toString().equals("")
                                || addr.getText().toString().equals("")
                                ) {
                            Toast.makeText(LoginActivity.this,
                                    LoginActivity.this.getResources().getString(R.string.InputData_text), Toast.LENGTH_SHORT).show();
                        } else {
                            /***GA**/
                            tracker.send(new HitBuilders.EventBuilder().setCategory("註冊")
//                .setAction("click")
//                .setLabel("submit")
                                    .build());
                            /***GA**/
//                    Log.d("1/4
                            sighUp sighUp = new sighUp(account.getText().toString(),
                                    password.getText().toString(), name.getText().toString(),
                                    phone.getText().toString(), email.getText().toString(),
                                    addr.getText().toString(),signDialog);
                            sighUp.execute();
                        }
                    }
                });
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (signDialog.isShowing())
                            signDialog.dismiss();
                    }
                });
                signDialog.setCancelable(false);
                signDialog.show();
            }
        });
    } //onCreate

    void checkLogin() {
        DataBaseHelper helper = DataBaseHelper.getmInstance(LoginActivity.this);
        SQLiteDatabase database = helper.getWritableDatabase();
        Cursor member_cursor = database.query("member", new String[]{"account", "password",
                "name", "phone", "email", "addr"}, null, null, null, null, null);
        if (member_cursor != null && member_cursor.getCount() > 0) {
//            Toast.makeText(LoginActivity.this, "登入過了!", Toast.LENGTH_SHORT).show();
            Timer a = new Timer();
            //如果正確才會跳到下個畫面
//            a.schedule(new TimerTask() {
//                @Override
//                public void run() {
            Intent intent = new Intent();
            intent.setClass(LoginActivity.this, HomepageActivity.class);
            startActivity(intent);
            finish();
//                }
//            }, 2500);
        }
        if (member_cursor != null)
            member_cursor.close();
//        else Toast.makeText(LoginActivity.this,"沒登入過!",Toast.LENGTH_SHORT).show();

    }

    class sighUp extends AsyncTask<String, Void, Boolean> {

        String account, password, name, phone, email, message,address;
        Dialog dialog;

        public sighUp(String maccount, String mpassword, String mname, String mphone, String memail,
                      String maddress,
                      Dialog mdialog) {
            this.account = maccount;
            this.password = mpassword;
            this.name = mname;
            this.phone = mphone;
            this.email = memail;
            this.address = maddress;
            this.dialog = mdialog;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost("http://zhiyou.lin366.com/api/user/index.aspx");
            MultipartEntity entity = new MultipartEntity();
            Charset chars = Charset.forName("UTF-8");
            //{"act":"reg","username":"ljd110@qq.com","password":"ljd110@qq.com",
            // "email":"ljd110@qq.com","mobile":"ljd110@qq.com","nickname":"ljd110@qq.com"}
            try {
                entity.addPart("json", new StringBody("{\"act\":\"reg\",\"username\":\""
                        + account + "\",\"password\":\"" + password
                        + "\",\"email\":\"" + email + "\",\"mobile\":\"" + phone
                        + "\",\"nickname\":\"" + name + "\"}", chars));
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
            String state = null;
            try {
                state = new JSONObject(result.substring(
                        result.indexOf("{"), result.lastIndexOf("}") + 1)).getString("states");
            } catch (JSONException | NullPointerException e2) {
                e2.printStackTrace();
            }
            try {
                message = new JSONObject(result.substring(
                        result.indexOf("{"), result.lastIndexOf("}") + 1)).getString("msg");
            } catch (JSONException | NullPointerException e2) {
                e2.printStackTrace();
            }
            return state != null && state.equals("1");
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if (aBoolean)
                message = LoginActivity.this.getResources().getString(R.string.regReply_text) + message;

            Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
            Timer a = new Timer();
            if (aBoolean){
                if(dialog.isShowing())
                    dialog.dismiss();
                new login_Data(account,password).execute();
            }
            super.onPostExecute(aBoolean);
        }
    }

    class findPwd extends AsyncTask<String, Void, Boolean> {
        Dialog mdialog;
        String maccount, memail, message;

        public findPwd(Dialog dialog, String account, String email) {
            mdialog = dialog;
            maccount = account;
            memail = email;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            /**{"act":"findpwd","username":"ljd110@qq.com","email":"ljd110@qq.com"}**/
            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost("http://zhiyou.lin366.com/api/user/index.aspx");
            MultipartEntity entity = new MultipartEntity();
            Charset chars = Charset.forName("UTF-8");
            //"act":"login","username":"ljd110@qq.com","password":"ljd110@qq.com
            try {
                entity.addPart("json", new StringBody("{\"act\":\"findpwd\",\"username\":\""
                        + maccount + "\",\"email\":\"" + memail + "\"}", chars));
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
            String state = null;
            try {
                state = new JSONObject(result.substring(
                        result.indexOf("{"), result.lastIndexOf("}") + 1)).getString("states");
                message = new JSONObject(result.substring(
                        result.indexOf("{"), result.lastIndexOf("}") + 1)).getString("msg");
            } catch (JSONException | NullPointerException e2) {
                e2.printStackTrace();
            }
            return state != null && state.equals("1");


        }

        @Override
        protected void onPostExecute(Boolean s) {
            if (s)
                message = LoginActivity.this.getResources().getString(R.string.errorReply_text) + message;

            Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
            Timer a = new Timer();
            if (s)
                a.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (mdialog.isShowing())
                            mdialog.dismiss();
                    }
                }, 2500);
            super.onPostExecute(s);
        }
    }

    class login_Data extends AsyncTask<String, Void, String> {
        public String maccount, mpassword, mName, mPhone, mEmail, mAddr, login_result;
        Boolean OK = false;

        login_Data(String account, String password) {
            this.maccount = account;
            this.mpassword = password;
        }

        @Override
        protected void onPreExecute() {
            //Loading Dialog
            mDialog = new ProgressDialog(LoginActivity.this);
            mDialog.setMessage(LoginActivity.this.getResources().getString(R.string.logining_text));
            mDialog.setCancelable(false);
            if (!mDialog.isShowing()) {
                mDialog.show();
            }

            //Loading Dialog
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {

                /*
                * http://zhiyou.lin366.com/api/user/index.aspx
                * {"act":"getinfo","uid":"ljd110@qq.com"}
                * default: (error)state:0 (correct)state:1
                *
                * */
            String total = null;
            //0107

            try {

                HttpClient client9 = new DefaultHttpClient();
                HttpPost post9 = new HttpPost("http://zhiyou.lin366.com/api/user/index.aspx");
                MultipartEntity entity9 = new MultipartEntity();
                Charset chars = Charset.forName("UTF-8");
                //"act":"login","username":"ljd110@qq.com","password":"ljd110@qq.com
                entity9.addPart("json", new StringBody("{\"act\":\"login\",\"username\":\""
                        + maccount + "\",\"password\":\"" + mpassword + "\"}", chars));

                post9.setEntity(entity9);
                HttpResponse resp9 = client9.execute(post9);
                total = EntityUtils.toString(resp9.getEntity());

                //取得登入會員資料
//                Log.e("2.26", "msg:" + total);
                String state = null;
                try {
                    state = new JSONObject(total.substring(
                            total.indexOf("{"), total.lastIndexOf("}") + 1)).getString("states");
                } catch (JSONException | NullPointerException e2) {
                    e2.printStackTrace();
                }
                try {
                    login_result = new JSONObject(total.substring(
                            total.indexOf("{"), total.lastIndexOf("}") + 1)).getString("msg");
                } catch (JSONException | NullPointerException e) {
                    e.printStackTrace();
                }

                if (state != null && state.equals("1")) {
                    OK = true;
                    HttpClient client = new DefaultHttpClient();
                    HttpPost post = new HttpPost("http://zhiyou.lin366.com/api/user/index.aspx");
                    MultipartEntity entity = new MultipartEntity();
                    //"act":"login","username":"ljd110@qq.com","password":"ljd110@qq.com
                    entity.addPart("json", new StringBody("{\"act\":\"getinfo\",\"uid\":\""
                            + maccount + "\"}", chars));

                    post.setEntity(entity);
                    HttpResponse resp = client.execute(post);
                    String result = EntityUtils.toString(resp.getEntity());
                    String message = null;
                    try {
                        message = new JSONObject(result.substring(
                                result.indexOf("{"), result.lastIndexOf("}") + 1)).getString("states");
                    } catch (JSONException | NullPointerException e) {
                        e.printStackTrace();
                    }
                    try {
                        login_result = new JSONObject(result.substring(
                                result.indexOf("{"), result.lastIndexOf("}") + 1)).getString("msg");
                    } catch (JSONException | NullPointerException e) {
                        e.printStackTrace();
                    }

                    if (message != null && message.equals("1")) {
                        try {
                            mName = new JSONObject(result.substring(
                                    result.indexOf("{"), result.lastIndexOf("}") + 1)).getString("nick_name");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        try {
                            mPhone = new JSONObject(result.substring(
                                    result.indexOf("{"), result.lastIndexOf("}") + 1)).getString("mobile");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        try {
                            mAddr = new JSONObject(result.substring(
                                    result.indexOf("{"), result.lastIndexOf("}") + 1)).getString("address");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        try {
                            mEmail = new JSONObject(result.substring(
                                    result.indexOf("{"), result.lastIndexOf("}") + 1)).getString("email");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

//                    Log.e("2.26", "getinfo: " + result + "states:" + message);
//                    Log.e("2.26", "name: " + mName);
//                    Log.e("2.26", "phone: " + mPhone);
//                    Log.e("2.26", "Email: " + mEmail);
//                    Log.e("2.26", "Address: " + mAddr);
                }
//                else Log.e("2.26", "state: " + state);


                /*
                * http://zhiyou.lin366.com/api/user/index.aspx
                * default: (error)state:0 (correct)state:1
                *
                * {"act":"getinfo","uid":"ljd110@qq.com"}
                *
                * */


            } catch (UnsupportedEncodingException e) {
//                Log.d("1/7", "UnsupportedEncodingException");
                e.printStackTrace();
            } catch (ClientProtocolException e) {
//                Log.d("1/7", "ClientProtocolException");
                e.printStackTrace();
            } catch (IOException e) {
//                Log.d("1/7", "IOException");
                e.printStackTrace();
            }
//            Log.d("2.26", "login result: " + login_result);

            return total;
        }

        @Override
        protected void onPostExecute(String string) {
            mDialog.dismiss();
            /** 新增會員資料 **/
            if (OK) {
                DataBaseHelper helper = DataBaseHelper.getmInstance(LoginActivity.this);
                SQLiteDatabase database = helper.getWritableDatabase();
                Cursor member_cursor = database.query("member", new String[]{"account", "password",
                        "name", "phone", "email", "addr"}, null, null, null, null, null);

                if (member_cursor != null && member_cursor.getCount() > 0) {
                    database.delete("member", null, null);
                }
                if (member_cursor != null)
                    member_cursor.close();

                ContentValues cv = new ContentValues();
                cv.put("account", maccount);
                cv.put("password", mpassword);
                cv.put("name", mName);
                cv.put("phone", mPhone);
                cv.put("email", mEmail);
                cv.put("addr", mAddr);
                long result = database.insert("member", null, cv);
//                Log.d("2.26", "member_insert:" + result);


            }


            //等toast跑完再跳到下個activity
            if (login_result == null)
                login_result = LoginActivity.this.getResources().getString(R.string.nonet_text);
            if (!OK)
                login_result = LoginActivity.this.getResources().getString(R.string.errorReply_text) + login_result;
            else login_result = LoginActivity.this.getResources().getString(R.string.loginok_text);
            final Toast toast = Toast.makeText(getApplicationContext(),
//                    "=====測試結果=====" + "\n" +
                    login_result, Toast.LENGTH_LONG);
            toast.show();
            //custom time
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    toast.cancel();
                }
            }, 1000);


            Timer a = new Timer();

            if (OK)
            //如果正確才會跳到下個畫面
            {
                a.schedule(new TimerTask() {
                               @Override
                               public void run() {
//                        Intent intent = new Intent();
//                        intent.setClass(LoginActivity.this, HomepageActivity.class);
//                        startActivity(intent);
                                   finish();

                               }
                           },
//                        0
                        1500
                );
            }


            super.onPostExecute(string);
        }
    }
}
