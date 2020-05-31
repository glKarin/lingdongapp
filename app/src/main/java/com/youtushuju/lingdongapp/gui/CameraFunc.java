package com.youtushuju.lingdongapp.gui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.youtushuju.lingdongapp.ProfileActivity;
import com.youtushuju.lingdongapp.R;
import com.youtushuju.lingdongapp.api.DeviceApi;
import com.youtushuju.lingdongapp.api.DeviceApiResp;
import com.youtushuju.lingdongapp.common.Common;
import com.youtushuju.lingdongapp.common.Configs;
import com.youtushuju.lingdongapp.common.Constants;
import com.youtushuju.lingdongapp.common.Logf;
import com.youtushuju.lingdongapp.common.Sys;
import com.youtushuju.lingdongapp.device.LingDongApi;
import com.youtushuju.lingdongapp.device.LingDongApi_real;
import com.youtushuju.lingdongapp.json.JsonMap;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CameraFunc {
    private static final String ID_TAG = "CameraFunc";
    private static final int ID_WAIT_PREVIEW_INTERVAL = 200;

    private DynamicTextureView m_textureView = null;
    private Activity m_activity = null;

    private CameraInfoModel m_currentCamera = new CameraInfoModel();
    private OnCameraListener m_cameraListener = null;

    private int m_captureInterval = Configs.ID_PREFERENCE_DEFAULT_FACE_FREQUENCY;
    private boolean m_cameraAccessed = false;
    private boolean m_alwaysCapture = true;
    private Surface m_surface = null;
    private int m_textureWidth = 0;
    private int m_textureHeight = 0;
    private String m_cameraResolution = null;
    private String m_cameraPreferFace = null;

    private CameraDevice m_cameraDevice = null;
    private CameraCaptureSession m_cameraCaptureSession = null;
    private CaptureRequest.Builder m_captureRequestBuilder = null;
    private CaptureRequest m_captureRequest = null;

    private HandlerThread m_threadHandlerThread = null;
    private Handler m_threadHandler = null; // on new thread
    private long m_lastCaptureTime = 0L; // 最后抓图时间
    private List<CameraInfoModel> m_cameraList;

    public interface OnCameraListener{
        public void OnCameraOpenResult(boolean success); // 当打开相机后结果回调
        public void OnCameraOpenPreviewResult(boolean success); // 当打开相机会话后结果回调
        public void OnPreviewStart();
        public void OnPreviewStop();
        public void OnPreviewCapture(TotalCaptureResult result, Face faces[], long time, boolean always, int face_mode);
        public void OnClose();
        public void OnWarning(String message);
        public void OnError(String message);
        public void OnFail(String message);
        public void OnDebug(String message);
        public void OnCameraChanged(CameraInfoModel info);
    }

    public final class CameraInfoModel
    {
        public String camera_id; // ID
        public int face; // 前置/后置/外置
        public int face_mode = 0; // 不支持/简易/全
        public int orientation = 0; // 摄像头传感器方向 // 一般前摄像头是270度 后摄像头是90度
        // 选择的分辨率
        public int width = 0;
        public int height = 0;
        public int max_face_count = 0; // 最大人脸检测数
        public List<Size> support_size_list = null;
        public Rect rect = null; // 成像区域

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
            sb.append("支持分辨率: " + (support_size_list != null ? support_size_list.toString() : "未获取")).append('\n');
            sb.append("成像区域: " + (rect != null ? rect.toString() : "未获取")).append('\n');
            sb.append("选择分辨率: " + String.format("(%d x %d)", width, height)).append('\n');
            return sb.toString();
        }
    }

    public CameraFunc(Activity activity, DynamicTextureView textureView, Handler handler)
    {
        m_activity = activity;
        m_textureView = textureView;
        m_threadHandler = handler;
    }

    // 打开相机
    private void OpenCamera(final int width, final int height)
    {
        CameraManager manager = (CameraManager)m_activity.getSystemService(Context.CAMERA_SERVICE);

        try
        {
            manager.openCamera(m_currentCamera.camera_id, new CameraDevice.StateCallback(){
                public void onOpened(CameraDevice device)
                {
                    m_cameraDevice = device;
                    if(m_cameraListener != null)
                        m_cameraListener.OnCameraOpenResult(true);
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
                    if(m_cameraListener != null)
                        m_cameraListener.OnFail("相机错误: " + error);
                }
            }, m_threadHandler);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    // 打开相机预览对话
    public void OpenCameraSession(int width, int height)
    {
        SurfaceTexture surfaceTexture = m_textureView.getSurfaceTexture();

        surfaceTexture.setDefaultBufferSize(width, height);
        //Logf.d(ID_TAG, "(%d %d) (%d %d)", width, height, m_textureWidth, m_textureHeight);
        m_surface = new Surface(surfaceTexture);

        try
        {
            m_captureRequestBuilder = m_cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            m_captureRequestBuilder.addTarget(m_surface);

            m_captureRequestBuilder.set(CaptureRequest.STATISTICS_FACE_DETECT_MODE, m_currentCamera.face_mode);
            m_captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            m_cameraDevice.createCaptureSession(Arrays.asList(m_surface), new CameraCaptureSession.StateCallback(){
                public void onConfigured(CameraCaptureSession session)
                {
                    m_cameraCaptureSession = session;
                    m_captureRequest = m_captureRequestBuilder.build();

                    // StartPreview(); // TODO: 不自动预览
                }

                public void onConfigureFailed(CameraCaptureSession session)
                {
                    if(m_cameraListener != null)
                        m_cameraListener.OnFail("相机预览对话配置错误");
                    session.close();
                    m_captureRequestBuilder = null;
                    m_captureRequest = null;
                    m_cameraCaptureSession = null;
                    m_cameraAccessed = false;
                    if(m_cameraListener != null)
                        m_cameraListener.OnCameraOpenPreviewResult(false);
                }
            }, m_threadHandler);

        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    // 等待预览
    public void WaitPreview(int timeout)
    {
        if(timeout != 0)
        {
            int start = 0;
            while(m_cameraCaptureSession == null)
            {
                try
                {
                    Thread.sleep(ID_WAIT_PREVIEW_INTERVAL);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                start += ID_WAIT_PREVIEW_INTERVAL;
                if(timeout > 0)
                {
                    if(start >= timeout)
                        break;
                }
            }
        }

        StartPreview();
    }

    // 开始预览
    public void StartPreview()
    {
        if(m_cameraCaptureSession == null)
            return;
        if(m_cameraAccessed)
            return;

        if(m_captureRequest == null)
            return;
        m_cameraAccessed = true;
        m_lastCaptureTime = 0;

        try
        {
            m_cameraCaptureSession.setRepeatingRequest(m_captureRequest, new CameraCaptureSession.CaptureCallback(){
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result)
                {
                    if(!m_cameraAccessed)
                        return;

                    long now = System.currentTimeMillis();
                    if(m_lastCaptureTime == 0)
                    {
                        m_lastCaptureTime = now;
                        if(m_cameraListener != null)
                            m_cameraListener.OnPreviewStart();
                        return;
                    }

                    if(now - m_lastCaptureTime >= m_captureInterval)
                    {
                        //m_lastCaptureTime = now - (now - m_lastCaptureTime - m_captureInterval); // 固定时间间隔
                        //Logf.e(ID_TAG, "%d - %d = %d(%d)", now, m_lastCaptureTime, now - m_lastCaptureTime, m_captureInterval);
                        m_lastCaptureTime = now;
                        Face faces[] = result.get(TotalCaptureResult.STATISTICS_FACES);
                        // TODO: 测试
                        /*if(!Common.ArrayIsEmpty(faces))
                        {
                            if(m_cameraListener != null)
                                m_cameraListener.OnDebug("检测到人脸: " + faces.length);
                                        List<RectF> rects = new ArrayList<RectF>();
                                        rects.add(CaleFaceRect(faces[0]));
                                        m_faceRectView.SetFaces(rects);
                        }*/

                        // 仅有人脸时
                        //boolean ava = m_alwaysCapture || (m_currentCamera.face_mode == CaptureRequest.STATISTICS_FACE_DETECT_MODE_OFF || !Common.ArrayIsEmpty(faces));

                        //final Bitmap bitmap = m_textureView.getBitmap();
                        if(m_cameraListener != null)
                            m_cameraListener.OnPreviewCapture(result, faces, now, m_alwaysCapture, m_currentCamera.face_mode);
                    }
                }
            }, m_threadHandler);
            if(m_cameraListener != null)
                m_cameraListener.OnCameraOpenPreviewResult(true);
        }
        catch(Exception e)
        {
            e.printStackTrace();
            m_cameraListener.OnCameraOpenPreviewResult(false);
        }
    }

    // 结束预览
    public void StopPreview()
    {
        if(m_cameraCaptureSession == null)
            return;
        if(!m_cameraAccessed)
            return;
        m_cameraAccessed = false;
        m_lastCaptureTime = 0;
        try
        {
            m_cameraCaptureSession.stopRepeating();
            if(m_cameraListener != null)
                m_cameraListener.OnPreviewStop();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    // 打开摄像头
    public void StartCamera()
    {
        if(m_textureWidth <= 0 || m_textureHeight <= 0)
            return;

        CloseCamera();
        Size size = CalePreferPreviewSize(m_textureWidth, m_textureHeight);
        TransformTextureView(m_textureWidth, m_textureHeight);
        int orientation = m_activity.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE
                || (m_currentCamera.orientation != 90 && m_currentCamera.orientation != 270)) // 如果摄像头传感器度数为0
            m_textureView.SetAspectRatio(size.getWidth(), size.getHeight());
        else
            m_textureView.SetAspectRatio(size.getHeight(), size.getWidth());

        OpenCamera(size.getWidth(), size.getHeight());
    }

    // 关闭摄像头
    public void CloseCamera()
    {
        StopPreview();
        m_captureRequest = null;
        m_captureRequestBuilder = null;
        //m_captureRequestBuilder.removeTarget(m_surface);
        if(m_cameraCaptureSession != null)
        {
            m_cameraCaptureSession.close();
            m_cameraCaptureSession = null;
        }
        if(m_cameraDevice != null)
        {
            m_cameraDevice.close();
            m_cameraDevice = null;
        }
        m_surface = null;
        m_cameraAccessed = false;
        if(m_cameraListener != null)
            m_cameraListener.OnClose();
    }

    public void ShutdownCamera()
    {
        CloseCamera();
        // TODO
    }

    // 选择最适合当前屏幕的相机分辨率
    private Size CalePreferPreviewSize(int width, int height)
    {
        Size max = null;

        if(Constants.ID_CONFIG_CAMERA_RESOLUTION_HIGHEST.equals(m_cameraResolution))
        {
            max =  Collections.max(m_currentCamera.support_size_list, m_sizeComparator);
            Logf.d(ID_TAG, "直接选择最大支持(%s)", max);
        }
        else if(Constants.ID_CONFIG_CAMERA_RESOLUTION_LOWEST.equals(m_cameraResolution))
        {
            max =  Collections.min(m_currentCamera.support_size_list, m_sizeComparator);
            Logf.d(ID_TAG, "直接选择最小支持(%s)", max);
        }
        else
        {
            int displayRotation = m_activity.getWindowManager().getDefaultDisplay().getRotation();
            boolean needSwap = WidthAndHeightNeedSwap(displayRotation, m_currentCamera.orientation);
            int w = needSwap ? height : width;
            int h = needSwap ? width : height;
            Logf.d(ID_TAG, "选择最优比例: 屏幕方向(%d), 相机传感器方向(%d), 交换宽高(%b)", displayRotation, m_currentCamera.orientation, needSwap);

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
            Logf.e(ID_TAG, "比较适合的分辨率: 宽高比(%f), 列表(%s)", min, prefer.toString());
            List<Size> upper = new ArrayList<Size>();
            List<Size> lower = new ArrayList<Size>();
            for(Size s : prefer)
            {
                if(s.getWidth() >= w || s.getHeight() >= h)
                    upper.add(s);
                else
                    lower.add(s);
            }
            Size upperMin = null, lowerMax = null;
            if(upper != null && !upper.isEmpty())
                upperMin = Collections.max(upper, m_sizeComparator);
            if(lower != null && !lower.isEmpty())
                lowerMax = Collections.min(lower, m_sizeComparator);
            Logf.e(ID_TAG, "分辨率排序(%s), 当前纹理比例(%f), 上下浮分辨率(%s, %s)", res.toString(), p, upperMin != null ? upperMin.toString() : "不存在", lowerMax != null ? lowerMax.toString() : "不存在");

            max = prefer.get(0);
            if(Constants.ID_CONFIG_CAMERA_RESOLUTION_LOWER.equals(m_cameraResolution))
            {
                if(lowerMax != null)
                    max = lowerMax;
                else if(upperMin != null)
                    max = upperMin;
            }
            else if(Constants.ID_CONFIG_CAMERA_RESOLUTION_HIGHER.equals(m_cameraResolution))
            {
                if(upperMin != null)
                    max = upperMin;
                else if(lowerMax != null)
                    max = lowerMax;
            }
        }

        Logf.d(ID_TAG, "最终选择相机分辨率(%s)", max.toString());

        //Size size = needSwap ? new Size(max.getHeight(), max.getWidth()) : max;
        Size size = max;
        //Size size = needSwap ? max : new Size(max.getHeight(), max.getWidth());
        m_currentCamera.width = size.getWidth();
        m_currentCamera.height = size.getHeight();
        if(m_cameraListener != null)
            m_cameraListener.OnCameraChanged(m_currentCamera);

        return size;
    }

    // 重置状态
    public void Reset()
    {
        m_lastCaptureTime = 0;
        m_currentCamera.Reset();
        if(m_cameraListener != null)
            m_cameraListener.OnCameraChanged(m_currentCamera);
    }

    // 准备打开相机, 初始化相关打开变量(直接从偏好中同步)
    public void Ready()
    {
        SharedPreferences preference;

        preference = PreferenceManager.getDefaultSharedPreferences(m_activity);

        m_captureInterval = Integer.parseInt(preference.getString(Constants.ID_PREFERENCE_FACE_FREQUENCY, "" + Configs.ID_PREFERENCE_DEFAULT_FACE_FREQUENCY));
        String faceCaptureScheme = preference.getString(Constants.ID_PREFERENCE_FACE_CAPTURE_SCHEME, Configs.ID_PREFERENCE_DEFAULT_FACE_CAPTURE_SCHEME);
        m_alwaysCapture = Constants.ID_CONFIG_FACE_CAPTURE_SCHEME_ALWAYS.equals(faceCaptureScheme);
        m_cameraResolution = preference.getString(Constants.ID_PREFERENCE_CAMERA_RESOLUTION, Configs.ID_PREFERENCE_DEFAULT_CAMERA_RESOLUTION);
        m_cameraPreferFace = preference.getString(Constants.ID_PREFERENCE_FACE_CAMERA, "" + Configs.ID_PREFERENCE_DEFAULT_FACE_CAMERA);
    }

    // 相机是否已经打开
    public boolean CameraAvailable()
    {
        return m_cameraDevice != null;
    }

    // 相机会话是否有效
    public boolean CameraSessionAvailable()
    {
        return CameraAvailable() && m_cameraCaptureSession != null;
    }

    // 初始化并打开相机
    public void InitCamera()
    {
        try
        {
            CameraManager manager = (CameraManager)m_activity.getSystemService(Context.CAMERA_SERVICE);
            String cameraIds[] = manager.getCameraIdList();
            String toggleCameraId = null;
            int cameraFace = CameraCharacteristics.LENS_FACING_FRONT;
            for (String str : cameraIds)
            {
                CameraCharacteristics cc = manager.getCameraCharacteristics(str);
                int face = cc.get(CameraCharacteristics.LENS_FACING);
                if(("" + face).equals(m_cameraPreferFace))
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
                if(m_cameraListener != null)
                    m_cameraListener.OnWarning("设备不支持该摄像头类型, 将打开摄像头默认");
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(m_activity).edit();
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
            {
                if(m_cameraListener != null)
                    m_cameraListener.OnWarning("该摄像头可能不支持人脸检测");
            }
            m_currentCamera.face_mode = faceMode;
            m_currentCamera.max_face_count = faceDetectCount;

            StreamConfigurationMap map = cc.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            List<Size> sizes = Arrays.asList(map.getOutputSizes(SurfaceTexture.class));
            Logf.d(ID_TAG, "当前相机支持分辨率(%s)", sizes.toString());
            m_currentCamera.support_size_list = sizes;
            m_currentCamera.orientation = cc.get(CameraCharacteristics.SENSOR_ORIENTATION);
            m_currentCamera.rect = cc.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);

            if(m_cameraListener != null)
                m_cameraListener.OnCameraChanged(m_currentCamera);
            StartCamera();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    // 判断屏幕与摄像头是否都是`长>高`或`长<高`, 即方向相同
    public boolean WidthAndHeightNeedSwap(int displayRotation, int sensorOrientation)
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

    // TODO: !!!转换人脸坐标!!!
    public RectF CaleFaceRect(Face face)
    {
        boolean mirror = (m_currentCamera.face == CameraCharacteristics.LENS_FACING_FRONT);
        int displayRotation = m_activity.getWindowManager().getDefaultDisplay().getRotation();
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

    // 设置TextureView变换矩阵
    private void TransformTextureView(int viewWidth, int viewHeight)
    {
        int rotation = m_activity.getWindowManager().getDefaultDisplay().getRotation();
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
            return
                    //Long.signum
                            (a.getWidth() * a.getHeight()) - (b.getWidth() * b.getHeight());
        }
    };

    // 相预览是否已经开始
    public boolean IsOpened()
    {
        return CameraAvailable() && m_cameraAccessed;
    }

    public void SetOnCameraListener(OnCameraListener l)
    {
        m_cameraListener = l;
    }

    public CameraInfoModel CurrentCamera()
    {
        return m_currentCamera;
    }

    public void ResizeTextureView(int width, int height)
    {
        m_textureWidth = width;
        m_textureHeight = height;
        TransformTextureView(m_textureWidth, m_textureHeight);
    }

    public boolean IsAlwaysCaptureMode()
    {
        return m_alwaysCapture;
    }
}
