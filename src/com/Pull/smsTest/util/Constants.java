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
package com.Pull.smsTest.util;

import java.util.Locale;

import android.content.Intent;

public class Constants {
    //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    // This sample App is for demonstration purposes only.
    // It is not secure to embed your credentials into source code.
    // Please read the following article for getting credentials
    // to devices securely.
    // http://aws.amazon.com/articles/Mobile/4611615499399490
    //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	
	public static final String ACCESS_KEY_ID = "AKIAJCCNLR3ZM3G6QCPQ";
	public static final String SECRET_KEY = "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";
	
	public static final String PICTURE_BUCKET = "pull-lauren-julia-hackday1";
	public static final String PICTURE_NAME = "NameOfThePicture";
	public static final boolean DEBUG = false;
	public static final boolean LOG_SMS = false;
	public static final String EXTRA_NAME = "NAME";
	public static final String EXTRA_READ = "READ";
	public static final String EXTRA_NUMBER = "NUMBER";
	public static final String EXTRA_THREAD_ID = "THREAD_ID";
	public static final String EXTRA_SET_OF_MESSAGES = "SET_OF_MESSAGES";
	
	public static String getPictureBucket() {
		return (PICTURE_BUCKET).toLowerCase(Locale.US);
	}
    public static final String PREFS = "omnitor.prefs";
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
    
    public static final String ACTION_INITIAL_UPLOAD = "com.Pull.smsTest.util.ACTION_INITIAL_UPLOAD";
    public static final String ACTION_CHECK_OUT_SMS = "com.Pull.smsTest.util.ACTION_CHECK_OUT_SMS";
    public static final String ACTION_UPLOAD_LOGS = "com.Pull.smsTest.util.ACTION_UPLOAD_LOGS";
    public static final String ACTION_LOG_DATA = "com.Pull.smsTest.util.ACTION_LOG_DATA";
    public static final String ACTION_SEND_DELAYED_TEXT = "com.Pull.smsTest.util.ACTION_SEND_DELAYED_TEXT";
    public static final String ACTION_SMS_DELIVERED = "com.Pull.SMS_DELIVERED";	
    public static final String ACTION_SMS_OUTBOXED = "com.Pull.SMS_OUTBOXED";	
    public static final String ACTION_SMS_UNOUTBOXED = "com.Pull.SMS_UNOUTBOXED";	
    
    public static final String TYPE_IN_SMS = "in_sms";
    public static final String TYPE_OUT_SMS = "out_sms";
    public static final String TYPE_IN_CALL = "in_call";
    public static final String TYPE_OUT_CALL = "out_call";
    public static final String TYPE_DATA = "data";
    
    public static final String EXTRA_RECIPIENT = "RECIPIENT";
    public static final String EXTRA_MESSAGE_BODY = "MESSAGE_BODY";
    public static final String EXTRA_TIME_LAUNCHED = "TIME_LAUNCHED";
	public static final String EXTRA_TIME_SCHEDULED_FOR = "TIME_SCHEDULED_FOR";
	
    

}
