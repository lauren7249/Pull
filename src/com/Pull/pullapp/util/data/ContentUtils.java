package com.Pull.pullapp.util.data;


import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.database.sqlite.SqliteWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Data;
import android.provider.Telephony;
import android.provider.Telephony.Mms;
import android.provider.Telephony.TextBasedSmsColumns;
import android.provider.Telephony.ThreadsColumns;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import com.Pull.pullapp.model.InitiatingData;
import com.Pull.pullapp.model.MMSMessage;
import com.Pull.pullapp.model.MessageParams;
import com.Pull.pullapp.model.SMSMessage;
import com.Pull.pullapp.util.Constants;
import com.jjoe64.graphview.CustomLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;
import com.jjoe64.graphview.LineGraphView;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class ContentUtils {
	public static CharSequence getInitials(String name, String number) {
		String initials = "";
		if(name==null || name.isEmpty()) return initials;
		if(name.equals(number)) return "??";
		String[] i = name.split(" ");
		for(String chunk : i) {
			if(chunk.length()<1) continue;
			initials = initials + chunk.substring(0,1);
		}
		return initials;
	}	

	public static String getContactDisplayNameByNumber(Context context, String number) {
		if(number == null || number.length()==0) return null;
		//Log.i("getContactDisplayNameByNumber number",  ": " + number);
		Uri uri = Uri.withAppendedPath(
				ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
				Uri.encode(number));
		String name = number;
		ContentResolver contentResolver = context.getContentResolver();
		Cursor contactLookup = null;
		if(uri!=null) try {
			contactLookup = contentResolver.query(uri, new String[] {
					BaseColumns._ID, ContactsContract.PhoneLookup.DISPLAY_NAME },
					null, null, null);			
			if (contactLookup != null && contactLookup.getCount() >0) {
				contactLookup.moveToNext();
				if(contactLookup.getColumnCount()>
				contactLookup.getColumnIndex(ContactsContract.Data.DISPLAY_NAME))
					name = contactLookup.getString(contactLookup
						.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
			}
		} finally {
			if (contactLookup != null) {
				contactLookup.close();
			}
		}
		return name;
	}	
	public static String getAddressFromID(Context context, String recipientId) {
		  if(recipientId.length()==0) return null;
		  String address;
		  address = ""; 
		  Cursor c = context.getContentResolver().query(Uri
				  .parse("content://mms-sms/canonical-addresses"), null, BaseColumns._ID 
				  + " = " + recipientId, null, null);	  
		  if  (c.moveToNext()) {
			  address = c.getString(c.getColumnIndexOrThrow(TextBasedSmsColumns.ADDRESS)).toString();	  
		  }
		  c.close();
		  return address;
	  }
	  public static Cursor getThreadsCursor(Context context) {
		  
		  Cursor c = context.getContentResolver().query(
				  Uri.parse("content://mms-sms/conversations?simple=true"), 
				  new String[]{ThreadsColumns._ID, ThreadsColumns.READ, 
					  ThreadsColumns.RECIPIENT_IDS, ThreadsColumns.DATE,
					  ThreadsColumns.SNIPPET},
					  ThreadsColumns._ID+" is not null " 
					  /*+ "and replace("+ 
					  ThreadsColumns.RECIPIENT_IDS + ",' ','')=" + ThreadsColumns.RECIPIENT_IDS*/,
					  null,ThreadsColumns.DATE+" DESC");		  
		 
		  return c;
	  }	
	  
	  public static void markRead(Cursor c) {
		  
	  }		  
	  public static String getNextThreadID(Context context, String recipient_ids) {
		  Uri threadIdUri = Uri.parse("content://mms-sms/threadID");
		  Uri.Builder builder = threadIdUri.buildUpon();
		  String[] recipients = recipient_ids.split(" ");
		  for(String recipient : recipients){
		      builder.appendQueryParameter("recipient", recipient);
		  }
		  Uri uri = builder.build();		  
		  double threadId = 0.0;
		  Cursor cursor = context.getContentResolver().query(uri, new String[]{BaseColumns._ID},
				  null, null, null);
		  if (cursor != null) {
		      try {
		          if (cursor.moveToFirst()) {
		              threadId = cursor.getLong(0);
		              }
		      } finally {
		              cursor.close();
		      }
		  }		
		  return Double.toString(threadId);
	  }
	  
		// User input validation
		private static Boolean isNumberValid(String contact)	{
			if (contact == null)	{
				return false;
			}
			boolean valid1 = PhoneNumberUtils.isGlobalPhoneNumber(contact);
			boolean valid2 = PhoneNumberUtils.isWellFormedSmsAddress(contact);
			if ((valid1 == true) && (valid2 == true))	{
				return true;
			}
			return false;
		}
		public static String makeNumberValid(String contact)	{
			if (contact == null)	{
				return null;
			}
			String number = null;
			number = PhoneNumberUtils.formatNumber(contact);
			Boolean valid = isNumberValid(number);
			if (valid)	{
				return number;
			}
			return null;
		}
	    public static String subtractCountryCode(String number) {
	    	if(number == null) return number;
	    	if(number.trim().length()<=10) return number;
	    	return number.substring(number.length()-10);
	    }	
	    
	    public static String addCountryCode(String number) {
	    	if(number == null) return number;
	    	number = PhoneNumberUtils.stripSeparators(number);
	    	if(number.length() == 0) return number;
	    	if(number.substring(0,1).equals("+")) return number.substring(1);
	    	if(number.trim().length()==10) return "1"+number;
	    	return number;
	    }		    
	    public static String addCountryCode(TelephonyManager tmgr, String number) {
	    	return addCountryCode(number);
	    }	

		public static String setChannel(TelephonyManager tmgr, String recipient) {
			return "phoneNumber"+addCountryCode(tmgr,recipient);
		}
		public static String setChannel(String recipient) {
			return "phoneNumber"+addCountryCode(recipient);
		}
		public static Cursor getMessagesCursor(Context mContext,
				String thread_id, String number, boolean isGroupMessage) {
			String querystring;
			if(thread_id == null && number!=null) {
				Log.i("thread id is null", number);
				querystring = 
				"REPLACE(REPLACE(REPLACE(REPLACE(" + TextBasedSmsColumns.ADDRESS + 
						",'(',''),')',''),' ',''),'-','') " 
						+ " in ('"+ 
		   				ContentUtils.subtractCountryCode(number) + "', '" +
		   				ContentUtils.addCountryCode(number) + "', '+" +
		   				ContentUtils.addCountryCode(number) + "', '" +
		   				number + "')" 
		   				+  " and " + TextBasedSmsColumns.TYPE + " in (" + 
		   				TextBasedSmsColumns.MESSAGE_TYPE_SENT 					
   				+  " , " + TextBasedSmsColumns.MESSAGE_TYPE_INBOX + ")"  ;					

			}
			else if(thread_id!=null){
				Log.i("thread id is here", thread_id);
				querystring = TextBasedSmsColumns.THREAD_ID + "=" + thread_id 
					+ " AND "
					+ TextBasedSmsColumns.TYPE + " in (" + 
			   				TextBasedSmsColumns.MESSAGE_TYPE_SENT 					
			   				+  " , " + TextBasedSmsColumns.MESSAGE_TYPE_INBOX + ") and " +
			   				TextBasedSmsColumns.ADDRESS + " is not null and " +
   				TextBasedSmsColumns.ADDRESS + " is not null and " + 
   				TextBasedSmsColumns.BODY + " is not null and " +
   				BaseColumns._ID + " is not null and " + 
   				TextBasedSmsColumns.READ + " is not null and " +
   				TextBasedSmsColumns.DATE + " is not null" ;				
				Log.i("thread id",thread_id);
			}
			else return null;
			String[] variables = new String[]{"'sent' as category",
	        		TextBasedSmsColumns.TYPE,TextBasedSmsColumns.BODY,
	        		BaseColumns._ID, TextBasedSmsColumns.ADDRESS, TextBasedSmsColumns.READ,
	        		TextBasedSmsColumns.DATE, TextBasedSmsColumns.THREAD_ID};

			Cursor messages_cursor = mContext.getContentResolver().query(Telephony.Sms.CONTENT_URI,
					variables,querystring ,null,TextBasedSmsColumns.DATE);	   
			if(messages_cursor!=null && messages_cursor.getCount()==0 && isGroupMessage) {
				//Log.i("get messages cursor","adding an empty row");
				MatrixCursor matrixCursor = new MatrixCursor(variables);
				matrixCursor.addRow(new Object[] {"sent","-1","","-1","","-1","-1",thread_id});	
				MergeCursor mergeCursor = new MergeCursor(new Cursor[] { matrixCursor, messages_cursor });
				return mergeCursor;
			}
			//Log.i("messages_cursor size", " " + messages_cursor.getCount());
	        return messages_cursor;
		}		
		

	       public static String getOrCreateThreadId(
	                Context context, String[] numbers) {
	            Uri THREAD_ID_CONTENT_URI = Uri.parse(
	                    "content://mms-sms/threadID");
				Uri.Builder uriBuilder = THREAD_ID_CONTENT_URI.buildUpon();

	            for (String recipient : numbers) {
	                uriBuilder.appendQueryParameter("recipient", recipient);
	            }

	            Uri uri = uriBuilder.build();

	            String[] ID_PROJECTION = { BaseColumns._ID };
				Cursor cursor = SqliteWrapper.query(context, context.getContentResolver(),
	                    uri, ID_PROJECTION, null, null, null);
	            if (cursor != null) {
	                try {
	                    if (cursor.moveToFirst()) {
	                        return Long.toString(cursor.getLong(0));
	                    } else {
	                    }
	                } finally {
	                    cursor.close();
	                }
	            }

	            throw new IllegalArgumentException("Unable to find or allocate a thread ID.");
	        }
	    
		public static Cursor getMMSCursor(Context mContext,String thread_id) {
			String querystring = Telephony.BaseMmsColumns.THREAD_ID + "=" + thread_id ;
			String[] vars = new String[]{ Telephony.BaseMmsColumns._ID, 
					Telephony.BaseMmsColumns.DATE, Telephony.BaseMmsColumns.MESSAGE_BOX,
					Telephony.BaseMmsColumns.READ};
			Cursor messages_cursor = mContext.getContentResolver().query(Telephony.Mms.CONTENT_URI,
					vars,querystring ,null,Telephony.BaseMmsColumns.DATE + " desc");	   
	        return messages_cursor;
		}			   
		
		public static String saveToInternalStorage(Context mContext, Bitmap bitmapImage, String name){
	        ContextWrapper cw = new ContextWrapper(mContext);
	         // path to /data/data/yourapp/app_data/imageDir
	        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
	        // Create imageDir
	        File mypath=new File(directory,name+"_.png");

	        FileOutputStream fos = null;
	        try {           

	            fos = new FileOutputStream(mypath);

	       // Use the compress method on the BitMap object to write image to the OutputStream
	            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
	            fos.close();
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	        return mypath.getAbsolutePath();
	    }
		public static String getInternalStoragePath(Context mContext, String name){
	        ContextWrapper cw = new ContextWrapper(mContext);
	         // path to /data/data/yourapp/app_data/imageDir
	        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
	        // Create imageDir
	        File mypath=new File(directory,name+"_.png");

	        return mypath.getAbsolutePath();
	    }		
		public static Bitmap getBitmapFromRedirectingURL(String src) {
		    try {
		        URL url = new URL(src);
		        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		        connection.connect();
		        connection.getInputStream();
		        URL redirectedURL = connection.getURL();
		        Bitmap myBitmap = getBitmapFromURL(redirectedURL);
		        return myBitmap;
		    } catch (IOException e) {
		        e.printStackTrace();
		        return null;
		    }
		}
		private static Bitmap getBitmapFromURL(URL redirectedURL) {
		    try {
		        HttpURLConnection connection = (HttpURLConnection) redirectedURL.openConnection();
		        connection.setDoInput(true);
		        connection.connect();
		        InputStream input = connection.getInputStream();
		        Bitmap myBitmap = BitmapFactory.decodeStream(input);
		        return myBitmap;
		    } catch (IOException e) {
		        e.printStackTrace();
		        return null;
		    }
		}
		public static String getFacebookPath(String facebookID) {
			return "http://graph.facebook.com/"+facebookID+"/picture?type=large";
		}
		public static Bitmap getBitmapFromPath(String path) {
			File imgFile = new  File(path);
			if(imgFile.exists()){

			    Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
			    return myBitmap;

			}
			return null;
		}
		public static byte[] getByteArray(Bitmap bmp) {
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
			byte[] byteArray = stream.toByteArray();
			return byteArray;
		}		
		

		public static Bitmap decodeSampledBitmapFromResource(String path,
		        int reqWidth, int reqHeight) {

		    // First decode with inJustDecodeBounds=true to check dimensions
		    final BitmapFactory.Options options = new BitmapFactory.Options();
		    options.inJustDecodeBounds = true;
		    BitmapFactory.decodeFile(path, options);

		    // Calculate inSampleSize
		    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

		    // Decode bitmap with inSampleSize set
		    options.inJustDecodeBounds = false;
		    return BitmapFactory.decodeFile(path, options);
		} 
		
		public static int calculateInSampleSize(
	            BitmapFactory.Options options, int reqWidth, int reqHeight) {
		    // Raw height and width of image
			
		    final int height = options.outHeight;
		    final int width = options.outWidth;
		    int inSampleSize = 1;
		    
		   // Log.i("calculateInSampleSize", "height: " + height + " width: " + width);
	
		    if (height > reqHeight || width > reqWidth) {
	
		        final int halfHeight = height / 2;
		        final int halfWidth = width / 2;
	
		        // Calculate the largest inSampleSize value that is a power of 2 and keeps both
		        // height and width larger than the requested height and width.
		        while ((halfHeight / inSampleSize) > reqHeight
		                && (halfWidth / inSampleSize) > reqWidth) {
		            inSampleSize *= 2;
		        }
		    }
	
		    return inSampleSize;
		}	
		
	/**	public static int calculateInSampleSizeMaintainRatio(
	            BitmapFactory.Options options, int reqWidth, int reqHeight) {

		    int ratio = Math.min(options.outWidth/reqWidth, options.outHeight/reqHeight);
		    int sampleSize = Integer.highestOneBit((int)Math.floor(ratio));
		    if(sampleSize == 0){
		        sampleSize = 1;
		    }    
		    return sampleSize;
		}	**/	
		public static class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
		    private final WeakReference<ImageView> imageViewReference;
		    private String path = "";
		    private int alt_id;
		    private Context mContext;
		    public BitmapWorkerTask(Context context, ImageView imageView, int alt_id) {
		        // Use a WeakReference to ensure the ImageView can be garbage collected
		        imageViewReference = new WeakReference<ImageView>(imageView);
		        this.alt_id = alt_id;
		        this.mContext = context;
		    }

		    // Decode image in background.
		    @Override
		    protected Bitmap doInBackground(String... params) {
		        path = params[0];
		        if (path!=null) {
		        	return ContentUtils.decodeSampledBitmapFromResource(path, 100, 100);
		        }
		        else {
		        	return ContentUtils.decodeSampledBitmapFromResource(mContext.getResources(), alt_id, 100, 100);
		        		    
		        }
		        		
		    }

		    // Once complete, see if ImageView is still around and set bitmap.
		    @Override
		    protected void onPostExecute(Bitmap bitmap) {
		        if (imageViewReference != null) {
		            final ImageView imageView = imageViewReference.get();
		            if (imageView != null && bitmap!=null) {
		                imageView.setImageBitmap(bitmap);
		            }
		        }
		    }
		}	
		  	
		public static void loadBitmap(Context context, String path, ImageView imageView, int alt_id) {
		    BitmapWorkerTask task = new BitmapWorkerTask(context, imageView, alt_id);
		    task.execute(path);
		}
		public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
		        int reqWidth, int reqHeight) {

		    // First decode with inJustDecodeBounds=true to check dimensions
		    final BitmapFactory.Options options = new BitmapFactory.Options();
		    options.inJustDecodeBounds = true;
		    BitmapFactory.decodeResource(res, resId, options);
		    
		    // Calculate inSampleSize
		    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

		    // Decode bitmap with inSampleSize set
		    options.inJustDecodeBounds = false;
		    return BitmapFactory.decodeResource(res, resId, options);
		}


		public static void deleteConversation(Context mContext, String threadID) {
			mContext.getContentResolver().delete(Uri.parse("content://sms/conversations/" + threadID),null,null);
			String where = TextBasedSmsColumns.THREAD_ID+"=?";
			String[] args = new String[] { threadID };
			mContext.getContentResolver().delete( Telephony.Sms.CONTENT_URI, where, args );
			mContext.getContentResolver().delete( Telephony.Mms.CONTENT_URI, where, args );
		}

		public static Cursor getMessageIDsCursor(Context mContext, String thread_id) {
	        String[] variables = new String[]{Telephony.BaseMmsColumns.THREAD_ID, 
	        		Telephony.BaseMmsColumns.CONTENT_TYPE, Telephony.BaseMmsColumns._ID,
	        		Telephony.BaseMmsColumns.DATE};

			Cursor messages_cursor = mContext.getContentResolver().query(Uri.parse("content://mms-sms/conversations/" + thread_id),
					variables,null ,null,Telephony.BaseMmsColumns.DATE);	      
			//Log.i("getMessageIDsCursor size", " " + messages_cursor.getCount());
	        return messages_cursor;
		}

		public static ArrayList<SMSMessage> getPreviousMessages(Context context, String thread_id,
				Long date, UserInfoStore store, int type) {
			SMSMessage c_message;
			ArrayList<SMSMessage> previous_messages = new ArrayList<SMSMessage>();
			Uri message_ids = Uri.parse("content://mms-sms/conversations/" + thread_id);
			Cursor cursor = context.getContentResolver().query(message_ids, 
					new String[]{"_id", "date", "normalized_date"},
					 "normalized_date<" + date, null, "normalized_date desc");
			//
			int next_type = 0;
			long next_date_normalized = 0;
			if(cursor!=null && cursor.moveToFirst()) {
				do {
					
					String c_message_id = cursor.getString(0);
					long c_date = cursor.getLong(1);
					long c_date_normalized = cursor.getLong(2);

					//previous message is mms
					if(c_date_normalized!=c_date){
						//
						c_message = ContentUtils
								.getMMSMessage(context,thread_id,c_message_id, store);
					} else {
						//Log.i("previous message id ",previous_message_id);
						c_message = ContentUtils
								.getSMSMessage(context,thread_id,c_message_id, store);
					}
					
					//we arent on the last prior message
					if(next_type != 0) {
						//the next prior message is from a 
						//different sender, so we will ignore the current message
						if(next_type != c_message.getType()) break;
						//the next prior message is from the same sender
						//but it is too much later to be considered the same message
						if(next_date_normalized>0 &&
								next_date_normalized-c_date_normalized > 
								Constants.MAX_HOURS_ELAPSED_BEFORE_REINITIATING 
								* Constants.MILLISECONDS_PER_HOUR) break;
					}
					
					if(c_message!=null) {
						c_message.setMessageID(c_message_id);
						previous_messages.add(0,c_message);
						
						//the last message was from the same user as the current message, so its all we need
						if(c_message.getType()==type) break;
						
						/**the last prior message was from the other user, so we may need to grab 
						 * more of their previous messages
						 */
						next_date_normalized = c_date_normalized;
						next_type = c_message.getType();						
					}
	
				} while(cursor.moveToNext());
			}
			if(cursor!=null) cursor.close();
			return previous_messages;
		}

		private static SMSMessage getSMSMessage(Context context,
				String thread_id, String _id, UserInfoStore store) {
			String querystring = TextBasedSmsColumns.THREAD_ID + "=" + thread_id 
					+ " AND " + BaseColumns._ID + "=" + _id + " AND "
					+ TextBasedSmsColumns.TYPE + " in (" + 
			   				TextBasedSmsColumns.MESSAGE_TYPE_SENT 					
			   				+  " , " + TextBasedSmsColumns.MESSAGE_TYPE_INBOX + ") and " +
			   				TextBasedSmsColumns.ADDRESS + " is not null and " +
   				TextBasedSmsColumns.ADDRESS + " is not null and " + 
   				TextBasedSmsColumns.BODY + " is not null and " +
   				BaseColumns._ID + " is not null and " + 
   				TextBasedSmsColumns.READ + " is not null and " +
   				TextBasedSmsColumns.DATE + " is not null" ;		
			String[] variables = new String[]{"'sent' as category",
	        		TextBasedSmsColumns.TYPE,TextBasedSmsColumns.BODY,
	        		BaseColumns._ID, TextBasedSmsColumns.ADDRESS, TextBasedSmsColumns.READ,
	        		TextBasedSmsColumns.DATE, TextBasedSmsColumns.THREAD_ID};

			Cursor c = context.getContentResolver().query(Telephony.Sms.CONTENT_URI,
					variables,querystring ,null,TextBasedSmsColumns.DATE);	   
			if(c!=null && c.moveToFirst()) {
				long date = c.getLong(6);
				int type = Integer.parseInt(c.getString(1).toString());
				String body = c.getString(2).toString();
		    	String address = c.getString(4).toString();
		    	//Log.i("SmsMessageId","SMSDATE"+date);
			
		    	SMSMessage message = new SMSMessage(date, body, address, store.getName(address), 
		    			type, store, ParseUser.getCurrentUser().getUsername());	
		    	c.close();
				return message;
			}
			return null;
		}

		private static MMSMessage getMMSMessage(Context context,
				String thread_id, String _id, UserInfoStore store) {
			String querystring = Telephony.BaseMmsColumns.THREAD_ID + "=" + thread_id 
				+ " and " + Telephony.BaseMmsColumns._ID + "=" + _id ;
			String[] vars = new String[]{ Telephony.BaseMmsColumns._ID, 
					Telephony.BaseMmsColumns.DATE, Telephony.BaseMmsColumns.MESSAGE_BOX,
					Telephony.BaseMmsColumns.MESSAGE_TYPE};
			Cursor messages_cursor = context.getContentResolver().query(Telephony.Mms.CONTENT_URI,
					vars,querystring ,null,Telephony.BaseMmsColumns.DATE + " desc");
			if(messages_cursor.moveToFirst()) {
				MMSMessage m = getNextMMSMessage(context, messages_cursor, store, false);
				messages_cursor.close();
				return m;
			} 
			return null;
		}		
		
		public static MMSMessage getNextMMSMessage(Context context, Cursor mCursor, 
				UserInfoStore store, boolean updateReadStatus) {
			String mmsId = mCursor.getString(mCursor.getColumnIndexOrThrow(Telephony.BaseMmsColumns._ID));
			long date = 1000 * mCursor.getLong(mCursor.getColumnIndexOrThrow(Telephony.BaseMmsColumns.DATE));
			int m_type = mCursor.getInt(mCursor.getColumnIndex(Telephony.BaseMmsColumns.MESSAGE_BOX));
			String address = getAddressNumber(context, Integer.parseInt(mmsId));
			if(address==null || mmsId == null) return null;
			if(updateReadStatus) {
				String read = mCursor.getString(mCursor.getColumnIndex(Telephony.BaseMmsColumns.READ));
		    	if(!mmsId.equals("") && read.equals("0")) {
		        	ContentValues values = new ContentValues();
		    		values.put(Telephony.BaseMmsColumns.READ,true);
		    		context.getContentResolver().update(Telephony.Mms.CONTENT_URI,
		    				values, Telephony.BaseMmsColumns._ID+"="+mmsId, null);	
		    	}		
			}
			MMSMessage m = new MMSMessage(date, "", address, store.getName(address), m_type, 
					store, ParseUser.getCurrentUser().getUsername());
			m.setMessageID(mmsId);
			String selectionPart = "mid=" + mmsId;
			Uri uri = Uri.parse("content://mms/part");
			Cursor cursor = context.getContentResolver().query(uri, null,
			    selectionPart, null, null);
			if (cursor.moveToFirst()) {
			    do {
			        String partId = cursor.getString(cursor.getColumnIndex("_id"));
			        String type = cursor.getString(cursor.getColumnIndex("ct"));
			        if ("text/plain".equals(type)) {
			            String data = cursor.getString(cursor.getColumnIndex("_data"));
			            String body = "";
			            if (data != null) {
			                // implementation of this method below
			                body = getMmsText(context,partId);
			            } else {
			                body = cursor.getString(cursor.getColumnIndex("text"));
			            }
			           // Log.i("body",body);
			           if(body!=null) m.setMessage(body);
			        }
			        else if ("image/jpeg".equals(type) || "image/bmp".equals(type) ||
			                "image/gif".equals(type) || "image/jpg".equals(type) ||
			                "image/png".equals(type)) {
			            Uri image_uri = getMmsImageUri(context, partId);
			            if(image_uri!=null) m.addImage(image_uri);
			        }		        
			    } while (cursor.moveToNext());
			}
			cursor.close();
			return m;
		}


		private static String getAddressNumber(Context context, int id) {
		//Log.i("getaddressnumber id",""+id);
		    String selectionAdd = new String("msg_id=" + id);
		    String uriStr = MessageFormat.format("content://mms/{0}/addr", id);
		    Uri uriAddress = Uri.parse(uriStr);
		    Cursor cAdd = context.getContentResolver().query(uriAddress, null,
		        selectionAdd, null, null);
		    String name = null;
		    if (cAdd!=null && cAdd.moveToFirst()) {
		        do {
		            String number = cAdd.getString(cAdd.getColumnIndex("address"));
		            if (number != null) {
		                try {
		                    Long.parseLong(number.replace("-", ""));
		                    name = number;
		                } catch (NumberFormatException nfe) {
		                    if (name == null) {
		                        name = number;
		                    }
		                }
		            }
		        } while (cAdd.moveToNext());
		    }
		    if (cAdd != null) {
		        cAdd.close();
		    }
		    return name;
		}	
		private static Uri getMmsImageUri(Context context, String _id) {
		    Uri partURI = Uri.parse("content://mms/part/" + _id);
		    return partURI;
		}	
		private static String getMmsText(Context context, String id) {
		    Uri partURI = Uri.parse("content://mms/part/" + id);
		    InputStream is = null;
		    StringBuilder sb = new StringBuilder();
		    try {
		        is = context.getContentResolver().openInputStream(partURI);
		        if (is != null) {
		            InputStreamReader isr = new InputStreamReader(is, "UTF-8");
		            BufferedReader reader = new BufferedReader(isr);
		            String temp = reader.readLine();
		            while (temp != null) {
		                sb.append(temp);
		                temp = reader.readLine();
		            }
		        }
		    } catch (IOException e) {}
		    finally {
		        if (is != null) {
		            try {
		                is.close();
		            } catch (IOException e) {}
		        }
		    }
		    return sb.toString();
		}

		public static boolean isQuestion(String message) {
			if(message == null || message.isEmpty()) return false;
			if( message.contains("?")) return true;
			String[] chunks = message.toLowerCase().replaceAll("[^a-zA-Z0-9]", "").split(" ");
			String firstWord = chunks[0];
			String[] questionWords = new String[]{"what","who","why","how","is","when"};
			if(Arrays.asList(questionWords).contains(firstWord)) return true;
			String[] maybeQuestionWords = new String[]{"shall","was","wasnt","are","arent",
					"did","didnt","should","shouldnt","were","werent","has","hasnt",
					"do","dont","have","havent"};
			if(Arrays.asList(maybeQuestionWords).contains(firstWord)) {
				int nextWordIndex = Arrays.asList(maybeQuestionWords).indexOf(firstWord) + 1;
				if(nextWordIndex>chunks.length-1) return false;
				String nextWord = chunks[nextWordIndex];
				String[] pronounWords = new String[]{"you","i","we","them","he","she","they","her","his",
						"it","their","yall"};
				if(Arrays.asList(pronounWords).contains(nextWord)) return true;
			}
			return false;
		}

		public static String getFullMessageBody(ArrayList<SMSMessage> previous_messages) {
			String full_message = "";
			for(SMSMessage m : previous_messages) {
				full_message = full_message + m.getMessage() + "\n ";
			}
			return full_message;
		}			
		
}
