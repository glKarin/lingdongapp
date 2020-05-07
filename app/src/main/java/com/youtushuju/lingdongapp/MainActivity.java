package com.youtushuju.lingdongapp;

import android.Manifest;
import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.ColorDrawable;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.Face;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Size;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.youtushuju.lingdongapp.api.DeviceApi;
import com.youtushuju.lingdongapp.api.DeviceApiResp;
import com.youtushuju.lingdongapp.common.Logf;
import com.youtushuju.lingdongapp.common.Sys;
import com.youtushuju.lingdongapp.device.LingDongApi_real;
import com.youtushuju.lingdongapp.device.LingDongApi;
import com.youtushuju.lingdongapp.common.Common;
import com.youtushuju.lingdongapp.common.Configs;
import com.youtushuju.lingdongapp.common.Constants;
import com.youtushuju.lingdongapp.gui.ActivityUtility;
import com.youtushuju.lingdongapp.gui.App;
import com.youtushuju.lingdongapp.gui.DynamicTextureView;
import com.youtushuju.lingdongapp.gui.FaceRectView;
import com.youtushuju.lingdongapp.json.JsonMap;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class MainActivity extends AppCompatActivity {
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

    private static final int ID_HIDE_PERSON_PANEL_DELAY = 3500;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
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
    private View mControlsView;
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
    private boolean mVisible;
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

    private static final String ID_TAG = "MainActivity";
    private LingDongApi m_lingdongApi;
    private CameraDevice m_cameraDevice = null;
    private DynamicTextureView m_textureView = null;
    private CameraCaptureSession m_cameraCaptureSession = null;
    private HandlerThread m_threadHandlerThread = null;
    private Handler m_threadHandler = null; // new thread
    private int m_captureInterval = 3000;
    private long m_lastCaptureTime = 0;
    private ImageView m_previewImage;
    private TextView m_personName;
    private TextView m_personTime;
    private View m_personView;
    private List<CameraInfoModel> m_cameraList;
    private int m_textureWidth = 0;
    private int m_textureHeight = 0;
    private Surface m_surface;
    private boolean m_alwaysCapture = true;
    private boolean m_cameraAccessed = false;
    private FaceRectView m_faceRectView = null;
    private CameraInfoModel m_currentCamera = new CameraInfoModel();

    private class CameraInfoModel
    {
        String camera_id; // ID
        int face; // 前置/后置/外置
        int face_mode = 0; // 不支持/简易/全
        int orientation = 0; // 摄像头传感器方向 // 一般前摄像头是270度 后摄像头是90度
        // 选择的分辨率
        int width = 0;
        int height = 0;
        int max_face_count = 0; // 最大人脸检测数
        List<Size> support_size_list = null;
        Rect rect = null; // 成像区域

        public void Reset()
        {
            camera_id = null;
            face = 0;
            face_mode = CaptureRequest.STATISTICS_FACE_DETECT_MODE_OFF;
            orientation = 0;
            width = height = 0;
            max_face_count = 0;
            support_size_list = null;
            rect = null;
        }

        public boolean IsValid()
        {
            return camera_id != null;
        }

        @NonNull
        @Override
        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append("摄像头ID: " + camera_id).append('\n');
            sb.append("位置: " + face).append('\n');
            sb.append("人脸检测模式: " + face_mode).append('\n');
            sb.append("最大人脸检测数: " + max_face_count).append('\n');
            sb.append("传感器角度: " + orientation).append('\n');
            sb.append("支持分辨率: " + support_size_list != null ? support_size_list.toString() : "未获取").append('\n');
            sb.append("成像区域: " + rect != null ? rect.toString() : "未获取").append('\n');
            sb.append("选择分辨率: " + String.format("(%d x %d)", width, height)).append('\n');
            return sb.toString();
        }
    }

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);

        ImageButton button;

        setContentView(R.layout.main);

        m_lingdongApi = new LingDongApi_real(this);

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
                Intent intent;

                intent = new Intent(MainActivity.this, ProfileActivity.class);
                MainActivity.this.startActivity(intent);
            }
        });

        m_previewImage = (ImageView)findViewById(R.id.main_preview_image);
        m_personName = (TextView) findViewById(R.id.main_person_name);
        m_personTime = (TextView) findViewById(R.id.main_person_time);
        m_personView = findViewById(R.id.main_person_view);
        m_faceRectView = (FaceRectView)findViewById(R.id.main_face_rect_layer);
        m_faceRectView.setVisibility(View.GONE);

        m_cameraList = new ArrayList<CameraInfoModel>();

        SetupUI();

        App.Instance().PushActivity(this);
    }

    private void SetupUI()
    {
        m_personView.setVisibility(View.GONE);
        m_threadHandlerThread = new HandlerThread("preview_capture_thread");
        m_threadHandlerThread.start();
        m_threadHandler = new Handler(m_threadHandlerThread.getLooper());

        m_textureView = (DynamicTextureView) findViewById(R.id.main_camera_texture);
        m_textureView.SetFileScheme(DynamicTextureView.ID_FILL_SCHEME_WIDTH_PREFER);
        m_textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener(){
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height)
            {
                m_textureWidth = width;
                m_textureHeight = height;
                Logf.d(ID_TAG, "纹理视图(%d, %d)", m_textureWidth, m_textureHeight);
                //StartCamera();
                InitCamera();
            }

            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height)
            {
                m_textureWidth = width;
                m_textureHeight = height;
                Logf.d(ID_TAG, "纹理视图更新(%d, %d)", m_textureWidth, m_textureHeight);
                TransformTextureView(m_textureWidth, m_textureHeight);
            }

            public void onSurfaceTextureUpdated(SurfaceTexture surface)
            {
            }

            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface)
            {
                return false;
            }
        });
    }

    // 打开相机预览对话
    private void OpenCameraSession(int width, int height)
    {
        SurfaceTexture surfaceTexture = m_textureView.getSurfaceTexture();

        surfaceTexture.setDefaultBufferSize(width, height);
        //Logf.d(ID_TAG, "(%d %d) (%d %d)", width, height, m_textureWidth, m_textureHeight);
        m_surface = new Surface(surfaceTexture);

        try
        {
            final CaptureRequest.Builder captureRequestBuilder = m_cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(m_surface);

            captureRequestBuilder.set(CaptureRequest.STATISTICS_FACE_DETECT_MODE, m_currentCamera.face_mode);
            captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            m_cameraDevice.createCaptureSession(Arrays.asList(m_surface), new CameraCaptureSession.StateCallback(){
                public void onConfigured(CameraCaptureSession session)
                {
                    m_cameraCaptureSession = session;
                    m_cameraAccessed = true;
                    CaptureRequest captureRequest = captureRequestBuilder.build();
                    try
                    {
                        session.setRepeatingRequest(captureRequest, new CameraCaptureSession.CaptureCallback(){
                            public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result)
                            {
                                long now = System.currentTimeMillis();
                                if(m_lastCaptureTime == 0)
                                {
                                    m_lastCaptureTime = now;
                                    return;
                                }
                                if(now - m_lastCaptureTime > m_captureInterval)
                                {
                                    m_lastCaptureTime = now - (now - m_lastCaptureTime - m_captureInterval);
                                    Face faces[] = result.get(TotalCaptureResult.STATISTICS_FACES);
                                    // TODO: 测试
                                    if(!Common.ArrayIsEmpty(faces))
                                    {
                                        ShowToast("检测到人脸", Toast.LENGTH_LONG);
                                        /*List<RectF> rects = new ArrayList<RectF>();
                                        rects.add(CaleFaceRect(faces[0]));
                                        m_faceRectView.SetFaces(rects);*/
                                    }

                                    if(m_alwaysCapture || (m_currentCamera.face_mode == CaptureRequest.STATISTICS_FACE_DETECT_MODE_OFF || !Common.ArrayIsEmpty(faces))) // 仅有人脸时
                                    {
                                        final Bitmap bitmap = m_textureView.getBitmap();
                                        VerifyFace(bitmap);
                                        /*runOnUiThread(new Runnable(){
                                            public void run()
                                            {
                                            HandleCapturePreview(bitmap);
                                            }
                                        });*/
                                    }
                                }
                            }
                        }, m_threadHandler);
                    }
                    catch(Exception e)
                    {
                        e.printStackTrace();
                    }
                }

                public void onConfigureFailed(CameraCaptureSession session)
                {
                    ShowToast("相机预览对话配置错误", Toast.LENGTH_LONG);
                    session.close();
                    m_cameraCaptureSession = null;
                }
            }, m_threadHandler);

        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    private void StartCamera()
    {
        if(m_textureWidth <= 0 || m_textureHeight <= 0)
            return;

        CloseCamera();
        Size size = CalePreferPreviewSize(m_textureWidth, m_textureHeight);
        TransformTextureView(m_textureWidth, m_textureHeight);
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE)
            m_textureView.SetAspectRatio(size.getWidth(), size.getHeight());
        else
            m_textureView.SetAspectRatio(size.getHeight(), size.getWidth());

        ((TextView)findViewById(R.id.main_camera_info_text)).setText(m_currentCamera.toString());

        OpenCamera(size.getWidth(), size.getHeight());
    }

    private void CloseCamera()
    {
        if(m_cameraCaptureSession != null)
        {
            try
            {
                m_cameraCaptureSession.stopRepeating();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            m_cameraCaptureSession.close();
            m_cameraCaptureSession = null;
        }
        if(m_cameraDevice != null)
        {
            m_cameraDevice.close();
            m_cameraDevice = null;
        }
        m_surface = null;
    }

    private void HandleCapturePreview(final Bitmap bitmap)
    {
        //if(true) return;
        m_threadHandler.post(new Runnable(){
            public void run()
            {
                VerifyFace(bitmap);
            }
        });
    }

    // 打开相机
    private void OpenCamera(final int width, final int height)
    {
        CameraManager manager = (CameraManager)getSystemService(Context.CAMERA_SERVICE);

        try
        {
            manager.openCamera(m_currentCamera.camera_id, new CameraDevice.StateCallback(){
                public void onOpened(CameraDevice device)
                {
                    m_cameraDevice = device;
                    OpenCameraSession(width, height);
                }

                public void onDisconnected(CameraDevice device)
                {
                    device.close();
                    m_cameraDevice = null;
                }

                public void onError(CameraDevice device, int error)
                {
                    m_cameraDevice = null;
                    device.close();
                    ShowToast("相机错误" + error, Toast.LENGTH_LONG);
                }
            }, m_threadHandler);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    // TODO: 计算相机视图宽高
    private Size CalePreferPreviewSize(int width, int height)
    {
        int displayRotation = getWindowManager().getDefaultDisplay().getRotation();
        boolean needSwap = WidthAndHeightNeedSwap(displayRotation, m_currentCamera.orientation);
        int w = needSwap ? height : width;
        int h = needSwap ? width : height;
        Logf.d(ID_TAG, "屏幕方向(%d), 相机传感器方向(%d), 交换宽高(%b)", displayRotation, m_currentCamera.orientation, needSwap);

        final float p = (float)w / (float)h;
        Map<Float, List<Size> > res = new HashMap<Float, List<Size> >();
        for (Size s : m_currentCamera.support_size_list)
        {
            /*if(s.getWidth() > w || s.getHeight() > h)
                continue;*/ // 获取全部
            float dp = (float)s.getWidth() / (float)s.getHeight();
            if(!res.containsKey(dp))
                res.put(dp, new ArrayList<Size>());
            res.get(dp).add(s);
        }
        float min = Collections.min(res.keySet(), new Comparator<Float>(){
            public int compare(Float a, Float b)
            {
                float pa = a - p;
                float pb = b - p;
                float f = (Math.abs(pa) - Math.abs(pb));
                if(f < 0)
                    return -1;
                else if(f > 0)
                    return 1;
                else
                {
                    float d = pa - pb;
                    return d < 0 ? -1 : (d > 0 ? 1 : 0);
                }
            }
        });
        List<Size> prefer = res.get(min);
        List<Size> upper = new ArrayList<Size>();
        List<Size> lower = new ArrayList<Size>();
        for(Size s : prefer)
        {
            if(s.getWidth() >= w || s.getHeight() >= h)
                upper.add(s);
            else
                lower.add(s);
        }
        Size upperMin = Collections.max(upper, m_sizeComparator);
        Size lowerMax = Collections.min(lower, m_sizeComparator);
        Logf.e(ID_TAG, "分辨率排序(%s), 当前纹理比例(%f), 上下浮分辨率(%s, %s), 最适合比例(%f)", res.toString(), p, upperMin != null ? upperMin.toString() : "不存在", lowerMax != null ? lowerMax.toString() : "不存在", min);
        Size max = upperMin != null ? upperMin : (lowerMax != null ? lowerMax : prefer.get(0));
        Logf.d(ID_TAG, "选择相机分辨率(%s)", max.toString());

        //Size size = needSwap ? new Size(max.getHeight(), max.getWidth()) : max;
        Size size = max;
        //Size size = needSwap ? max : new Size(max.getHeight(), max.getWidth());
        m_currentCamera.width = size.getWidth();
        m_currentCamera.height = size.getHeight();

        return size;
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
        SharedPreferences preference;

        m_lastCaptureTime = 0;
        m_currentCamera.Reset();

        findViewById(R.id.main_response_debug_view).setVisibility(View.GONE);
        preference = PreferenceManager.getDefaultSharedPreferences(this);
        m_captureInterval = Integer.parseInt(preference.getString(Constants.ID_PREFERENCE_FACE_FREQUENCY, "" + Configs.ID_PREFERENCE_DEFAULT_FACE_FREQUENCY));
        String faceCaptureScheme = preference.getString(Constants.ID_PREFERENCE_FACE_CAPTURE_SCHEME, Configs.ID_PREFERENCE_DEFAULT_FACE_CAPTURE_SCHEME);
        m_alwaysCapture = Constants.ID_CONFIG_FACE_CAPTURE_SCHEME_ALWAYS.equals(faceCaptureScheme);

        if(ActivityUtility.IsGrantPermission(this, Manifest.permission.CAMERA) && !CameraAvailable())
        {
            InitCamera();
        }

        ((TextView)findViewById(R.id.main_camera_info_text)).setText(m_currentCamera.toString());
    }

    private boolean CameraAvailable()
    {
        return m_cameraDevice != null;
    }

    @Override
    protected void onPause() {
        super.onPause();

        mHideHandler.postDelayed(m_hideFacePanel, 100);
        delayedHide(100);

        CloseCamera();
        m_lastCaptureTime = 0;
        m_currentCamera.Reset();

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
        CloseCamera();
        App.Instance().PopActivity();
    }

    private void InitCamera()
    {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        try
        {
            CameraManager manager = (CameraManager)getSystemService(Context.CAMERA_SERVICE);
            String cameraId = preferences.getString(Constants.ID_PREFERENCE_FACE_CAMERA, "" + Configs.ID_PREFERENCE_DEFAULT_FACE_CAMERA);
            String cameraIds[] = manager.getCameraIdList();
            String toggleCameraId = null;
            int cameraFace = CameraCharacteristics.LENS_FACING_FRONT;
            for (String str : cameraIds)
            {
                CameraCharacteristics cc = manager.getCameraCharacteristics(str);
                int face = cc.get(CameraCharacteristics.LENS_FACING);
                if(("" + face).equals(cameraId))
                {
                    toggleCameraId = str;
                    cameraFace = face;
                }
            }
            if(toggleCameraId == null)
            {
                toggleCameraId = cameraIds[0];
                CameraCharacteristics cc = manager.getCameraCharacteristics(toggleCameraId);
                cameraFace = cc.get(CameraCharacteristics.LENS_FACING);
                Toast.makeText(this, "设备不支持该摄像头类型, 将打开摄像头默认", Toast.LENGTH_LONG).show();
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(Constants.ID_PREFERENCE_FACE_CAMERA, "" + cameraFace);
                editor.commit();
            }
            m_currentCamera.camera_id = toggleCameraId;
            m_currentCamera.face = cameraFace;
            CameraCharacteristics cc = manager.getCameraCharacteristics(m_currentCamera.camera_id);
            int faceDetectCount = cc.get(CameraCharacteristics.STATISTICS_INFO_MAX_FACE_COUNT);
            int faceDetectModes[] = cc.get(CameraCharacteristics.STATISTICS_INFO_AVAILABLE_FACE_DETECT_MODES);

            int faceMode = CaptureRequest.STATISTICS_FACE_DETECT_MODE_OFF;
            for (int m : faceDetectModes)
            {
                if(m == CaptureRequest.STATISTICS_FACE_DETECT_MODE_FULL)
                    faceMode = CaptureRequest.STATISTICS_FACE_DETECT_MODE_FULL;
                if(m == CaptureRequest.STATISTICS_FACE_DETECT_MODE_SIMPLE && faceMode == CaptureRequest.STATISTICS_FACE_DETECT_MODE_OFF)
                    faceMode = CaptureRequest.STATISTICS_FACE_DETECT_MODE_SIMPLE;
            }
            if(faceMode == CaptureRequest.STATISTICS_FACE_DETECT_MODE_OFF)
                Toast.makeText(this, "该摄像头可能不支持人脸检测", Toast.LENGTH_LONG).show();
            m_currentCamera.face_mode = faceMode;
            m_currentCamera.max_face_count = faceDetectCount;

            StreamConfigurationMap map = cc.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            List<Size> sizes = Arrays.asList(map.getOutputSizes(SurfaceTexture.class));
            Logf.d(ID_TAG, "当前相机支持分辨率(%s)", sizes.toString());
            m_currentCamera.support_size_list = sizes;
            m_currentCamera.orientation = cc.get(CameraCharacteristics.SENSOR_ORIENTATION);
            m_currentCamera.rect = cc.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);

            StartCamera();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
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
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", MainActivity.this.getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
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
        builder.setPositiveButton("确定", listener);
        builder.setNegativeButton("退出", listener);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private String VerifyFace(final Bitmap bitmap)
    {
        int quality = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getInt(Constants.ID_PREFERENCE_FACE_IMAGE_QUALITY, Configs.ID_PREFERENCE_DEFAULT_FACE_IMAGE_QUALITY);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out);
        String image = android.util.Base64.encodeToString(out.toByteArray(), android.util.Base64.NO_WRAP);
        final DeviceApiResp resp = DeviceApi.VerifyFace(Sys.GetIMEI(MainActivity.this), image); // TODO: 当前后台线程
        if(resp != null)
        {
            if(resp.IsSuccess())
            {
                if(resp.data == null || resp.data instanceof String) // MISMATCHED
                {
                    ShowFacePanel(bitmap/*null*/, "未识别身份", Common.Now());
                }
                else
                {
                    try
                    {
                        JsonMap data = (JsonMap)resp.data;
                        String name = data.<String>GetT("username");
                        ShowFacePanel(bitmap, name, Common.Now());
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
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    findViewById(R.id.main_response_debug_view).setVisibility(View.VISIBLE);
                    ((TextView)findViewById(R.id.main_response_debug_text)).setText(resp.json);
                }
            });
        }
        else
            ShowToast("请求人脸识别服务器异常!", Toast.LENGTH_LONG);

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
                mHideHandler.postDelayed(m_hideFacePanel, ID_HIDE_PERSON_PANEL_DELAY);
            }
        });
    }

    private boolean WidthAndHeightNeedSwap(int displayRotation, int sensorOrientation)
    {
        boolean exchange = false;

        switch (displayRotation)
        {
            case Surface.ROTATION_0:
            case Surface.ROTATION_180:
                if (sensorOrientation == 90 || sensorOrientation == 270)
                {
                    exchange = true;
                }
                break;
            case Surface.ROTATION_90:
            case Surface.ROTATION_270:
                if (sensorOrientation == 0 || sensorOrientation == 180)
                {
                    exchange = true;
                }
                break;
            default:
                break;
        }

        return exchange;
    }

    private RectF CaleFaceRect(Face face)
    {
        boolean mirror = (m_currentCamera.face == CameraCharacteristics.LENS_FACING_FRONT);
        int displayRotation = getWindowManager().getDefaultDisplay().getRotation();
        boolean needSwap = WidthAndHeightNeedSwap(displayRotation, m_currentCamera.orientation);

        float w = (float)m_currentCamera.width;
        float h = (float)m_currentCamera.height;

        float scaledWidth = w / (float)m_currentCamera.rect.width();
        float scaledHeight = h / (float)m_currentCamera.rect.height();
        Logf.e(ID_TAG, "%d, %b | %f, %f | %b | %f, %f", displayRotation, needSwap, scaledWidth, scaledHeight, mirror, w, h);
        Matrix mFaceDetectMatrix = new Matrix();
        mFaceDetectMatrix.setRotate(m_currentCamera.orientation);
        mFaceDetectMatrix.postScale(mirror ? -scaledWidth : scaledWidth, scaledHeight);
        if (needSwap)
            mFaceDetectMatrix.postTranslate(h, w);

        Rect bounds = face.getBounds();
        float left = bounds.left;
        float top = bounds.top;
        float right = bounds.right;
        float bottom = bounds.bottom;
        RectF rawFaceRect = new RectF(left, top, right, bottom);
        mFaceDetectMatrix.mapRect(rawFaceRect);

        //0, 0 - 2608, 1960
        RectF resultFaceRect = mirror ? rawFaceRect : new RectF(rawFaceRect.left, rawFaceRect.top - w, rawFaceRect.right, rawFaceRect.bottom - w);

        Logf.e(ID_TAG, "Face(%s) | (%s) -> Screen(%s) | (%s)", face.getBounds().toString(), m_currentCamera.rect, resultFaceRect.toString(), rawFaceRect.toString());
        //return dstRect;

        return resultFaceRect;
    }

    private void TransformTextureView(int viewWidth, int viewHeight)
    {
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, m_currentCamera.height, m_currentCamera.width);
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation)
        {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max((float)viewHeight / (float)m_currentCamera.height, (float)viewWidth / (float)m_currentCamera.width);
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate((float)(90 * (rotation - 2)), centerX, centerY);
        }
        else if (Surface.ROTATION_180 == rotation)
        {
            matrix.postRotate(180, centerX, centerY);
        }
        m_textureView.setTransform(matrix);
    }

    private Comparator<Size> m_sizeComparator = new Comparator<Size>(){
        public int compare(Size a, Size b)
        {
            return (a.getWidth() * a.getHeight()) - (b.getWidth() * b.getHeight());
        }
    };
}
