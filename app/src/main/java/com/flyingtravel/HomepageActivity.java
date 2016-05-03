package com.flyingtravel;

/****
 * //
 * //                       _oo0oo_
 * //                      o8888888o
 * //                      88" . "88
 * //                      (| -_- |)
 * //                      0\  =  /0
 * //                    ___/`---'\___
 * //                  .' \\|     |// '.
 * //                 / \\|||  :  |||// \
 * //                / _||||| -:- |||||- \
 * //               |   | \\\  -  /// |   |
 * //               | \_|  ''\---/''  |_/ |
 * //               \  .-\__  '-'  ___/-. /
 * //             ___'. .'  /--.--\  `. .'___
 * //          ."" '<  `.___\_<|>_/___.' >' "".
 * //         | | :  `- \`.;`\ _ /`;.`/ - ` : | |
 * //         \  \ `_.   \_ __\ /__ _/   .-` /  /
 * //     =====`-.____`.___ \_____/___.-`___.-'=====
 * //                       `=---='
 * //
 * //
 * //     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * //
 * //               佛祖保佑         永無BUG
 * /
 ****/

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.multidex.MultiDex;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.flyingtravel.Activity.LoginActivity;
import com.flyingtravel.Fragment.MainFragment;
import com.flyingtravel.Fragment.MemberFragment;
import com.flyingtravel.Fragment.MoreFragment;
import com.flyingtravel.Fragment.ShopRecordFragment;
import com.flyingtravel.ImageSlide.MainImageFragment;
import com.flyingtravel.Utility.Functions;
import com.flyingtravel.Utility.HttpService;
import com.flyingtravel.Utility.LoadApiService;
import com.flyingtravel.Utility.View.MyTextview;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class HomepageActivity extends FragmentActivity {
    private Fragment contentFragment;
    MainImageFragment homefragment;
    LinearLayout homeLayout, memberLayout, shoprecordLayout, moreLayout;
    TextView homeText, memberText, shoprecordText, moreText;
    ImageView homeImg, memberImg, shoprecordImg, moreImg;
    MyTextview textview;
    Bundle bundle;
    MemberFragment memberFragment = new MemberFragment();
    MainFragment mainFragment = new MainFragment();
    ShopRecordFragment shopRecordFragment = new ShopRecordFragment();
    MoreFragment moreFragment = new MoreFragment();


    //3.10 Hua
    final int REQUEST_PERMISSION = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_test);

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(HomepageActivity.this)
                .build();
        ImageLoader.getInstance().init(config);

        Intent intent_LoadApiService = new Intent(HomepageActivity.this, LoadApiService.class);
        startService(intent_LoadApiService);
        Intent intent = new Intent(HomepageActivity.this, HttpService.class);
        startService(intent);

        // Prompt the user to Enabled GPS
        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean enabled = service.isProviderEnabled(LocationManager.GPS_PROVIDER);

        // check if enabled and if not send user to the GSP settings
        // Better solution would be to display a dialog and suggesting to
        // go to the settings
        if (!enabled) {
            Intent intent_GPS = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent_GPS);
        }

        // API 23 Needs to Check Permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)  != PackageManager.PERMISSION_GRANTED) {

            // Check Permissions Now
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CAMERA,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION);
        } else {
            UI();

            changeFragment(mainFragment);
            homeImg.setClickable(false);
            homeImg.setImageResource(R.drawable.tab_selected_home);
            homeText.setTextColor(getResources().getColor(R.color.blue_click));
        }

    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public void changeFragment(Fragment f) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//        Log.d("4.7","HA:"+getSupportFragmentManager()+"=manager"+getSupportFragmentManager().getFragments());
        transaction.replace(R.id.fragment_test, f);
        transaction.commitAllowingStateLoss();
    }

    void UI() {

        /**TAB**/
        //======= HOME =======//
        homeImg = (ImageView) findViewById(R.id.main_home_img);
        homeText = (TextView) findViewById(R.id.main_home_text);
        homeLayout = (LinearLayout) findViewById(R.id.main_home_layout);
        homeText.setTextColor(getResources().getColor(R.color.gray));
        homeImg.setImageResource(R.drawable.tab_home);
        homeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeFragment(mainFragment);
                memberClick(false);
                homeClick(true);
                moreClick(false);
                orderClick(false);
            }
        });
        homeLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Functions.ClickTouchEvent(homeImg, homeText, "home", false, event.getAction());
                return false;
            }
        });

        //======= HOME =======//

        //HomepageActivity.this,MemberActivity.class
        memberImg = (ImageView) findViewById(R.id.main_member_img);
        memberText = (TextView) findViewById(R.id.main_member_text);
        memberLayout = (LinearLayout) findViewById(R.id.main_member_layout);
        memberImg.setImageResource(R.drawable.tab_member);
        memberText.setTextColor(getResources().getColor(R.color.gray));

        memberLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Functions.ifLogin(HomepageActivity.this)) {
                    AlertDialog goLogin = new AlertDialog.Builder(HomepageActivity.this).create();

                    // 設置對話框標題
                    //getApplicationContext().getResources().getString(R.string.requestLocation_text)
                    goLogin.setTitle(getApplicationContext().getResources().getString(R.string.systemMessage_text));
                    goLogin.setCancelable(false);
                    // 設置對話框消息
                    goLogin.setMessage(getApplicationContext().getResources().getString(R.string.LoginFirst_text));
                    // 添加選擇按鈕並注冊監聽
                    goLogin.setButton(getApplicationContext().getResources().getString(R.string.ok_text), listenerLogin);
                    goLogin.setButton2(getApplicationContext().getResources().getString(R.string.cancel_text), listenerLogin);
                    // 顯示對話框
                    if (!goLogin.isShowing())
                        goLogin.show();
                } else {
                    changeFragment(memberFragment);
                    memberClick(true);
                    homeClick(false);
                    moreClick(false);
                    orderClick(false);
                }


            }
        });
        memberLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Functions.ClickTouchEvent(memberImg, memberText, "member", false, event.getAction());
                return false;
            }
        });

        //======= SHOP RECORD =======//
        shoprecordImg = (ImageView) findViewById(R.id.main_shoprecord_img);
        shoprecordText = (TextView) findViewById(R.id.main_shoprecord_text);
        shoprecordLayout = (LinearLayout) findViewById(R.id.main_shoprecord_layout);
        shoprecordImg.setImageResource(R.drawable.tab_record);
        shoprecordText.setTextColor(getResources().getColor(R.color.gray));
        shoprecordLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Functions.ifLogin(HomepageActivity.this)) {
                    AlertDialog goLogin = new AlertDialog.Builder(HomepageActivity.this).create();

                    // 設置對話框標題
                    goLogin.setTitle(getApplicationContext().getResources().getString(R.string.systemMessage_text));
                    goLogin.setCancelable(false);
                    // 設置對話框消息
                    goLogin.setMessage(getApplicationContext().getResources().getString(R.string.LoginFirst_text));
                    // 添加選擇按鈕並注冊監聽
                    goLogin.setButton(AlertDialog.BUTTON_NEGATIVE, getApplicationContext().getResources().getString(R.string.cancel_text), listenerLogin);
                    goLogin.setButton(AlertDialog.BUTTON_POSITIVE,getApplicationContext().getResources().getString(R.string.ok_text),listenerLogin);
                    /*
    On devices prior to Honeycomb, the button order (left to right) was POSITIVE - NEUTRAL - NEGATIVE.
    On newer devices using the Holo theme, the button order (left to right) is now NEGATIVE - NEUTRAL - POSITIVE.

                    * */
                    // 顯示對話框
                    if (!goLogin.isShowing())
                        goLogin.show();
                } else {
                    changeFragment(shopRecordFragment);
                    memberClick(false);
                    homeClick(false);
                    moreClick(false);
                    orderClick(true);
                }
            }
        });
        shoprecordLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Functions.ClickTouchEvent(shoprecordImg, shoprecordText, "shoprecord", false, event.getAction());
                return false;
            }
        });
        //======= SHOP RECORD =======//

        //======= MORE =======//
        moreImg = (ImageView) findViewById(R.id.main_more_img);
        moreText = (TextView) findViewById(R.id.main_more_text);
        moreLayout = (LinearLayout) findViewById(R.id.main_more_layout);
        moreImg.setImageResource(R.drawable.tab_more);
        moreText.setTextColor(getResources().getColor(R.color.gray));
        moreLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeFragment(moreFragment);
                memberClick(false);
                homeClick(false);
                moreClick(true);
                orderClick(false);
            }
        });
        moreLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Functions.ClickTouchEvent(moreImg, moreText, "more", false, event.getAction());
                return false;
            }
        });
        //======= MORE =======//


    }

    void homeClick(Boolean yes) {
        if (yes) {
            homeText.setTextColor(getResources().getColor(R.color.blue_click));
            homeImg.setImageResource(R.drawable.tab_selected_home);
            homeLayout.setClickable(false);
        } else {
            homeText.setTextColor(getResources().getColor(R.color.gray));
            homeImg.setImageResource(R.drawable.tab_home);
            homeLayout.setClickable(true);
        }
    }

    void memberClick(Boolean yes) {
        if (yes) {
            memberText.setTextColor(getResources().getColor(R.color.blue_click));
            memberImg.setImageResource(R.drawable.tab_selected_member);
            memberLayout.setClickable(false);

        } else {
            memberText.setTextColor(getResources().getColor(R.color.gray));
            memberImg.setImageResource(R.drawable.tab_member);
            memberLayout.setClickable(true);
        }
    }

    void orderClick(Boolean yes) {
        if (yes) {
            shoprecordText.setTextColor(getResources().getColor(R.color.blue_click));
            shoprecordImg.setImageResource(R.drawable.tab_selected_record);
            shoprecordLayout.setClickable(false);
        } else {
            shoprecordText.setTextColor(getResources().getColor(R.color.gray));
            shoprecordImg.setImageResource(R.drawable.tab_record);
            shoprecordLayout.setClickable(true);
        }
    }

    void moreClick(Boolean yes) {
        if (yes) {
            moreText.setTextColor(getResources().getColor(R.color.blue_click));
            moreImg.setImageResource(R.drawable.tab_selected_more);
            moreLayout.setClickable(false);
        } else {
            moreText.setTextColor(getResources().getColor(R.color.gray));
            moreImg.setImageResource(R.drawable.tab_more);
            moreLayout.setClickable(true);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // 創建退出對話框
            AlertDialog isExit = new AlertDialog.Builder(this).create();
            // 設置對話框標題
            isExit.setTitle(getApplicationContext().getResources().getString(R.string.systemMessage_text));
            // 設置對話框消息
            isExit.setMessage(getApplicationContext().getResources().getString(R.string.LeaveMessage_text));
            // 添加選擇按鈕並注冊監聽
            isExit.setButton(getApplicationContext().getResources().getString(R.string.ok_text), listener);
            isExit.setButton2(getApplicationContext().getResources().getString(R.string.cancel_text), listener);
            // 顯示對話框
            isExit.show();

        }
        return false;
    }

    DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case AlertDialog.BUTTON_POSITIVE:// "確認"按鈕退出程序
                    Intent MyIntent = new Intent(Intent.ACTION_MAIN);
                    MyIntent.addCategory(Intent.CATEGORY_HOME);
                    startActivity(MyIntent);
                    finish();
                    break;
                case AlertDialog.BUTTON_NEGATIVE:// "取消"第二個按鈕取消對話框
                    break;
                default:
                    break;
            }
        }
    };
    DialogInterface.OnClickListener listenerLogin = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case AlertDialog.BUTTON_POSITIVE:// "確認"按鈕前往登入
                    Functions.go(false, HomepageActivity.this, HomepageActivity.this, LoginActivity.class, null);
                    break;
                case AlertDialog.BUTTON_NEGATIVE:// "取消"第二個按鈕取消對話框
                    break;
                default:
                    break;
            }
        }
    };

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION) {
            if(grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // We can now safely use the API we requested access to

                UI();

                changeFragment(mainFragment);
                homeImg.setClickable(false);
                homeImg.setImageResource(R.drawable.tab_selected_home);
                homeText.setTextColor(getResources().getColor(R.color.blue_click));

            } else {
                // Permission was denied or request was cancelled
                Toast.makeText(HomepageActivity.this, getApplicationContext().getResources()
                        .getString(R.string.requestLocation_text), Toast.LENGTH_LONG).show();

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION);
            }
        }
    }
}
