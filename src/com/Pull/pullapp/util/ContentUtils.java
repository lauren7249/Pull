package com.Pull.pullapp.util;


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

		public static boolean isInitiating(String thread_id, String message_id, Context context, 
				Long date, UserInfoStore store, SMSMessage message) {
			DatabaseHandler dh = new DatabaseHandler(context);
			InitiatingData initiatingData = dh
					.getInitiatingRecord(thread_id,message_id, context, date, store, message);
			dh.close();
			return initiatingData.isInitiating();

		}		
		public static boolean isInitiating(long date, int type, String body,
				long previous_date, int previous_type, String previous_body) {
			boolean initiating = false, retexting=false;
     		if(previous_type==type) retexting = true;
     		long milliseconds_elapsed = (long)(date-previous_date);
     		long seconds_elapsed = (long) (milliseconds_elapsed*0.001);
     		long minutes_elapsed = (long) (seconds_elapsed*0.016666666667);
     		float hours_elapsed = (float) (minutes_elapsed*0.016666666667);
     		if(retexting && hours_elapsed > 0.167) initiating=true;
     		else if(!retexting) {
     			if (hours_elapsed > 24) initiating=true;
     			else if(hours_elapsed > 8 && !previous_body.contains("?")) initiating = true;
     		}
     		return initiating;
		}
	

		public static HashMap<String, TreeMap<Long, Float>> getDataSeries(
				Cursor messages_cursor, Context context) {
			boolean initiating = false;
			int previous_type = 0;
			long previous_date = 0;
			long my_previous_initiation_date =0, their_previous_initiation_date=0;
			String previous_body = null;
			int me=1, them=1, responses_mine=0, responses_theirs=0, my_characters=0, their_characters=0;
			float me_freq =0 , them_freq = 0, start_date = 0,
					my_total_minutes = 0, their_total_minutes = 0;
			int initiation_count=0;			
			long date;
			float balance, minutes_elapsed, my_average_response_time = 0, their_average_response_time = 0;
			int num = messages_cursor.getCount();
			HashMap<String,TreeMap<Long,Float>> data = new HashMap<String,TreeMap<Long,Float>>();
			
			TreeMap<Long,Float> initiation_me = new TreeMap<Long,Float>();
			TreeMap<Long,Float> initiation_them = new TreeMap<Long,Float>();
			TreeMap<Long,Float> initiation_ratio = new TreeMap<Long,Float>();	
			
			TreeMap<Long,Float> response_times_me = new TreeMap<Long,Float>();
			TreeMap<Long,Float> response_times_them = new TreeMap<Long,Float>();
			TreeMap<Long,Float> response_time_ratio = new TreeMap<Long,Float>();

			TreeMap<Long,Float> volume_me = new TreeMap<Long,Float>();
			TreeMap<Long,Float> volume_them = new TreeMap<Long,Float>();
			TreeMap<Long,Float> volume_ratio = new TreeMap<Long,Float>();
			for (int i=0; i<num; i++) {
				messages_cursor.moveToPosition(i);
				date = messages_cursor.getLong(6);
				if(i==0) {
					start_date = date;
				}
				String body = messages_cursor.getString(2).toString();
				String message_id = messages_cursor.getString(3).toString();
				int type = Integer.parseInt(messages_cursor.getString(1).toString());
				if(previous_body!=null && previous_date>0) {

			  		initiating = isInitiating(date, type, body, previous_date, previous_type, previous_body);
					/*	message = new SMSMessage(date, body, address, store.getName(address), 
			    			type, store, ParseUser.getCurrentUser().getUsername());
					 * initiating = ContentUtils.isInitiating(thread_id, message_id, context, 
							date, store, message);*/
				} 
				long previous_initiation_date = 0;
				int previous_me = 0;
				int previous_them = 0;
				if(initiating) {
					if(type==TextBasedSmsColumns.MESSAGE_TYPE_SENT) {
						me++;
					}
					else {
						them++;
					}
				initiation_count++;
				
				} 
				else if(previous_type != type && previous_date>0) { // not a retext and not an initiation, therefore a response
		     		long milliseconds_elapsed = (long)(date-previous_date);
		     		long seconds_elapsed = (long) (milliseconds_elapsed*0.001);
		     		minutes_elapsed = (long) (seconds_elapsed*0.016666666667);
				  if(type==TextBasedSmsColumns.MESSAGE_TYPE_SENT) {
					  my_total_minutes = (float) my_total_minutes + minutes_elapsed;
					  responses_mine++;
					  my_average_response_time = my_total_minutes/(float)responses_mine;
					  if(my_average_response_time>0) response_times_me.put(date, my_average_response_time);					  
				  }
				  else {
					their_total_minutes = (float) their_total_minutes + minutes_elapsed;
					responses_theirs++;
					their_average_response_time = their_total_minutes/(float)responses_theirs;
					if(their_average_response_time>0) response_times_them.put(date, their_average_response_time);					
				  }
				  if(their_average_response_time>0 && my_average_response_time>0)
				  response_time_ratio.put(date, (float) their_average_response_time/(float) my_average_response_time);
			  }
			  if(initiation_count>2) {

				  if(them>me) balance=((float)them/(float)me)-1;
				  else balance=-(((float)me/(float)them)-1);
				  
				  float their_period;
				  float my_period;
				  if(their_previous_initiation_date>0) {
					  their_period = date-their_previous_initiation_date;
				  } else their_period = date-start_date;

				  if(my_previous_initiation_date>0) {
					  my_period = date-my_previous_initiation_date;
				  } else my_period = date-start_date;
				
				  me_freq = (float)(me-1)/(date-start_date);
				  them_freq = (float)(them-1)/(date-start_date);

				  initiation_me.put(date, me_freq);
				  initiation_them.put(date, them_freq);
				  initiation_ratio.put(date, balance);

			  }		
			  if(initiating) {
				  if(type==TextBasedSmsColumns.MESSAGE_TYPE_SENT) {
					  my_previous_initiation_date = date;
					  previous_me = me;
				  }
				  else {
					  their_previous_initiation_date = date;
					  previous_them = them;
				  }
				  initiation_count++;
			  }
			  			  
			  previous_date = date;
			  previous_body = body;
			  previous_type = type;
			}
			
			date = System.currentTimeMillis();
			if(them>me) balance=((float)them/(float)me)-1;
			else balance=-(((float)me/(float)them)-1);			
			me_freq = (float)(me-1)/(date-start_date);
			them_freq = (float)(them-1)/(date-start_date);			
			initiation_me.put(date, me_freq);
			initiation_them.put(date, them_freq);
			initiation_ratio.put(date, balance);			
			
			data.put(Constants.GRAPH_CONTACT_INIT_FREQ_THEM, initiation_them);
			data.put(Constants.GRAPH_CONTACT_INIT_FREQ_ME,initiation_me);
			data.put(Constants.GRAPH_CONTACT_INIT_FREQ_RATIO,initiation_ratio);
			 
			data.put(Constants.GRAPH_RESPONSE_TIME_THEM,response_times_them);
			data.put(Constants.GRAPH_RESPONSE_TIME_ME, response_times_me);
			data.put(Constants.GRAPH_RESPONSE_TIME_RATIO,response_time_ratio);	
			
			return data;
		}
		
		@Deprecated
		public static void addGraphs(Activity activity,
				LinearLayout mGraphView, String original_name,
				Cursor messages_cursor, Context mContext) {
			HashMap<String,TreeMap<Long,Float>> data = getDataSeries(messages_cursor, mContext);
			GraphView[] graphViews = new GraphView[data.size()];
			String title = "";
			for(int i=0; i<data.size(); i++) {
				if(i%3==0) title = original_name;
				else if(i%3==1) title = "You";
				else if(i%3==2) title = "";
				GraphView graphView = new LineGraphView(
					    activity
					    , title
					);
					// add data
				try {
					graphView.addSeries(new GraphViewSeries(treemapToGraphSeries(data.get(i))));
				} catch(RuntimeException e) {
					continue;
				}
				graphView.setCustomLabelFormatter(new CustomLabelFormatter() {
					  @Override
					  public String formatLabel(double value, boolean isValueX) {
					    if (isValueX) {
					    	
					    	Date d = new Date((long) value);
					    	DateFormat dateFormatter = DateFormat.getDateInstance(DateFormat.DEFAULT, Locale.ENGLISH);
					    	String dateOut = dateFormatter.format(d);
					    	//return dateOut;
					    	return "";
					    }
					    return "";
					  }
					});		
				graphView.setLayoutParams(new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,1f));
				graphViews[i] = graphView;
				mGraphView.addView(graphViews[i]);	 
			}
		}	
		
		private static GraphViewData[] treemapToGraphSeries(
				TreeMap<Long, Float> hashMap) {
			GraphViewData[] k = new GraphViewData[hashMap.size()];
			int i = 0;
			for(Entry<Long, Float> entry : hashMap.entrySet()) {
				k[i] = new GraphViewData(entry.getKey(),entry.getValue());
				i++;
			}
			return k;
			
		}

		public static ArrayList<View> getGraphs(Activity activity,String original_name, HashMap<String, TreeMap<Long,Float>> data,
				String[] series_names, String title) {
			
				ArrayList<View> views = new ArrayList<View>();
				GraphView graphView = new LineGraphView(
					    activity
					    , title
					);

				try {
					String ratio_name = series_names[2];
					TreeMap <Long,Float> ratios = data.get(ratio_name);
					Long lastDate = ratios.lastKey();
					Float lastRatio = ratios.get(lastDate);
					String response_Time_balance_overall;
					if(ratio_name.equals(Constants.GRAPH_RESPONSE_TIME_RATIO)) {
						//if(lastRatio<.9) response_Time_balance_overall = original_name + " generally responds to texts faster than you.";
					} else if(ratio_name.equals(Constants.GRAPH_CONTACT_INIT_FREQ_RATIO)) {
						
					}
					
					//Log.i("ratio",series_names[1] + " " + lastRatio);					
					graphView.addSeries(new GraphViewSeries(original_name,
							new GraphViewSeriesStyle(Color.rgb(251, 76, 60), 3), 
							treemapToGraphSeries(data.get(series_names[0]))));
					
					graphView.addSeries(new GraphViewSeries("You",
							new GraphViewSeriesStyle(Color.rgb(52, 185, 204), 3),
							treemapToGraphSeries(data.get(series_names[1]))));
				} catch(RuntimeException e) {
					TextView tv = new TextView(activity);
					tv.setText("Not enough messages for this graph -- check back later!");
					e.printStackTrace();
					views.add(tv);
					return views;
				}
				graphView.setShowLegend(true);
				graphView.setCustomLabelFormatter(new CustomLabelFormatter() {
					  @Override
					  public String formatLabel(double value, boolean isValueX) {
					    if (isValueX) {
					    	
					    	Date d = new Date((long) value);
					    	DateFormat dateFormatter = DateFormat.getDateInstance(DateFormat.DEFAULT, Locale.ENGLISH);
					    	String dateOut = dateFormatter.format(d); 
					    	return dateOut;
					    	//return "";
					    }
					    return "";
					  }
					});		

				graphView.getGraphViewStyle().setNumHorizontalLabels(1);
				graphView.getGraphViewStyle().setNumVerticalLabels(1);	
				graphView.getGraphViewStyle().setVerticalLabelsWidth(0);
				graphView.getGraphViewStyle().setTextSize(20);
				graphView.getGraphViewStyle().setLegendWidth(280);
				graphView.setPadding(0, 0, 20, 10);
				graphView.setLayoutParams(new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,1f));
				views.add(graphView);
				
				
				return views;
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

		public static SMSMessage getPreviousMessage(Context context, String thread_id,
				Long date, UserInfoStore store) {
			SMSMessage previous_message;
			Uri message_ids = Uri.parse("content://mms-sms/conversations/" + thread_id);
			Cursor cursor = context.getContentResolver().query(message_ids, 
					new String[]{"_id", "date", "normalized_date"},
					 "normalized_date<" + date, null, "normalized_date desc");
			//
			if(cursor!=null && cursor.moveToFirst()) {
				String previous_message_id = cursor.getString(0);
				long previous_date = cursor.getLong(1);
				long previous_n_date = cursor.getLong(2);
				//previous message is mms
				if(previous_n_date!=previous_date){
					//
					previous_message = ContentUtils
							.getMMSMessage(context,thread_id,previous_message_id, store);
				} else {
					//Log.i("previous message id ",previous_message_id);
					previous_message = ContentUtils
							.getSMSMessage(context,thread_id,previous_message_id, store);
				}
				if(previous_message!=null) previous_message.setMessageID(previous_message_id);
				return previous_message;
			}
			if(cursor!=null) cursor.close();
			return null;
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
		
}
