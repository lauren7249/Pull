package com.Pull.pullapp.adapter;

import java.util.HashMap;
import java.util.TreeSet;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.Telephony;
import android.provider.Telephony.TextBasedSmsColumns;
import android.support.v4.widget.CursorAdapter;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.Pull.pullapp.R;
import com.Pull.pullapp.fragment.SimplePopupWindow;
import com.Pull.pullapp.model.SMSMessage;
import com.Pull.pullapp.util.Constants;
import com.Pull.pullapp.util.ContentUtils;
import com.Pull.pullapp.util.UserInfoStore;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.parse.ParsePush;
import com.parse.ParseUser;

public class MessageCursorAdapter extends CursorAdapter {
	
	public boolean showCheckboxes;
	public TreeSet<SMSMessage> check_hash;
	public HashMap<Long,Integer> delayedMessages;
	private String other_person, other_person_name;
	private Activity activity;
	private UserInfoStore store;
	private boolean isTextConvo;
	private ContentUtils cu;
	private String conversant;
	private SimplePopupWindow popup;
	private boolean isMine;
	private String conversant_name;
    @SuppressWarnings("deprecation")
	public MessageCursorAdapter(Context context, Cursor cursor, String number, 
			Activity activity) {
    	super(context, cursor, FLAG_REGISTER_CONTENT_OBSERVER);
    	check_hash = new TreeSet<SMSMessage>();
    	delayedMessages = new HashMap<Long,Integer>();
    	store = new UserInfoStore(context);
    	other_person = ContentUtils.addCountryCode(number);
    	other_person_name = store.getName(other_person);
		this.activity = activity;
		this.isTextConvo = true;
		cu  = new ContentUtils();
   	    	
    }
	public MessageCursorAdapter(Context context, Cursor cursor, Activity activity, 
			String conversant, String other_person, String person_shared, boolean isMine) {
    	super(context, cursor);
    	check_hash = new TreeSet<SMSMessage>();
    	delayedMessages = new HashMap<Long,Integer>();
    	store = new UserInfoStore(context);
    	this.conversant = conversant;
    	this.conversant_name = store.getName(conversant);
    	this.other_person = other_person;
    	this.other_person_name = person_shared;
    	Log.i("other_person",other_person);
    	Log.i("other_person_name",other_person_name);
		this.activity = activity;
		this.isTextConvo = false;
		this.isMine = isMine;
		cu  = new ContentUtils();
    }
	
	@Override
	public void bindView(View v, Context context, Cursor c) {
		if(isTextConvo) {
			populateTextConvo(context, c, v, false);
		}
		else populateSharedConvo(context, c, v, false);
	}

	@Override
	public View newView(Context context, Cursor c, ViewGroup parent) {
		final LayoutInflater inflater = LayoutInflater.from(context);
		View v = inflater.inflate(R.layout.sms_row, parent, false);
		if(isTextConvo) {
			populateTextConvo(context, c, v, true);
		}
		else populateSharedConvo(context, c, v, true);
		return v;
	}
	private void populateSharedConvo(Context context, Cursor c, View v, boolean isnew) {
		String body="";
		final String address;
		long date;
		final SMSMessage message;
		final int position = c.getPosition();
		if(c.getColumnCount()<8) return;
		//for(int i=0; i<c.getColumnCount(); i++) {
			//Log.i("column ", c.getColumnName(i) + ": " + c.getString(i));
		//}
		int type = Integer.parseInt(c.getString(c.getColumnIndex(TextBasedSmsColumns.TYPE)).toString());

		body = c.getString(2).toString();
    	address = c.getString(4).toString();
    	date = c.getLong(6);
    	String owner = c.getString(7);
       	message = new SMSMessage(date, body, address, store.getName(address), type, store, owner);

		final ViewHolder holder; 
		if(isnew) holder = new ViewHolder();
		else holder = (ViewHolder) v.getTag();
		
		holder.messageBox = (LinearLayout) v.findViewById(R.id.message_box);
		holder.message = (TextView) v.findViewById(R.id.message_text);
		holder.time = (TextView) v.findViewById(R.id.message_time);
		holder.edit = (Button) v.findViewById(R.id.edit_message_button);	
		holder.their_pic = (CircularImageView) v.findViewById(R.id.contact_image);
		holder.their_initials = (TextView) v.findViewById(R.id.contact_initials);
		holder.my_initials = (TextView) v.findViewById(R.id.my_initials);
		holder.my_pic = (ImageView) v.findViewById(R.id.my_image);
		holder.addPPl = (ImageView) v.findViewById(R.id.add_ppl);

		holder.addPPl.setVisibility(View.GONE);

    	holder.message.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD);
    	holder.my_initials.setVisibility(View.GONE);
    	LayoutParams layoutParams=(LayoutParams) holder.addPPl.getLayoutParams();
    	LayoutParams lp = (LayoutParams) holder.messageBox.getLayoutParams();
		if(message.isSentByMe()) {
			if(type == Constants.MESSAGE_TYPE_SENT_COMMENT) {
				holder.messageBox.setBackgroundResource(R.drawable.blank_outgoing);
				holder.message.setTypeface(Typeface.SANS_SERIF, Typeface.ITALIC);
				holder.time.setBackgroundResource(0);
				holder.time.setTextColor(Color.GRAY);						
			} else {
				holder.messageBox.setBackgroundResource(R.drawable.outgoing);
				holder.message.setTextColor(context.getResources().getColor(R.color.lightGreen));
				holder.time.setTextColor(context.getResources().getColor(R.color.lightGreen));				
				if(!isMine) {
					holder.my_initials.setVisibility(View.VISIBLE);
		    		holder.my_initials.setText(ContentUtils.getInitials(conversant_name, conversant));
		    		holder.my_initials.setOnClickListener(new OnClickListener(){
						@Override
						public void onClick(View v) {
							popup = new SimplePopupWindow(v);
							popup.showLikePopDownMenu();
							popup.setMessage(conversant_name);	
							
						}
		    			
		    		});	 					
				}else {
					
				}
			}
			layoutParams.gravity = Gravity.LEFT;
			holder.message.setGravity(Gravity.RIGHT);
			holder.time.setGravity(Gravity.RIGHT);		
			holder.their_pic.setVisibility(View.GONE);
			holder.their_initials.setVisibility(View.GONE);
			lp.gravity = Gravity.RIGHT;			
		}else {
			lp.gravity = Gravity.LEFT;			
			if(type == Constants.MESSAGE_TYPE_RECEIVED_COMMENT) {
				holder.messageBox.setBackgroundResource(R.drawable.blank_incoming);
				holder.message.setTypeface(Typeface.SANS_SERIF, Typeface.ITALIC);
		    	if(conversant!=null && store.getPhotoPath(conversant)!=null) {
	 	    		cu.loadBitmap(mContext, store.getPhotoPath(conversant),holder.their_pic, 0);
	 	    		holder.their_initials.setVisibility(View.GONE);
		    		holder.their_pic.setVisibility(View.VISIBLE);
		    		holder.their_pic.setOnClickListener(new OnClickListener(){
						@Override
						public void onClick(View v) {
							popup = new SimplePopupWindow(v);
							popup.showLikePopDownMenu();
							popup.setMessage(conversant_name);	
							
						}
		    			
		    		});

		    	}
		    	else if(conversant_name!=null && conversant_name.length()>0) {
		    		holder.their_pic.setVisibility(View.GONE);	
		    		holder.their_initials.setVisibility(View.VISIBLE);
		    		holder.their_initials.setText(ContentUtils.getInitials(conversant_name, conversant));
		    		holder.their_initials.setOnClickListener(new OnClickListener(){
						@Override
						public void onClick(View v) {
							popup = new SimplePopupWindow(v);
							popup.showLikePopDownMenu();
							popup.setMessage(conversant_name);	
							
						}
		    			
		    		});	    		
		    	}		
		    	else {
		    		holder.their_pic.setVisibility(View.GONE);	
		    		holder.their_initials.setVisibility(View.GONE);		    		
		    	}
			}else {
				holder.messageBox.setBackgroundResource(R.drawable.incoming);
				holder.message.setTextColor(Color.WHITE);
				holder.time.setTextColor(Color.WHITE);					
				holder.their_pic.setVisibility(View.GONE);
	    		holder.their_initials.setVisibility(View.VISIBLE);
	    		holder.their_initials.setText(ContentUtils.getInitials(other_person_name, other_person));
	    		holder.their_initials.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v) {
						popup = new SimplePopupWindow(v);
						popup.showLikePopDownMenu();
						popup.setMessage(other_person_name);	
						
					}
	    			
	    		});	  				
			}
			layoutParams.gravity = Gravity.RIGHT;
			holder.message.setGravity(Gravity.LEFT);
			holder.time.setGravity(Gravity.LEFT);					
		}		
		v.setTag(holder);		
		holder.message.setText(message.getMessage());	
		holder.their_pic.setScaleType(ImageView.ScaleType.CENTER_CROP);
		holder.their_pic.setAdjustViewBounds(true);
		
		if(type==TextBasedSmsColumns.MESSAGE_TYPE_OUTBOX) {
		//if(message.isDelayed) {
		    final CharSequence relativeTime;
			relativeTime = DateUtils.getRelativeDateTimeString(context, date, DateUtils.MINUTE_IN_MILLIS, DateUtils.DAY_IN_MILLIS, 0);			
			holder.edit.setVisibility(View.GONE);
			holder.my_pic.setBackgroundResource(R.drawable.send);
			holder.my_pic.setVisibility(View.VISIBLE);
			holder.my_pic.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					message.setApproved();
					try {
						message.saveToParse();
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
			}) ;
			holder.messageBox.setBackgroundResource(R.drawable.comment_box_background);
			holder.messageBox.setPadding(20, 20, 20, 20);
			holder.message.setGravity(Gravity.LEFT);
			holder.time.setGravity(Gravity.LEFT);
		    holder.time.setText(relativeTime + ", after you approve");
		    holder.time.setTextColor(Color.RED);
		    holder.time.setBackgroundResource(R.drawable.comment_box_background);
		} else {
			CharSequence relativeTime;
			if(System.currentTimeMillis()-message.getDate()>DateUtils.MINUTE_IN_MILLIS){
				relativeTime = DateUtils.getRelativeDateTimeString(context, message.getDate(), 
						DateUtils.MINUTE_IN_MILLIS, DateUtils.DAY_IN_MILLIS, 0);
			}else{
				relativeTime = "Just Now";
			}
			holder.time.setText(relativeTime);
			holder.edit.setVisibility(View.GONE);
			holder.my_pic.setVisibility(View.GONE);
		}
		holder.messageBox.setLayoutParams(lp);		
	}

	private void populateTextConvo(final Context context, Cursor c, View v, boolean isnew) {
		
		final ViewHolder holder; 
		if(isnew) holder = new ViewHolder();
		else holder = (ViewHolder) v.getTag();
		
		String msgContentType = c.getString(c.
			     getColumnIndex(Telephony.BaseMmsColumns.CONTENT_TYPE));
    	
		boolean initiating = false;
		if ("application/vnd.wap.multipart.related".equals(msgContentType)) {			
			initiating=ContentUtils.isInitiating(c, false, context);
		}
		else {
			initiating=ContentUtils.isInitiating(c, true, context);
			Cursor mCursor = ContentUtils.getSMS(c, context);
	        if(mCursor!=null && mCursor.moveToFirst()) populateSMSConvo(mCursor, holder, v, context, initiating);
		}		

	}
	private void populateSMSConvo(Cursor c, ViewHolder holder, View v, final Context context, boolean initiating) {
		String body="";
		final String address;
		long date;
		final SMSMessage message;
		int type = 0;		
		try {
			type = Integer.parseInt(c.getString(c.getColumnIndex(TextBasedSmsColumns.TYPE)).toString());
		} catch(RuntimeException e) {
			e.printStackTrace();
			return;
		} 
		if(type==TextBasedSmsColumns.MESSAGE_TYPE_OUTBOX) return;
		
		body = c.getString(c.getColumnIndex(TextBasedSmsColumns.BODY)).toString();
		//Log.i("body",body);
    	address = c.getString(c.getColumnIndex(TextBasedSmsColumns.ADDRESS)).toString();
    	date = c.getLong(c.getColumnIndex(TextBasedSmsColumns.DATE));
    	String read = c.getString(c.getColumnIndex(TextBasedSmsColumns.READ)).toString();
    	String SmsMessageId = c.getString(c.getColumnIndex(BaseColumns._ID)).toString();
    	
    	message = new SMSMessage(date, body, address, store.getName(address), 
    			type, store, ParseUser.getCurrentUser().getUsername());
		
		holder.messageBox = (LinearLayout) v.findViewById(R.id.message_box);
		holder.message = (TextView) v.findViewById(R.id.message_text);
		holder.time = (TextView) v.findViewById(R.id.message_time);
		holder.edit = (Button) v.findViewById(R.id.edit_message_button);	
		holder.their_pic = (CircularImageView) v.findViewById(R.id.contact_image);
		holder.separator = (View) v.findViewById(R.id.separator);
		
		holder.addPPl = (ImageView) v.findViewById(R.id.add_ppl);
		
		if(initiating) holder.separator.setVisibility(View.VISIBLE);
		else  holder.separator.setVisibility(View.GONE);
		
		if(showCheckboxes) holder.addPPl.setVisibility(View.VISIBLE);
		else  holder.addPPl.setVisibility(View.GONE);
		
		LayoutParams layoutParams=(LayoutParams) holder.addPPl.getLayoutParams();		
        if (check_hash.contains(message)) {
        	holder.addPPl.setBackgroundResource(R.drawable.good_indicator);
			if(message.isSentByMe()) {
				if(showCheckboxes) {
					holder.messageBox.setBackgroundResource(R.drawable.outgoing_pressed);
					holder.message.setTextColor(Color.GRAY);
					holder.time.setTextColor(Color.GRAY);						
				}
				else {
					holder.messageBox.setBackgroundResource(R.drawable.outgoing);
					holder.message.setTextColor(context.getResources().getColor(R.color.lightGreen));
					holder.time.setTextColor(context.getResources().getColor(R.color.lightGreen));							
				}
				holder.message.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD);			
				holder.message.setGravity(Gravity.RIGHT);
				holder.time.setGravity(Gravity.RIGHT);
				layoutParams.gravity = Gravity.LEFT;
			}else {
				if(showCheckboxes) {
					holder.messageBox.setBackgroundResource(R.drawable.incoming_pressed);
					holder.message.setTextColor(Color.GRAY);
					holder.time.setTextColor(Color.GRAY);							
				}
				else {
					holder.messageBox.setBackgroundResource(R.drawable.incoming); 
					holder.message.setTextColor(Color.WHITE);
					holder.time.setTextColor(Color.WHITE);						
				}
				holder.message.setGravity(Gravity.LEFT);
				holder.time.setGravity(Gravity.LEFT);
				layoutParams.gravity = Gravity.RIGHT;
			}
        }
        else {
        	
			if(message.isSentByMe()) {
				holder.addPPl.setBackgroundResource(R.drawable.add_green);
				holder.messageBox.setBackgroundResource(R.drawable.outgoing);
				holder.message.setTextColor(context.getResources().getColor(R.color.lightGreen));
				holder.time.setTextColor(context.getResources().getColor(R.color.lightGreen));
				holder.message.setPadding(50, 0, 10, 0);
				holder.time.setPadding(50, 0, 10, 0);
				layoutParams.gravity = Gravity.LEFT;
				holder.message.setGravity(Gravity.RIGHT);
				holder.time.setGravity(Gravity.RIGHT);				
			}else {
				holder.addPPl.setBackgroundResource(R.drawable.add_white);
				holder.messageBox.setBackgroundResource(R.drawable.incoming);  
				holder.message.setTextColor(Color.WHITE);
				holder.time.setTextColor(Color.WHITE);					
				holder.message.setPadding(10, 0, 50, 0);
				holder.time.setPadding(10, 0, 50, 0);
				layoutParams.gravity = Gravity.RIGHT;
				holder.message.setGravity(Gravity.LEFT);
				holder.time.setGravity(Gravity.LEFT);					
			}
        }
		
		v.setTag(holder);		
		holder.message.setText(message.getMessage());
		if(showCheckboxes) holder.messageBox.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent broadcastIntent = new Intent();
				broadcastIntent.setAction(Constants.ACTION_SHARE_STATE_CHANGED);								
				if(check_hash.contains(message)) {
					check_hash.remove(message);
				}else{ 
					check_hash.add(message);
				//	Log.i("cjssl","check hash added message");
				}
				context.sendBroadcast(broadcastIntent);	
				notifyDataSetChanged();
			};
        });				
		LayoutParams lp = (LayoutParams) holder.messageBox.getLayoutParams();

		if(message.isSentByMe())
		{
			holder.their_pic.setVisibility(View.GONE);	
			lp.gravity = Gravity.RIGHT;
			
		}
		else
		{
	    	if(store.getPhotoPath(address)!=null) {
	    		cu.loadBitmap(mContext, store.getPhotoPath(address),holder.their_pic, 0);
	    		holder.their_pic.setVisibility(View.VISIBLE);
	    	}
	    	else holder.their_pic.setVisibility(View.GONE);			
			lp.gravity = Gravity.LEFT;
		}
		
		CharSequence relativeTime;
		if(System.currentTimeMillis()-message.getDate()>DateUtils.MINUTE_IN_MILLIS){
			relativeTime = DateUtils.getRelativeDateTimeString(context, message.getDate(), 
					DateUtils.MINUTE_IN_MILLIS, DateUtils.DAY_IN_MILLIS, 0);
		}else{
			relativeTime = "Just Now";
		}
		holder.time.setText(relativeTime);
		holder.edit.setVisibility(View.GONE);
		holder.messageBox.setLayoutParams(lp);
		
    	if(!SmsMessageId.equals("") && read.equals("0")) {
        	ContentValues values = new ContentValues();
    		values.put("read",true);
    		context.getContentResolver().update(Telephony.Sms.CONTENT_URI,
    				values, BaseColumns._ID+"="+SmsMessageId, null);	
    	}	    	
		
	}
	private static class ViewHolder
	{
		public View separator;
		public TextView my_initials;
		public TextView their_initials;
		public ImageView my_pic;
		TextView message, time;
		LinearLayout messageBox;
		Button edit;
		CircularImageView their_pic;
		ImageView addPPl;
	}
	
	private void sendApproval(String message,long scheduledFor,String approver) {
		ParsePush push = new ParsePush();
		JSONObject data = new JSONObject();
		try {
			data.put("action", Constants.ACTION_APPROVE_MESSAGE);
			data.put("phoneNumber", other_person);
			data.put("message", message);
			data.put("scheduledFor", scheduledFor);
			data.put("approver", approver);
			push.setChannel(ContentUtils.setChannel(conversant));
			push.setData(data);
			push.sendInBackground();				
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

}
