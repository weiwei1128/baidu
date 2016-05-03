package com.flyingtravel.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.flyingtravel.R;
import com.flyingtravel.Utility.DataBaseHelper;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

/**
 * Created by wei on 2015/11/11.
 */
public class BuyAdapter extends BaseAdapter {
    ImageLoader loader = ImageLoader.getInstance();
    DisplayImageOptions options;
    private ImageLoadingListener listener;
    DataBaseHelper helper;
    SQLiteDatabase database;

    private Context context;
    private LayoutInflater inflater;

    int pageNO = 0;

    GridView gridView;

    public BuyAdapter(Context mcontext, Integer index, GridView gridView) {
        this.context = mcontext;
        inflater = LayoutInflater.from(mcontext);
        this.gridView = gridView;

        pageNO = index;
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
                ImageView imageView = (ImageView) view.findViewById(R.id.buy_thingImg);
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
        Cursor goods_cursor = database.query("goods", new String[]{"totalCount", "goods_id", "goods_title",
                "goods_url", "goods_money", "goods_content", "goods_click", "goods_addtime"}, null, null, null, null, null);
        if (goods_cursor != null) {
            number = goods_cursor.getCount();
            goods_cursor.close();
        }
        if ((number % 10 > 0)) {
            if (number / 10 + 1 == pageNO) {
                number = number % 10;
            } else number = 10;
        } else
            number = 10;
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

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        cell mcell;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.buy_item, null);
            mcell = new cell(
                    (ImageView) convertView.findViewById(R.id.buy_thingImg),
                    (TextView) convertView.findViewById(R.id.buy_thingText),
                    (TextView) convertView.findViewById(R.id.buything_clickText),
                    (TextView) convertView.findViewById(R.id.buy_thingMoney)
            );
            convertView.setTag(mcell);
        } else
            mcell = (cell) convertView.getTag();


        Cursor goods_cursor = database.query("goods", new String[]{"totalCount", "goods_id", "goods_title",
                "goods_url", "goods_money", "goods_content", "goods_click", "goods_addtime"}, null, null, null, null, null);

        if (goods_cursor != null && goods_cursor.getCount() >= (pageNO - 1) * 10 + position) {
            goods_cursor.moveToPosition((pageNO - 1) * 10 + position);

            if (goods_cursor.getString(2) != null)
                mcell.buyText.setText(goods_cursor.getString(2));
            else mcell.buyText.setText(R.string.wrongData_text);
            if (goods_cursor.getString(4) != null)
                mcell.moneyText.setText("$" + goods_cursor.getString(4));

//            if (!(mcell.clickText.getText().toString().substring(3).startsWith("0") &&
//                    mcell.clickText.getText().toString().endsWith("0")))//避免出現:00
                if (goods_cursor.getString(6) != null) {
                    Log.d("4.25", "BuyAdapter:" + goods_cursor.getString(6));
                    String click = context.getResources().getString(R.string.buyClick_text);
                    mcell.clickText.setText(click + goods_cursor.getString(6));
                }

            if (goods_cursor.getString(3) != null)
                if (goods_cursor.getString(3).startsWith("http:"))
                    loader.displayImage(goods_cursor.getString(3)
                            , mcell.buyImg, options, listener);
                else
                    loader.displayImage("http://zhiyou.lin366.com/" + goods_cursor.getString(3)
                            , mcell.buyImg, options, listener);

        }

        if (goods_cursor != null)
            goods_cursor.close();

        return convertView;
    }

    public class cell {
        ImageView buyImg;
        TextView buyText, clickText, moneyText;

        public cell(ImageView buy_img, TextView buy_text, TextView click_Text, TextView money_Text) {
            this.buyImg = buy_img;
            this.buyText = buy_text;
            this.clickText = click_Text;
            this.moneyText = money_Text;
        }

    }


    /**
     * Update certain view
     * [BAD] whole adapter wont update
     *
     * @param ItemIndex item position in DB
     * @param position item position in GridView
     *
     *
     */
    public void UpdateView(int ItemIndex, int position) {
        int visiblePosition = gridView.getFirstVisiblePosition();
//        Log.e("4.25", "UpdateView" + ItemIndex + "visiblePosition" + visiblePosition + "position" + position);
        View view = gridView.getChildAt(position - visiblePosition);
        cell mcell = new cell(
                (ImageView) view.findViewById(R.id.buy_thingImg),
                (TextView) view.findViewById(R.id.buy_thingText),
                (TextView) view.findViewById(R.id.buything_clickText),
                (TextView) view.findViewById(R.id.buy_thingMoney)
        );
        Cursor goods_cursor = database.query("goods", new String[]{"totalCount", "goods_id", "goods_title",
                "goods_url", "goods_money", "goods_content", "goods_click", "goods_addtime"}, null, null, null, null, null);

        if (goods_cursor != null && goods_cursor.getCount() >= ItemIndex) {
            goods_cursor.moveToPosition(ItemIndex);
            if (goods_cursor.getString(6) != null) {
                Log.e("4.25", "2222 BuyAdapter:" + goods_cursor.getString(6));
                String click = context.getResources().getString(R.string.buyClick_text);
                mcell.clickText.setText(click + goods_cursor.getString(6));
            }
//            else {
//                Log.e("4.25", "2222 BuyAdapter: NULL");
//            }

        }
//        else
//            Log.e("4.25","UpdateView NULL!");
//        Log.e("4.25", "UpdateView" + mcell.buyText.getText());
        if (goods_cursor != null)
            goods_cursor.close();

    }
}
