package com.youtushuju.lingdongapp;

import android.Manifest;
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
import android.graphics.SurfaceTexture;
import android.graphics.drawable.ColorDrawable;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.Face;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.preference.PreferenceManager;
import android.provider.Settings;
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
import com.youtushuju.lingdongapp.common.Logf;
import com.youtushuju.lingdongapp.common.Sys;
import com.youtushuju.lingdongapp.device.LingDongApi;
import com.youtushuju.lingdongapp.common.Common;
import com.youtushuju.lingdongapp.common.Configs;
import com.youtushuju.lingdongapp.common.Constants;
import com.youtushuju.lingdongapp.gui.ActivityUtility;
import com.youtushuju.lingdongapp.gui.App;
import com.youtushuju.lingdongapp.gui.CameraFunc;
import com.youtushuju.lingdongapp.gui.DeviceFunc;
import com.youtushuju.lingdongapp.gui.DynamicTextureView;
import com.youtushuju.lingdongapp.gui.FaceRectView;
import com.youtushuju.lingdongapp.gui.ScreenSaverView;
import com.youtushuju.lingdongapp.json.JSON;
import com.youtushuju.lingdongapp.json.JsonMap;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

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
    private ImageView m_previewImage;
    private TextView m_personName;
    private TextView m_personTime;
    private View m_personView;
    private CameraFunc.CameraInfoModel m_currentCamera = null;
    private FaceRectView m_faceRectView = null;
    private ScreenSaverView m_webView = null;
    private View m_screenSaverView = null;
    private int m_operateDeviceTimeout = Configs.ID_PREFERENCE_DEFAULT_OPERATE_DEVICE_TIMEOUT; // 操作设备超时

    // 设备交互
    private LingDongApi m_lingdongApi = null;
    private DeviceFunc m_deviceFunc = null;

    // 测试
    private View m_apiDebugView = null;
    private View m_serialPortDebugView = null;
    private long m_lastVerifyTime = 0L; // 上次检测到人脸的时间
    private int m_verifyFaceMaxInterval = Configs.ID_PREFERENCE_DEFAULT_OPEN_SCREEN_SAVER_MAX_INTERVAL; // 检测不到人脸则进入屏保的间隔

    // 相机线程
    private HandlerThread m_cameraHandlerThread = null;
    private Handler m_cameraHandler = null; // new thread
    // 网络/设备线程
    private HandlerThread m_deviceHandlerThread = null;
    private Handler m_deviceHandler = null; // new thread

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
            m_personView.setVisibility(View.GONE);
            m_previewImage.setImageDrawable(new ColorDrawable(Color.BLACK));
            m_personName.setText("");
            m_personTime.setText("");
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

    private CameraFunc.OnCameraListener m_onCameraListener = new CameraFunc.OnCameraListener() {
        @Override
        public void OnCameraOpenResult(boolean success) {
            if(success)
            {
                ((TextView)findViewById(R.id.main_camera_info_text)).setText(m_currentCamera.toString());
                Logf.i(ID_TAG, "打开相机成功");
            }
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

        @Override
        public void OnPreviewCapture(TotalCaptureResult result, Face faces[], long time, boolean always, int face_mode) {
            if(m_deviceFunc.IsRunning())
                return; // 正在操作设备

            // TODO: 测试
            if(!Common.ArrayIsEmpty(faces))
            {
                Logf.e(ID_TAG, "检测到人脸: " + faces.length);
                 ShowToast("检测到人脸: " + faces.length, Toast.LENGTH_LONG);
            }

            // 仅有人脸时
            boolean available = always || (face_mode == CaptureRequest.STATISTICS_FACE_DETECT_MODE_OFF || !Common.ArrayIsEmpty(faces));

            if(available)
            {
                if(m_deviceFunc.IsCanStart())
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
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                            m_camera.StopPreview();
                            m_lastVerifyTime = 0;
                            OpenScreenSaver();

                            findViewById(R.id.main_response_debug_view).setVisibility(View.GONE);
                            }
                        });
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
    };

    private Handler m_handler = new Handler(); // main thread
    private ScreenSaverView.WindowObject m_windowObject = new ScreenSaverView.WindowObject(this, m_handler) {
        @JavascriptInterface
        public void ToFace(final String name)
        {
            m_handler.post(new Runnable(){
                @Override
                public void run() {
                Logf.i(ID_TAG, "设置垃圾类别: " + name);
                if("waste".equals(name))
                    m_deviceFunc.SetDoorId(DeviceApi.ID_KITCHEN_WASTE_DOOR_ID);
                else if("other".equals(name))
                    m_deviceFunc.SetDoorId(DeviceApi.ID_OTHER_WASTE_DOOR_ID);
                else
                {
                    OpenWarningDialog("门类型无效!");
                    return;
                }

                m_camera.StartPreview();
                m_lastVerifyTime = System.currentTimeMillis();
                CloseScreenSaver();

                findViewById(R.id.main_response_debug_view).setVisibility(View.GONE);
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
            ShowToast(msg, Toast.LENGTH_SHORT);
        }

        @Override
        public void OnError(final String error) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    OpenWarningDialog(error);
                }
            });
        }

        @Override
        public void OnRecv(final String recvData, String sendData) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                int debugMode = (int)(Configs.Instance().GetConfig(Configs.ID_CONFIG_DEBUG));
                if(debugMode != 0)
                {
                    findViewById(R.id.main_response_debug_view).setVisibility(View.VISIBLE);
                    ((TextView)m_serialPortDebugView.findViewById(R.id.main_response_debug_text)).setText(recvData);
                }
                }
            });
            mHideHandler.postDelayed(m_hideFacePanel, ID_HIDE_PERSON_PANEL_DELAY);
            //m_deviceFunc.Reset();
        }

        @Override
        public void OnSend(final String data, boolean success) {
            int debugMode = (int)(Configs.Instance().GetConfig(Configs.ID_CONFIG_DEBUG));
            if(debugMode != 0)
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
                ShowToast("串口数据发送成功", Toast.LENGTH_SHORT);
            }
        }

        @Override
        public void OnStateChanged(int state) {
            Logf.e(ID_TAG, "串口读写状态变更: " + state);
            m_lastVerifyTime = 0L; //System.currentTimeMillis();
        }

        @Override
        public void OnTimeout(String sendData, final int timeout) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    OpenWarningDialog("读取数据超时: " + timeout);
                }
            });
            //m_deviceFunc.Reset();
            mHideHandler.postDelayed(m_hideFacePanel, ID_HIDE_PERSON_PANEL_DELAY);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);

        ImageButton button;

        setContentView(R.layout.main);

        m_lingdongApi = Configs.Instance().GetLingDongApi(this);

        mVisible = true;
        mControlsView = findViewById(R.id.main_layer);
        mContentView = findViewById(R.id.main_content);

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

        // 测试
        m_apiDebugView = findViewById(R.id.main_api_debug_panel);
        m_serialPortDebugView = findViewById(R.id.main_serial_port_debug_panel);

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
        m_personView.setVisibility(View.GONE);

        m_textureView = (DynamicTextureView) findViewById(R.id.main_camera_texture);
        m_textureView.SetFileScheme(DynamicTextureView.ID_FILL_SCHEME_WIDTH_PREFER);
        m_textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener(){
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height)
            {
                Logf.d(ID_TAG, "纹理视图(%d, %d)", width, height);
                m_camera.ResizeTextureView(width, height);
                OpenCamera();
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

        findViewById(R.id.main_screensaver_refresh).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m_webView.reload();
            }
        });
        findViewById(R.id.main_menu_open_screensaver).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            OpenScreenSaver();
            }
        });

        ((TextView)m_apiDebugView.findViewById(R.id.main_title_debug_text)).setText("远程API");
        ((TextView)m_serialPortDebugView.findViewById(R.id.main_title_debug_text)).setText("本地串口");
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
        //if(true) return;
        /*m_cameraHandler.post(new Runnable(){
            public void run()
            {
                VerifyFace(bitmap);
            }
        });*/
        if(false) // 检测到人脸时关闭屏保
        {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    CloseScreenSaver();
                }
            });
        }

        // 新线程执行
        m_deviceHandler.post(new Runnable() {
            @Override
            public void run() {
                VerifyFace(bitmap);
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
        OpenScreenSaver();

        m_camera.Reset();

        findViewById(R.id.main_response_debug_view).setVisibility(View.GONE);

        if(ActivityUtility.IsGrantPermission(this, Manifest.permission.CAMERA))
        {
            // TODO: Init
            OpenCamera();
        }

       // CloseScreenSaver();
        ((TextView)findViewById(R.id.main_camera_info_text)).setText(m_currentCamera.toString());
    }

    @Override
    protected void onPause() {
        super.onPause();

        mHideHandler.postDelayed(m_hideFacePanel, 100);
        delayedHide(100);

        CloseCamera();
        CloseScreenSaver();

        CloseSerialPortDriver();

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

    private String VerifyFace(final Bitmap bitmap)
    {
        final int debugMode = (int)(Configs.Instance().GetConfig(Configs.ID_CONFIG_DEBUG));
        int quality = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getInt(Constants.ID_PREFERENCE_FACE_IMAGE_QUALITY, Configs.ID_PREFERENCE_DEFAULT_FACE_IMAGE_QUALITY);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out);
        String image = android.util.Base64.encodeToString(out.toByteArray(), android.util.Base64.NO_WRAP);

        if(debugMode != 0)
        {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                findViewById(R.id.main_response_debug_view).setVisibility(View.VISIBLE);
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

        final DeviceApiResp resp = DeviceApi.VerifyFace(Sys.GetIMEI(MainActivity.this), image); // TODO: 当前后台线程
        if(resp != null)
        {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(debugMode != 0)
                    {
                        findViewById(R.id.main_response_debug_view).setVisibility(View.VISIBLE);
                        ((TextView)m_apiDebugView.findViewById(R.id.main_response_debug_text)).setText(resp.json);
                    }
                }
            });
            if(resp.IsSuccess())
            {
                if(resp.data == null || resp.data instanceof String) // MISMATCHED
                {
                    ShowFacePanel(bitmap/*null*/, "未识别身份", Common.Now());
                    if(true) // TODO: 仅开发测试
                    {
                        m_deviceFunc.SetDoorId(DeviceApi.ID_KITCHEN_WASTE_DOOR_ID);
                        OpenDoor();
                    }
                }
                else
                {
                    try
                    {
                        JsonMap data = (JsonMap)resp.data;
                        String name = data.<String>GetT("username");
                        ShowFacePanel(bitmap, name, Common.Now());
                        OpenDoor();
                    }
                    catch (Throwable e)
                    {
                        e.printStackTrace();
                        ShowFacePanel(bitmap/*null*/, "识别失败", Common.Now());
                    }
                }
            }
            else
            {
                ShowToast("人脸识别服务器错误!", Toast.LENGTH_LONG);
            }
        }
        else
        {
            ShowToast("请求人脸识别服务器异常!", Toast.LENGTH_LONG);
        }

        return null;
    }

    private void ShowFacePanel(final Bitmap face, final String name, final String time)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
            m_personView.setVisibility(View.VISIBLE);
            if(face != null)
                m_previewImage.setImageBitmap(face);
            else
                m_previewImage.setImageDrawable(new ColorDrawable(Color.BLACK));
            // new String[]{"Ada", "徐天龙", "Leon", "Jill", "张三", "李四", "王五", "赵二"}[Common.Rand(0, 7)]
            m_personName.setText(name);
            m_personTime.setText(time);
            }
        });
    }

    private void OpenScreenSaver()
    {
        hide();
        m_screenSaverView.setVisibility(View.VISIBLE);
        m_webView.Load();
    }

    private void CloseScreenSaver()
    {
        m_screenSaverView.setVisibility(View.GONE);
    }

    private boolean OpenSerialPortDriver() { return false; } // UNUSED: DeviceFunc里自动控制

    private void CloseSerialPortDriver()
    {
        m_deviceFunc.Shutdown(); // TODO
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

    private boolean OpenDoor()
    {
        boolean ok = m_deviceFunc.OpenDoor(m_operateDeviceTimeout); // 会阻塞线程
        if(!ok)
        {
            return false;
        }
        return true;
    }

    private void OpenMenu()
    {
        Intent intent;

        intent = new Intent(MainActivity.this, ProfileActivity.class);
        MainActivity.this.startActivity(intent);
    }
}
