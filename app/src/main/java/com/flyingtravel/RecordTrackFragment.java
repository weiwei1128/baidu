
package com.flyingtravel;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.location.Location;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.flyingtravel.Utility.DataBaseHelper;
import com.flyingtravel.Utility.Functions;
import com.flyingtravel.Utility.TrackRouteService;
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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RecordTrackFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link RecordTrackFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RecordTrackFragment extends Fragment implements
        com.google.android.gms.location.LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    public static final String TAG = RecordTrackFragment.class.getSimpleName();
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

    //private GlobalVariable globalVariable;

    private Location CurrentLocation;

    private LatLng CurrentLatlng;
    private Marker CurrentMarker;

    private Bitmap MarkerIcon;
    private ArrayList<LatLng> TraceRoute;

    private Dialog spotDialog;
    private ScrollView dialog_scrollview;
    private LinearLayout record_start_layout, record_spot_layout;
    private LinearLayout dialog_choose_layout, dialog_confirm_layout, title_layout, content_layout;
    private RelativeLayout dialog_relativeLayout;
    private ImageView record_start_img, record_spot_img;
    private ImageView dialog_img, write, camera, leave;
    private TextView record_start_text, record_spot_text;
    private TextView dialog_header_text, title_textView, title_confirmTextView, content_textView;
    private EditText title_editText, content_editText;

    private Integer RoutesCounter = 1;
    private Integer Track_no = 1;
    private Integer record_status = 0;
    private Long tempSpent = 0L;

    final int REQUEST_CAMERA = 99;
    final int SELECT_FILE = 98;
    final Long[] starttime = new Long[1];
    //----for upload image//
    Bitmap memo_img;
    Uri imageUri = null;
    long inDB = 0;

    public static final String TIMER_TO_SERVICE = "com.example.tracking.restartcount";
    public static final String TRACK_TO_SERVICE = "com.example.tracking.restarttrack";

    public static ProgressDialog mProgressDialog;

    //private OnFragmentInteractionListener mListener;

    public RecordTrackFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param fragementName Parameter 1.
     * @return A new instance of fragment RecordTrackFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RecordTrackFragment newInstance(String fragementName) {
        RecordTrackFragment fragment = new RecordTrackFragment();
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

        //globalVariable = (GlobalVariable) getActivity().getApplicationContext();
        getActivity().registerReceiver(broadcastReceiver, new IntentFilter(TrackRouteService.BROADCAST_ACTION));
        getActivity().registerReceiver(broadcastReceiver_timer, new IntentFilter(TrackRouteService.BROADCAST_ACTION_TIMER));

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
        View rootView = inflater.inflate(R.layout.fragment_record_track, container, false);

        mapView = (MapView) rootView.findViewById(R.id.TrackMap);
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

        record_start_layout = (LinearLayout) rootView.findViewById(R.id.record_start_layout);
        record_start_img = (ImageView) rootView.findViewById(R.id.record_start_img);
        record_start_text = (TextView) rootView.findViewById(R.id.record_start_text);

        record_spot_layout = (LinearLayout) rootView.findViewById(R.id.record_spot_layout);
        record_spot_img = (ImageView) rootView.findViewById(R.id.record_spot_img);
        record_spot_text = (TextView) rootView.findViewById(R.id.record_spot_text);

        spotDialog = new Dialog(getActivity());
        spotDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        spotDialog.setContentView(R.layout.record_diary_dialog);

        dialog_choose_layout = (LinearLayout) spotDialog.findViewById(R.id.dialog_choose_layout);
        dialog_header_text = (TextView) spotDialog.findViewById(R.id.dialog_header_text);
        title_layout = (LinearLayout) spotDialog.findViewById(R.id.title_layout);
        title_textView = (TextView) spotDialog.findViewById(R.id.title_TextView);
        title_editText = (EditText) spotDialog.findViewById(R.id.title_editText);
        title_confirmTextView = (TextView) spotDialog.findViewById(R.id.title_confirmTextView);

        dialog_scrollview = (ScrollView) spotDialog.findViewById(R.id.dialog_scrollview);
        dialog_relativeLayout = (RelativeLayout) spotDialog.findViewById(R.id.dialog_relativeLayout);
        dialog_img = (ImageView) spotDialog.findViewById(R.id.dialog_img);
        content_layout = (LinearLayout) spotDialog.findViewById(R.id.content_layout);
        content_textView = (TextView) spotDialog.findViewById(R.id.content_TextView);
        content_editText = (EditText) spotDialog.findViewById(R.id.content_editText);

        dialog_confirm_layout = (LinearLayout) spotDialog.findViewById(R.id.dialog_confirm_layout);
        write = (ImageView) spotDialog.findViewById(R.id.dialog_write_img);
        camera = (ImageView) spotDialog.findViewById(R.id.dialog_camera_img);
        leave = (ImageView) spotDialog.findViewById(R.id.dialog_leave_img);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        RecordActivity.record_completeImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // reset內容
                title_editText.setText("");
                // 輸入這趟旅程的標題
                dialog_header_text.setText(getContext().getResources().getString(R.string.recordTitleInput_text));
                title_layout.setVisibility(View.VISIBLE);
                RelativeLayout.LayoutParams otelParams = new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                otelParams.addRule(RelativeLayout.BELOW, R.id.dialog_header_text);
                title_layout.setLayoutParams(otelParams);

                // 隱藏View
                dialog_scrollview.setVisibility(View.INVISIBLE);
                RelativeLayout.LayoutParams otelParams1 = new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, 0);
                otelParams1.addRule(RelativeLayout.BELOW, R.id.dialog_header_text);
                dialog_scrollview.setLayoutParams(otelParams1);

                dialog_choose_layout.setVisibility(View.INVISIBLE);
                dialog_confirm_layout.setVisibility(View.INVISIBLE);
                // 隱藏View

                // 按下確認
                title_confirmTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (title_editText.getText().equals("")) {
                            Toast.makeText(getActivity(), getContext().getResources().getString(R.string.emptyInput_text), Toast.LENGTH_SHORT).show();
                        } else {
                            mProgressDialog.setMessage(getContext().getResources().getString(R.string.handling_text));
                            mProgressDialog.setCancelable(false);
                            mProgressDialog.show();

                            Track_no = 1;
                            RoutesCounter++;
                            RecordActivity.time_text.setVisibility(View.INVISIBLE);
                            RecordActivity.record_completeImg.setVisibility(View.INVISIBLE);

                            record_start_layout.setBackgroundColor(Color.parseColor("#FFFFFF"));
                            record_start_text.setTextColor(Color.parseColor("#555555"));
                            record_start_text.setText(getContext().getResources().getString(R.string.startRecord_text));
                            record_start_img.setImageResource(R.drawable.ic_play_light);
                            record_status = 0;

                            if (Functions.isMyServiceRunning(getActivity(), TrackRouteService.class)) {
                                Intent intent_Trace = new Intent(TRACK_TO_SERVICE);
                                intent_Trace.putExtra("record_status", 0);
                                intent_Trace.putExtra("track_title", title_editText.getText().toString());
                                getActivity().sendBroadcast(intent_Trace);
                            }
                            spotDialog.dismiss();
                        }
                    }
                });
                spotDialog.show();
            }
        });

        record_start_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (record_status == 1) {
                    record_start_layout.setBackgroundColor(Color.parseColor("#FFFFFF"));
                    record_start_text.setTextColor(Color.parseColor("#555555"));
                    record_start_text.setText(getContext().getResources().getString(R.string.startRecord_text));
                    record_start_img.setImageResource(R.drawable.ic_play_light);
                    record_status = 2;

                    if (Functions.isMyServiceRunning(getActivity(), TrackRouteService.class)) {
                        Intent intent_Trace = new Intent(TRACK_TO_SERVICE);
                        intent_Trace.putExtra("record_status", 2);
                        getActivity().sendBroadcast(intent_Trace);
                    }
                } else {
                    record_start_layout.setBackgroundColor(Color.parseColor("#5599FF"));
                    record_start_text.setTextColor(Color.parseColor("#FFFFFF"));
                    record_start_text.setText(getContext().getResources().getString(R.string.stopRecord_text));
                    record_start_img.performClick();
                    record_start_img.setImageResource(R.drawable.record_selected_pause);
                    record_status = 1;
                    //====1.29
                    RecordActivity.time_text.setVisibility(View.VISIBLE);
                    RecordActivity.record_completeImg.setVisibility(View.VISIBLE);
                    starttime[0] = System.currentTimeMillis();
                    //----2.4

                    DataBaseHelper helper = DataBaseHelper.getmInstance(getActivity());
                    SQLiteDatabase database = helper.getWritableDatabase();
                    Cursor trackRoute_cursor = database.query("trackRoute",
                            new String[]{"routesCounter", "track_no", "track_lat", "track_lng",
                                    "track_start", "track_title", "track_totaltime", "track_completetime"},
                            null, null, null, null, null);
                    if (trackRoute_cursor != null) {
                        if (trackRoute_cursor.getCount() != 0) {
                            trackRoute_cursor.moveToLast();
                            Integer routesCounter = trackRoute_cursor.getInt(0);
                            if (routesCounter == RoutesCounter) {
                                Track_no++;
                            }
                        }
                        trackRoute_cursor.close();
                    }

                    if (!Functions.isMyServiceRunning(getActivity(), TrackRouteService.class)) {
                        Intent intent_Trace = new Intent(getActivity(), TrackRouteService.class);
                        intent_Trace.putExtra("record_status", 1);
                        intent_Trace.putExtra("start", starttime[0]);
                        intent_Trace.putExtra("routesCounter", RoutesCounter);
                        intent_Trace.putExtra("track_no", Track_no);
                        getActivity().startService(intent_Trace);
                    } else {
                        Intent intent_Trace = new Intent(TIMER_TO_SERVICE);
                        intent_Trace.putExtra("record_status", 1);
                        intent_Trace.putExtra("start", starttime[0]);
                        intent_Trace.putExtra("spent", tempSpent);
                        intent_Trace.putExtra("routesCounter", RoutesCounter);
                        intent_Trace.putExtra("track_no", Track_no);
                        getActivity().sendBroadcast(intent_Trace);
                        tempSpent = 0L;
                    }
                }
            }
        });

        record_spot_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (record_status == 0) {
                    Toast.makeText(getActivity(), getContext().getResources().getString(R.string.recordremind_text), Toast.LENGTH_LONG).show();
                } else if (!spotDialog.isShowing()) {
                    // reset內容
                    dialog_header_text.setText(getContext().getResources().getString(R.string.recordupload_text));
                    dialog_img.setImageBitmap(null);
                    content_editText.setText("");

                    // 隱藏View
                    title_layout.setVisibility(View.INVISIBLE);
                    RelativeLayout.LayoutParams otelParams = new RelativeLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, 0);
                    otelParams.addRule(RelativeLayout.BELOW, R.id.dialog_header_text);
                    title_layout.setLayoutParams(otelParams);

                    dialog_scrollview.setVisibility(View.INVISIBLE);
                    RelativeLayout.LayoutParams otelParams1 = new RelativeLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, 0);
                    otelParams1.addRule(RelativeLayout.BELOW, R.id.dialog_header_text);
                    dialog_scrollview.setLayoutParams(otelParams1);

                    dialog_choose_layout.setVisibility(View.VISIBLE);
                    dialog_confirm_layout.setVisibility(View.INVISIBLE);
                    // 隱藏View

                    spotDialog.show();
                }
            }
        });

        write.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialog_img.getVisibility() == dialog_img.VISIBLE) {
                    dialog_img.setImageBitmap(null);
                    dialog_img.setVisibility(View.INVISIBLE);
                }
                dialog_scrollview.setVisibility(View.VISIBLE);
                RelativeLayout.LayoutParams otelParams = new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, getPx(120));
                otelParams.addRule(RelativeLayout.BELOW, R.id.dialog_header_text);
                dialog_scrollview.setLayoutParams(otelParams);

                dialog_relativeLayout.setVisibility(View.VISIBLE);
                content_layout.setVisibility(View.VISIBLE);

                dialog_choose_layout.setVisibility(View.INVISIBLE);

                dialog_confirm_layout.setVisibility(View.VISIBLE);
                dialog_confirm_layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // save content to DB
                        if (content_editText.getText().equals("")) {
                            Toast.makeText(getActivity(), getContext().getResources().getString(R.string.emptyInput_text), Toast.LENGTH_SHORT).show();
                        } else {
                            DataBaseHelper helper = DataBaseHelper.getmInstance(getActivity());
                            SQLiteDatabase db = helper.getReadableDatabase();
                            Cursor memo_cursor = db.query("travelMemo", new String[]{"memo_routesCounter", "memo_trackNo",
                                            "memo_content", "memo_img", "memo_latlng", "memo_time", "memo_imgUrl"},
                                    null, null, null, null, null);
                            if (memo_cursor != null) {
                                ContentValues cv = new ContentValues();
                                cv.put("memo_routesCounter", RoutesCounter);
                                cv.put("memo_trackNo", Track_no);
                                cv.put("memo_content", content_editText.getText().toString());
                                if (CurrentLatlng != null) {
                                    cv.put("memo_latlng", CurrentLocation.getLongitude()+","+CurrentLocation.getLatitude());
                                }
                                SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
                                Date date = new Date();
                                String dateString = fmt.format(date);
                                cv.put("memo_time", dateString);
                                inDB = db.insert("travelMemo", null, cv);
//                                Log.e("3/23_", "DB insert content" + inDB + " content:"
//                                        + content_editText.getText().toString() + " Addtime " + dateString);

                                if (inDB != -1) {
                                    if (spotDialog.isShowing()) {
                                        if (content_layout.getVisibility() == content_layout.VISIBLE) {
                                            content_layout.setVisibility(View.INVISIBLE);
                                            content_editText.setText("");
                                        }
                                        spotDialog.dismiss();
                                    }
                                    Toast.makeText(getActivity(), getContext().getResources().getString(R.string.uploaded_text), Toast.LENGTH_SHORT).show();
                                }
                                memo_cursor.close();
                                if (spotDialog.isShowing()) {
                                    spotDialog.dismiss();
                                }
                            }
                        }

                    }
                });
            }
        });

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Log.e("3/23_", "CAMERA");
                //getContext().getResources().getString(R.string.uploaded_text)
                final CharSequence[] items = {getContext().getResources().getString(R.string.camera_text),
                        getContext().getResources().getString(R.string.album_text),
                        getContext().getResources().getString(R.string.cancel_text)};
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(getContext().getResources().getString(R.string.uploadPhoto_text));
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        if (items[item].equals(getContext().getResources().getString(R.string.camera_text))) {
                            // Create parameters for Intent with filename
                            ContentValues values = new ContentValues();
                            values.put(MediaStore.Images.Media.TITLE, "New Picture");
                            values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
                            // imageUri is the current activity attribute, define and save it for later usage
                            imageUri = getActivity().getContentResolver().insert(
                                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

                            Intent intent_camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            intent_camera.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                            startActivityForResult(intent_camera, REQUEST_CAMERA);
                            } else if (items[item].equals(getContext().getResources().getString(R.string.album_text))) {
                                Intent intent_photo = new Intent(Intent.ACTION_GET_CONTENT);
                                intent_photo.setType("image/*");
                                startActivityForResult(Intent.createChooser(intent_photo, getContext().getResources().getString(R.string.chooseFile_text)),
                                        SELECT_FILE);
                            } else if (items[item].equals(getContext().getResources().getString(R.string.cancel_text))) {
                                dialog.dismiss();
                        }
                        }
                });
                builder.show();
            }
        });

        leave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (spotDialog.isShowing()) {
                    if (content_layout.getVisibility() == content_layout.VISIBLE) {
                        content_layout.setVisibility(View.INVISIBLE);
                        content_editText.setText("");
                    }
                    if (memo_img != null) {
                        dialog_img.setImageBitmap(null);
                    }
                    spotDialog.dismiss();
                }
            }
        });

        RetrieveRouteFromDB();
    }

    @Override
    public void onResume() {
        mapView.onResume();
        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
        if (MarkerIcon == null) {
            MarkerIcon = decodeBitmapFromResource(getResources(), R.drawable.location3, 10, 18);
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        mapView.onPause();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
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
//        Log.e("3/23_RecordTrack", "onDestroyView");
        mapView.onDestroy();
        if (broadcastReceiver_timer != null)
            getActivity().unregisterReceiver(broadcastReceiver_timer);
        if (broadcastReceiver != null)
            getActivity().unregisterReceiver(broadcastReceiver);
        MarkerIcon.recycle();
        System.gc();
        super.onDestroyView();
    }

    @Override
    public void onLowMemory() {
//        Log.e("3/23_RecordTrack", "onLowMemory");
        mapView.onLowMemory();
        MarkerIcon.recycle();
        System.gc();
        super.onLowMemory();
    }

    @Override
    public void onConnected(Bundle bundle) {
        // 已經連線到Google Services
        // 啟動位置更新服務
        // 位置資訊更新的時候，應用程式會自動呼叫LocationListener.onLocationChanged
        Log.i(TAG, "Location services connected.");

        Location last_location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (last_location == null) {
            LocationServices.FusedLocationApi.requestLocationUpdates
                    (mGoogleApiClient, mLocationRequest, (LocationListener) this);
        } else {
            HandleNewLocation(last_location);
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
            HandleNewLocation(CurrentLocation);
        }
    }

    private void HandleNewLocation(Location location) {
//        Log.d(TAG, location.toString());

        CurrentLocation = location;
        CurrentLatlng = new LatLng(location.getLatitude(), location.getLongitude());

        // 設定目前位置的標記
        if (CurrentMarker == null) {
            // 移動地圖到目前的位置
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(CurrentLatlng, 15));
            CurrentMarker = mMap.addMarker(new MarkerOptions().position(CurrentLatlng).title("I am here!")
                    .icon(BitmapDescriptorFactory.fromBitmap(MarkerIcon)));
        } else {
            CurrentMarker.setPosition(CurrentLatlng);
        }

    }

    // 顯示軌跡紀錄 TODO Need to modify
    private void DisplayRoute(LatLng track_latlng, int rC, int tN) {
        int routesCounter = rC;
        int track_no = tN;
        DataBaseHelper helper = DataBaseHelper.getmInstance(getActivity());
        SQLiteDatabase database = helper.getWritableDatabase();
        Cursor trackRoute_cursor = database.query("trackRoute",
                new String[]{"routesCounter","track_no", "track_lat", "track_lng",
                        "track_start", "track_title", "track_totaltime", "track_completetime"},
                null, null, null, null, null);
        if (trackRoute_cursor != null) {
            if (TraceRoute == null) {
                TraceRoute = new ArrayList<LatLng>();
            }
            if (trackRoute_cursor.getCount() == 0) {
                TraceRoute.add(track_latlng);
            } else {
                trackRoute_cursor.moveToLast();
                Integer rCounterInDB = trackRoute_cursor.getInt(0);
                Integer tNoInDB = trackRoute_cursor.getInt(1);
                if (routesCounter != rCounterInDB || track_no != tNoInDB) {
                    TraceRoute.clear();
                    TraceRoute.add(track_latlng);
                } else {
                    TraceRoute.add(track_latlng);
                }
            }
            trackRoute_cursor.close();
        }

        PolylineOptions polylineOpt = new PolylineOptions();
        for (LatLng latlng : TraceRoute) {
            polylineOpt.add(latlng);
        }

        polylineOpt.color(Color.parseColor("#2BB7EC"));

        Polyline line = mMap.addPolyline(polylineOpt);
        line.setWidth(10);

//        Log.d("3/20_畫出軌跡", "DisplayRoute" + track_latlng.toString());
    }

    // retrieve trackRoute from DB
    private void RetrieveRouteFromDB() {
        DataBaseHelper helper = DataBaseHelper.getmInstance(getActivity());
        SQLiteDatabase database = helper.getWritableDatabase();
        Cursor trackRoute_cursor = database.query("trackRoute",
                new String[]{"routesCounter","track_no", "track_lat", "track_lng",
                        "track_start", "track_title", "track_totaltime", "track_completetime"},
                null, null, null, null, null);
        if (trackRoute_cursor != null) {
            if (trackRoute_cursor.getCount() != 0) {
                //track_start = 0:該Route最後一筆(停止)，1:記錄中(開始)，2:該Track最後一筆(暫停)
                Boolean DontDisplay = false;
                if (TraceRoute == null) {
                    TraceRoute = new ArrayList<LatLng>();
                }
                while (trackRoute_cursor.moveToNext()) {
                    Double track_lat = trackRoute_cursor.getDouble(2);
                    Double track_lng = trackRoute_cursor.getDouble(3);
                    Integer track_start = trackRoute_cursor.getInt(4);
                    LatLng track_latLng = new LatLng(track_lat, track_lng);

                    if (DontDisplay) {
                        TraceRoute.clear();
                        TraceRoute.add(track_latLng);
                        DontDisplay = false;
                        continue;
                    } else {
                        TraceRoute.add(track_latLng);

                        if (track_start == 0 || track_start == 2) {
                            DontDisplay = true;
                        }
                    }
                    PolylineOptions polylineOpt = new PolylineOptions();
                    for (LatLng latlng : TraceRoute) {
                        polylineOpt.add(latlng);
                    }

                    polylineOpt.color(Color.parseColor("#2BB7EC"));

                    Polyline line = mMap.addPolyline(polylineOpt);
                    line.setWidth(10);

//                    Log.d("3/20_還原軌跡", "RetrieveRouteFromDB" + track_latLng.toString());

                }
                //TraceRoute.clear();

                // 還原紀錄狀態
                trackRoute_cursor.moveToLast();
                Integer track_start = trackRoute_cursor.getInt(4);
                String temp_totaltime = trackRoute_cursor.getString(6);
                // 停止
                if (track_start == 0) {
                    RoutesCounter = trackRoute_cursor.getInt(0) + 1;
                    Track_no = 1;
                    TraceRoute.clear();
                } else {
                    RoutesCounter = trackRoute_cursor.getInt(0);
                    Track_no = trackRoute_cursor.getInt(1);
                    // 還在紀錄
                    if (track_start == 1) {
                        // 正在紀錄
                        record_status = 1;
                        record_start_layout.setBackgroundColor(Color.parseColor("#5599FF"));
                        record_start_text.setTextColor(Color.parseColor("#FFFFFF"));
                        record_start_text.setText(getContext().getResources().getString(R.string.stopRecord_text));
                        record_start_img.performClick();
                        record_start_img.setImageResource(R.drawable.record_selected_pause);
                    } else if (track_start == 2) {
                        // 暫停紀錄
                        record_status = 2;
                        Integer min = Integer.valueOf(temp_totaltime.substring(0, temp_totaltime.indexOf(":", 0)));
                        Integer sec = Integer.valueOf(temp_totaltime.substring(temp_totaltime.indexOf(":", 0)+1, temp_totaltime.length()));
                        //Log.d("4/1_", "min:" + temp_totaltime.substring(0, temp_totaltime.indexOf(":", 0)));
                        //Log.d("4/1_", "sec:" + temp_totaltime.substring(temp_totaltime.indexOf(":", 0)+1, temp_totaltime.length()));
                        tempSpent = Long.valueOf((min*60+sec)*1000);
                        record_start_layout.setBackgroundColor(Color.parseColor("#FFFFFF"));
                        record_start_text.setTextColor(Color.parseColor("#555555"));
                        record_start_text.setText(getContext().getResources().getString(R.string.startRecord_text));
                        record_start_img.setImageResource(R.drawable.ic_play_light);
                        TraceRoute.clear();
                    }
                }
            }
            trackRoute_cursor.close();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == getActivity().RESULT_OK) {
            if (requestCode == REQUEST_CAMERA) {
                ContentResolver cr = getActivity().getContentResolver();
                try {
                    memo_img = Functions.ScalePic(BitmapFactory.decodeStream(cr.openInputStream(imageUri)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (requestCode == SELECT_FILE) {
                if (data != null) {
                    imageUri = data.getData();
                    ContentResolver cr = getActivity().getContentResolver();
                    try {
//                        Log.e("3/23_", "uri:" + imageUri);
                        memo_img = Functions.ScalePic(BitmapFactory.decodeStream(cr.openInputStream(imageUri)));

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }

            ExifInterface exif = null;     //Since API Level 5
            try {
                exif = new ExifInterface(getPath(getActivity(), imageUri));
            } catch (IOException e) {
                e.printStackTrace();
            }
            String orientString = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
            int orientation = orientString != null ? Integer.parseInt(orientString):ExifInterface.ORIENTATION_NORMAL;

//            Log.e("4/1_", "exifOrientation: " + orientation);

            int rotationAngle = 0;
            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) rotationAngle = 90;
            if (orientation == ExifInterface.ORIENTATION_ROTATE_180) rotationAngle = 180;
            if (orientation == ExifInterface.ORIENTATION_ROTATE_270) rotationAngle = 270;
//            Log.e("4/1_", "rotationAngle: " + rotationAngle);

            memo_img = rotateImage(memo_img, rotationAngle);
            dialog_img.setImageBitmap(memo_img);

            if (content_layout.getVisibility() == content_layout.VISIBLE) {
                content_layout.setVisibility(View.INVISIBLE);
            }
            dialog_scrollview.setVisibility(View.VISIBLE);
            RelativeLayout.LayoutParams otelParams = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, getPx(200));
            otelParams.addRule(RelativeLayout.BELOW, R.id.dialog_header_text);
            dialog_scrollview.setLayoutParams(otelParams);

            dialog_relativeLayout.setVisibility(View.VISIBLE);

            dialog_img.setVisibility(View.VISIBLE);

            dialog_choose_layout.setVisibility(View.INVISIBLE);

            dialog_confirm_layout.setVisibility(View.VISIBLE);
            dialog_confirm_layout.setOnClickListener(ok);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    View.OnClickListener ok = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // TODO:upload image
            new uploadImage().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    };

    private BroadcastReceiver broadcastReceiver_timer = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Update Your UI here..
            if (intent != null) {
                Integer status = intent.getIntExtra("record_status", 0);
                Long spent = intent.getLongExtra("spent", 99);
                if (spent != 99) {
                    if (status == 2) {
                        tempSpent = spent;
                    } else {
                        if (((spent / 1000) / 60) > 0) {
                            if (((spent / 1000) / 60) < 10)
                                if (((spent / 1000) % 60) < 10)
                                    RecordActivity.time_text.setText("0" + ((spent / 1000) / 60) + ":0" + ((spent / 1000) % 60));
                                else
                                    RecordActivity.time_text.setText("0" + ((spent / 1000) / 60) + ":" + ((spent / 1000) % 60));
                            else
                                if (((spent / 1000) % 60) < 10)
                                    RecordActivity.time_text.setText(((spent / 1000) / 60) + ":0" + ((spent / 1000) % 60));
                                else
                                    RecordActivity.time_text.setText(((spent / 1000) / 60) + ":" + ((spent / 1000) % 60));
                        } else {
                            if (((spent / 1000) % 60) < 10)
                                RecordActivity.time_text.setText("00:0" + ((spent / 1000) % 60));
                            else
                                RecordActivity.time_text.setText("00:" + ((spent / 1000) % 60));
                        }
                    }
                }
//                Log.d("3/26", "BroadcastReceiver: " + intent.getLongExtra("spent", 99));
            }
        }
    };

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Update Your UI here..
            if (intent != null) {
                Integer routesCounter = intent.getIntExtra("routesCounter", 1);
                Integer track_no = intent.getIntExtra("track_no", 1);
                Double track_lat = intent.getDoubleExtra("track_lat", 0);
                Double track_lng = intent.getDoubleExtra("track_lng", 0);
                LatLng track_latLng = new LatLng(track_lat, track_lng);

                DisplayRoute(track_latLng, routesCounter, track_no);
            }
        }
    };

    public int getPx(int dimensionDp) {
        float density = getResources().getDisplayMetrics().density;
        return (int) (dimensionDp * density + 0.5f);
    }

    public class uploadImage extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                ByteArrayOutputStream stream2 = new ByteArrayOutputStream();
                memo_img.compress(Bitmap.CompressFormat.JPEG, 80, stream2);
                byte[] data = stream2.toByteArray();

                HttpClient client = new DefaultHttpClient();
                HttpPost post = new HttpPost("http://zhiyou.lin366.com/api/diy/upload.aspx");

                ByteArrayBody bab = new ByteArrayBody(data, "image.jpg");
                MultipartEntity multipartEntity = new MultipartEntity();
                multipartEntity.addPart("file", bab);
                post.setEntity(multipartEntity);

                HttpResponse response = null;
                String getString = null;
                try {
                    response = client.execute(post);
                    getString = EntityUtils.toString(response.getEntity());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                String state = null;
                String imageUrl = null;
                try {
                    state = new JSONObject(getString.substring(
                            getString.indexOf("{"), getString.lastIndexOf("}") + 1)).getString("states");
                    imageUrl = new JSONObject(getString).getString("msg");
                } catch (JSONException | NullPointerException e) {
                    e.printStackTrace();
                }

                if (state != null && state.equals("1") && imageUrl != null) {
                    Log.e("4/26", "imageUrl: " + imageUrl);

                    DataBaseHelper helper = DataBaseHelper.getmInstance(getActivity());
                    SQLiteDatabase db = helper.getReadableDatabase();
                    Cursor memo_cursor = db.query("travelMemo", new String[]{"memo_routesCounter", "memo_trackNo",
                                    "memo_content", "memo_img", "memo_latlng", "memo_time", "memo_imgUrl"},
                            null, null, null, null, null);
                    if (memo_cursor != null) {
                        ContentValues cv = new ContentValues();
                        cv.put("memo_routesCounter", RoutesCounter);
                        cv.put("memo_trackNo", Track_no);
                        if (memo_img != null) {
                            cv.put("memo_img", data);
                        }
                        if (CurrentLatlng != null) {
                            cv.put("memo_latlng", CurrentLocation.getLongitude()+","+CurrentLocation.getLatitude());
                        }
                        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
                        Date date = new Date();
                        String dateString = fmt.format(date);
                        cv.put("memo_time", dateString);
                        cv.put("memo_imgUrl", imageUrl);
                        inDB = db.insert("travelMemo", null, cv);
                    }
                    if (memo_cursor != null)
                        memo_cursor.close();
                }
            } catch (Exception e) {
                // handle exception here
                Log.e("4/26", e.toString());
            }
            return null;
        }

        protected void onPostExecute(String s) {
            if (inDB != -1) {
                if (spotDialog.isShowing()) {
                    if (memo_img != null) {
                        dialog_img.setImageBitmap(null);
                    }
                    spotDialog.dismiss();
                }
                Toast.makeText(getActivity(), getContext().getResources().getString(R.string.uploadedPic_text), Toast.LENGTH_SHORT).show();
            }

            if (spotDialog.isShowing()) {
                spotDialog.dismiss();
            }
            super.onPostExecute(s);
        }
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Bitmap retVal;

        Matrix matrix = new Matrix();
        matrix.setRotate(angle);
        retVal = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);

        return retVal;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

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
//        Log.e("3/27_", "Marker size. "+height+","+width);
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

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        /*if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }*/
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        /*if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }*/
    }

    @Override
    public void onDetach() {
        super.onDetach();
        //mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
