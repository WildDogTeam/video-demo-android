package com.wilddog.demo;

import android.app.Application;
import android.content.Context;

import com.wilddog.demo.utils.Contants;

import org.lasque.tusdk.core.TuSdk;
import org.lasque.tusdk.core.TuSdkApplication;
import org.lasque.tusdk.core.utils.NativeLibraryHelper;

/**
 * Created by fly on 17-6-12.
 */

public class ConversationApplication extends Application {
    private static ConversationApplication application;

    public static ConversationApplication getInstance(){
        return application;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        application=this;
        // 自定义 .so 文件路径，在 init 之前调用
        /*NativeLibraryHelper.shared().mapLibrary(NativeLibraryHelper.NativeLibType.LIB_CORE, "jniLibs/arm64-v8a/libtusdk-library.so");
        NativeLibraryHelper.shared().mapLibrary(NativeLibraryHelper.NativeLibType.LIB_IMAGE, "jniLibs/arm64-v8a/libtusdk-image.so");*/
        /**
         *  初始化SDK，应用密钥是您的应用在 TuSDK 的唯一标识符。每个应用的包名(Bundle Identifier)、密钥、资源包(滤镜、贴纸等)三者需要匹配，否则将会报错。
         *
         *  @param appkey 应用秘钥 (请前往 http://tusdk.com 申请秘钥)
         */
        TuSdk.init(this.getApplicationContext(), Contants.TUSDK_KEY);

    }

    public static Context getContext() {
        return application.getApplicationContext();
    }
}
