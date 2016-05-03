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
 * Created by wei on 2016/3/10.
 */
public class SpecialFragmentViewPagerAdapter
        extends PagerAdapter
{
    List<Fragment> fragments;
    FragmentManager fragmentManager;
    ViewPager viewPager;
    int currentPageIndex = 0;
    Context context;


    public SpecialFragmentViewPagerAdapter(FragmentManager fragment_Manager, ViewPager view_Pager,
                                           List<Fragment> m_fragments, Context mContext) {
        this.fragmentManager = fragment_Manager;
        this.viewPager = view_Pager;
        this.fragments = m_fragments;
        this.context = mContext;
    }

    @Override
    public int getCount() {
        if (fragments != null)
            return fragments.size();
        else return 0;
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
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(fragments.get(position).getView());
//        super.destroyItem(container, position, object);
    }
}
