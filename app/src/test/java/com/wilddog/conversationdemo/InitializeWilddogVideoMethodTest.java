package com.wilddog.conversationdemo;

import com.wilddog.video.BuildConfig;
import com.wilddog.video.WilddogVideo;
import com.wilddog.wilddogcore.WilddogApp;
import com.wilddog.wilddogcore.WilddogOptions;

import org.junit.Assert;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowApplication;

import static org.robolectric.Shadows.shadowOf;

/**
 * Created by fly on 17-6-9.
 *
 * 本类用来测试WilddogVideo的initializeWilddogVideo（）方法
 *
 * 本方法完成的操作有 判断传入参数context是否为空，初始化数据库,初始化WilddogClient对象
 *
 */




@RunWith(RobolectricTestRunner.class)
@org.robolectric.annotation.Config(constants = BuildConfig.class, sdk = 23)
public class InitializeWilddogVideoMethodTest {
    // 上下文环境
    ShadowApplication application = shadowOf(RuntimeEnvironment.application);
    // 前置条件，需要先初始化WilddogApp对象
    @Before
    public void setUp() {
        WilddogOptions.Builder builder = new WilddogOptions.Builder().setSyncUrl("http://" + Constants.appId + ".wilddogio.com");
        WilddogOptions options = builder.build();
        WilddogApp.initializeApp(application.getApplicationContext(), options);
    }

    //正常传入参数进行初始化
    public void testInitializeWilddogVideo(){
        WilddogVideo.initializeWilddogVideo(application.getApplicationContext(),Constants.appId);
    }
    //传入空的Context对象 期待结果 抛出IllegalArgumentException异常
    public void testInitializeWilddogVideoWithNullContext(){
        boolean isInitSuccess = false;
        try {
            WilddogVideo.initializeWilddogVideo(null,Constants.appId);
        }catch (Exception e){
            if(e instanceof IllegalArgumentException){
               isInitSuccess = ((IllegalArgumentException)e).getMessage().endsWith("Context 不能为null");
            }
        }

        // 等一段时间
        try {
            Thread.sleep(Constants.sleepTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // 判断结果
        Assert.assertTrue(isInitSuccess);


    }
    //传入空的appId
    //TODO SDK 中没有对appId进行空指针判断
    public void testInitializeWilddogVideoWithNullAppId(){
        WilddogVideo.initializeWilddogVideo(application.getApplicationContext(),null);
    }

}
