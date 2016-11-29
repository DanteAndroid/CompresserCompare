package com.dante.rxdemo;

import android.app.Application;
import android.content.Context;

/**
 * Created by yons on 16/9/19.
 */
public class App extends Application {

    private static final String TAG = "Test";
    public  Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
    }

}
