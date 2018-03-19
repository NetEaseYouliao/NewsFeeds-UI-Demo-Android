package com.netease.youliao.uidemo;

import android.content.Context;
import android.support.v4.widget.NestedScrollView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by chenjensen on 1/22/18.
 */

public class MyScrollView extends NestedScrollView  {

    private static final float MAX = 1000000f;

    private float lastY = MAX;


    public MyScrollView(Context context) {
        this(context, null);
    }

    public MyScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    //判断ScrollView是否下拉到底部
    public boolean isBottom() {
        View contentView = getChildAt(0);
        if (contentView != null && contentView.getMeasuredHeight() <= getScrollY() + getHeight()) {
            return true;
        }
        return false;
    }

    //判断ScrollView是否处于顶部
    public boolean isTop() {
        return getScrollY() == 0;
    }

    //判断是否为上拉的手势
    private boolean isUp(float y) {
        boolean isUp = (y - lastY) < 0;
        if (lastY == MAX) {
            isUp = false;
        }
        lastY = y;
        return isUp;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_MOVE:
                if (isTop() && isUp(ev.getY())) {
                    return true;
                }
                if (isBottom() || isTop()) {
                    return false;
                } else {
                    return true;
                }
            default:
                lastY = ev.getY();
        }
        return super.onInterceptTouchEvent(ev);
    }
}
