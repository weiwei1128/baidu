package com.flyingtravel.Utility.View;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

/**
 * Created by wei on 2016/4/14.
 */
public class MyTextview2 extends TextView implements Runnable {
    private int currentScrollX;// 当前滚动的位置
    private boolean isStop = false;
    private int textWidth;
    private boolean isMeasure = false;
    int mDistance;
    int mDuration;
    Boolean FirstRun = true;
    int count=0;

    public MyTextview2(Context context) {
        super(context);
        setSingleLine();
        // TODO Auto-generated constructor stub
    }

    public MyTextview2(Context context, AttributeSet attrs) {
        super(context, attrs);
        setSingleLine();
    }

    public MyTextview2(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setSingleLine();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        super.onDraw(canvas);
        if (!isMeasure) {// 文字宽度只需获取一次就可以了
            getTextWidth();
            isMeasure = true;
        }
    }

    /**
     * 获取文字宽度
     */
    private void getTextWidth() {
        Paint paint = this.getPaint();
        String str = this.getText().toString();
        textWidth = (int) paint.measureText(str);
    }

    @Override
    public void run() {
        if(FirstRun) {
            currentScrollX = -(this.getWidth());
            FirstRun = false;
        }
        if(count<5)
            count++;
        else {
            count=0;
            currentScrollX += 1;// 速度
        }
//        scrollTo(currentScrollX, 0);
        scrollTo(currentScrollX, 0);
        if (isStop) {
            return;
        }
        if (getScrollX() >= (textWidth)) {
//            Log.e("4.14","!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            scrollTo(-this.getWidth(), 0);
            currentScrollX = -this.getWidth();
//                        return;
        }
//        if(currentScrollX==0)
//            Log.d("4.14","------------------------------------------");
//        Log.e("4.14","currentScrollX"+currentScrollX);
//        Log.d("4.14", "getScrollX" + getScrollX());
//        Log.i("4.14", "getWidth" + this.getWidth()+" textWidth:"+textWidth);
        postDelayed(this, 5);
    }
    private int calculateMoveDistance(boolean isFirstRun){
        Rect rect = new Rect();
//        String textString = (String) getText();
        //0414w in case java.lang.ClassCastException: android.text.SpannableString cannot be cast to java.lang.String
        String textString =getText()+"";
        getPaint().getTextBounds(textString,0,textString.length(),rect);
        int moveDistance = rect.width();
        rect = null;
        this.mDistance = isFirstRun ? moveDistance : moveDistance+getWidth();
        this.mDuration = (int) (2*mDistance);
        return this.mDistance;
    }

    // 开始滚动
    public void startScroll() {
        isStop = false;
        this.removeCallbacks(this);
        post(this);
    }

    // 停止滚动
    public void stopScroll() {
        isStop = true;
    }

    // 从头开始滚动
    public void startFor0() {
        currentScrollX = 0;
        startScroll();
    }
}
