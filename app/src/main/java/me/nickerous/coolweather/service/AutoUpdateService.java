package me.nickerous.coolweather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

import java.net.URLEncoder;

import me.nickerous.coolweather.receiver.AutoUpdateReceiver;
import me.nickerous.coolweather.util.HttpCallbackListener;
import me.nickerous.coolweather.util.HttpUtil;
import me.nickerous.coolweather.util.Utility;

/**
 * Created by Troy Bolton on 17/04/2016.
 */
public class AutoUpdateService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                updateWeather();
            }
        }).start();
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int anHour = 8 * 60 * 60 * 1000; // 这是8小时的毫秒数
//        int anHour = 20 * 1000;
        long triggerAtTime = SystemClock.elapsedRealtime() + anHour;
        Intent i = new Intent(this, AutoUpdateReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 更新天气信息。
     */
    private void updateWeather() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String cityCode = prefs.getString("city_code", "");
        Log.d("responsecityCode", cityCode);
        String address = "https://api.thinkpage.cn/v3/weather/now.json?key=umkwuf77r7puvevu&location="
                + URLEncoder.encode(cityCode) + "&language=zh-Hans&unit=c";
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                Log.d("responseresponse", response);
                Utility.handleWeatherResponse(AutoUpdateService.this, response);
            }
            @Override
            public void onError(Exception e) {
                Log.d("ErrorError: ", e.getMessage());
                e.printStackTrace();
            }
        });
    }
}
