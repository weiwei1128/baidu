package com.flyingtravel;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;

import com.flyingtravel.Utility.Functions;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.util.List;

public class ScheduleMapsActivity extends FragmentActivity implements
        com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;
    private MapView mapView;
    Marker startMarker;
    LinearLayout startLayout, stopLayout;
    int addressCount = 0;
    String[] address;
    private Bitmap MarkerIcon;
    LatLngBounds bounds;
    LatLngBounds.Builder builder;
    LatLng[] LatLngList;
    final Handler handler = new Handler();
    Boolean startPlay=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.schedulemap_activity);
        startLayout = (LinearLayout) findViewById(R.id.start_layout);
//        stopLayout = (LinearLayout) findViewById(R.id.stop_layout);
        MarkerIcon = Functions.decodeBitmapFromResource(getResources(), R.drawable.location3, 10, 18);

        Bundle bundle = this.getIntent().getExtras();
        if (bundle.containsKey("count")) {
//            Log.e("5.6", "count:" + bundle.getInt("count"));
            addressCount = bundle.getInt("count");
        }
//        address = new String[addressCount];
        if (bundle.containsKey("address")) {
            address = bundle.getStringArray("address");
        }
//        Log.d("5.6", "address size:" + address.length + "string:" + address);

        mapView = (MapView) findViewById(R.id.Map);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        mMap = mapView.getMap();
        if (mMap != null) {
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setCompassEnabled(true);
            mMap.getUiSettings().setAllGesturesEnabled(true);
            mMap.getUiSettings().setMapToolbarEnabled(false);
            /////----------------------------------Zooming camera to position user-----------------
            /*
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Criteria criteria = new Criteria();

            Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
            if (location != null)
            {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(location.getLatitude(), location.getLongitude()), 13));

                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(new LatLng(location.getLatitude(), location.getLongitude()))      // Sets the center of the map to location user
                        .zoom(17)                   // Sets the zoom
                        .bearing(90)                // Sets the orientation of the camera to east
//                        .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                        .build();                   // Creates a CameraPosition from the builder
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));



            }
            */
/////----------------------------------Zooming camera to position user-----------------


            builder = new LatLngBounds.Builder();
            LatLngList = new LatLng[addressCount];
            for (int i = 0; i < addressCount; i++)
                LatLngList[i] = getLat(address[i]);
            if (addressCount > 1)
                for (int i = 0; i < addressCount - 1; i++) {
                    drawLine(LatLngList[i], LatLngList[i + 1]);
                }
            bounds = builder.build();
            final CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 100);
//        Log.e("5.6", "Location:bounds= " + bounds);
            mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                @Override
                public void onMapLoaded() {
                    mMap.moveCamera(cameraUpdate);

                }
            });

            startLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!startPlay) {
                        if(startMarker!=null&&!startMarker.getPosition().equals(LatLngList[0]))
                            startMarker.setVisible(false);

                        startMarker = mMap.addMarker(new MarkerOptions().position(LatLngList[0])
                                .icon(BitmapDescriptorFactory.fromBitmap(MarkerIcon)));
                        animateMarker(startMarker, LatLngList, false);
                        startPlay=true;
                    }
                }
            });
            /*
            stopLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(startPlay) {
                        handler.removeCallbacksAndMessages(null);
//                        startMarker.setVisible(false);
                        startPlay=false;
                    }
                }
            });
            */
        }

    }


    LatLng getLat(String address) {
        Geocoder coder = new Geocoder(this);
        double longitude = 0;
        double latitude = 0;
        try {
            List<Address> adresses = coder.getFromLocationName(address, 1);
            if (adresses == null)
                return null;
            Address location = adresses.get(0);
            longitude = location.getLongitude();
            latitude = location.getLatitude();
            Marker marker = mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title(address)
                    .icon(BitmapDescriptorFactory.fromBitmap(MarkerIcon)));
//            marker.showInfoWindow();
//

            builder.include(new LatLng(latitude, longitude));
            Log.e("5.6", "Location:" + address + " longitude= " + longitude + " latitude=" + latitude);
            return new LatLng(latitude, longitude);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    void drawLine(LatLng start, LatLng end) {
//        Log.d("5.6","draw");
        PolylineOptions polylineOptions = new PolylineOptions().width(10).color(R.color.black);
        polylineOptions.add(start);
        polylineOptions.add(end);
        mMap.addPolyline(polylineOptions);
    }

    public void animateMarker(final Marker marker, final LatLng[] locationList,
                              final boolean hideMarker) {
        if (locationList.length <= 1)
            return;

        final long[] start = {SystemClock.uptimeMillis()};

        final long duration = 1500;
        final Interpolator interpolator = new LinearInterpolator();
        handler.sendEmptyMessage(1);
        final int[] i = {0};
        handler.post(new Runnable() {
            @Override
            public void run() {

                LatLng[] startLatLng = {locationList[i[0]]};
                LatLng toPosition = locationList[i[0] + 1];

                long elapsed = SystemClock.uptimeMillis() - start[0];
                float t = interpolator.getInterpolation((float) elapsed / duration);
                double lng = t * toPosition.longitude + (1 - t)
                        * startLatLng[0].longitude;
                double lat = t * toPosition.latitude + (1 - t)
                        * startLatLng[0].latitude;
                marker.setPosition(new LatLng(lat, lng));
//
                if (t < 1.0) {
                    // Post again 16ms later.

                    handler.postDelayed(this, 16);
                } else {
                    if((i[0] +3<=locationList.length)) {
//                        Log.e("5.6","i:"+i[0]+" length:"+locationList.length);
                        i[0]++;
                        start[0] = SystemClock.uptimeMillis();
                        handler.postDelayed(this, 16);
                    }else {
                        marker.setVisible(false);
                        startPlay = false;
                    }
                }
            }
        });
    }

    @Override
    public void onLocationChanged(Location location) {
//        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
    }
}
