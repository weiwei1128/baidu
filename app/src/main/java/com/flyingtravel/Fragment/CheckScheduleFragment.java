package com.flyingtravel.Fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.flyingtravel.Adapter.CheckScheduleNavAdapter;
import com.flyingtravel.R;

public class CheckScheduleFragment extends Fragment {
    String[] data = new String[5];
    int count = 0;
    String[] getsummary, getaddress;
    Context context;
    Activity activity;
    ListView gridView;
    CheckScheduleNavAdapter adapter;

    public CheckScheduleFragment() {
        // Required empty public constructor
    }

//05020502!!!!
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Log.e("4.26", "-------onCreate");
        data[0] = getArguments().getString("scheduleday");
        data[1] = getArguments().getString("scheduledate");
        data[2] = getArguments().getString("scheduletime");

        if (getArguments().containsKey("schedulecount")) {
            count = getArguments().getInt("schedulecount");
            getsummary = new String[count];
            getaddress = new String[count];
            for (int i = 0; i < count; i++) {
                if (getArguments().containsKey("schedulesummary" + i)) {
                    getsummary[i] = getArguments().getString("schedulesummary" + i);
                    getaddress[i] = getArguments().getString("scheduleaddress" + i);
//                    Log.d("4.26", "!!!!!!!!!" + getArguments().getString("schedulesummary" + i));
                }
            }

        } else
            data[3] = getArguments().getString("schedulesummary");
//        Log.d("4.26","-------"+getArguments().getString("schedulesummary"));
        if (getArguments().containsKey("schedulejinwei"))
            data[4] = getArguments().getString("schedulejinwei");
        context = getActivity().getBaseContext();
        activity = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.checkschedule_frament, container, false);
        gridView = (ListView) view.findViewById(R.id.schedule_gridview);
        if (count != 0)
            adapter = new CheckScheduleNavAdapter(context, count, getsummary, getaddress);
        else {
            String[] tdata = {data[3]};
            String[] add4 = {data[4]};
            adapter = new CheckScheduleNavAdapter(context, 1, tdata, add4);
        }
        gridView.setAdapter(adapter);
        gridView.setDividerHeight(10);
        gridView.setOnItemClickListener(new itemListener());
        return view;
    }

    class itemListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//            Intent intent = new Intent(Intent.ACTION_VIEW,Uri.parse("http://maps.google.com/maps?" + "saddr="
//                        + "111" + "," + "222" + "&daddr=" + "333" + "," + "444"));
//                intent.setClassName("com.google.android.apps.maps","com.google.android.maps.MapsActivity");
            String addr = null;
            if (count == 0)
                addr = data[4];
            else addr = getaddress[position];
//            Log.e("4.30","itemClick:"+addr);
            if (addr != null) {
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                        Uri.parse("google.navigation:q=" + addr));
//            Log.d("4.26","-------!!!!"+data[4]);
                startActivity(intent);
            } else Toast.makeText(context,
                    context.getResources().getString(R.string.wrongData_text), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

}
