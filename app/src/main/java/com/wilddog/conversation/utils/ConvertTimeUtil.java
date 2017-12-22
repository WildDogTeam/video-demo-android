package com.wilddog.conversation.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Administrator on 2017/1/13.
 */
public class ConvertTimeUtil {
    public static long hourValue = 60*60*1000;
    public static long minuteValue =60*1000;
    public static final String yyyyMMddWWHHmm = "yyyy-MM-dd(E)  HH:mm";
    public static final String yyyyMMdd = "yyyyMMdd";
    public static final String HHmm = "HH:mm";
    public static final String yyyyMMddHHmm = "yyyy/MM/dd HH:mm";

    public static String longToDurationString(long duration){
        int h = (int) (duration/hourValue);
        int m = (int)((duration%hourValue)/minuteValue);
        return  h+"小时"+m+"分钟";
    }

    public static String longToTimeString(long time){
        SimpleDateFormat sdf = new SimpleDateFormat(yyyyMMddWWHHmm);
        Date date = new Date(time);
      return   sdf.format(date).toString();

    }

    private static String longToDayString(long time){
        SimpleDateFormat sdf = new SimpleDateFormat(yyyyMMdd);
        Date date = new Date(time);
        return   sdf.format(date).toString();
    }

    public static String getDayString(long time){
       return longToDayString(time);
    }

    public static String getHourAndMinute(long time){
        SimpleDateFormat sdf = new SimpleDateFormat(HHmm);
        Date date = new Date(time);
        return   sdf.format(date).toString();
    }

    public static String getYyyyMMddHHmm(long time){
        SimpleDateFormat sdf = new SimpleDateFormat(yyyyMMddHHmm);
        Date date = new Date(time);
        return   sdf.format(date).toString();
    }



    public static String secToTime(int time) {
        String timeStr = null;
        int hour = 0;
        int minute = 0;
        int second = 0;
        if (time <= 0)
            return "00:00:00";
        else {
            minute = time / 60;
            if (minute < 60) {
                second = time % 60;
                timeStr = unitFormat(minute) + ":" + unitFormat(second);
            } else {
                hour = minute / 60;
                if (hour > 99)
                    return "99:59:59";
                minute = minute % 60;
                second = time - hour * 3600 - minute * 60;
                timeStr = unitFormat(hour) + ":" + unitFormat(minute) + ":" + unitFormat(second);
            }
        }
        return timeStr;
    }

    public static String unitFormat(int i) {
        String retStr = null;
        if (i >= 0 && i < 10)
            retStr = "0" + Integer.toString(i);
        else
            retStr = "" + i;
        return retStr;
    }

    public static long getLastSecondToday()  {

        DateFormat format=new SimpleDateFormat("yyyy-MM-dd");
        Date d=new Date();
        String str=format.format(d);
        Date d2= null;
        try {
            d2 = format.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        int dayMis=1000*60*60*24;
        long curMillisecond=d2.getTime();//当天的毫秒
        long resultMis=curMillisecond+(dayMis-1);
        return  resultMis;
    }

}
