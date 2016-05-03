package com.flyingtravel.Adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.flyingtravel.R;

import java.util.List;


/**
 * Created by Tinghua on 2016/3/26.
 */
public class RecordFragmentPagerAdapter extends FragmentPagerAdapter {
    private List<Fragment> fragments;
    Context context;
    private String tabTitles[] ;
    public RecordFragmentPagerAdapter(FragmentManager fm,List<Fragment> fragments,Context context) {
        super(fm);
        this.fragments = fragments;
        this.context = context;
        tabTitles= new String[] {context.getResources().getString(R.string.track_text),
                context.getResources().getString(R.string.trackmemo_text)};
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        return tabTitles[position];
    }
}
