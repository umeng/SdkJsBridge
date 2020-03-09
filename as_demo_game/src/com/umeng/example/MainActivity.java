package com.umeng.example;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.umeng.example.analytics.AnalyticsHome;
import com.umeng.example.game.GameAnalyticsHome;


public class MainActivity extends Activity {

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
    }

    public void onClick(View v) {
        int id = v.getId();
        Intent in = null;
        if (id == R.id.normal) {
            in = new Intent(this, AnalyticsHome.class);
            startActivity(in);
        } else if (id == R.id.game) {
            in = new Intent(this, GameAnalyticsHome.class);
            startActivity(in);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}
