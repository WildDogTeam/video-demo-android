package com.wilddog.demo.utils;

import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.wilddog.demo.ConversationApplication;
import com.wilddog.demo.R;

/**
 * Created by fly on 17-6-12.
 */

public class AlertMessageUtil {

    public static Toast toast;
    private static TextView tv;

    public static void showShortToast(String content) {
        if (toast == null) {
            toast = new Toast(ConversationApplication.getContext());
            toast.setDuration(Toast.LENGTH_SHORT);
            View view = View.inflate(ConversationApplication.getContext(), R.layout.toast_bg, null);
            toast.setView(view);
            tv = (TextView) view.findViewById(R.id.toast_content);
            tv.setText(content);
        } else {
            tv.setText(content);
        }
        toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 66);
        toast.show();
    }


}
