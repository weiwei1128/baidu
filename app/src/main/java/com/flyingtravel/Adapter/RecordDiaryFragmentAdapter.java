package com.flyingtravel.Adapter;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.daimajia.slider.library.Indicators.PagerIndicator;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.DefaultSliderView;
import com.daimajia.slider.library.Tricks.ViewPagerEx;
import com.flyingtravel.R;
import com.flyingtravel.Utility.DataBaseHelper;

/**
 * Created by Tinghua on 3/27/2016.
 */
public class RecordDiaryFragmentAdapter extends BaseAdapter implements ViewPagerEx.OnPageChangeListener {

    private LayoutInflater inflater;
    private ViewHolder mViewHolder;

    private Context context;
    private DataBaseHelper helper;
    private SQLiteDatabase database;

    private Integer RoutesCounter = 1;

    public RecordDiaryFragmentAdapter(Context mcontext) {
        this.context = mcontext;
        inflater = LayoutInflater.from(mcontext);

        helper = DataBaseHelper.getmInstance(context);
        database = helper.getWritableDatabase();
    }

    @Override
    public int getCount() {
        int number = 0;
        Cursor RouteCount_cursor = database.query("trackRoute",
                new String[]{"routesCounter", "track_no", "track_lat", "track_lng",
                        "track_start", "track_title", "track_totaltime", "track_completetime"},
                "track_start=\"0\"", null, null, null, null, null);
        if (RouteCount_cursor != null) {
            if (RouteCount_cursor.getCount() != 0) {
                RouteCount_cursor.moveToLast();
                number = RouteCount_cursor.getInt(0);
            }
            RouteCount_cursor.close();
        }
        return number;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.fragment_record_diarylist, parent, false);
            mViewHolder = new ViewHolder();
            mViewHolder.ImageSlider = (SliderLayout) convertView.findViewById(R.id.slider);
            mViewHolder.pagerIndicator = (PagerIndicator) convertView.findViewById(R.id.custom_indicator);
            mViewHolder.DiaryImage = (ImageView) convertView.findViewById(R.id.DiaryImage);
            mViewHolder.DiaryTitle = (TextView) convertView.findViewById(R.id.Title);
            mViewHolder.DiaryTotalTime = (TextView) convertView.findViewById(R.id.TimeStamp);
            mViewHolder.DiaryString = (TextView) convertView.findViewById(R.id.DiaryString);

            convertView.setTag(mViewHolder);

        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }

        Cursor trackRoute_cursor = database.query("trackRoute",
                new String[]{"routesCounter", "track_no", "track_lat", "track_lng",
                        "track_start", "track_title", "track_totaltime", "track_completetime"},
                "track_start=\"0\"", null, null, null, null, null);
        if (trackRoute_cursor != null) {
            if (trackRoute_cursor.getCount() != 0) {
                trackRoute_cursor.moveToPosition(trackRoute_cursor.getCount() - position-1);
                mViewHolder.DiaryTitle.setText(trackRoute_cursor.getString(5));
                mViewHolder.DiaryTotalTime.setText(trackRoute_cursor.getString(6));
                RoutesCounter = trackRoute_cursor.getInt(0);

                Cursor memo_cursor = database.query("travelmemo", new String[]{"memo_routesCounter", "memo_trackNo",
                                "memo_content", "memo_img", "memo_latlng", "memo_time"},
                        "memo_routesCounter=\"" + RoutesCounter + "\" AND memo_content!=\"null\"", null, null, null, null, null);
                if (memo_cursor != null) {
                    if (memo_cursor.getCount() != 0) {
                        memo_cursor.moveToFirst();
                        mViewHolder.DiaryString.setText(memo_cursor.getString(2));
                    } else {
                        mViewHolder.DiaryString.setText("");
                    }
                    memo_cursor.close();
                }

                Cursor img_cursor = database.query("travelmemo", new String[]{"memo_routesCounter", "memo_trackNo",
                                "memo_content", "memo_img", "memo_latlng", "memo_time"},
                        "memo_routesCounter=\"" + RoutesCounter + "\" AND memo_img!=\"null\"", null, null, null, null, null);
                if (img_cursor != null) {
                    mViewHolder.ImageSlider.removeAllSliders();
                    if (img_cursor.getCount() > 1) {
                        //Log.e("3/28_", "img count:" + number);
                        while (img_cursor.moveToNext()) {
                            //Log.e("3/28_", "img: " + img_cursor.getBlob(3));
                            byte[] d = img_cursor.getBlob(3);
                            DefaultSliderView sliderView = new DefaultSliderView(context);
                            sliderView.image(BitmapFactory.decodeByteArray(d, 0, d.length))
                                    .setScaleType(BaseSliderView.ScaleType.Fit);
                            mViewHolder.ImageSlider.addSlider(sliderView);
                        }
                        mViewHolder.ImageSlider.setCustomIndicator(mViewHolder.pagerIndicator);
                        mViewHolder.ImageSlider.addOnPageChangeListener(this);
                        //Log.e("3/29_", "position: " + position);
                    } else if (img_cursor.getCount() == 1) {
                        img_cursor.moveToFirst();
                        byte[] d = img_cursor.getBlob(3);
                        mViewHolder.DiaryImage.setImageBitmap(BitmapFactory.decodeByteArray(d, 0, d.length));
                        mViewHolder.DiaryImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    } else if (img_cursor.getCount() == 0) {
                        mViewHolder.DiaryImage.setImageResource(R.drawable.empty);
                        mViewHolder.DiaryImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
                        //Log.e("3/28_", "img_cursor = 0 ");
                    }
                    mViewHolder.ImageSlider.stopAutoCycle();
                    img_cursor.close();
                }
            }
            trackRoute_cursor.close();
        }

        return convertView;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private static class ViewHolder {
        SliderLayout ImageSlider;
        PagerIndicator pagerIndicator;
        ImageView DiaryImage;
        TextView DiaryTitle, DiaryTotalTime, DiaryString;
    }
}
