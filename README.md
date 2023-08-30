# 腾讯云联络中心 Android 用户端简单Demo
腾讯云联络中心支持安卓设备音视频通话，用户通过安卓设备发起通话，客服可在电脑端工作台接待。

- [快速跑通 Android Demo](QuickStartDemo.md)
- [快速集成 Android SDK](QuickStartSDK.md)
- [API 概览以及示例](api.md)
- [音频客服请参考tccc-user-android-example](https://github.com/TencentCloud/tccc-user-android-example)

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
