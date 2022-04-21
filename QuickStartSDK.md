# 快速集成Android SDK

本文主要介绍如何快速地将腾讯云呼叫中心 TCCC SDK(Android) 集成到您的项目中，只要按照如下步骤进行配置，就可以完成 SDK 的集成工作。

## 开发环境要求
- Android Studio 3.5+。
- Android 4.1（SDK API 16）及以上系统。

## 集成 SDK（aar、jar）

### 手动下载（aar、jar）
目前我们暂时还未发布到 mavenCentral ，您只能手动下载 SDK 集成到工程里：

1. 下载最新版本 [TCCC SDK](https://tccc.qcloud.com/assets/doc/user/android/TCCC_SDK_Android_latest.zip)。
2. 将下载到的 aar 文件拷贝到工程的 **app/libs** 目录下。如果你项目中也用到了 okhttp3.0 这个库，保留其中一个即可。
3. 在工程根目录下的 build.gradle 中，指定本地仓库路径。
![](https://qcloudimg.tencent-cloud.cn/raw/272b561db87fedec0442d41b757b0b53.png)
```
implementation fileTree(dir: "libs",includes: ['*.aar','*.jar'])
```
4. 在 app/build.gradle的defaultConfig 中，指定 App 使用的 CPU 架构。
```
defaultConfig {
       ndk {
           abiFilters "armeabi", "armeabi-v7a", "arm64-v8a"
       }
}
```
>?目前 TCCC SDK 支持 armeabi ， armeabi-v7a 和 arm64-v8a。
5. 在 app/src/AndroidManifest.xml 中，指定 App 不允许应用参与备份和恢复基础架构。
![](https://qcloudimg.tencent-cloud.cn/raw/5ddbf9424b6f5157b17a61f368b54f20.png)
6. 单击![](https://main.qcloudimg.com/raw/d6b018054b535424bb23e42d33744d03.png)**Sync Now**，完成 TCCC SDK 的集成工作。


## 配置 App 权限
在 AndroidManifest.xml 中配置 App 的权限，TCCC SDK 需要以下权限：
```
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.READ_PHONE_STATE" />
<uses-feature android:name="android.hardware.camera.autofocus" />
```

>! 请勿设置 `android:hardwareAccelerated="false"`，关闭硬件加速之后，会导致对方的视频流无法渲染。

## 设置混淆规则
在 proguard-rules.pro 文件，将 TCCC SDK 相关类加入不混淆名单：

```
-keep class com.tencent.** { *; }
```


## API 概览
### 创建实例和事件回调
| API | 描述 |
|-----|-----|
| [sharedInstance](https://tccc.qcloud.com/assets/doc/user/android/classcom_1_1tencent_1_1tccc_1_1_t_c_c_c_cloud.html#a22af858f9d59111b6cf8f6a6258b7c11) | 创建 TCCCCloud 实例（单例模式） |
| [destroySharedInstance](https://tccc.qcloud.com/assets/doc/user/android/classcom_1_1tencent_1_1tccc_1_1_t_c_c_c_cloud.html#a201ba05e2bc74c4059a5300aaf494054) | 销毁 TCCCCloud 实例（单例模式）  |
| [setListener](https://tccc.qcloud.com/assets/doc/user/android/classcom_1_1tencent_1_1tccc_1_1_t_c_c_c_cloud.html#a15d9d2786df1d0982eda5d681fbd1643) | 设置 TCCC 事件回调 |

#### 创建实例和设置事件回调示例代码
```java
// 创建实例和设置事件回调
TCCCCloud mTCCCCloud = TCCCCloud.sharedInstance(getApplicationContext());
mTCCCCloud.setListener(new TCCCCloudListener() {});
```

### 登录相关接口函数
| API | 描述 |
|-----|-----|
| [login](https://tccc.qcloud.com/assets/doc/user/android/classcom_1_1tencent_1_1tccc_1_1_t_c_c_c_cloud.html#ae8cb4ddde379643f1f15af74655eab02) | SDK 登录 |
| [isLogin](https://tccc.qcloud.com/assets/doc/user/android/classcom_1_1tencent_1_1tccc_1_1_t_c_c_c_cloud.html#a7af854f3c3ab94926e326ea964de396c) | 判断 SDK 是否已经登录 |

#### 判断是否登录和登录示例代码
```java
/// 判断SDK是否已登录
boolean isLogin = mTCCCCloud.isLogin();
if(isLogin){
    // 发起呼叫
}else{
    /// SDK 登录
    TCCCCloudDef.TCCCLoginParams loginParams = new TCCCCloudDef.TCCCLoginParams();
    // 你呼叫中心 14xxx 开头的应用ID
    loginParams.sdkAppId= 0;
    loginParams.clientUserId = "UserId";
    // 正确的 UserSig 签发方式是将 UserSig 的计算代码集成到您的服务端，并提供面向 App 的接口，在需要 UserSig 时由您的 App 向业务服务器发起请求获取动态 UserSig。
    // 更多详情请参见 [创建用户数据签名](https://cloud.tencent.com/document/product/679/58260)
    loginParams.clientUserSig = ""; 
    mTCCCCloud.login(loginParams, new TXCallback() {
        @Override
        public void onSuccess() {
            // 登录成功
            // 发起呼叫
        }

        @Override
        public void onError(int i, String s) {
            // 登录失败
        }
    });
}
```

### 呼叫相关接口函数
| API | 描述 |
|-----|-----|
| [startCall](https://tccc.qcloud.com/assets/doc/user/android/classcom_1_1tencent_1_1tccc_1_1_t_c_c_c_cloud.html#a910147a1720b2fde3f433e905d753670) | 发起通话 |
| [endCall](https://tccc.qcloud.com/assets/doc/user/android/classcom_1_1tencent_1_1tccc_1_1_t_c_c_c_cloud.html#a125469b72de4255d694ff8f3d5b48ed0) | 结束通话 |

#### 发起呼叫和结束呼叫示例代码
```java
// 发起视频呼叫
TCCCCloudDef.TCCCStartCallParams callParams = new TCCCCloudDef.TCCCStartCallParams();
callParams.channelId = ""; // 应用配置入口的 APP ID
mTCCCCloud.startCall(callParams);
// 用户主动结束呼叫或者结束通话
mTCCCCloud.endCall();
```

### 视频相关接口函数
| API | 描述 |
|-----|-----|
| [startLocalPreview](https://tccc.qcloud.com/assets/doc/user/android/classcom_1_1tencent_1_1tccc_1_1_t_c_c_c_cloud.html#a0956a22c6f75b29a5b9e740220fe9e30) | 开启本地摄像头的预览画面（移动端） |
| [stopLocalPreview](https://tccc.qcloud.com/assets/doc/user/android/classcom_1_1tencent_1_1tccc_1_1_t_c_c_c_cloud.html#aeb690f5e973323d45d9ed30c6caa9a6c) | 停止摄像头预览 |
| [switchCamera](https://tccc.qcloud.com/assets/doc/user/android/classcom_1_1tencent_1_1tccc_1_1_t_c_c_c_cloud.html#abd7bc0c604338ac028c7785e710245f7) | 切换摄像头 |
| [updateLocalView](https://tccc.qcloud.com/assets/doc/user/android/classcom_1_1tencent_1_1tccc_1_1_t_c_c_c_cloud.html#a2f0eafc317860c0833129610af601ad7) | 更新本地摄像头的预览画面 |
| [setLocalRenderParams](https://tccc.qcloud.com/assets/doc/user/android/classcom_1_1tencent_1_1tccc_1_1_t_c_c_c_cloud.html#a4dc074c69bdd51db822816e399044feb) | 设置本地画面的渲染参数 |
| [startRemoteView](https://tccc.qcloud.com/assets/doc/user/android/classcom_1_1tencent_1_1tccc_1_1_t_c_c_c_cloud.html#a7edc51b17a4edad04ef18bfbd88cf2da) | 订阅远端坐席的视频流，并绑定视频渲染控件 |
| [stopRemoteView](https://tccc.qcloud.com/assets/doc/user/android/classcom_1_1tencent_1_1tccc_1_1_t_c_c_c_cloud.html#ac4d693712eb10553ba1efd04dc4e4ada) | 停止订阅远端用户的视频流，并释放渲染控件 |
| [setVideoEncoderParam](https://tccc.qcloud.com/assets/doc/user/android/classcom_1_1tencent_1_1tccc_1_1_t_c_c_c_cloud.html#a6cc850ec05b564c81808c522d40f2fe2) | 设置视频编码器的编码参数 |
| [updateRemoteView](https://tccc.qcloud.com/assets/doc/user/android/classcom_1_1tencent_1_1tccc_1_1_t_c_c_c_cloud.html#ab2aeb6c7d34085d8f8ec18802a90231c) | 更新远端用户的视频渲染控件 |

#### 开启本地视频和远端视频示例代码
```java
// 开启前置摄像头预览，其中 txvMainVideoView 为视频画面控件
mTCCCCloud.startLocalPreview(true, txvMainVideoView);
// 显示坐席端画面，其中 txvMainVideoView 为视频画面控件
mTCCCCloud.startRemoteView(TCCCCloudDef.TCCC_VIDEO_STREAM_TYPE_BIG,txvMainVideoView);               
```

### 音频相关接口函数
| API | 描述 |
|-----|-----|
| [startLocalAudio](https://tccc.qcloud.com/assets/doc/user/android/classcom_1_1tencent_1_1tccc_1_1_t_c_c_c_cloud.html#ab3148068d47300eac5bb31ad15696de0) | 开启本地音频的采集和发布 |
| [stopLocalAudio](https://tccc.qcloud.com/assets/doc/user/android/classcom_1_1tencent_1_1tccc_1_1_t_c_c_c_cloud.html#a23fd941d20940e1f219a306a1ccab353) | 停止本地音频的采集和发布 |
| [muteLocalAudio](https://tccc.qcloud.com/assets/doc/user/android/classcom_1_1tencent_1_1tccc_1_1_t_c_c_c_cloud.html#a0aaa8f20140d86fcbb7ab4043f7ca823) | 暂停/恢复发布本地的音频流 |
| [setSoundMode](https://tccc.qcloud.com/assets/doc/user/android/classcom_1_1tencent_1_1tccc_1_1_t_c_c_c_cloud.html#a61360c7ba5ec16fd243ea9f6d4f80e9a) | 设置音频路由 |
| [muteRemoteAudio](https://tccc.qcloud.com/assets/doc/user/android/classcom_1_1tencent_1_1tccc_1_1_t_c_c_c_cloud.html#a15564e6d2d4a430fb316981280e4f144) | 暂停/恢复播放远端的音频流 |
| [enableAudioVolumeEvaluation](https://tccc.qcloud.com/assets/doc/user/android/classcom_1_1tencent_1_1tccc_1_1_t_c_c_c_cloud.html#a1518deda8754cbf34525f02fe73a2fa2) | 启用音量大小提示 |
| [setAudioCaptureVolume](https://tccc.qcloud.com/assets/doc/user/android/classcom_1_1tencent_1_1tccc_1_1_t_c_c_c_cloud.html#ae25ff48c24d7e5e28e63fd90ffc88873) | 设定本地音频的采集音量 |
| [getAudioCaptureVolume](https://tccc.qcloud.com/assets/doc/user/android/classcom_1_1tencent_1_1tccc_1_1_t_c_c_c_cloud.html#ac3aa956dbc6e7fc7e6d3b549e3f8cd36) | 获取本地音频的采集音量 |
| [setAudioPlayoutVolume](https://tccc.qcloud.com/assets/doc/user/android/classcom_1_1tencent_1_1tccc_1_1_t_c_c_c_cloud.html#ae7af92546733f6bd7bf902e803e1e51b) | 设定远端音频的播放音量 |
| [getAudioPlayoutVolume](https://tccc.qcloud.com/assets/doc/user/android/classcom_1_1tencent_1_1tccc_1_1_t_c_c_c_cloud.html#ae3095a5f198c6b1cfa3821a37bbc43c2) | 获取远端音频的播放音量 |

#### 开启本地音频和停止本地音频示例代码
```java
// 开启本地音频采集
mTCCCCloud.startLocalAudio(TCCCCloudDef.TCCC_AUDIO_QUALITY_SPEECH);
// 暂停发布本地音频流
mTCCCCloud.muteLocalAudio(mIsMuteMic);
```

### 调试相关接口
| API | 描述 |
|-----|-----|
| [getSDKVersion](https://tccc.qcloud.com/assets/doc/user/android/classcom_1_1tencent_1_1tccc_1_1_t_c_c_c_cloud.html#a06fc025ba34778f33131804bb13cc452) | 获取 SDK 版本信息 |
| [setLogLevel](https://tccc.qcloud.com/assets/doc/user/android/classcom_1_1tencent_1_1tccc_1_1_t_c_c_c_cloud.html#a85a5fcff692eb579ceb38cb91f72c75e) | 设置 Log 输出级别 |
| [setConsoleEnabled](https://tccc.qcloud.com/assets/doc/user/android/classcom_1_1tencent_1_1tccc_1_1_t_c_c_c_cloud.html#a3ebbe12044944180abc5675bff8f1936) | 启用/禁用控制台日志打印 |
| [showDebugView](https://tccc.qcloud.com/assets/doc/user/android/classcom_1_1tencent_1_1tccc_1_1_t_c_c_c_cloud.html#acb95a94f4328e794b25fa8d201cc617d) | 显示仪表盘 |
| [callExperimentalAPI](https://tccc.qcloud.com/assets/doc/user/android/classcom_1_1tencent_1_1tccc_1_1_t_c_c_c_cloud.html#a2ced0e180d468367b84658b707c7ed71) | 调用实验性接口 |

#### 获取SDK版本示例代码
```java
// 获取SDK 版本号
TCCCCloud.getSDKVersion();
```


### 错误和警告事件
| API | 描述 |
|-----|-----|
| [onError](https://tccc.qcloud.com/assets/doc/user/android/classcom_1_1tencent_1_1tccc_1_1_t_c_c_c_cloud_listener.html#a0608e16b140142c8e709ef4dc602ee8b) | 错误事件回调 |
| [onWarning](https://tccc.qcloud.com/assets/doc/user/android/classcom_1_1tencent_1_1tccc_1_1_t_c_c_c_cloud_listener.html#a11c8223c4ca981a7c05c677401895a70) | 警告事件回调 |
#### 处理错误回调事件回调示例代码
```java
mTCCCCloud.setListener(new TCCCCloudListener() {
    /**
        * 错误事件回调
        * 错误事件，表示 SDK 抛出的不可恢复的错误，比如进入房间失败或设备开启失败等。
        *
        * @param errCode   错误码
        * @param errMsg    错误信息
        * @param extraInfo 扩展信息字段，个别错误码可能会带额外的信息帮助定位问题
        */
    @Override
    public void onError(int errCode, String errMsg, Bundle extraInfo) {
        super.onError(errCode, errMsg, extraInfo);
    }

    /**
        * 警告事件回调
        * 警告事件，表示 SDK 抛出的提示性问题，比如视频出现卡顿或 CPU 使用率太高等。
        *
        * @param warningCode 警告码
        * @param warningMsg  警告信息
        * @param extraInfo   扩展信息字段，个别警告码可能会带额外的信息帮助定位问题
        */
    @Override
    public void onWarning(int warningCode, String warningMsg, Bundle extraInfo) {
        super.onWarning(warningCode, warningMsg, extraInfo);
    }
});
```

### 呼叫相关事件回调
| API | 描述 |
|-----|-----|
| [onStartCall](https://tccc.qcloud.com/assets/doc/user/android/classcom_1_1tencent_1_1tccc_1_1_t_c_c_c_cloud_listener.html#a6666f9e33b31cab07da2ed9494f82004) | 发起通话成功与否的事件回调 |
| [onAccepted](https://tccc.qcloud.com/assets/doc/user/android/classcom_1_1tencent_1_1tccc_1_1_t_c_c_c_cloud_listener.html#acd3e1cf6416ea0bffe582dd856e54832) | 坐席端接听回调 |
| [onCallEnd](https://tccc.qcloud.com/assets/doc/user/android/classcom_1_1tencent_1_1tccc_1_1_t_c_c_c_cloud_listener.html#a515b52ccf18864a11a3061576fc5caa1) | 通话结束回调 |

#### 处理接听和坐席挂断事件回调示例代码
```java
mTCCCCloud.setListener(new TCCCCloudListener() {
    /**
     * 发起通话成功与否的事件回调
     * 调用 TCCCCloud 中的 startCall() 接口执行呼叫操作后，会收到来自 TCCCCloudListener 的 onStartCall(result) 回调：
     * 如果发起视频通话成功，回调 result 会是一个正数（result > 0），代表发起视频通话所消耗的时间，单位是毫秒（ms）。
     * 如果发起视频通话失败，回调 result 会是一个负数（result < 0），代表失败原因的错误码。 发起视频通话失败的错误码含义请参见错误码表。
     *
     * @param result result > 0 时为发起视频通话耗时（ms），result < 0 时为发起视频通话错误码。
     */
    @Override
    public void onStartCall(long result) {
        super.onStartCall(result);
    }

    /**
     * 坐席端接听回调，默认不播放视频，需要播放画面调用 startRemoteView
     */
    @Override
    public void onAccepted() {
        super.onAccepted();
    }

    /**
     * 通话结束回调
     * 收到该回调说明本次通话结束了。
     *
     * @param reason  结束原因
     *                坐席无人接听     TCCC_SESSION_END_NO_SEAT_ONLINE
     *                坐席端挂断       TCCC_SESSION_END_SEAT_HAND_UP
     *                接听超时         TCCC_SESSION_END_TIME_OUT
     *                坐席接听响应失败  TCCC_SESSION_END_SEAT_UNKNOWN
     *                用户主动挂断     TCCC_SESSION_END_USER_HANG_UP
     * @param message 结束原因描述
     */
    @Override
    public void onCallEnd(int reason, String message) {
        super.onCallEnd(reason, message);
    }
});
```

### 音视频相关事件回调
| API | 描述 |
|-----|-----|
| [onRemoteVideoAvailable](https://tccc.qcloud.com/assets/doc/user/android/classcom_1_1tencent_1_1tccc_1_1_t_c_c_c_cloud_listener.html#a7f9ed6f1ffcd0f54317b66243fd3f7f1) | 坐席端用户发布/取消了自己的视频 |
| [onRemoteAudioAvailable](https://tccc.qcloud.com/assets/doc/user/android/classcom_1_1tencent_1_1tccc_1_1_t_c_c_c_cloud_listener.html#a0f54877aac5f4a576c87d6cdff979acc) | 坐席端用户发布/取消了自己的音频 |
| [onUserVoiceVolume](https://tccc.qcloud.com/assets/doc/user/android/classcom_1_1tencent_1_1tccc_1_1_t_c_c_c_cloud_listener.html#a8108146367c7efe59da09e8e99dcd3f7) | 音量大小的反馈回调 |

#### 处理远端视频画面事件回调示例代码
```java
mTCCCCloud.setListener(new TCCCCloudListener() {
    /**
        * 坐席端用户发布/取消了自己的视频
        * 当您收到 onSeatVideoAvailable(true) 通知时，表示坐席端用户发布了自己的声音
        *
        * @param available 远端用户是否发布（或取消发布）了主路视频画面，true: 发布；false：取消发布。
        */
    @Override
    public void onRemoteVideoAvailable(boolean available) {
        super.onRemoteVideoAvailable(available);
        if(available) {
            mRemoteView.setVisibility(View.VISIBLE);
            mTCCCCloud.startRemoteView(TCCCCloudDef.TCCC_VIDEO_STREAM_TYPE_SMALL,mRemoteView);
        }else{
            mRemoteView.setVisibility(View.GONE);
        }
    }
});
```

### 与云端连接情况的事件回调
| API | 描述 |
|-----|-----|
| [onConnectionLost](https://tccc.qcloud.com/assets/doc/user/android/classcom_1_1tencent_1_1tccc_1_1_t_c_c_c_cloud_listener.html#af9f1a8ae89e66c9f8082d45f58b43704) | SDK 与云端的连接已经断开 |
| [onTryToReconnect](https://tccc.qcloud.com/assets/doc/user/android/classcom_1_1tencent_1_1tccc_1_1_t_c_c_c_cloud_listener.html#a38d2b9cb600fb8aefcf658b53e937513) | SDK 正在尝试重新连接到云端 |
| [onConnectionRecovery](https://tccc.qcloud.com/assets/doc/user/android/classcom_1_1tencent_1_1tccc_1_1_t_c_c_c_cloud_listener.html#af76acf1d6646bd45243e6a8f08e28089) | SDK 与云端的连接已经恢复 |

#### 与云端连接情况的事件回调示例代码
```java

```


## API 错误码
### 基础错误码

| 符号 | 值 | 含义 |
|---|---|---|
|TCCC_ERR_NULL|0|无错误|
|TCCC_SYSTEM_ERROR|-1002|TCCC 系统错误|
|TCCC_SYSTEM_PARAMETER_ERROR|-1001|参数错误|

### 登录相关错误码

| 符号 | 值 | 含义 |
|---|---|---|
|TCCC_USER_NO_LOGIN|-2019|用户未登录|
|TCCC_SYSTEM_REGISTRATION_FAILED|-2013|消息系统注册失败|
|TCCC_LOGIN_TICKET_ERROR|-2014|登录态异常，签名校验失败|
|TCCC_LOGIN_TICKET_EXPIRED|-2015|登录态过期|

### 呼叫相关错误码

| 符号 | 值 | 含义 |
|---|---|---|
|TCCC_TIM_INIT_FAIL|-3001|TIM 初始化失败|
|TCCC_START_SESSION_FAIL|-3002|开始会话失败|

## 常见问题
###  如何查看 TCCC 日志？
TCCC 的日志默认压缩加密，后缀为 .xlog。日志是否加密是可以通过 setLogCompressEnabled 来控制，生成的文件名里面含 C(compressed) 的就是加密压缩的，含 R(raw) 的就是明文的。
- Android：
	- 日志路径：`/sdcard/Android/data/包名/files/log/liteav/`
>?
>- 查看 .xlog 文件需要下载解密工具，在python 2.7环境中放到 xlog 文件同目录下直接使用 `python decode_mars_log_file.py` 运行即可。
>- 日志解密工具下载地址：`dldir1.qq.com/hudongzhibo/log_tool/decode_mars_log_file.py`，日志相关详情参考 [日志输出配置](https://cloud.tencent.com/developer/article/1502366)。

### TCCC Android 端能不能支持模拟器？
TCCC 目前版本暂时不支持，未来会支持模拟器。

###  两台设备同时运行 Demo，为什么画面、声音会断断续续？
请确保两台设备在运行 Demo 时使用的是不同的 clientUserID，TCCC 不支持同一个 clientUserID （除非 SDKAppID 不同）在两个设备同时使用。

### TCCC 怎么校验生成的 UserSig 是否正确？ 
可通过云 API 调用生成UserSig，具体可查看 [创建用户数据签名](https://cloud.tencent.com/document/product/679/58260) 接口文档

### TCCC 视频画面出现黑边怎么去掉？
设置 TCCC_VIDEO_RENDER_MODE_FILL（填充）即可解决，TCCC 视频渲染模式分为填充和适应，本地渲染画面可以通过 setLocalRenderParams() 设置，远端渲染画面可以通过 setRemoteRenderParams 设置：

- TCCC_VIDEO_RENDER_MODE_FILL：图像铺满屏幕，超出显示视窗的视频部分将被截掉，所以画面显示可能不完整。
- TCCC_VIDEO_RENDER_MODE_FIT：图像长边填满屏幕，短边区域会被填充黑色，但画面的内容肯定是完整的。

### TCCC 自己的本地画面和远端画面左右相反？
本地默认采集的画面是镜像的。App 端可以通过 setLocalRenderParams 接口设置 mirrorType ，该接口只改变本地摄像头预览画面的镜像模式；

