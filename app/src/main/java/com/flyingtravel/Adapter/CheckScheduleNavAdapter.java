package com.flyingtravel.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.flyingtravel.R;

/**
 * Created by wei on 2016/4/30.
 */
public class CheckScheduleNavAdapter extends BaseAdapter {
    int count = 0, itemcount = 0;
    String[] title, address;
    Context context;
    LayoutInflater layoutInflater;

    public CheckScheduleNavAdapter(Context context, int totalcount,
//                                   int itemcount,
                                   String[] title, String[] address) {
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
        this.count = totalcount;
//        this.itemcount = itemcount;
        this.title = title;
        this.address = address;
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
        item item;
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.checkschedulenav_item, null);
            item = new item((TextView) convertView.findViewById(R.id.schedule_titleTxt),
                    (LinearLayout) convertView.findViewById(R.id.schedule_navLayout));
            convertView.setTag(item);
        } else
            item = (item) convertView.getTag();
        if (address == null)
            item.navLayout.setVisibility(View.INVISIBLE);
        if (title.length >= position)
            item.title.setText(title[position]);
        return convertView;
    }

    public class item {
        TextView title;
        LinearLayout navLayout;

        public item(TextView title, LinearLayout navLayout) {
            this.title = title;
            this.navLayout = navLayout;
        }
    }
}
