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
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.flyingtravel.Utility.DataBaseHelper;
import com.flyingtravel.Utility.GetSpotsNSort;
import com.flyingtravel.Utility.GlobalVariable;
import com.flyingtravel.Utility.LoadApiService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class SpotMapFragment extends Fragment implements
        com.google.android.gms.location.LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    public static final String TAG = SpotMapFragment.class.getSimpleName();
    private static final String FRAGMENT_NAME = "FRAGMENT_NAME";
    //private static final String ARG_PARAM2 = "param2";
    private String mFragmentName;
    //private String mParam2;

    private MapView mapView;
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    // Location updates intervals in sec
    private static int UPDATE_INTERVAL = 5000; // 5 sec
    private static int FATEST_INTERVAL = 1000; // 1 sec
    private static int DISPLACEMENT = 3;       // 5 meters

    private GlobalVariable globalVariable;

    private ProgressDialog mProgressDialog;

    private Location CurrentLocation;
    private Marker CurrentMarker;

    private Bitmap MarkerIcon;

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
        getActivity().registerReceiver(broadcastReceiver, new IntentFilter(LoadApiService.BROADCAST_ACTION));
        getActivity().registerReceiver(broadcastReceiver_SpotSort, new IntentFilter(GetSpotsNSort.BROADCAST_ACTION));

        mProgressDialog = new ProgressDialog(getActivity());
        MarkerIcon = decodeBitmapFromResource(getResources(), R.drawable.location3, 10, 18);

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
        View rootView = inflater.inflate(R.layout.fragment_spot_map, container, false);

        mapView = (MapView) rootView.findViewById(R.id.SpotMap);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();         // needed to get the map to display immediately

        // Gets to GoogleMap from the MapView and does initialization stuff
        mMap = mapView.getMap();
        if (mMap != null) {
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setCompassEnabled(true);
            mMap.getUiSettings().setAllGesturesEnabled(true);
            mMap.getUiSettings().setMapToolbarEnabled(false);
        }

        // Needs to call MapsInitializer before doing any CameraUpdateFactory calls
        MapsInitializer.initialize(this.getActivity());

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
        }
    }

    @Override
    public void onResume() {
        mapView.onResume();
        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
        if (MarkerIcon.isRecycled()) {
            MarkerIcon = decodeBitmapFromResource(getResources(), R.drawable.location3, 10, 18);
            if (!globalVariable.MarkerOptionsArray.isEmpty()) {
                int MarkerCount = globalVariable.MarkerOptionsArray.size();
                for (int i = 0; i < MarkerCount/12; i++) {
                    mMap.addMarker(globalVariable.MarkerOptionsArray.get(i)
                            .icon(BitmapDescriptorFactory.fromBitmap(MarkerIcon)));
                }
            }
        } else {
            if (!globalVariable.MarkerOptionsArray.isEmpty()) {
                int MarkerCount = globalVariable.MarkerOptionsArray.size();
                for (int i = 0; i < MarkerCount/12; i++) {
                    mMap.addMarker(globalVariable.MarkerOptionsArray.get(i)
                            .icon(BitmapDescriptorFactory.fromBitmap(MarkerIcon)));
                }
            }
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        mapView.onPause();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates (mGoogleApiClient, this);
        }
        super.onPause();
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
//        Log.e("3/23_SpotMap", "onDestroyView");
        mapView.onDestroy();
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
        mapView.onLowMemory();
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
            if (MarkerIcon.isRecycled()) {
                MarkerIcon = decodeBitmapFromResource(getResources(), R.drawable.location3, 10, 18);
                if (!globalVariable.MarkerOptionsArray.isEmpty()) {
                    int MarkerCount = globalVariable.MarkerOptionsArray.size();
                    for (int i = 0; i < MarkerCount/12; i++) {
                        mMap.addMarker(globalVariable.MarkerOptionsArray.get(i)
                                .icon(BitmapDescriptorFactory.fromBitmap(MarkerIcon)));
                    }
                }
            }
        }
        else {
            //you are no longer visible to the user so cleanup whatever you need
            //Log.e("3/23_SpotMap", "setUserVisibleHint: not Visible");
            if (MarkerIcon != null) {
                MarkerIcon.recycle();
            }
            System.gc();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        // 已經連線到Google Services
        // 啟動位置更新服務
        // 位置資訊更新的時候，應用程式會自動呼叫LocationListener.onLocationChanged
//        Log.i(TAG, "Location services connected.");

        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location == null) {
            LocationServices.FusedLocationApi.requestLocationUpdates
                    (mGoogleApiClient, mLocationRequest, (LocationListener) this);
        } else {
            HandleNewLocation(location);
            if (globalVariable.isAPILoaded && globalVariable.SpotDataSorted.isEmpty()) {
//                Log.e("3/23_Connected", "事先Sort");
                new GetSpotsNSort(getActivity(), CurrentLocation.getLatitude(),
                        CurrentLocation.getLongitude()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Location services suspended. Please reconnect.");
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
            HandleNewLocation(CurrentLocation);
        }
    }

    private void HandleNewLocation(Location location) {
        Log.d(TAG, location.toString());

        CurrentLocation = location;
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        // 設定目前位置的標記
        if (CurrentMarker == null) {
            CurrentMarker = mMap.addMarker(new MarkerOptions().position(latLng).title("I am here!")
                    .icon(BitmapDescriptorFactory.fromBitmap(MarkerIcon)));
        } else {
            CurrentMarker.setPosition(latLng);
        }

        // 移動地圖到目前的位置
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));

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
                mMap.addMarker(globalVariable.MarkerOptionsArray.get(i)
                        .icon(BitmapDescriptorFactory.fromBitmap(MarkerIcon)));
            }
            //Log.d("3/23_MarkerCount", MarkerCount+" MarkerCount/12: "+MarkerCount/12);
            mProgressDialog.dismiss();
            /*
            for (MarkerOptions markerOptions : globalVariable.MarkerOptionsArray) {
                mMap.addMarker(markerOptions);
            }*/
            super.onPostExecute(markerOptionsArray);
        }
    }
}
