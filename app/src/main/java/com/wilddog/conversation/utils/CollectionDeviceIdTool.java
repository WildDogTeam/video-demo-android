package com.wilddog.conversation.utils;

import android.os.Build;
import android.util.Log;

import java.util.UUID;

/**
 * Created by fly on 17-9-4.
 */

public class CollectionDeviceIdTool {
    public static String getDeviceId(){
        String serial = null;
        String deviceId =null;

        /*
        * BOARD 主板：The name of the underlying board, like goldfish.
        * BRAND 系统定制商：The consumer-visible brand with which the product/hardware will be associated, if any.
        * CPU_ABI cpu指令集：The name of the instruction set (CPU type + ABI convention) of native code.
        * DEVICE 设备参数：The name of the industrial design.
        * DISPLAY 显示屏参数：A build ID string meant for displaying to the user
        * HOST
        * ID 修订版本列表：Either a changelist number, or a label like M4-rc20.
        * MANUFACTURER 硬件制造商：The manufacturer of the product/hardware.
        * MODEL 版本即最终用户可见的名称：The end-user-visible name for the end product.
        * PRODUCT 整个产品的名称：The name of the overall product.
        * TAGS 描述build的标签,如未签名，debug等等。：Comma-separated tags describing the build, like unsigned,debug.
        * TYPE build的类型：The type of build, like user or eng.
        * USER
        * SERIAL 硬件序列号：RCZHWOPJKZLNEYIZ
        * */
        String m_szDevIDShort = "35" +
                Build.BOARD.length()%10+ Build.BRAND.length()%10 +

                Build.CPU_ABI.length()%10 + Build.DEVICE.length()%10 +

                Build.DISPLAY.length()%10 + Build.HOST.length()%10 +

                Build.ID.length()%10 + Build.MANUFACTURER.length()%10 +

                Build.MODEL.length()%10 + Build.PRODUCT.length()%10 +

                Build.TAGS.length()%10 + Build.TYPE.length()%10 +

                Build.USER.length()%10 ; //13 位
        Log.d("result", "deviceInfo::BOARD:"+Build.BOARD+" BRAND:"+Build.BRAND+" CPU_ABI:"+Build.CPU_ABI+
                " DEVICE:"+Build.DEVICE+" DISPLAY:"+Build.DISPLAY+ " HOST:"+Build.HOST
                +" ID:"+Build.ID+" MANUFACTURER:"+Build.MANUFACTURER+ " MODEL:"+Build.MODEL
                +" PRODUCT:"+Build.PRODUCT+" TAGS:"+Build.TAGS+ " TYPE:"+Build.TYPE+
                " USER:"+Build.USER);
        Log.d("result","m_szDevIDShort:"+m_szDevIDShort);
        try {
            serial = Build.class.getField("SERIAL").get(null).toString();
            Log.d("result","serial:"+serial);
            //API>=9 使用serial号
        } catch (Exception exception) {
            //serial需要一个初始化
            serial = "serial"; // 随便一个初始化
            //使用硬件信息拼凑出来的15位号码
            Log.d("result","serial:"+serial);
        }
        deviceId= new UUID(m_szDevIDShort.hashCode(), serial.hashCode()).toString();
        Log.d("result","deviceId:"+deviceId);
        return deviceId;

    }

}
