package com.youtushuju.lingdongapp.common;

public final class Constants
{
	public static final String ID_PREFERENCE_FACE_FREQUENCY = "FACE_FREQUENCY";
	public static final String ID_PREFERENCE_FACE_CAMERA = "FACE_CAMERA";
	public static final String ID_PREFERENCE_SERIAL_PATH = "SERIAL_PATH";
	public static final String ID_PREFERENCE_SERIAL_BAUDRATE = "SERIAL_BAUDRATE";
	public static final String ID_PREFERENCE_SERIAL_DRIVER = "SERIAL_DRIVER";
	public static final String ID_PREFERENCE_FACE_IMAGE_QUALITY = "FACE_IMAGE_QUALITY";
	public static final String ID_PREFERENCE_SERIAL_PATH_SELECT = "SERIAL_PATH_SELECT";
	public static final String ID_PREFERENCE_FACE_CAPTURE_SCHEME = "FACE_CAPTURE_SCHEME";
	public static final String ID_PREFERENCE_CAMERA_RESOLUTION = "CAMERA_RESOLUTION";
	public static final String ID_PREFERENCE_CLEAN_LOG = "CLEAN_LOG";
	public static final String ID_PREFERENCE_OPEN_SCREEN_SAVER_MAX_INTERVAL = "OPEN_SCREEN_SAVER_MAX_INTERVAL";
	public static final String ID_PREFERENCE_OPERATE_DEVICE_TIMEOUT = "OPERATE_DEVICE_TIMEOUT";
	public static final String ID_PREFERENCE_CAMERA_USAGE_PLAN = "CAMERA_USAGE_PLAN";
	public static final String ID_PREFERENCE_PREVIEW_CAPTURE_CROP = "PREVIEW_CAPTURE_CROP";
	public static final String ID_PREFERENCE_CLEAN_RECORD = "CLEAN_RECORD";
	public static final String ID_PREFERENCE_RECORD_HISTORY = "RECORD_HISTORY";
	public static final String ID_PREFERENCE_DEBUG_MODE = "DEBUG_MODE";
	public static final String ID_PREFERENCE_PLAY_VOICE_ALERT = "PLAY_VOICE_ALERT";
	public static final String ID_PREFERENCE_LAYOUT_EDIT = "LAYOUT_EDIT";
	public static final String ID_PREFERENCE_MAIN_MENU_GEOMETRY = "MAIN_MENU_GEOMETRY";
	public static final String ID_PREFERENCE_CAMERA_DRAW_BOX = "CAMERA_DRAW_BOX";
	
	public static final int ID_REQUEST_RESULT_SUCCESS = 0;
	public static final int ID_REQUEST_RESULT_FAIL = 1;
	public static final int ID_REQUEST_RESULT_ERROR = 2;

	public static final String ID_CONFIG_API_EMULATE = "emulate";
	public static final String ID_CONFIG_API_REAL = "real";

	public static final String ID_CONFIG_SERIAL_DRIVER_CEPR = "cepr";
	public static final String ID_CONFIG_SERIAL_DRIVER_UART = "uart";
	public static final String ID_CONFIG_SERIAL_DRIVER_TEST = "test";

	public static final String ID_CONFIG_FACE_CAPTURE_SCHEME_ALWAYS = "always";
	public static final String ID_CONFIG_FACE_CAPTURE_SCHEME_WHEN_FACE = "face_only";

	public static final String ID_CONFIG_CAMERA_RESOLUTION_HIGHER = "higher";
	public static final String ID_CONFIG_CAMERA_RESOLUTION_LOWER = "lower";
	public static final String ID_CONFIG_CAMERA_RESOLUTION_HIGHEST = "highest";
	public static final String ID_CONFIG_CAMERA_RESOLUTION_LOWEST = "lowest";

	public static final int ID_CONFIG_CAMERA_USAGE_PLAN_OPEN_ONCE_AND_ONLY_STOP_PREVIEW_WHEN_UNUSED = 1;
	public static final int ID_CONFIG_CAMERA_USAGE_PLAN_CLOSE_WHEN_UNUSED_AND_REOPEN_WHEN_NEED = 0;

	public static final int ID_APP_BUILD_DEBUG = 0;
	public static final int ID_APP_BUILD_RELEASE = 1;
	
	private Constants(){}
}
