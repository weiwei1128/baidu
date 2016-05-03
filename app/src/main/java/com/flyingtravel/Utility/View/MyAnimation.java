package com.flyingtravel.Utility.View;

import android.view.View;
import android.view.animation.Transformation;

/**
 * Created by wei on 2015/11/18.
 * textview 縮小的動畫
 */
public class MyAnimation extends android.view.animation.Animation {

    private final int targetWeidth;
    private final View view;
    private final boolean down;

    public MyAnimation(View view, int targetWeidth, boolean down){
        this.view = view;
        this.targetWeidth = targetWeidth;
        this.down = down;
    }
    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        int newWidth;
        if (down) {
            newWidth = (int) (targetWeidth * interpolatedTime);
        } else {
            newWidth = (int) (targetWeidth * (1 - interpolatedTime));
        }
        view.getLayoutParams().width = newWidth;
        view.requestLayout();
    }

    @Override
    public void initialize(int width, int height, int parentWidth,
                           int parentHeight) {
        super.initialize(width, height, parentWidth, parentHeight);
    }

    @Override
    public boolean willChangeBounds() {
        return true;
    }
}
