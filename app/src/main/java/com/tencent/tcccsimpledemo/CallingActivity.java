package com.tencent.tcccsimpledemo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

import java.util.UUID;

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

        mTCCCCloud = TCCCCloud.sharedInstance(getApplicationContext());
        mainHandler = new Handler(this.getApplicationContext().getMainLooper());
        initListener();
    }

    private void initListener(){
        ll_mute_mic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mIsMuteMic = !mIsMuteMic;
                img_mute_mic.setActivated(mIsMuteMic);
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
                mTCCCCloud.endCall();
            }
        });
    }

    private void initTCCC(){
        TCCCCloudDef.TCCCRenderParams tcccRenderParams = new TCCCCloudDef.TCCCRenderParams();
        tcccRenderParams.fillMode = TCCCCloudDef.TCCC_VIDEO_RENDER_MODE_FILL;
        mTCCCCloud.setLocalRenderParams(tcccRenderParams);
        mTCCCCloud.startLocalPreview(mIsFrontCamera, txvMainVideoView);
        mTCCCCloud.startLocalAudio(TCCCCloudDef.TCCC_AUDIO_QUALITY_SPEECH);
        txt_tips.setText("准备呼叫...");

        mTCCCCloud.setListener(new TCCCCloudListener() {
            @Override
            public void onError(int errCode, String errMsg, Bundle extraInfo) {
                super.onError(errCode, errMsg, extraInfo);
            }

            @Override
            public void onWarning(int warningCode, String warningMsg, Bundle extraInfo) {
                super.onWarning(warningCode, warningMsg, extraInfo);
            }
            @Override
            public void onStartCall(long result) {
                if(result>0){
                    txt_tips.setText("呼出成功");
                }else{
                    txt_tips.setText("呼出异常["+result+"]");
                }
            }

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

            @Override
            public void onRemoteVideoAvailable(boolean available) {
                super.onRemoteVideoAvailable(available);
                if(available) {
                    mTCCCCloud.startRemoteView(TCCCCloudDef.TCCC_VIDEO_STREAM_TYPE_BIG,txvMainVideoView);
                    txvMainVideoView.setVisibility(View.VISIBLE);
                }
                else {
                    txvMainVideoView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onRemoteAudioAvailable(boolean available) {
                super.onRemoteAudioAvailable(available);
            }

            @Override
            public void onConnectionLost() {
                super.onConnectionLost();
            }

            @Override
            public void onTryToReconnect() {
                super.onTryToReconnect();
            }

            @Override
            public void onConnectionRecovery() {
                super.onConnectionRecovery();
            }
        });
        boolean isLogin = mTCCCCloud.isLogin();
        if(isLogin){
            startVideoCall();
        }else{
            String clientUserId = "46256ef2870b4848a1dc57fff22b9121";
            GenerateTestUserSig.genTestUserSig(clientUserId, new GenerateTestUserSig.UserSigCallBack() {
                @Override
                public void onSuccess(String userSig) {
                    TCCCCloudDef.TCCCLoginParams loginParams = new TCCCCloudDef.TCCCLoginParams();
                    loginParams.sdkAppId= GenerateTestUserSig.SDKAPPID;
                    loginParams.clientUserId = clientUserId;
                    loginParams.clientUserSig = userSig;
                    mTCCCCloud.login(loginParams, new TXCallback() {
                        @Override
                        public void onSuccess() {
                            txt_tips.setText("登录成功...");
                            startVideoCall();
                        }

                        @Override
                        public void onError(int i, String s) {
                            Toast.makeText(CallingActivity.this,"登录失败,"+s,Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onError(int code, String desc) {
                    Toast.makeText(CallingActivity.this,"计算UserSig签名失败,"+desc,Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

    private void startVideoCall() {
        mIsCalling = false;
        txt_tips.setText("呼叫中...");
        TCCCCloudDef.TCCCStartCallParams callParams = new TCCCCloudDef.TCCCStartCallParams();
        callParams.channelId = GenerateTestUserSig.VIDEO_CHANNELID;

        mTCCCCloud.startCall(callParams);
    }

    private void stopNgoBack(String msg){
        txt_tips.setText(msg);
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        mainHandler.postDelayed(()->{
            Intent intent = new Intent(CallingActivity.this, MainActivity.class);
            startActivity(intent);
        },3000);
    }

}