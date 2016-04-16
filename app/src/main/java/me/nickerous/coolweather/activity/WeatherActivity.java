package me.nickerous.coolweather.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.net.URLEncoder;

import me.nickerous.coolweather.R;
import me.nickerous.coolweather.util.HttpCallbackListener;
import me.nickerous.coolweather.util.HttpUtil;
import me.nickerous.coolweather.util.Utility;

/**
 * Created by Troy Bolton on 16/04/2016.
 */
public class WeatherActivity extends Activity implements View.OnClickListener {

    private LinearLayout weatherInfoLayout;

    private TextView cityNameText;
    private TextView publishText;
    private TextView weatherDescText;
    private TextView tempText;
    private TextView directionText;
    private TextView currentDateText;
    private Button switchCity;
    private Button refreshWeather;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.weather_layout);

        // 初始化各控件
        weatherInfoLayout = (LinearLayout) findViewById(R.id.weather_info_layout);
        cityNameText = (TextView) findViewById(R.id.city_name);
        publishText = (TextView) findViewById(R.id.publish_text);
        weatherDescText = (TextView) findViewById(R.id.weather_desc);
        tempText = (TextView) findViewById(R.id.temperature);
        directionText = (TextView) findViewById(R.id.wind_direction);
        currentDateText = (TextView) findViewById(R.id.current_date);
        switchCity = (Button) findViewById(R.id.switch_city);
        refreshWeather = (Button) findViewById(R.id.refresh_weather);
        String countyName = getIntent().getStringExtra("county_name");
        if (!TextUtils.isEmpty(countyName)) {
            // 有县级代号时就去查询天气
            publishText.setText("Synchronizing ...");
            weatherInfoLayout.setVisibility(View.INVISIBLE);
            cityNameText.setVisibility(View.INVISIBLE);
            queryWeatherInfo(countyName);
        } else {
            // 没有县级代号时就直接显示本地天气
            showWeather();
        }
        switchCity.setOnClickListener(this);
        refreshWeather.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.switch_city:
                Intent intent = new Intent(this, ChooseAreaActivity.class);
                intent.putExtra("from_weather_activity", true);
                startActivity(intent);
                finish();
                break;
            case R.id.refresh_weather:
                publishText.setText("Synchronizing ...");
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                String cityCode = prefs.getString("city_code", "");
                Log.d("city_code", cityCode);
                if (!TextUtils.isEmpty(cityCode)) {
                    queryWeatherInfo(cityCode);
                }
                break;
            default:
                break;
        }
    }

    /**
     * 查询县级代号所对应的天气代号。
     */
//    private void queryWeatherCode(String countyCode) {
//        String address = "http://www.weather.com.cn/data/list3/city" + countyCode + ".xml";
//        queryFromServer(address, "countyCode");
//    }

    /**
     * 查询天气代号所对应的天气。
     */
    private void queryWeatherInfo(String cityCode) {
        String address = "https://api.thinkpage.cn/v3/weather/now.json?key=umkwuf77r7puvevu&location=" + URLEncoder.encode(cityCode)
                + "&language=zh-Hans&unit=c";
        queryFromServer(address, "weatherCode");
    }

    /**
     * 根据传入的地址和类型去向服务器查询天气代号或者天气信息。
     */
    private void queryFromServer(final String address, final String type) {
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(final String response) {
                if ("weatherCode".equals(type)) {
                    // 处理服务器返回的天气信息
                    Utility.handleWeatherResponse(WeatherActivity.this, response);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showWeather();
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                Log.d("Error Msg: ", e.getMessage());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        publishText.setText("Synchronization failed");
                    }
                });
            }
        });
    }

    /**
     * 从SharedPreferences文件中读取存储的天气信息，并显示到界面上。
     */
    private void showWeather() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        cityNameText.setText( prefs.getString("city_name", ""));
        String temperature = prefs.getString("temp", "") + "\u00B0";
        tempText.setText(temperature);
        String wind_direction = prefs.getString("wind_direction", "");
        if (!wind_direction.equals("")) {
            wind_direction = "风向: " + wind_direction;
        }
        directionText.setText(wind_direction);
        weatherDescText.setText(prefs.getString("weather_desc", ""));
        String weatherInfo = "Today " + prefs.getString("publish_time", "") + " 发布";
        publishText.setText(weatherInfo);
        currentDateText.setText(prefs.getString("current_date", ""));
        weatherInfoLayout.setVisibility(View.VISIBLE);
        cityNameText.setVisibility(View.VISIBLE);
    }
}
