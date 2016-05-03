package com.flyingtravel.Fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.flyingtravel.Activity.MoreItemActivity;
import com.flyingtravel.Adapter.MoreAdapter;
import com.flyingtravel.Utility.Functions;

/**
 * A simple {@link Fragment} subclass.
 */
public class MoreFragment extends Fragment {


    public MoreFragment() {
        // Required empty public constructor
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
                    case 0:
                        bundle.putInt("position", 0);
                        break;
                    case 1:
                        bundle.putInt("position", 1);
                        break;
                }
                Functions.go(false,getActivity(),getActivity(),MoreItemActivity.class,bundle);
            }
        });
        /*
        final ProgressDialog dialog = new ProgressDialog(getActivity());
        dialog.setMessage("載入中");
        dialog.show();
        WebView webView = new WebView(getActivity());
        String myURL = "http://zhiyou.lin366.com/help.aspx?tid=84";

        WebSettings websettings = webView.getSettings();
        websettings.setSupportZoom(true);
        websettings.setBuiltInZoomControls(true);
        websettings.setJavaScriptEnabled(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                dialog.dismiss();
            }
        });
        webView.loadUrl(myURL);
        */
        return listView;
    }


}
