package com.umeng.example;

import android.app.Application;
import com.umeng.commonsdk.UMConfigure;

public class UmengApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        UMConfigure.setLogEnabled(true);

        //可视化SDK
        UMConfigure.init(this, "59c4b341734be43f04000025", "Umeng", UMConfigure.DEVICE_TYPE_PHONE, null);

    }

}
