package com.tencent.tcccsimpledemo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.debug.GenerateTestUserSig;
import com.tencent.tccc.TCCCCloud;
import com.tencent.tccc.TCCCCloudDef;
import com.tencent.tccc.TCCCCloudListener;
import com.tencent.tccc.TXCallback;
import com.tencent.tccc.ui.TXVideoView;
import com.tencent.tcccsimpledemo.base.TCCCBaseActivity;

import java.util.ArrayList;

public class CallingActivity extends TCCCBaseActivity {
    private TXVideoView txvMainVideoView;
    private TXVideoView txvSmallView;
    private TextView txt_tips;
    private TCCCCloud mTCCCCloud;
    private Handler mainHandler;

    private RelativeLayout ll_hangup;
    private ImageView img_hangup;
    private TextView  tv_hangup;
    private RelativeLayout ll_mute_mic;
    private ImageView img_mute_mic;
    private TextView  tv_mute_mic;
    private RelativeLayout ll_camera;
    private ImageView img_camera;
    private TextView tv_camera;
    protected boolean mIsFrontCamera  = true;   // 是否是前置摄像头
    protected boolean mIsCameraOpen  = true;    // 是否打开摄像头
    protected boolean mIsMuteMic     = false;  // 是否静音
    protected boolean mIsCalling     = false; // 正在通话中
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calling);
        initView();
        if (checkPermission()) {
            initTCCC();
        }
    }

    @Override
    protected void onPermissionGranted() {
        initTCCC();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        TCCCCloud.destroySharedInstance();
    }

    private void initView() {
        txvMainVideoView = findViewById(R.id.txv_main);
        txvSmallView = findViewById(R.id.txv_small);
        txt_tips = findViewById(R.id.txt_tips);
        ll_hangup = findViewById(R.id.ll_hangup);
        img_hangup = findViewById(R.id.img_hangup);
        tv_hangup = findViewById(R.id.tv_hangup);
        ll_camera = findViewById(R.id.ll_camera);
        img_camera = findViewById(R.id.img_camera);
        tv_camera = findViewById(R.id.tv_camera);
        ll_mute_mic = findViewById(R.id.ll_mute_mic);
        img_mute_mic = findViewById(R.id.img_mute_mic);
        tv_mute_mic = findViewById(R.id.tv_mute_mic);

        mainHandler = new Handler(this.getApplicationContext().getMainLooper());
        initListener();
    }

    private void initListener(){
        ll_mute_mic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mIsMuteMic = !mIsMuteMic;
                img_mute_mic.setActivated(mIsMuteMic);
                // 暂停/恢复发布本地的音频流
                mTCCCCloud.muteLocalAudio(mIsMuteMic);
                tv_mute_mic.setText(mIsMuteMic?R.string.calling_toast_enable_mute:R.string.calling_toast_disable_mute);
            }
        });
        ll_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mIsCameraOpen = !mIsCameraOpen;
                if(mIsCameraOpen){
                    tv_camera.setText(R.string.calling_toast_disable_camera);
                    mTCCCCloud.startLocalPreview(mIsFrontCamera,mIsCalling?txvSmallView:txvMainVideoView);
                    img_camera.setActivated(false);

                }else{
                    mTCCCCloud.stopLocalPreview();
                    tv_camera.setText(R.string.calling_toast_enable_camera);
                    img_camera.setActivated(true);
                }
            }
        });
        ll_hangup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mIsCalling = false;
                // 用户主动结束呼叫或者结束通话
                mTCCCCloud.endCall();
            }
        });
    }

    private void initTCCC(){
        /// 创建实例和设置事件回调
        mTCCCCloud = TCCCCloud.sharedInstance(getApplicationContext());
        /// 设置事件回调
        mTCCCCloud.setListener(new TCCCCloudListener() {

            /// 错误事件回调
            @Override
            public void onError(int errCode, String errMsg, Bundle extraInfo) {
                super.onError(errCode, errMsg, extraInfo);

            }

            /// 警告事件回调
            @Override
            public void onWarning(int warningCode, String warningMsg, Bundle extraInfo) {
                super.onWarning(warningCode, warningMsg, extraInfo);
            }

            /// 发起通话成功与否的事件回调
            @Override
            public void onStartCall(long result) {
                if(result>0){
                    txt_tips.setText("呼出成功");
                }else{
                    txt_tips.setText("呼出异常["+result+"]");
                }
            }

            /// 坐席端接听回调
            @Override
            public void onAccepted() {
                super.onAccepted();
                mIsCalling = true;
                txt_tips.setText("已接通");
                txvSmallView.setVisibility(View.VISIBLE);
                mTCCCCloud.updateLocalView(txvSmallView);
                TCCCCloudDef.TCCCRenderParams tcccRenderParams = new TCCCCloudDef.TCCCRenderParams();
                tcccRenderParams.fillMode = TCCCCloudDef.TCCC_VIDEO_RENDER_MODE_FILL;
                mTCCCCloud.setRemoteRenderParams(TCCCCloudDef.TCCC_VIDEO_STREAM_TYPE_BIG,tcccRenderParams);
            }

            /// 通话结束回调
            @Override
            public void onCallEnd(int reason, String message) {
                super.onCallEnd(reason, message);
                String endMsg = "系统异常挂断";
                if (TCCCCloudDef.TCCC_CALL_END_USER_HANG_UP == reason) {
                    endMsg ="挂断成功";
                }else if(TCCCCloudDef.TCCC_CALL_END_NO_SEAT_ONLINE == reason){
                    endMsg ="坐席无人接听";
                }else if(TCCCCloudDef.TCCC_CALL_END_SEAT_HAND_UP == reason){
                    endMsg ="坐席已挂断";
                }else if(TCCCCloudDef.TCCC_CALL_END_TIME_OUT == reason){
                    endMsg ="坐席接听超时";
                }
                stopNgoBack(endMsg);
            }

            /// 坐席端用户发布/取消了自己的视频
            @Override
            public void onRemoteVideoAvailable(boolean available) {
                super.onRemoteVideoAvailable(available);
                if(available) {
                    // 显示坐席端画面
                    mTCCCCloud.startRemoteView(TCCCCloudDef.TCCC_VIDEO_STREAM_TYPE_BIG,txvMainVideoView);
                    txvMainVideoView.setVisibility(View.VISIBLE);
                }
                else {
                    txvMainVideoView.setVisibility(View.GONE);
                }
            }

            /// 音量大小的反馈回调
            @Override
            public void onUserVoiceVolume(ArrayList<TCCCCloudDef.TCCCVolumeInfo> userVolumes, int totalVolume){
                super.onUserVoiceVolume(userVolumes,totalVolume);
            }

            /// 坐席端用户发布/取消了自己的音频
            @Override
            public void onRemoteAudioAvailable(boolean available) {
                super.onRemoteAudioAvailable(available);
            }

            /// SDK 与云端的连接已经断开
            @Override
            public void onConnectionLost() {
                super.onConnectionLost();
            }

            /// SDK 正在尝试重新连接到云端
            @Override
            public void onTryToReconnect() {
                super.onTryToReconnect();
            }

            /// SDK 与云端的连接已经恢复
            @Override
            public void onConnectionRecovery() {
                super.onConnectionRecovery();
            }
        });
        
        // 设置本地画面的渲染参数
        // 参考： https://tccc.qcloud.com/assets/doc/user/android/classcom_1_1tencent_1_1tccc_1_1_t_c_c_c_cloud.html#a4dc074c69bdd51db822816e399044feb
        TCCCCloudDef.TCCCRenderParams tcccRenderParams = new TCCCCloudDef.TCCCRenderParams();
        tcccRenderParams.fillMode = TCCCCloudDef.TCCC_VIDEO_RENDER_MODE_FILL;
        mTCCCCloud.setLocalRenderParams(tcccRenderParams);
        // 开启摄像头预览
        mTCCCCloud.startLocalPreview(mIsFrontCamera, txvMainVideoView);
        // 开启本地音频采集
        mTCCCCloud.startLocalAudio(TCCCCloudDef.TCCC_AUDIO_QUALITY_SPEECH);
        txt_tips.setText("准备呼叫...");

        /// 判断SDK是否已登录
        boolean isLogin = mTCCCCloud.isLogin();
        if(isLogin){
            // 发起呼叫
            startVideoCall();
        }else{
            String clientUserId = "46256ef2870b4848a1dc57fff22b9121";
            // 计算UserSig
            GenerateTestUserSig.genTestUserSig(clientUserId, new GenerateTestUserSig.UserSigCallBack() {
                @Override
                public void onSuccess(String userSig) {
                    /// SDK 登录
                    TCCCCloudDef.TCCCLoginParams loginParams = new TCCCCloudDef.TCCCLoginParams();
                    loginParams.sdkAppId= GenerateTestUserSig.SDKAPPID;
                    loginParams.clientUserId = clientUserId;
                    // 正确的 UserSig 签发方式是将 UserSig 的计算代码集成到您的服务端，并提供面向 App 的接口，在需要 UserSig 时由您的 App 向业务服务器发起请求获取动态 UserSig。
                    // 更多详情请参见 [创建用户数据签名](https://cloud.tencent.com/document/product/679/58260)
                    loginParams.clientUserSig = userSig;
                    mTCCCCloud.login(loginParams, new TXCallback() {
                        @Override
                        public void onSuccess() {
                            txt_tips.setText("登录成功...");
                            startVideoCall();
                        }

                        @Override
                        public void onError(int i, String s) {
                            showToast("登录失败,"+s);
                        }
                    });
                }

                @Override
                public void onError(int code, String desc) {
                    showToast("计算UserSig签名失败,"+desc);
                }
            });
        }
    }

    /// 发起呼叫
    private void startVideoCall() {
        mIsCalling = false;
        txt_tips.setText("呼叫中...");
        // 发起视频呼叫
        TCCCCloudDef.TCCCStartCallParams callParams = new TCCCCloudDef.TCCCStartCallParams();
        callParams.channelId = GenerateTestUserSig.VIDEO_CHANNELID;
        mTCCCCloud.startCall(callParams);
    }

    private void stopNgoBack(String msg){
        txt_tips.setText(msg);
        showToast(msg);
        mainHandler.postDelayed(()->{
            Intent intent = new Intent(CallingActivity.this, MainActivity.class);
            startActivity(intent);
        },3000);
    }

    private void showToast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}