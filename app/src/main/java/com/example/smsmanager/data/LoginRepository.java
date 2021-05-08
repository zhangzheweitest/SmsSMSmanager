package com.example.smsmanager.data;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.example.smsmanager.data.model.LoggedInUser;

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
 * 该类从远程数据源请求身份验证和用户信息，并维护登录状态和用户凭据信息的内存缓存。
 */
public class LoginRepository {

    private static volatile LoginRepository instance;

    private final LoginDataSource dataSource;

    private LoggedInUser user = null;

    private LoginRepository(LoginDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public static LoginRepository getInstance(LoginDataSource dataSource) {
        if (instance == null) {
            instance = new LoginRepository(dataSource);
        }
        return instance;
    }

    public boolean isLoggedIn() {
        return user != null;
    }

    public void logout() {
        user = null;
        dataSource.logout();
    }

    /**
     * 保存用户登录状态、数据（未实现）
     * @param user 登录用户数据类
     */
    private void setLoggedInUser(LoggedInUser user) {
        this.user = user;
        //todo：建议数据进行加密处理，是否需要复杂化处理？
    }

    /**
     * 登录维护方法
     * @param username 电话号码
     * @param password 密码
     * @return 登陆结果
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public Result<LoggedInUser> login(String username, String password) {
        Result<LoggedInUser> result = dataSource.login(username, password);
        if (result instanceof Result.Success) {
            setLoggedInUser(((Result.Success<LoggedInUser>) result).getData());
        }
        return result;
    }

    /**
     * 注册维护方法
     * @param username 电话号码
     * @param password 密码
     * @return 注册结果
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public String register(String username, String password) {
        return dataSource.register(username, password);
    }
}