package com.wilddog.conversation.fragments;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wilddog.conversation.R;
import com.wilddog.conversation.activities.LoginActivity;
import com.wilddog.conversation.activities.RecordFileActivity;
import com.wilddog.conversation.activities.SDKVersionActivity;
import com.wilddog.conversation.bean.UserInfo;
import com.wilddog.conversation.utils.AlertMessageUtil;
import com.wilddog.conversation.utils.ImageManager;
import com.wilddog.conversation.utils.ObjectAndStringTool;
import com.wilddog.conversation.utils.SharedpereferenceTool;
import com.wilddog.conversation.view.CircleImageView;
import com.wilddog.conversation.wilddog.WilddogSyncManager;

/**
 * Created by fly on 17-6-9.
 */

public class MeFragment extends BaseFragment implements View.OnClickListener{
   private TextView tvUid;
   private TextView btnCopy;
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

    private UserInfo info ;

   public MeFragment(){

    }

    @Override
    public View initView(LayoutInflater inflater) {
        View view = inflater.inflate(R.layout.fragment_setting,null);
        llParent = (LinearLayout) view.findViewById(R.id.ll_parent);
        llDimension = (LinearLayout) view.findViewById(R.id.ll_dimension);
        llBeautyPlan = (LinearLayout) view.findViewById(R.id.ll_beauty_plan);
        llRecordFile = (LinearLayout) view.findViewById(R.id.ll_record_file);
        llSDKVersion = (LinearLayout) view.findViewById(R.id.ll_SDK_version);
        llLoginout = (LinearLayout) view.findViewById(R.id.ll_user_login_out);
        tvUid = (TextView) view.findViewById(R.id.tv_uid);
        tvNickName = (TextView) view.findViewById(R.id.tv_nickName);
        civPhotoUrl = (CircleImageView) view.findViewById(R.id.civ_photo);
        tvDimension = (TextView) view.findViewById(R.id.tv_dimension);
        tvBeautyPlan = (TextView) view.findViewById(R.id.tv_beauty_plan);
        btnCopy = (TextView) view.findViewById(R.id.tv_copy);
        btnCopy.setOnClickListener(this);
        llDimension.setOnClickListener(this);
        llBeautyPlan.setOnClickListener(this);
        llRecordFile.setOnClickListener(this);
        llSDKVersion.setOnClickListener(this);
        llLoginout.setOnClickListener(this);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        initDate();
    }

    private void initDate(){
        info = ObjectAndStringTool.getObjectFromJson(SharedpereferenceTool.getUserInfo(getContext()),UserInfo.class);
        tvUid.setText("ID:"+info.getUid());
        tvNickName.setText(info.getNickName());
        ImageManager.Load(info.getPhotoUrl(),civPhotoUrl);
        tvDimension.setText(SharedpereferenceTool.getDimension(getContext()));
        tvBeautyPlan.setText(SharedpereferenceTool.getBeautyPlan(getContext()));
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_copy:
                copyUid();
                break;
            case R.id.ll_dimension:
                setDimension();
                break;
            case R.id.ll_beauty_plan:
                setBeautyPlan();
                break;
            case R.id.ll_record_file:
                gotoRecordFileActivity();
                break;
            case R.id.ll_SDK_version:
                gotoSDKVersionActivity();
                break;
            case R.id.ll_user_login_out:
                loginout();
                break;
            default:
                break;
        }
    }

    private void loginout() {
        // 清理登录状态,将用户信息移除
        WilddogSyncManager.getWilddogSyncTool().removeUserInfo(info.getUid());
        SharedpereferenceTool.setUserInfo(getContext(),"");
        SharedpereferenceTool.setLoginStatus(getContext(),false);
        startActivity(new Intent(getContext(), LoginActivity.class));
        getActivity().finish();
    }

    private void setDimension(){
        View view = View.inflate(getContext(), R.layout.popupwindow_dimension, null);
        TextView tvDimension360P = (TextView) view.findViewById(R.id.tv_dimesion_360P);
        TextView tvDimension480P = (TextView) view.findViewById(R.id.tv_dimesion_480P);
        TextView tvDimension720P = (TextView) view.findViewById(R.id.tv_dimension_720P);
        TextView tvDimension1080P = (TextView) view.findViewById(R.id.tv_dimesion_1080P);
        TextView tvDimensionCancel = (TextView) view.findViewById(R.id.tv_cancel);
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
    private void setBeautyPlan(){
        View view = View.inflate(getContext(), R.layout.popupwindow_beauty_plan, null);
        TextView tvCamera360 = (TextView) view.findViewById(R.id.tv_beauty_plan_Camera360);
        TextView tvTuSDK = (TextView) view.findViewById(R.id.tv_beauty_plan_TuSDK);
        TextView tvNothing = (TextView) view.findViewById(R.id.tv_beauty_nothing);
        TextView tvDimensionCancel = (TextView) view.findViewById(R.id.tv_cancel);
        tvCamera360.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveBeautyPlanAndChangeUI(getResources().getString(R.string.beauty_plan_Camera360));
                popupWindowDismiss();
            }
        });
        tvTuSDK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveBeautyPlanAndChangeUI(getResources().getString(R.string.beauty_plan_TuSDK));
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

    private void showPopupWindow(View view){
        popupWindow = new PopupWindow(view, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        popupWindow.setFocusable(true);
        popupWindow.showAtLocation(llParent, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
    }

    private void saveDimensionAndChangeUI(String dimension){
        SharedpereferenceTool.saveDimension(getContext(),dimension);
        tvDimension.setText(dimension);
    }
    private void saveBeautyPlanAndChangeUI(String beautyPlan){
        SharedpereferenceTool.saveBeautyPlan(getContext(),beautyPlan);
        tvBeautyPlan.setText(beautyPlan);
    }

    private void popupWindowDismiss(){
        if(popupWindow!=null && popupWindow.isShowing()){
            popupWindow.dismiss();
        }
    }

    private void copyUid(){
       getClipboardManager().setText(SharedpereferenceTool.getUserId(getContext()));
        AlertMessageUtil.showShortToast("复制成功");
    }

    private ClipboardManager getClipboardManager(){
        if(clipboardManager==null){
            clipboardManager = (ClipboardManager)getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        }
        return  clipboardManager;
    }

    private void gotoSDKVersionActivity(){
        startActivity(new Intent(getContext(), SDKVersionActivity.class));
    }

    private void gotoRecordFileActivity(){
        startActivity(new Intent(getContext(), RecordFileActivity.class));
    }
}
