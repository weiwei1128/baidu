package com.flyingtravel.Fragment;


import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.flyingtravel.Adapter.ShopRecordAdapter;
import com.flyingtravel.R;
import com.flyingtravel.Activity.ShopRecordItemActivity;
import com.flyingtravel.Utility.DataBaseHelper;
import com.flyingtravel.Utility.Functions;
import com.flyingtravel.Utility.GlobalVariable;
import com.flyingtravel.Utility.OrderGet;
import com.flyingtravel.Utility.OrderUpdate;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;


/**
 * 1.OnResume 確認是否有更新資料
 * ->Y
 * 抓資料 then renew the view
 * ->N
 * show the data
 **/

public class ShopRecordFragment extends Fragment {
    Context context;
    String userId = null;
    int OldCount = 0;
    GridView gridView;
    public ShopRecordAdapter adapter;
    ProgressDialog dialog;
    /**GA**/
    public static Tracker tracker;


    public ShopRecordFragment() {
        // Required empty public constructor
    }

    @Override
    public void onResume() {
        //
        dialog.setMessage(getContext().getResources().getString(R.string.loading_text));
        dialog.show();
//        Log.e("3.22", "!!!!!!shop record on resume!!!!!!");
        new OrderUpdate(userId, OldCount, context, new Functions.TaskCallBack() {
            @Override
            public void TaskDone(Boolean Update) {
                methodThatDoesSomethingWhenTaskIsDone(Update);
            }
        }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        super.onResume();
        /**GA**/
        tracker.setScreenName("訂單");
        tracker.send(new HitBuilders.ScreenViewBuilder().build());
        /**GA**/
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dialog = new ProgressDialog(getActivity());
        this.context = getActivity();
        /**GA**/
        GlobalVariable globalVariable = (GlobalVariable)getActivity().getApplication();
        tracker = globalVariable.getDefaultTracker();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.shoprecord_activity, container, false);
        UI(view);
        return view;
    }

    void UI(View view) {
        gridView = (GridView) view.findViewById(R.id.shop_record_gridview);


        gridView.setOnItemClickListener(new itemlistener());
        DataBaseHelper helper = DataBaseHelper.getmInstance(context);
        SQLiteDatabase database = helper.getWritableDatabase();
        Cursor member_cursor = database.query("member", new String[]{"account", "password",
                "name", "phone", "email", "addr"}, null, null, null, null, null);

        if (member_cursor != null && member_cursor.getCount() > 0) {
            member_cursor.moveToFirst();
            this.userId = member_cursor.getString(0);
            if (userId != null) {
                adapter = new ShopRecordAdapter(context, userId);
                gridView.setAdapter(adapter);
                Cursor order_cursor = database.query("shoporder", new String[]{"order_id", "order_userid ", "order_no",
                                "order_time", "order_name", "order_phone", "order_email",
                                "order_money", "order_state", "order_schedule"}, "order_userid=" + "\"" + userId + "\"",
                        null, null, null, null, null);
                if (order_cursor != null) {
                    order_cursor.moveToFirst();
                    this.OldCount = order_cursor.getCount();
                    order_cursor.close();
                }
            }
        }
        if (member_cursor != null)
            member_cursor.close();


    }

    class itemlistener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            DataBaseHelper helper = DataBaseHelper.getmInstance(context);
            SQLiteDatabase database = helper.getWritableDatabase();
            Cursor order_cursor = database.query("shoporder", new String[]{"order_id", "order_userid ", "order_no",
                            "order_time", "order_name", "order_phone", "order_email",
                            "order_money", "order_state", "order_schedule"}, "order_userid=" + "\"" + userId + "\"",
                    null, null, null, null);
            String Order_id;
            if (order_cursor != null && order_cursor.getCount() >= position) {
                order_cursor.moveToPosition(position);
                Order_id = order_cursor.getString(0);
                Bundle bundle = new Bundle();
                bundle.putString("WhichItem", Order_id);
                Functions.go(false, getActivity(), context, ShopRecordItemActivity.class, bundle);
            }
            if (order_cursor != null)
                order_cursor.close();
        }
    }


    private void methodThatDoesSomethingWhenTaskIsDone(Boolean a) {
        if (dialog.isShowing())
            dialog.dismiss();
        if (a) {//need to updated
//            Log.e("3.23", "need to update shoprecord!");
            new OrderGet(context, userId, new Functions.TaskCallBack() {
                @Override
                public void TaskDone(Boolean OrderNeedUpdate) {
                    if (OrderNeedUpdate)//有收到更新資料
                        adapter.notifyDataSetChanged();
                }
            }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else if (OldCount == 0)
            Toast.makeText(context, getContext().getResources().getString(R.string.nofile_text), Toast.LENGTH_SHORT).show();
    }


}
