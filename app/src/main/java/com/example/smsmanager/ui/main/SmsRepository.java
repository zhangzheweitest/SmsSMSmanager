package com.example.smsmanager.ui.main;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import androidx.lifecycle.MutableLiveData;

import com.example.smsmanager.data.GlobalData;

import java.util.ArrayList;
import java.util.List;

public class SmsRepository {

    private final MutableLiveData<List<SmsEntity>> mAllSms = new MutableLiveData<>();
    private final Context mAppContext;

    SmsRepository(Application application) {
        mAppContext = application;
    }

    /**
     * @deprecated 未使用方法
     * @return 读取到的格式固定的短信内容
     */
    MutableLiveData<List<SmsEntity>> getAllWords() {
        LoadSms loadSms = new LoadSms();
        loadSms.execute();
        return mAllSms;
    }

    /**
     * @deprecated 未用到
     * @param smsEntity 插入的短信实体
     */
    public void insert(SmsEntity smsEntity) {
        new insertAsyncTask().execute(smsEntity);
    }

    //没有使用到
    @SuppressLint("StaticFieldLeak")
    private class insertAsyncTask extends AsyncTask<SmsEntity, Void, Void> {

        @Override
        protected Void doInBackground(SmsEntity... sms) {
            Uri newUri;
            ContentValues newValues = new ContentValues();

            newValues.put(GlobalData.KEY_MSG_BODY, sms[0].getMsg());
            newValues.put(GlobalData.KEY_DATE, sms[0].getDate());
            newValues.put(GlobalData.KEY_ADDRESS, sms[0].getAddress());

            newUri = mAppContext.getContentResolver().insert(
                    Uri.parse("content://sms/sent"),
                    newValues
            );
            return null;
        }

    }

    @SuppressLint("StaticFieldLeak")
    class LoadSms extends AsyncTask<String, Void, List<SmsEntity>> {

        List<SmsEntity> mAdapterList = new ArrayList<>();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mAdapterList.clear();
        }

        protected List<SmsEntity> doInBackground(String... args) {
            try {
                Uri uriInbox = Uri.parse("content://sms/");

                Cursor c = mAppContext.getContentResolver().query(uriInbox, null, "address IS NOT NULL) GROUP BY (thread_id", null, null); // 2nd null = "address IS NOT NULL) GROUP BY (address"
//                Uri uriSent = Uri.parse("content://sms/sent");
//                Cursor sent = getContentResolver().query(uriSent, null, "address IS NOT NULL) GROUP BY (thread_id", null, null); // 2nd null = "address IS NOT NULL) GROUP BY (address"
//                Cursor c = new MergeCursor(new Cursor[]{inbox,sent}); // Attaching inbox and sent sms

                if (c.moveToFirst()) {
                    for (int i = 0; i < c.getCount(); i++) {

                        String _id = c.getString(c.getColumnIndexOrThrow(GlobalData._ID));//短消息序号
                        String thread_id = c.getString(c.getColumnIndexOrThrow(GlobalData.KEY_THREAD_ID));//对话的序号（conversation）
                        String msg = c.getString(c.getColumnIndexOrThrow(GlobalData.KEY_MSG_BODY));
                        String type = c.getString(c.getColumnIndexOrThrow(GlobalData.KEY_TYPE));// 类型 1是**到的，2是发出的
                        String date = c.getString(c.getColumnIndexOrThrow(GlobalData.KEY_DATE));// 日期 long型
                        String user = c.getString(c.getColumnIndexOrThrow(GlobalData.KEY_ADDRESS));// 发件人地址，手机号

                        mAdapterList.add(new SmsEntity(_id, thread_id, user, type, date, msg));

                        c.moveToNext();
                    }
                }
                c.close();

            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

//            Collections.sort(smsList, new MapComparator(Function.KEY_TIMESTAMP, "dsc")); // Arranging sms by timestamp decending
//            ArrayList<HashMap<String, String>> purified = Function.removeDuplicates(smsList); // Removing duplicates from inbox & sent
//            smsList.clear();
//            smsList.addAll(purified);


            return mAdapterList;

        }

        @Override
        protected void onPostExecute(List<SmsEntity> list) {
            mAllSms.postValue(list);
        }
    }

}

