package com.jyf9774.bookyi;

import android.util.Log;

import androidx.recyclerview.widget.RecyclerView;

public class MyScrollListener extends RecyclerView.OnScrollListener {

    private HideAndShowListener mHideAndShowListener;
    private static final int THRESHOLD = 20;
    private int distance = 0;
    private boolean visible = true;


    public MyScrollListener(HideAndShowListener hideAndShowListener) {
        mHideAndShowListener = hideAndShowListener;
    }


    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        /**
         * dy:Y轴方向的增量
         * 有正和负
         * 当正在执行动画的时候，就不要再执行了
         */
        Log.i("tag","dy--->"+dy);
        if (distance > THRESHOLD && visible) {
            //隐藏动画
            visible = false;
            mHideAndShowListener.hide();
            distance = 0;
        } else if (distance < -20 && !visible) {
            //显示动画
            visible = true;
            mHideAndShowListener.show();
            distance = 0;
        }
        if (visible && dy > 0 || (!visible && dy < 0)) {
            distance += dy;
        }
    }

    public interface HideAndShowListener {
        void hide();

        void show();
    }
}

