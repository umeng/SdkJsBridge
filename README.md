## 友盟+ 游戏统计SDK 原生native SDK + js桥接 API例程

### 介绍

基于 基础组件库 v2.2.5 + 游戏统计SDK v8.1.6+G 集成。


  游戏统计SDK包含 统计SDK的所有API接口，是统计SDK的超集。
本例程通过Android 原生WebView的addJavascriptInterface方法实现对底层
友盟统计SDK/游戏统计SDK相关API进行桥接，在js层暴露API接口供H5或者Web开发者调用。

  一般情况下，只有MobclickAgent.onPageStart/onPageEnd，MobclickAgent.onEvent、
MobclickAgent.onProfileSignIn/onProfileSignOff、以及各种游戏统计事件需要在H5页面
中调用，其它参数设置类API、SDK初始化API、以及MobclickAgent.onResume/onPause会话API，
都可以直接在native代码中直接调用，不需要桥接。

### 详情

1. web页面示例文件：as_demo_game/assets/demo.html
2. js桥接接口文件：as_demo_game/assets/Umeng.js
3. 示例WebView宿主Activity所在文件：as_demo_game/src/com/umeng/example/analytics/WebviewAnalytic.java
4. native层桥接文件：as_demo_game/src/com/umeng/example/analytics/SdkBridge.java

