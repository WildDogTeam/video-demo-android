package com.wilddog.conversation.floatingwindow;

import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.wilddog.conversation.R;
import com.wilddog.conversation.activities.CallingActivity;
import com.wilddog.conversation.activities.ConversationActivity;
import com.wilddog.conversation.utils.ParamsStore;
import com.wilddog.video.base.WilddogVideoView;

import java.util.List;

public class WindowService extends Service {


    private final String TAG = this.getClass().getSimpleName();

    private WindowManager.LayoutParams wmParams;
    private WindowManager mWindowManager;
    private View mWindowView;
    private RelativeLayout mPercent;

    private int mStartX;
    private int mStartY;
    private int mEndX;
    private int mEndY;
    private MyBinder myBinder;
    private WilddogVideoView local;
    private WilddogVideoView remote;
    private boolean isShow;


    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate");
        initWindowParams();
        initView();
        initClick();



    }

    private void initWindowParams() {
        mWindowManager = (WindowManager) getApplication().getSystemService(getApplication().WINDOW_SERVICE);
        wmParams = new WindowManager.LayoutParams();
        wmParams.type = Integer.parseInt(android.os.Build.VERSION.SDK) > 18 ? WindowManager.LayoutParams.TYPE_TOAST :
                WindowManager.LayoutParams.TYPE_PHONE;//TYPE_PHONE
        wmParams.format = PixelFormat.TRANSLUCENT;
        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        wmParams.gravity = Gravity.LEFT | Gravity.TOP;
        wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
    }

    private void initView() {
        mWindowView = LayoutInflater.from(getApplication()).inflate(R.layout.layout_window, null);
        mPercent = (RelativeLayout) mWindowView.findViewById(R.id.fullscreen_content);
        local = (WilddogVideoView) mWindowView.findViewById(R.id.local_video_view);
        remote = (WilddogVideoView) mWindowView.findViewById(R.id.remote_video_view);
        local.setZOrderMediaOverlay(true);
        myBinder = new MyBinder();
    }

    private void addWindowView2Window() {
        isShow = true;
        mWindowManager.addView(mWindowView, wmParams);
        if (StreamsHolder.getLocalStream() == null || StreamsHolder.getRemoteStream() == null) {
            return;
        }
        StreamsHolder.getLocalStream().attach(local);
        StreamsHolder.getRemoteStream().attach(remote);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removeWindowView();
        Log.i(TAG, "onDestroy");
    }

    private void removeWindowView() {
        if (mWindowView != null&&isShow) {
            //移除悬浮窗口
            Log.i(TAG, "removeView");
            mWindowManager.removeView(mWindowView);
            isShow = false;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }


    private void initClick() {
        mPercent.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mStartX = (int) event.getRawX();
                        mStartY = (int) event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:

                        if (needIntercept()) {
                            //getRawX是触摸位置相对于屏幕的坐标，getX是相对于按钮的坐标
                            wmParams.x = (int) event.getRawX() - mWindowView.getMeasuredWidth() / 2;
                            wmParams.y = (int) event.getRawY() - mWindowView.getMeasuredHeight() / 2;
                            mWindowManager.updateViewLayout(mWindowView, wmParams);
                            return true;
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        mEndX = (int) event.getRawX();
                        mEndY = (int) event.getRawY();
                        if (needIntercept()) {
                            return true;
                        }
                        break;
                    default:
                        break;
                }
                return false;
            }
        });

        mPercent.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // 处理点击事件
                StreamsHolder.getLocalStream().detach();
                StreamsHolder.getRemoteStream().detach();

                if (!ParamsStore.isInitiativeCall) {
                    Intent intent = new Intent(getBaseContext(), ConversationActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(getBaseContext(), CallingActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }

            }
        });
    }

    /**
     * 是否拦截
     *
     * @return true:拦截;false:不拦截.
     */
    private boolean needIntercept() {
        if (Math.abs(mStartX - mEndX) > 30 || Math.abs(mStartY - mEndY) > 30) {
            return true;
        }
        return false;
    }

    /**
     * 判断当前应用程序处于前台还是后台
     */
    private boolean isAppAtBackground(final Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
        if (!tasks.isEmpty()) {
            ComponentName topActivity = tasks.get(0).topActivity;
            if (!topActivity.getPackageName().equals(context.getPackageName())) {
                return true;
            }
        }
        return false;
    }

    public class MyBinder extends Binder {
        public void hidFloatingWindow() {
            removeWindowView();
        }

        public void showFloatingWindow() {
            addWindowView2Window();
        }


        public boolean isWindowShow() {
            return isShow;
        }
    }
}
