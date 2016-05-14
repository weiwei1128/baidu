package com.flyingtravel.Activity;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.flyingtravel.Adapter.CheckScheduleFragmentAdapter;
import com.flyingtravel.Fragment.CheckScheduleFragment;
import com.flyingtravel.R;
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
import java.util.ArrayList;
import java.util.List;

public class CheckScheduleOKActivity extends AppCompatActivity {
    String itemid = null;
    LinearLayout backImg;
    CheckScheduleFragmentAdapter checkScheduleFragmentAdapter;

    String[][] data;
    String[] summary, address, lat;
    int count = 0;
    List<Fragment> fragments = new ArrayList<>();
    TextView dayText;
    TextView dateText;
    /**
     * GA
     **/
    public static Tracker tracker;
    //

    @Override
    protected void onResume() {
        super.onResume();
        if (itemid != null) {
            Log.e("5.8", "not null");
            /**GA**/
            tracker.setScreenName("行程查詢內頁-ID:" + itemid);
            tracker.send(new HitBuilders.ScreenViewBuilder().build());
            /**GA**/
        }
    }

    /**
     * http://zhiyou.lin366.com/test/diyline.aspx
     * http://zhiyou.lin366.com/api/diy/line.aspx
     * {"act":"show","id":"12"}
     **/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.checkschedule_okactivity);
        /**GA**/
        GlobalVariable globalVariable = (GlobalVariable) getApplication();
        tracker = globalVariable.getDefaultTracker();
        /**GA**/
        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null && bundle.containsKey("order_id")) {
            itemid = bundle.getString("order_id");
            new getScheduleDetail(itemid, new Functions.TaskCallBack() {
                @Override
                public void TaskDone(Boolean OrderNeedUpdate) {
                    methodThatDoesSomethingWhenTaskIsDone(OrderNeedUpdate);
                }
            }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//            setupWebview();
        } else Toast.makeText(CheckScheduleOKActivity.this,
                CheckScheduleOKActivity.this.getResources().getString(R.string.wrongData_text), Toast.LENGTH_SHORT).show();
        dayText = (TextView) findViewById(R.id.checkschedule_dayText);
        dateText = (TextView) findViewById(R.id.checkschedule_dateText);
        backImg = (LinearLayout) findViewById(R.id.checkschedule_backImg);
        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Functions.go(true, CheckScheduleOKActivity.this, CheckScheduleOKActivity.this,
                        CheckScheduleActivity.class, null);
            }
        });

    }

    //    }
    class getScheduleDetail extends AsyncTask<String, Void, Boolean> {
        ProgressDialog progressDialog = new ProgressDialog(CheckScheduleOKActivity.this);
        Functions.TaskCallBack taskCallBack;

        public getScheduleDetail(String uid, Functions.TaskCallBack taskCallBack) {
            this.taskCallBack = taskCallBack;
        }

        @Override
        protected void onPreExecute() {
            progressDialog.setMessage(CheckScheduleOKActivity.this.getResources().getString(R.string.loading_text));
//            progressDialog.setCancelable(false);
            progressDialog.show();
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            Boolean result = false;

            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost("http://zhiyou.lin366.com/api/diy/line.aspx");
            MultipartEntity entity = new MultipartEntity();
            Charset chars = Charset.forName("UTF-8");
            try {
                entity.addPart("json", new StringBody("{\"act\":\"show\",\"id\":\"" + itemid + "\"}", chars));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            post.setEntity(entity);
            HttpResponse resp = null;
            String resultM = null;
            String states = null;
            String message = "";
            try {
                resp = client.execute(post);
                resultM = EntityUtils.toString(resp.getEntity());
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                states = new JSONObject(resultM.substring(
                        resultM.indexOf("{"), resultM.lastIndexOf("}") + 1)).getString("states");
            } catch (JSONException | NullPointerException e2) {
                e2.printStackTrace();
            }
//            Log.i("3.25", "doInBackground" + states);
            if (states == null || states.equals("0"))
                return false;
            else {
                JSONArray jsonArray = null;

                try {
                    jsonArray = new JSONObject(resultM).getJSONArray("jindianlist");
                } catch (JSONException | NullPointerException e) {
                    e.printStackTrace();
                }
                if (jsonArray != null && jsonArray.length() > 0) {

                    count = jsonArray.length();
                    data = new String[count][6];
                    summary = new String[count];
                    address = new String[count];
                    lat = new String[count];
//                    Log.e("4.26","jsonlength:"+count);
                    String temp_summary = null, temp_address = null, temp_lat = null;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        try {
                            data[i][0] = jsonArray.getJSONObject(i).getString("day");
                        } catch (JSONException | NullPointerException e) {
                            e.printStackTrace();
                        }
                        if (i != 0 && data[i][0].equals(data[i - 1][0])) {
                            summary[i - 1] = temp_summary;
                            try {
                                summary[i] = jsonArray.getJSONObject(i).getString("summary");
                            } catch (JSONException | NullPointerException e) {
                                e.printStackTrace();
                            }

                            address[i - 1] = temp_address;
                            try {
                                address[i] = jsonArray.getJSONObject(i).getString("address");
                            } catch (JSONException | NullPointerException e) {
                                e.printStackTrace();
                            }

                            lat[i - 1] = temp_lat;

                            try {
                                lat[i] = jsonArray.getJSONObject(i).getString("jinwei");
                            } catch (JSONException | NullPointerException e) {
                                e.printStackTrace();
                            }
                        }
//                        Log.e("4.26","data[i][0]:"+data[i][0]+" i:"+i);
                        try {
                            data[i][1] = jsonArray.getJSONObject(i).getString("date");
                        } catch (JSONException | NullPointerException e) {
                            e.printStackTrace();
                        }
                        try {
                            data[i][2] = jsonArray.getJSONObject(i).getString("time");
                        } catch (JSONException | NullPointerException e) {
                            e.printStackTrace();
                        }
                        try {
                            data[i][3] = jsonArray.getJSONObject(i).getString("summary");
                        } catch (JSONException | NullPointerException e) {
                            e.printStackTrace();
                        }
                        temp_summary = data[i][3];
                        try {
                            data[i][4] = jsonArray.getJSONObject(i).getString("address");
                        } catch (JSONException | NullPointerException e) {
                            e.printStackTrace();
                        }
                        temp_address = data[i][4];
                        try {
                            data[i][5] = jsonArray.getJSONObject(i).getString("jinwei");
                        } catch (JSONException | NullPointerException e) {
                            e.printStackTrace();
                        }

                        temp_lat = data[i][5];
                    }
                    return true;
                } else return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean s) {
            if (progressDialog.isShowing())
                progressDialog.dismiss();
            taskCallBack.TaskDone(s);
            super.onPostExecute(s);
        }
    }

    private void methodThatDoesSomethingWhenTaskIsDone(Boolean a) {
        if (a) {
            Boolean addFragment = false;
            for (int i = 0; i < count; i++) {
//                Log.d("4.26","for:"+i+"-count:"+count);
//
                if ((i != 0 && data[i][0].equals(data[i - 1][0]) || (i == 0 && count > 1 && data[i][0].equals(data[i + 1][0])))) {
//                    Log.d("4.26","in if::"+data[i][4]+"i:"+i+"add:"+addFragment);
                    if (addFragment)
                        break;
                    CheckScheduleFragment fragment = new CheckScheduleFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("scheduleday", data[i][0]);
                    bundle.putString("scheduledate", data[i][1]);
                    bundle.putString("scheduletime", data[i][2]);
                    for (int k = 0; k < summary.length; k++) {
                        bundle.putString("schedulesummary" + k, summary[k]);
                        bundle.putString("scheduleaddress" + k, address[k]);
                        bundle.putString("scheduleajinwei" + k, lat[k]);
                    }
                    bundle.putInt("schedulecount", count);
                    addFragment = true;
                    if (data[i][4] != null)
                        bundle.putString("schedulejinwei", data[i][4]);
                    else if (data[i][5] != null)
                        bundle.putString("schedulejinwei", data[i][5]);
                    if (itemid != null)
                        bundle.putString("scheduleid", itemid);
                    fragment.setArguments(bundle);
                    fragments.add(fragment);
                } else {
                    CheckScheduleFragment fragment = new CheckScheduleFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("scheduleday", data[i][0]);
                    bundle.putString("scheduledate", data[i][1]);
                    bundle.putString("scheduletime", data[i][2]);
                    bundle.putString("schedulesummary", data[i][3]);
                    if (data[i][4] != null)
                        bundle.putString("schedulejinwei", data[i][4]);
                    else if (data[i][5] != null)
                        bundle.putString("schedulejinwei", data[i][5]);
                    if (itemid != null)
                        bundle.putString("scheduleid", itemid);
                    fragment.setArguments(bundle);
                    fragments.add(fragment);
                }

            }
//            Log.e("4.26","fragments"+fragments.size());
            ViewPager viewPager = (ViewPager) findViewById(R.id.checkschedule_viewpager);
            checkScheduleFragmentAdapter = new CheckScheduleFragmentAdapter(
                    CheckScheduleOKActivity.this.getSupportFragmentManager(),
                    viewPager, fragments, CheckScheduleOKActivity.this);
            viewPager.setOffscreenPageLimit(1);
            viewPager.setAdapter(checkScheduleFragmentAdapter);
            viewPager.setOnPageChangeListener(new PageListener());
            dayText.setText("Day" + data[0][0]);
            dateText.setText(data[0][1]);
        }
    }

    private class PageListener implements ViewPager.OnPageChangeListener {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        public void onPageSelected(int position) {
//            Log.e("4.26","onPageSelected"+position+"--data[position][0]"+data[position][0]);
            dayText.setText("Day" + data[position][0]);
            dateText.setText(data[position][1]);
            /*
            pageNo = position + 1;
            if (pageNo == pages)
                nextPage.setVisibility(View.INVISIBLE);
            else nextPage.setVisibility(View.VISIBLE);

            if (pageNo == 1)
                lastPage.setVisibility(View.INVISIBLE);
            else lastPage.setVisibility(View.VISIBLE);

            minus = pageNo - 1;
            String get = String.valueOf(position + 1);
            number.setText(get);
            */
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK)
//            if (ifWebView && webView.canGoBack())
//                webView.goBack();
//            else
            Functions.go(true, CheckScheduleOKActivity.this, CheckScheduleOKActivity.this,
                    CheckScheduleActivity.class, null);
        return super.onKeyDown(keyCode, event);
    }

}
