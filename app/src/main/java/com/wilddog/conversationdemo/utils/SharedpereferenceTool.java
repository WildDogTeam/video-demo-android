package com.wilddog.conversationdemo.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by fly on 17-6-12.
 */

public class SharedpereferenceTool {
    private static SharedPreferences sp;
    private static final String FILE_NAME = "share_date";

    public static  void saveUserId(Context context,String uid){
        sp= context.getSharedPreferences(FILE_NAME,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("userId",uid);
        editor.commit();
    }

    public static String getUserId(Context context){
        String userId = null;
        sp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        userId = sp.getString("userId",null);
        return userId;
    }
    public static  void saveDimension(Context context,String dimension ){
        sp= context.getSharedPreferences(FILE_NAME,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("dimension",dimension);
        editor.commit();
    }

    public static String getDimension(Context context){
        String dimension = null;
        sp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        dimension = sp.getString("dimension","480P");
        return dimension;
    }
    public static  void saveBeautyPlan(Context context,String beautyPlan ){
        sp= context.getSharedPreferences(FILE_NAME,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("beautyPlan",beautyPlan);
        editor.commit();
    }

    public static String getBeautyPlan(Context context){
        String beautyPlan = null;
        sp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        beautyPlan = sp.getString("beautyPlan","Camera360P");
        return beautyPlan;
    }




}
