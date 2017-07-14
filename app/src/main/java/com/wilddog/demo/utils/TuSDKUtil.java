package com.wilddog.demo.utils;

import org.lasque.tusdk.core.seles.tusdk.SelesFilterProcessor;
import org.lasque.tusdk.core.struct.TuSdkSize;

/**
 * Created by fly on 17-7-14.
 */

public class TuSDKUtil {
    // TuSDK 滤镜引擎
    private static SelesFilterProcessor mFilterProcessor;

    public static void init(int width,int height){
        mFilterProcessor = new SelesFilterProcessor();
        // 预览尺寸传递给引擎
        mFilterProcessor.init(TuSdkSize.create(width, height));
        // 美颜滤镜
        mFilterProcessor.switchFilter("VideoFair");

    }

    public static void processFrame(byte[] bytes){
        if (mFilterProcessor != null)
        {
            mFilterProcessor.processData(bytes);
        }
    }


    public static  void destory(){
        if (mFilterProcessor != null)
        {
            mFilterProcessor.destroy();
            mFilterProcessor=null;
        }
    }
}
