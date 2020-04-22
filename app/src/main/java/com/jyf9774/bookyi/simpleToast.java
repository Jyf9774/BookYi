package com.jyf9774.bookyi;

import android.app.Application;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

public class simpleToast {
    static Toast mToast;

    public static void showToast(String msg, Context context) {

        if (mToast == null) {
            mToast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        } else {
            View view = mToast.getView();
            mToast.cancel();
            mToast = new Toast(context);
            mToast.setView(view);
            mToast.setDuration(Toast.LENGTH_SHORT);
            mToast.setText(msg);
        }
        mToast.setGravity(Gravity.CENTER, 0, 0);
        mToast.show();

    }
}
