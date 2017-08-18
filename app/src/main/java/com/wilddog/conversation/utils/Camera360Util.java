package com.wilddog.conversation.utils;

import android.content.Context;

import com.wilddog.conversation.R;
import com.wilddog.conversation.bean.PrettifySDKContants;

import java.nio.ByteBuffer;

import us.pinguo.pgskinprettifyengine.PGSkinPrettifyEngine;
import us.pinguo.prettifyengine.PGPrettifySDK;
import us.pinguo.prettifyengine.utils.FileReadUtils;

/**
 * Created by fly on 17-7-13.
 */

public class Camera360Util {
    //
    private static PGPrettifySDK prettifySDK;
    private static ByteBuffer byteBuffer;


    /**
     * step 1
     * 初始化 美颜SDK
     */

    public static void initEngine(Context context, boolean isInitEGL, int frameWidth, int frameHeight, int rotation) {
         if(prettifySDK==null){
             prettifySDK = new PGPrettifySDK(context);
         }
        // 初始化引擎
        if (Contants.IS_HAS_STICKER) {
            boolean b = prettifySDK.InitialiseEngine(Contants.SDK_KEY_NEW, isInitEGL, FileReadUtils.getFileContent(context, R.raw.megvii_facepp_model));//
        } else {
            boolean b = prettifySDK.InitialiseEngine(Contants.SDK_KEY_NEW, isInitEGL);
        }
        //引擎相关参数设置
        prettifySDK.SetSizeForAdjustInput(frameWidth, frameHeight);//调整输入帧的宽高
        prettifySDK.SetOrientForAdjustInput(PGSkinPrettifyEngine.PG_Orientation.PG_OrientationNormal);//设置输入帧的方向
        prettifySDK.SetOutputOrientation(PGSkinPrettifyEngine.PG_Orientation.PG_OrientationNormal);//设置输出帧的方向
        prettifySDK.SetOutputFormat(PGSkinPrettifyEngine.PG_PixelFormat.PG_Pixel_NV21);//设置美肤结果的输出格式
        prettifySDK.SetSkinSoftenStrength(PrettifySDKContants.SOFTEN_VALUE);//设置美颜程度
        prettifySDK.SetSkinColor(PrettifySDKContants.PINK_VALUE, PrettifySDKContants.WHITE_VALUE, PrettifySDKContants.REDDEN_VALUE);//设置美颜参数
        // ""Skinbw
        prettifySDK.SetColorFilterByName("Grace");//设置美颜滤镜，详细参数参考中的 mFilterType
        prettifySDK.SetColorFilterStrength(PrettifySDKContants.SOFTEN_VALUE);//设置美颜强度
        prettifySDK.SetSkinSoftenAlgorithm(PGSkinPrettifyEngine.PG_SoftenAlgorithm.PG_SoftenAlgorithmContrast);//设置磨皮算法
    }

    public static void processFrame(byte[] data,int frameWidth, int frameHeight){
        //调整输入帧的格式
        prettifySDK.SetInputFrameByNV21(data, frameWidth, frameHeight);
        prettifySDK.RunEngine();
        //获取美颜结果
        byteBuffer = prettifySDK.SkinSoftenGetResult();
        byteBuffer.clear();
        //将美颜结果回写到相机帧数据 data（获取的原始帧数据，nv21 格式的 byte 数组） 中
        byteBuffer.get(data, 0, byteBuffer.capacity());
    }


}
