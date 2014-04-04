/*
 * This file is part of No Stranger SMS.
 *
 * No Stranger SMS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * No Stranger SMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with No Stranger SMS.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.Pull.pullapp.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.Pull.pullapp.MessageActivityCheckbox;
import com.Pull.pullapp.ThreadsListActivity;
import com.Pull.pullapp.R;
import com.Pull.pullapp.R.drawable;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.provider.Telephony.Sms.Intents;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

public class SMSReceiver extends BroadcastReceiver {
	public SMSReceiver() {
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction()
				.equals(Intents.SMS_RECEIVED_ACTION)) {
			Bundle bundle = intent.getExtras();
			if (bundle != null) {
				// Get SMS objects.
				Object[] pdus = (Object[]) bundle.get("pdus");
				if (pdus.length == 0) {
					return;
				}
				// Large message might be broken into many.
				SmsMessage[] messages = new SmsMessage[pdus.length];
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < pdus.length; i++) {
					messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
					sb.append(messages[i].getMessageBody());
				}
				String sender = messages[0].getOriginatingAddress();
				if (true) {
					// Contact not in address book: log message and don't let it through.
					String message = sb.toString();
					// Prevent other broadcast receivers from receiving broadcast.
					//abortBroadcast();
					SimpleDateFormat dateFormat = new SimpleDateFormat(
							"yyyy-MM-dd'T'HH:mm'Z'"); // ISO 8601, Local time zone.
					dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
					
					/*String date = dateFormat.format(new Date()); // Current time in UTC.
					String name = ContentUtils.getContactDisplayNameByNumber(context, sender);
					String threadID = ContentUtils.getThreadIDFromNumber(context, sender);
					if(threadID == null || threadID.length()==0) {
						threadID = ContentUtils.getNextThreadID(context,sender);
						Log.i("thread id",threadID);
					}
					pushMessage(context,message,sender,threadID);
					NotificationManager mNotificationManager = (NotificationManager) context
							.getSystemService(Context.NOTIFICATION_SERVICE);
					int icon;
					icon = R.drawable.ic_launcher;
					NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
							context).setSmallIcon(icon).setContentTitle(name)
							.setContentText(message)
							.setPriority(NotificationCompat.PRIORITY_LOW)
							.setOnlyAlertOnce(true);
					// TODO: Optional light notification.
					Intent ni = new Intent(context, MessageActivity.class);
					ni.putExtra("NUMBER",sender);
					ni.putExtra("NAME",name);
					ni.putExtra("THREAD_ID",threadID);
					ni.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
							| Intent.FLAG_ACTIVITY_SINGLE_TOP);
					PendingIntent pi = PendingIntent.getActivity(context, 0,
							ni, 0);
					mBuilder.setContentIntent(pi);
					mBuilder.setAutoCancel(true);
					mNotificationManager.notify(777, mBuilder.build());*/
				}
			}
		}
	}
	private void pushMessage(Context context, String message, String number, String thread_id) {

		ContentValues values = new ContentValues(7);
		values.put("address", number);
		values.put("read", false);
		values.put("subject", "");
		values.put("body", message);
		values.put("thread_id", thread_id);
		
		Uri uri = Uri.parse("content://sms/inbox");
		context.getContentResolver().insert(uri, values);		

	}	

}
