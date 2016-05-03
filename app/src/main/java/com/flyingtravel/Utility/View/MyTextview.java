package com.flyingtravel.Utility.View;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.animation.LinearInterpolator;
import android.widget.Scroller;
import android.widget.TextView;

/**
 * Created by wei on 2015/11/11.
 *
 * TextView跑馬燈
 */
public class MyTextview extends TextView {
    Context context;
    Scroller mScroller;
    int mDistance;
    int mDuration;
    float mVelocity;

    public MyTextview(Context context) {
        super(context);
        this.context = context;
        setSingleLine();
        mScroller = new Scroller(context,new LinearInterpolator());
        setScroller(mScroller);
    }

    public MyTextview(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        setSingleLine();
        mScroller = new Scroller(context,new LinearInterpolator());
        setScroller(mScroller);

    }

    public MyTextview(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        setSingleLine();
        //加入linearInterpolater線性插補器參數
        //此參數可以讓跑馬燈以線性等速度移動 否則預設值為黏黏viscous的移動
        mScroller = new Scroller(context,new LinearInterpolator());
        setScroller(mScroller);
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if(mScroller.computeScrollOffset()){
            scrollTo(mScroller.getCurrX(),mScroller.getCurrY());
        }else{
            if(mScroller.isFinished())
                mScroller.startScroll(-getWidth(),0
                        ,calculateMoveDistance(false,mVelocity),0,mDuration);
        }
    }
    private int calculateMoveDistance(boolean isFirstRun, float velocity){
        Rect rect = new Rect();
//        String textString = (String) getText();
        //0414w in case java.lang.ClassCastException: android.text.SpannableString cannot be cast to java.lang.String
        String textString =getText()+"";
        getPaint().getTextBounds(textString,0,textString.length(),rect);
        int moveDistance = rect.width();
        rect = null;
        this.mDistance = isFirstRun ? moveDistance : moveDistance+getWidth();
        this.mDuration = (int) (velocity*mDistance);
        return this.mDistance;
    }

    public  void scrollText(float velocity){
        this.mVelocity = velocity;
        mScroller.startScroll(0,0,calculateMoveDistance(true,velocity),0,mDuration);
    }
}
