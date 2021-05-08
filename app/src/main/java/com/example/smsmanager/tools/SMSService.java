package com.example.smsmanager.tools;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.util.Log;

import com.example.smsmanager.data.GlobalData;

import java.text.SimpleDateFormat;
import java.util.Date;
//读取短息服务类
public class SMSService {
    @SuppressLint("LongLogTag")
    public static StringBuilder getSmsInPhone(Context context) {
        final String SMS_URI_ALL = "content://sms/"; // 所有短信
        final String SMS_URI_INBOX = "content://sms/inbox"; // 收件箱
        final String SMS_URI_SEND = "content://sms/sent"; // 已发送
        final String SMS_URI_DRAFT = "content://sms/draft"; // 草稿
        final String SMS_URI_OUTBOX = "content://sms/outbox"; // 发件箱
        final String SMS_URI_FAILED = "content://sms/failed"; // 发送失败
        final String SMS_URI_QUEUED = "content://sms/queued"; // 待发送列表

        StringBuilder smsBuilder = new StringBuilder();

        try {
            Uri uri = Uri.parse(SMS_URI_INBOX);
            String[] projection = new String[] { "_id", "address", "person",
                    "body", "date", "type","read",};
            ContentResolver contentResolver=context.getContentResolver();
            // 获取手机内部短信，read=0表示未读，1为已读，类似sql语句
            Cursor cur = contentResolver.query(uri, projection, "read=?", new String[]{"0"}, "date desc");
            if(cur.getCount()==0){
                return null;
            }
            else{
                // 获取短信中最新的未读短信
                if (cur.moveToFirst()) {
                    int index_Address = cur.getColumnIndex("address");//发件人手机号码
                    int index_Person = cur.getColumnIndex("person");//发件人姓名，如果没在通讯录中则为0
                    int index_Body = cur.getColumnIndex("body");//短信内容
                    int index_Date = cur.getColumnIndex("date");//日期
                    int index_Type = cur.getColumnIndex("type");//类型

                    do {
                        String strAddress = cur.getString(index_Address);
                        if(strAddress.equals(GlobalData.getCarPhoneNumber(MyApplication.getContextObject()))){
                            int intPerson = cur.getInt(index_Person);
                            String strbody = cur.getString(index_Body);
                            long longDate = cur.getLong(index_Date);
                            int intType = cur.getInt(index_Type);

                            @SuppressLint("SimpleDateFormat")
                            SimpleDateFormat dateFormat = new SimpleDateFormat(
                                    "yyyy-MM-dd HH:mm:ss");//HH：返回的是24小时制的时间 hh：返回的是12小时制的时间
                            Date d = new Date(longDate);
                            String strDate = dateFormat.format(d);

                            String strType = "";
                            if (intType == 1) {
                                strType = "接收";
                            } else if (intType == 2) {
                                strType = "发送";
                            } else if (intType == 3) {
                                strType = "草稿";
                            } else if (intType == 4) {
                                strType = "发件箱";
                            } else if (intType == 5) {
                                strType = "发送失败";
                            } else if (intType == 6) {
                                strType = "待发送列表";
                            } else if (intType == 0) {
                                strType = "所以短信";
                            } else {
                                strType = "null";
                            }

                            smsBuilder.append("@");
                            smsBuilder.append(strAddress).append(", ");
                            smsBuilder.append(intPerson).append(", ");
                            smsBuilder.append(strbody).append(", ");
                            smsBuilder.append(strDate).append(", ");
                            smsBuilder.append(strType);
                            //更新状态，未读——>已读
                            String id = cur.getString(cur.getColumnIndex("_id"));
                            ContentValues values=new ContentValues();
                            values.put("read",1);
                            contentResolver.update(Uri.parse(SMS_URI_ALL),values,"_id=?", new String[]{id});//更新状态，此方法返回影响数据行数，此方法正常运行的条件是进入手机设置将默认短息应用更改为本应用
                        }
                    } while (cur.moveToNext());

                }

                if (!cur.isClosed()) {
                    cur.close();
                    cur = null;
                }
            }

            smsBuilder.append(", getSmsInPhone has executed!");

        } catch (SQLiteException ex) {
            Log.d("SQLiteException in getSmsInPhone", ex.getMessage());
        }

        return smsBuilder;
    }
}
