package com.flyingtravel.ImageSlide;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.flyingtravel.R;
import com.flyingtravel.Utility.DataBaseHelper;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;


public class MainImageFragment extends Fragment {
    public static final String ARG_ITEM_ID = "home_fragment";

    private static final long ANIM_VIEWPAGER_DELAY = 5000;
    private static final long ANIM_VIEWPAGER_DELAY_USER_VIEW = 10000;

    // UI References
    private ViewPager mViewPager;
    PageIndicator mIndicator;

    AlertDialog alertDialog;

    List<Product> products;
    List<Product> productsAdd = new ArrayList<Product>();
    Product product;
    RequestImgTask task;
    boolean stopSliding = false;
    String message;

    private Runnable animateViewPager;
    private Handler handler;

    FragmentActivity activity;
    RelativeLayout putProgressLayout;
    ProgressBar progressBar;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();

    }

    ////1110

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.main_image_frament, container, false);
        findViewById(view);
        putProgressLayout = (RelativeLayout) view.findViewById(R.id.img_slideshow_layout);
        progressBar = new ProgressBar(activity.getBaseContext());
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        progressBar.setLayoutParams(layoutParams);
        progressBar.setMinimumHeight(100);
//        putProgressLayout.addView(progressBar);

        mIndicator.setOnPageChangeListener(new PageChangeListener());
        mViewPager.setOnPageChangeListener(new PageChangeListener());
        mViewPager.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
                switch (event.getAction()) {

                    case MotionEvent.ACTION_CANCEL:
                        break;

                    case MotionEvent.ACTION_UP:
                        // calls when touch release on ViewPager
                        if (products != null && products.size() != 0) {
//                            putProgressLayout.removeView(progressBar);
                            stopSliding = false;
                            runnable(products.size());
                            handler.postDelayed(animateViewPager,
                                    ANIM_VIEWPAGER_DELAY_USER_VIEW);
                        }
                        break;

                    case MotionEvent.ACTION_MOVE:
                        // calls when ViewPager touch
                        if (handler != null && !stopSliding) {
                            stopSliding = true;
                            handler.removeCallbacks(animateViewPager);
                        }
                        break;
                }
                return false;
            }
        });


        return view;
    }

    private void findViewById(View view) {
        mViewPager = (ViewPager) view.findViewById(R.id.view_pager);
        mIndicator = (CirclePageIndicator) view.findViewById(R.id.indicator);
    }

    @Override
    public void onResume() {
        if (products == null) {
            sendRequest();
        } else {
//            putProgressLayout.removeView(progressBar);
            mViewPager.setAdapter(new ImageSliderAdapter(activity, products,
                    MainImageFragment.this));

            mIndicator.setViewPager(mViewPager);
            runnable(products.size());
            //Re-run callback
            handler.postDelayed(animateViewPager, ANIM_VIEWPAGER_DELAY);
        }
        super.onResume();
    }


    @Override
    public void onPause() {
        if (task != null)
            task.cancel(true);
        if (handler != null) {
            //Remove callback
            handler.removeCallbacks(animateViewPager);
        }
        super.onPause();
    }

    private void sendRequest() {
        if (CheckNetworkConnection.isConnectionAvailable(activity)) {
            task = new RequestImgTask(activity);
            task.execute();
        } else {
            Log.d("LoadImage", "no internet");
        }
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
        public void onFragmentInteraction(Uri uri);
    }

    ////1110

    private class PageChangeListener implements OnPageChangeListener {

        @Override
        public void onPageScrollStateChanged(int state) {
            if (state == ViewPager.SCROLL_STATE_IDLE) {
                if (products != null) {
//                    Log.d("LoadImage", "name= " + ((Product) products.get(mViewPager
//                            .getCurrentItem())).getName());
                }
            }
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageSelected(int arg0) {
        }
    }

    public void runnable(final int size) {
        handler = new Handler();
        animateViewPager = new Runnable() {
            public void run() {
                if (!stopSliding) {
                    if (mViewPager.getCurrentItem() == size - 1) {
                        mViewPager.setCurrentItem(0);
                    } else {
                        mViewPager.setCurrentItem(
                                mViewPager.getCurrentItem() + 1, true);
                    }
                    handler.postDelayed(animateViewPager, ANIM_VIEWPAGER_DELAY);
                }
            }
        };
    }

    private class RequestImgTask extends AsyncTask<String, Void, List<Product>> {
        private final WeakReference<Activity> activityWeakRef;
        Throwable error;
        //0307 wei
        Context context;

        private RequestImgTask(Activity activityWeakRef) {
            this.context = activityWeakRef.getBaseContext();
            this.activityWeakRef = new WeakReference<Activity>(activityWeakRef);
        }

        @Override
        protected List<Product> doInBackground(String... params) {
            //0307
            DataBaseHelper helper = DataBaseHelper.getmInstance(context);
            SQLiteDatabase database = helper.getWritableDatabase();
            Cursor cursor = database.query("banner", new String[]{"img_url"}, null, null, null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
//                    Log.i("3.25", "Getting Banner~" + cursor.getString(0));
                    product = new Product();
                    product.setId(434);
                    product.setName("Pattern - Fractal Wallpaper");
                    product.setImageUrl(cursor.getString(0));
                    productsAdd.add(product);
                }
                cursor.close();
            }
/*
            product = new Product();
            product.setId(434);
            product.setName("Pattern - Fractal Wallpaper");
            product.setImageUrl("http://www.hchcc.gov.tw/ch/temp/flower2014/images/index_F_01.jpg");
            productsAdd.add(product);


            product = new Product();
            product.setId(431);
            product.setName("Mickey Mouse");
            product.setImageUrl("http://www.hchcc.gov.tw/ch/temp/flower2014/images/index_F_01.jpg");
            productsAdd.add(product);

            product = new Product();
            product.setId(424);
            product.setName("Pattern - Wallpaper");
            product.setImageUrl("http://www.hchcc.gov.tw/ch/temp/flower2014/images/index_F_01.jpg");
            productsAdd.add(product);

            product = new Product();
            product.setId(426);
            product.setName("Batman");
            product.setImageUrl("http://www.hchcc.gov.tw/ch/temp/flower2014/images/index_F_01.jpg");
            productsAdd.add(product);

            product = new Product();
            product.setId(419);
            product.setName("Pattern - Music");
            product.setImageUrl("http://www.hchcc.gov.tw/ch/temp/flower2014/images/index_F_01.jpg");
            productsAdd.add(product);
            */
            return productsAdd;
        }

        @Override
        protected void onPostExecute(List<Product> results) {
            if (activityWeakRef != null && !activityWeakRef.get().isFinishing()) {
                if (error != null && error instanceof IOException) {
                    Log.d("LoadImage", "time out");
                } else if (error != null) {
                    Log.d("LoadImage", "error occured");
                } else {
                    products = results;
                    if (results != null) {
                        if (products != null && products.size() != 0) {
//                            putProgressLayout.removeView(progressBar);
                            mViewPager.setAdapter(new ImageSliderAdapter(activity,
                                    products, MainImageFragment.this));
                            mIndicator.setViewPager(mViewPager);
                            runnable(products.size());
                            handler.postDelayed(animateViewPager, ANIM_VIEWPAGER_DELAY);
                        }
                    } else {
                        Log.d("LoadImage", "noProducts");
                    }
                }
            }

            super.onPostExecute(products);
        }
    }

    ////1110
}
