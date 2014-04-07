package com.Pull.pullapp.util;

public abstract class This {
    
    public static final String PREFS = "omnitor.prefs";
    public static final String NULL = "null";
    public static final long DEFAULT_LONG = -1;
    public static final boolean DEBUG = true;
    
    public static final String KEY_FIRST_RUN = "fr";
    public static final String KEY_SIGNED_UP = "su";
    public static final String KEY_UUID = "uid";
    public static final String KEY_ROAMING_STATE = "rs";
    public static final String KEY_TIME_FIRST_RUN = "frt";
    public static final String KEY_TIME_LAST_CHECKED_OUT_SMS = "tos";
    public static final String KEY_DEVICE_LOG = "dl";
    public static final String KEY_MOBILE_TX = "mtx";
    public static final String KEY_MOBILE_RX = "mrx";
    public static final String KEY_NETWORK_TX = "ntx";
    public static final String KEY_NETWORK_RX = "nrx";
    
    public static final String ACTION_INITIAL_UPLOAD = "com.Pull.pullapp.util.ACTION_INITIAL_UPLOAD";
    public static final String ACTION_CHECK_OUT_SMS = "com.Pull.pullapp.util.ACTION_CHECK_OUT_SMS";
    public static final String ACTION_UPLOAD_LOGS = "com.Pull.pullapp.util.ACTION_UPLOAD_LOGS";
    public static final String ACTION_LOG_DATA = "com.Pull.pullapp.util.ACTION_LOG_DATA";
    public static final String ACTION_SEND_DELAYED_TEXT = "com.Pull.pullapp.util.ACTION_SEND_DELAYED_TEXT";
    
    public static final String TYPE_IN_SMS = "in_sms";
    public static final String TYPE_OUT_SMS = "out_sms";
    public static final String TYPE_IN_CALL = "in_call";
    public static final String TYPE_OUT_CALL = "out_call";
    public static final String TYPE_DATA = "data";
    
    public static final String EXTRA_RECIPIENT = "RECIPIENT";
    public static final String EXTRA_MESSAGE_BODY = "MESSAGE_BODY";
    public static final String EXTRA_TIME_LAUNCHED = "TIME_LAUNCHED";
    
}