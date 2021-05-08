package com.example.smsmanager.ui.main;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class SmsViewModel extends AndroidViewModel {


    private final SmsRepository mRepository;

    public SmsViewModel(@NonNull Application application) {
        super(application);
        mRepository = new SmsRepository(application);
    }

    //原本打算使用recyclerView展示告警信息，此处并未实现
    public void insert(SmsEntity sms) {
        mRepository.insert(sms);
    }

    LiveData<List<SmsEntity>> getAllSms() { return mRepository.getAllWords(); }

}

