package com.umeng.example.analytics;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.umeng.analytics.MobclickAgent;
import com.umeng.example.R;
import com.umeng.example.analytics.SdkBridge;

public class WebviewAnalytic extends Activity {

    private Context appContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appContext = this.getApplicationContext();
        setContentView(R.layout.umeng_example_analytics_webview);

        WebView webView = (WebView)findViewById(R.id.webview);

        //***
        SdkBridge inst = SdkBridge.getInstance(appContext);
        inst.attach(webView);
//        //***
//        if (Build.VERSION.SDK_INT > 11) {
//            webView.removeJavascriptInterface("searchBoxJavaBridge_");
//            webView.removeJavascriptInterface("accessibility");
//            webView.removeJavascriptInterface("accessibilityTraversal");
//        }
//        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("file:///android_asset/demo.html");
        //webView.setWebViewClient(new MyWebviewClient());
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
        android.util.Log.i("mob", "--Webview-onPause-----");
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
        android.util.Log.i("mob", "--Webview-onResume-----");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SdkBridge.getInstance(appContext).detach(); // 在WebView宿主页面销毁时解除绑定
    }

    class MyWebviewClient extends WebViewClient {

        @Override
        public void onPageFinished(WebView view, String url) {
            view.loadUrl("javascript:setWebViewFlag()");
            if (url != null && url.endsWith("/index.html")) {
                MobclickAgent.onPageStart("index.html");
            }
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            try {
                Log.d("SdkBridge", "shouldOverrideUrlLoading url:" + url);
                String decodedURL = java.net.URLDecoder.decode(url, "UTF-8");
                SdkBridge.getInstance(WebviewAnalytic.this).execute(decodedURL, view);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }
    }

}
