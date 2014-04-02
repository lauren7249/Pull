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
	public static final String EXTRA_NAME = "NAME";
	public static final String EXTRA_READ = "READ";
	public static final String EXTRA_NUMBER = "NUMBER";
	public static final String EXTRA_THREAD_ID = "THREAD_ID";
	public static final String EXTRA_SET_OF_MESSAGES = "SET_OF_MESSAGES";
	
	public static String getPictureBucket() {
		return (PICTURE_BUCKET).toLowerCase(Locale.US);
	}
	
}
