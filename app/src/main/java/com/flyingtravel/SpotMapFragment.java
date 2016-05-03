package com.flyingtravel;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.flyingtravel.Utility.GetSpotsNSort;
import com.flyingtravel.Utility.GlobalVariable;

public class SpotMapFragment extends Fragment {

    public static final String TAG = SpotMapFragment.class.getSimpleName();
    private static final String FRAGMENT_NAME = "FRAGMENT_NAME";
    //private static final String ARG_PARAM2 = "param2";
    private String mFragmentName;
    //private String mParam2;

    private MapView mapView;
    private BaiduMap mBaiduMap;
    private LocationClient mLocationClient = null;
    private BDLocationListener myListener = new MyLocationListener();

    private Location CurrentLocation;
    private BitmapDescriptor CurrentMarker;
    boolean isFirstLoc = true; // 是否首次定位

    private Bitmap MarkerIcon;

    private GlobalVariable globalVariable;
    private ProgressDialog mProgressDialog;

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

        globalVariable = (GlobalVariable) getActivity().getApplicationContext();
        //getActivity().registerReceiver(broadcastReceiver, new IntentFilter(LoadApiService.BROADCAST_ACTION));
        //getActivity().registerReceiver(broadcastReceiver_SpotSort, new IntentFilter(GetSpotsNSort.BROADCAST_ACTION));

        mProgressDialog = new ProgressDialog(getActivity());
        MarkerIcon = decodeBitmapFromResource(getResources(), R.drawable.location3, 10, 18);

        mLocationClient = new LocationClient(getActivity());  //声明LocationClient类
        mLocationClient.registerLocationListener(myListener); //注册监听函数
        initLocation();
        mLocationClient.start();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_spot_map, container, false);

        mapView = (MapView) rootView.findViewById(R.id.SpotMap);
        mBaiduMap = mapView.getMap();
        if (mBaiduMap != null) {
            mBaiduMap.setMyLocationEnabled(true);
//            mBaiduMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
//            mBaiduMap.getUiSettings().setCompassEnabled(true);
//            mBaiduMap.getUiSettings().setAllGesturesEnabled(true);
        }

        // Needs to call MapsInitializer before doing any CameraUpdateFactory calls
        //MapsInitializer.initialize(this.getActivity());

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
/*
        if (!globalVariable.isAPILoaded) {
//            Log.e("3/23_", "API is not ready");
            mProgressDialog.setMessage(getContext().getResources().getString(R.string.spotLoading_text));
            mProgressDialog.show();
        } else {
            if (CurrentLocation != null) {
//                Log.e("3/23_onActivityCreated", "事先Sort");
                if (globalVariable.isAPILoaded && globalVariable.SpotDataSorted.isEmpty()) {
                    new GetSpotsNSort(getActivity(), CurrentLocation.getLatitude(),
                            CurrentLocation.getLongitude()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            }
            if (globalVariable.MarkerOptionsArray.isEmpty()) {
//                Log.e("3/23_", "Marker is not ready");
                mProgressDialog.setMessage(getContext().getResources().getString(R.string.spotMarkerLoading_text));
                mProgressDialog.show();
                // Get Marker Info
                //new GetMarkerInfo(getActivity()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }*/
    }

    @Override
    public void onResume() {
        mapView.onResume();
/*        if (MarkerIcon.isRecycled()) {
            MarkerIcon = decodeBitmapFromResource(getResources(), R.drawable.location3, 10, 18);
            if (!globalVariable.MarkerOptionsArray.isEmpty()) {
                int MarkerCount = globalVariable.MarkerOptionsArray.size();
                for (int i = 0; i < MarkerCount/12; i++) {
//                    mMap.addMarker(globalVariable.MarkerOptionsArray.get(i)
//                            .icon(BitmapDescriptorFactory.fromBitmap(MarkerIcon)));
                }
            }
        } else {
            if (!globalVariable.MarkerOptionsArray.isEmpty()) {
                int MarkerCount = globalVariable.MarkerOptionsArray.size();
                for (int i = 0; i < MarkerCount/12; i++) {
//                    mMap.addMarker(globalVariable.MarkerOptionsArray.get(i)
//                            .icon(BitmapDescriptorFactory.fromBitmap(MarkerIcon)));
                }
            }
        }*/
        super.onResume();
    }

    @Override
    public void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroyView() {
//        Log.e("3/23_SpotMap", "onDestroyView");
        // 退出时销毁定位
        mLocationClient.stop();
        // 关闭定位图层
        mBaiduMap.setMyLocationEnabled(false);
        mapView.onDestroy();
        mapView = null;
        if (broadcastReceiver != null)
            getActivity().unregisterReceiver(broadcastReceiver);
        if (broadcastReceiver_SpotSort != null)
            getActivity().unregisterReceiver(broadcastReceiver_SpotSort);
        MarkerIcon.recycle();
        System.gc();
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
//        Log.e("3/23_SpotMap", "onLowMemory");
        MarkerIcon.recycle();
        System.gc();
        super.onLowMemory();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            //you are visible to user now - so set whatever you need
            //Log.e("3/23_SpotMap", "setUserVisibleHint: Visible");
/*            if (MarkerIcon.isRecycled()) {
                MarkerIcon = decodeBitmapFromResource(getResources(), R.drawable.location3, 10, 18);
                if (!globalVariable.MarkerOptionsArray.isEmpty()) {
                    int MarkerCount = globalVariable.MarkerOptionsArray.size();
                    for (int i = 0; i < MarkerCount/12; i++) {
                        mMap.addMarker(globalVariable.MarkerOptionsArray.get(i)
                                .icon(BitmapDescriptorFactory.fromBitmap(MarkerIcon)));
                    }
                }
            }*/
        }
        else {
            //you are no longer visible to the user so cleanup whatever you need
            //Log.e("3/23_SpotMap", "setUserVisibleHint: not Visible");
            /*if (MarkerIcon != null) {
                MarkerIcon.recycle();
            }*/
            System.gc();
        }
    }

    private void initLocation(){
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        int span=1000;
        option.setScanSpan(span);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        //option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        //option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        //option.setIgnoreKillProcess(false);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤gps仿真结果，默认需要
        mLocationClient.setLocOption(option);
    }

    public class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            if (location == null || mapView == null) {
                return;
            }
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                            // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(100).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            mBaiduMap.setMyLocationData(locData);
            if (isFirstLoc) {
                isFirstLoc = false;
                LatLng ll = new LatLng(location.getLatitude(),
                        location.getLongitude());
                MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(ll).zoom(18.0f);
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory
                        .newMapStatus(builder.build()));
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
                        // Get Marker Info
                        mProgressDialog.setMessage(getContext().getResources().getString(R.string.spotMarkerLoading_text));
                        //new GetMarkerInfo(getActivity()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
                Boolean isSpotSorted = intent.getBooleanExtra("isSpoted", false);
                if (isSpotSorted) {
                    //Log.e("3/23_景點排序完畢", "Receive Broadcast");
//                    new GetMarkerInfo(getActivity()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
/*
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
            ArrayList<MarkerOptions> MarkerOptionsArray = new ArrayList<MarkerOptions>();
            //get Marker Info
            if (!globalVariable.SpotDataSorted.isEmpty()) {
                Integer SpotCount = globalVariable.SpotDataSorted.size();
                for (int i = 0; i < SpotCount; i++) {
                    String Name = globalVariable.SpotDataSorted.get(i).getName();
                    Double Latitude = globalVariable.SpotDataSorted.get(i).getLatitude();
                    Double Longitude = globalVariable.SpotDataSorted.get(i).getLongitude();
                    LatLng latLng = new LatLng(Latitude,Longitude);
                    String OpenTime = globalVariable.SpotDataSorted.get(i).getOpenTime();
                    MarkerOptions markerOpt = new MarkerOptions();
                    markerOpt.position(latLng).title(Name).snippet(OpenTime);

                    MarkerOptionsArray.add(markerOpt);
                }
            } else {
                DataBaseHelper helper = DataBaseHelper.getmInstance(getActivity());
                SQLiteDatabase database = helper.getWritableDatabase();
                Cursor spotDataRaw_cursor = database.query("spotDataRaw", new String[]{"spotName", "spotAdd",
                                "spotLat", "spotLng", "picture1", "picture2","picture3",
                                "openTime", "ticketInfo", "infoDetail"},
                        null, null, null, null, null);
                if (spotDataRaw_cursor != null) {
                    while (spotDataRaw_cursor.moveToNext()) {
                        String Name = spotDataRaw_cursor.getString(1);
                        Double Latitude = spotDataRaw_cursor.getDouble(3);
                        Double Longitude = spotDataRaw_cursor.getDouble(4);
                        LatLng latLng = new LatLng(Latitude,Longitude);
                        String OpenTime = spotDataRaw_cursor.getString(8);
                        MarkerOptions markerOpt = new MarkerOptions();
                        markerOpt.position(latLng).title(Name).snippet(OpenTime);

                        MarkerOptionsArray.add(markerOpt);
                    }
                    spotDataRaw_cursor.close();
                }
            }
            return MarkerOptionsArray;
        }

        protected void onPostExecute(ArrayList<MarkerOptions> markerOptionsArray) {
            if (globalVariable.MarkerOptionsArray.isEmpty()) {
                globalVariable.MarkerOptionsArray = markerOptionsArray;
            }
            int MarkerCount = globalVariable.MarkerOptionsArray.size();
            for (int i = 0; i < MarkerCount/12; i++) {
                mBaiduMap.addMarker(globalVariable.MarkerOptionsArray.get(i)
                        .icon(BitmapDescriptorFactory.fromBitmap(MarkerIcon)));
            }
            //Log.d("3/23_MarkerCount", MarkerCount+" MarkerCount/12: "+MarkerCount/12);
            mProgressDialog.dismiss();

            for (MarkerOptions markerOptions : globalVariable.MarkerOptionsArray) {
                mMap.addMarker(markerOptions);
            }
            super.onPostExecute(markerOptionsArray);
        }
    }*/
}
