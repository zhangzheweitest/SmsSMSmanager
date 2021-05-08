package com.example.smsmanager.data;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.example.smsmanager.data.model.LoggedInUser;
import com.example.smsmanager.tools.MyApplication;
import com.example.smsmanager.tools.StringCheck;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * 对用户数据进行最终认证的类
 */
public class LoginDataSource {

    /**
     * 登录
     * @param username 电话号码
     * @param password 密码
     * @return 登陆最终结果
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public Result<LoggedInUser> login(String username, String password)  {
        if (!StringCheck.isTelPhoneNumber(username)) {
            return new Result.Error(new IOException("电话号码格式有误"));
        } else {
            int rs = GlobalData.isCorrect(MyApplication.getContextObject(), username, password);
            if (rs == 1) {
                LoggedInUser fakeUser =
                        new LoggedInUser(
                                java.util.UUID.randomUUID().toString(),
                                username);
                return new Result.Success<>(fakeUser);
            } else if (rs == 0) {
                return new Result.Error(new IOException("账号未注册过"));
            } else{
                return new Result.Error(new IOException("密码错误"));
            }
        }
    }


    /**
     * 注册
     * @param username 电话号码
     * @param password 密码
     * @return 最终结果
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public String register (String username, String password) {
        if(!StringCheck.isTelPhoneNumber(username)){
            return "电话号码格式有误";
        }else{
            if(GlobalData.saveUserInfo(MyApplication.getContextObject(),username,password)){
                return "注册成功";
            }else{
                return "注册失败";
            }
        }
    }

    public void logout() {
        // TODO: 撤销登录授权，未实现
    }
}