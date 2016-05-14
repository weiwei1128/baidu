package com.flyingtravel.Fragment;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.flyingtravel.Activity.MoreItemActivity;
import com.flyingtravel.Adapter.MoreAdapter;
import com.flyingtravel.Utility.Functions;
import com.flyingtravel.Utility.GlobalVariable;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

public class MoreFragment extends Fragment {
    /*GA*/
    public static Tracker tracker;

    public MoreFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /**GA**/
        GlobalVariable globalVariable = (GlobalVariable) getActivity().getApplication();
        tracker = globalVariable.getDefaultTracker();
        /**GA**/
    }

    @Override
    public void onResume() {
        super.onResume();
        /**GA**/
        tracker.setScreenName("更多");
        tracker.send(new HitBuilders.ScreenViewBuilder().build());
        /**GA**/
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
//        Log.i("4.7", "activity0:" + getActivity().getSupportFragmentManager()+"list"+getActivity().getSupportFragmentManager().getFragments());
        ListView listView = new ListView(getActivity());
        MoreAdapter adapter = new MoreAdapter(getActivity());
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Bundle bundle = new Bundle();
                switch (position) {
                    //顯示不同的頁面
                    case 0:
                        bundle.putInt("position", 0);
                        break;
                    case 1:
                        bundle.putInt("position", 1);
                        break;
                }
                Functions.go(false, getActivity(), getActivity(), MoreItemActivity.class, bundle);
            }
        });
        return listView;
    }


}
