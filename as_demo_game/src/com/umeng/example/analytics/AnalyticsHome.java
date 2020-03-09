package com.umeng.example.analytics;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.os.Bundle;

import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import com.umeng.analytics.MobclickAgent;
import com.umeng.analytics.MobclickAgent.EScenarioType;

import com.umeng.example.R;

public class AnalyticsHome extends Activity {
    private Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setTitle("友盟统计(普通场景)");
        setContentView(R.layout.umeng_example_analytics);
        MobclickAgent.setScenarioType(mContext, EScenarioType.E_UM_NORMAL);

    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(mContext);
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(mContext);
    }

    /**
     * android:onClick="onButtonClick"
     *
     * @param view
     */
    public void onButtonClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.umeng_example_analytics_event:
                android.util.Log.i("mob", "-------umeng_example_analytics_event--------");
                MobclickAgent.onEvent(mContext, "click");
                MobclickAgent.onEvent(mContext, "click", "clickLabel");
                break;
            case R.id.umeng_example_analytics_ekv:
                android.util.Log.i("mob", "-------umeng_example_analytics_ekv--------");
                Map<String, String> ekvs = new HashMap<String, String>();
                ekvs.put("type", "popular");
                ekvs.put("artist", "JJLin");
                ekvs.put("type", "popular");
                ekvs.put("artist", "JJLin");
                MobclickAgent.onEvent(mContext, "music", ekvs);
                Log.i("mob", "----map_ekv---");
                break;

            case R.id.umeng_example_analytics_make_crash:
                "123".substring(10);
                break;
            case R.id.umeng_example_analytics_js_analytic:
                startActivity(new Intent(mContext, WebviewAnalytic.class));
                break;

            case R.id.umeng_example_analytics_signinDefalut:
                MobclickAgent.onProfileSignIn("example_id");
                break;
            case R.id.umeng_example_analytics_signin:
                MobclickAgent.onProfileSignIn("example_id", "uid");
                break;
            case R.id.umeng_example_analytics_signoff:
                MobclickAgent.onProfileSignOff();
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {

            hook();

            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    // /对于好多应用，会在程序中杀死 进程，这样会导致我们统计不到此时Activity结束的信息，
    // /对于这种情况需要调用 'MobclickAgent.onKillProcess( Context )'
    // /方法，保存一些页面调用的数据。正常的应用是不需要调用此方法的。
    private void hook() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setPositiveButton("退出应用", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                MobclickAgent.onKillProcess(mContext);

                int pid = android.os.Process.myPid();
                android.os.Process.killProcess(pid);
            }
        });
        builder.setNeutralButton("后退一下", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                finish();
            }
        });
        builder.setNegativeButton("点错了", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });
        builder.show();
    }
}