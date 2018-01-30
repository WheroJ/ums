package com.zetavision.panda.ums.Utils;


public class Constant {
    /**
     * 记录当前是否为debug状态
     */
    public static final boolean isDebug = false;

    /**
     * 1: 内网   2：外网
     */
    private static final int net_type = 1;

    public static final String EVENT_REFRESH_LANGUAGE = "LANGUAGE";
    public static final String EVENT_REFRESH_USER = "USER";
    public static String API_BASE_URL;
    static {
        switch (net_type) {
            case 1:
                API_BASE_URL = "http://7.177.122.179:8088/ums/control/";
                break;
            case 2:
                API_BASE_URL = "http://192.168.0.200:8088/ums/control/";
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


}