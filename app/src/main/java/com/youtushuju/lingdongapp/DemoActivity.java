package com.youtushuju.lingdongapp;

import android.app.ISmatekListener;
import android.app.SmatekManager;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.SystemClock;
import android.provider.Settings;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.youtushuju.lingdongapp.demo.EthernetUtils;
import com.youtushuju.lingdongapp.gui.App;


public class DemoActivity extends AppCompatActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    private Button btnApiVersion, btnDeviceModel, btnOsVersion,
            btnRunningMemory, btnInnerStorage, btnFreeStorage,
            btnKernelVersion, btnBuilderNumber,
            btnShutDown, btnReboot, btnScreenshot, btnBrightness,
            btnRotation, btnHeight, btnWidth, btnOpenBackLight,
            btnCloseBackLight, btnStatusBarShow, btnStatusBarHide,
            btnEthMacAddress, btnEthIpAddress, btnScardPath, btnUsbPath,
            btnInner, btnInstall,
            btnSysTime, btnSysDate, btn24, btn12, btnHdmiStatus, btnEthernetConnect,
            btnSubmitStaticIp, btnSleep, btnScreen, btnScreenOpen, btnMasterClear, btnFilterPermission,
            btnOta, btnKeyInput, btnCpuGovernor, btnFrequencies, btnCurrentFreq, btnCpuControl, btnDramCurrentFreq,
            btnDramGovernor, btnHdmiOpen, btnHdmiClose, btnGestureOpen, btnGestureClose;
    //    btnWatchDogOpen, btnWatchDogClose,
    private SeekBar sbVolume;
    private TextView tvContent;
    private EditText editIpAddress, editGateway, editNetmask, editDns1, editDns2, editBrightness,
            editRotation, editGovernor, editFrequencies, editCpuNode, editCpuNodeValue, editDramGovernor;

    private SmatekManager smatekManager;
    private static String TAG = "MainActivity";


    //@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        smatekManager = (SmatekManager) getSystemService("smatek");
        smatekManager.addSmatekListener(new ISmatekListener() {
            @Override
            public void onKeyEvent(int i, int i1) {
                Log.e(TAG, "i :" + i + " i1 :" + i1);
            }

            @Override
            public void ethernetLinkState(int i) {
                Log.e(TAG, "i :" + i);
            }

        });

        App.Instance().PushActivity(this);
    }


    private void initView() {
        tvContent = findViewById(R.id.tv_content);
        btnApiVersion = findViewById(R.id.btn_api_version);
        btnDeviceModel = findViewById(R.id.btn_device_model);
        btnOsVersion = findViewById(R.id.btn_os_version);
        btnRunningMemory = findViewById(R.id.btn_running_memory);
        btnInnerStorage = findViewById(R.id.btn_inner_storage);
        btnFreeStorage = findViewById(R.id.btn_free_storage);
        btnKernelVersion = findViewById(R.id.btn_kernel_version);
        btnBuilderNumber = findViewById(R.id.btn_builder_number);
        btnShutDown = findViewById(R.id.btn_shut_down);
        btnReboot = findViewById(R.id.btn_reboot);
        btnScreenshot = findViewById(R.id.btn_screenshot);
        btnBrightness = findViewById(R.id.btn_brightness);
        btnRotation = findViewById(R.id.btn_rotation);
        btnHeight = findViewById(R.id.btn_height);
        btnWidth = findViewById(R.id.btn_width);
        btnOpenBackLight = findViewById(R.id.btn_open_back_light);
        btnCloseBackLight = findViewById(R.id.btn_close_back_light);
        btnStatusBarShow = findViewById(R.id.btn_statusBar_show);
        btnStatusBarHide = findViewById(R.id.btn_statusBar_hide);
        btnEthMacAddress = findViewById(R.id.btn_eth_mac_address);
        btnEthIpAddress = findViewById(R.id.btn_eth_ip_address);
        btnScardPath = findViewById(R.id.btn_sdcard);
        btnUsbPath = findViewById(R.id.btn_usb);
        btnInner = findViewById(R.id.btn_inner);
        btnInstall = findViewById(R.id.btn_install);
//        btnWatchDogOpen = findViewById(R.id.btn_watch_dog_open);
//        btnWatchDogClose = findViewById(R.id.btn_watch_dog_close);
        btnSysTime = findViewById(R.id.btn_sys_time);
        btnSysDate = findViewById(R.id.btn_sys_date);
        btn24 = findViewById(R.id.btn_24);
        btn12 = findViewById(R.id.btn_12);
        btnHdmiStatus = findViewById(R.id.btn_hdmi_status);
        btnEthernetConnect = findViewById(R.id.btn_ethernet_connect);
        editRotation = findViewById(R.id.edit_rotation);

        sbVolume = findViewById(R.id.sb_volume);
        sbVolume.setOnSeekBarChangeListener(this);

        editIpAddress = findViewById(R.id.edit_ip_address);
        editGateway = findViewById(R.id.edit_gateway);//网关
        editNetmask = findViewById(R.id.edit_netmask);//掩码
        editDns1 = findViewById(R.id.edit_dns1);
        editDns2 = findViewById(R.id.edit_dns2);
        btnSubmitStaticIp = findViewById(R.id.btn_submit_static_ip);

        btnSleep = findViewById(R.id.btn_sleep);
        btnScreen = findViewById(R.id.btn_screen);
        btnScreenOpen = findViewById(R.id.btn_screen_open);
        editBrightness = findViewById(R.id.edit_brightness);
        btnMasterClear = findViewById(R.id.btn_master_clear);
        btnFilterPermission = findViewById(R.id.btn_filter_permission);
        btnOta = findViewById(R.id.btn_ota);
        btnKeyInput = findViewById(R.id.btn_key_input);
        editGovernor = findViewById(R.id.edit_cpu_governor);
        editFrequencies = findViewById(R.id.edit_cpu_frequencies);
        btnCpuGovernor = findViewById(R.id.btn_cpu_governor);
        btnFrequencies = findViewById(R.id.btn_cpu_frequencies);
        btnCurrentFreq = findViewById(R.id.btn_current_freq);
        editCpuNode = findViewById(R.id.edit_cpu_node);
        editCpuNodeValue = findViewById(R.id.edit_cpu_node_value);
        btnCpuControl = findViewById(R.id.btn_cpu_control);
        btnDramCurrentFreq = findViewById(R.id.btn_dram_current_freq);
        editDramGovernor = findViewById(R.id.edit_dram_governor);
        btnDramGovernor = findViewById(R.id.btn_dram_governor);
        btnHdmiOpen = findViewById(R.id.btn_hdmi_open);
        btnHdmiClose = findViewById(R.id.btn_hdmi_close);
        btnGestureOpen = findViewById(R.id.btn_gesture_open);
        btnGestureClose = findViewById(R.id.btn_gesture_close);


        btnApiVersion.setOnClickListener(this);
        btnDeviceModel.setOnClickListener(this);
        btnOsVersion.setOnClickListener(this);
        btnRunningMemory.setOnClickListener(this);
        btnInnerStorage.setOnClickListener(this);
        btnFreeStorage.setOnClickListener(this);
        btnKernelVersion.setOnClickListener(this);
        btnBuilderNumber.setOnClickListener(this);
        btnShutDown.setOnClickListener(this);
        btnReboot.setOnClickListener(this);
        btnScreenshot.setOnClickListener(this);
        btnBrightness.setOnClickListener(this);
        btnRotation.setOnClickListener(this);
        btnHeight.setOnClickListener(this);
        btnWidth.setOnClickListener(this);
        btnOpenBackLight.setOnClickListener(this);
        btnCloseBackLight.setOnClickListener(this);
        btnStatusBarShow.setOnClickListener(this);
        btnStatusBarHide.setOnClickListener(this);
        btnEthMacAddress.setOnClickListener(this);
        btnEthIpAddress.setOnClickListener(this);
        btnScardPath.setOnClickListener(this);
        btnUsbPath.setOnClickListener(this);
        btnInner.setOnClickListener(this);
        btnInstall.setOnClickListener(this);
//        btnWatchDogOpen.setOnClickListener(this);
//        btnWatchDogClose.setOnClickListener(this);

        btnSysTime.setOnClickListener(this);
        btnSysDate.setOnClickListener(this);
        btn24.setOnClickListener(this);
        btn12.setOnClickListener(this);
        btnHdmiStatus.setOnClickListener(this);
        btnEthernetConnect.setOnClickListener(this);
        btnSubmitStaticIp.setOnClickListener(this);
        btnSleep.setOnClickListener(this);
        btnScreen.setOnClickListener(this);
        btnScreenOpen.setOnClickListener(this);
        btnMasterClear.setOnClickListener(this);
        btnFilterPermission.setOnClickListener(this);
        btnOta.setOnClickListener(this);
        btnKeyInput.setOnClickListener(this);
        btnCpuGovernor.setOnClickListener(this);
        btnFrequencies.setOnClickListener(this);
        btnCurrentFreq.setOnClickListener(this);
        btnCpuControl.setOnClickListener(this);
        btnDramCurrentFreq.setOnClickListener(this);
        btnDramGovernor.setOnClickListener(this);

        btnHdmiOpen.setOnClickListener(this);
        btnHdmiClose.setOnClickListener(this);
        btnGestureOpen.setOnClickListener(this);
        btnGestureClose.setOnClickListener(this);

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_api_version:
                String apiVersion = smatekManager.getAPIVersion();
                Log.e(TAG, "apiVersion :" + apiVersion);
                tvContent.setText(apiVersion);
                break;
            case R.id.btn_device_model:
                String deviceModel = smatekManager.getDeviceModel();
                Log.e(TAG, "deviceModel :" + deviceModel);
                tvContent.setText(deviceModel);
                break;
            case R.id.btn_os_version:
                String androidOSVersion = smatekManager.getAndroidOSVersion();
                Log.e(TAG, "androidOSVersion :" + androidOSVersion);
                tvContent.setText(androidOSVersion);

                break;
            case R.id.btn_running_memory:
                String runningMemory = smatekManager.getRunningMemory();
                Log.e(TAG, "runningMemory :" + runningMemory);
                tvContent.setText(runningMemory);
                break;
            case R.id.btn_inner_storage:
                String internalStorageMemory = smatekManager.getInternalStorageMemory();
                Log.e(TAG, "internalStorageMemory :" + internalStorageMemory);
                tvContent.setText(internalStorageMemory);
                break;
            case R.id.btn_free_storage:
                String internalFreeStorageMemory = smatekManager.getInternalFreeStorageMemory();
                Log.e(TAG, "internalFreeStorageMemory :" + internalFreeStorageMemory);
                tvContent.setText(internalFreeStorageMemory);
                break;
            case R.id.btn_kernel_version:
                String kernelVersion = smatekManager.getKernelVersion();
                Log.e(TAG, "kernelVersion :" + kernelVersion);
                tvContent.setText(kernelVersion);
                break;
            case R.id.btn_builder_number:
                String builderNumberDisplay = smatekManager.getBuilderNumberDisplay();
                Log.e(TAG, "builderNumberDisplay :" + builderNumberDisplay);
                tvContent.setText(builderNumberDisplay);
                break;
            case R.id.btn_shut_down:
                smatekManager.shutdown();
                break;
            case R.id.btn_reboot:
                smatekManager.reboot();
                break;
            case R.id.btn_screenshot:
                smatekManager.screenshots("/storage/emulated/0/", "test##.png");
                break;
            case R.id.btn_brightness:
                String trim = editBrightness.getText().toString().trim();
                if (!TextUtils.isEmpty(trim)) {
                    smatekManager.setBrightness(Integer.parseInt(trim));
                } else {
                    Toast.makeText(DemoActivity.this, "情输入亮度值", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.btn_rotation:
                String rotation = editRotation.getText().toString().trim();
                smatekManager.setRotation(rotation);
                break;
            case R.id.btn_height:
                int screenHeight = smatekManager.getScreenHeight(this);
                Log.e(TAG, "screenHeight :" + screenHeight);
                tvContent.setText(screenHeight + "");
                break;
            case R.id.btn_width:
                int screenWidth = smatekManager.getScreenWidth(this);
                Log.e(TAG, "screenWidth :" + screenWidth);
                tvContent.setText(screenWidth + "");
                break;
            case R.id.btn_open_back_light:
                smatekManager.setLcdBlackLight(true);
                break;
            case R.id.btn_close_back_light:
                smatekManager.setLcdBlackLight(false);
                break;
            case R.id.btn_statusBar_show:
                Intent intent = new Intent();
                intent.setAction("com.smatek.show.navigationbar");
                intent.putExtra("show_navigationbar", true);//auto true 为显示 ，false 为 隐藏
                sendBroadcast(intent);

                break;
            case R.id.btn_statusBar_hide:
                Intent intent1 = new Intent();
                intent1.setAction("com.smatek.show.navigationbar");
                intent1.putExtra("show_navigationbar", false);//auto true 为显示 ，false 为 隐藏
                sendBroadcast(intent1);
                break;
            case R.id.btn_eth_mac_address:
                String ethMacAddress = smatekManager.getEthMacAddress();
                Log.e(TAG, "ethMacAddress :" + ethMacAddress);
                tvContent.setText(ethMacAddress);
                break;
            case R.id.btn_eth_ip_address:
                String ethIPAddress = smatekManager.getEthIPAddress();
                Log.e(TAG, "ethIPAddress :" + ethIPAddress);
                tvContent.setText(ethIPAddress);
                break;

            case R.id.btn_inner:
                String primaryStoragePath = smatekManager.getPrimaryStoragePath();
                Log.e(TAG, "primaryStoragePath :" + primaryStoragePath);
                tvContent.setText(primaryStoragePath);
                break;
            case R.id.btn_sdcard:
                String sdcardPath = smatekManager.getSdcardPath();
                Log.e(TAG, "sdcardPath :" + sdcardPath);
                tvContent.setText(sdcardPath);
                break;
            case R.id.btn_usb:
                String usbPath = smatekManager.getUSBPath();
                Log.e(TAG, "usbPath :" + usbPath);
                tvContent.setText(usbPath);
                break;
            case R.id.btn_install:
                Intent intent3 = new Intent();
                intent3.setAction("com.smatek.silentInstall");
                intent3.putExtra("com.smatek.silentInstall.path", "/storage/emulated/0/lantern-installer.apk");
                intent3.putExtra("com.smatek.silentInstall.packageName", "org.getlantern.lantern");
                sendBroadcast(intent3);

                break;
//            case R.id.btn_watch_dog_open:
//                smatekManager.watchDogEnable(true);
//                break;
//            case R.id.btn_watch_dog_close:
//                smatekManager.watchDogEnable(false);
//                break;
            case R.id.btn_sys_time:
                smatekManager.setSystemTime(16, 5);
                break;
            case R.id.btn_sys_date:
                smatekManager.setSystemDate(2019, 11, 5);
                break;
            case R.id.btn_24:
                smatekManager.setDateTime24hour(true);
                break;
            case R.id.btn_12:
                smatekManager.setDateTime24hour(false);
                break;
            case R.id.btn_hdmi_status:
                int hdmiinStatus = smatekManager.getHdmiinStatus();
                Log.e(TAG, "hdmiinStatus :" + hdmiinStatus);
                break;
            case R.id.btn_ethernet_connect:
                int ethernetLinkStatus = smatekManager.getEthernetLinkStatus();
                Log.e(TAG, "ethernetLinkStatus :" + ethernetLinkStatus);
                break;
            case R.id.btn_submit_static_ip:
                //设置静态ip
                String ipAddress = editIpAddress.getText().toString().trim();
                String gateway = editGateway.getText().toString().trim();
                String netmask = editNetmask.getText().toString().trim();
                String dns1 = editDns1.getText().toString().trim();
                String dns2 = editDns2.getText().toString().trim();

                EthernetUtils ethernetUtils = new EthernetUtils();
                boolean b = ethernetUtils.checkIPValue(ipAddress, gateway, netmask, dns1, dns2);
                if (b) {
                    smatekManager.setStaticIp(ipAddress, netmask, gateway, dns1, dns2);
                }
                break;
            case R.id.btn_sleep:
                smatekManager.goToSleep();

                break;
            case R.id.btn_screen:
                smatekManager.openOrCloseScreen(true);
                break;
            case R.id.btn_screen_open:
                smatekManager.openOrCloseScreen(false);
                break;
            case R.id.btn_master_clear:
                smatekManager.eraseAllData();
                break;
            case R.id.btn_filter_permission:
                smatekManager.setFilterPermissionPackageName("com.smatek.smatekapi");

                Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 0);

                break;
            case R.id.btn_ota:
                Intent intent2 = new Intent();
                intent2.setAction("com.smatek.ota.download.complete");
                intent2.putExtra("com.smatek.ota.download.path", "/storage/emulated/0/rk3288-ota-100825.zip");
                sendBroadcast(intent2);
                break;

            case R.id.btn_key_input:
                // 模拟键值输入
                long now = SystemClock.uptimeMillis();
//                KeyEvent down =  new KeyEvent(now, now,KeyEvent.ACTION_DOWN, 25, 0);
//                KeyEvent up = new KeyEvent(now, now,KeyEvent.ACTION_UP, 25, 0);
//                smatekManager.injectInputEvent(down);
//                smatekManager.injectInputEvent(up);

                //模拟触摸点触摸
                MotionEvent event = MotionEvent.obtain(now, now, MotionEvent.ACTION_DOWN, 960, 540, 0);
                MotionEvent event1 = MotionEvent.obtain(now, now, MotionEvent.ACTION_UP, 960, 540, 0);
                if ((event.getSource() & InputDevice.SOURCE_CLASS_POINTER) == 0) {
                    event.setSource(InputDevice.SOURCE_TOUCHSCREEN);
                }

                if ((event1.getSource() & InputDevice.SOURCE_CLASS_POINTER) == 0) {
                    event1.setSource(InputDevice.SOURCE_TOUCHSCREEN);
                }
                smatekManager.injectInputEvent(event);
                smatekManager.injectInputEvent(event1);
                break;
            case R.id.btn_current_freq:
                String currentFreq = smatekManager.getDataFromNode("/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq");
                Log.e(TAG, "currentFreq :" + currentFreq);
                break;

            case R.id.btn_cpu_governor:
                String cpuGovernor = editGovernor.getText().toString().trim();
                Log.e(TAG, cpuGovernor);
                if (cpuGovernor != null) {
                    smatekManager.writeToNode("/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor", cpuGovernor);
                }

                break;
            case R.id.btn_cpu_frequencies:
                String dataFromNode = smatekManager.getDataFromNode("/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_frequencies");
                String[] spit = dataFromNode.trim().split(" ");
                //  126000, 216000, 408000, 600000, 696000, 816000, 1008000, 1200000, 1416000, 1512000, 1608000
                smatekManager.writeToNode("/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor", "userspace");  // 首先将cpu策略设置为userspace模式
                smatekManager.writeToNode("/sys/devices/system/cpu/cpu0/cpufreq/scaling_setspeed", spit[0]);             //设置频率,spit[0] 是可用频率中的一个

                break;
            case R.id.btn_cpu_control:
                String cpuNode = editCpuNode.getText().toString().trim();
                String nodeValue = editCpuNodeValue.getText().toString().trim();
                if (cpuNode != null && nodeValue != null) {
                    smatekManager.writeToNode(cpuNode, nodeValue);
                }
                break;
            case R.id.btn_dram_current_freq:
                String dramFreq = smatekManager.getDataFromNode("/sys/devices/platform/dmc/devfreq/dmc/cur_freq");
                Log.e(TAG, "dramFreq :" + dramFreq);
                break;

            case R.id.btn_dram_governor:
                String dramGovernor = editDramGovernor.getText().toString().trim();
                if (dramGovernor != null) {
                    smatekManager.writeToNode("/sys/class/devfreq/dmc/governor", dramGovernor);// dramGovernor可传入 dmc_ondemand、userspace、powersave、performance、simple_ondemand
                }

                break;
            case R.id.btn_hdmi_open:
                smatekManager.controlHDMI(true);
                break;
            case R.id.btn_hdmi_close:

                smatekManager.controlHDMI(false);
                break;
            case R.id.btn_gesture_open:
                smatekManager.controlUpwardGesture(true);
                break;
            case R.id.btn_gesture_close:
                smatekManager.controlUpwardGesture(false);
                break;


        }

    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            switch (seekBar.getId()) {
                case R.id.sb_volume:
//                    smatekManager.setSystemVolume(progress);
                    AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                    int streamMaxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                    int i = (progress * streamMaxVolume / 100);
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, i, AudioManager.FLAG_PLAY_SOUND);

                    break;
            }
        }

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        App.Instance().PopActivity();
    }

}
