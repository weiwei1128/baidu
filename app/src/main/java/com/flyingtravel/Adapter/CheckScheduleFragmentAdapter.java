package com.flyingtravel.Adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by wei on 2016/4/26.
 */
public class CheckScheduleFragmentAdapter extends PagerAdapter implements ViewPager.OnPageChangeListener {

    List<Fragment> fragments;
    FragmentManager fragmentManager;
    ViewPager viewPager;
    int currentPageIndex = 0;
    Context context;

    public CheckScheduleFragmentAdapter(FragmentManager mfragmentmanager, ViewPager mviewpager,
                                        List<Fragment> mfragments,
                                        Context mcontext) {
        this.fragmentManager = mfragmentmanager;
        this.viewPager = mviewpager;
        this.fragments = mfragments;
        this.context = mcontext;
    }

    @Override
    public int getCount() {
        if (fragments != null)
            return fragments.size();
        else return 0;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(fragments.get(position).getView());
//        super.destroyItem(container, position, object);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
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

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        if (fragments.size() > 0) {
            fragments.get(currentPageIndex).onStop();
            if (fragments.get(position).isAdded())
                fragments.get(position).onResume();
            currentPageIndex = position;
        }
//        Log.d("1/12", "currrentPage:" + position);
        super.setPrimaryItem(container, position, object);

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
