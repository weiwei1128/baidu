package com.flyingtravel.Activity.Special;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.flyingtravel.R;
import com.flyingtravel.Utility.DataBaseHelper;
import com.flyingtravel.Utility.Functions;

public class SpecialDetailActivity extends AppCompatActivity {
    ImageView itemImg;
    TextView itemTitle, itemContent;
    DataBaseHelper helper;
    SQLiteDatabase database;
    int itemPosition;
    ImageLoader loader = ImageLoader.getInstance();
    DisplayImageOptions options;
    private ImageLoadingListener listener;
    LinearLayout backImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.special_detail_activity);
        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null)
            if (bundle.containsKey("WhichItem")) {
                itemPosition = bundle.getInt("WhichItem");
            }
        itemImg = (ImageView) findViewById(R.id.specailitem_Img);
        backImg = (LinearLayout) findViewById(R.id.specialitem_backImg);
        itemTitle = (TextView) findViewById(R.id.specialitemName_Text);
        itemContent = (TextView) findViewById(R.id.specailitemDetail_text);
        DataBaseHelper helper = DataBaseHelper.getmInstance(SpecialDetailActivity.this);
        SQLiteDatabase database = helper.getWritableDatabase();
        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Functions.go(true, SpecialDetailActivity.this, SpecialDetailActivity.this,
                        SpecialActivity.class, null);
            }
        });
        options = new DisplayImageOptions.Builder()
                .showImageOnFail(R.drawable.error)
                .showImageForEmptyUri(R.drawable.empty)
                .showImageOnLoading(R.drawable.loading2)
                .cacheInMemory(false)
                .cacheOnDisk(true).build();
        listener = new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String s, View view) {

            }

            @Override
            public void onLoadingFailed(String s, View view, FailReason failReason) {

            }

            @Override
            public void onLoadingComplete(String s, View view, Bitmap bitmap) {

            }

            @Override
            public void onLoadingCancelled(String s, View view) {

            }
        };
//        ImageLoaderConfiguration configuration = new ImageLoaderConfiguration.Builder(
//                SpecialDetailActivity.this).build();
//        ImageLoader.getInstance().init(configuration);
        Cursor special = database.query("special_activity", new String[]{"special_id",
                        "title", "img", "content", "price", "click"},
                null, null, null, null, null);
        if (special != null) {
            if (special.getCount() >= itemPosition) {
                special.moveToPosition(itemPosition);
                if (special.getString(1) != null)
                    itemTitle.setText(special.getString(1));
                if (special.getString(2).startsWith("http:"))
                    loader.displayImage(special.getString(2)
                            , itemImg, options, listener);
                else loader.displayImage("http://zhiyou.lin366.com/" + special.getString(2)
                        , itemImg, options, listener);
                if (special.getString(3) != null)
                    itemContent.setText(special.getString(3));
            }
            special.close();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK)
            Functions.go(true, SpecialDetailActivity.this, SpecialDetailActivity.this,
                    SpecialActivity.class, null);
        return false;
    }
}
