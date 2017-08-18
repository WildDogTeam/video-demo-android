package com.wilddog.conversation.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Administrator on 2015/12/17.
 */
public class String2DateUtil {
    /**
     * 将时间戳转为代表"距现在多久之前"的字符串
     * @param timeStr   时间戳
     * @return
     */
    public static String getStandardDate(String timeStr) {

        StringBuffer sb = new StringBuffer();

        long t = Long.parseLong(timeStr);
        long time = System.currentTimeMillis() - (t);
        long mill = (long) Math.floor(time / 1000);//秒前

        long minute = (long) Math.floor(time / 60 / 1000.0f);// 分钟前

        long hour = (long) Math.floor(time / 60 / 60 / 1000.0f);// 小时

        long day = (long) Math.floor(time/24/60/60/1000.0f);// 天前


         if (hour - 1 >0) {
            if (hour >= 24) {
                sb.append(format2Datetime(timeStr));
            } else {
                sb.append(hour + "小时");
            }
        } else if (minute - 1 > 0) {
            if (minute >60) {
                sb.append("1小时");
            } else if(minute<60){
                sb.append(minute + "分钟");
            }
        } else if (mill - 1 > 0) {
            if (mill == 60) {
                sb.append("1分钟");
            } else {
                sb.append(mill + "秒");
            }
        } else {
            sb.append("刚刚");
        }
        if (!sb.toString().equals("刚刚")&&!sb.toString().contains("-")) {
            sb.append("前");
        }
        return sb.toString();
    }

    public static final SimpleDateFormat datetimeFormat = new SimpleDateFormat(
            "yyyy年MM月dd日 HH:mm");

    public static String format2Datetime(String time) {
        Date date=new Date(Long.parseLong(time));
        return datetimeFormat2.format(date);
    }
    public static final SimpleDateFormat datetimeFormat2 = new SimpleDateFormat(
            "MM-dd HH:mm");
     public static final SimpleDateFormat onlyYearMonthDay = new SimpleDateFormat(
            "yyyy年MM月dd日");

    public static String formatDatetime(String time) {
        Date date=new Date(Long.parseLong(time));
        return datetimeFormat.format(date);
    }



    public static final SimpleDateFormat onlyhourandsecondtimeFormat = new SimpleDateFormat(
            "a  hh:mm");

    public static String formatpushDatetime(String time) {
        Date date = new Date(Long.parseLong(time));
        return onlyhourandsecondtimeFormat.format(date);
    }

    public static long hour2String(String s) {
        SimpleDateFormat sdf = new SimpleDateFormat("a  hh:mm");
        Date d = new Date();
        try {
            d = sdf.parse(s);
        } catch (Exception e) {

        }
        return d.getTime();
    }

    public static String timeaddone(String time){
        Long longtime= Long.parseLong(time);
        longtime++;
        return longtime+"";
    }
    public static String timeminusone(String time){
        Long longtime= Long.parseLong(time);
        longtime--;
        return longtime+"";
    }
   public static String getNowDate(){
       Date date=new Date(System.currentTimeMillis());
       return onlyYearMonthDay.format(date);
   }


}
