package com.wilddog.conversation.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.wilddog.conversation.activities.MainActivity;
import com.wilddog.conversation.bean.UserInfo;
import com.wilddog.conversation.utils.ActivityHolder;
import com.wilddog.conversation.utils.AlertMessageUtil;
import com.wilddog.conversation.utils.CollectionDeviceIdTool;
import com.wilddog.conversation.utils.Constant;
import com.wilddog.conversation.utils.ObjectAndStringTool;
import com.wilddog.conversation.utils.SharedPreferenceTool;
import com.wilddog.conversation.wilddog.WilddogAuthManager;
import com.wilddog.conversation.wilddog.WilddogSyncManager;
import com.wilddog.wilddogauth.core.Task;
import com.wilddog.wilddogauth.core.credentialandprovider.WeiXinAuthCredential;
import com.wilddog.wilddogauth.core.credentialandprovider.WeiXinAuthProvider;
import com.wilddog.wilddogauth.core.listener.OnCompleteListener;
import com.wilddog.wilddogauth.core.result.AuthResult;
import com.wilddog.wilddogauth.model.WilddogUser;

import org.json.JSONObject;

public class WXEntryActivity extends Activity implements IWXAPIEventHandler {
    private IWXAPI api;
    private JSONObject MyJsonObject;
    private static final String TAG = WXEntryActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        api = WXAPIFactory.createWXAPI(this, Constant.WX_APP_ID);
        api.registerApp(Constant.WX_APP_ID);
        api.handleIntent(getIntent(), this);
    }

    @Override
    public void onReq(BaseReq baseReq) {
        Log.d("WXEntryActivity", baseReq.getType() + ":" + baseReq.transaction + ":" + baseReq.openId);

    }

    @Override
    public void onResp(BaseResp baseResp) {
        Log.d("WXEntryActivity", baseResp.getType() + ":" + baseResp.transaction + ":" + baseResp.openId + ":" + baseResp.errCode + ":" + baseResp.errStr);
        switch (baseResp.errCode) {
            case BaseResp.ErrCode.ERR_BAN:
                AlertMessageUtil.showShortToast("你的签名id不正确,需要正式包");
                break;
            case BaseResp.ErrCode.ERR_OK:
                //成功
                if (ConstantsAPI.COMMAND_SENDMESSAGE_TO_WX == baseResp.getType()) {
                    //成功分享到微信
                    AlertMessageUtil.showShortToast("分享成功");
                } else {
                    //授权成功，获取token值
                    AlertMessageUtil.showShortToast("授权成功");
                    String code = ((SendAuth.Resp) baseResp).code;
                    Log.d("weixincode:", code);
                    WeiXinAuthCredential credential = (WeiXinAuthCredential) WeiXinAuthProvider.getCredential(code);
                    WilddogAuthManager.getWilddogAuth().signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // 成功
                                WilddogUser user = task.getResult().getWilddogUser();
                                SharedPreferenceTool.saveUserId(WXEntryActivity.this, user.getUid());
                                UserInfo info = new UserInfo();
                                info.setNickname(user.getDisplayName());
                                info.setUid(user.getUid());
                                info.setFaceurl(user.getPhotoUrl().toString());
                                info.setDeviceid(CollectionDeviceIdTool.getDeviceId());
                                // WilddogSyncManager.getWilddogSyncTool().writeToUser(user.getUid());
                                WilddogSyncManager.getWilddogSyncTool().writeToUserInfo(info);
                                SharedPreferenceTool.setUserInfo(WXEntryActivity.this, ObjectAndStringTool.getJsonFromObject(info));
                                SharedPreferenceTool.setLoginStatus(WXEntryActivity.this, true);
                                //TODO 需要记下所有的登录的用户的uid和昵称等用于推送
                                AlertMessageUtil.showShortToast("登录成功");
                                Constant.isLoginClickable = true;
                                AlertMessageUtil.dismissprogressbar();
                                startActivity(new Intent(WXEntryActivity.this, MainActivity.class));
                                //将登录界面关闭
                                ActivityHolder.finish();
                                // 将用户信息缓存
                            } else {
                                // 失败
                                Constant.isLoginClickable = true;
                                AlertMessageUtil.showShortToast("登录失败");
                                Log.e(TAG, task.getException().toString());
                            }
                        }
                    });

                }
                break;
            case BaseResp.ErrCode.ERR_AUTH_DENIED:
                //拒绝
                if (ConstantsAPI.COMMAND_SENDMESSAGE_TO_WX == baseResp.getType()) {
                    //取消分享到微信
                    AlertMessageUtil.showShortToast("用户拒绝分享到微信");
                } else {
                    //用户拒绝授权
                    Constant.isLoginClickable = true;
                    AlertMessageUtil.showShortToast("用户拒绝授权");
                    AlertMessageUtil.dismissprogressbar();
                }

                break;
            case BaseResp.ErrCode.ERR_USER_CANCEL:
                if (ConstantsAPI.COMMAND_SENDMESSAGE_TO_WX == baseResp.getType()) {
                    AlertMessageUtil.showShortToast("取消分享到微信");
                } else {
                    Constant.isLoginClickable = true;
                    AlertMessageUtil.showShortToast("取消授权");
                    AlertMessageUtil.dismissprogressbar();
                }

                break;
            default:
                if (ConstantsAPI.COMMAND_SENDMESSAGE_TO_WX == baseResp.getType()) {
                    Constant.isLoginClickable = true;
                    AlertMessageUtil.showShortToast("分享到微信出现未知错误");
                    AlertMessageUtil.dismissprogressbar();
                }else {
                    AlertMessageUtil.dismissprogressbar();
                }
                break;

        }
        finish();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        api.handleIntent(intent, this);
    }

}
