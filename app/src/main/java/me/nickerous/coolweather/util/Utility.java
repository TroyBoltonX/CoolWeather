package me.nickerous.coolweather.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import me.nickerous.coolweather.db.CoolWeatherDB;
import me.nickerous.coolweather.model.City;
import me.nickerous.coolweather.model.County;
import me.nickerous.coolweather.model.Province;

/**
 * Created by Troy Bolton on 16/04/2016.
 */
public class Utility {

    public static String substringJson(String jsonStr){

        return jsonStr.substring(jsonStr.lastIndexOf("(")+1, jsonStr.lastIndexOf(")"));
    }

    /**
     * 解析和处理服务器返回的省级数据
     */
    public synchronized static boolean handleProvincesResponse(CoolWeatherDB coolWeatherDB, String response) {

        boolean isEmptyOrError = true;
        if (!TextUtils.isEmpty(response)) {

            response = substringJson(response);

            try {
                JSONArray array = new JSONArray(response);
                Province province = null;
                for (int i = 0; i < array.length(); ++i) {

                    JSONArray jsonArray = array.getJSONArray(i);
                    province = new Province();
                    province.setProvinceName(jsonArray.getString(0));
                    province.setProvinceCode(jsonArray.getString(1));
                    coolWeatherDB.saveProvince(province);
                }
                isEmptyOrError = false;
            } catch (Exception e) {
                e.printStackTrace();
                isEmptyOrError = true;
            }
        }
        return !isEmptyOrError;
    }

    public static boolean handleCitiesResponse(CoolWeatherDB coolWeatherDB, String response, int provinceId) {

        boolean isEmptyOrError = true;
        if (!TextUtils.isEmpty(response)) {

            response = substringJson(response);

            try {
                JSONArray array = new JSONArray(response);
                City city = null;
                for (int i = 0; i < array.length(); ++i) {

                    JSONArray jsonArray = array.getJSONArray(i);
                    city = new City();
                    city.setCityName(jsonArray.getString(0));
                    city.setCityCode(jsonArray.getString(1));
                    city.setProvinceId(provinceId);
                    coolWeatherDB.saveCity(city);
                }
                isEmptyOrError = false;
            } catch (Exception e) {
                e.printStackTrace();
                isEmptyOrError = true;
            }
        }
        return !isEmptyOrError;
    }

    /**
     * 解析和处理服务器返回的县级数据
     */
    public static boolean handleCountiesResponse(CoolWeatherDB coolWeatherDB, String response, int cityId) {

        boolean isEmptyOrError = true;
        if (!TextUtils.isEmpty(response)) {

            response = substringJson(response);

            try {
                JSONArray array = new JSONArray(response);
                County county = null;
                for (int i = 0; i < array.length(); ++i) {

                    JSONArray jsonArray = array.getJSONArray(i);
                    county = new County();
                    county.setCountyName(jsonArray.getString(0));
                    county.setCountyCode(jsonArray.getString(1));
                    county.setCityId(cityId);
                    coolWeatherDB.saveCounty(county);
                }
                isEmptyOrError = false;
            } catch (Exception e) {
                e.printStackTrace();
                isEmptyOrError = true;
            }
        }
        return !isEmptyOrError;
    }

    /**
     * 解析服务器返回的JSON数据，并将解析出的数据存储到本地
     */
    public static void handleWeatherResponse(Context context, String response) {

        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray resultsArray = jsonObject.getJSONArray("results");
            JSONObject weatherInfo = resultsArray.getJSONObject(0);
            Object location = weatherInfo.getString("location");
            Object weatherStatus = weatherInfo.getString("now");
            Object last_update = weatherInfo.getString("last_update");

            JSONObject locationObject = new JSONObject(location.toString());
            String cityCode = locationObject.getString("id");
            String cityName = locationObject.getString("name");

            JSONObject weatherObject = new JSONObject(weatherStatus.toString());
            String temperature = weatherObject.getString("temperature");
            String weatherDesc = weatherObject.getString("text");
            String wind_direction = weatherObject.getString("wind_direction");

            String publishTime = last_update.toString().substring(0, 10);

            if (true) {
                System.out.println("+++++++++++++++++++location: " + location);
                System.out.println("+++++++++++++++++++weatherStatus: " + weatherStatus);
                System.out.println("+++++++++++++++++++publishTime: " + publishTime);
                System.out.println("+++++++++++++++++++cityCode: " + cityCode);
                System.out.println("+++++++++++++++++++cityName: " + cityName);
                System.out.println("+++++++++++++++++++temperature: " + temperature + "\u00B0");
                System.out.println("+++++++++++++++++++weatherDesc: " + weatherDesc);
                System.out.println("+++++++++++++++++++wind_direction: " + wind_direction);
            }

            saveWeatherInfo(context, cityCode, cityName, temperature, weatherDesc, wind_direction, publishTime);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 将服务器返回的所有天气信息存储到SharedPreferences文件中
     */
    public static void saveWeatherInfo(Context context, String cityCode, String cityName,
                                       String temperature, String weatherDesc, String wind_direction, String publishTime) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean("city_selected", true);
        editor.putString("city_code", cityCode);
        editor.putString("city_name", cityName);
        editor.putString("temp", temperature);
        editor.putString("weather_desc", weatherDesc);
        editor.putString("wind_direction", wind_direction);
        editor.putString("publish_time", publishTime);
        editor.putString("current_date", sdf.format(new Date()));
        editor.apply();
    }
}
