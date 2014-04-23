/*
 * Copyright 2010-2012 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.Pull.pullapp.util;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import android.R;
import android.content.Intent;

public class Constants {
	
	public static final String PICTURE_BUCKET = "pull-lauren-julia-hackday1";
	public static final String PICTURE_NAME = "NameOfThePicture";
	public static final boolean DEBUG = false;
	public static final boolean LOG_SMS = false;
	public static final String EXTRA_NAME = "NAME";
	public static final String EXTRA_READ = "READ";
	public static final String EXTRA_NUMBER = "NUMBER";
	public static final String EXTRA_THREAD_ID = "THREAD_ID";
	public static final String EXTRA_SET_OF_MESSAGES = "SET_OF_MESSAGES";
	public static final String EXTRA_SHARE_TYPE = "SHARE_TYPE";
	public static final String IS_SIGNED_IN = "isSignedIn";
	public static final String USER_EMAIL = "userEmail";
	public static final String USER_NAME = "userName";
	
	public static String getPictureBucket() {
		return (PICTURE_BUCKET).toLowerCase(Locale.US);
	}
    public static final String PREFS = "pull.prefs";
    public static final String NULL = "null";
    public static final long DEFAULT_LONG = -1;
    
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
    public static final String ACTION_SMS_DELIVERED = "com.Pull.SMS_DELIVERED";	
    public static final String ACTION_SMS_OUTBOXED = "com.Pull.SMS_OUTBOXED";	
    public static final String ACTION_SMS_UNOUTBOXED = "com.Pull.SMS_UNOUTBOXED";	
    public static final String ACTION_SHARE_TAG = "com.Pull.pullapp.util.ACTION_SHARE_TAG";
    public static final String ACTION_RECEIVE_SHARE_TAG = "com.Pull.pullapp.util.ACTION_RECEIVE_SHARE_TAG";
    public static final String ACTION_SHARE_COMPLETE = "com.Pull.pullapp.util.ACTION_SHARE_COMPLETE";
    public static final String ACTION_RECEIVE_COMMENT = "com.Pull.pullapp.util.ACTION_RECEIVE_COMMENT";;
    
    public static final String TYPE_IN_SMS = "in_sms";
    public static final String TYPE_OUT_SMS = "out_sms";
    public static final String TYPE_IN_CALL = "in_call";
    public static final String TYPE_OUT_CALL = "out_call";
    public static final String TYPE_DATA = "data";
    
    public static final String EXTRA_RECIPIENT = "RECIPIENT";
    public static final String EXTRA_MESSAGE_BODY = "MESSAGE_BODY";
    public static final String EXTRA_TIME_LAUNCHED = "TIME_LAUNCHED";
	public static final String EXTRA_TIME_SCHEDULED_FOR = "TIME_SCHEDULED_FOR";
	public static final String EXTRA_SHARED_CONVERSATION_ID = "SHARED_CONVERSATION_ID";
	public static final String EXTRA_SHARE_RESULT_CODE = "EXTRA_SHARE_RESULT_CODE";
	
	public static final String[] ALL_HASHTAGS = 
			{"#Anger","#Annoyance","#Contempt","#Disgust","#Irritation",
			"#Anxiety","#Embarrassment","#Fear","#Helplessness","#Powerlessness",
			"#Worry","#Doubt","#Envy","#Frustration","#Guilt","#Shame","#Boredom",
			"#Despair","#Disappointment","#Hurt","#Sadness","#Stress","#Shock","#Tension",
			"#Amusement","#Delight","#Elation","#Excitement","#Happiness","#Joy","#Pleasure",
			"#Affection","#Empathy","#Friendliness","#Love","#Courage","#Hope","#Pride",
			"#Satisfaction","#Trust","#Calm","#Content","#Relaxed","#Relieved","#Serene",
			"#Interest","#Politeness","#Surprised","#WTF","#B****Please"};
	public static List<String> ALL_HASHTAGS_LIST = Arrays.asList(ALL_HASHTAGS);
	public static final String GOOGLE_PLAY_LINK = "https://play.google.com/store/apps/details?id=com.pull.pullapp";
	public static final String APP_PLUG_END = "Download the app at Google play: " + GOOGLE_PLAY_LINK;
	public static final int MIN_TEXT_LENGTH = 40;
	public static final int MAX_TEXT_LENGTH = 100;
	public static final int NOTIFICATION_NEW_SHARE = 1;
	public static final String PREFERENCE_TIME_DELAY_PROMPT = "PREFERENCE_TIME_DELAY_PROMPT";
	
	


}
