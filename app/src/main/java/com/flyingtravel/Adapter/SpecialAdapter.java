package com.flyingtravel.Adapter;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.flyingtravel.R;
import com.flyingtravel.Utility.DataBaseHelper;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

/**
 * Created by wei on 2016/1/30.
 */
public class SpecialAdapter extends BaseAdapter {
    ImageLoader loader = ImageLoader.getInstance();
    DisplayImageOptions options;
    DataBaseHelper helper;
    SQLiteDatabase database;
    Context mContext;
    LayoutInflater layoutInflater;
    int page_no;
    private ImageLoadingListener listener;

    public SpecialAdapter(Context context, Integer pageNo) {
        this.mContext = context;
        layoutInflater = LayoutInflater.from(context);
        this.page_no = pageNo;
        helper = DataBaseHelper.getmInstance(context);
        database = helper.getWritableDatabase();

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
                ImageView imageView = (ImageView) view.findViewById(R.id.special_img);
                loader.displayImage(null, imageView, options, listener);
            }

            @Override
            public void onLoadingComplete(String s, View view, Bitmap bitmap) {

            }

            @Override
            public void onLoadingCancelled(String s, View view) {

            }
        };
    }

    @Override
    public int getCount() {
        int number = 0;
        Cursor special = database.query("special_activity", new String[]{"special_id", "title", "img", "content", "price", "click"},
                null, null, null, null, null);
        if (special != null) {
            number = special.getCount();
            special.close();
        }
        if ((number % 10 > 0))
            if (number / 10 + 1 == page_no)
                number = number % 10;
            else number = 10;
        else number=10;
        return number;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        thing item;

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.special_item, null);
            item = new thing(
                    (ImageView) convertView.findViewById(R.id.special_img),
                    (TextView) convertView.findViewById(R.id.special_name_text),
                    (TextView) convertView.findViewById(R.id.special_price_text)
            );
            convertView.setTag(item);
        } else
            item = (thing) convertView.getTag();

        Cursor special = database.query("special_activity", new String[]{"special_id",
                        "title", "img", "content", "price", "click"},
                null, null, null, null, null);
        if (special != null && special.getCount() >= ((page_no - 1) * 10 + position)) {
            special.moveToPosition((page_no - 1) * 10 + position);
            if (special.getString(1) != null)
                item.name.setText(special.getString(1));
            if (special.getString(4) != null)
                item.what.setText(mContext.getResources().getString(R.string.price_text)+ special.getString(4));
            if (special.getString(2) != null)
                if (special.getString(2).startsWith("http:"))
                    loader.displayImage(special.getString(2), item.m_img, options, listener);
                else loader.displayImage("http://zhiyou.lin366.com/" + special.getString(2)
                        , item.m_img, options, listener);
        }
        if (special != null)
            special.close();

        return convertView;
    }

    public class thing {
        ImageView m_img;
        TextView name, what;

        public thing(ImageView imageView, TextView nameTxt, TextView whatTxt) {
            this.m_img = imageView;
            this.name = nameTxt;
            this.what = whatTxt;
        }
    }
}
