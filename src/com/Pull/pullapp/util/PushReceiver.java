/*
 * Copyright (C) 2007-2008 Esmertec AG.
 * Copyright (C) 2007-2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.Pull.pullapp.util;

import static android.provider.Telephony.Sms.Intents.WAP_PUSH_DELIVER_ACTION;
import static android.provider.Telephony.Sms.Intents.WAP_PUSH_RECEIVED_ACTION;
import static com.google.android.mms.pdu_alt.PduHeaders.MESSAGE_TYPE_DELIVERY_IND;
import static com.google.android.mms.pdu_alt.PduHeaders.MESSAGE_TYPE_NOTIFICATION_IND;
import static com.google.android.mms.pdu_alt.PduHeaders.MESSAGE_TYPE_READ_ORIG_IND;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SqliteWrapper;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.provider.Telephony.Mms;
import android.provider.Telephony.Mms.Inbox;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.Pull.pullapp.MessageActivityCheckboxCursor;
import com.Pull.pullapp.R;
import com.android.mms.MmsConfig;
import com.android.mms.transaction.NotificationTransaction;
import com.android.mms.transaction.Transaction;
import com.android.mms.transaction.TransactionBundle;

import com.google.android.mms.ContentType;
import com.google.android.mms.MmsException;
import com.google.android.mms.pdu_alt.DeliveryInd;
import com.google.android.mms.pdu_alt.GenericPdu;
import com.google.android.mms.pdu_alt.NotificationInd;
import com.google.android.mms.pdu_alt.PduHeaders;
import com.google.android.mms.pdu_alt.PduParser;
import com.google.android.mms.pdu_alt.PduPersister;
import com.google.android.mms.pdu_alt.ReadOrigInd;

/**
 * Receives Intent.WAP_PUSH_RECEIVED_ACTION intents and starts the
 * TransactionService by passing the push-data to it.
 */
public class PushReceiver extends BroadcastReceiver {
    private static final String TAG = "PushReceiver";
    private static final boolean LOCAL_LOGV = true;

    private class ReceivePushTask extends AsyncTask<Intent,Void,Void> {
        private Context mContext;
        public ReceivePushTask(Context context) {
            mContext = context;
        }

        @Override
        protected Void doInBackground(Intent... intents) {
            Intent intent = intents[0];

            // Get raw PDU push-data from the message and parse it
            byte[] pushData = intent.getByteArrayExtra("data");
            PduParser parser = new PduParser(pushData);
            GenericPdu pdu = parser.parse();
     
            if (null == pdu) {
                Log.e(TAG, "Invalid PUSH data");
                return null;
            }

            PduPersister p = PduPersister.getPduPersister(mContext);
            ContentResolver cr = mContext.getContentResolver();
            int type = pdu.getMessageType();
            
            long threadId = -1;

            try {
            	Log.v(TAG,"type "+type);
                switch (type) {
                
                    case MESSAGE_TYPE_DELIVERY_IND: {

                    }
                    case MESSAGE_TYPE_READ_ORIG_IND: {
                        threadId = findThreadId(mContext, pdu, type);
                        if (threadId == -1) {
                            // The associated SendReq isn't found, therefore skip
                            // processing this PDU.
                            break;
                        }

                        Uri uri = p.persist(pdu, Inbox.CONTENT_URI, true,
                               true, null);
                        // Update thread ID for ReadOrigInd & DeliveryInd.
                        ContentValues values = new ContentValues(1);
                        values.put(Mms.THREAD_ID, threadId);
                        SqliteWrapper.update(mContext, cr, uri, values, null, null);
                        break;
                    }
                    case MESSAGE_TYPE_NOTIFICATION_IND: {
                        NotificationInd nInd = (NotificationInd) pdu;
                    	
                        if (MmsConfig.getTransIdEnabled()) {
                            byte [] contentLocation = nInd.getContentLocation();
                            Log.v(TAG,"MmsConfig.getTransIdEnabled() " + MmsConfig.getTransIdEnabled());
                            if ('=' == contentLocation[contentLocation.length - 1]) {
                                byte [] transactionId = nInd.getTransactionId();
                                byte [] contentLocationWithId = new byte [contentLocation.length
                                                                          + transactionId.length];
                                System.arraycopy(contentLocation, 0, contentLocationWithId,
                                        0, contentLocation.length);
                                System.arraycopy(transactionId, 0, contentLocationWithId,
                                        contentLocation.length, transactionId.length);
                                nInd.setContentLocation(contentLocationWithId);
                            }
                        }

                        if (!isDuplicateNotification(mContext, nInd)) {
                            // Save the pdu. If we can start downloading the real pdu immediately,
                            // don't allow persist() to create a thread for the notificationInd
                            // because it causes UI jank.
                            Uri uri = p.persist(pdu, Inbox.CONTENT_URI,
                                    !NotificationTransaction.allowAutoDownload(mContext),
                                    true,
                                    null);

                            // Start service to finish the notification transaction.
                            Intent svc = new Intent(mContext, TransactionService.class);
                            svc.putExtra(TransactionBundle.URI, uri.toString());
                            svc.putExtra(TransactionBundle.TRANSACTION_TYPE,
                                    Transaction.NOTIFICATION_TRANSACTION);
                            ComponentName cn = mContext.startService(svc);
                            Log.v(TAG,"pdu uri " + uri.toString());
                            Log.v(TAG,"ComponentName " + cn.toShortString());
                            
        					NotificationManager mNotificationManager = (NotificationManager) mContext
        							.getSystemService(Context.NOTIFICATION_SERVICE);
        					int icon;
        					String sender = pdu.getFrom().getString();
        				
        					//Log.v(TAG,"sender " + sender);
        					UserInfoStore store = new UserInfoStore(mContext);
        					String name = store.getName(sender);
        					icon = R.drawable.ic_launcher_gray;
        					NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
        							mContext).setSmallIcon(icon).setContentTitle(name)
        							.setContentText("MMS")
        							.setPriority(NotificationCompat.PRIORITY_LOW)
        							.setOnlyAlertOnce(true);
        					Intent ni = new Intent(mContext, MessageActivityCheckboxCursor.class);
        					//ni.putExtra(Constants.EXTRA_THREAD_ID,threadID);
        					ni.putExtra(Constants.EXTRA_THREAD_ID,threadId);
        			       // ni.putExtra(Constants.EXTRA_NUMBER,sender);
        					ni.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        					//ni.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        					ni.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        					PendingIntent pi = PendingIntent.getActivity(mContext, 0,
        							ni, PendingIntent.FLAG_CANCEL_CURRENT);
        					mBuilder.setContentIntent(pi);
        					mBuilder.setAutoCancel(true);
        					Notification notification = mBuilder.build();
        					notification.defaults|= Notification.DEFAULT_SOUND;
        					notification.defaults|= Notification.DEFAULT_LIGHTS;
        					notification.defaults|= Notification.DEFAULT_VIBRATE;		
        					mNotificationManager.notify(sender.hashCode(),notification);                            
                        } else if (LOCAL_LOGV) {
                            Log.v(TAG, "Skip downloading duplicate message: "
                                    + new String(nInd.getContentLocation()));
                        }
                        break;
                    }
                    default:
                        Log.e(TAG, "Received unrecognized PDU.");
                }
            } catch (MmsException e) {
                Log.e(TAG, "Failed to save the data from PUSH: type=" + type, e);
            } catch (RuntimeException e) {
                Log.e(TAG, "Unexpected RuntimeException.", e);
            }

            if (LOCAL_LOGV) {
                Log.v(TAG, "PUSH Intent processed.");
            }

            return null;
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
    	int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if ((intent.getAction().equals(WAP_PUSH_DELIVER_ACTION) || 
        		(intent.getAction().equals(WAP_PUSH_RECEIVED_ACTION) && 
        				currentapiVersion < android.os.Build.VERSION_CODES.KITKAT) )
                && ContentType.MMS_MESSAGE.equals(intent.getType())) {
            if (LOCAL_LOGV) {
                Log.v(TAG, "Received PUSH Intent: " + intent);
            }

            // Hold a wake lock for 5 seconds, enough to give any
            // services we start time to take their own wake locks.
            PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                                            "MMS PushReceiver");
            wl.acquire(5000);
            new ReceivePushTask(context).execute(intent);
            abortBroadcast();
        }
    }

    public static long findThreadId(Context context, GenericPdu pdu, int type) {
        String messageId;

        if (type == MESSAGE_TYPE_DELIVERY_IND) {
            messageId = new String(((DeliveryInd) pdu).getMessageId());
        } else {
            messageId = new String(((ReadOrigInd) pdu).getMessageId());
        }

        StringBuilder sb = new StringBuilder('(');
        sb.append(Mms.MESSAGE_ID);
        sb.append('=');
        sb.append(DatabaseUtils.sqlEscapeString(messageId));
        sb.append(" AND ");
        sb.append(Mms.MESSAGE_TYPE);
        sb.append('=');
        sb.append(PduHeaders.MESSAGE_TYPE_SEND_REQ);
        // TODO ContentResolver.query() appends closing ')' to the selection argument
        // sb.append(')');

        Cursor cursor = SqliteWrapper.query(context, context.getContentResolver(),
                            Mms.CONTENT_URI, new String[] { Mms.THREAD_ID },
                            sb.toString(), null, null);
        if (cursor != null) {
            try {
                if ((cursor.getCount() == 1) && cursor.moveToFirst()) {
                    return cursor.getLong(0);
                }
            } finally {
                cursor.close();
            }
        }

        return -1;
    }

    private static boolean isDuplicateNotification(
            Context context, NotificationInd nInd) {
        byte[] rawLocation = nInd.getContentLocation();
        if (rawLocation != null) {
            String location = new String(rawLocation);
            String selection = Mms.CONTENT_LOCATION + " = ?";
            String[] selectionArgs = new String[] { location };
            Cursor cursor = SqliteWrapper.query(
                    context, context.getContentResolver(),
                    Mms.CONTENT_URI, new String[] { Mms._ID },
                    selection, selectionArgs, null);
            if (cursor != null) {
                try {
                    if (cursor.getCount() > 0) {
                        // We already received the same notification before.
                        return true;
                    }
                } finally {
                    cursor.close();
                }
            }
        }
        return false;
    }
}
