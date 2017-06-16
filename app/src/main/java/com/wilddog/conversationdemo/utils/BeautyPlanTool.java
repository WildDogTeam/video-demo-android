package com.wilddog.conversationdemo.utils;

import com.wilddog.conversationdemo.ConversationApplication;

/**
 * Created by fly on 17-6-12.
 */

public class BeautyPlanTool {
  /*  //美颜 SDK
   static   PGPrettifySDK prettifySDK = new PGPrettifySDK(ConversationApplication.getContext());

    private static float mPinkValue = 1.0f;    //美颜 pink  取值范围：0f-1.0f
    private static float mWhitenValue = 1.0f;  //美颜 white 取值范围：0f-1.0f
    private static float mReddenValue = 1.0f;  //美颜 red   取值范围：0f-1.0f
    private static int mSoftenValue = 100;     //美颜程度    取值范围：0-100

    *
     * step 1
     * 初始化 美颜SDK



    public static void initEngine(boolean isInitEGL, int frameWidth, int frameHeight){
        // 初始化引擎
        if (AppConfig.IS_HAS_STICKER) {
            boolean b = prettifySDK.InitialiseEngine(AppConfig.SDK_KEY_NEW, isInitEGL, FileReadUtils.getFileContent(mContext, R.raw.megvii_facepp_model));//
        } else {
            boolean b = prettifySDK.InitialiseEngine(AppConfig.SDK_KEY_NEW, isInitEGL);
        }
        //引擎相关参数设置
        prettifySDK.SetSizeForAdjustInput(frameWidth, frameHeight);//调整输入帧的宽高
        prettifySDK.SetOrientForAdjustInput(PGSkinPrettifyEngine.PG_Orientation.PG_OrientationNormal);//设置输入帧的方向
        prettifySDK.SetOutputOrientation(PGSkinPrettifyEngine.PG_Orientation.PG_OrientationNormal);//设置输出帧的方向
        prettifySDK.SetOutputFormat(PGSkinPrettifyEngine.PG_PixelFormat.PG_Pixel_NV21);//设置美肤结果的输出格式
        prettifySDK.SetSkinSoftenStrength(mSoftenValue);//设置美颜程度
        prettifySDK.SetSkinColor(mPinkValue, mWhitenValue, mReddenValue);//设置美颜参数
        prettifySDK.SetColorFilterByName("Sexylips");//设置美颜滤镜，详细参数参考 AppConfig 中的 mFilterType
        prettifySDK.SetColorFilterStrength(mSoftenValue);//设置美颜强度
        prettifySDK.SetSkinSoftenAlgorithm(PGSkinPrettifyEngine.PG_SoftenAlgorithm.PG_SoftenAlgorithmContrast);//设置磨皮算法
    }

    *
     * step 2
     * 美颜 run
     *
     * @param data      数据流
     * @param textureId 外部纹理id


    public static void frameProcess(byte[] data, int textureId, boolean isFirstFrame, boolean isInitEGL, int frameWidth, int frameHeight) {

        if (isFirstFrame) initEngine(isInitEGL, frameWidth, frameHeight);//  在第一帧视频到来时，初始化，指定需要的输出大小以及方向
        //调整输入帧的格式
        prettifySDK.SetInputFrameByNV21(data, frameWidth, frameHeight);
        prettifySDK.RunEngine();
        prettifySDK.SetOutputOrientation(PGSkinPrettifyEngine.PG_Orientation.PG_OrientationNormal);
        //获取美颜结果
        byteBuffer = prettifySDK.SkinSoftenGetResult();
        byteBuffer.clear();
        //将美颜结果回写到相机帧数据 data（获取的原始帧数据，nv21 格式的 byte 数组） 中
        byteBuffer.get(data, 0, byteBuffer.capacity());
    }*/

}
