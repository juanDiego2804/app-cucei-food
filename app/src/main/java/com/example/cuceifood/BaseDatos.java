package com.example.cuceifood;

import android.app.Application;

import com.backendless.Backendless;

public class BaseDatos extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        String APP_ID ="18CFE9E5-DC47-458C-AD0A-D62AEF29AEDF";
        String API_KEY ="63A6C9A3-C7B7-49ED-82D9-C47C6D349597";
        Backendless.initApp(this, APP_ID, API_KEY);
    }

}
