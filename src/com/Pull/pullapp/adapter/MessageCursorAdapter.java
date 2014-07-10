package com.Pull.pullapp.adapter;

import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
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
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.Pull.pullapp.R;
import com.Pull.pullapp.model.SMSMessage;
import com.Pull.pullapp.util.Constants;
import com.Pull.pullapp.util.ContentUtils;
import com.Pull.pullapp.util.SendUtils;
import com.Pull.pullapp.util.UserInfoStore;

public class MessageCursorAdapter extends CursorAdapter {
	
	public boolean showCheckboxes;
	public TreeSet<SMSMessage> check_hash;
	public HashMap<Long,Integer> delayedMessages;
	private String other_person, other_person_name;
	private Activity activity;
	private UserInfoStore store;
	private boolean isMyConversation;
	
    @SuppressWarnings("deprecation")
	public MessageCursorAdapter(Context context, Cursor cursor, String number, Activity activity, 
			boolean isMine) {
    	super(context, cursor);
    	check_hash = new TreeSet<SMSMessage>();
    	delayedMessages = new HashMap<Long,Integer>();
    	store = new UserInfoStore(context);
    	other_person = ContentUtils.addCountryCode(number);
    	other_person_name = store.getName(other_person);
		this.activity = activity;
		this.isMyConversation = isMine;
   	    	
    }
	public MessageCursorAdapter(Context context, Cursor cursor, Activity activity, 
			boolean isMyConversation) {
    	super(context, cursor);
    	check_hash = new TreeSet<SMSMessage>();
    	delayedMessages = new HashMap<Long,Integer>();
    	store = new UserInfoStore(context);
		this.activity = activity;
		this.isMyConversation = isMyConversation;
   	    	
    }
	
	@Override
	public void bindView(View v, Context context, Cursor c) {
		if(isMyConversation) populateMine(context, c, v, false);
		else populateTheirs(context, c, v, false);
	}

	@Override
	public View newView(Context context, Cursor c, ViewGroup parent) {
		final LayoutInflater inflater = LayoutInflater.from(context);
		View v = inflater.inflate(R.layout.sms_row, parent, false);
		if(isMyConversation) populateMine(context, c, v, true);
		else populateTheirs(context, c, v, true);
		return v;
	}
	private void populateTheirs(Context context, Cursor c, View v, boolean isnew) {
		String body="";
		final String address;
		long date;
		final SMSMessage message;
		final int position = c.getPosition();
		
		for(int i=0; i<c.getColumnCount(); i++) {
			//Log.i("column ", c.getColumnName(i) + ": " + c.getString(i));
		}
		int type = Integer.parseInt(c.getString(c.getColumnIndex(TextBasedSmsColumns.TYPE)).toString());
		if(type==TextBasedSmsColumns.MESSAGE_TYPE_OUTBOX) return;
		body = c.getString(2).toString();
    	address = c.getString(4).toString();
    	date = c.getLong(6);
    	
       	message = new SMSMessage(date, body, address, type, store);

		final ViewHolder holder; 
		if(isnew) holder = new ViewHolder();
		else holder = (ViewHolder) v.getTag();
		
		holder.messageBox = (LinearLayout) v.findViewById(R.id.message_box);
		holder.message = (TextView) v.findViewById(R.id.message_text);
		holder.time = (TextView) v.findViewById(R.id.message_time);
		holder.box = (CheckBox) v.findViewById(R.id.cbBox);
		holder.edit = (Button) v.findViewById(R.id.edit_message_button);	
		holder.pic = (ImageView) v.findViewById(R.id.contact_image);
		holder.sharedWith = (LinearLayout) v.findViewById(R.id.shared_with);
		holder.shared_with_text = (TextView) v.findViewById(R.id.shared_with_text);
		holder.addPPl = (ImageView) v.findViewById(R.id.add_ppl);
		
		//int id = Resources.getSystem().getIdentifier("btn_check_holo_light", "drawable", "android");
		//holder.box.setButtonDrawable(id);
		holder.sharedWith.setVisibility(View.GONE);
		holder.shared_with_text.setVisibility(View.GONE);
		holder.addPPl.setVisibility(View.GONE);
    	
    	holder.box.setChecked(false);
    	holder.box.setVisibility(View.GONE);
    	
    	LayoutParams layoutParams=(LayoutParams) holder.addPPl.getLayoutParams();
		if(message.isSentByMe()) {
			if(type == Constants.MESSAGE_TYPE_SENT_COMMENT) {
				holder.messageBox.setBackgroundResource(R.drawable.blank_outgoing);
				holder.message.setTypeface(Typeface.SANS_SERIF, Typeface.ITALIC);
			} else
				holder.messageBox.setBackgroundResource(R.drawable.outgoing);
			layoutParams.gravity = Gravity.LEFT;
			holder.message.setGravity(Gravity.RIGHT);
			holder.time.setGravity(Gravity.RIGHT);				
		}else {
			if(type == Constants.MESSAGE_TYPE_RECEIVED_COMMENT) {
				holder.messageBox.setBackgroundResource(R.drawable.blank_incoming);
				holder.message.setTypeface(Typeface.SANS_SERIF, Typeface.ITALIC);
			}else
				holder.messageBox.setBackgroundResource(R.drawable.incoming);
			layoutParams.gravity = Gravity.RIGHT;
			holder.message.setGravity(Gravity.LEFT);
			holder.time.setGravity(Gravity.LEFT);					
		}		
		v.setTag(holder);		
		holder.message.setText(message.getMessage());	
		LayoutParams lp = (LayoutParams) holder.messageBox.getLayoutParams();

		if(message.isSentByMe())
		{
			holder.pic.setVisibility(View.GONE);	
			lp.gravity = Gravity.RIGHT;
			
		}
		//If not mine then it is from sender to show yellow background and align to left
		else
		{
	    	holder.pic.setVisibility(View.GONE);			
			lp.gravity = Gravity.LEFT;
		}
		
		if(message.isDelayed) {
			holder.edit.setVisibility(View.VISIBLE);

		    CharSequence relativeTime;
		    if(System.currentTimeMillis()-message.getFutureSendTime()<DateUtils.MINUTE_IN_MILLIS){
				relativeTime = DateUtils.getRelativeDateTimeString(context, message.getFutureSendTime(), DateUtils.MINUTE_IN_MILLIS, DateUtils.DAY_IN_MILLIS, 0);
			}else{
				relativeTime = "Now";
			}
		    holder.time.setText(relativeTime);
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
		}
		holder.messageBox.setLayoutParams(lp);		
	}

	private void populateMine(final Context context, Cursor c, View v, boolean isnew) {
		String body="";
		final String address;
		long date;
		final SMSMessage message;
		final int position = c.getPosition();

		int type = Integer.parseInt(c.getString(1).toString());
		if(type==TextBasedSmsColumns.MESSAGE_TYPE_OUTBOX) return;
		body = c.getString(2).toString();
    	address = c.getString(4).toString();
    	date = c.getLong(6);
    	String read = c.getString(5).toString();
    	String SmsMessageId = c.getString(3).toString();
    	
    	if(!SmsMessageId.equals("") && read.equals("0")) {
        	ContentValues values = new ContentValues();
    		values.put("read",true);
    		context.getContentResolver().update(Uri.parse("content://sms/"),
    				values, "_id="+SmsMessageId, null);	
    	}	    	
    	
    	message = new SMSMessage(date, body, address, type, store);

		final ViewHolder holder; 
		if(isnew) holder = new ViewHolder();
		else holder = (ViewHolder) v.getTag();
		
		holder.messageBox = (LinearLayout) v.findViewById(R.id.message_box);
		holder.message = (TextView) v.findViewById(R.id.message_text);
		holder.time = (TextView) v.findViewById(R.id.message_time);
		holder.box = (CheckBox) v.findViewById(R.id.cbBox);
		holder.edit = (Button) v.findViewById(R.id.edit_message_button);	
		holder.pic = (ImageView) v.findViewById(R.id.contact_image);
		holder.sharedWith = (LinearLayout) v.findViewById(R.id.shared_with);
		holder.shared_with_text = (TextView) v.findViewById(R.id.shared_with_text);
		holder.addPPl = (ImageView) v.findViewById(R.id.add_ppl);
		
		holder.shared_with_text.setVisibility(View.GONE);
		
		Set<String> sharedWith = message.getConfidantes();
		holder.sharedWith.removeAllViews();
		if(sharedWith.size()>0) {
			holder.shared_with_text.setVisibility(View.VISIBLE);
			holder.shared_with_text.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					if(holder.sharedWith.getVisibility() == View.VISIBLE) 
						holder.sharedWith.setVisibility(View.GONE);
					else 
						holder.sharedWith.setVisibility(View.VISIBLE);
				}
				
			});
			for(final String confidante : sharedWith) {
				final String name = store.getName(confidante);
				ImageView p = new ImageView(mContext);
				
				final String path = store.getPhotoPath(confidante);
				if(!path.isEmpty()) {
					p.setImageDrawable(Drawable.createFromPath(path));
					p.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							View pullProfileView = View.inflate(mContext, R.layout.pull_profile, null);
							ImageView pp = (ImageView) pullProfileView
									.findViewById(R.id.contact_image);
							pp.setImageDrawable(Drawable.createFromPath(path));
							AlertDialog.Builder builder = new AlertDialog.Builder(activity);
							builder.setTitle(name);
					        builder.setCancelable(true).setView(pullProfileView)	
					           .setPositiveButton("Close", new DialogInterface.OnClickListener() {
					               public void onClick(DialogInterface dialog, int id){
					            	   dialog.cancel();
					               	}
					           });					        
					        builder.show();
						};
			        });			
					holder.sharedWith.addView(p);
				} else {
					final TextView tv = new TextView(mContext);
					LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT,
							LayoutParams.MATCH_PARENT);
					//params.setMargins(0, 0, 0, 0);
					tv.setText(name);
					tv.setHint(confidante);
					tv.setTextColor(R.color.textColor);
					//tv.setPadding(15, 15, 15, 15);
					tv.setGravity(Gravity.LEFT|Gravity.TOP);
					tv.setLayoutParams(params);
					if(!store.isFriend(confidante)) tv.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							View addFriendView = View.inflate(mContext, R.layout.add_friend, null);
							AlertDialog.Builder builder = new AlertDialog.Builder(activity);
						    builder.setTitle("Invite Friend");
						    builder.setMessage("Invite " + tv.getText().toString() 
						    		+ " to be your friend on Pull?")
						           .setCancelable(true)
						           .setView(addFriendView)
						           .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
						               public void onClick(DialogInterface dialog, int id)
						               {

						            	   SendUtils.inviteFriend(tv.getHint().toString(), mContext, activity);

						               	}
						           }) 
						           .setNegativeButton("No", new DialogInterface.OnClickListener() {
						               public void onClick(DialogInterface dialog, int id) 
						               {
						                    dialog.cancel();
						               }
						           }).show();	
						};
			        });				
					holder.sharedWith.addView(tv);
				}
			}
		}


        if (check_hash.contains(message)) {
        	holder.box.setChecked(true);
        	holder.addPPl.setVisibility(View.GONE);
			if(message.isSentByMe()) {
				holder.messageBox.setBackgroundResource(R.drawable.outgoing_pressed);
				holder.message.setGravity(Gravity.RIGHT);
				holder.time.setGravity(Gravity.RIGHT);
			}else {
				holder.messageBox.setBackgroundResource(R.drawable.incoming_pressed); 
				holder.message.setGravity(Gravity.LEFT);
				holder.time.setGravity(Gravity.LEFT);				
			}
        }
        else {
        	holder.box.setChecked(false);
        	
        	LayoutParams layoutParams=(LayoutParams) holder.addPPl.getLayoutParams();
			if(message.isSentByMe()) {
				holder.messageBox.setBackgroundResource(R.drawable.outgoing);
				holder.message.setPadding(40, 0, 10, 0);
				holder.time.setPadding(40, 0, 10, 0);
				layoutParams.gravity = Gravity.LEFT;
				//layoutParams.leftMargin = -40;
				holder.message.setGravity(Gravity.RIGHT);
				holder.time.setGravity(Gravity.RIGHT);				
			}else {
				holder.messageBox.setBackgroundResource(R.drawable.incoming);  
				holder.message.setPadding(10, 0, 40, 0);
				holder.time.setPadding(10, 0, 40, 0);
				layoutParams.gravity = Gravity.RIGHT;
				//layoutParams.rightMargin = -40;
				holder.message.setGravity(Gravity.LEFT);
				holder.time.setGravity(Gravity.LEFT);					
			}
		//	holder.addPPl.setLayoutParams(layoutParams);
			holder.addPPl.setVisibility(View.VISIBLE);
        }
		if(showCheckboxes) holder.box.setVisibility(View.VISIBLE);
		else holder.box.setVisibility(View.GONE);
		
		v.setTag(holder);		
		holder.message.setText(message.getMessage());
		holder.messageBox.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent broadcastIntent = new Intent();
				broadcastIntent.setAction(Constants.ACTION_SHARE_STATE_CHANGED);
				//broadcastIntent.putExtra(Constants.EXTRA_MESSAGE_POSITION, position);
				context.sendBroadcast(broadcastIntent);					
				holder.box.toggle();
				if(check_hash.contains(message)) 
					check_hash.remove(message);
				else 
					check_hash.add(message);
				notifyDataSetChanged();
			};
        });				
		LayoutParams lp = (LayoutParams) holder.messageBox.getLayoutParams();

		if(message.isSentByMe())
		{
			holder.pic.setVisibility(View.GONE);	
			lp.gravity = Gravity.RIGHT;
			
		}
		//If not mine then it is from sender to show yellow background and align to left
		else
		{
	    	if(store.getPhotoPath(address)!=null) {
	    		holder.pic.setImageDrawable(Drawable.createFromPath(store.getPhotoPath(address)));
	    		holder.pic.setVisibility(View.VISIBLE);
	    	}
	    	else holder.pic.setVisibility(View.GONE);			
			lp.gravity = Gravity.LEFT;
		}
		
		if(message.isDelayed) {
			holder.edit.setVisibility(View.VISIBLE);

		    CharSequence relativeTime;
		    if(System.currentTimeMillis()-message.getFutureSendTime()<DateUtils.MINUTE_IN_MILLIS){
				relativeTime = DateUtils.getRelativeDateTimeString(context, message.getFutureSendTime(), DateUtils.MINUTE_IN_MILLIS, DateUtils.DAY_IN_MILLIS, 0);
			}else{
				relativeTime = "Now";
			}
		    holder.time.setText(relativeTime);
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
		}
		holder.messageBox.setLayoutParams(lp);

	}
	private static class ViewHolder
	{
		TextView message, time, shared_with_text;
		LinearLayout messageBox, sharedWith;
		CheckBox box;
		Button edit;
		ImageView pic;
		ImageView addPPl;
	}
	


}
