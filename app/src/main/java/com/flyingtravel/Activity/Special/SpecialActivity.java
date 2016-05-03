package com.flyingtravel.Activity.Special;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.flyingtravel.Adapter.SpecialFragmentViewPagerAdapter;
import com.flyingtravel.Fragment.SpecialFragment;
import com.flyingtravel.HomepageActivity;
import com.flyingtravel.R;
import com.flyingtravel.Utility.DataBaseHelper;
import com.flyingtravel.Utility.Functions;

import java.util.ArrayList;
import java.util.List;

public class SpecialActivity extends AppCompatActivity {
    LinearLayout layout, backImg;
    int FragmentNumber = 0;
    int PageNo = 0, pageNo = 1, pages = 0;
    ViewPager viewPager;
    List<Fragment> fragments = new ArrayList<>();
    SpecialFragmentViewPagerAdapter specialFragmentViewPagerAdapter;
    DataBaseHelper helper;
    SQLiteDatabase database;
    FragmentManager fragmentManager;
    TextView number, lastPage, nextPage;
    ;

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.special_activity);
        lastPage = (TextView) findViewById(R.id.lastpage_text);
        lastPage.setVisibility(View.INVISIBLE);
        nextPage = (TextView) findViewById(R.id.nextpage_text);
        backImg = (LinearLayout) findViewById(R.id.special_backImg);
        layout = (LinearLayout) findViewById(R.id.special_textLayout);
        viewPager = (ViewPager) findViewById(R.id.special_viewpager);
        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Functions.go(true, SpecialActivity.this, SpecialActivity.this, HomepageActivity.class, null);
            }
        });
        lastPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(pageNo - 2);
            }
        });
        nextPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(pageNo);

            }
        });
        helper = DataBaseHelper.getmInstance(SpecialActivity.this);
        database = helper.getReadableDatabase();
        Cursor special = database.query("special_activity", new String[]{"special_id", "title", "img", "content", "price", "click"},
                null, null, null, null, null);
        if (special != null) {
            FragmentNumber = special.getCount();
            special.close();
        }
        fragmentManager = this.getSupportFragmentManager();

        if (FragmentNumber % 10 > 0)
            PageNo = (FragmentNumber / 10) + 1;
        else PageNo = FragmentNumber / 10;


        TextView textView = new TextView(this);
        textView.setText("/" + PageNo);
        textView.setTextColor((Color.parseColor("#000000")));
        number = new TextView(this);
        number.setText("1");
        number.setTextColor((Color.parseColor("#FF0088")));
        layout.addView(number);
        layout.addView(textView);

        for (int i = 0; i < PageNo; i++) {
            SpecialFragment fragment = new SpecialFragment();
            Bundle bundle = new Bundle();
            bundle.putInt("pagenumber", (i + 1));
            fragment.setArguments(bundle);
            //pagenumber
            fragments.add(fragment);
        }
        specialFragmentViewPagerAdapter = new SpecialFragmentViewPagerAdapter(this.getSupportFragmentManager(),
                viewPager, fragments, this);
        viewPager.setOffscreenPageLimit(1);
        viewPager.setAdapter(specialFragmentViewPagerAdapter);
        viewPager.setOnPageChangeListener(new PageListener());
        if (specialFragmentViewPagerAdapter.getCount() == 0)
            Toast.makeText(this, SpecialActivity.this.getResources().getString(R.string.nofile_text), Toast.LENGTH_SHORT).show();
//*/
//
    }


    private class PageListener implements ViewPager.OnPageChangeListener {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        public void onPageSelected(int position) {
            pageNo = position + 1;
            if (pageNo == pages)
                nextPage.setVisibility(View.INVISIBLE);
            else nextPage.setVisibility(View.VISIBLE);
            if (pageNo == 1)
                lastPage.setVisibility(View.INVISIBLE);
            else lastPage.setVisibility(View.VISIBLE);
            String get = String.valueOf(position + 1);
            number.setText(get);
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Functions.go(true, SpecialActivity.this, SpecialActivity.this, HomepageActivity.class, null);
        }

        return false;
    }
}
