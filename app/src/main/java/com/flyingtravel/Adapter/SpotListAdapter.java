package com.flyingtravel.Adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.flyingtravel.Activity.Spot.SpotData;
import com.flyingtravel.R;
import com.flyingtravel.Utility.GlobalVariable;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by Tinghua on 2015/11/26.
 * Updated by Tinghua on 2016/03/08.
 */
public class SpotListAdapter extends BaseAdapter implements Filterable {

    private ImageLoader loader = ImageLoader.getInstance();
    private DisplayImageOptions options;
    private ImageLoadingListener listener;

    private LayoutInflater inflater;
    private ViewHolder mViewHolder;

    private Context context;
    private int index = 0;

    private GlobalVariable globalVariable;
    private ArrayList<SpotData> mSpotsData;
    private ArrayList<SpotData> mFilteredSpots;

    private SpotFilter mFilter = new SpotFilter();

    public SpotListAdapter(Context mcontext, Integer pageNO) {
        this.context = mcontext;
        this.index = pageNO-1;
        inflater = LayoutInflater.from(mcontext);

        globalVariable = (GlobalVariable) context.getApplicationContext();
        mSpotsData = new ArrayList<SpotData>();

        if (!globalVariable.SpotDataSorted.isEmpty()) {
            if (globalVariable.SpotDataSorted.size() / 10 == pageNO) {
                mSpotsData.addAll(globalVariable.SpotDataSorted
                        .subList(0 + index * 10, globalVariable.SpotDataSorted.size() % 10 + 1 + index * 10));
            } else {
                mSpotsData.addAll(globalVariable.SpotDataSorted.subList(0+index * 10, 10+index*10));
            }
            mFilteredSpots = new ArrayList<SpotData>();
            mFilteredSpots = mSpotsData;
            //Log.e("3/23_", "SpotListAdapter: mFilteredSpots.size " + mFilteredSpots.size());
            //Log.e("4/1_", "SpotListAdapter: mPosition " + index);
        }

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
                ImageView imageView = (ImageView) view.findViewById(R.id.SpotImg);
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
        int count = 0;
        if (!mFilteredSpots.isEmpty()) {
            count = mFilteredSpots.size();
        } else if (!globalVariable.SpotDataSorted.isEmpty()) {
            if (globalVariable.SpotDataSorted.size() / 10 == index) {
                count = globalVariable.SpotDataSorted.size() % 10;
            } else {
                count = 10;
            }
        }
        //Log.e("3/23_", "SpotListAdapter: count " + count);
        return count;
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
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.spot_item, parent, false);
            mViewHolder = new ViewHolder();
            mViewHolder.SpotImg = (ImageView) convertView.findViewById(R.id.SpotImg);
            mViewHolder.SpotName = (TextView) convertView.findViewById(R.id.SpotName);
            mViewHolder.SpotAddress = (TextView) convertView.findViewById(R.id.SpotAddress);
            mViewHolder.SpotDistance = (TextView) convertView.findViewById(R.id.SpotDistance);
            mViewHolder.SpotOpenTime = (TextView) convertView.findViewById(R.id.SpotOpenTime);

            convertView.setTag(mViewHolder);

        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }

        String ImgString = mFilteredSpots.get(position).getPicture1();
        if (!ImgString.endsWith(".jpg"))
            ImgString = null;
        //Log.e("3/25_**** ", "ImgString: " + ImgString);
        loader.displayImage(ImgString, mViewHolder.SpotImg, options, listener);
        mViewHolder.SpotName.setText(mFilteredSpots.get(position).getName());
        mViewHolder.SpotAddress.setText(mFilteredSpots.get(position).getAdd());
        mViewHolder.SpotDistance.setText(DistanceText(mFilteredSpots.get(position).getDistance()));
        if (mFilteredSpots.get(position).getOpenTime() == null) {
            mViewHolder.SpotOpenTime.setText("開放時間：無");
        } else {
            mViewHolder.SpotOpenTime.setText("開放時間：" + mFilteredSpots.get(position).getOpenTime());
        }

        //Log.e("3/23_", "SpotListAdapter: show list");

        return convertView;
    }

    private static class ViewHolder {
        ImageView SpotImg;
        TextView SpotName, SpotAddress, SpotDistance, SpotOpenTime;
    }

    @Override
    public Filter getFilter() {
        if (mFilter == null)
            mFilter = new SpotFilter();
        return mFilter;
    }

    private class SpotFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            FilterResults results = new FilterResults();
            if (constraint == "" || constraint.length() == 0) {
                // No filter implemented we return all the list
                results.values = mSpotsData;
                results.count = mSpotsData.size();
                //Log.e("4/1_", "沒打字時 results.count: " + results.count);
            } else {
                ArrayList<SpotData> FilteredSpots = new ArrayList<SpotData>();

                for (SpotData spotData : mSpotsData) {
                    if (spotData.getName().toUpperCase().contains(constraint.toString().toUpperCase()))//.startsWith(constraint.toString().toUpperCase()))
                        FilteredSpots.add(spotData);
                }
                results.values = FilteredSpots;
                results.count = FilteredSpots.size();
                //Log.e("4/1_", "有打字時 results.count: " + results.count);
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            // Now we have to inform the adapter about the new list filtered
            if (results.count == 0) {
                Toast.makeText(context, "無此景點！", Toast.LENGTH_LONG).show();
                notifyDataSetInvalidated();
                //Log.e("4/1_", "沒東西時 results.count: " + results.count);
            } else {
                mFilteredSpots = (ArrayList<SpotData>) results.values;
                notifyDataSetChanged();
                //Log.e("4/1_", "有東西時 results.count: " + results.count);
            }
        }
    }

    //帶入距離回傳字串 (距離小於一公里以公尺呈現，距離大於一公里以公里呈現並取小數點兩位)
    private String DistanceText(double distance) {
        if (distance < 1000) return String.valueOf((int) distance) + "m";
        else return new DecimalFormat("#.00").format(distance / 1000) + "km";
    }
}
