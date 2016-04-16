package me.nickerous.coolweather.util;

/**
 * Created by Troy Bolton on 16/04/2016.
 */

public interface HttpCallbackListener {
    void onFinish(String response);
    void onError(Exception e);
}
