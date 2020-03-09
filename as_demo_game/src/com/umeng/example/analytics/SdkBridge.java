package com.umeng.example.analytics;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.content.Context;
import android.os.Build;

import android.os.Handler;
import android.util.Log;
import android.webkit.JavascriptInterface;

import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.umeng.analytics.MobclickAgent;
import com.umeng.analytics.MobclickAgent.EScenarioType;
import com.umeng.analytics.game.UMGameAgent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SdkBridge {

    private static final String TAG = "SdkBridge";
    private static final String JS_INTERFACE_NAME = "UmengSdkWrapper";
    private static final String UMENG_USER_AGENT = "UmengSdkWrapper/1.0.0";

    private static Context mAppContext = null;

    private WebView mWebView = null;

    private Handler mHandler = null;

    public void attach(WebView v) {
        if (mAppContext == null) {
            Log.i(TAG, "--->>> attach函数中应用上下文参数为null。请先对UmengSdkWrapper进行初始化。");
            return;
        }

        if (v != null && (v instanceof WebView)) {
            mWebView = v;
        } else {
            Log.i(TAG, "--->>> attach函数参数必须是一个非空WebView对象.");
            return;
        }

        if (Build.VERSION.SDK_INT > 11) {
            mWebView.removeJavascriptInterface("searchBoxJavaBridge_");
            mWebView.removeJavascriptInterface("accessibility");
            mWebView.removeJavascriptInterface("accessibilityTraversal");
        }

        if (mHandler == null) {
            mHandler = new Handler();
        }

        if (Build.VERSION.SDK_INT > 19) {
            WebView.setWebContentsDebuggingEnabled(true);
            mWebView.getSettings().setDomStorageEnabled(true);// 打开本地缓存提供JS调用,至关重要
            mWebView.getSettings().setAppCacheMaxSize(1024 * 1024 * 8);// 实现8倍缓存
            mWebView.getSettings().setAllowFileAccess(true);
            mWebView.getSettings().setAppCacheEnabled(true);
            String appCachePath = mAppContext.getCacheDir().getAbsolutePath();
            mWebView.getSettings().setAppCachePath(appCachePath);
            mWebView.getSettings().setDatabaseEnabled(true);
        }

        mWebView.getSettings().setJavaScriptEnabled(true);
        String oldUA = mWebView.getSettings().getUserAgentString();
        String UA = oldUA + ";" + UMENG_USER_AGENT;
        mWebView.getSettings().setUserAgentString(UA); // 修改WebView UserAgent值
        final JavaScriptInterface jsInterface = new JavaScriptInterface(mAppContext);
        // A JS interface object is bound to a webview object.
        mWebView.addJavascriptInterface(jsInterface, JS_INTERFACE_NAME);
        mWebView.setWebViewClient(new UmengWebviewClient());
    }

    public void detach() {
        mWebView = null;
    }

    /**
     * SDK API执行接口
     * @param opts
     */
    public void invokeApi(String opts) {
        try {
            execute(opts, mWebView);
        } catch (Throwable e) {

        }
    }

    public String getWebViewName () {
        return mWebView.getClass().getName();
    }

    /**
     * 获取webview名接口
     * @return
     */
    public String env() {
        return SdkBridge.getInstance(mAppContext).getWebViewName();
    }

    private class JavaScriptInterface {
        Context mContext;
        JavaScriptInterface(Context c) {
            mContext = c;
        }

        // JS调用统计API
        @JavascriptInterface
        public void CALL(String opts){
            invokeApi(opts);
        }

        // 获取WebView相关信息API
        @JavascriptInterface
        public String ENV() {
            return env();
        }
    }

    private class UmengWebviewClient extends WebViewClient {

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            view.loadUrl("javascript:setWebViewFlag()");
            if (url != null && url.endsWith("/demo.html")) {
                MobclickAgent.onPageStart("demo.html");
            }
        }
    }

    /**
     * 可以设置是否为游戏，如果是游戏会进行初始化
     */
    private static boolean isGameInited = false;

    private static class Holder {
        private static final SdkBridge INSTANCE = new SdkBridge();
    }

    private SdkBridge() {
    }

    /**
     * 初始化游戏
     */
    private void initGame() {
        UMGameAgent.init(mAppContext);
        UMGameAgent.setPlayerLevel(1);
        MobclickAgent.setScenarioType(mAppContext, EScenarioType.E_UM_GAME);
        isGameInited = true;
    }

    public static SdkBridge getInstance(Context context) {
        if (context != null) {
            mAppContext = context.getApplicationContext();
        }
        return Holder.INSTANCE;
    }

    /**
     * native SDK API分发函数
     * @param url 包含方法及参数的json对象字符串
     * @param webView 绑定的页面显示容器
     */
    public void execute(final String url, final WebView webView) throws Exception {

        if (url.startsWith("umeng")) {
            String str = url.substring(6);

            JSONObject jsonObj = new JSONObject(str);
            String functionName = jsonObj.getString("functionName");
            JSONArray args = jsonObj.getJSONArray("arguments");

            if (functionName.equals("getDeviceId")) {
                getDeviceId(args, webView);
            } else {
                Class<SdkBridge> classType = SdkBridge.class;
                Method method = classType.getDeclaredMethod(functionName, JSONArray.class);
                method.invoke(getInstance(mAppContext), args);
            }
        }
    }

    private void getDeviceId(JSONArray args, WebView webView) {
        Log.d("SdkBridge", "getDeviceId  args:" + args.toString());
        try {
            android.telephony.TelephonyManager tm = (android.telephony.TelephonyManager)mAppContext
                .getSystemService(Context.TELEPHONY_SERVICE);
            final String deviceId = "test_IMEI_1234567890";//tm.getDeviceId();
            final String callBack = args.getString(0);
            final WebView wv = webView;
            if (mHandler != null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            wv.loadUrl("javascript:" + callBack + "('" + deviceId + "')");
                        }catch (Throwable e) {
                        }
                    }
                });
            }
        } catch (Exception e) {
            e.toString();
        }
    }

    @SuppressWarnings("unused")
    private void onEvent(final JSONArray args) throws JSONException {
        Log.d("SdkBridge", "onEvent  args:" + args.toString());
        String eventId = args.getString(0);
        MobclickAgent.onEvent(mAppContext, eventId);
    }

    @SuppressWarnings("unused")
    private void onEventWithLabel(final JSONArray args) throws JSONException {
        Log.d("SdkBridge", "onEventWithLabel  args:" + args.toString());
        String eventId = args.getString(0);
        String label = args.getString(1);
        MobclickAgent.onEvent(mAppContext, eventId, label);
    }

    @SuppressWarnings({"unused", "unchecked"})
    private void onEventWithParameters(final JSONArray args) throws JSONException {
        Log.d("SdkBridge", "onEventWithParameters  args:" + args.toString());
        String eventId = args.getString(0);
        JSONObject obj = args.getJSONObject(1);
        Map<String, String> map = new HashMap<String, String>();
        Iterator<String> it = obj.keys();
        while (it.hasNext()) {
            String key = String.valueOf(it.next());
            Object o = obj.get(key);
            if (o instanceof Integer) {
                String value = String.valueOf(o);
                map.put(key, value);
            } else if (o instanceof String) {
                String strValue = (String)o;
                map.put(key, strValue);
            }
        }
        MobclickAgent.onEvent(mAppContext, eventId, map);
    }

    @SuppressWarnings({"unused", "unchecked"})
    private void onEventWithCounter(final JSONArray args) throws JSONException {
        Log.d("SdkBridge", "onEventWithCounter  args:" + args.toString());
        String eventId = args.getString(0);
        JSONObject obj = args.getJSONObject(1);
        Map<String, String> map = new HashMap<String, String>();
        Iterator<String> it = obj.keys();
        while (it.hasNext()) {
            String key = String.valueOf(it.next());
            Object o = obj.get(key);
            if (o instanceof Integer) {
                String value = String.valueOf(o);
                map.put(key, value);
            } else if (o instanceof String) {
                String strValue = (String)o;
                map.put(key, strValue);
            }
        }
        int value = args.getInt(2);
        MobclickAgent.onEventValue(mAppContext, eventId, map, value);
    }

    @SuppressWarnings({"unused"})
    private void onPageBegin(final JSONArray args) throws JSONException {
        Log.d("SdkBridge", "onPageBegin  args:" + args.toString());
        String pageName = args.getString(0);
        MobclickAgent.onPageStart(pageName);
    }

    @SuppressWarnings({"unused"})
    private void onPageEnd(final JSONArray args) throws JSONException {
        Log.d("SdkBridge", "onPageEnd  args:" + args.toString());
        String pageName = args.getString(0);
        MobclickAgent.onPageEnd(pageName);
    }

    @SuppressWarnings({"unused"})
    private void profileSignInWithPUID(final JSONArray args) throws JSONException {
        Log.d("SdkBridge", "profileSignInWithPUID  args:" + args.toString());
        String puid = args.getString(0);
        MobclickAgent.onProfileSignIn(puid);
    }

    @SuppressWarnings({"unused"})
    private void profileSignInWithPUIDWithProvider(final JSONArray args) throws JSONException {
        Log.d("SdkBridge", "profileSignInWithPUIDWithProvider  args:" + args.toString());
        String puid = args.getString(0);
        String provider = args.getString(1);
        MobclickAgent.onProfileSignIn(puid, provider);
    }

    @SuppressWarnings({"unused"})
    private void profileSignOff(final JSONArray args) throws JSONException {
        Log.d("SdkBridge", "profileSignOff");
        MobclickAgent.onProfileSignOff();
    }

    @SuppressWarnings({"unused"})
    private void setUserLevelId(final JSONArray args) throws JSONException {
        Log.d("SdkBridge", "setUserLevelId [" + isGameInited + "] args:" + args.toString());
        if (!isGameInited) {
            initGame();
        }
        int level = args.getInt(0);
        UMGameAgent.setPlayerLevel(level);
    }

    @SuppressWarnings({"unused"})
    private void startLevel(final JSONArray args) throws JSONException {
        Log.d("SdkBridge", "startLevel  args:" + args.toString());
        if (!isGameInited) {
            initGame();
        }
        String level = args.getString(0);
        UMGameAgent.startLevel(level);
    }

    @SuppressWarnings({"unused"})
    private void failLevel(final JSONArray args) throws JSONException {
        Log.d("SdkBridge", "failLevel  args:" + args.toString());
        if (!isGameInited) {
            initGame();
        }
        String level = args.getString(0);
        UMGameAgent.failLevel(level);
    }

    @SuppressWarnings({"unused"})
    private void finishLevel(final JSONArray args) throws JSONException {
        Log.d("SdkBridge", "finishLevel  args:" + args.toString());
        if (!isGameInited) {
            initGame();
        }
        String level = args.getString(0);
        UMGameAgent.finishLevel(level);
    }

    @SuppressWarnings({"unused"})
    private void exchange(final JSONArray args) throws JSONException {
        Log.d("SdkBridge", "exchange  args:" + args.toString());
        if (!isGameInited) {
            initGame();
        }
        double currencyAmount = args.getDouble(0);
        String currencyType = args.getString(1);
        double virtualAmount = args.getDouble(2);
        int channel = args.getInt(3);
        String orderId = args.getString(4);
        UMGameAgent.exchange(currencyAmount, currencyType, virtualAmount, channel, orderId);
    }

    @SuppressWarnings({"unused"})
    private void pay(final JSONArray args) throws JSONException {
        Log.d("SdkBridge", "pay  args:" + args.toString());
        if (!isGameInited) {
            initGame();
        }
        double money = args.getDouble(0);
        double coin = args.getDouble(1);
        int source = args.getInt(2);
        UMGameAgent.pay(money, coin, source);
    }

    @SuppressWarnings({"unused"})
    private void payWithItem(final JSONArray args) throws JSONException {
        Log.d("SdkBridge", "payWithItem  args:" + args.toString());
        if (!isGameInited) {
            initGame();
        }
        double money = args.getDouble(0);
        String item = args.getString(1);
        int number = args.getInt(2);
        double price = args.getDouble(3);
        int source = args.getInt(4);
        UMGameAgent.pay(money, item, number, price, source);
    }

    @SuppressWarnings({"unused"})
    private void buy(final JSONArray args) throws JSONException {
        Log.d("SdkBridge", "buy  args:" + args.toString());
        if (!isGameInited) {
            initGame();
        }
        String item = args.getString(0);
        int number = args.getInt(1);
        double price = args.getDouble(2);
        UMGameAgent.buy(item, number, price);
    }

    @SuppressWarnings({"unused"})
    private void use(final JSONArray args) throws JSONException {
        Log.d("SdkBridge", "use  args:" + args.toString());
        if (!isGameInited) {
            initGame();
        }
        String item = args.getString(0);
        int number = args.getInt(1);
        double price = args.getDouble(2);
        UMGameAgent.use(item, number, price);
    }

    @SuppressWarnings({"unused"})
    private void bonus(final JSONArray args) throws JSONException {
        Log.d("SdkBridge", "bonus  args:" + args.toString());
        if (!isGameInited) {
            initGame();
        }
        double coin = args.getDouble(0);
        int source = args.getInt(1);
        UMGameAgent.bonus(coin, source);
    }

    @SuppressWarnings({"unused"})
    private void bonusWithItem(final JSONArray args) throws JSONException {
        Log.d("SdkBridge", "bonusWithItem  args:" + args.toString());
        if (!isGameInited) {
            initGame();
        }
        String item = args.getString(0);
        int number = args.getInt(1);
        double price = args.getDouble(2);
        int source = args.getInt(3);
        UMGameAgent.bonus(item, number, price, source);
    }
}
