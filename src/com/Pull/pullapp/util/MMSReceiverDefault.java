package com.Pull.pullapp.util;

import java.util.HashMap;
import java.util.HashSet;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.preference.PreferenceManager;
import android.provider.Telephony.Sms.Intents;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.android.mms.ContentType;
import com.google.android.mms.pdu_alt.EncodedStringValue;
import com.google.android.mms.pdu_alt.PduBody;
import com.google.android.mms.pdu_alt.PduParser;
import com.google.android.mms.pdu_alt.PduPart;
import com.google.android.mms.pdu_alt.DeliveryInd;
import com.google.android.mms.pdu_alt.GenericPdu;
import com.google.android.mms.pdu_alt.PduHeaders;
import com.google.android.mms.pdu_alt.MultimediaMessagePdu;

public class MMSReceiverDefault extends BroadcastReceiver {
	
	//TODO: initialize
	private TelephonyManager mTelephonyManager;
	private GenericPdu mGenericPdu;
	private ConnectivityManager mConnMgr;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		int currentapiVersion = android.os.Build.VERSION.SDK_INT;
		
		if (intent.getAction().equals(Intents.WAP_PUSH_DELIVER_ACTION) || 
			(intent.getAction().equals(Intents.WAP_PUSH_RECEIVED_ACTION) && currentapiVersion < android.os.Build.VERSION_CODES.KITKAT )) {

	        mConnMgr =  (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
	        byte[] rawPdu = intent.getByteArrayExtra("data");
	        if(rawPdu != null && rawPdu.length>0) {
	        	mGenericPdu = new PduParser(rawPdu).parse();
	            int type = mGenericPdu.getMessageType();
	            int status = ((DeliveryInd)mGenericPdu).getStatus();
	            if(status == PduHeaders.STATUS_RETRIEVED) {
	            	Log.i("generic pdu","status retrieved");
	            }
	        	//
		/**		// Large message might be broken into many.
				SmsMessage[] messages = new SmsMessage[pdus.length];
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < pdus.length; i++) {
					messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
					sb.append(messages[i].getMessageBody());
				}
				String sender = ContentUtils.addCountryCode(messages[0].getOriginatingAddress());
				long date = messages[0].getTimestampMillis();
				String message = sb.toString();	        
		        String twilioNumber = ContentUtils.addCountryCode(store.getTwilioNumber());
		        if(twilioNumber!=null && sender!=null && 
		        		ContentUtils.addCountryCode(sender).equals(twilioNumber)){
		        	abortBroadcast();
		        	if(message.equals(store.getVerificationCode())) {
		        	    intent = new Intent(Constants.ACTION_NUMBER_VERIFIED);
		        	    context.sendBroadcast(intent);		
		        	   // Log.i("received broadcast",Constants.ACTION_NUMBER_VERIFIED);
		        	}
		        	return;
		        }
		        Log.i("received broadcast",sender);
		       
				
				boolean receive = sharedPrefs.getBoolean("prefReceiveTexts", true);
				 Log.i("receive","is " + receive);
				if ((currentapiVersion >= android.os.Build.VERSION_CODES.KITKAT &&
						Telephony.Sms.getDefaultSmsPackage(context).equals(context.getPackageName())) ||
					(currentapiVersion < android.os.Build.VERSION_CODES.KITKAT && receive)){
					if(currentapiVersion < android.os.Build.VERSION_CODES.KITKAT ) 
						abortBroadcast();
					SimpleDateFormat dateFormat = new SimpleDateFormat(
							"yyyy-MM-dd'T'HH:mm'Z'"); // ISO 8601, Local time zone.
					dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

					String name = ContentUtils.getContactDisplayNameByNumber(context, sender);
					String threadID = ContentUtils.getThreadIDFromNumber(context, sender);
					if(threadID == null || threadID.length()==0) {
						threadID = ContentUtils.getNextThreadID(context,sender);
						Log.i("thread id",threadID);
					}
					pushMessage(context,message,sender,threadID,date);		
					SMSMessage m = new SMSMessage(date, message, sender, 
							name, TextBasedSmsColumns.MESSAGE_TYPE_INBOX, store, 
							ParseUser.getCurrentUser().getUsername());
					try {
						m.saveToParse();
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					NotificationManager mNotificationManager = (NotificationManager) context
							.getSystemService(Context.NOTIFICATION_SERVICE);
					int icon;
					icon = R.drawable.ic_launcher_gray;
					NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
							context).setSmallIcon(icon).setContentTitle(name)
							.setContentText(message)
							.setPriority(NotificationCompat.PRIORITY_LOW)
							.setOnlyAlertOnce(true);
					Intent ni = new Intent(context, MessageActivityCheckboxCursor.class);
					//ni.putExtra(Constants.EXTRA_THREAD_ID,threadID);
					ni.putExtra(Constants.EXTRA_NAME,name);
			        ni.putExtra(Constants.EXTRA_NUMBER,sender);
					ni.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					//ni.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
					ni.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					PendingIntent pi = PendingIntent.getActivity(context, 0,
							ni, PendingIntent.FLAG_CANCEL_CURRENT);
					mBuilder.setContentIntent(pi);
					mBuilder.setAutoCancel(true);
					Notification notification = mBuilder.build();
					notification.defaults|= Notification.DEFAULT_SOUND;
					notification.defaults|= Notification.DEFAULT_LIGHTS;
					notification.defaults|= Notification.DEFAULT_VIBRATE;		
					mNotificationManager.notify(sender.hashCode(),notification);
				}
			}**/
			return;
		}
	}
	}
	private void processPduAttachments() throws Exception {
	    if (mGenericPdu instanceof MultimediaMessagePdu) {
	        PduBody body = ((MultimediaMessagePdu) mGenericPdu).getBody();
	        if (body != null) {
	            int partsNum = body.getPartsNum();
	            for (int i = 0; i < partsNum; i++) {
	                try {
	                    PduPart part = body.getPart(i);
	                    if (part == null || part.getData() == null || part.getContentType() == null || part.getName() == null)
	                        continue;
	                    String partType = new String(part.getContentType());
	                    String partName = new String(part.getName());
	                    Log.d("tag","Part Name: " + partName);
	                    Log.d("tag","Part Type: " + partType);
	                    if (ContentType.isTextType(partType)) {
	                    } else if (ContentType.isImageType(partType)) {
	                    } else if (ContentType.isVideoType(partType)) {
	                    } else if (ContentType.isAudioType(partType)) {
	                    }
	                } catch (Exception e) {
	                    e.printStackTrace();
	                    // Bad part shouldn't ruin the party for the other parts
	                }
	            }
	        }
	    } else {
	        Log.d("TAG","Not a MultimediaMessagePdu PDU");
	    }
	}
	
	private void loadRecipients(int addressType, HashSet<String> recipients, HashMap<Integer, EncodedStringValue[]> addressMap, boolean excludeMyNumber) {
	    EncodedStringValue[] array = addressMap.get(addressType);
	    if (array == null) {
	        return;
	    }
	    // If the TO recipients is only a single address, then we can skip loadRecipients when
	    // we're excluding our own number because we know that address is our own.
	    if (excludeMyNumber && array.length == 1) {
	        return;
	    }
	    String myNumber = excludeMyNumber ? mTelephonyManager.getLine1Number() : null;
	    for (EncodedStringValue v : array) {
	        if (v != null) {
	            String number = v.getString();
	            if ((myNumber == null || !PhoneNumberUtils.compare(number, myNumber)) && !recipients.contains(number)) {
	                // Only add numbers which aren't my own number.
	                recipients.add(number);
	            }
	        }
	    }
	}	
    /**private static final int[] ADDRESS_FIELDS = new int[] {
        PduHeaders.BCC,
        PduHeaders.CC,
        PduHeaders.FROM,
        PduHeaders.TO
};
	private HashSet<String> getRecipients(GenericPdu pdu) {

	    PduHeaders header = pdu.getPduHeaders();
	    HashMap<Integer, EncodedStringValue[]> addressMap = new HashMap<Integer, EncodedStringValue[]>(ADDRESS_FIELDS.length);
	    for (int addrType : ADDRESS_FIELDS) {
	        EncodedStringValue[] array = null;
	        if (addrType == PduHeaders.FROM) {
	            EncodedStringValue v = header.getEncodedStringValue(addrType);
	            if (v != null) {
	                array = new EncodedStringValue[1];
	                array[0] = v;
	            }
	        } else {
	            array = header.getEncodedStringValues(addrType);
	        }
	        addressMap.put(addrType, array);
	    }
	    HashSet<String> recipients = new HashSet<String>();
	    loadRecipients(PduHeaders.FROM, recipients, addressMap, false);
	    loadRecipients(PduHeaders.TO, recipients, addressMap, true);
	    return recipients;
	}	
	
	private boolean beginMmsConnectivity() {
	    try {
	        int result = mConnMgr.startUsingNetworkFeature(ConnectivityManager.TYPE_MOBILE, "enableMMS");
	        NetworkInfo info = mConnMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE_MMS);
	        boolean isAvailable = info != null && info.isConnected() && result == PhoneConstants.APN_ALREADY_ACTIVE && 
	        		!PhoneConstants.REASON_VOICE_CALL_ENDED.equals(info.getReason());
	        return isAvailable;
	    } catch(Exception e) {
	        return false;
	    }
	}	**/
}
