package com.flyingtravel.Activity.Spot;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.flyingtravel.Utility.GlobalVariable;
import com.flyingtravel.R;
import com.flyingtravel.Utility.DataBaseHelper;
import com.flyingtravel.Utility.Functions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class SpotDetailActivity extends AppCompatActivity {

    ImageLoader loader = ImageLoader.getInstance();
    DisplayImageOptions options;
    private ImageLoadingListener listener;

    Integer mPosition;
    TextView SpotName, SpotOpenTime, SpotAddress, SpotTicketInfo, SpotDetail;
    ImageView SpotImg,BackImg;

    private Double Latitude;
    private Double Longitude;

    private DataBaseHelper helper;
    private SQLiteDatabase database;

    private GlobalVariable globalVariable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.spot_detail_activity);

        helper = DataBaseHelper.getmInstance(getApplicationContext());
        database = helper.getWritableDatabase();

        globalVariable = (GlobalVariable) getApplicationContext();
        if (globalVariable.SpotDataSorted == null || globalVariable.SpotDataSorted.isEmpty()) {
            // retrieve Location from DB
            Cursor location_cursor = database.query("location",
                    new String[]{"CurrentLat", "CurrentLng"}, null, null, null, null, null);
            if (location_cursor != null) {
                if (location_cursor.getCount() != 0) {
                    while (location_cursor.moveToNext()) {
                        Latitude = location_cursor.getDouble(0);
                        Longitude = location_cursor.getDouble(1);
//                        Log.d("3/8_抓取位置", Latitude.toString() + Longitude.toString());
                    }
                }
                location_cursor.close();
            }
            // get SpotData
            Cursor spotDataSorted_cursor = database.query("spotDataSorted",
                    new String[]{"spotName", "spotAdd","spotLat", "spotLng", "picture1",
                            "picture2","picture3", "openTime", "ticketInfo", "infoDetail", "distance"},
                    null, null, null, null, null);
            if (spotDataSorted_cursor != null) {
                while (spotDataSorted_cursor.moveToNext()) {
                    String Name = spotDataSorted_cursor.getString(0);
                    String Add = spotDataSorted_cursor.getString(1);
                    Double Latitude = spotDataSorted_cursor.getDouble(2);
                    Double Longitude = spotDataSorted_cursor.getDouble(3);
                    String Picture1 = spotDataSorted_cursor.getString(4);
                    String Picture2 = spotDataSorted_cursor.getString(5);
                    String Picture3 = spotDataSorted_cursor.getString(6);
                    String OpenTime = spotDataSorted_cursor.getString(7);
                    String TicketInfo = spotDataSorted_cursor.getString(8);
                    String InfoDetail = spotDataSorted_cursor.getString(9);
                    globalVariable.SpotDataSorted.add(new SpotData(Name, Latitude, Longitude, Add,
                            Picture1, Picture2, Picture3, OpenTime,TicketInfo, InfoDetail));
                }
                spotDataSorted_cursor.close();
            }

            //Log.d(TAG, "排序");
            for (SpotData mSpot : globalVariable.SpotDataSorted) {
                //for迴圈將距離帶入，判斷距離為Distance function
                //需帶入使用者取得定位後的緯度、經度、景點店家緯度、經度。
                mSpot.setDistance(Distance(Latitude, Longitude,
                        mSpot.getLatitude(), mSpot.getLongitude()));
            }

            //依照距離遠近進行List重新排列
            DistanceSort(globalVariable.SpotDataSorted);
        }

        SpotImg = (ImageView) findViewById(R.id.spotdetail_Img);
        SpotName = (TextView) findViewById(R.id.spotdetailName_Text);
        SpotOpenTime = (TextView) findViewById(R.id.opentime_Text);
        SpotAddress = (TextView) findViewById(R.id.address_Text);
        SpotTicketInfo = (TextView) findViewById(R.id.ticketinfo_Text);
        SpotDetail = (TextView) findViewById(R.id.spotdetail_Text);

        BackImg = (ImageView) findViewById(R.id.spotdetail_backImg);
        BackImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putInt("position", 1);
                Functions.go(true, SpotDetailActivity.this, SpotDetailActivity.this, SpotActivity.class, bundle);
            }
        });

        Bundle bundle = this.getIntent().getExtras();
        if (bundle.containsKey("WhichItem")) {
            mPosition = bundle.getInt("WhichItem");
        }

        options = new DisplayImageOptions.Builder()
                .showImageOnFail(R.drawable.error)
                .showImageOnLoading(R.drawable.loading2)
                .showImageForEmptyUri(R.drawable.empty)
                .cacheInMemory(false)
                .cacheOnDisk(true).build();
        listener = new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String s, View view) {

            }

            @Override
            public void onLoadingFailed(String s, View view, FailReason failReason) {
                ImageView imageView = (ImageView) view.findViewById(R.id.spotdetail_Img);
                loader.displayImage(null, imageView, options, listener);
            }

            @Override
            public void onLoadingComplete(String s, View view, Bitmap bitmap) {

            }

            @Override
            public void onLoadingCancelled(String s, View view) {

            }
        };

        String ImgString = globalVariable.SpotDataSorted.get(mPosition).getPicture1();
        if (!ImgString.endsWith(".jpg"))
            ImgString = null;
        loader.displayImage(ImgString, SpotImg, options, listener);

        SpotName.setText(globalVariable.SpotDataSorted.get(mPosition).getName());

        if (globalVariable.SpotDataSorted.get(mPosition).getOpenTime().equals("")) {
            SpotOpenTime.setText(SpotDetailActivity.this.getResources().getString(R.string.OpenTime_text)+
                    SpotDetailActivity.this.getResources().getString(R.string.Empty_text));
        } else {
            SpotOpenTime.setText(SpotDetailActivity.this.getResources().getString(R.string.OpenTime_text)+ globalVariable.SpotDataSorted.get(mPosition).getOpenTime());
        }

        if (globalVariable.SpotDataSorted.get(mPosition).getAdd().equals("")) {
            SpotAddress.setText(SpotDetailActivity.this.getResources().getString(R.string.Address_text)+
                    SpotDetailActivity.this.getResources().getString(R.string.Empty_text));
        } else {
            SpotAddress.setText(SpotDetailActivity.this.getResources().getString(R.string.Address_text) + globalVariable.SpotDataSorted.get(mPosition).getAdd());
        }

        if (globalVariable.SpotDataSorted.get(mPosition).getTicketInfo().equals("")) {
            SpotTicketInfo.setText(SpotDetailActivity.this.getResources().getString(R.string.TicketInfo_text)+
                    SpotDetailActivity.this.getResources().getString(R.string.Empty_text));
        } else {
            SpotTicketInfo.setText(SpotDetailActivity.this.getResources().getString(R.string.TicketInfo_text) + globalVariable.SpotDataSorted.get(mPosition).getTicketInfo());
        }

        SpotDetail.setText(globalVariable.SpotDataSorted.get(mPosition).getInfoDetail());

        if(SpotImg!=null)
            SpotImg.setScaleType(ImageView.ScaleType.CENTER_CROP);

    }

    @Override
    protected void onDestroy() {
        System.gc();
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK) {
            Bundle bundle = new Bundle();
            bundle.putInt("position", 1);
            Functions.go(true, SpotDetailActivity.this, SpotDetailActivity.this, SpotActivity.class, bundle);
        }
        return false;
    }

    //List排序，依照距離由近開始排列，第一筆為最近，最後一筆為最遠
    private void DistanceSort(ArrayList<SpotData> spot) {
        Collections.sort(spot, new Comparator<SpotData>() {
            @Override
            public int compare(SpotData spot1, SpotData spot2) {
                return spot1.getDistance() < spot2.getDistance() ? -1 : 1;
            }
        });
    }

    //帶入使用者及景點店家經緯度可計算出距離
    public double Distance(double longitude1, double latitude1, double longitude2, double latitude2) {
        double radLatitude1 = latitude1 * Math.PI / 180;
        double radLatitude2 = latitude2 * Math.PI / 180;
        double l = radLatitude1 - radLatitude2;
        double p = longitude1 * Math.PI / 180 - longitude2 * Math.PI / 180;
        double distance = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(l / 2), 2)
                + Math.cos(radLatitude1) * Math.cos(radLatitude2)
                * Math.pow(Math.sin(p / 2), 2)));
        distance = distance * 6378137.0;
        distance = Math.round(distance * 10000) / 10000;

        return distance;
    }
}
