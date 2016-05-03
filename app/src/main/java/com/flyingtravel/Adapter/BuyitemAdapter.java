package com.flyingtravel.Adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.flyingtravel.R;
import com.flyingtravel.Utility.DataBaseHelper;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

/**
 * Created by wei on 2015/12/30.
 */
public class BuyitemAdapter extends BaseAdapter {
    Context context;
    LayoutInflater layoutInflater;
    SharedPreferences sharedPreferences;
    DataBaseHelper helper;
    SQLiteDatabase database;
    ImageLoader loader = ImageLoader.getInstance();
    DisplayImageOptions options;
    private ImageLoadingListener listener;

    public BuyitemAdapter(Context context) {
        this.context = context;
        helper = DataBaseHelper.getmInstance(context);
        database = helper.getWritableDatabase();
        layoutInflater = LayoutInflater.from(context);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        options = new DisplayImageOptions.Builder()
                .showImageOnFail(R.drawable.error)
                .showImageOnLoading(R.drawable.empty)
                .showImageForEmptyUri(R.drawable.empty)
                .cacheInMemory(false)
                .cacheOnDisk(true).build();
        listener = new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String s, View view) {

            }

            @Override
            public void onLoadingFailed(String s, View view, FailReason failReason) {

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
        return sharedPreferences.getInt("InBuyList", 0);
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        final cell newcell;
        View mview;
        mview = layoutInflater.inflate(R.layout.buylist_item, null);
        newcell = new cell(
                (ImageView) mview.findViewById(R.id.buyitemlist_itemImg),
                (LinearLayout) mview.findViewById(R.id.buyitemlist_delImg),
                (TextView) mview.findViewById(R.id.buyitemlist_nameTxt),
                (TextView) mview.findViewById(R.id.buyitemlist_itemTxt),
                (TextView) mview.findViewById(R.id.butitemlist_moneyTxt),
                (TextView) mview.findViewById(R.id.buyitemlist_totalTxt),
                (TextView) mview.findViewById(R.id.buyitemlist_numbertext),
                (LinearLayout) mview.findViewById(R.id.buyitemlist_addbutton),
                (LinearLayout) mview.findViewById(R.id.buyitemlist_minusbutton)
        );
        newcell.cellnumberTxt.setText("0");

        //TODO need modify
        int getPosition = position, getitemPosition = 0, BiginCart = 0;
        Boolean get = false;
        String BigitemID = null, SmallitemID = null, itemName = null, itemImg = null;
        Cursor goods_cursor = database.query("goods", new String[]{"totalCount", "goods_id", "goods_title",
                "goods_url", "goods_money", "goods_content", "goods_click", "goods_addtime"}, null, null, null, null, null);
        if (goods_cursor != null) {
            while (goods_cursor.moveToNext()) {
                if (get) {
//                    Log.i("3.24","我在if裡面!!!要離開while了喔");
                    break;
                } else {
                    BiginCart = sharedPreferences.getInt("InBuyListg" + goods_cursor.getString(1), 0);
                    if (BiginCart > 0) {
                        for (int k = 0; k < BiginCart; k++) {
                            String a = sharedPreferences.getString("InBuyListg" + goods_cursor.getString(1) + "id" + (k + 1), null);
                            if (a != null && getPosition == 0 &&
                                    sharedPreferences.getInt("InBuyListgC" + goods_cursor.getString(1) + "id" + (k + 1), 0) != 0) {
//                            Log.e("3.24", "這就是我要的!!!!" + getPosition + ".." + position+"~~~~~"+goods_cursor.getString(2)+"單位:"+a);
                                BigitemID = goods_cursor.getString(1);
                                itemName = goods_cursor.getString(2);
                                itemImg = goods_cursor.getString(3);
                                getitemPosition = k + 1;
                                SmallitemID = a;
                                get = true;
                                break;
                            } else if (a != null && getPosition != 0 &&
                                    sharedPreferences.getInt("InBuyListgC" + goods_cursor.getString(1) + "id" + (k + 1), 0) != 0) {
                                getPosition--;
//                            Log.e("3.24", "這不是我要的!!!!" + getPosition + "!!!.." + position);
                            }
                        }
                    }
//                else {//這個大項目沒有小項目在購物車裡面
//                    Log.e("3.24", "這不是我要的!!!!" + getPosition + "." + position+"///"+goods_cursor.getString(1));
//                }
//                Log.i("3.24","我在while裡面!!!要執行下一輪");
                }
            }
        }
        if (BigitemID != null && SmallitemID != null) {
            Cursor goods_cursor_big = database.query("goodsitem", new String[]{"goods_bigid",
                            "goods_itemid", "goods_title", "goods_money", "goods_url"},
                    "goods_bigid=? and goods_itemid=?", new String[]{BigitemID, SmallitemID}, null, null, null);
            if (goods_cursor_big != null && goods_cursor_big.getCount() > 0) {
//                Log.e("3.24","~~~~~~~~~~~~找到你啦!!!");
                goods_cursor_big.moveToFirst();
                newcell.cellnameTxt.setText(itemName);
                newcell.cellfromTxt.setText(goods_cursor_big.getString(2));
                newcell.cellmoneyTxt.setText(goods_cursor_big.getString(3));
                newcell.cellnumberTxt.setText(sharedPreferences.getInt("InBuyListgC" + BigitemID + "id" + getitemPosition, 0) + "");
                if (itemImg.startsWith("http:"))
                    loader.displayImage(itemImg, newcell.cellImg, options, listener);
                else
                    loader.displayImage("http://zhiyou.lin366.com/" + itemImg, newcell.cellImg, options, listener);

//                Log.i("3.24", " bigID " + goods_cursor_big.getString(0));
//                Log.i("3.24", " itemID " + goods_cursor_big.getString(1));
//                Log.i("3.24", " title " + goods_cursor_big.getString(2));
//                Log.i("3.24", " money " + goods_cursor_big.getString(3));
                goods_cursor_big.close();
            }
        }


        /////TODO^^^^^^-----

        final int[] howmany = {sharedPreferences.getInt("InBuyList", 0)};
        int itemPosition = sharedPreferences.getInt("InBuyList" + (position + 1), 0);

        if (goods_cursor != null)
            goods_cursor.close();


        final String finalBigitemID = BigitemID;
        final int finalGetitemPosition = getitemPosition;
        final int[] finalBiginCart = {BiginCart};
        final String finalSmallitemID = SmallitemID;
        newcell.plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                if (Integer.valueOf(newcell.cellnumberTxt.getText().toString() + "") == 0) {
                    howmany[0]++;
                    finalBiginCart[0]++;
                    editor.putInt("InBuyList", howmany[0]);//the total count of the cart
                    editor.putInt("InBuyListg" + finalBigitemID, finalBiginCart[0]);//這個大項目裡面有幾個小項目在購物車裡
                }

                newcell.cellnumberTxt.setText((Integer.valueOf(newcell.cellnumberTxt.getText().toString() + "") + 1) + "");
                editor.putInt("InBuyListgC" + finalBigitemID + "id" + finalGetitemPosition, Integer.valueOf(newcell.cellnumberTxt.getText().toString()));
                editor.apply();
                newcell.celltotalTxt.setText(Integer.parseInt(newcell.cellmoneyTxt.getText().toString())
                        * Integer.valueOf(newcell.cellnumberTxt.getText().toString() + "") + "");
            }
        });

        newcell.minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((Integer.valueOf(newcell.cellnumberTxt.getText().toString()) > 1)) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    int beforeClickCount = (Integer.valueOf(newcell.cellnumberTxt.getText().toString()));
                    newcell.cellnumberTxt.setText((Integer.valueOf(newcell.cellnumberTxt.getText().toString()) - 1) + "");
                    if (beforeClickCount - 1 == 0) {
                        howmany[0]--;
                        if (finalBiginCart[0] > 0)
                            finalBiginCart[0]--;
                        else Log.e("3.24", "~!~!~!~!ERROR!!!!!!!!");
                        editor.putInt("InBuyList", howmany[0]);//the total count of the cart
                        editor.putInt("InBuyListg" + finalBigitemID, finalBiginCart[0]);//這個大項目裡面有幾個小項目在購物車裡
                        editor.putString("InBuyListg" + finalBigitemID + "id" + finalGetitemPosition, finalSmallitemID);//第幾個小項目的id
                        editor.putInt("InBuyListgC" + finalBigitemID + "id" + finalGetitemPosition,
                                Integer.valueOf(newcell.cellnumberTxt.getText().toString()));//第幾個小項目的數量
                        editor.apply();

                    } else {
                        editor.putInt("InBuyListgC" + finalBigitemID + "id" + finalGetitemPosition, Integer.valueOf(newcell.cellnumberTxt.getText().toString()));
                        editor.apply();
                    }
                    newcell.celltotalTxt.setText(Integer.parseInt(newcell.cellmoneyTxt.getText().toString())
                            * Integer.valueOf(newcell.cellnumberTxt.getText().toString() + "") + "");
                }
            }
        });
        ///1.13


        newcell.celltotalTxt.setText(Integer.parseInt(newcell.cellmoneyTxt.getText().toString())
                * Integer.valueOf(newcell.cellnumberTxt.getText().toString() + "") + "");

        //delete chosed item
        newcell.celldelImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newcell.celltotalTxt.setText("0");
                newcell.cellnumberTxt.setText("0");
                howmany[0]--;
                if (finalBiginCart[0] > 0)
                    finalBiginCart[0]--;
//                else Log.e("3.24", "~!~!~!~!ERROR!!!!!!!!");
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("InBuyList", howmany[0]);//the total count of the cart
                editor.putInt("InBuyListg" + finalBigitemID, finalBiginCart[0]);//這個大項目裡面有幾個小項目在購物車裡
                editor.putString("InBuyListg" + finalBigitemID + "id" + finalGetitemPosition, finalSmallitemID);//第幾個小項目的id
                editor.putInt("InBuyListgC" + finalBigitemID + "id" + finalGetitemPosition, 0);//第幾個小項目的數量
                editor.apply();
            }
        });

        return mview;
    }

    public class cell {
        ImageView cellImg;
        TextView cellnameTxt, cellfromTxt, cellmoneyTxt, celltotalTxt, cellnumberTxt;
        LinearLayout plus, minus, celldelImg;

        public cell(ImageView itemImg, LinearLayout itemdelImg, TextView itemnameTxt, TextView itemfromTxt,
                    TextView itemmoneyTxt, TextView itemtotalTxt,
                    TextView itemnumberTxt, LinearLayout mPlus, LinearLayout mMinus) {
            this.cellImg = itemImg;
            this.celldelImg = itemdelImg;
            this.cellnameTxt = itemnameTxt;
            this.cellfromTxt = itemfromTxt;
            this.cellmoneyTxt = itemmoneyTxt;
            this.celltotalTxt = itemtotalTxt;
            this.cellnumberTxt = itemnumberTxt;
            this.plus = mPlus;
            this.minus = mMinus;
        }
    }
}
