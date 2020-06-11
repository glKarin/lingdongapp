package com.youtushuju.lingdongapp;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
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
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Size;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.youtushuju.lingdongapp.api.DeviceApi;
import com.youtushuju.lingdongapp.api.DeviceApiDef;
import com.youtushuju.lingdongapp.api.DeviceApiResp;
import com.youtushuju.lingdongapp.api.UserModel;
import com.youtushuju.lingdongapp.common.Logf;
import com.youtushuju.lingdongapp.common.Sys;
import com.youtushuju.lingdongapp.database.RecordModel;
import com.youtushuju.lingdongapp.database.RecordServices;
import com.youtushuju.lingdongapp.device.GetOpenDoorReqStruct;
import com.youtushuju.lingdongapp.device.GetOpenDoorRespStruct;
import com.youtushuju.lingdongapp.device.HeartbeatRespStruct;
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
import com.youtushuju.lingdongapp.gui.MainMaintenanceDialogView;
import com.youtushuju.lingdongapp.gui.MainQRCodeView;
import com.youtushuju.lingdongapp.gui.MainScreenSaverView;
import com.youtushuju.lingdongapp.gui.MainScreenSaverView_native;
import com.youtushuju.lingdongapp.gui.MainScreenSaverView_webview;
import com.youtushuju.lingdongapp.gui.SerialPortDeviceDriver;
import com.youtushuju.lingdongapp.gui.DynamicTextureView;
import com.youtushuju.lingdongapp.gui.FaceRectView;
import com.youtushuju.lingdongapp.gui.OperationIntent;
import com.youtushuju.lingdongapp.gui.ScreenSaverView;
import com.youtushuju.lingdongapp.gui.SoundAlert;
import com.youtushuju.lingdongapp.gui.SoundAlert_mediaplayer;
import com.youtushuju.lingdongapp.gui.StatusMachine;
import com.youtushuju.lingdongapp.json.JSON;
import com.youtushuju.lingdongapp.json.JsonMap;

import java.io.ByteArrayOutputStream;
import java.util.Timer;
import java.util.TimerTask;

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
    private static final int ID_OPERATION_FINISHED_INTERVAL = 5000;
    private static final int ID_PERSON_VIEW_ANIM_DELAY = 500;
    private static final int ID_SCREEN_SAVER_ANIM_DELAY = 500;
    private static final int ID_MAINTENANCE_WAITING_INTERVAL = 10000; // 清维人员对话框等待时间

    // 界面状态枚举
    private static final int ENUM_STATE_READY = 0;
    private static final int ENUM_STATE_SCREENSAVER = 1; // 屏保状态
    private static final int ENUM_STATE_PREVIEW = 2; // 预览状态
    private static final int ENUM_STATE_SCANNER = 3; // 扫码状态

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;

    private final Handler mHideHandler = new Handler();
    private View mContentView; // 主View, 人脸相关
    private View mControlsView; // 辅助View, 菜单按钮等
    private boolean mVisible;

    private DynamicTextureView m_textureView = null; // 相机预览可调整宽高的TextureView
    private CameraMaskView m_cameraMask = null; // 人脸背景框
    private ImageView m_previewImage; // 人脸结果-照片
    private TextView m_personName; // 人脸结果-姓名
    private TextView m_personTime; // 人脸结果-时间
    private TextView m_personRole; // 人脸结果-角色
    private View m_personView; // 人脸信息结果
    private CameraFunc.CameraInfoModel m_currentCamera = null; // 当前摄像机信息
    private MainScreenSaverView m_screenSaverView = null; // 屏保View
    private int m_operateDeviceTimeout = Configs.ID_PREFERENCE_DEFAULT_OPERATE_DEVICE_TIMEOUT; // 操作设备超时
    private boolean m_cropBitmap = true; // 是否裁剪图像到屏幕比例
    private int m_cameraUsagePlan = Configs.ID_PREFERENCE_DEFAULT_CAMERA_USAGE_PLAN; // 相机使用计划
    private Size m_textureViewInitSize = null; // TextureView初始宽高
    private int m_imageQuality = Configs.ID_PREFERENCE_DEFAULT_FACE_IMAGE_QUALITY; // 图像质量
    private View m_resultDialog = null; // 结果对话框
    private Handler m_handler = new Handler(Looper.getMainLooper()); // main thread UI线程处理队列
    private int m_debugMode = 0; // 调试模式
    private AnimatorSet m_resultDialogOpenAnimation = null; // 结果对话框打开动画
    private AnimatorSet m_resultDialogCloseAnimation = null; // 结果对话框关闭动画
    private RecordServices m_recordDB = null; // 记录数据库
    private boolean m_recordHistory = Configs.ID_PREFERENCE_DEFAULT_RECORD_HISTORY; // 是否储存本地记录
    private boolean m_allowExit = Configs.ID_PREFERENCE_DEFAULT_ALLOW_EXIT; // 是否允许退出
    private boolean m_playAlert = Configs.ID_PREFERENCE_DEFAULT_PLAY_VOICE_ALERT; // 是否播放语音
    private SoundAlert m_soundAlert = null; // 语音播放
    private int m_state = ENUM_STATE_READY; // 界面状态
    private OperationIntent m_optIntent = new OperationIntent(); // 刷脸意图
    private int m_verifyFaceMaxInterval = Configs.ID_PREFERENCE_DEFAULT_OPEN_SCREEN_SAVER_MAX_INTERVAL; // 检测不到人脸则进入屏保的间隔
    private MainMaintenanceDialogView m_maintenanceDialog = new MainMaintenanceDialogView(this, m_handler); // 清维门对话框

    // 设备交互
    private LingDongApi m_lingdongApi = null; // 灵动API
    private SerialPortDeviceDriver m_deviceFunc = null; // 串口功能封装
    private CameraFunc m_camera = null; // 相机功能封装

    // 测试
    private View m_apiDebugView = null;
    private View m_serialPortDebugView = null;
    private View m_afterApiDebugView = null;
    private long m_lastVerifyTime = 0L; // 上次检测到人脸的时间

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
            SetState(ENUM_STATE_SCREENSAVER);
            StopPreview();
            m_optIntent.Reset();
            //CloseSerialPortDriver();
            findViewById(R.id.main_response_debug_view).setVisibility(View.GONE);
            CloseMaintenanceDialog(false);
            CloseResultDialog(true);

            m_lastVerifyTime = 0L;
            OpenScreenSaver(true);
            m_hideFacePanel.run();

            ((TextView)m_afterApiDebugView.findViewById(R.id.main_request_debug_text)).setText("");
            ((TextView)m_afterApiDebugView.findViewById(R.id.main_response_debug_text)).setText("");
            ((TextView)m_apiDebugView.findViewById(R.id.main_request_debug_text)).setText("");
            ((TextView)m_apiDebugView.findViewById(R.id.main_response_debug_text)).setText("");
            ((TextView)m_serialPortDebugView.findViewById(R.id.main_request_debug_text)).setText("");
            ((TextView)m_serialPortDebugView.findViewById(R.id.main_response_debug_text)).setText("");
            m_cameraMask.SetState(CameraMaskView.ID_STATE_READY);
            PlayBGM();
        }
    };
    // 流程中有失败时执行: 准备下次刷脸
    private final Runnable m_waitNextOperation = new Runnable() {
        @Override
        public void run() {
            findViewById(R.id.main_response_debug_view).setVisibility(View.GONE);
            CloseMaintenanceDialog(false);
            CloseResultDialog(true);

            m_lastVerifyTime = 0L;
            //m_deviceFunc.Reset();
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

    // 全部流程结束执行: 打开屏保, 停止预览, 重置状态
    private final Runnable m_maintenanceOperation = new Runnable() {
        @Override
        public void run() {
            if(m_optIntent.Type() == OperationIntent.ENUM_FACE_INTENT_OPEN_MAINTENANCE_DOOR)
                return; // 已经选择了门
            m_finishOperation.run();
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
                    m_personRole.setText("");
                }
            }).start();
            //mHideHandler.removeCallbacks(m_hideFacePanel); // TODO: ???
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
                //m_deviceFunc.Reset();
                Bitmap bitmap = m_textureView.getBitmap();
                m_lastVerifyTime = time;
                if(!HandleCapturePreview(bitmap))
                {
                    bitmap.recycle();
                    bitmap = null;
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

    private class OptOpenDoorRunnable implements Runnable {
        private Bitmap bitmap;
        public OptOpenDoorRunnable(Bitmap b)
        {
            bitmap = b;
        }
        @Override
        public void run() {
            // 开始流程
            UserModel user = DoVerifyFace(bitmap);
            long now = System.currentTimeMillis();
            if(user == null) // 人脸识别异常
            {
                ShowFacePanel(null, "识别错误", Common.TimestampToStr(now), null);
                m_cameraMask.SetState(CameraMaskView.ID_STATE_FACE_VERIFY_FAIL);
                WaitNextTime("识别错误");
                return;
            }

            if(!user.IsValid()) // 无效人员
            {
                ShowFacePanel(user.Photo(), "未识别身份", Common.TimestampToStr(now), null);
                m_cameraMask.SetState(CameraMaskView.ID_STATE_FACE_VERIFY_FAIL);
                WaitNextTime("未识别身份");
                return;
            }
            ShowFacePanel(user.Photo(), user.Username(), Common.TimestampToStr(now), user);
            m_cameraMask.SetState(CameraMaskView.ID_STATE_FACE_VERIFY_SUCCESS);

            m_cameraMask.SetState(CameraMaskView.ID_STATE_PROCESSING);
            PlayAlert(SoundAlert.CONST_SOUND_ALERT_NOTIFICATION_DOOR_OPENING); // TODO: 提前播放语音
            // 提前定时提醒
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    PlayAlert(SoundAlert.CONST_SOUND_ALERT_NOTIFICATION_DOOR_CLOSING);
                }
            }, Math.min(6000, (int)((float)m_operateDeviceTimeout * 0.6)));
            // 继续开门操作
            SerialPortDeviceDriver.IOResult result = DoOpenDoor();
            try
            {
                timer.purge();
                timer.cancel();
                timer = null;
            }
            catch (Exception e) // TODO: forbid canceled exception
            {
                e.printStackTrace();
            }
            finally {
                timer = null;
            }
            if(result == null) // 开门错误
            {
                m_cameraMask.SetState(CameraMaskView.ID_STATE_PROCESS_FAIL);
                OpenResultDialog(false, "设备异常", SoundAlert.CONST_SOUND_ALERT_ERROR_OPEN_DOOR_FAILED,true);
                return;
            }
            SerialSessionStruct session = result.session;
            if(!session.IsValid()) // 开门失败
            {
                m_cameraMask.SetState(CameraMaskView.ID_STATE_PROCESS_FAIL);
                if(result.res == SerialPortDeviceDriver.ENUM_RESULT_TIMEOUT)
                    OpenResultDialog(false, "操作超时", SoundAlert.CONST_SOUND_ALERT_ERROR_OPEN_DOOR_FAILED, true);
                else
                    OpenResultDialog(false, "开门失败", SoundAlert.CONST_SOUND_ALERT_ERROR_OPEN_DOOR_FAILED, true);
                return;
            }
            if(!session.IsValidSession()
                    || !session.IsPair() // TODO: 是否需要检测请求与相应的token???
            )
            {
                m_cameraMask.SetState(CameraMaskView.ID_STATE_PROCESS_FAIL);
                if(!session.IsPair())
                    Logf.e(ID_TAG, "返回数据无效");
                else
                    Logf.e(ID_TAG, "无效对话");
                OpenResultDialog(false, "开门失败", SoundAlert.CONST_SOUND_ALERT_ERROR_OPEN_DOOR_FAILED, true);
                return;
            }
            PutOpenDoorReqStruct req = (PutOpenDoorReqStruct)session.req;
            PutOpenDoorRespStruct resp = (PutOpenDoorRespStruct)session.resp;
            StatusMachine.Instance().SetDoorStatus(req.door_id, resp.res);
            if(!resp.IsSuccess())
            {
                m_cameraMask.SetState(CameraMaskView.ID_STATE_PROCESS_FAIL);
                Logf.e(ID_TAG, "返回开门数据返回失败结果: " + resp.res);
                if(PutOpenDoorRespStruct.CONST_RES_FULL.equals(resp.res))
                {
                    if(DeviceApiDef.ID_KITCHEN_WASTE_DOOR_ID.equals(req.door_id))
                    {
                        OpenResultDialog(false, "满载", SoundAlert.CONST_SOUND_ALERT_WARNING_KITCHEN_WASTE_FULL, true);
                        return;
                    }
                    else if(DeviceApiDef.ID_KITCHEN_WASTE_DOOR_ID.equals(req.door_id))
                    {
                        OpenResultDialog(false, "满载", SoundAlert.CONST_SOUND_ALERT_WARNING_RECYCLE_WASTE_FULL, true);
                        return;
                    }
                }
                OpenResultDialog(false, "开门失败", SoundAlert.CONST_SOUND_ALERT_ERROR_OPEN_DOOR_FAILED, true);
                return;
            }

            m_cameraMask.SetState(CameraMaskView.ID_STATE_PROCESS_SUCCESS);

            // 继续上报重量
            String uuid = DoUploadWeight(session, user);
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
                        item.SetDevice(req.door_id);
                        item.SetWeight(resp.weightOld + "," + resp.weightNew + "," + resp.weightAll);
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
                OpenResultDialog(true, "上报重量成功", SoundAlert.CONST_SOUND_ALERT_NOTIFICATION_DROP_FINISHED, true);
            }
            else
            {
                m_cameraMask.SetState(CameraMaskView.ID_STATE_UPLOAD_FAIL);
                OpenResultDialog(false, "开门失败", SoundAlert.CONST_SOUND_ALERT_ERROR_OPEN_DOOR_FAILED, true);
            }

            // 流程结束
        }
    }

    private class OptOpenMenuRunnable implements Runnable {
        private Bitmap bitmap;
        public OptOpenMenuRunnable(Bitmap b)
        {
            bitmap = b;
        }
        @Override
        public void run() {
            // 开始流程
            UserModel user = DoVerifyFace(bitmap);
            long now = System.currentTimeMillis();
            if(user == null) // 人脸识别异常
            {
                ShowFacePanel(null, "识别错误", Common.TimestampToStr(now), null);
                m_cameraMask.SetState(CameraMaskView.ID_STATE_FACE_VERIFY_FAIL);
                OpenResultDialog(false, "识别错误", SoundAlert.CONST_SOUND_ALERT_ERROR_FACE_NOT_IDENTIFIED, true);
                return;
            }

            if(!user.IsValid()) // 无效人员
            {
                ShowFacePanel(user.Photo(), "未识别身份", Common.TimestampToStr(now), null);
                m_cameraMask.SetState(CameraMaskView.ID_STATE_FACE_VERIFY_FAIL);
                OpenResultDialog(false, "未识别身份", SoundAlert.CONST_SOUND_ALERT_ERROR_FACE_NOT_IDENTIFIED, true);
                return;
            }

            if(!user.IsAdministrator() && !user.IsMaintenance()) // 不是管理员/维护人员
            {
                ShowFacePanel(user.Photo(), "用户无权操作", Common.TimestampToStr(now), null);
                m_cameraMask.SetState(CameraMaskView.ID_STATE_FACE_VERIFY_FAIL);
                OpenResultDialog(false, "用户无权操作", SoundAlert.CONST_SOUND_ALERT_ERROR_FACE_NOT_IDENTIFIED, true);
                return;
            }

            ShowFacePanel(user.Photo(), user.Username(), Common.TimestampToStr(now), user);
            m_cameraMask.SetState(CameraMaskView.ID_STATE_FACE_VERIFY_SUCCESS);

            if(user.IsAdministrator() && !user.IsMaintenance()) // 只是管理员则直接打开菜单
            {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        MainActivity.this.OpenMenu();
                    }
                });
            }
            else/* if(user.IsMaintenance())*/ // 打开清维对话框
            {
                OpenMaintenanceDialog(user, true);
            }
            // 流程结束
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        ActivityUtility.HideNavBar(this);

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
        m_personRole = (TextView) findViewById(R.id.main_person_role);
        m_personView = findViewById(R.id.main_person_view);

        m_screenSaverView = new
                //MainScreenSaverView_webview /*屏保实现: webview*/
                MainScreenSaverView_native /*屏保实现: 原生实现*/
                (this, m_handler);
        m_screenSaverView.Setup();

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

        m_deviceFunc = SerialPortDeviceDriver.Instance();
        //m_deviceFunc.SetOnSerialPortListener(null);

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
                if(m_screenSaverView.IsVisible())
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

    // 打开相机
    private void OpenCamera()
    {
        m_camera.Ready();
        m_camera.InitCamera();
    }

    // 关闭相机
    private void CloseCamera()
    {
        m_camera.CloseCamera();
        m_camera.Reset();
    }

    // 处理人脸图像数据
    private boolean HandleCapturePreview(final Bitmap bitmap) {
        if (false) // 检测到人脸时关闭屏保 // 需要相机一直预览
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

        Runnable runnable = null;

        switch (m_optIntent.Type())
        {
            case OperationIntent.ENUM_FACE_INTENT_OPEN_DOOR:
                runnable = new OptOpenDoorRunnable(bitmap);
                break;
            case OperationIntent.ENUM_FACE_INTENT_OPEN_MENU:
                runnable = new OptOpenMenuRunnable(bitmap);
                break;
            default:
                break;
        }
        // 新线程执行
        if(runnable != null)
        {
            m_deviceHandler.post(runnable);
            return true;
        }
        return false;
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
        if(m_debugMode == 0)
            return;
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
        mControlsView.setVisibility(View.INVISIBLE);
        //mControlsView.setVisibility(View.GONE); // ori
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

        // 开启心跳服务
        HeartbeatService.Start(this);
        // 开启BGM服务
        BackgroundService.Start(this);

        StatusMachine.Instance().boot_timestamp = System.currentTimeMillis();
    }

    @Override
    protected void onResume() {
        super.onResume();

        SetState(ENUM_STATE_SCREENSAVER);
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
        m_allowExit = preferences.getBoolean(Constants.ID_PREFERENCE_ALLOW_EXIT, Configs.ID_PREFERENCE_DEFAULT_ALLOW_EXIT);
        m_cameraMask.SetDrawBox(preferences.getBoolean(Constants.ID_PREFERENCE_CAMERA_DRAW_BOX, Configs.ID_PREFERENCE_DEFAULT_CAMERA_DRAW_BOX));
        OpenScreenSaver(true);
        CloseMaintenanceDialog(false);
        CloseResultDialog(false);

        m_camera.Reset();

        m_debugMode = (int)(Configs.Instance().GetConfig(Configs.ID_CONFIG_DEBUG));
        m_debugMode = 0;

        findViewById(R.id.main_response_debug_view).setVisibility(View.GONE);

        HeartbeatService.Bind(this, m_heartbeatConn);
        BackgroundService.Bind(this, m_backgroundConn);
        m_lingdongApi.DeviceControlNavGesture(false);
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
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(!hasFocus)
            return;

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String menuGeometry = preferences.getString(Constants.ID_PREFERENCE_MAIN_MENU_GEOMETRY, null/*Configs.ID_PREFERENCE_DEFAULT_MAIN_MENU_GEOMETRY*/);
        if(!Common.StringIsEmpty(menuGeometry))
        {
            String parts[] = menuGeometry.split(",");
            if(parts.length >= 2)
            {
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(mControlsView.getLayoutParams());
                View view = findViewById(R.id.main_second_content);
                try {
                    int horizontalMargin = Integer.parseInt(parts[0]);
                    int verticalMargin = Integer.parseInt(parts[1]);
                    params.setMargins(
                            horizontalMargin >= 0 ? horizontalMargin : (view.getWidth() - (-horizontalMargin) - mControlsView.getWidth()),
                            verticalMargin >= 0 ? verticalMargin : (view.getHeight() - (-verticalMargin) - mControlsView.getHeight()),
                            0,
                            0
                    );
                    mControlsView.setLayoutParams(params);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        SetState(ENUM_STATE_SCREENSAVER);
        m_optIntent.Reset();
        //CloseSerialPortDriver();
        mHideHandler.postDelayed(m_hideFacePanel, 100);
        delayedHide(100);

        CloseCamera();
        OpenScreenSaver(false);
        CloseMaintenanceDialog(false);
        CloseResultDialog(false);

        StopAlert();

        ((TextView)findViewById(R.id.main_camera_info_text)).setText("");

        HeartbeatService.Unbind(this, m_heartbeatConn);
        BackgroundService.Unbind(this, m_backgroundConn);
        m_lingdongApi.DeviceControlNavGesture(true);

        StatusMachine.Instance().shutdown_timestamp = System.currentTimeMillis();
    }

    // 非UI线程显示吐司
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
        // 停止心跳服务
        HeartbeatService.Stop(this);
        BackgroundService.Stop(this);

        m_camera.ShutdownCamera();
        m_handler = null;
        m_cameraHandler = null;
        m_cameraHandlerThread.quit();
        m_cameraHandlerThread = null;

        m_deviceHandler = null;
        m_deviceHandlerThread.quit();
        m_deviceHandlerThread = null;
        StopAlert();
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

    // 打开授权失败对话框
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
        builder.setIcon(R.drawable.icon_warning);
        builder.setCancelable(false);
        builder.setPositiveButton("确定", listener);
        builder.setNegativeButton("退出", listener);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // 验证人脸照片, 返回人员对象, 网络线程
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
                String json = DeviceApi.MakeRequestJson(DeviceApiDef.ID_DEVICE_API_METHOD_FACE_LOGIN, Sys.GetIMEI(MainActivity.this),
                        "base64", "DEBUG_IMAGE_BASE64_CODE......"
                        );
                ((TextView)m_apiDebugView.findViewById(R.id.main_request_debug_text)).setText(json);
                }
            });
        }

        StatusMachine.Instance().verify_face_count++;
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
                    if(ActivityUtility.BuildOnDebug(this)/* && false*/) // TODO: 仅开发测试
                    {
                        user.SetId(UserModel.CONST_TEST_USER_ID);
                        user.SetUsername("开发者");
                        user.SetIsEmployee(UserModel.ENUM_USER_ROLE_ADMIN);
                        user.SetIsEmployee(UserModel.ENUM_USER_ROLE_MAINTENANCE);
                        user.SetIsEmployee("0,1,2");
                    }
                    Logf.e(ID_TAG, "未识别人员: " + Common.Now());
                }
                else
                {
                    try
                    {
                        JsonMap data = (JsonMap)resp.data;
                        String name = data.<String>GetT("username");
                        String uid = data.Get("id").toString();
                        user.SetId(uid);
                        user.SetUsername(name);
                        user.SetIsEmployee(data.Get("isEmployee").toString());
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

    // 本次刷脸认证失败, 继续等待下次刷脸
    private void WaitNextTime(String message)
    {
        // 当总是定时抓图模式下, 进入屏保
        if(m_camera.IsAlwaysCaptureMode())
        {
            OpenResultDialog(false, message, SoundAlert.CONST_SOUND_ALERT_ERROR_FACE_NOT_IDENTIFIED, true);
        }
        else // 当检测到人类模式下, 继续刷脸
        {
            CloseResultDialog(true);

            ShowToast(message, Toast.LENGTH_SHORT);
            m_handler.postDelayed(m_waitNextOperation, ID_HIDE_PERSON_PANEL_DELAY);
        }
    }

    // 更换人脸信息图片, 显示释放bitmap
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

    // 显示人员信息, 网络线程
    private void ShowFacePanel(final Bitmap face, final String name, final String time, final UserModel user)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
            SetPreviewImage(face);
            m_personName.setText(name);
            m_personTime.setText(time);
            if(user != null)
            {
                m_personRole.setText(user.GetRoleName());
            }
            m_personView.animate().setDuration(ID_PERSON_VIEW_ANIM_DELAY).alpha(1.0f).withStartAction(new Runnable() {
                @Override
                public void run() {
                    m_personView.setVisibility(View.VISIBLE);
                }
            }).start();
            }
        });
    }

    // 打开屏保
    private void OpenScreenSaver(boolean anim)
    {
        hide();
        m_screenSaverView.Open(anim);
    }

    // 关闭屏保
    private void CloseScreenSaver(boolean anim)
    {
        m_screenSaverView.Close(anim);
    }

    // 打开串口文件
    private boolean OpenSerialPortDriver() { return false; } // UNUSED: DeviceFunc里自动控制

    // 关闭串口文件
    private void CloseSerialPortDriver() // UNUSED: 不要影响心跳服务的串口读写
    {
        m_deviceFunc.Shutdown(); // TODO: others?
    }

    // 发送串口开门数据, 串口线程
    private SerialPortDeviceDriver.IOResult DoOpenDoor()
    {
        SerialPortDeviceDriver.IOResult res = m_deviceFunc.IO(SerialPortDeviceDriver.ENUM_ACTION_OPEN_DOOR, m_operateDeviceTimeout, m_optIntent.<String>GetData_t("door_id")); // 会阻塞线程
        if(!res.IsSuccess())
        {
            // TODO: 处理???
            Logf.e(ID_TAG, "开门失败");
            if(res.res == SerialPortDeviceDriver.ENUM_RESULT_TIMEOUT)
                Logf.e(ID_TAG, "开门失败原因: 超时");
        }
        return res;
    }

    // 发送串口清维开门数据, 串口线程
    private SerialPortDeviceDriver.IOResult DoOpenMaintenanceDoor()
    {
        SerialPortDeviceDriver.IOResult res = m_deviceFunc.IO(SerialPortDeviceDriver.ENUM_ACTION_OPEN_MAINTENANCE_DOOR, -1, m_optIntent.<String>GetData_t("door_id")); // 会阻塞线程
        if(!res.IsSuccess())
        {
            // TODO: 处理???
            Logf.e(ID_TAG, "清维开门失败");
        }
        return res;
    }

    // 进入菜单活动
    private void OpenMenu()
    {
        Intent intent;

        intent = new Intent(MainActivity.this, ProfileActivity.class);
        MainActivity.this.startActivity(intent);
    }

    // 计算照片裁剪宽高
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

    // 开始相机预览, 准备刷脸
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

    // 停止相机预览, 停止刷脸
    private void StopPreview()
    {
        // TODO: 结束预览 or 停止相机
        if(m_cameraUsagePlan == Constants.ID_CONFIG_CAMERA_USAGE_PLAN_OPEN_ONCE_AND_ONLY_STOP_PREVIEW_WHEN_UNUSED)
            m_camera.StopPreview();
        else
            CloseCamera();
    }

    // 上报投递重量, 网络线程
    private String DoUploadWeight(SerialSessionStruct session, final UserModel user)
    {
        if(!user.IsValid())
        {
            Logf.e(ID_TAG, "无效用户");
            return null;
        }

        if(!session.IsValidSession() || !session.IsPair() || !session.IsPair())
        {
            Logf.e(ID_TAG, "对话无效");
            return null;
        }

        final PutOpenDoorReqStruct req = (PutOpenDoorReqStruct)session.req;
        final PutOpenDoorRespStruct resp = (PutOpenDoorRespStruct)session.resp;

        if(!resp.IsSuccess())
        {
            Logf.e(ID_TAG, "返回数据缺失必要数据: " + resp.toString());
            return null;
        }

        if(m_debugMode != 0)
        {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                ((TextView)m_afterApiDebugView.findViewById(R.id.main_response_debug_text)).setText("");
                String json = DeviceApi.MakeRequestJson(DeviceApiDef.ID_DEVICE_API_METHOD_RUBBISH_RECORD, Sys.GetIMEI(MainActivity.this),
                            "weightOld", resp.weightOld,
                            "weightNew", resp.weightNew,
                            "weightAll", resp.weightAll,
                            "userId", user.Id(),
                            "dropChannel", resp.device_id,
                            "dropStatus", resp.res
                        );
                ((TextView)m_afterApiDebugView.findViewById(R.id.main_request_debug_text)).setText(json);
                }
            });
        }
        final DeviceApiResp apiResp = DeviceApi.UploadWeight(Sys.GetIMEI(MainActivity.this), user.Id(), resp.weightOld, resp.weightNew, resp.weightAll, resp.res, resp.device_id);
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
            Logf.e(ID_TAG, "上报重量服务器异常!");
            return null;
        }
    }

    // 打开结果对话框, 串口网络线程
    private void OpenResultDialog(final boolean suc, final String message, String alert, boolean anim)
    {
        CloseResultDialog(false);

        if(alert != null)
            PlayAlert(alert);
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
        //runOnUiThread(m_finishOperation);
        m_handler.postDelayed(m_finishOperation, ID_OPERATION_FINISHED_INTERVAL);
    }

    // 关闭结果对话框, 主线程
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

    // 播放语音
    private void PlayAlert(String name)
    {
        if(!m_playAlert)
            return;
        if(m_soundAlert == null)
        {
            m_soundAlert = new SoundAlert_mediaplayer(this);
        }
        m_soundAlert.Play(name);
    }

    private void PlayAlertWithCallback(String name, SoundAlert.MediaListener l)
    {
        if(!m_playAlert)
            return;
        if(m_soundAlert == null)
        {
            m_soundAlert = new SoundAlert_mediaplayer(this);
        }
        m_soundAlert.PlayWithCallback(name, l);
    }

    // 关闭语音系统
    private void StopAlert()
    {
        if(m_soundAlert == null)
            return;
        m_soundAlert.Shutdown();
        m_soundAlert = null;
    }

    // 设置界面状态
    private void SetState(int state)
    {
        if(m_state != state)
            m_state = state;
    }

    private void ScanFace(int type)
    {
        m_optIntent.SetType(type);

        StartPreview();
        m_lastVerifyTime = System.currentTimeMillis();
        CloseScreenSaver(true);
        StopBGM();
        PlayAlert(SoundAlert.CONST_SOUND_ALERT_MESSAGE_START_FACE);
    }

    // 打开清维人员对话框, 串口网络线程
    private void OpenMaintenanceDialog(final UserModel user, final boolean anim)
    {
        CloseMaintenanceDialog(false);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                m_maintenanceDialog.Open(user, anim);
                m_handler.postDelayed(m_maintenanceOperation, ID_MAINTENANCE_WAITING_INTERVAL); // TODO: !!!Runnable可能不会执行!!!
            }
        });
    }

    // 关闭结果对话框, 主线程
    private void CloseMaintenanceDialog(final boolean anim)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                m_maintenanceDialog.Close(anim);
            }
        });
    }

    // 上报清维重量, 网络线程
    private String DoCleanDevice(SerialSessionStruct session, final UserModel user)
    {
        if(!session.IsValidSession())
        {
            Logf.e(ID_TAG, "无效对话");
            return null;
        }
        if(!user.IsValid())
        {
            Logf.e(ID_TAG, "无效用户");
            return null;
        }

        /// TODO: 是否需要检测请求与相应的token???
        if(!session.IsPair())
        {
            Logf.e(ID_TAG, "返回数据token无效");
            return null;
        }

        final GetOpenDoorReqStruct req = (GetOpenDoorReqStruct)session.req;
        final GetOpenDoorRespStruct resp = (GetOpenDoorRespStruct)session.resp;

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
                    String json = DeviceApi.MakeRequestJson(DeviceApiDef.ID_DEVICE_API_METHOD_CLEAR_RUBBISH, Sys.GetIMEI(MainActivity.this),
                            "weightOld", resp.weightOld,
                            "weightNew", resp.weightNew,
                            "weightAll", resp.weightAll,
                            "employee", user.Id(),
                            "clearChannel", resp.device_id,
                            "clearStatus", resp.res
                    );
                    ((TextView)m_afterApiDebugView.findViewById(R.id.main_request_debug_text)).setText(json);
                }
            });
        }
        final DeviceApiResp apiResp = DeviceApi.CleanDevice(Sys.GetIMEI(MainActivity.this), user.Id(), resp.weightOld, resp.weightNew, resp.weightAll, resp.res, resp.device_id);
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
            Logf.e(ID_TAG, "上报清维重量服务器异常!");
            return null;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        switch(keyCode)
        {
            case KeyEvent.KEYCODE_BACK:
            {
                if(!m_allowExit)
                    return true;
            }
            break;
            /*case KeyEvent.KEYCODE_HOME:
            {
                if(!m_allowExit)
                    return true;
            }
            break;*/
            default:
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    private HeartbeatService.HeartbeatBinder m_heartbeatBinder = null;
    private ServiceConnection m_heartbeatConn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Logf.e(ID_TAG, "MainActivity解绑心跳服务");
            if(m_heartbeatBinder != null)
            {
                m_heartbeatBinder.SetDeviceStatusListener(null);
                m_heartbeatBinder = null;
            }
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Logf.e(ID_TAG, "MainActivity绑定心跳服务");
            m_heartbeatBinder = (HeartbeatService.HeartbeatBinder) service;
            m_heartbeatBinder.SetDeviceStatusListener(m_heartbeatListener);
        }
    };
    private HeartbeatService.DeviceStatusListener m_heartbeatListener = new HeartbeatService.DeviceStatusListener()
    {
        @Override
        public void OnGetDeviceStatus(String code, String desc) {
            NotifyDeviceStatus(code, desc);
            if(!HeartbeatRespStruct.DeviceIsNormal(code))
                DeviceBrokenAlarm();
        }
    };

    private void DeviceBrokenAlarm()
    {
        StopBGM();
        PlayAlertWithCallback(SoundAlert.CONST_SOUND_ALERT_ERROR_DEVICE_BROKEN, new SoundAlert.MediaListener() {
            @Override
            public void OnEnd(String name) {
                if(SoundAlert.CONST_SOUND_ALERT_ERROR_DEVICE_BROKEN.equals(name))
                    PlayBGM();
            }

            @Override
            public boolean Once() {
                return true;
            }
        });
    }

    private BackgroundService.BackgroundBinder m_backgroundBinder = null;
    private ServiceConnection m_backgroundConn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Logf.e(ID_TAG, "MainActivity解绑背景服务");
            if(m_backgroundBinder != null)
            {
                m_backgroundBinder = null;
            }
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Logf.e(ID_TAG, "MainActivity绑定背景服务");
            m_backgroundBinder = (BackgroundService.BackgroundBinder) service;
        }
    };

    private void StopBGM()
    {
        if(m_backgroundBinder != null)
        {
            m_backgroundBinder.Pause();
        }
    }

    private void PlayBGM()
    {
        if(m_backgroundBinder != null)
        {
            m_backgroundBinder.Replay();
        }
    }

    public void ToDropWaste(final String name)
    {
        if(!StatusMachine.Instance().DeviceIsAccess())
        {
            DeviceBrokenAlarm();
            return;
        }
        if(m_state == ENUM_STATE_PREVIEW)
            return;

        Logf.i(ID_TAG, "扫脸设置垃圾类别: " + name);
        SetState(ENUM_STATE_PREVIEW);
        m_handler.post(new Runnable(){
            @Override
            public void run() {
                findViewById(R.id.main_response_debug_view).setVisibility(View.GONE);

                if("waste".equals(name))
                    m_optIntent/*.SetType(OperationIntent.ENUM_FACE_INTENT_OPEN_DOOR)*/.SetData("door_id", DeviceApiDef.ID_KITCHEN_WASTE_DOOR_ID);
                else if("other".equals(name))
                    m_optIntent/*.SetType(OperationIntent.ENUM_FACE_INTENT_OPEN_DOOR)*/.SetData("door_id", DeviceApiDef.ID_OTHER_WASTE_DOOR_ID);
                else
                {
                    Toast.makeText(MainActivity.this, "门类型无效!", Toast.LENGTH_SHORT).show();
                    return;
                }

                ScanFace(OperationIntent.ENUM_FACE_INTENT_OPEN_DOOR);
            }
        });
    }

    public void ToDropWasteByScanCode(final String name)
    {
        if(!StatusMachine.Instance().DeviceIsAccess())
        {
            DeviceBrokenAlarm();
            return;
        }
        if(m_state == ENUM_STATE_PREVIEW)
            return;

        Logf.i(ID_TAG, "扫码设置垃圾类别: " + name);
        SetState(ENUM_STATE_PREVIEW);
        m_handler.post(new Runnable(){
            @Override
            public void run() {
                findViewById(R.id.main_response_debug_view).setVisibility(View.GONE);

                if("waste".equals(name))
                    m_optIntent/*.SetType(OperationIntent.ENUM_FACE_INTENT_OPEN_DOOR)*/.SetData("door_id", DeviceApiDef.ID_KITCHEN_WASTE_DOOR_ID);
                else if("other".equals(name))
                    m_optIntent/*.SetType(OperationIntent.ENUM_FACE_INTENT_OPEN_DOOR)*/.SetData("door_id", DeviceApiDef.ID_OTHER_WASTE_DOOR_ID);
                else
                {
                    Toast.makeText(MainActivity.this, "门类型无效!", Toast.LENGTH_SHORT).show();
                    return;
                }

                ScanCode(OperationIntent.ENUM_FACE_INTENT_OPEN_DOOR);
            }
        });
    }

    public void ToOpenMenu()
    {
        if(false)
        if(!StatusMachine.Instance().DeviceIsAccess())
        {
            DeviceBrokenAlarm();
            if(false)
                return;
        }
        if(m_state == ENUM_STATE_PREVIEW)
            return;
        SetState(ENUM_STATE_PREVIEW);
        Logf.i(ID_TAG, "请求打开菜单");
        m_handler.post(new Runnable(){
            @Override
            public void run() {
                ScanFace(OperationIntent.ENUM_FACE_INTENT_OPEN_MENU);
            }
        });
    }

    public void NotifyDeviceStatus(String code, String desc)
    {
        m_screenSaverView.NotifyDeviceStatus(code, desc);
    }

    public View GetFrontContentView()
    {
        return mContentView;
    }

    public View GetBackContentView()
    {
        return mControlsView;
    }

    private void ScanCode(int type)
    {
        m_optIntent.SetType(type);

        m_lastVerifyTime = System.currentTimeMillis();
        CloseScreenSaver(true);
        StopBGM();
        PlayAlert(SoundAlert.CONST_SOUND_ALERT_MESSAGE_START_FACE);

        MainQRCodeView view = new MainQRCodeView(this, m_handler);
        view.Open(true);
        ShowToast("扫码开门: " + type, Toast.LENGTH_LONG);
    }

    public void ChooseMaintenance(UserModel user, int type)
    {
        CloseMaintenanceDialog(true);
        m_handler.removeCallbacks(m_maintenanceOperation); // TODO: !!!未测试!!!
        switch(type)
        {
            case R.id.main_maintenance_door3:
                m_optIntent.SetType(OperationIntent.ENUM_FACE_INTENT_OPEN_MAINTENANCE_DOOR).SetData("door_id", DeviceApiDef.ID_KITCHEN_WASTE_RECYCLE_DOOR_ID);
                break;
            case R.id.main_maintenance_door4:
                m_optIntent.SetType(OperationIntent.ENUM_FACE_INTENT_OPEN_MAINTENANCE_DOOR).SetData("door_id", DeviceApiDef.ID_OTHER_WASTE_RECYCLE_DOOR_ID);
                break;
            case R.id.main_maintenance_menu_btn:
                OpenMenu();
                return;
            default:
                return;
        }
        m_deviceHandler.post(new MaintenanceRunnable(user));
    }

    private class MaintenanceRunnable implements Runnable
    {
        private UserModel user;
        public MaintenanceRunnable(UserModel user)
        {
            this.user = user;
        }

        @Override
        public void run() {
            m_cameraMask.SetState(CameraMaskView.ID_STATE_PROCESSING);
            // 继续开门操作
            PlayAlert(SoundAlert.CONST_SOUND_ALERT_NOTIFICATION_MAINTENANCE_FINISHED); // TODO: 直接播放开门语音
            SerialPortDeviceDriver.IOResult result = DoOpenMaintenanceDoor();
            if(result == null) // 开门错误
            {
                m_cameraMask.SetState(CameraMaskView.ID_STATE_PROCESS_FAIL);
                OpenResultDialog(false, "设备异常", SoundAlert.CONST_SOUND_ALERT_ERROR_OPEN_DOOR_FAILED, true);
                return;
            }
            SerialSessionStruct session = result.session;
            if(!session.IsValid()) // 开门失败
            {
                m_cameraMask.SetState(CameraMaskView.ID_STATE_PROCESS_FAIL);
                if(result.res == SerialPortDeviceDriver.ENUM_RESULT_TIMEOUT)
                    OpenResultDialog(false, "操作超时", SoundAlert.CONST_SOUND_ALERT_ERROR_OPEN_DOOR_FAILED, true);
                else
                    OpenResultDialog(false, "开门失败", SoundAlert.CONST_SOUND_ALERT_ERROR_OPEN_DOOR_FAILED, true);
                return;
            }
            m_cameraMask.SetState(CameraMaskView.ID_STATE_PROCESS_SUCCESS);

            // 继续上报清维结果
            String uuid = DoCleanDevice(session, user);
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
                        GetOpenDoorRespStruct resp = (GetOpenDoorRespStruct)session.resp;
                        GetOpenDoorReqStruct req = (GetOpenDoorReqStruct)session.req;
                        RecordModel item = new RecordModel();
                        item.SetName(user.Username());
                        item.SetTime(System.currentTimeMillis());
                        item.SetDevice(req.door_id);
                        item.SetWeight(resp.weightOld + "," + resp.weightNew + "," + resp.weightAll);
                        item.SetOperation(SerialDataDef.ID_SERIAL_DATA_TYPE_PUT_OPEN_DOOR);
                        item.SetResult(RecordModel.ID_RESULT_SUCCESS);
                        item.SetUuid(uuid);
                        item.SetCreateTime(System.currentTimeMillis());
                        if(m_recordDB.Add(item))
                            Logf.i(ID_TAG, "清运结果写入数据库成功: " + item.Id());
                        else
                            Logf.e(ID_TAG, "清运结果写入数据库失败");
                    }
                    catch(Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                OpenResultDialog(true, "清运成功", null, true);
            }
            else
            {
                m_cameraMask.SetState(CameraMaskView.ID_STATE_UPLOAD_FAIL);
                OpenResultDialog(false, "清运开门失败", null, true);
            }
            // 流程结束
        }
    };

}
