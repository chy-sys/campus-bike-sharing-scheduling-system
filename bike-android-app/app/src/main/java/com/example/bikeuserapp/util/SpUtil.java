package com.example.bikeuserapp.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SpUtil {
    private static final String SP_NAME = "bike_sp";
    private static SharedPreferences sp;

    //初始化
    public static void init(Context context){
        if(sp == null){
            sp = context.getApplicationContext().getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        }
    }

    //保存字符串
    public static void putString(String key,String value){
        sp.edit().putString(key,value).apply();
    }

    //读取字符串
    public static String getString(String key,String defValue){
        return sp.getString(key,defValue);
    }

    //清除全部（退出登录的时候调用）
    public static void clear(){
        SharedPreferences.Editor editor = sp.edit(); //拿到编辑器
        editor.clear(); //标记全部清空
        editor.apply(); //提交改动，写到本地
    }
    /*public static void clear(){
        sp.edit().clear().apply();}
*/
    //常量key，统一管理key名字，防止写错
    public static final String KEY_TOKEN = "token";
    public static final String KEY_USER_ID = "userId";
    public static final String KEY_USER_NAME = "userName";
    public static final String KEY_PHONE = "phone";

}