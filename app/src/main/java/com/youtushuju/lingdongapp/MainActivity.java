package com.youtushuju.lingdongapp;

import android.Manifest;
import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.ColorDrawable;
import android.hardware.camera2.TotalCaptureResult;
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
import com.youtushuju.lingdongapp.gui.CameraFunc;
import com.youtushuju.lingdongapp.gui.DynamicTextureView;
import com.youtushuju.lingdongapp.gui.FaceRectView;
import com.youtushuju.lingdongapp.gui.ScreenSaverView;
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

    private static final int ID_HIDE_PERSON_PANEL_DELAY = 3500;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;

    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private View mControlsView;
    private boolean mVisible;

    private LingDongApi m_lingdongApi;
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

    private HandlerThread m_threadHandlerThread = null;
    private Handler m_threadHandler = null; // new thread

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
        public void OnPreviewCapture(TotalCaptureResult result) {
            final Bitmap bitmap = m_textureView.getBitmap();
            HandleCapturePreview(bitmap);
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

        m_screenSaverView = findViewById(R.id.main_screensaver_view);
        m_webView = (ScreenSaverView)findViewById(R.id.main_screensaver_content);

        SetupUI();

        m_camera = new CameraFunc(this, m_textureView);
        m_currentCamera = m_camera.CurrentCamera();
        m_camera.SetOnCameraListener(m_onCameraListener);

        App.Instance().PushActivity(this);
    }

    private void SetupUI()
    {
        m_personView.setVisibility(View.GONE);
        /*m_threadHandlerThread = new HandlerThread("preview_capture_thread");
        m_threadHandlerThread.start();
        m_threadHandler = new Handler(m_threadHandlerThread.getLooper());*/

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
        /*m_threadHandler.post(new Runnable(){
            public void run()
            {
                VerifyFace(bitmap);
            }
        });*/
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                CloseScreenSaver();
            }
        });
        VerifyFace(bitmap);
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

        m_camera.Reset();

        findViewById(R.id.main_response_debug_view).setVisibility(View.GONE);

        if(ActivityUtility.IsGrantPermission(this, Manifest.permission.CAMERA))
        {
            // TODO: Init
            OpenCamera();
        }

        CloseScreenSaver();
        ((TextView)findViewById(R.id.main_camera_info_text)).setText(m_currentCamera.toString());
    }

    @Override
    protected void onPause() {
        super.onPause();

        mHideHandler.postDelayed(m_hideFacePanel, 100);
        delayedHide(100);

        CloseCamera();
        CloseScreenSaver();

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
}
