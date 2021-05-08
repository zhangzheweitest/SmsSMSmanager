package com.example.smsmanager.ui.main;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smsmanager.R;
import com.example.smsmanager.data.GlobalData;
import com.example.smsmanager.tools.GrantAuthorization;
import com.example.smsmanager.tools.MyApplication;
import com.example.smsmanager.tools.SMSBroadcastReceiver;
import com.example.smsmanager.tools.SMSService;
import com.example.smsmanager.tools.VibratorUtil;
import com.example.smsmanager.ui.login.LoginActivity;
import com.hjq.bar.ITitleBarStyle;
import com.hjq.bar.OnTitleBarListener;
import com.hjq.bar.TitleBar;

import java.lang.reflect.Field;
import java.text.ParseException;

public class MainActivity extends AppCompatActivity implements SMSBroadcastReceiver.SmsListener{

    @SuppressLint("StaticFieldLeak")
    private static TextView notificationView;
    @SuppressLint("StaticFieldLeak")
    private static ImageView alertView;
    private static boolean isDisplay=false;//告警信息是否已经展示出来
    public static boolean isGot=true;//是否要开始获取信息
    public static boolean AUTH=false;
    public static  boolean isShowAlertDialog=false;
    Thread thread;
    private static boolean isStop=false;
    //以下两个变量虽然没用到，如果app要以作为默认短信管理软件，在定义对应广播或是服务时是必须的
    public static final String CLASS_SMS_MANAGER = "com.android.internal.telephony.SmsApplication";
    public static final String METHOD_SET_DEFAULT = "setDefaultApplication";
    @SuppressLint("HandlerLeak")
    static Handler mHandler = new Handler(){
        public void handleMessage( Message msg) {
            super.handleMessage(msg);
            notificationView.setText((String)msg.obj);
            Log.w("调试：","状态改变成功，信息展示成功");
            if(msg.what==0x001){
                alertView.setImageResource(R.drawable.ic_normal);
            }else if(msg.what==0x002){
                isDisplay=true;
                alertView.setImageResource(R.drawable.ic_alert);
            }

        }
    };

    /**
     * 弹出弹窗
     */
    public void showTestDialog() {
        mHandler.post((Runnable) () -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle(R.string.authorization_warn);
            builder.setMessage("请联系技术员索要权限授予");
            builder.setCancelable(false);
            View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.authorization_dialog, null);
            //    设置我们自己定义的布局文件作为弹出框的Content
            builder.setView(view);
            final EditText authorization = (EditText)view.findViewById(R.id.authorization);
            builder.setPositiveButton("确定", null);
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    isShowAlertDialog=false;
                    isStop=true;
                    finish();
                }
            });
            AlertDialog dialog =builder.create();
            dialog.show();
            isShowAlertDialog=true;
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String a = authorization.getText().toString().trim();
                Toast toast;
                if(a==null || a.equals("")){
                    toast = Toast.makeText(MyApplication.getContextObject(), "授权码为空，授权失败", Toast.LENGTH_LONG);
                }else{
                    GlobalData.saveAuth(MyApplication.getContextObject(),a);
                    toast = Toast.makeText(MyApplication.getContextObject(), "使用授权成功", Toast.LENGTH_SHORT);
                    dialog.dismiss();
                    isShowAlertDialog=false;
                }
                toast.setGravity(Gravity.TOP, 0, 200);
                toast.show();
            });
        });
    }

    /**
     * 短信检测
     */
    public void detectSms() {
        isStop=false;
        //todo:若一直不发送开关窗短信，程序会一直阻塞在此，不会有多余动作，生产消费者模型，接收告警（生产），发送短信（消费）
        //todo:线程重复被开启的问题
        if(thread==null){
            thread=new Thread(() -> {
                while(!isStop){
                    if(GlobalData.isSmsSuccess){//短息发送成功，更新以下这几个状态
                        isGot=true;
                        isDisplay=false;
                        GlobalData.isSmsSuccess=false;
                    }
                    try {
                        AUTH=GrantAuthorization.grant(GlobalData.getAuth(MyApplication.getContextObject()));
                        Log.w("调试：","认证情况"+ AUTH);
                    } catch (ParseException e) {
                        e.printStackTrace();
                        Log.w("调试：","获取本地权限失败");
                    }
                    if(!AUTH&&!isShowAlertDialog){
                        isShowAlertDialog=true;
                        Log.w("调试：","使用途中过期了");
                        showTestDialog();
                    }else{
                        if(!isShowAlertDialog&&isGot){
                            Log.w("调试：","检测进程，isGot（是否已经遍历？）"+isGot);
                            StringBuilder s= SMSService.getSmsInPhone(MyApplication.getContextObject());

                            if(s==null){
                                Message message=new Message();
                                message.what=0x001;
                                message.obj="无异常";
                                mHandler.sendMessage(message);
                            }else if(s.toString().equals(", getSmsInPhone has executed!")) {
                                Log.w("调试：","s:"+s.toString());
                                Message message=new Message();
                                message.what=0x001;
                                message.obj="无异常";
                                mHandler.sendMessage(message);
                            }else if(!isDisplay&&!s.toString().equals(", getSmsInPhone has executed!")){
                                Log.w("调试：","告警信息处理");
                                Log.w("调试：","s:"+s.toString());
                                isGot=false;
                                final String[] rs = s.toString().split("@");
                                String[] temp=rs[1].split(", ");//因为时最新一条故从1开始
                                String phone=temp[0];
                                if(phone.equals(GlobalData.getCarPhoneNumber(MyApplication.getContextObject()))){
                                    String text=temp[2];
                                    String time=temp[3];
                                    Message message=new Message();
                                    message.what=0x002;
                                    message.obj="告警信息："+text+"\n"+time;
                                    VibratorUtil vibratorUtil=new VibratorUtil();
                                    vibratorUtil.vibrateAndPlayTone();
                                    mHandler.sendMessage(message);
                                }
                            }
                        }
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                }

            });
            thread.start();
        }
        Log.w("调试：","gfdgfd   "+thread.getName());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(GlobalData.getCarPhoneNumber(MyApplication.getContextObject())!=null){
            detectSms();
        }else{
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle(R.string.car_phone_warn);
            builder.setMessage("单片机电话号码");
            builder.setCancelable(false);
            View view1 = LayoutInflater.from(MainActivity.this).inflate(R.layout.authorization_dialog, null);
            //    设置我们自己定义的布局文件作为弹出框的Content
            builder.setView(view1);
            final EditText authorization = (EditText)view1.findViewById(R.id.authorization);
            builder.setPositiveButton("确定", null);
            builder.setNegativeButton("取消", (dialog, which) -> dialog.dismiss());
            AlertDialog dialog =builder.create();
            dialog.show();
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String a = authorization.getText().toString().trim();
                Toast toast;
                if(a==null || a.equals("")){
                    toast = Toast.makeText(MyApplication.getContextObject(), "请填入单片机电话号码", Toast.LENGTH_LONG);
                }else{
                    if(GlobalData.setCarPhoneNumber(MyApplication.getContextObject(),a)){
                        toast = Toast.makeText(MyApplication.getContextObject(), "设置成功", Toast.LENGTH_SHORT);
                        dialog.dismiss();
                        detectSms();
                    }else{
                        toast = Toast.makeText(MyApplication.getContextObject(), "设置失败", Toast.LENGTH_SHORT);
                    }
                }
                toast.setGravity(Gravity.TOP, 0, 200);
                toast.show();
            });
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.w("调试：","登录过，状态：true");
        try {
            AUTH=GrantAuthorization.grant(GlobalData.getAuth(MyApplication.getContextObject()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        SMSBroadcastReceiver.setListener(this);
        String user_name=GlobalData.getLoggedUserName(MyApplication.getContextObject());

        TitleBar mTitleBar = findViewById(R.id.mTitle);
        mTitleBar.setRightColor(R.color.white);
        mTitleBar.setLeftIcon(R.drawable.ic_back_button);
        mTitleBar.setRightIcon(R.drawable.ic_setting_button);
        mTitleBar.setOnTitleBarListener(new OnTitleBarListener() {
            @Override
            public void onLeftClick(View view) {
                Log.w("调试：","退出MainActivity");
                GlobalData.removeData(MyApplication.getContextObject(),user_name);
                isStop=true;
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onTitleClick(View view) {
                Toast.makeText(getApplicationContext(), "点击了标题", Toast.LENGTH_SHORT).show();
            }

            @SuppressLint("ShowToast")
            @Override
            public void onRightClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(R.string.car_phone_warn);
                builder.setMessage("单片机电话号码");
                builder.setCancelable(false);
                View view1 = LayoutInflater.from(MainActivity.this).inflate(R.layout.authorization_dialog, null);
                //    设置我们自己定义的布局文件作为弹出框的Content
                builder.setView(view1);
                final EditText authorization = (EditText)view1.findViewById(R.id.authorization);
                builder.setPositiveButton("确定", null);
                builder.setNegativeButton("取消", (dialog, which) -> dialog.dismiss());
                AlertDialog dialog =builder.create();
                dialog.show();
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                    String a = authorization.getText().toString().trim();
                    Toast toast;
                    if(a.equals("")){
                        toast = Toast.makeText(MyApplication.getContextObject(), "请填入单片机电话号码", Toast.LENGTH_LONG);
                    }else{
                        if(GlobalData.setCarPhoneNumber(MyApplication.getContextObject(),a)){
                            toast = Toast.makeText(MyApplication.getContextObject(), "设置成功", Toast.LENGTH_SHORT);
                            dialog.dismiss();
                        }else{
                            toast = Toast.makeText(MyApplication.getContextObject(), "设置失败", Toast.LENGTH_SHORT);
                        }
                    }
                    toast.setGravity(Gravity.TOP, 0, 200);
                    toast.show();
                });
            }
        });
        Button openButton = findViewById(R.id.open_button);
        Button closeButton = findViewById(R.id.close_button);
        TextView userName = findViewById(R.id.user_name);
        userName.setText(user_name);
        notificationView= findViewById(R.id.alert_textView);
        alertView=findViewById(R.id.alert_imageView);

        openButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendSms(1);
            }
        });

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendSms(2);
            }
        });
    }

    /**
     * 启动发送短息App
     * @param type 类型
     */
    private void sendSms(int type){
        Bundle bundle = new Bundle();
        if(type==1){
            // bundle序列化
            bundle.putSerializable("content","开窗");

        }else{
            bundle.putSerializable("content","关窗");
        }
        Intent intent = new Intent(MainActivity.this, SmsComposeActivity.class);
        intent.putExtras(bundle);;
        startActivity(intent);
    }

    /**
     * 短信接收后用于自定义控件展示的方法（未使用到，但是是必须有的）
     * @param address 电话号码
     * @param msg 短信内容
     */
    @Override
    public void onTextReceived(String address, String msg) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * 重写返回按钮事件，自定义一些方法
     * @param keyCode 键值码
     * @param event 发生的事件
     * @return 结果状态
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 当按下返回键时所执行的命令
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            isStop=true;//停止线程运行
            Log.w("调试：","MainActivity按下返回按钮");
            
            return super.onKeyDown(keyCode, event);
        }
        return super.onKeyDown(keyCode, event);
    }

}
