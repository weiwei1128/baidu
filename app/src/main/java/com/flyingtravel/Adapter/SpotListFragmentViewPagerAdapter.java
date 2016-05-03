package com.flyingtravel.Adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.flyingtravel.Fragment.SpotListViewFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tinghua on 2016/3/25.
 */
public class SpotListFragmentViewPagerAdapter extends PagerAdapter implements ViewPager.OnPageChangeListener {
    Context context;
    List<Fragment> fragments;
    FragmentManager fragmentManager;
    //ViewPager viewPager;

    int pageNo = 0;
    int currentPageIndex = 0;

    public SpotListFragmentViewPagerAdapter(FragmentManager mfragmentmanager, int pages) {
        fragmentManager = mfragmentmanager;
        pageNo = pages;
        fragments = new ArrayList<Fragment>();

        for (int i = 0; i < pageNo; i++)
            fragments.add(SpotListViewFragment.newInstance("SpotListViewFragment"+(i+1), i+1));
    }

    @Override
    public int getCount() {
        //Log.e("3/23_", "SpotListFragmentViewPagerAdapter: pages " + pageNo);
        return fragments.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        //Log.e("3/23","onPageScrolled"+position);
    }

    @Override
    public void onPageSelected(int position) {
        //Log.e("3/23","onPageSelected"+position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        //Log.e("3/23","onPageScrollStateChanged"+state);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
//        Log.e("4/1_ListPagerAdapter", "destroyItem");
        container.removeView(fragments.get(position).getView());
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment m_fragment = fragments.get(position);
        if (!m_fragment.isAdded()) {
            FragmentTransaction ft = fragmentManager.beginTransaction();
            ft.add(m_fragment, m_fragment.getClass().getSimpleName());
            ft.commit();
            fragmentManager.executePendingTransactions();
        }
        if (m_fragment.getView() == null || m_fragment.getView().getParent() == null)
            container.addView(m_fragment.getView());
        return m_fragment.getView();
    }

    public Integer getCurrentPosition() {
        return currentPageIndex;
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        //Log.e("3/23","fragments!!"+fragments.size());
        if(fragments.size() > 0) {
            fragments.get(currentPageIndex).onStop();
            if (fragments.get(position).isAdded())
                fragments.get(position).onResume();
            currentPageIndex = position;
        }
//        Log.d("4/1", "currrentPage:" + position);
        super.setPrimaryItem(container, position, object);
    }
}
