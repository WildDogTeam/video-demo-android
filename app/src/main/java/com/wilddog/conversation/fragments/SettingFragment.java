package com.wilddog.conversation.fragments;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wilddog.conversation.activities.BlacklistActivity;
import com.wilddog.conversation.utils.Constant;
import com.wilddog.conversation.R;
import com.wilddog.conversation.activities.LoginActivity;
import com.wilddog.conversation.activities.RecordFileActivity;
import com.wilddog.conversation.activities.SDKVersionActivity;
import com.wilddog.conversation.bean.UserInfo;
import com.wilddog.conversation.utils.AlertMessageUtil;
import com.wilddog.conversation.utils.ImageLoadingUtil;
import com.wilddog.conversation.utils.JsonConvertUtil;
import com.wilddog.conversation.utils.SharedPreferenceTool;
import com.wilddog.conversation.view.CircleImageView;
import com.wilddog.conversation.wilddog.WilddogSyncManager;

/**
 * Created by fly on 17-6-9.
 */

public class SettingFragment extends BaseFragment implements View.OnClickListener {
    private TextView tvUid;
    private TextView tvDimension;
    private TextView tvBeautyPlan;
    private TextView tvNickName;
    private CircleImageView civPhotoUrl;
    private LinearLayout llParent;
    private LinearLayout llDimension;
    private LinearLayout llBeautyPlan;
    private LinearLayout llRecordFile;
    private LinearLayout llSDKVersion;
    private LinearLayout llLoginout;

    private PopupWindow popupWindow;

    private ClipboardManager clipboardManager;

    private UserInfo info;
    private LinearLayout llBlackList;

    public SettingFragment() {

    }

    @Override
    public View initView(LayoutInflater inflater) {
        View view = inflater.inflate(R.layout.fragment_setting, null);
        llParent = (LinearLayout) view.findViewById(R.id.ll_parent);
        llDimension = (LinearLayout) view.findViewById(R.id.ll_dimension);
        llBeautyPlan = (LinearLayout) view.findViewById(R.id.ll_beauty_plan);
        llRecordFile = (LinearLayout) view.findViewById(R.id.ll_record_file);
        llSDKVersion = (LinearLayout) view.findViewById(R.id.ll_SDK_version);
        llLoginout = (LinearLayout) view.findViewById(R.id.ll_user_login_out);
        llBlackList = (LinearLayout) view.findViewById(R.id.ll_blacklist);

        tvUid = (TextView) view.findViewById(R.id.tv_uid);
        tvBeautyPlan = (TextView) view.findViewById(R.id.tv_beauty_plan);
        tvNickName = (TextView) view.findViewById(R.id.tv_nickname);
        civPhotoUrl = (CircleImageView) view.findViewById(R.id.civ_photo);
        tvDimension = (TextView) view.findViewById(R.id.tv_dimension);
        llDimension.setOnClickListener(this);
        llBeautyPlan.setOnClickListener(this);
        llRecordFile.setOnClickListener(this);
        llSDKVersion.setOnClickListener(this);
        llLoginout.setOnClickListener(this);
        llBlackList.setOnClickListener(this);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        initDate();
    }

    private void initDate() {
        info = JsonConvertUtil.getObjectFromJson(SharedPreferenceTool.getUserInfo(getContext()), UserInfo.class);
        tvUid.setText("ID:" + info.getUid());
        tvNickName.setText(info.getNickname());
        ImageLoadingUtil.Load(info.getFaceurl(), civPhotoUrl);
        tvDimension.setText(SharedPreferenceTool.getDimension(getContext()));
        tvBeautyPlan.setText(SharedPreferenceTool.getBeautyPlan(getContext()));
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_dimension:
                setDimension();
                break;
            case R.id.ll_beauty_plan:
                setBeautyPlan();
                break;
            case R.id.ll_record_file:
                gotoRecordFileActivity();
                break;
            case R.id.ll_blacklist:
                gotoBlackUsersActivity();
                break;
            case R.id.ll_SDK_version:
                gotoSDKVersionActivity();
                break;
            case R.id.ll_user_login_out:
                showLoginOutDialog();
                break;
            default:
                break;
        }
    }

    private void gotoBlackUsersActivity() {
        startActivity(new Intent(getContext(), BlacklistActivity.class));
    }


    private void showLoginOutDialog(){
        View view = View.inflate(getContext(), R.layout.popupwindow_loginout, null);
        Button btnYes = (Button) view.findViewById(R.id.btn_yes);
        Button btnNo = (Button) view.findViewById(R.id.btn_no);
        btnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindowDismiss();
                loginout();
            }
        });
        btnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindowDismiss();
            }
        });
        showPopupWindow(view);
    }

    private void loginout() {
        // 清理登录状态,将用户信息移除
        WilddogSyncManager.getWilddogSyncTool().removeUserInfo(info.getUid());
        SharedPreferenceTool.setUserInfo(getContext(), "");
        SharedPreferenceTool.setLoginStatus(getContext(), false);
        startActivity(new Intent(getContext(), LoginActivity.class));
        getActivity().finish();
    }

    private void setDimension() {
        View view = View.inflate(getContext(), R.layout.popupwindow_dimension, null);
        TextView tvDimension120P = (TextView) view.findViewById(R.id.tv_dimesion_120P);
        TextView tvDimension240P = (TextView) view.findViewById(R.id.tv_dimesion_240P);
        TextView tvDimension360P = (TextView) view.findViewById(R.id.tv_dimesion_360P);
        TextView tvDimension480P = (TextView) view.findViewById(R.id.tv_dimesion_480P);
        TextView tvDimension720P = (TextView) view.findViewById(R.id.tv_dimension_720P);
        TextView tvDimension1080P = (TextView) view.findViewById(R.id.tv_dimesion_1080P);
        TextView tvDimensionCancel = (TextView) view.findViewById(R.id.tv_cancel);
        tvDimension120P.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveDimensionAndChangeUI(getString(R.string.dimension_120P));
                popupWindowDismiss();
            }
        });
        tvDimension240P.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveDimensionAndChangeUI(getString(R.string.dimension_240P));
                popupWindowDismiss();
            }
        });
        tvDimension360P.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveDimensionAndChangeUI(getString(R.string.dimension_360P));
                popupWindowDismiss();
            }
        });
        tvDimension480P.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveDimensionAndChangeUI(getString(R.string.dimension_480P));
                popupWindowDismiss();
            }
        });
        tvDimension720P.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveDimensionAndChangeUI(getString(R.string.dimension_720P));
                popupWindowDismiss();
            }
        });
        tvDimension1080P.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveDimensionAndChangeUI(getString(R.string.dimension_1080P));
                popupWindowDismiss();
            }
        });
        tvDimensionCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindowDismiss();
            }
        });
        showPopupWindow(view);
    }

    private void setBeautyPlan() {
        View view = View.inflate(getContext(), R.layout.popupwindow_beauty_plan, null);
        TextView tvCamera360 = (TextView) view.findViewById(R.id.tv_beauty_plan_Camera360);
        TextView tvTuSDK = (TextView) view.findViewById(R.id.tv_beauty_plan_TuSDK);
        TextView tvNothing = (TextView) view.findViewById(R.id.tv_beauty_nothing);
        TextView tvDimensionCancel = (TextView) view.findViewById(R.id.tv_cancel);
        tvCamera360.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(Constant.SDK_KEY_NEW)) {
                    AlertMessageUtil.showShortToast("请先申请Camera360的key,在自己的sdk中集成");
                } else {
                    saveBeautyPlanAndChangeUI(getResources().getString(R.string.beauty_plan_Camera360));
                }
                popupWindowDismiss();
            }
        });
        tvTuSDK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(Constant.TUSDK_KEY)) {
                    AlertMessageUtil.showShortToast("请先申请TuSDK的key,在自己的sdk中集成");
                } else {
                    saveBeautyPlanAndChangeUI(getResources().getString(R.string.beauty_plan_TuSDK));
                }
                popupWindowDismiss();
            }
        });
        tvNothing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveBeautyPlanAndChangeUI(getResources().getString(R.string.change_beauty_nothing));
                popupWindowDismiss();
            }
        });
        tvDimensionCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindowDismiss();
            }
        });
        showPopupWindow(view);
    }

    private void showPopupWindow(View view) {
        popupWindow = new PopupWindow(view, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        popupWindow.setFocusable(true);
        popupWindow.showAtLocation(llParent, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
    }

    private void saveDimensionAndChangeUI(String dimension) {
        SharedPreferenceTool.saveDimension(getContext(), dimension);
        tvDimension.setText(dimension);
    }

    private void saveBeautyPlanAndChangeUI(String beautyPlan) {
        SharedPreferenceTool.saveBeautyPlan(getContext(), beautyPlan);
        tvBeautyPlan.setText(beautyPlan);
    }

    private void popupWindowDismiss() {
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
        }
    }

    private ClipboardManager getClipboardManager() {
        if (clipboardManager == null) {
            clipboardManager = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        }
        return clipboardManager;
    }

    private void gotoSDKVersionActivity() {
        startActivity(new Intent(getContext(), SDKVersionActivity.class));
    }

    private void gotoRecordFileActivity() {
        startActivity(new Intent(getContext(), RecordFileActivity.class));
    }
}
