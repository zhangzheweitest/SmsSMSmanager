package com.example.smsmanager.tools;

import android.content.Context;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;

public class VibratorUtil {

    protected AudioManager audioManager;
    protected Vibrator vibrator;
    private Ringtone ringtone;

    private static final int MIN_TIME_OUT = 4000; //时间间隔

    long lastNotificationTime;

    public VibratorUtil() {
        audioManager = (AudioManager) MyApplication.getContextObject().getSystemService(Context.AUDIO_SERVICE); //此方法是由Context调用的
        vibrator = (Vibrator)MyApplication.getContextObject().getSystemService(Context.VIBRATOR_SERVICE);  //同上
    }


    /**
     * 开启手机震动和播放系统提示铃声
     */
    public void vibrateAndPlayTone() {
        if (System.currentTimeMillis() - lastNotificationTime < MIN_TIME_OUT) {
            return;
        }
        try {
            lastNotificationTime = System.currentTimeMillis();
            if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT) {
                return;
            }
            //数组的a[0]表示静止的时间，a[1]代表的是震动的时间，然后数组的a[2]表示静止的时间,以此类推(毫秒)
            long[] pattern = new long[]{0, 400, 80, 400, 80, 400};
            vibrator.vibrate(pattern, -1);  //震动

            if (ringtone == null) {
                Uri notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                ringtone = RingtoneManager.getRingtone(MyApplication.getContextObject(), notificationUri);
                if (ringtone == null) {
                    return;
                }
            }
            if (!ringtone.isPlaying()) {
                ringtone.play();


                //判断手机品牌
                String vendor = Build.MANUFACTURER;
                if (vendor != null && vendor.toLowerCase().contains("samsung")) {
                    Thread ctlThread = new Thread() {
                        public void run() {
                            try {
                                Thread.sleep(3000);
                                if (ringtone.isPlaying()) {
                                    ringtone.stop();
                                }
                            } catch (Exception e) {

                            }
                        }
                    };
                    ctlThread.run();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
