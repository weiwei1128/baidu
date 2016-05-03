package com.flyingtravel.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.flyingtravel.R;

/**
 * Created by wei on 2016/4/7.
 * 更多頁面更新
 */

public class MoreAdapter extends BaseAdapter {
    Context context;
    LayoutInflater layoutInflater;

    public  MoreAdapter(Context context){
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return 2;
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
        if(convertView==null){
            convertView = layoutInflater.inflate(R.layout.morelist_item,null);
            item = new item((TextView)convertView.findViewById(R.id.more_list_text));
            convertView.setTag(item);
        }
        else item = (item)convertView.getTag();

        switch (position){
            case 0:
                item.itemName.setText(context.getResources().getString(R.string.aboutUs_text));
                break;
            case 1:
                item.itemName.setText(context.getResources().getString(R.string.planschedule_text));
                break;
        }


        return convertView;
    }

    public class item{
        TextView itemName;
        public item(TextView itemName){
            this.itemName = itemName;
        }
    }
}
