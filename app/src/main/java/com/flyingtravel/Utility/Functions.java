package com.flyingtravel.Utility;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.flyingtravel.R;

import java.io.ByteArrayOutputStream;

public class Functions {
    /**
     * go to another Activity or go back.
     *
     * @param isBack if true then only call activity.finish()
     *
     *
     */
    public static void go(Boolean isBack, Activity activity, Context context, Class goclass, Bundle bundle) {

        if (isBack)
            activity.finish();
        else {
            Intent intent = new Intent();
            intent.setClass(context, goclass);
            if (bundle != null)
                intent.putExtras(bundle);
            activity.startActivity(intent);
        }
    }

    public static boolean isMyServiceRunning(Activity activity, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static Bitmap ScalePic(Bitmap bitmap) {
        Bitmap GetImage = null;
        int oldWidth = bitmap.getWidth();
        int oldHeight = bitmap.getHeight();
        int l = bitmap.getWidth();
        int i = bitmap.getHeight();
        while ((int) l > 500 || (int) i > 500) {
            l = (int) (l * 0.9);
            i = (int) (i * 0.9);
        }
        int newWidth = l;
        int newHeight = i;

        float scaleWidth = ((float) newWidth) / oldWidth;
        float scaleHeight = ((float) newHeight) / oldHeight;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        GetImage = Bitmap.createBitmap(bitmap, 0, 0, oldWidth, oldHeight,
                matrix, true);

        return GetImage;
    }

    public static RelativeLayout.LayoutParams RecordMemoItem() {

        int width = 0, height = 0;
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(width, height);

        return layoutParams;
    }

    public static String getImageUri(Context inContext, Bitmap inImage) {

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return (Uri.parse(path)).toString();
    }

    public static void ClickTouchEvent(ImageView imageView, TextView textView, String where, Boolean isClick, int event) {
        switch (where) {
            case "home":
                imageView.setImageResource(R.drawable.tab_selected_home);
                textView.setTextColor(Color.parseColor("#0044BB"));
                if (isClick) {
                    imageView.performClick();
                }
                if (event == MotionEvent.ACTION_UP) {
                    imageView.setImageResource(R.drawable.click_home_img);
                    textView.setTextColor(Color.parseColor("#555555"));
                }
                if (event == MotionEvent.ACTION_DOWN)
                    textView.setTextColor(Color.parseColor("#0044BB"));
                break;
            case "member":
                imageView.setImageResource(R.drawable.tab_selected_member);
                textView.setTextColor(Color.parseColor("#0044BB"));
                if (isClick) {
                    imageView.performClick();
                }
                if (event == MotionEvent.ACTION_UP) {
                    imageView.setImageResource(R.drawable.member_img_click);
                    textView.setTextColor(Color.parseColor("#555555"));
                }
                if (event == MotionEvent.ACTION_DOWN)
                    textView.setTextColor(Color.parseColor("#0044BB"));
                break;
            case "shoprecord":
                imageView.setImageResource(R.drawable.tab_selected_record);
                textView.setTextColor(Color.parseColor("#0044BB"));
                if (isClick) {
                    imageView.performClick();
                }
                if (event == MotionEvent.ACTION_UP) {
                    imageView.setImageResource(R.drawable.record_img_click);
                    textView.setTextColor(Color.parseColor("#555555"));
                }
                if (event == MotionEvent.ACTION_DOWN)
                    textView.setTextColor(Color.parseColor("#0044BB"));
                break;
            case "more":
                imageView.setImageResource(R.drawable.tab_selected_more);
                textView.setTextColor(Color.parseColor("#0044BB"));
                if (isClick) {
                    imageView.performClick();
                    imageView.setImageResource(R.drawable.more_img_click);
                    textView.setTextColor(Color.parseColor("#555555"));
                }
                if (event == MotionEvent.ACTION_UP) {
                    imageView.setImageResource(R.drawable.more_img_click);
                    textView.setTextColor(Color.parseColor("#555555"));
                }
                if (event == MotionEvent.ACTION_DOWN)
                    textView.setTextColor(Color.parseColor("#0044BB"));
                break;
        }
    }

    public static Boolean ifLogin(Context context) {
        DataBaseHelper helper = DataBaseHelper.getmInstance(context);
        SQLiteDatabase database = helper.getWritableDatabase();

        Cursor member_cursor = database.query("member", new String[]{"account", "password",
                "name", "phone", "email", "addr"}, null, null, null, null, null);
        if (member_cursor == null || member_cursor.getCount() == 0) {
            if (member_cursor != null)
                member_cursor.close();
            return false;
        } else {
            if (member_cursor != null)
                member_cursor.close();
            return true;
        }

    }

    public interface TaskCallBack {
        /**
         * method That Does Something When Task Is Done
         *
         * @param OrderNeedUpdate if Order record updated
         */
        public void TaskDone(Boolean OrderNeedUpdate);
    }
}
