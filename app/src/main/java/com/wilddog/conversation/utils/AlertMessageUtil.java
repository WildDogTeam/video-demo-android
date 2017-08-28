package com.wilddog.conversation.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.wilddog.conversation.ConversationApplication;
import com.wilddog.conversation.R;

/**
 * Created by fly on 17-6-12.
 */

public class AlertMessageUtil {

    public static Toast toast;
    private static TextView tv;
    public static ProgressDialog progressDialog;

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

    public static void showprogressbar(String title, Context context) {
        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle(title);
        progressDialog.setMessage("载入中");
        progressDialog.show();
    }

    public static void dismissprogressbar() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

}
