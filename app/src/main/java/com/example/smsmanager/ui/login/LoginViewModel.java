package com.example.smsmanager.ui.login;

import androidx.annotation.RequiresApi;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Patterns;

import com.example.smsmanager.data.GlobalData;
import com.example.smsmanager.data.LoginRepository;
import com.example.smsmanager.data.Result;
import com.example.smsmanager.data.model.LoggedInUser;
import com.example.smsmanager.R;
import com.example.smsmanager.tools.MyApplication;
import com.example.smsmanager.tools.StringCheck;
import com.example.smsmanager.ui.main.MainActivity;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class LoginViewModel extends ViewModel {

    private final MutableLiveData<LoginFormState> loginFormState = new MutableLiveData<>();
    private final MutableLiveData<LoginResult> loginResult = new MutableLiveData<>();
    private final MutableLiveData<RegisterResult> registerResult = new MutableLiveData<>();
    private final LoginRepository loginRepository;

    LoginViewModel(LoginRepository loginRepository) {
        this.loginRepository = loginRepository;
    }

    LiveData<LoginFormState> getLoginFormState() {
        return loginFormState;
    }

    LiveData<LoginResult> getLoginResult() {
        return loginResult;
    }

    LiveData<RegisterResult> getRegisterResult() {
        return registerResult;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void login(Context context, String username, String password)  {

        Result<LoggedInUser> result = loginRepository.login(username, password);

        if (result instanceof Result.Success) {
            GlobalData.saveLoggedState(MyApplication.getContextObject(),username);
            LoggedInUser data = ((Result.Success<LoggedInUser>) result).getData();
            loginResult.setValue(new LoginResult(new LoggedInUserView(data.getDisplayName())));
            Intent intent = new Intent(context, MainActivity.class);
            //???????????????activity
            context.startActivity(intent);
            ((Activity)context).finish();
        } else {
            //??????????????????
            loginResult.setValue(new LoginResult(((Result.Error) result).getError().toString()));
        }
    }

    /**
     * ??????????????????
     * @param context ?????????
     * @param username ????????????
     * @param password ??????
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void register(Context context, String username, String password) {

        String result = loginRepository.register(username, password);

        if (result .equals("????????????")) {
            registerResult.setValue(new RegisterResult("????????????"));
        } else {
            registerResult.setValue(new RegisterResult("????????????"));
        }
    }

    /**
     * ?????????????????????????????????????????????
     * @param username ????????????
     * @param password ??????
     */
    public void loginDataChanged(String username, String password) {
        if (!isUserNameValid(username)) {
            loginFormState.setValue(new LoginFormState(R.string.invalid_username, null));
        } else if (!isPasswordValid(password)) {
            loginFormState.setValue(new LoginFormState(null, R.string.invalid_password));
        } else {
            loginFormState.setValue(new LoginFormState(true));
        }
    }

    /**
     * ???????????????
     * @param username ??????????????????
     * @return ??????
     */
    private boolean isUserNameValid(String username) {
        if (username == null) {
            return false;
        }
        if (StringCheck.isTelPhoneNumber(username)) {
            return true;
        } else {
            return !username.trim().isEmpty();
        }
    }

    /**
     * ????????????
     * @param password ????????????
     * @return ??????
     */
    private boolean isPasswordValid(String password) {
        return password != null && password.trim().length() > 5;
    }
}