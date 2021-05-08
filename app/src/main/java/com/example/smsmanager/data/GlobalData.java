package com.example.smsmanager.data;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.example.smsmanager.tools.EncryptAndDecryptUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 全局变量类，包含一些常量和SharedPreferences类，存储用户信息，登录or注册认证
 */
public class GlobalData {

    //短息数据库一些列的名字
    public static final String _ID = "_id";
    public static final String KEY_THREAD_ID = "thread_id";
    public static final String KEY_ADDRESS = "address";
    public static final String KEY_TYPE = "type";
    public static final String KEY_DATE = "date";
    public static final String KEY_MSG_BODY = "body";
    //短信发送状态
    public static boolean isSmsSuccess;


    public SharedPreferences sp;

    public SharedPreferences getSp() {
        return sp;
    }

    public void setSp(SharedPreferences sp) {
        this.sp = sp;
    }

    /**
     * 保存信息
     * @param context 上下文
     * @param n 电话号码
     * @param password 密码
     * @return 保存结果
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static boolean saveUserInfo(Context context, String n, String password){
        SharedPreferences sp = context.getSharedPreferences("data", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        edit.putString(n, n);
        edit.putString(n+"password", password);
        edit.apply();
        return true;
    }

    /**
     * 认证方法
     * @param context 上下文
     * @param name 电话号码
     * @param password 密码
     * @return 验证结果
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static int isCorrect(Context context, String name, String password) {
        SharedPreferences sp = context.getSharedPreferences("data", Context.MODE_PRIVATE);
        if(sp.getString(name, null)==null){
            return 0;
        }else{
            if(sp.getString(name+"password", null).equals(password)){
                return 1;
            }else{
                return 2;
            }
        }
    }

    /**
     * 清除登录数据
     * @param context 上下文
     * @param name 用户名
     */
    public static void removeData(Context context,String name){
        SharedPreferences.Editor editor = context.getSharedPreferences("data", Context.MODE_PRIVATE).edit();
        editor.remove(name);
        editor.remove(name+"password");
        editor.remove("Logged");
        editor.apply();
        editor.commit();
    }

    /**
     * 存储登录状态
     * @param context 上下文
     */
    public static void saveLoggedState(Context context,String name){
        SharedPreferences sp = context.getSharedPreferences("data", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        edit.putBoolean("Logged", true);
        edit.putString("LoggedUser",name);
        edit.apply();
    }

    /**
     * 设置单片机号码
     * @param context 上下文
     * @param n 号码值
     * @return 执行结果
     */
    public static boolean setCarPhoneNumber(Context context,String n){
        SharedPreferences sp = context.getSharedPreferences("data", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        edit.putString("car_phone_number",n);
        edit.apply();
        return sp.getString("car_phone_number", null) != null;
    }

    public static String getCarPhoneNumber(Context context){
        SharedPreferences sp = context.getSharedPreferences("data", Context.MODE_PRIVATE);
        return sp.getString("car_phone_number",null);
    }

    /**
     * 获取登录状态
     * @param context 上下文
     * @return 登录状态
     */
    public static boolean getLoggedState(Context context){
        SharedPreferences sp = context.getSharedPreferences("data", Context.MODE_PRIVATE);
        return sp.getBoolean("Logged", false);
    }

    public static String getLoggedUserName(Context context){
        SharedPreferences sp = context.getSharedPreferences("data", Context.MODE_PRIVATE);
        return sp.getString("LoggedUser", null);
    }

    public static void saveAuth(Context context, String n){
        SharedPreferences sp = context.getSharedPreferences("data", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        edit.putString("密钥", n);
        edit.apply();
    }

    public static Date getAuth(Context context) throws ParseException {
        SharedPreferences sp = context.getSharedPreferences("data", Context.MODE_PRIVATE);

        if(sp.getString("密钥",null)==null)return null;
        else {
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat dateFormat = new SimpleDateFormat(
                    "yyyy-MM-dd hh:mm:ss");
            return dateFormat.parse(EncryptAndDecryptUtils.desDecrypt(sp.getString("密钥",null),null));
        }

    }

}
