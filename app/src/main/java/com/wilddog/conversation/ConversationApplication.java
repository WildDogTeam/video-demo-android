package com.wilddog.conversation;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.ndk.CrashlyticsNdk;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;
import com.wilddog.conversation.utils.Constant;
import com.wilddog.conversation.utils.WXUtil;
import com.wilddog.toolbar.util.QiniuUtil;

import io.fabric.sdk.android.Fabric;
import org.lasque.tusdk.core.TuSdk;

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
        Fabric.with(this, new Crashlytics());
        super.onCreate();
        Fabric.with(this, new Crashlytics(), new CrashlyticsNdk());
        application=this;
        TuSdk.enableDebugLog(true);
        // 自定义 .so 文件路径，在 init 之前调用
        /*NativeLibraryHelper.shared().mapLibrary(NativeLibraryHelper.NativeLibType.LIB_CORE, "jniLibs/arm64-v8a/libtusdk-library.so");
        NativeLibraryHelper.shared().mapLibrary(NativeLibraryHelper.NativeLibType.LIB_IMAGE, "jniLibs/arm64-v8a/libtusdk-image.so");*/
        /**
         *  初始化SDK，应用密钥是您的应用在 TuSDK 的唯一标识符。每个应用的包名(Bundle Identifier)、密钥、资源包(滤镜、贴纸等)三者需要匹配，否则将会报错。
         *
         *  @param appkey 应用秘钥 (请前往 http://tusdk.com 申请秘钥)
         */
        TuSdk.init(this.getApplicationContext(), Constant.TUSDK_KEY);
        ImageLoaderConfiguration config;
        // imageloader
        DisplayImageOptions options=new DisplayImageOptions.Builder()
                .showStubImage(R.drawable.logo) // resource or drawable
                .showImageForEmptyUri(R.drawable.logo) // resource or// drawable
                .showImageOnFail(R.drawable.logo) // resource or
                // drawable
                .resetViewBeforeLoading(false) // default
                .delayBeforeLoading(200).cacheInMemory(true) // default
                .cacheOnDisk(true) // default
                .considerExifParams(false) // default
                .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2) // default
                .bitmapConfig(Bitmap.Config.RGB_565) // default
                .displayer(new SimpleBitmapDisplayer()) // default
                .handler(new Handler()) // default
                .build();
        config = new ImageLoaderConfiguration.Builder(getApplicationContext())
                .defaultDisplayImageOptions(options)
                .denyCacheImageMultipleSizesInMemory()
                .memoryCache(new LruMemoryCache((int) (Runtime.getRuntime().maxMemory()/4)))
                .memoryCacheSize(2 * 1024 * 1024)
                .discCacheSize(100 * 1024 * 1024)
                .denyCacheImageMultipleSizesInMemory()
                .discCacheFileNameGenerator(new Md5FileNameGenerator())
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .discCacheFileCount(100)
                .build();
        ImageLoader.getInstance().init(config);
        WXUtil.initWeixin(this);
        //初始化七牛云存储
        QiniuUtil.getInstance().init();
    }

    public static Context getContext() {
        return application.getApplicationContext();
    }
}
