package com.example.smsmanager.ui.login;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import android.provider.Telephony;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.smsmanager.R;
import com.example.smsmanager.data.GlobalData;
import com.example.smsmanager.tools.GrantAuthorization;
import com.example.smsmanager.tools.MyApplication;
import com.example.smsmanager.ui.main.MainActivity;

import java.lang.reflect.Field;
import java.text.ParseException;

/**
 * 注册登录类
 */
public class LoginActivity extends AppCompatActivity {


    private LoginViewModel loginViewModel;//UI界面元素与用户数据控制媒介
    private final String[] PERMISSIONS = new String[]{Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS, Manifest.permission.SEND_SMS,Manifest.permission.READ_PHONE_STATE};//App所需申请权限
    private static final int REQUEST_PERMISSION_KEY = 0;
    private static boolean AUTHORIZATION = false;
    private AlertDialog d;

    @SuppressLint("ShowToast")
    @Override
    protected void onResume() {
        super.onResume();
        //去请求权限
        if(!hasPermissions(this, PERMISSIONS)){
            ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_PERMISSION_KEY);
        }
    }


    /**
     * 权限请求回调类
     * @param requestCode 请求码
     * @param permissions 权限
     * @param grantResults 授权结果
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSION_KEY) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast toast = Toast.makeText(MyApplication.getContextObject(), "功能权限授予成功", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.TOP, 0, 200);
                toast.show();
            } else {
                Toast toast = Toast.makeText(MyApplication.getContextObject(), "必须授予相关权限才能正常使用此App", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.TOP, 0, 200);
                toast.show();
                LoginActivity.this.finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @SuppressLint("ShowToast")
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            AUTHORIZATION=GrantAuthorization.grant(GlobalData.getAuth(MyApplication.getContextObject()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if(GlobalData.getLoggedState(MyApplication.getContextObject())&&AUTHORIZATION){
            Log.w("调试：","登录过且未过期，进入MainActivity");
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            //启动下一个activity
            startActivity(intent);
            finish();
        }else if(GlobalData.getLoggedState(MyApplication.getContextObject())&&!AUTHORIZATION){
            Log.w("调试：","登录过但已过期。");
            AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
            builder.setTitle(R.string.authorization_warn);
            builder.setMessage("请联系技术员索要权限授予");
            builder.setCancelable(false);
            View view = LayoutInflater.from(LoginActivity.this).inflate(R.layout.authorization_dialog, null);
            //    设置我们自己定义的布局文件作为弹出框的Content
            builder.setView(view);
            final EditText authorization = (EditText)view.findViewById(R.id.authorization);
            builder.setPositiveButton("确定", null);
            builder.setNegativeButton("取消", (dialog, which) -> finish());
            AlertDialog dialog =builder.create();
            dialog.show();
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String a = authorization.getText().toString().trim();
                Toast toast;
                if(a==null || a.equals("")){
                    toast = Toast.makeText(MyApplication.getContextObject(), "授权码为空，授权失败", Toast.LENGTH_LONG);
                }else{
                    GlobalData.saveAuth(MyApplication.getContextObject(),a);
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    toast = Toast.makeText(MyApplication.getContextObject(), "使用授权成功", Toast.LENGTH_SHORT);
                    dialog.dismiss();
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    //启动下一个activity
                    startActivity(intent);
                    finish();
                }
                toast.setGravity(Gravity.TOP, 0, 200);
                toast.show();
            });
        }else{
            Log.w("调试：","未登录过未过期，正常LoginActivity");
            setContentView(R.layout.activity_login);
            loginViewModel = new ViewModelProvider(this, new LoginViewModelFactory())
                    .get(LoginViewModel.class);

            final EditText usernameEditText = findViewById(R.id.username);
            final EditText passwordEditText = findViewById(R.id.password);
            final Button loginButton = findViewById(R.id.login);
            final Button registerButton = findViewById(R.id.register);

            loginViewModel.getLoginFormState().observe(this, new Observer<LoginFormState>() {
                @Override
                public void onChanged(@Nullable LoginFormState loginFormState) {
                    if (loginFormState == null) {
                        return;
                    }
                    loginButton.setEnabled(loginFormState.isDataValid());
                    registerButton.setEnabled(loginFormState.isDataValid());
                    if (loginFormState.getUsernameError() != null) {
                        usernameEditText.setError(getString(loginFormState.getUsernameError()));
                    }
                    if (loginFormState.getPasswordError() != null) {
                        passwordEditText.setError(getString(loginFormState.getPasswordError()));
                    }
                }
            });

            loginViewModel.getLoginResult().observe(this, new Observer<LoginResult>() {
                @Override
                public void onChanged(@Nullable LoginResult loginResult) {
                    if (loginResult == null) {
                        return;
                    }

                    if(loginResult.getWarn()!=null){
                        Toast.makeText(getApplicationContext(), loginResult.getWarn().split(":")[1], Toast.LENGTH_SHORT).show();
                        if(loginResult.getWarn().split(":")[1].equals(" 账号未注册过")){
                            loginButton.setEnabled(true);
                            registerButton.setEnabled(true);
                        }else if("电话号码格式有误".equals(loginResult.getWarn().split(":")[1])){
                            loginButton.setEnabled(true);
                            registerButton.setEnabled(true);
                        }else{
                            loginButton.setEnabled(true);
                            registerButton.setEnabled(true);
                        }
                    }
                    if (loginResult.getSuccess() != null) {
                        updateUiWithUser(loginResult.getSuccess());
                    }
                    setResult(Activity.RESULT_OK);
                }
            });

            loginViewModel.getRegisterResult().observe(this, (Observer<RegisterResult>) registerResult -> {
                if (registerResult == null) {
                    return;
                }


                if (registerResult.getResult().equals("注册成功")) {
                    Toast.makeText(getApplicationContext(), "注册成功，可进行登录", Toast.LENGTH_LONG).show();
                    loginButton.setEnabled(true);
                    registerButton.setEnabled(false);
                }else if (registerResult.getResult().equals("注册失败")){
                    Toast.makeText(getApplicationContext(), "注册失败", Toast.LENGTH_LONG).show();
                }
                setResult(Activity.RESULT_OK);
            });

            TextWatcher afterTextChangedListener = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    // ignore
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // ignore
                }

                @Override
                public void afterTextChanged(Editable s) {
                    loginViewModel.loginDataChanged(usernameEditText.getText().toString(),
                            passwordEditText.getText().toString());
                }
            };
            usernameEditText.addTextChangedListener(afterTextChangedListener);
            passwordEditText.addTextChangedListener(afterTextChangedListener);
            passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    // 键盘上敲击回车后默认会进行登录
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        loginViewModel.login(LoginActivity.this,usernameEditText.getText().toString(),
                                passwordEditText.getText().toString());
                    }
                    return false;
                }
            });

            loginButton.setOnClickListener(v -> {
                hideInput();
                loginViewModel.login(LoginActivity.this,usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            });
            registerButton.setOnClickListener(v -> {
                hideInput();
                loginViewModel.register(LoginActivity.this,usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            });

            if(!AUTHORIZATION){
                Log.w("调试：","未登陆过，过期了");
                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                builder.setTitle(R.string.authorization_warn);
                builder.setMessage("请联系技术员索要权限授予");
                builder.setCancelable(false);
                View view = LayoutInflater.from(LoginActivity.this).inflate(R.layout.authorization_dialog, null);
                //    设置我们自己定义的布局文件作为弹出框的Content
                builder.setView(view);
                final EditText authorization = (EditText)view.findViewById(R.id.authorization);
                builder.setPositiveButton("确定", null);
                builder.setNegativeButton("取消", (dialog, which) -> finish());
                AlertDialog dialog =builder.create();
                dialog.show();
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                    String a = authorization.getText().toString().trim();
                    Toast toast;
                    if(a==null || a.equals("")){
                        toast = Toast.makeText(MyApplication.getContextObject(), "授权码为空，授权失败", Toast.LENGTH_LONG);
                    }else{
                        GlobalData.saveAuth(MyApplication.getContextObject(),a);
                        toast = Toast.makeText(MyApplication.getContextObject(), "使用授权成功", Toast.LENGTH_SHORT);
                        dialog.dismiss();
                    }
                    toast.setGravity(Gravity.TOP, 0, 200);
                    toast.show();
                });
            }

        }


    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 当按下返回键时所执行的命令
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Log.w("调试：","LoginActivity按下返回按钮");
            return super.onKeyDown(keyCode, event);
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 权限判断
     * @param context 上下文
     * @param permissions 待申请权限
     * @return 是否已经申请权限
     */
    public static boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 显示键盘
     * @param et 输入焦点
     */
    public void showInput(final EditText et) {
        et.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.showSoftInput(et, InputMethodManager.SHOW_IMPLICIT);
    }

    /**
     * 隐藏键盘
     */
    protected void hideInput() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        View v = getWindow().peekDecorView();
        if (null != v) {
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }

    /**
     * 登陆后弹出提示框
     * @param model 用户实体类
     */
    private void updateUiWithUser(LoggedInUserView model) {
        String welcome = getString(R.string.welcome) + model.getDisplayName();
        // TODO : initiate successful logged in experience
        Toast toast =Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER,0,0);
        toast.show();
    }

    private void showLoginFailed(@StringRes Integer errorString) {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
    }
}