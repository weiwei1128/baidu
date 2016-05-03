package com.flyingtravel.Fragment;

import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.flyingtravel.Adapter.SpotListFragmentViewPagerAdapter;
import com.flyingtravel.R;
import com.flyingtravel.Utility.DataBaseHelper;
import com.flyingtravel.Utility.GetSpotsNSort;
import com.flyingtravel.Utility.GlobalVariable;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

public class SpotListFragment extends Fragment implements
        com.google.android.gms.location.LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    public static final String TAG = SpotListFragment.class.getSimpleName();
    private static final String FRAGMENT_NAME = "FRAGMENT_NAME";
    //private static final String ARG_PARAM2 = "param2";
    private String mFragmentName;
    //private String mParam2;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    // Location updates intervals in sec
    private static int UPDATE_INTERVAL = 5000; // 5 sec
    private static int FATEST_INTERVAL = 1000; // 1 sec
    private static int DISPLACEMENT = 3;       // 5 meters

    private Location CurrentLocation;

    int count = 0, pageNo = 1, pages = 0, minus = pageNo-1;
    private TextView number, lastPage, nextPage;
    private LinearLayout spotList_pageLayout, spotList_textLayout;

    public static ViewPager viewPager;
    private ProgressBar progressBar;
    private SpotListFragmentViewPagerAdapter adapter;

    private List<Fragment> fragments = new ArrayList<>();

    private DataBaseHelper helper;
    private SQLiteDatabase database;
    private GlobalVariable globalVariable;

    public SpotListFragment() {
    }

    public static SpotListFragment newInstance(String fragementName) {
        SpotListFragment fragment = new SpotListFragment();
        Bundle args = new Bundle();
        args.putString(FRAGMENT_NAME, fragementName);
        //args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mFragmentName = getArguments().getString(FRAGMENT_NAME);
            //mParam2 = getArguments().getString(ARG_PARAM2);
        }
        globalVariable = (GlobalVariable) getActivity().getApplicationContext();
        getActivity().registerReceiver(broadcastReceiver, new IntentFilter(GetSpotsNSort.BROADCAST_ACTION));

        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)        // 5 seconds, in milliseconds
                .setFastestInterval(FATEST_INTERVAL) // 1 second, in milliseconds
                .setSmallestDisplacement(DISPLACEMENT);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_spot_list, container, false);


        spotList_pageLayout = (LinearLayout) view.findViewById(R.id.spotList_pageLayout);
        spotList_textLayout = (LinearLayout) view.findViewById(R.id.spotList_textLayout);

        lastPage = (TextView) view.findViewById(R.id.lastpage_text);
        lastPage.setVisibility(View.INVISIBLE);
        lastPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(pageNo - 2);
            }
        });
        nextPage = (TextView) view.findViewById(R.id.nextpage_text);
        nextPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(pageNo);

            }
        });

        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        viewPager = (ViewPager) view.findViewById(R.id.spotList_viewpager);

        if (globalVariable.SpotDataSorted.isEmpty()) {
//            Log.e("3/23_SpotListFragment", "no sort");
            progressBar.setVisibility(View.VISIBLE);
            spotList_pageLayout.setVisibility(View.INVISIBLE);
            //viewPager.setAdapter(null);

        } else {
            count = globalVariable.SpotDataSorted.size();
            count = count / 20;
            if (count % 10 > 0) {
                pages = (count / 10) + 1;
            } else {
                pages = (count / 10);
            }



            //fragment(i) -> i代表第幾頁
            TextView textView = new TextView(getContext());
            textView.setText("/" + pages);
            textView.setTextColor((Color.parseColor("#000000")));
            number = new TextView(getContext());
            number.setText("1");
            number.setTextColor((Color.parseColor("#FF0088")));
            spotList_textLayout.addView(number);
            spotList_textLayout.addView(textView);
            //viewPager.setAdapter(new SpotListFragmentViewPagerAdapter(getChildFragmentManager(), pages));
            //viewPager.setOnPageChangeListener(new PageListener());
            //viewPager.setOffscreenPageLimit(1);
        }

        return view;
    }

    private class PageListener implements ViewPager.OnPageChangeListener {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        public void onPageSelected(int position) {
            pageNo = position + 1;
            if (pageNo == pages)
                nextPage.setVisibility(View.INVISIBLE);
            else nextPage.setVisibility(View.VISIBLE);
            if (pageNo == 1)
                lastPage.setVisibility(View.INVISIBLE);
            else lastPage.setVisibility(View.VISIBLE);
            minus = pageNo-1;
            String get = String.valueOf(position + 1);
            number.setText(get);
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    }

    @Override
    public void onResume() {
        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
        super.onResume();
    }

    @Override
    public void onStop() {
        // 移除Google API用戶端連線
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        //Log.e("3/23_SpotList", "onDestroyView");
        if (broadcastReceiver != null)
            getActivity().unregisterReceiver(broadcastReceiver);
        System.gc();
        super.onDestroyView();
    }

    @Override
    public void onLowMemory() {
        //Log.e("3/23_SpotList", "onLowMemory");
        System.gc();
        super.onLowMemory();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        if (isVisibleToUser) {
            //you are visible to user now - so set whatever you need
            //Log.e("3/23_SpotList", "setUserVisibleHint: Visible");
            if (viewPager != null && viewPager.getAdapter() == null && pages != 0) {
                if (adapter == null)
                    adapter = new SpotListFragmentViewPagerAdapter(getChildFragmentManager(), pages);
                viewPager.setOffscreenPageLimit(1);
                viewPager.setAdapter(adapter);
                viewPager.setOnPageChangeListener(new PageListener());
                adapter.notifyDataSetChanged();

            }
        }
        else {
            //you are no longer visible to the user so cleanup whatever you need
            //Log.e("3/23_SpotList", "setUserVisibleHint: not Visible");
            /*if (viewPager != null) {
                viewPager.removeAllViews();
                viewPager.removeOnPageChangeListener(new PageListener());
            }*/
            System.gc();
        }
        super.setUserVisibleHint(isVisibleToUser);
    }

    @Override
    public void onConnected(Bundle bundle) {
        // 已經連線到Google Services
        // 啟動位置更新服務
        // 位置資訊更新的時候，應用程式會自動呼叫LocationListener.onLocationChanged
        Log.i(TAG, "Location services connected.");

        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location == null) {
            LocationServices.FusedLocationApi.requestLocationUpdates
                    (mGoogleApiClient, mLocationRequest, (LocationListener) this);
        } else {
            //HandleNewLocation(location);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
//        Log.i(TAG, "Location services suspended. Please reconnect.");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // Google Services連線失敗
        // ConnectionResult參數是連線失敗的資訊
        int errorCode = connectionResult.getErrorCode();
        // 裝置沒有安裝Google Play服務
        if (errorCode == ConnectionResult.SERVICE_MISSING) {
            Toast.makeText(getActivity(), R.string.google_play_service_missing, Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public void onLocationChanged(Location location) {
        if (CurrentLocation != location) {
            //HandleNewLocation(CurrentLocation);
        }
    }

    private void HandleNewLocation(Location location) {
        Log.d(TAG, location.toString());
/*
        CurrentLocation = location;
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        // 設定目前位置的標記
        if (CurrentMarker == null) {
            // 移動地圖到目前的位置
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
            CurrentMarker = mMap.addMarker(new MarkerOptions().position(latLng).title("I am here!")
                    .icon(BitmapDescriptorFactory.fromBitmap(MarkerIcon)));
        } else {
            CurrentMarker.setPosition(latLng);
        }

        DataBaseHelper helper = new DataBaseHelper(getActivity());
        SQLiteDatabase database = helper.getWritableDatabase();
        Cursor location_cursor = database.query("location",
                new String[]{"CurrentLat", "CurrentLng"}, null, null, null, null, null);
        if (location_cursor != null) {
            if (location_cursor.getCount() == 0) {
                ContentValues cv = new ContentValues();
                cv.put("CurrentLat", location.getLatitude());
                cv.put("CurrentLng", location.getLongitude());
                long result = database.insert("location", null, cv);
                Log.d("3/10_新增位置", result + " = DB INSERT " + location.getLatitude() + " " + location.getLongitude());

            } else {
                ContentValues cv = new ContentValues();
                cv.put("CurrentLat", location.getLatitude());
                cv.put("CurrentLng", location.getLongitude());
                long result = database.update("location", cv, "_ID=1", null);
                Log.d("3/10_位置更新", result + " = DB INSERT " + location.getLatitude() + " " + location.getLongitude());
            }
            location_cursor.close();
        }
        database.close();
        helper.close();*/
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Update Your UI here..
            if (intent != null) {
                Boolean isSpotSorted = intent.getBooleanExtra("isSpoted", false);
                if (isSpotSorted) {
//                    Log.e("3/23_景點排序完畢", "Receive Broadcast");

                    count = globalVariable.SpotDataSorted.size();
                    count = count / 20;
                    if (count % 10 > 0) {
                        pages = (count / 10) + 1;
                    } else {
                        pages = (count / 10);
                    }

                    //fragment(i) -> i代表第幾頁
                    TextView textView = new TextView(getContext());
                    textView.setText("/" + pages);
                    textView.setTextColor((Color.parseColor("#000000")));
                    number = new TextView(getContext());
                    number.setText("1");
                    number.setTextColor((Color.parseColor("#FF0088")));
                    spotList_textLayout.addView(number);
                    spotList_textLayout.addView(textView);

                    //adapter = new SpotListFragmentViewPagerAdapter(getChildFragmentManager(), pages);
                    //viewPager.setAdapter(adapter);
                    //viewPager.setOnPageChangeListener(new PageListener());
                    //viewPager.setOffscreenPageLimit(1);
                    //adapter.notifyDataSetChanged();

                    spotList_pageLayout.setVisibility(View.VISIBLE);

                    progressBar.setVisibility(View.INVISIBLE);
                }
            }
        }
    };
}
