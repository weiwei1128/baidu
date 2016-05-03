package com.flyingtravel.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.flyingtravel.R;

/**
 * Created by wei on 2016/3/7.
 */
public class CheckScheduleAdapter extends BaseAdapter {
    LayoutInflater layoutInflater;
    int count = 0;

    String[] id, no, date, price, content, state;

    public CheckScheduleAdapter(Context context, int Count, String[] id, String[] no, String[] date,
                                String[] price, String[] content, String[] state) {
        layoutInflater = LayoutInflater.from(context);
        this.count = Count;
        this.id = id;
        this.no = no;
        this.date = date;
        this.price = price;
        this.content = content;
        this.state = state;
    }

    public String getWebviewId(int position) {
        return id[position];
    }

    @Override
    public int getCount() {
        return count;
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
        item mitem;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.checkschedule_item, null);
            mitem = new item((TextView) convertView.findViewById(R.id.shoprecorditem_no),
                    (TextView) convertView.findViewById(R.id.shoprecorditem_date),
                    (TextView) convertView.findViewById(R.id.shoprecorditem_money),
                    (TextView) convertView.findViewById(R.id.shoprecorditem_content),
                    (TextView) convertView.findViewById(R.id.shoprecorditem_state));
            convertView.setTag(mitem);
        } else {
            mitem = (item) convertView.getTag();
        }
        if (no[position] != null)
            mitem.no.setText(no[position]);
        if (date[position] != null)
            mitem.date.setText(date[position]);
        if (price[position] != null)
            mitem.price.setText(price[position]);
        if (content[position] != null)
            mitem.content.setText(content[position]);
        if (state[position] != null)
            mitem.state.setText(state[position]);


        return convertView;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    public class item {
        TextView no, date, price, content, state;

        public item(TextView no, TextView date, TextView price, TextView content, TextView state) {
            this.no = no;
            this.date = date;
            this.price = price;
            this.content = content;
            this.state = state;

        }
    }
}
