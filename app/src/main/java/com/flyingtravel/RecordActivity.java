package com.flyingtravel;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.flyingtravel.Adapter.RecordFragmentPagerAdapter;
import com.flyingtravel.Utility.DataBaseHelper;
import com.flyingtravel.Utility.Functions;

import java.util.ArrayList;
import java.util.List;

public class RecordActivity extends FragmentActivity  {

    public static final String TAG = RecordActivity.class.getSimpleName();

    public static ImageView record_completeImg;
    public static TextView time_text;

    private ImageView BackImg;

    List<android.support.v4.app.Fragment> fragments = new ArrayList<>();

    final int REQUEST_LOCATION = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.record_activity);

        BackImg = (ImageView) findViewById(R.id.record_backImg);
        BackImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Functions.go(true, RecordActivity.this, RecordActivity.this, HomepageActivity.class, null);
            }
        });

        time_text = (TextView) findViewById(R.id.record_time_text);
        time_text.setVisibility(View.INVISIBLE);
        record_completeImg = (ImageView) findViewById(R.id.record_completeImg);
        record_completeImg.setVisibility(View.INVISIBLE);

        DataBaseHelper helper = DataBaseHelper.getmInstance(getApplicationContext());
        SQLiteDatabase database = helper.getWritableDatabase();
        Cursor trackRoute_cursor = database.query("trackRoute",
                new String[]{"routesCounter","track_no", "track_lat", "track_lng",
                        "track_start", "track_title", "track_totaltime", "track_completetime"},
                null, null, null, null, null);
        if (trackRoute_cursor != null) {
            if (trackRoute_cursor.getCount() != 0) {
                trackRoute_cursor.moveToLast();
                Integer track_start = trackRoute_cursor.getInt(4);
                if (track_start != 0) {
                    time_text.setText(trackRoute_cursor.getString(6));
                    time_text.setVisibility(View.VISIBLE);
                    record_completeImg.setVisibility(View.VISIBLE);
                }
            }
            trackRoute_cursor.close();
        }

        // Prompt the user to Enabled GPS
        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean enabled = service.isProviderEnabled(LocationManager.GPS_PROVIDER);

        // check if enabled and if not send user to the GSP settings
        // Better solution would be to display a dialog and suggesting to
        // go to the settings
        if (!enabled) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }

        // API 23 Needs to Check Permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Check Permissions Now
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        }

        initViewPager();

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        System.gc();
        super.onDestroy();
    }

    private void initViewPager(){
        List<android.support.v4.app.Fragment> fragments = getFragments();
        RecordFragmentPagerAdapter adapter = new RecordFragmentPagerAdapter(getSupportFragmentManager(), fragments,getApplicationContext());

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(adapter);

        // Give the TabLayout the ViewPager
        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    private List<android.support.v4.app.Fragment> getFragments() {
        fragments.add(RecordTrackFragment.newInstance("RecordTrack"));
        fragments.add(RecordDiaryFragment.newInstance("RecordDiary"));
        return fragments;
    }

    // Android 系統返回鍵
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Functions.go(true, RecordActivity.this, RecordActivity.this, HomepageActivity.class, null);
        }
        return false;
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_LOCATION) {
            if(grantResults.length == 1
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // We can now safely use the API we requested access to

            } else {
                // Permission was denied or request was cancelled
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
                Toast.makeText(RecordActivity.this, getApplicationContext().getResources().getString(R.string.requestLocation_text), Toast.LENGTH_LONG).show();
            }
        }
    }
}