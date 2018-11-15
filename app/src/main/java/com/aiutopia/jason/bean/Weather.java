package com.aiutopia.jason.bean;

import android.util.Log;

/**
 * Created by Jason on 15/11/2018.
 */

public class Weather {
    public String date, high, low, type, fengxiang;
    public static void printInfo(Weather w){
        Log.d("Players: ", w.date+w.high+w.low+w.type+w.fengxiang);
    }
}
