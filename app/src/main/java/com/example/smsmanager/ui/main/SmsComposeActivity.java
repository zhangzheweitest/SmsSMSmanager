package com.example.smsmanager.ui.main;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProviders;
import com.example.smsmanager.R;
import com.example.smsmanager.data.GlobalData;
import com.example.smsmanager.tools.GrantAuthorization;
import com.example.smsmanager.tools.MyApplication;

import java.text.ParseException;

/**
 * 短信内容整合类，即发送类
 */
public class SmsComposeActivity extends AppCompatActivity {


    private final int REQUEST_SEND_SMS = 103;
    private String content;
    @SuppressLint("StaticFieldLeak")
    private static EditText toEditText;
    @SuppressLint("HandlerLeak")
    static Handler mHandler = new Handler(){
        public void handleMessage( Message msg) {
            super.handleMessage(msg);
            toEditText.setText((String)msg.obj);
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sms_compose);
        Bundle bundle = getIntent().getExtras();
        content=(String)bundle.getSerializable("content");
        //未使用到的变量
        SmsViewModel smsViewModel = ViewModelProviders.of(this).get(SmsViewModel.class);
        setUpViews();

    }

    /**
     * 界面初始化
     */
    private  void setUpViews() {
        Button button = findViewById(R.id.send_button);
        toEditText = findViewById(R.id.contact_edit_text);
        new Thread(() -> {
            Message message=new Message();
            //todo:单片机接收方号码定义
            message.obj=GlobalData.getCarPhoneNumber(MyApplication.getContextObject());
            mHandler.sendMessage(message);
        }).start();
        //todo: 双卡情况下为何会默认使用卡二发送信息？？？
        button.setOnClickListener(view -> {
            String msg = toEditText.getText().toString().trim();
            boolean isGrant=false;
            try {
                isGrant=GrantAuthorization.grant(GlobalData.getAuth(MyApplication.getContextObject()));
            } catch (ParseException e) {
                Toast.makeText(SmsComposeActivity.this,e.getMessage(), Toast.LENGTH_SHORT).show();
            }finally {
                if (sendSMS(msg,content)&&isGrant) {
                    finish();
                    GlobalData.isSmsSuccess=true;
                    //todo: 此处应该还有一个单片机操作成功后的反馈结果才能证明车机是否已经正常动作
                    Toast.makeText(SmsComposeActivity.this,"短信已发送", Toast.LENGTH_SHORT).show();
                } else {
                    GlobalData.isSmsSuccess=false;
                    Toast.makeText(SmsComposeActivity.this,"短信发送失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    /**
     * 发送短信
     * @param toPhoneNumber 收件手机号
     * @param smsMessage 短信内容
     * @return 发送状态
     */
    private boolean sendSMS(String toPhoneNumber, String smsMessage) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            requestSendSmsPermission();
        } else {
            try {
                SmsManager smsManager = SmsManager.getDefault();
                MainActivity.isGot=true;
                smsManager.sendTextMessage(toPhoneNumber, null, smsMessage, null, null);
                Log.w("w","短信发送成功");
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    /**
     * 权限请求
     */
    private void requestSendSmsPermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.SEND_SMS)) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.sms_permission_rationale);
            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ActivityCompat.requestPermissions(SmsComposeActivity.this, new String[]{Manifest.permission.SEND_SMS},
                            REQUEST_SEND_SMS);
                }
            });
            builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    dialog.dismiss();
                }
            });
            builder.create().show();

        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS},
                    REQUEST_SEND_SMS);
        }
    }

    /**
     * 权限请求回调方法
     * @param requestCode 上一个方法中的请求码
     * @param permissions 请求的权限，非空
     * @param grantResults 权限授予结果
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == REQUEST_SEND_SMS) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(SmsComposeActivity.this,"授权成功！", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(SmsComposeActivity.this,"权限未授予！", Toast.LENGTH_SHORT).show();
            }

        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }


    }
}
