package com.youtushuju.lingdongapp;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.Face;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.preference.PreferenceManager;
import android.util.Size;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.youtushuju.lingdongapp.api.DeviceApi;
import com.youtushuju.lingdongapp.api.DeviceApiResp;
import com.youtushuju.lingdongapp.api.UserModel;
import com.youtushuju.lingdongapp.common.Logf;
import com.youtushuju.lingdongapp.common.Sys;
import com.youtushuju.lingdongapp.database.RecordModel;
import com.youtushuju.lingdongapp.database.RecordServices;
import com.youtushuju.lingdongapp.device.LingDongApi;
import com.youtushuju.lingdongapp.common.Common;
import com.youtushuju.lingdongapp.common.Configs;
import com.youtushuju.lingdongapp.common.Constants;
import com.youtushuju.lingdongapp.device.PutOpenDoorReqStruct;
import com.youtushuju.lingdongapp.device.PutOpenDoorRespStruct;
import com.youtushuju.lingdongapp.device.SerialDataDef;
import com.youtushuju.lingdongapp.device.SerialReqStruct;
import com.youtushuju.lingdongapp.device.SerialRespStruct;
import com.youtushuju.lingdongapp.device.SerialSessionStruct;
import com.youtushuju.lingdongapp.gui.ActivityUtility;
import com.youtushuju.lingdongapp.gui.App;
import com.youtushuju.lingdongapp.gui.CameraFunc;
import com.youtushuju.lingdongapp.gui.CameraMaskView;
import com.youtushuju.lingdongapp.gui.DeviceFunc;
import com.youtushuju.lingdongapp.gui.DynamicTextureView;
import com.youtushuju.lingdongapp.gui.FaceRectView;
import com.youtushuju.lingdongapp.gui.ScreenSaverView;
import com.youtushuju.lingdongapp.gui.SoundAlert;
import com.youtushuju.lingdongapp.json.JSON;
import com.youtushuju.lingdongapp.json.JsonMap;

import java.io.ByteArrayOutputStream;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class MainActivity extends AppCompatActivity {
    private static final String ID_TAG = "MainActivity";
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    private static final int ID_HIDE_PERSON_PANEL_DELAY = 5000;
    private static final int ID_OPERATION_FINISHED_INTERVAL = 3500;
    private static final int ID_PERSON_VIEW_ANIM_DELAY = 500;
    private static final int ID_SCREEN_SAVER_ANIM_DELAY = 500;

    private static final boolean ID_SAVE_RECORD = true;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;

    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private View mControlsView;
    private boolean mVisible;

    private CameraFunc m_camera = null;
    private DynamicTextureView m_textureView = null;
    private CameraMaskView m_cameraMask = null;
    private ImageView m_previewImage;
    private TextView m_personName;
    private TextView m_personTime;
    private View m_personView;
    private CameraFunc.CameraInfoModel m_currentCamera = null;
    private FaceRectView m_faceRectView = null;
    private ScreenSaverView m_webView = null;
    private View m_screenSaverView = null;
    private int m_operateDeviceTimeout = Configs.ID_PREFERENCE_DEFAULT_OPERATE_DEVICE_TIMEOUT; // 操作设备超时
    private boolean m_cropBitmap = true;
    private int m_cameraUsagePlan = Configs.ID_PREFERENCE_DEFAULT_CAMERA_USAGE_PLAN;
    private Size m_textureViewInitSize = null;
    private int m_imageQuality = Configs.ID_PREFERENCE_DEFAULT_FACE_IMAGE_QUALITY;
    private View m_resultDialog = null;
    private Handler m_handler = new Handler(); // main thread
    private int m_debugMode = 0;
    private AnimatorSet m_resultDialogOpenAnimation = null;
    private AnimatorSet m_resultDialogCloseAnimation = null;
    private RecordServices m_recordDB = null;
    private boolean m_recordHistory = Configs.ID_PREFERENCE_DEFAULT_RECORD_HISTORY;
    private boolean m_playAlert = Configs.ID_PREFERENCE_DEFAULT_PLAY_VOICE_ALERT;
    private SoundAlert m_soundAlert = null;

    // 设备交互
    private LingDongApi m_lingdongApi = null;
    private DeviceFunc m_deviceFunc = null;

    // 测试
    private View m_apiDebugView = null;
    private View m_serialPortDebugView = null;
    private View m_afterApiDebugView = null;
    private long m_lastVerifyTime = 0L; // 上次检测到人脸的时间
    private int m_verifyFaceMaxInterval = Configs.ID_PREFERENCE_DEFAULT_OPEN_SCREEN_SAVER_MAX_INTERVAL; // 检测不到人脸则进入屏保的间隔

    // 相机线程
    private HandlerThread m_cameraHandlerThread = null;
    private Handler m_cameraHandler = null; // new thread
    // 网络/设备线程
    private HandlerThread m_deviceHandlerThread = null;
    private Handler m_deviceHandler = null; // new thread

    // 全部流程结束执行: 打开屏保, 停止预览, 重置状态
    private final Runnable m_finishOperation = new Runnable() {
        @Override
        public void run() {
            findViewById(R.id.main_response_debug_view).setVisibility(View.GONE);
            CloseResultDialog(true);

            StopPreview();

            m_lastVerifyTime = 0;
            OpenScreenSaver(true);
            CloseSerialPortDriver();
            m_hideFacePanel.run();

            m_cameraMask.SetState(CameraMaskView.ID_STATE_READY);
        }
    };
    // 流程中有失败时执行: 准备下次刷脸
    private final Runnable m_waitNextOperation = new Runnable() {
        @Override
        public void run() {
            findViewById(R.id.main_response_debug_view).setVisibility(View.GONE);
            CloseResultDialog(true);

            m_lastVerifyTime = 0;
            m_deviceFunc.Reset();
            m_hideFacePanel.run();

            ((TextView)m_afterApiDebugView.findViewById(R.id.main_request_debug_text)).setText("");
            ((TextView)m_afterApiDebugView.findViewById(R.id.main_response_debug_text)).setText("");
            ((TextView)m_apiDebugView.findViewById(R.id.main_request_debug_text)).setText("");
            ((TextView)m_apiDebugView.findViewById(R.id.main_response_debug_text)).setText("");
            ((TextView)m_serialPortDebugView.findViewById(R.id.main_request_debug_text)).setText("");
            ((TextView)m_serialPortDebugView.findViewById(R.id.main_response_debug_text)).setText("");
            m_cameraMask.SetState(CameraMaskView.ID_STATE_READY);
        }
    };
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            /*ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }*/
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private Runnable m_hideFacePanel = new Runnable() {
        @Override
        public void run() {
            m_personView.animate().setDuration(ID_PERSON_VIEW_ANIM_DELAY).alpha(0.0f).withEndAction(new Runnable() {
                @Override
                public void run() {
                    SetPreviewImage(null);
                    m_personView.setVisibility(View.INVISIBLE);
                    m_personName.setText("");
                    m_personTime.setText("");
                }
            }).start();
            mHideHandler.removeCallbacks(m_hideFacePanel);
        }
    };
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };
    private Runnable m_openScreenSaver = new Runnable() {
        @Override
        public void run() {
            m_webView.Load();
            m_screenSaverView.setVisibility(View.VISIBLE);
        }
    };
    private Runnable m_closeScreenSaver = new Runnable() {
        @Override
        public void run() {
            //m_screenSaverView.setVisibility(View.INVISIBLE);
            m_screenSaverView.setVisibility(View.GONE);
        }
    };

    // 相机事件回调
    private CameraFunc.OnCameraListener m_onCameraListener = new CameraFunc.OnCameraListener() {
        @Override
        public void OnCameraOpenResult(boolean success) {
            if(success)
                Logf.i(ID_TAG, "打开相机成功");
            else
                ShowToast("打开相机失败", Toast.LENGTH_LONG);
        }

        @Override
        public void OnCameraOpenPreviewResult(boolean success) {
            if(success)
                Logf.i(ID_TAG, "打开相机预览成功");
            else
                ShowToast("打开相机预览失败", Toast.LENGTH_LONG);
        }

        @Override
        public void OnPreviewStart() {
            Logf.i(ID_TAG, "相机预览开始");
        }

        @Override
        public void OnPreviewStop() {
            Logf.i(ID_TAG, "相机预览结束");
        }

        // 每次预览时回调
        @Override
        public void OnPreviewCapture(TotalCaptureResult result, Face faces[], long time, boolean always, int face_mode) {
            if(m_cameraMask.State() != CameraMaskView.ID_STATE_READY)
                return; // 正在操作设备

            // TODO: 测试
            if(!Common.ArrayIsEmpty(faces))
            {
                 Logf.e(ID_TAG, "检测到人脸: " + faces.length);
                 if(m_debugMode != 0)
                    ShowToast("检测到人脸: " + faces.length, Toast.LENGTH_SHORT);
            }

            // 仅有人脸时
            boolean available = always || (face_mode == CaptureRequest.STATISTICS_FACE_DETECT_MODE_OFF || !Common.ArrayIsEmpty(faces));

            if(available)
            {
                m_cameraMask.SetState(CameraMaskView.ID_STATE_SCANNING);
                //if(m_deviceFunc.IsCanStart())
                {
                    m_deviceFunc.Reset();
                    final Bitmap bitmap = m_textureView.getBitmap();
                    m_lastVerifyTime = time;
                    HandleCapturePreview(bitmap);
                }
            }
            else
            {
                if(m_lastVerifyTime == 0)
                    m_lastVerifyTime = time;
                else
                {
                    if(time - m_lastVerifyTime >= m_verifyFaceMaxInterval)
                    {
                        runOnUiThread(m_finishOperation);
                    }
                }
            }
        }

        @Override
        public void OnClose() {
            Logf.d(ID_TAG, "相机关闭");
        }

        @Override
        public void OnWarning(String message) {
            ShowToast(message, Toast.LENGTH_LONG);
            Logf.w(ID_TAG, message);
        }

        @Override
        public void OnError(String message) {
            ShowToast(message, Toast.LENGTH_LONG);
            Logf.e(ID_TAG, message);
        }

        @Override
        public void OnFail(String message) {
            ShowToast(message, Toast.LENGTH_LONG);
            Logf.e(ID_TAG, message);
        }

        @Override
        public void OnDebug(String message) {
            Logf.d(ID_TAG, message);
        }

        @Override
        public void OnCameraChanged(CameraFunc.CameraInfoModel info) {
            ((TextView)findViewById(R.id.main_camera_info_text)).setText(info.toString());
        }
    };

    // 屏保webview宿主对象
    private ScreenSaverView.WindowObject m_windowObject = new ScreenSaverView.WindowObject(this, m_handler) {
        // 开始刷脸
        @JavascriptInterface
        public void ToFace(final String name)
        {
            m_handler.post(new Runnable(){
                @Override
                public void run() {
                    findViewById(R.id.main_response_debug_view).setVisibility(View.GONE);

                Logf.i(ID_TAG, "设置垃圾类别: " + name);
                if("waste".equals(name))
                    m_deviceFunc.SetDoorId(DeviceApi.ID_KITCHEN_WASTE_DOOR_ID);
                else if("other".equals(name))
                    m_deviceFunc.SetDoorId(DeviceApi.ID_OTHER_WASTE_DOOR_ID);
                else
                {
                    Toast.makeText(MainActivity.this, "门类型无效!", Toast.LENGTH_SHORT).show();
                    return;
                }

                StartPreview();
                m_lastVerifyTime = System.currentTimeMillis();
                CloseScreenSaver(true);
                PlayAlert(SoundAlert.ID_SOUND_ALERT_WELCOME);
                }
            });
        }

        @JavascriptInterface
        public void OpenMenu()
        {
            m_handler.post(new Runnable(){
                @Override
                public void run() {
                    MainActivity.this.OpenMenu();
                }
            });
        }
    };
    // 串口事件回调
    private DeviceFunc.OnSerialPortListener m_deviceFuncListener = new DeviceFunc.OnSerialPortListener() {
        @Override
        public void OnOpened() {
            Logf.d(ID_TAG, "开始串口读写");
        }

        @Override
        public void OnClosed() {
            Logf.d(ID_TAG, "关闭串口读写");
        }

        @Override
        public void OnMessage(String msg) {
            //ShowToast(msg, Toast.LENGTH_SHORT);
            Logf.i(ID_TAG, msg);
        }

        // 非主流程的错误
        @Override
        public void OnError(final String error) {
            ShowToast(error, Toast.LENGTH_SHORT);
        }

        // 在主流程的错误
        @Override
        public void OnFatal(final String error) {
            ShowToast(error, Toast.LENGTH_SHORT);
           /* runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    WaitNextTime(error);
                }
            });*/
        }

        @Override
        public void OnRecv(final String recvData, String sendData, SerialRespStruct resp, SerialReqStruct req) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                if(m_debugMode != 0)
                {
                    findViewById(R.id.main_response_debug_view).setVisibility(View.VISIBLE);
                    ((TextView)m_serialPortDebugView.findViewById(R.id.main_response_debug_text)).setText(recvData);
                }
                }
            });
            //mHideHandler.postDelayed(m_hideFacePanel, ID_HIDE_PERSON_PANEL_DELAY);
            //m_deviceFunc.Reset();
        }

        @Override
        public void OnSend(final String data, SerialReqStruct req, boolean success) {
            if(m_debugMode != 0)
            {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    findViewById(R.id.main_response_debug_view).setVisibility(View.VISIBLE);
                    ((TextView)m_serialPortDebugView.findViewById(R.id.main_response_debug_text)).setText("");
                    ((TextView)m_serialPortDebugView.findViewById(R.id.main_request_debug_text)).setText(data);
                    }
                });
            }
            if(!success)
            {
                ShowToast("串口数据发送失败", Toast.LENGTH_SHORT);
            }
        }

        @Override
        public void OnStateChanged(int state) {
            Logf.e(ID_TAG, "串口读写状态变更: " + state);
            m_lastVerifyTime = 0L; //System.currentTimeMillis();
        }

        @Override
        public void OnTimeout(String sendData, final int timeout) {
            ShowToast("读取数据超时: " + timeout, Toast.LENGTH_SHORT);
            /*m_deviceFunc.Reset();
            mHideHandler.postDelayed(m_hideFacePanel, ID_HIDE_PERSON_PANEL_DELAY);*/
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        super.onCreate(savedInstanceState);

        ImageButton button;
        SharedPreferences preferences;

        setContentView(R.layout.main);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        m_lingdongApi = Configs.Instance().GetLingDongApi(this);

        mVisible = true;
        mControlsView = findViewById(R.id.main_layer);
        mContentView = findViewById(R.id.main_content);

        try
        {
            m_cameraUsagePlan = Integer.parseInt(preferences.getString(Constants.ID_PREFERENCE_CAMERA_USAGE_PLAN, "" + Configs.ID_PREFERENCE_DEFAULT_CAMERA_USAGE_PLAN));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        //findViewById(R.id.main_menu_button).setOnTouchListener(mDelayHideTouchListener);

        button = (ImageButton)findViewById(R.id.main_menu_button);
        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                OpenMenu();
            }
        });

        m_previewImage = (ImageView)findViewById(R.id.main_preview_image);
        m_personName = (TextView) findViewById(R.id.main_person_name);
        m_personTime = (TextView) findViewById(R.id.main_person_time);
        m_personView = findViewById(R.id.main_person_view);
        m_faceRectView = (FaceRectView)findViewById(R.id.main_face_rect_layer);
        m_faceRectView.setVisibility(View.GONE);

        m_screenSaverView = findViewById(R.id.main_screensaver_view);
        m_webView = (ScreenSaverView)findViewById(R.id.main_screensaver_content);
        m_webView.SetNativeObject(m_windowObject);
        m_resultDialog = findViewById(R.id.main_result_dialog);

        // 测试
        m_apiDebugView = findViewById(R.id.main_api_debug_panel);
        m_serialPortDebugView = findViewById(R.id.main_serial_port_debug_panel);
        m_afterApiDebugView = findViewById(R.id.main_after_api_debug_panel);

        m_cameraHandlerThread = new HandlerThread("_Camera_preview_thread");
        m_cameraHandlerThread.start();
        m_cameraHandler = new Handler(m_cameraHandlerThread.getLooper());

        m_deviceHandlerThread = new HandlerThread("_Network_device_thread");
        m_deviceHandlerThread.start();
        m_deviceHandler = new Handler(m_deviceHandlerThread.getLooper());

        SetupUI();

        m_camera = new CameraFunc(this, m_textureView, m_cameraHandler);
        m_currentCamera = m_camera.CurrentCamera();
        m_camera.SetOnCameraListener(m_onCameraListener);

        m_deviceFunc = new DeviceFunc(this, m_deviceHandler);
        m_deviceFunc.SetOnSerialPortListener(m_deviceFuncListener);

        App.Instance().PushActivity(this);
    }

    private void SetupUI()
    {
        m_cameraMask = (CameraMaskView)findViewById(R.id.main_camera_mask_layer);
        m_textureView = (DynamicTextureView) findViewById(R.id.main_camera_texture);
        m_textureView.SetFileScheme(DynamicTextureView.ID_FILL_SCHEME_WIDTH_PREFER);
        m_textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener(){
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height)
            {
                Logf.d(ID_TAG, "纹理视图(%d, %d)", width, height);
                m_textureViewInitSize = new Size(width, height);
                m_camera.ResizeTextureView(width, height);
                //OpenCamera();
            }

            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height)
            {
                Logf.d(ID_TAG, "纹理视图更新(%d, %d)", width, height);
                m_camera.ResizeTextureView(width, height);
            }

            public void onSurfaceTextureUpdated(SurfaceTexture surface)
            {
            }

            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface)
            {
                return false;
            }
        });

        findViewById(R.id.main_menu_open_screensaver).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(m_screenSaverView.getVisibility() == View.VISIBLE)
                    CloseScreenSaver(true);
                else
                    OpenScreenSaver(true);
            }
        });

        ((TextView)m_apiDebugView.findViewById(R.id.main_title_debug_text)).setText("人脸识别API");
        ((TextView)m_serialPortDebugView.findViewById(R.id.main_title_debug_text)).setText("本地串口IO");
        ((TextView)m_afterApiDebugView.findViewById(R.id.main_title_debug_text)).setText("上报重量API");

        m_resultDialogOpenAnimation = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.result_dialog_open_anim);
        m_resultDialogOpenAnimation.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        m_resultDialog.setVisibility(View.VISIBLE);
                    }
                });
            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {
                animation.end();
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                animation.end();
            }
        });
        m_resultDialogCloseAnimation = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.result_dialog_close_anim);
        m_resultDialogCloseAnimation.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        m_resultDialog.setVisibility(View.INVISIBLE);
                    }
                });
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                animation.end();
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                animation.end();
            }
        });
        m_resultDialogOpenAnimation.setTarget(m_resultDialog);
        m_resultDialogCloseAnimation.setTarget(m_resultDialog);

        m_personView.setAlpha(0.0f);
        m_personView.setVisibility(View.INVISIBLE);
    }

    private void OpenCamera()
    {
        m_camera.Ready();
        m_camera.InitCamera();
    }

    private void CloseCamera()
    {
        m_camera.CloseCamera();
        m_camera.Reset();
    }

    private void HandleCapturePreview(final Bitmap bitmap)
    {
        if(false) // 检测到人脸时关闭屏保 // 需要相机一直预览
        {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    CloseScreenSaver(true);
                }
            });
        }

        if(m_debugMode != 0)
        {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    findViewById(R.id.main_response_debug_view).setVisibility(View.VISIBLE);
                }
            });
        }

        // 新线程执行
        m_deviceHandler.post(new Runnable() {
            @Override
            public void run() {
                // 开始流程
                UserModel user = DoVerifyFace(bitmap);
                long now = System.currentTimeMillis();
                if(user == null) // 人脸识别异常
                {
                    ShowFacePanel(null, "识别错误", Common.TimestampToStr(now));
                    m_cameraMask.SetState(CameraMaskView.ID_STATE_FACE_VERIFY_FAIL);
                    WaitNextTime("识别错误");
                    return;
                }

                if(!user.IsValid()) // 无效人员
                {
                    ShowFacePanel(user.Photo(), "未识别身份", Common.TimestampToStr(now));
                    m_cameraMask.SetState(CameraMaskView.ID_STATE_FACE_VERIFY_FAIL);
                    WaitNextTime("未识别身份");
                    return;
                }
                ShowFacePanel(user.Photo(), user.Username(), Common.TimestampToStr(now));
                m_cameraMask.SetState(CameraMaskView.ID_STATE_FACE_VERIFY_SUCCESS);

                m_cameraMask.SetState(CameraMaskView.ID_STATE_PROCESSING);
                // 继续开门操作
                SerialSessionStruct session = DoOpenDoor();
                if(session == null) // 开门错误
                {
                    m_cameraMask.SetState(CameraMaskView.ID_STATE_PROCESS_FAIL);
                    PlayAlert(SoundAlert.ID_SOUND_ALERT_OPERATION_ERROR);
                    OpenResultDialog(false, "设备异常", true);
                    return;
                }

                if(!session.IsValid()) // 开门失败
                {
                    m_cameraMask.SetState(CameraMaskView.ID_STATE_PROCESS_FAIL);
                    PlayAlert(SoundAlert.ID_SOUND_ALERT_OPERATION_ERROR);
                    if(m_deviceFunc.State() == DeviceFunc.ID_STATE_TIMEOUT)
                        OpenResultDialog(false, "操作超时", true);
                    else
                        OpenResultDialog(false, "开门失败", true);
                    return;
                }
                m_cameraMask.SetState(CameraMaskView.ID_STATE_PROCESS_SUCCESS);

                // 继续上报重量
                String uuid = DoUploadWeight(session);
                if(uuid != null)
                {
                    m_cameraMask.SetState(CameraMaskView.ID_STATE_UPLOAD_SUCCESS);

                    // 存储至本地数据库
                    if(m_recordHistory)
                    {
                        if(m_recordDB == null)
                            m_recordDB = new RecordServices(MainActivity.this);
                        try
                        {
                            RecordModel item = new RecordModel();
                            item.SetName(user.Username());
                            item.SetTime(now);
                            item.SetDevice(session.req.door_id);
                            item.SetWeight(((PutOpenDoorRespStruct)session.resp).weight);
                            item.SetOperation(SerialDataDef.ID_SERIAL_DATA_TYPE_PUT_OPEN_DOOR);
                            item.SetResult(RecordModel.ID_RESULT_SUCCESS);
                            item.SetUuid(uuid);
                            item.SetCreateTime(System.currentTimeMillis());
                            if(m_recordDB.Add(item))
                                Logf.i(ID_TAG, "结果写入数据库成功: " + item.Id());
                            else
                                Logf.e(ID_TAG, "结果写入数据库失败");
                        }
                        catch(Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                    PlayAlert(SoundAlert.ID_SOUND_ALERT_OPERATION_SUCCESS);
                    OpenResultDialog(true, "上报重量成功", true);
                }
                else
                {
                    m_cameraMask.SetState(CameraMaskView.ID_STATE_UPLOAD_FAIL);
                    PlayAlert(SoundAlert.ID_SOUND_ALERT_OPERATION_ERROR);
                    OpenResultDialog(false, "开门失败", true);
                }

                // 流程结束
            }
        });
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        /*ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }*/
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        //mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(!ActivityUtility.IsGrantPermission(this, Manifest.permission.CAMERA))
        {
            if(!ActivityUtility.RequestPermission(this, Manifest.permission.CAMERA))
                OpenPermissionGrantFailDialog();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        m_cameraMask.SetState(CameraMaskView.ID_STATE_READY);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        try
        {
            m_verifyFaceMaxInterval = Integer.parseInt(preferences.getString(Constants.ID_PREFERENCE_OPEN_SCREEN_SAVER_MAX_INTERVAL, "" + Configs.ID_PREFERENCE_DEFAULT_OPEN_SCREEN_SAVER_MAX_INTERVAL));
            m_operateDeviceTimeout = Integer.parseInt(preferences.getString(Constants.ID_PREFERENCE_OPERATE_DEVICE_TIMEOUT, "" + Configs.ID_PREFERENCE_DEFAULT_OPERATE_DEVICE_TIMEOUT));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        m_imageQuality = preferences.getInt(Constants.ID_PREFERENCE_FACE_IMAGE_QUALITY, Configs.ID_PREFERENCE_DEFAULT_FACE_IMAGE_QUALITY);
        m_cropBitmap = preferences.getBoolean(Constants.ID_PREFERENCE_PREVIEW_CAPTURE_CROP, Configs.ID_PREFERENCE_DEFAULT_PREVIEW_CAPTURE_CROP);
        m_recordHistory = preferences.getBoolean(Constants.ID_PREFERENCE_RECORD_HISTORY, Configs.ID_PREFERENCE_DEFAULT_RECORD_HISTORY);
        m_playAlert = preferences.getBoolean(Constants.ID_PREFERENCE_PLAY_VOICE_ALERT, Configs.ID_PREFERENCE_DEFAULT_PLAY_VOICE_ALERT);
        OpenScreenSaver(true);
        CloseResultDialog(false);

        m_camera.Reset();

        m_debugMode = (int)(Configs.Instance().GetConfig(Configs.ID_CONFIG_DEBUG));

        findViewById(R.id.main_response_debug_view).setVisibility(View.GONE);
/*
        if(ActivityUtility.IsGrantPermission(this, Manifest.permission.CAMERA))
        {
            // TODO: Init
            OpenCamera();
        }
*/

       // CloseScreenSaver();
    }

    @Override
    protected void onPause() {
        super.onPause();

        mHideHandler.postDelayed(m_hideFacePanel, 100);
        delayedHide(100);

        CloseCamera();
        OpenScreenSaver(false);
        CloseResultDialog(false);

        CloseSerialPortDriver();

        //StopAlert();
        if(m_soundAlert != null)
        {
            m_soundAlert.Shutdown();
            m_soundAlert = null;
        }

        ((TextView)findViewById(R.id.main_camera_info_text)).setText("");
    }

    // 对于异线程
    private void ShowToast(final String msg, final int delay)
    {
        runOnUiThread(new Runnable(){
            public void run()
            {
                Toast.makeText(MainActivity.this, msg, delay).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        m_camera.ShutdownCamera();
        m_handler = null;
        m_cameraHandler = null;
        m_cameraHandlerThread.quit();
        m_cameraHandlerThread = null;

        m_deviceHandler = null;
        m_deviceHandlerThread.quit();
        m_deviceHandlerThread = null;
        if(m_soundAlert != null)
        {
            m_soundAlert.Shutdown();
            m_soundAlert = null;
        }
        App.Instance().PopActivity();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == ActivityUtility.ID_REQUEST_PERMISSION_RESULT)
        {
            int index = 0; // only camera
            if(grantResults[index] != PackageManager.PERMISSION_GRANTED)
            {
                OpenPermissionGrantFailDialog();
            }
        }
    }

    private void OpenPermissionGrantFailDialog()
    {
        final DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        ActivityUtility.OpenAppSetting(MainActivity.this);
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                    default:
                        App.Instance().Exit(1);
                        break;
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("程序无权限访问摄像头设备!");
        builder.setMessage("请前往系统设置手动授权程序访问摄像头");
        builder.setIcon(R.drawable.icon_profile);
        builder.setCancelable(false);
        builder.setPositiveButton("确定", listener);
        builder.setNegativeButton("退出", listener);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // 验证人脸照片, 返回人员对象
    private UserModel DoVerifyFace(Bitmap bitmap)
    {
        UserModel user = new UserModel();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        if(m_cropBitmap && (m_textureViewInitSize != null && (m_textureViewInitSize.getWidth() > 0 && m_textureViewInitSize.getHeight() > 0)))
        {
            Rect rect = CaleCropSize(m_textureViewInitSize, new Size(bitmap.getWidth(), bitmap.getHeight()));
            Logf.e(ID_TAG, "裁剪大小: x(%d), y(%d), w(%d), h(%d)", rect.left, rect.top, rect.width(), rect.height());
            if(rect.left != 0 || rect.top != 0 || rect.width() != bitmap.getWidth() || rect.height() != bitmap.getHeight())
            {
                Bitmap newBitmap = Bitmap.createBitmap(bitmap, rect.left, rect.top, rect.width(), rect.height(), null, false);
                bitmap.recycle();
                bitmap = newBitmap;
            }
        }
        bitmap.compress(Bitmap.CompressFormat.JPEG, m_imageQuality, out);
        String image = android.util.Base64.encodeToString(out.toByteArray(), android.util.Base64.NO_WRAP);
        try
        {
            out.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        user.SetPhoto(bitmap);

        if(m_debugMode != 0)
        {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                ((TextView)m_apiDebugView.findViewById(R.id.main_response_debug_text)).setText("");
                JsonMap map = new JsonMap();
                map.put("c", DeviceApi.ID_DEVICE_API_COMMAND_VERIFY_FACE);
                map.put("imei", Sys.GetIMEI(MainActivity.this));
                map.put("m", "IMAGE_BASE64_CODE......");
                String json = JSON.Stringify(map);
                ((TextView)m_apiDebugView.findViewById(R.id.main_request_debug_text)).setText(json);
                }
            });
        }

        final DeviceApiResp resp = DeviceApi.VerifyFace(Sys.GetIMEI(MainActivity.this), image);
        if(resp != null)
        {
            if(m_debugMode != 0)
            {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((TextView)m_apiDebugView.findViewById(R.id.main_response_debug_text)).setText(resp.json);
                    }
                });
            }

            if(resp.IsSuccess())
            {
                if(resp.data == null || resp.data instanceof String) // MISMATCHED
                {
                    if(ActivityUtility.BuildOnDebug(this) /*&& false*/) // TODO: 仅开发测试
                    {
                        user.SetUsername("开发者");
                    }
                    Logf.e(ID_TAG, "未识别人员: " + Common.Now());
                }
                else
                {
                    try
                    {
                        JsonMap data = (JsonMap)resp.data;
                        String name = data.<String>GetT("username");
                        user.SetUsername(name);
                        user.SetPhoto(bitmap);
                    }
                    catch (Throwable e)
                    {
                        e.printStackTrace();
                        Logf.e(ID_TAG, "识别失败: " + Common.Now());
                    }
                }
            }
            else
            {
                Logf.e(ID_TAG, "人脸识别服务器错误: " + Common.Now());
            }
        }
        else
        {
            Logf.e(ID_TAG, "请求人脸识别服务器异常: " + Common.Now());
        }

        return user;
    }

    private void WaitNextTime(String message)
    {
        CloseResultDialog(true);

        ShowToast(message, Toast.LENGTH_SHORT);
        m_handler.postDelayed(m_waitNextOperation, ID_HIDE_PERSON_PANEL_DELAY);
    }

    private void SetPreviewImage(Bitmap face)
    {
        Drawable drawable = m_previewImage.getDrawable();
        Bitmap oldBitmap = null;
        if(drawable != null)
        {
            if(drawable instanceof BitmapDrawable)
            {
                oldBitmap = ((BitmapDrawable)drawable).getBitmap();
            }
        }

        if(face != null)
            m_previewImage.setImageBitmap(face);
        else
            m_previewImage.setImageDrawable(new ColorDrawable(Color.BLACK));
        if(oldBitmap != null && !oldBitmap.isRecycled())
        {
            oldBitmap.recycle();
            oldBitmap = null;
        }
    }

    // TODO: 销毁旧的bitmap
    private void ShowFacePanel(final Bitmap face, final String name, final String time)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
            SetPreviewImage(face);
            m_personName.setText(name);
            m_personTime.setText(time);
            m_personView.animate().setDuration(ID_PERSON_VIEW_ANIM_DELAY).alpha(1.0f).withStartAction(new Runnable() {
                @Override
                public void run() {
                    m_personView.setVisibility(View.VISIBLE);
                }
            }).start();
            }
        });
    }

    private void OpenScreenSaver(boolean anim)
    {
        hide();
        if(anim)
        {
            m_screenSaverView.animate().setDuration(ID_SCREEN_SAVER_ANIM_DELAY).alpha(1.0f).withStartAction(m_openScreenSaver).start();
        }
        else
        {
            m_openScreenSaver.run();
        }
    }

    private void CloseScreenSaver(boolean anim)
    {
        if(anim)
        {
            m_screenSaverView.animate().setDuration(ID_SCREEN_SAVER_ANIM_DELAY).alpha(0.0f).withEndAction(m_closeScreenSaver).start();
        }
        else
        {
            m_closeScreenSaver.run();
        }
    }

    private boolean OpenSerialPortDriver() { return false; } // UNUSED: DeviceFunc里自动控制

    private void CloseSerialPortDriver()
    {
        m_deviceFunc.Shutdown(); // TODO: others?
    }

    private void OpenWarningDialog(String message)
    {
        Logf.e(ID_TAG, message);
        try
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("错误");
            builder.setMessage(message);
            builder.setIcon(R.drawable.icon_profile);
            builder.setPositiveButton("确定", null);
            AlertDialog dialog = builder.create();
            dialog.show();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private SerialSessionStruct DoOpenDoor()
    {
        boolean suc = m_deviceFunc.OpenDoor(m_operateDeviceTimeout); // 会阻塞线程
        if(!suc)
        {
            // TODO: 处理???
            Logf.e(ID_TAG, "开门失败");
            if(m_deviceFunc.State() == DeviceFunc.ID_STATE_TIMEOUT)
                Logf.e(ID_TAG, "开门失败原因: 超时");
        }
        return m_deviceFunc.LastSession();
    }

    private void OpenMenu()
    {
        Intent intent;

        intent = new Intent(MainActivity.this, ProfileActivity.class);
        MainActivity.this.startActivity(intent);
    }

    public static Rect CaleCropSize(Size destSize, Size bitmapSize)
    {
        /*if(destSize.getWidth() >= bitmapSize.getWidth() && destSize.getHeight() >= bitmapSize.getHeight())
            return new Rect(0, 0, bitmapSize.getWidth(), bitmapSize.getHeight());*/

        float tag_w = (float)destSize.getWidth();
        float tag_h = (float)destSize.getHeight();
        float src_w = (float)bitmapSize.getWidth();
        float src_h = (float)bitmapSize.getHeight();

        float a = src_w / src_h;
        float b = tag_w / tag_h;
        float f = a > b ? (tag_h / src_h) : (tag_w / src_w);

        src_w *= f;
        src_h *= f;
        float w = Math.min(tag_w, src_w);
        float h = Math.min(tag_h, src_h);
        float x = Math.max(0, src_w / 2 - tag_w / 2);
        float y = Math.max(0, src_h / 2 - tag_h / 2);

        x /= f;
        y /= f;
        w /= f;
        h /= f;

        return new Rect(Math.round(x), Math.round(y), Math.round(x + w), Math.round(y + h));
    }

    private void StartPreview()
    {
        // TODO: 开始预览 or 启动相机
        if(m_cameraUsagePlan == Constants.ID_CONFIG_CAMERA_USAGE_PLAN_CLOSE_WHEN_UNUSED_AND_REOPEN_WHEN_NEED)
        {
            OpenCamera();
            m_camera.WaitPreview(-1);
        }
        else
        {
            if(!m_camera.CameraSessionAvailable())
            {
                OpenCamera();
                m_camera.WaitPreview(-1);
            }
            else
                m_camera.StartPreview();
        }
    }

    private void StopPreview()
    {
        // TODO: 结束预览 or 停止相机
        if(m_cameraUsagePlan == Constants.ID_CONFIG_CAMERA_USAGE_PLAN_CLOSE_WHEN_UNUSED_AND_REOPEN_WHEN_NEED)
            CloseCamera();
        else
            m_camera.StopPreview();
    }

    private String DoUploadWeight(SerialSessionStruct session)
    {
        if(!session.IsValidSession())
        {
            Logf.e(ID_TAG, "无效对话");
            return null;
        }

        /// TODO: 是否需要检测请求与相应的token???
        if(!session.IsPair())
        {
            Logf.e(ID_TAG, "返回数据token无效");
            return null;
        }

        final PutOpenDoorReqStruct req = (PutOpenDoorReqStruct)session.req;
        final PutOpenDoorRespStruct resp = (PutOpenDoorRespStruct)session.resp;

        if(!resp.IsValid())
        {
            Logf.e(ID_TAG, "返回数据缺失必要数据" + resp.toString());
            return null;
        }

        if(m_debugMode != 0)
        {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                ((TextView)m_afterApiDebugView.findViewById(R.id.main_response_debug_text)).setText("");
                JsonMap map = new JsonMap();
                map.put("c", DeviceApi.ID_DEVICE_API_COMMAND_UPLOAD_FACE);
                map.put("imei", Sys.GetIMEI(MainActivity.this));
                map.put("m", resp.weight);
                String json = JSON.Stringify(map);
                ((TextView)m_afterApiDebugView.findViewById(R.id.main_request_debug_text)).setText(json);
                }
            });
        }

        final DeviceApiResp apiResp = DeviceApi.UploadWeight(Sys.GetIMEI(MainActivity.this), resp.weight);
        if(apiResp != null)
        {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(m_debugMode != 0)
                    {
                        ((TextView)m_afterApiDebugView.findViewById(R.id.main_response_debug_text)).setText(apiResp.json);
                    }
                }
            });
            try
            {
                String uuid = (String)((JsonMap)apiResp.data).Get("uuid");
                return uuid;
            }
            catch (Exception e)
            {
                e.printStackTrace();
                return apiResp.IsSuccess() ? "" : null;
            }
            //return apiResp.IsSuccess();
        }
        else
        {
            Logf.e(ID_TAG, "上报重量服务器异常!", Toast.LENGTH_LONG);
            return null;
        }
    }

    private void OpenResultDialog(final boolean suc, final String message, boolean anim)
    {
        CloseResultDialog(false);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((ImageView)m_resultDialog.findViewById(R.id.main_result_icon)).setImageResource(R.mipmap.ic_launcher);
                TextView textView = (TextView)m_resultDialog.findViewById(R.id.main_result_message);
                textView.setText(message);
                textView.setTextColor(suc ? Color.GREEN  : Color.RED);
            }
        });
        if(anim)
            m_resultDialogOpenAnimation.start();
        else
            m_resultDialogOpenAnimation.end();
        m_handler.postDelayed(m_finishOperation, ID_OPERATION_FINISHED_INTERVAL);
    }

    private void CloseResultDialog(boolean anim)
    {
        if(m_resultDialog.getVisibility() != View.VISIBLE)
            return;
        if(anim)
        {
            m_resultDialogCloseAnimation.start();
        }
        else
        {
            m_resultDialogCloseAnimation.end();
        }
    }

    private void PlayAlert(String name)
    {
        if(!m_playAlert)
            return;
        if(m_soundAlert == null)
            m_soundAlert = new SoundAlert(this);
        m_soundAlert.Play(name);
    }

    private void StopAlert()
    {
        if(m_soundAlert == null)
            return;
        m_soundAlert.Reset();
    }
}
