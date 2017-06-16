package com.wilddog.conversationdemo.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.wilddog.client.SyncError;
import com.wilddog.client.SyncReference;
import com.wilddog.client.WilddogSync;
import com.wilddog.conversationdemo.R;
import com.wilddog.conversationdemo.utils.AlertMessageUtil;
import com.wilddog.conversationdemo.utils.PermissionHelper;
import com.wilddog.conversationdemo.utils.SharedpereferenceTool;
import com.wilddog.conversationdemo.wilddogAuth.WilddogAuthManager;
import com.wilddog.wilddogauth.core.Task;
import com.wilddog.wilddogauth.core.listener.OnCompleteListener;
import com.wilddog.wilddogauth.core.result.AuthResult;
import com.wilddog.wilddogauth.model.WilddogUser;

import android.Manifest;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = LoginActivity.class.getName();

    private Button login;
    private boolean isLoginClickable = true;

    private static final int REQUEST_CODE = 0; // 请求码

    static final String[] PERMISSIONS = new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        login = (Button) findViewById(R.id.btn_login);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //点击登录
                if (!isLoginClickable) return;
                isLoginClickable = false;
                login();

            }
        });
        //动态申请权限
        int sdk = android.os.Build.VERSION.SDK_INT;
        //6.0系统不提示权限授予，手动弹出授予权限页面
        if (sdk >= 23) {
            Log.d(TAG, "Current Android system is uper 23");
            Intent intent = new Intent(this, PermissionActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Bundle bundle = new Bundle();
            bundle.putStringArray("permission", PERMISSIONS);
            PermissionActivity.startActivityForResult(this, REQUEST_CODE, PERMISSIONS);
        }
    }

    //退出动画
    @Override
    public void finish() {
        super.finish();
        this.overridePendingTransition(0, R.anim.bottom_out);
    }

    private void login() {
        WilddogAuthManager.getWilddogAuth(LoginActivity.this).signInAnonymously().addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // 成功
                    WilddogUser user = task.getResult().getWilddogUser();
                    SharedpereferenceTool.saveUserId(LoginActivity.this, user.getUid());
                    writeToUser(user.getUid());
                    AlertMessageUtil.showShortToast("登录成功");
                    isLoginClickable = true;
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                } else {
                    // 失败
                    isLoginClickable = true;
                    AlertMessageUtil.showShortToast("登录失败");
                }

            }
        });
    }

    private void writeToUser(String uid) {
        WilddogSync wilddogSync = WilddogSync.getInstance();
        SyncReference syncReference = wilddogSync.getReference();
        syncReference.child("onlineusers/" + uid).setValue(true, new SyncReference.CompletionListener() {
            @Override
            public void onComplete(SyncError error, SyncReference ref) {
                if (error != null) {
                    Log.e("LoginActivity", error.toString());
                }
            }
        });
        syncReference.child("onlineusers/" + uid).onDisconnect().removeValue();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 拒绝时, 关闭页面, 缺少主要权限, 无法运行
        if (requestCode == REQUEST_CODE && resultCode == PermissionHelper.PERMISSIONS_DENIED) {
            AlertMessageUtil.showShortToast("PERMISSIONS_DENIED ,User refuse to give Permission ");
            finish();
        } else {
            Log.i(TAG, "Get Permission success");
        }
    }


}
