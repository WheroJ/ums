package com.zetavision.panda.ums.utils;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Constant {
    /**
     * 记录当前是否为debug状态
     */
    public static final boolean DEBUG = true;

    /**
     * 中文
     */
    public static final String LANG_CHINA = "zh_CN";

    /**
     * 英文
     */
    public static final String LANG_ENGLISH = "en";

    /**
     * 更新已下载表单数量
     */
    public static final String UPDATE_DOWN_COUNT = "UPDATE_DOWN_COUNT";

    /**
     * 更新等待上传表单数量
     */
    public static final String UPDATE_WAIT_UPLOAD_COUNT = "UPDATE_WAIT_UPLOAD_COUNT";
    public static final String LOADING_CONTENT_KEY = "content";

    /**
     * 最大并发
     */
    public static final int MAX_CONCURRENT = 2;

    /**
     * 本地最大下载量（表单）
     */
    public static final int MAX_DOWN = 100;

    /**
     * 1: 内网   2：外网   3:正式
     */
    public static int NET_TYPE = 1;

    public static final String EVENT_REFRESH_LANGUAGE = "LANGUAGE";
    public static final String EVENT_REFRESH_USER = "USER";
    public static final String COOKIE = "cookie";

    public static String API_BASE_URL;
    static {
        setBaseUrlByType();
    }

    public static final int MAX_LEN = 200;

    public static void setBaseUrlByType() {
        switch (NET_TYPE) {
            case 1:
                API_BASE_URL = "http://192.168.0.113:8088/";
                break;
            case 2:
                API_BASE_URL = "http://10.81.210.41:8082/";
                break;
            case 3:
                API_BASE_URL = "http://10.80.65.10:8090/";
                break;
        }
    }

    // capture
    public static final boolean IS_DISABLE_AUTO_ORIENTATION = false;     //是否禁止自动旋转
    public static final boolean IS_AUTO_FOCUS = true;    //自动对焦
    public static final boolean IS_DISABLE_CONTINUOUS_FOCUS = true;    //是否关闭持续对焦
    public static final boolean IS_INVERT_SCAN = false;               //反色 仅适用于部分设备
    public static final boolean IS_DISABLE_BARCODE_SCENE_MODE = true;  //不进行条码场景匹配
    public static final boolean IS_DISABLE_METERING = true;             //不用使用距离测量
    public static final boolean IS_VIBRATE = true;      //是否振动
    public static final boolean IS_PLAY_BEEP = true;     //是否播放声音
    public static final boolean IS_FRONT_LIGHT_MODE_ON = false;   //是否打开闪光灯
    public static final boolean IS_FRONT_LIGHT_AUTO_MODE = true;   //是否自动调节闪光灯亮度
    public static final boolean IS_DISABLE_EXPOSURE = false;       //是否关闭曝光

    //条码类型
    public static final boolean IS_DECODE_1D_PRODUCT = true;     //一维码：商品
    public static final boolean IS_DECODE_1D_INDUSTRIAL = true;     //一维码：工业
    public static final boolean IS_DECODE_QR = true;            //二维码
    public static final boolean IS_DECODE_DATA_MATRIX = false;    //Data matrix
    public static final boolean IS_DECODE_AZTEC = false;           //Aztec
    public static final boolean IS_DECODE_PDF417 = false;           //PDF417 测试


    public static final String FORM_STATUS_PLANNED = "PLANNED";
    public static final String FORM_STATUS_INPROGRESS = "INPROGRESS";
    public static final String FORM_STATUS_COMPLETED = "COMPLETED";
    public static final String FORM_STATUS_CLOSED = "CLOSED";
    public static final String FORM_STATUS_ALL = "ALL";

    public static final String RELOGINACTION = "com.zetavision.panda.ums.ui.LoginActivity";
    public static final String REGETTOKEN = "com.zetavision.panda.ums.ui.LoginActivity.reGetToken";
    public static final String SYSTEM_DIALOG_REASON_KEY = "reason";
    public static final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";
    public static final String SYSTEM_DIALOG_REASON_LOCK = "lock";
    public static final String IS_RUN_BACK = "isBackRun";
    public static final String WAIT_UPLOAD_CRASH_LOG = "WAIT_UPLOAD_CRASH_LOG";
    public static final String ACTION_UPLOAD_LOG = "com.zetavision.panda.ums.uploadImageAndVideo.log";

    @NotNull
    public static final String RE_LOGIN = "re_login";
    @Nullable
    public static final String PHOTOPATH = "loadFromPicturePath";
    public static final int REC_RADIUS_5 = 5;
    @NotNull
    public static final String TAKE_PHOTO = "take_photo";
    public static final String TAKE_VIDEO = "take_video";

    public static final String NET_CONNECT = "net_connect";
    public static final String NET_DISCONNECT = "net_disconnect";

    /**
     * 密码错误
     */
    public static final int PASS_ERROR = -2;

    /**
     * 用户不存在
     */
    public static final int USER_NOT_EXIST = -3;

    /**
     * 登出
     */
    public static final int USER_LOGOUT = -4;
}