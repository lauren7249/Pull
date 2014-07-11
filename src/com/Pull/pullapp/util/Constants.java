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
	public static final boolean DEBUG = false  ;
	
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
	public static final String USER_PASSWORD = "pull_password";
	
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
    
    public static final String PACKAGE_NAME = "com.Pull.pullapp";
    public static final String ACTION_INITIAL_UPLOAD = PACKAGE_NAME + ".util.ACTION_INITIAL_UPLOAD";
    public static final String ACTION_CHECK_OUT_SMS = PACKAGE_NAME + ".util.ACTION_CHECK_OUT_SMS";
    public static final String ACTION_UPLOAD_LOGS = PACKAGE_NAME + ".util.ACTION_UPLOAD_LOGS";
    public static final String ACTION_LOG_DATA = PACKAGE_NAME + ".util.ACTION_LOG_DATA";
    public static final String ACTION_SEND_DELAYED_TEXT = PACKAGE_NAME + ".util.ACTION_SEND_DELAYED_TEXT";
    public static final String ACTION_SMS_DELIVERED = "com.Pull.SMS_DELIVERED";	
    public static final String ACTION_SMS_OUTBOXED = "com.Pull.SMS_OUTBOXED";	
    public static final String ACTION_SMS_UNOUTBOXED = "com.Pull.SMS_UNOUTBOXED";	
    public static final String ACTION_SHARE_TAG = PACKAGE_NAME + ".util.ACTION_SHARE_TAG";
    public static final String ACTION_RECEIVE_SHARE_TAG = PACKAGE_NAME + ".util.ACTION_RECEIVE_SHARE_TAG";
    public static final String ACTION_SHARE_COMPLETE = PACKAGE_NAME + ".util.ACTION_SHARE_COMPLETE";
    public static final String ACTION_RECEIVE_COMMENT = PACKAGE_NAME + ".util.ACTION_RECEIVE_COMMENT";
    public static final String ACTION_SEND_COMMENT_CANCELED = PACKAGE_NAME + ".util.ACTION_SEND_COMMENT_CANCELED";
    public static final String ACTION_SEND_COMMENT_CONFIRMED = PACKAGE_NAME + ".util.ACTION_SEND_COMMENT_CONFIRMED";
    public static final String ACTION_TIME_TICK = PACKAGE_NAME + ".util.ACTION_TIME_TICK";
    
    public static final String TYPE_IN_SMS = "in_sms";
    public static final String TYPE_OUT_SMS = "out_sms";
    public static final String TYPE_IN_CALL = "in_call";
    public static final String TYPE_OUT_CALL = "out_call";
    public static final String TYPE_DATA = "data";
    
    public static final String EXTRA_RECIPIENT = PACKAGE_NAME + "RECIPIENT";
    public static final String EXTRA_MESSAGE_BODY = PACKAGE_NAME + "MESSAGE_BODY";
    public static final String EXTRA_TIME_LAUNCHED = PACKAGE_NAME + "TIME_LAUNCHED";
	public static final String EXTRA_TIME_SCHEDULED_FOR = PACKAGE_NAME + "TIME_SCHEDULED_FOR";
	public static final String EXTRA_SHARED_CONVERSATION_ID = PACKAGE_NAME + "SHARED_CONVERSATION_ID";
	public static final String EXTRA_SHARE_RESULT_CODE = PACKAGE_NAME + "EXTRA_SHARE_RESULT_CODE";
	
	public static final String[] ALL_HASHTAGS = 
			{"#Anger","#Annoyance","#Contempt","#Disgust","#Irritation",
			"#Anxiety","#Embarrassment","#Fear","#Helplessness","#Powerlessness",
			"#Worry","#Doubt","#Envy","#Frustration","#Guilt","#Shame","#Boredom",
			"#Despair","#Disappointment","#Hurt","#Sadness","#Stress","#Shock","#Tension",
			"#Amusement","#Delight","#Elation","#Excitement","#Happiness","#Joy","#Pleasure",
			"#Affection","#Empathy","#Friendliness","#Love","#Courage","#Hope","#Pride",
			"#Satisfaction","#Trust","#Calm","#Content","#Relaxed","#Relieved","#Serene",
			"#Interest","#Politeness","#Surprised","#WTF","#B****Please","#OMG","i have a date",
			"#flake","#player","#socute","#icanteven","#entitled","look who stopped responding",
			"make this person go away","#dyinginside"};
	public static List<String> ALL_HASHTAGS_LIST = Arrays.asList(ALL_HASHTAGS);
	public static String EXTRA_SHARED_CONVO_TYPE = PACKAGE_NAME + "EXTRA_SHARED_CONVO_TYPE";
	
	public static final String GOOGLE_PLAY_LINK = "https://play.google.com/store/apps/details?id=com.Pull.pullapp";
	public static final String APP_PLUG_END = "Download the app at Google play: " + GOOGLE_PLAY_LINK;
	public static final int MIN_TEXT_LENGTH = 40;
	public static final int MAX_TEXT_LENGTH = 100;
	public static final int NOTIFICATION_NEW_SHARE = 1;
	public static final int NOTIFICATION_COMMENT = 2;
	public static final int NOTIFICATION_UPDATE_SHARE = 3;
	public static final String PREFERENCE_TIME_DELAY_PROMPT = "PREFERENCE_TIME_DELAY_PROMPT";
	public static final String FACEBOOK_USER_ID = PACKAGE_NAME + "_FACEBOOK_ID" ;
	public static final String EXTRA_COMMENT_NUMBER = PACKAGE_NAME + "EXTRA_COMMENT_NUMBER";
	public static final String EXTRA_SIGNIN_RESULT = PACKAGE_NAME + "EXTRA_SIGNIN_RESULT";
	public static final String ACTION_COMPLETE_SIGNUP = PACKAGE_NAME + ".util.ACTION_COMPLETE_SIGNUP";
	public static final String MIXEDPANEL_TOKEN = "921bc5e3a1bac45c27da39e992f09da7";
	public static final String ACTION_DAILY_SHARE_SUGGESTION = PACKAGE_NAME + ".util.ACTION_DAILY_SHARE_SUGGESTION";
	public static final String EXTRA_SHARE_TO_NUMBER = PACKAGE_NAME + "EXTRA_SHARE_TO_NUMBER";
	public static final boolean SHARE_SUGGESTION_BOOLEAN = false;
	public static final String EXTRA_SHARE_SUGGESTION_ID = PACKAGE_NAME + "EXTRA_SHARE_SUGGESTION_ID";
	public static final String EXTRA_TAB_ID = PACKAGE_NAME + "EXTRA_TAB_ID";
	public static final String ACTION_SHARE_STATE_CHANGED = PACKAGE_NAME + ".util.ACTION_SHARE_STATE_CHANGED";
	public static final String EXTRA_MESSAGE_POSITION = PACKAGE_NAME + "EXTRA_MESSAGE_POSITION";
	public static final String ACTION_RECEIVE_SHARED_MESSAGES = PACKAGE_NAME + ".util.ACTION_RECEIVE_SHARED_MESSAGES";
	public static final String EXTRA_SHARED_SENDER = PACKAGE_NAME + "EXTRA_SHARED_SENDER";
	public static final String EXTRA_SHARED_ADDRESS = PACKAGE_NAME + "EXTRA_SHARED_ADDRESS";
	public static final String ACTION_INVITE_FRIEND = PACKAGE_NAME + ".util.ACTION_INVITE_FRIEND";
	public static final String  ACTION_CONFIRM_FRIEND= PACKAGE_NAME + ".util.ACTION_CONFIRM_FRIEND";
	public static final String EXTRA_USER_ID = PACKAGE_NAME + "EXTRA_USER_ID";
	public static final String EXTRA_SHARED_NAME = PACKAGE_NAME + "EXTRA_SHARED_NAME";
	public static final String EXTRA_SHARED_CONFIDANTE = PACKAGE_NAME + "EXTRA_SHARED_CONFIDANTE";
	public static final int MESSAGE_TYPE_SENT_COMMENT = "sentcomment".hashCode();
	public static final int MESSAGE_TYPE_RECEIVED_COMMENT = "receivedcomment".hashCode();
	public static final String ACTION_FACEBOOK_PHOTO_OBTAINED =  PACKAGE_NAME + ".util.ACTION_FACEBOOK_PHOTO_OBTAINED";
	public static final String ACTION_DATABASE_UPDATE = PACKAGE_NAME + ".util.ACTION_DATABASE_UPDATE";
	
	
	
	
	


}
