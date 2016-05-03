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
 * Created by wei on 2016/3/7.
 * 左滑右邊滑
 */
public class BuyFragmentViewPagerAdapter extends PagerAdapter implements ViewPager.OnPageChangeListener {
    List<Fragment> fragments;
    FragmentManager fragmentManager;
    ViewPager viewPager;
    int currentPageIndex = 0;
    Context context;

    public BuyFragmentViewPagerAdapter(FragmentManager mfragmentmanager, ViewPager mviewpager,
                                       List<Fragment> mfragments,
                                       Context mcontext) {

        this.fragmentManager = mfragmentmanager;
        this.viewPager = mviewpager;
        this.fragments = mfragments;
        this.context = mcontext;
//        viewPager.setOnPageChangeListener(this);
    }

    @Override
    public int getCount() {
//        Log.d("3.7", "fragmentsize" + fragments.size());
        if (fragments != null)
            return fragments.size();
        else return 0;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//        Log.e("3.8","onPageScrolled"+position);
    }

    @Override
    public void onPageSelected(int position) {
//        Log.e("3.8", "onPageSelected" + position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
//        Log.e("3.8", "onPageScrollStateChanged" + state);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(fragments.get(position).getView());
//        super.destroyItem(container, position, object);
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
//        Log.e("3.8","fragments!!"+fragments.size());
        if (fragments.size() > 0) {
            fragments.get(currentPageIndex).onStop();
            if (fragments.get(position).isAdded())
                fragments.get(position).onResume();
            currentPageIndex = position;
        }
//        Log.d("1/12", "currrentPage:" + position);
        super.setPrimaryItem(container, position, object);
    }
}
