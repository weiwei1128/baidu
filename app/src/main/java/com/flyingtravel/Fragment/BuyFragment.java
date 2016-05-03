package com.flyingtravel.Fragment;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.flyingtravel.Activity.Buy.BuyItemDetailActivity;
import com.flyingtravel.Adapter.BuyAdapter;
import com.flyingtravel.R;
import com.flyingtravel.Utility.DataBaseHelper;
import com.flyingtravel.Utility.Functions;

public class BuyFragment extends Fragment {

    GridView gridView;
    BuyAdapter adapter;
    int Position = 0;
    Context context;
    Activity activity;



    public BuyFragment() {

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Position = getArguments().getInt("position");
        context = this.getActivity().getBaseContext();
        activity = this.getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.buy_fragment, container, false);
        gridView = (GridView) view.findViewById(R.id.gridView);
        adapter = new BuyAdapter(getActivity(), Position,gridView);//position 代表頁碼
        gridView.setNumColumns(2);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new itemListener());
        if (adapter.getCount() == 0)
            Toast.makeText(context, getContext().getResources().getString(R.string.nofile_text), Toast.LENGTH_SHORT).show();
        return view;
    }

    class itemListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Bundle bundle = new Bundle();
            bundle.putInt("WhichItem", (Position - 1) * 10 + position);

            Functions.go(false, activity, context, BuyItemDetailActivity.class, bundle);

            DataBaseHelper helper;
            SQLiteDatabase database;
            helper = DataBaseHelper.getmInstance(context);
            database = helper.getWritableDatabase();
            Cursor goods_cursor = database.query("goods", new String[]{"totalCount", "goods_id",
                    "goods_title", "goods_url", "goods_money", "goods_content", "goods_click",
                    "goods_addtime"}, null, null, null, null, null);
            if (goods_cursor != null && goods_cursor.getCount() >= (Position - 1) * 10 + position) {
                goods_cursor.moveToPosition((Position - 1) * 10 + position);
                ContentValues cv = new ContentValues();
                int count = 0;
                if (goods_cursor.getString(6) != null)
                    count = Integer.parseInt(goods_cursor.getString(6)) + 1;
                cv.put("goods_click", count + "");
                Log.d("4.25", "click:" + count);
                long result = database.update("goods", cv, "goods_id=?", new String[]{goods_cursor.getString(1)});
            }
//            adapter.UpdateView((Position - 1) * 10 + position,position);
            adapter.notifyDataSetChanged();
        }
    }
}
