package com.wilddog.demo.utils;

import android.content.Context;

import com.wilddog.demo.R;

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
    //美颜 SDK
    public static float PINK_VALUE = 1.0f;    //美颜 pink  取值范围：0f-1.0f
    public static float WHITE_VALUE = 1.0f;  //美颜 white 取值范围：0f-1.0f
    public static float REDDEN_VALUE = 1.0f;  //美颜 red   取值范围：0f-1.0f
    public static int SOFTEN_VALUE = 100;     //美颜程度    取值范围：0-100
    //滤镜列表 17/02/27
    public static String[] mFilterName = {"深度美白", "清新丽人", "暖暖阳光", "香艳红唇", "艺术黑白", "温暖", "果冻", "甜美", "唯美", "淡雅", "清新", "电影（Lomo）", "电影色FM2", "电影色FM7", "Vista"};
    public static String[] mFilterType = {"Deep", "Skinfresh", "Sunshine", "Sexylips", "Skinbw", "Lightwarm", "Jelly", "Sweet", "Grace", "Elegant", "Fresh",
            "Movie", "FM2", "FM7", "Vista"};
    public static final boolean IS_HAS_STICKER = false;

    /**
     * step 1
     * 初始化 美颜SDK
     */

    public static void initEngine(Context context, boolean isInitEGL, int frameWidth, int frameHeight, int rotation) {
         if(prettifySDK==null){
             prettifySDK = new PGPrettifySDK(context);
         }
        // 初始化引擎
        if (IS_HAS_STICKER) {
            boolean b = prettifySDK.InitialiseEngine(Contants.SDK_KEY_NEW, isInitEGL, FileReadUtils.getFileContent(context, R.raw.megvii_facepp_model));//
        } else {
            boolean b = prettifySDK.InitialiseEngine(Contants.SDK_KEY_NEW, isInitEGL);
        }
        //引擎相关参数设置
        prettifySDK.SetSizeForAdjustInput(frameWidth, frameHeight);//调整输入帧的宽高
        prettifySDK.SetOrientForAdjustInput(PGSkinPrettifyEngine.PG_Orientation.PG_OrientationNormal);//设置输入帧的方向
        prettifySDK.SetOutputOrientation(PGSkinPrettifyEngine.PG_Orientation.PG_OrientationNormal);//设置输出帧的方向
        prettifySDK.SetOutputFormat(PGSkinPrettifyEngine.PG_PixelFormat.PG_Pixel_NV21);//设置美肤结果的输出格式
        prettifySDK.SetSkinSoftenStrength(SOFTEN_VALUE);//设置美颜程度
        prettifySDK.SetSkinColor(PINK_VALUE, WHITE_VALUE, REDDEN_VALUE);//设置美颜参数

        prettifySDK.SetColorFilterByName("Grace");//设置美颜滤镜，详细参数参考中的 mFilterType
        prettifySDK.SetColorFilterStrength(SOFTEN_VALUE);//设置美颜强度
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
