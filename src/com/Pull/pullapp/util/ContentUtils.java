package com.Pull.pullapp.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.TreeMap;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Resources;
import android.database.Cursor;
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

import com.jjoe64.graphview.CustomLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;
import com.jjoe64.graphview.LineGraphView;

public class ContentUtils {
	public static CharSequence getInitials(String name, String number) {
		String initials = "";
		if(name==null || name.isEmpty()) return initials;
		if(name.equals(number)) return "??";
		String[] i = name.split(" ");
		for(String chunk : i) {
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
					  ThreadsColumns._ID+" is not null and replace("+ 
					  ThreadsColumns.RECIPIENT_IDS + ",' ','')=" + ThreadsColumns.RECIPIENT_IDS,
					  null,ThreadsColumns.DATE+" DESC");		  
		 // ContentQueryMap map = new ContentQueryMap(c, ThreadsColumns._ID, true, null);
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
	  public static String getThreadIDFromNumber(Context context, String number) {
	      Cursor c = context.getContentResolver().query(Uri.parse("content://sms"),
	      		new String[]{"max(" + TextBasedSmsColumns.THREAD_ID + ") as " + 
	      				TextBasedSmsColumns.THREAD_ID,
	      				TextBasedSmsColumns.ADDRESS},
	      		TextBasedSmsColumns.ADDRESS+"='"+number+"'",null,null);
	     // Log.i("number",number);
	     // Log.i("size",c.getCount()+" ");
	      if (c!=null && c.getCount()>0) {
	    	  while(c.moveToNext()) {	
	    		  if (c.getString(c.getColumnIndexOrThrow(TextBasedSmsColumns.THREAD_ID))==null) {
	    			 // Log.i("column","nothing");
	    			  return null;
	    		  }
	    		  String id = c.getString(c.getColumnIndexOrThrow(
	    				  TextBasedSmsColumns.THREAD_ID)).toString();
	    		  return id;
	    	}
	      }
	      	
	      return null;
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
		// This function searches for an mobile phone entry for the contact
		public String getNumberfromContact(Context context, String contact, Boolean debugging)	{
			ContentResolver cr = context.getContentResolver();
			String result = null;
			boolean valid = false;	
			String val_num = null;
			int contact_id = 0;
		    // Cursor1 search for valid Database Entries who matches the contact name
			Uri uri = ContactsContract.Contacts.CONTENT_URI;
			String[] projection = new String[]{	ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts.HAS_PHONE_NUMBER };
			String selection = ContactsContract.Contacts.DISPLAY_NAME + "=?";
			String[] selectionArgs = new String[]{String.valueOf(contact)};
			String sortOrder = null;
			Cursor cursor1 = cr.query(uri, projection, selection, selectionArgs, sortOrder);
		
		    if(cursor1.moveToFirst()){
		    	if(cursor1.getInt(cursor1.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) == 1){
		    		contact_id = cursor1.getInt(cursor1.getColumnIndex(ContactsContract.Contacts._ID));
		            // Cursor 2 search for valid MOBILE Telephone numbers (selection = Phone.TYPE 2)
		        	Uri uri2 = ContactsContract.Data.CONTENT_URI;	
		        	String[] projection2 = new String[]{ Phone.NUMBER, Phone.TYPE };
		        	String selection2 = Phone.CONTACT_ID + "=? AND " + Data.MIMETYPE + "=? AND " + Phone.TYPE + "=2";
		    		String[] selectionArgs2 = new String[]{ String.valueOf(contact_id), Phone.CONTENT_ITEM_TYPE };
		    		String sortOrder2 = Data.IS_PRIMARY + " desc"; 	
		        	Cursor cursor2 = cr.query(uri2, projection2, selection2, selectionArgs2, sortOrder2);
		            
		        	if(cursor2.moveToFirst()){
		                result = cursor2.getString(cursor2.getColumnIndex(Phone.NUMBER));
		            }
		            cursor2.close();
		        }
		        cursor1.close();
		    }
		    return result;
		}
		public static String setChannel(TelephonyManager tmgr, String recipient) {
			return "phoneNumber"+addCountryCode(tmgr,recipient);
		}
		public static String setChannel(String recipient) {
			return "phoneNumber"+addCountryCode(recipient);
		}
		public static Cursor getMessagesCursor(Context mContext,
				String thread_id, String number) {
			String querystring;
			if(thread_id == null || true) {
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
			else {
				Log.i("thread id is here", thread_id);
				querystring = TextBasedSmsColumns.THREAD_ID + "=" + thread_id 
			
					+ TextBasedSmsColumns.TYPE + " in (" + 
			   				TextBasedSmsColumns.MESSAGE_TYPE_SENT 					
			   				+  " , " + TextBasedSmsColumns.MESSAGE_TYPE_INBOX + ")";
				Log.i("thread id",thread_id);
			}

	        String[] variables = new String[]{"'sent' as category",
	        		TextBasedSmsColumns.TYPE,TextBasedSmsColumns.BODY,
	        		BaseColumns._ID, TextBasedSmsColumns.ADDRESS, TextBasedSmsColumns.READ,
	        		TextBasedSmsColumns.DATE, TextBasedSmsColumns.THREAD_ID};

			Cursor messages_cursor = mContext.getContentResolver().query(Telephony.Sms.CONTENT_URI,
					variables,querystring ,null,TextBasedSmsColumns.DATE);	      
			Log.i("pending_messages_cursor size", " " + messages_cursor.getCount());
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
		public class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
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
		  
		public void loadBitmap(Context context, String path, ImageView imageView, int alt_id) {
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

		public static boolean isInitiating(long date, int type, String body,
				long previous_date, int previous_type, String previous_body) {
			boolean initiating = false, retexting=false;
     		if(previous_type==type) retexting = true;
     		long hours_elapsed = (long)(date-previous_date)/((long)(1000.0*60.0*60.0));
     		Log.i("hours elapsed ", ""+hours_elapsed);
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
				int type = Integer.parseInt(messages_cursor.getString(1).toString());
				if(previous_body!=null && previous_date>0) {
			  		initiating = isInitiating(date, type, body, previous_date, previous_type, previous_body);
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
				  minutes_elapsed = (date - previous_date)/(float)(1000*60);
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
					    	//return dateOut;
					    	return "";
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
				graphView = new LineGraphView(
					    activity
					    , original_name + " relative to you "
					);
					// add data
				try {
					graphView.addSeries(new GraphViewSeries(treemapToGraphSeries(data.get(series_names[2]))));
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
						    if(value==1) return "";
						    return "";
						  }
						});		
					graphView.setLayoutParams(new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,1f));
					graphView.getGraphViewStyle().setNumHorizontalLabels(1);
					graphView.getGraphViewStyle().setNumVerticalLabels(1);	
					graphView.getGraphViewStyle().setVerticalLabelsWidth(0);
					graphView.setPadding(0, 20, 10, 10);
					graphView.getGraphViewStyle().setTextSize(20);
					
					views.add(graphView);
					
				} catch(RuntimeException e) {
				/*	TextView tv = new TextView(activity);
					tv.setText("Not enough messages for this graph -- check back later!");
					mGraphView.addView(tv);**/
					e.printStackTrace();
					return views;
				}
				return views;
		}

		public static void deleteConversation(Context mContext, String threadID) {
			mContext.getContentResolver().delete(Uri.parse("content://sms/conversations/" + threadID),null,null);
			String where = TextBasedSmsColumns.THREAD_ID+"=?";
			String[] args = new String[] { threadID };
			mContext.getContentResolver().delete( Telephony.Sms.CONTENT_URI, where, args );
		}				
}
