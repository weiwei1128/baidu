package com.flyingtravel;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.flyingtravel.Utility.DataBaseHelper;
import com.flyingtravel.Utility.GetSpotsNSort;
import com.flyingtravel.Utility.GlobalVariable;
import com.flyingtravel.Utility.LoadApiService;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.ArrayList;

public class SpotMapFragment extends Fragment {

    public static final String TAG = SpotMapFragment.class.getSimpleName();
    private static final String FRAGMENT_NAME = "FRAGMENT_NAME";
    //private static final String ARG_PARAM2 = "param2";
    private String mFragmentName;
    //private String mParam2;

    private MapView mapView;
    private BaiduMap mBaiduMap;
    private LocationClient mLocationClient = null;
    private MyLocationListenner myListener = new MyLocationListenner();

    private GlobalVariable globalVariable;

    private ProgressDialog mProgressDialog;

    private Boolean isFirstLoc = true;
    private BDLocation CurrentLocation;
    private Marker CurrentMarker;
    private BitmapDescriptor MarkerIcon;

//    ArrayList<MarkerOptions> MarkerOptionsArray = new ArrayList<MarkerOptions>();

    /**GA**/
    public static Tracker tracker;

    public SpotMapFragment() {
        // Required empty public constructor
    }

    public static SpotMapFragment newInstance(String fragementName) {
        SpotMapFragment fragment = new SpotMapFragment();
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

        globalVariable = (GlobalVariable) getActivity().getApplication();
        /**GA**/
        tracker = globalVariable.getDefaultTracker();
        /**GA**/
        getActivity().registerReceiver(broadcastReceiver, new IntentFilter(LoadApiService.BROADCAST_ACTION));
        getActivity().registerReceiver(broadcastReceiver_SpotSort, new IntentFilter(GetSpotsNSort.BROADCAST_ACTION));

        mProgressDialog = new ProgressDialog(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        //注意该方法要再setContentView方法之前实现
        SDKInitializer.initialize(getActivity().getApplicationContext());

        View rootView = inflater.inflate(R.layout.fragment_spot_map, container, false);

//        Log.e("5/15_SpotMap", "onCreateView");

        mapView = (MapView) rootView.findViewById(R.id.SpotMap);
        mapView.onResume();         // needed to get the map to display immediately

        // Gets to GoogleMap from the MapView and does initialization stuff
        mBaiduMap = mapView.getMap();
        if (mBaiduMap != null) {
            mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
            mBaiduMap.setMyLocationEnabled(true);
            mBaiduMap.getUiSettings().setCompassEnabled(true);
            mBaiduMap.getUiSettings().setAllGesturesEnabled(true);
        }

        MarkerIcon = BitmapDescriptorFactory.fromBitmap(decodeBitmapFromResource(getResources(), R.drawable.location3, 10, 18));

        // 定位初始化
        mLocationClient = new LocationClient(getContext());
        mLocationClient.registerLocationListener(myListener);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);// 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(3000);
        mLocationClient.setLocOption(option);
        mLocationClient.start();

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!globalVariable.isAPILoaded) {
//            Log.e("5/15_", "API is not ready");
            mProgressDialog.setMessage(getContext().getResources().getString(R.string.spotLoading_text));
            mProgressDialog.show();
        } else {
            if (CurrentLocation != null) {
//                Log.e("5/15_onActivityCreated", "事先Sort");
                if (globalVariable.isAPILoaded && globalVariable.SpotDataSorted.isEmpty()) {
                    new GetSpotsNSort(getActivity(), CurrentLocation.getLatitude(),
                            CurrentLocation.getLongitude()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            }
            if (globalVariable.MarkerOptionsArray.isEmpty()) {
//                Log.e("5/15_", "Marker is not ready");
                mProgressDialog.setMessage(getContext().getResources().getString(R.string.spotMarkerLoading_text));
                mProgressDialog.show();
            }
        }
    }

    @Override
    public void onResume() {
//        Log.e("5/15_SpotMap", "onResume");
        mapView.onResume();
        mBaiduMap = mapView.getMap();

        if (!mLocationClient.isStarted())
            mLocationClient.start();

        if (MarkerIcon == null) {
            MarkerIcon = BitmapDescriptorFactory.fromBitmap(decodeBitmapFromResource(getResources(), R.drawable.location3, 10, 18));
            if (!globalVariable.MarkerOptionsArray.isEmpty()) {
                int MarkerCount = globalVariable.MarkerOptionsArray.size();
                for (int i = 0; i < MarkerCount / 12; i++) {
                    mBaiduMap.addOverlay(globalVariable.MarkerOptionsArray.get(i).icon(MarkerIcon));
                }
            }
        } else {
            if (!globalVariable.MarkerOptionsArray.isEmpty()) {
                int MarkerCount = globalVariable.MarkerOptionsArray.size();
                for (int i = 0; i < MarkerCount / 12; i++) {
                    mBaiduMap.addOverlay(globalVariable.MarkerOptionsArray.get(i).icon(MarkerIcon));
                }
            }
        }
        super.onResume();
        /**GA**/
        tracker.setScreenName("周邊景點地圖");
        tracker.send(new HitBuilders.ScreenViewBuilder().build());
        /**GA**/
    }

    @Override
    public void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroyView() {
//        Log.e("3/23_SpotMap", "onDestroyView");
        if (mLocationClient.isStarted())
            mLocationClient.stop();
        mBaiduMap.setMyLocationEnabled(false);
        mapView.onDestroy();
        mapView = null;
        if (broadcastReceiver != null)
            getActivity().unregisterReceiver(broadcastReceiver);
        if (broadcastReceiver_SpotSort != null)
            getActivity().unregisterReceiver(broadcastReceiver_SpotSort);
//        MarkerIcon.recycle();
        System.gc();
        super.onDestroyView();
    }

    @Override
    public void onLowMemory() {
        System.gc();
        super.onLowMemory();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            //you are visible to user now - so set whatever you need
//            Log.e("5/15_SpotMap", "setUserVisibleHint: Visible");
            if (mBaiduMap != null) {
                if (MarkerIcon == null) {
                    MarkerIcon = BitmapDescriptorFactory.fromBitmap(decodeBitmapFromResource(getResources(), R.drawable.location3, 10, 18));
                    if (!globalVariable.MarkerOptionsArray.isEmpty()) {
                        int MarkerCount = globalVariable.MarkerOptionsArray.size();
                        for (int i = 0; i < MarkerCount / 12; i++) {
                            mBaiduMap.addOverlay(globalVariable.MarkerOptionsArray.get(i).icon(MarkerIcon));
                        }
                    }
                }
            }
        } else {
            //you are no longer visible to the user so cleanup whatever you need
            //Log.e("3/23_SpotMap", "setUserVisibleHint: not Visible");

            if (MarkerIcon != null) {
                MarkerIcon.getBitmap().recycle();
                MarkerIcon = null;
            }
            System.gc();
        }
    }

    /**
     * 定位SDK监听函数
     */
    public class MyLocationListenner implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            // map view 销毁后不在处理新接收的位置
            if (location == null || mapView == null)
                return;

            if (CurrentLocation != location) {
                CurrentLocation = location;

                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                MyLocationData locData = new MyLocationData.Builder()
                        .accuracy(location.getRadius())
                        // 此处设置开发者获取到的方向信息，顺时针0-360
                        // 这里的方向需要用户通过传感器自定获取并设置
                        .direction(100).latitude(location.getLatitude())
                        .longitude(location.getLongitude()).build();
                mBaiduMap.setMyLocationData(locData);

                if (isFirstLoc) {
                    isFirstLoc = false;
                    if (globalVariable.isAPILoaded && globalVariable.SpotDataSorted.isEmpty()) {
                        new GetSpotsNSort(getActivity(), CurrentLocation.getLatitude(),
                                CurrentLocation.getLongitude()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }

                    MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newLatLngZoom(latLng, 16);
                    mBaiduMap.animateMapStatus(mMapStatusUpdate);
                }

                // 設定目前位置的標記
                if (CurrentMarker == null) {
                    //构建MarkerOption，用于在地图上添加Marker
                    OverlayOptions option = new MarkerOptions().position(latLng).title("I am here!").icon(MarkerIcon);
                    //在地图上添加Marker，并显示
                    CurrentMarker = (Marker) mBaiduMap.addOverlay(option);
                } else {
                    CurrentMarker.setPosition(latLng);
                }

                DataBaseHelper helper = DataBaseHelper.getmInstance(getActivity());
                SQLiteDatabase database = helper.getWritableDatabase();
                Cursor location_cursor = database.query("location",
                        new String[]{"CurrentLat", "CurrentLng"}, null, null, null, null, null);
                if (location_cursor != null) {
                    if (location_cursor.getCount() == 0) {
                        ContentValues cv = new ContentValues();
                        cv.put("CurrentLat", location.getLatitude());
                        cv.put("CurrentLng", location.getLongitude());
                        long result = database.insert("location", null, cv);
                        //Log.d("3/10_新增位置", result + " = DB INSERT " + location.getLatitude() + " " + location.getLongitude());

                    } else {
                        ContentValues cv = new ContentValues();
                        cv.put("CurrentLat", location.getLatitude());
                        cv.put("CurrentLng", location.getLongitude());
                        long result = database.update("location", cv, "_ID=1", null);
                        //Log.d("3/10_位置更新", result + " = DB INSERT " + location.getLatitude() + " " + location.getLongitude());
                    }
                    location_cursor.close();
                }
            }
        }
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Update Your UI here..
            if (intent != null) {
                globalVariable.isAPILoaded = intent.getBooleanExtra("isAPILoaded", false);
                if (globalVariable.isAPILoaded) {
                    //Log.e("3/23_", "Receive Broadcast: APILoaded");
                    if (CurrentLocation != null) {
                        //Log.e("3/23_broadcast", "事先Sort");
                        if (globalVariable.isAPILoaded && globalVariable.SpotDataSorted.isEmpty()) {
                            new GetSpotsNSort(getActivity(), CurrentLocation.getLatitude(),
                                    CurrentLocation.getLongitude()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        }
                    }
                    if (globalVariable.MarkerOptionsArray.isEmpty()) {
                        mProgressDialog.setMessage(getContext().getResources().getString(R.string.spotMarkerLoading_text));
                    } else {
                        if (mProgressDialog.isShowing()) {
                            mProgressDialog.dismiss();
                        }
                    }
                }
            }
        }
    };

    private BroadcastReceiver broadcastReceiver_SpotSort = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Update Your UI here..
            if (intent != null) {
                Boolean isSpotSorted = intent.getBooleanExtra("isSorted", false);
                if (isSpotSorted) {
                    //Log.e("3/23_景點排序完畢", "Receive Broadcast");
                    new GetMarkerInfo(getActivity()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            }
        }
    };

    public static Bitmap decodeBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    public class GetMarkerInfo extends AsyncTask<Void, Void, ArrayList<MarkerOptions>> {
        public static final String TAG = "GetMarkerInfo";
        Context mcontext;

        public GetMarkerInfo(Context context) {
            this.mcontext = context;
        }

        @Override
        protected void onPreExecute() {
            //Log.d("3/23_GetMarkerInfo", "MarkerOption載入中...");
            super.onPreExecute();
        }

        @Override
        protected ArrayList<MarkerOptions> doInBackground(Void... params) {
            ArrayList<MarkerOptions> markerOptionsArray = new ArrayList<MarkerOptions>();
            //get Marker Info
            if (!globalVariable.SpotDataSorted.isEmpty()) {
                Integer SpotCount = globalVariable.SpotDataSorted.size();
                for (int i = 0; i < SpotCount; i++) {
                    String Name = globalVariable.SpotDataSorted.get(i).getName();
                    Double Latitude = globalVariable.SpotDataSorted.get(i).getLatitude();
                    Double Longitude = globalVariable.SpotDataSorted.get(i).getLongitude();
                    LatLng latLng = new LatLng(Latitude, Longitude);
                    String OpenTime = globalVariable.SpotDataSorted.get(i).getOpenTime();
                    MarkerOptions markerOpt = new MarkerOptions();
                    markerOpt.position(latLng).title(Name);//.snippet(OpenTime);

                    markerOptionsArray.add(markerOpt);
                }
            } else {
                DataBaseHelper helper = DataBaseHelper.getmInstance(getActivity());
                SQLiteDatabase database = helper.getWritableDatabase();
                Cursor spotDataRaw_cursor = database.query("spotDataRaw", new String[]{"spotName", "spotAdd",
                                "spotLat", "spotLng", "picture1", "picture2", "picture3",
                                "openTime", "ticketInfo", "infoDetail"},
                        null, null, null, null, null);
                if (spotDataRaw_cursor != null) {
                    while (spotDataRaw_cursor.moveToNext()) {
                        String Name = spotDataRaw_cursor.getString(1);
                        Double Latitude = spotDataRaw_cursor.getDouble(3);
                        Double Longitude = spotDataRaw_cursor.getDouble(4);
                        LatLng latLng = new LatLng(Latitude, Longitude);
                        String OpenTime = spotDataRaw_cursor.getString(8);
                        MarkerOptions markerOpt = new MarkerOptions();
                        markerOpt.position(latLng).title(Name);//.snippet(OpenTime);

                        markerOptionsArray.add(markerOpt);
                    }
                    spotDataRaw_cursor.close();
                }
            }
            return markerOptionsArray;
        }

        protected void onPostExecute(ArrayList<MarkerOptions> markerOptionsArray) {
            if (globalVariable.MarkerOptionsArray.isEmpty()) {
                globalVariable.MarkerOptionsArray = markerOptionsArray;
            }
            int MarkerCount = globalVariable.MarkerOptionsArray.size();//globalVariable.MarkerOptionsArray.size();
            for (int i = 0; i < MarkerCount / 12; i++) {
                mBaiduMap.addOverlay(markerOptionsArray.get(i).icon(MarkerIcon));
            }
            //Log.d("3/23_MarkerCount", MarkerCount+" MarkerCount/12: "+MarkerCount/12);
            mProgressDialog.dismiss();
            super.onPostExecute(markerOptionsArray);
        }
    }
}
