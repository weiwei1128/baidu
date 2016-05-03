package com.flyingtravel.Adapter;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.flyingtravel.R;
import com.flyingtravel.Utility.DataBaseHelper;

/**
 * Created by wei on 2016/3/7.
 */
public class ShopRecordAdapter extends BaseAdapter {

    private ImageLoadingListener listener;
    LayoutInflater layoutInflater;
    Context m_context;
    DataBaseHelper helper;
    SQLiteDatabase database;
    String UserId;

    public ShopRecordAdapter(Context context, String UserId) {
        layoutInflater = LayoutInflater.from(context);
        this.m_context = context;
        this.UserId = UserId;
        helper = DataBaseHelper.getmInstance(context);
        database = helper.getReadableDatabase();
    }

    @Override
    public int getCount() {
        int number = 0;
        Cursor order_cursor = database.query("shoporder", new String[]{"order_id",
                        "order_userid ", "order_no",
                        "order_time", "order_name", "order_phone", "order_email",
                        "order_money", "order_state", "order_schedule"},
                "order_userid=" + "\"" + UserId + "\"", null, null, null, null, null);
        if (order_cursor != null) {
            number = order_cursor.getCount();
            order_cursor.close();
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
        item item;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.shoprecord_item, null);
            item = new item(
                    (TextView) convertView.findViewById(R.id.shoprecorditem_no),
                    (TextView) convertView.findViewById(R.id.shoprecorditem_date),
                    (TextView) convertView.findViewById(R.id.shoprecorditem_money),
                    (TextView) convertView.findViewById(R.id.shoprecorditem_content),
                    (TextView) convertView.findViewById(R.id.shoprecorditem_state)
            );
            convertView.setTag(item);
        } else
            item = (item) convertView.getTag();

        Cursor order_cursor = database.query("shoporder", new String[]{"order_id", "order_userid ", "order_no",
                "order_time", "order_name", "order_phone", "order_email", "order_money",
                "order_state", "order_schedule"}, "order_userid=" + "\"" + UserId + "\"", null, null, null, null);
        if (order_cursor != null && order_cursor.getCount() >= position) {
            order_cursor.moveToPosition(position);
            if (order_cursor.getString(2) != null)
                item.order_no.setText(order_cursor.getString(2));
            if (order_cursor.getString(3) != null)
                item.order_date.setText(order_cursor.getString(3));
            if (order_cursor.getString(4) != null)
                item.order_info.setText(m_context.getResources().getString(R.string.addressee_textColon) + order_cursor.getString(4));
//            if (order_cursor.getString(5) != null)
//                item.order_info.append("\n電話: " + order_cursor.getString(5));
            if (order_cursor.getString(7) != null)
                item.order_money.setText("$" + order_cursor.getString(7));
            if (order_cursor.getString(8) != null)
                item.order_state.setText(order_cursor.getString(8));
        }
        if (order_cursor != null)
            order_cursor.close();


        return convertView;
    }

    public class item {
        TextView order_no, order_date, order_money, order_info, order_state;

        item(TextView Order_no, TextView Order_date, TextView Order_money, TextView Order_info,
             TextView Order_state) {
            this.order_no = Order_no;
            this.order_date = Order_date;
            this.order_money = Order_money;
            this.order_info = Order_info;
            this.order_state = Order_state;
        }
    }
}
