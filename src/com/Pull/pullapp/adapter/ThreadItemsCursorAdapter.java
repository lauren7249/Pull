package com.Pull.pullapp.adapter;

import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.provider.Telephony.TextBasedSmsColumns;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.Pull.pullapp.MessageActivityCheckboxCursor;
import com.Pull.pullapp.R;
import com.Pull.pullapp.util.Constants;
import com.Pull.pullapp.util.ContentUtils;
import com.Pull.pullapp.util.SendUtils;
import com.Pull.pullapp.util.UserInfoStore;
import com.facebook.widget.ProfilePictureView;
public class ThreadItemsCursorAdapter extends CursorAdapter {
	
	public HashMap<Integer,String> recipientID_hash;
	private int cursorType;
	private Context mContext;
	protected Activity activity;
	private UserInfoStore store;
	
    @SuppressWarnings("deprecation")
	public ThreadItemsCursorAdapter(Context context, Cursor cursor, int cursorType, Activity a) {
    	super(context, cursor);
    	mContext = context;  	
    	this.store = new UserInfoStore(context);
    	recipientID_hash = new HashMap<Integer,String>();
    	this.cursorType = cursorType;
    	activity = a;
    }

	@Override
	public void bindView(View v, Context context, Cursor threads) {
		populateFields(v, context, threads);
		
	}

	@Override
	public View newView(Context context, Cursor threads, ViewGroup parent) {
		
        final LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.list_item_thread, parent, false);
        
        populateFields(v, context, threads);
		return v;
	}

	private void populateFields(View v, Context context, Cursor threads) {
		switch(cursorType){
		case R.id.my_conversation_tab:
			setTextMessageFields(context,threads,v);
			return;
		default:
			setSharedFields(context,threads,v);
			return;
		}
	}
	private void setTextMessageFields(Context context, Cursor threads, View v) {

		TextView name_view = (TextView) v.findViewById(R.id.txt_title);
		TextView snippet_view = (TextView) v.findViewById(R.id.txt_message_info);
		ImageView read_indicator = (ImageView) v.findViewById(R.id.indicator);
		ImageView share = (ImageView) v.findViewById(R.id.share_button);
	    final ImageView other_pic_mine = 
	    		(ImageView) v.findViewById(R.id.other_pic_mine);
	    final ProfilePictureView their_pic = 
	    		(ProfilePictureView) v.findViewById(R.id.profile_pic);
	    final ImageView other_pic = 
	    		(ImageView) v.findViewById(R.id.other_pic);

		String name="", snippet="", recipientId="";
		final String number;
		boolean read = true;        

        final int position= threads.getPosition();
        
    	read = (!threads.getString(1).equals("0"));	 
    	recipientId = threads.getString(2);
    	snippet = threads.getString(4);		    		    	

    	recipientID_hash.put(position, recipientId);
    	

    	if(store.getPhoneNumber(recipientId)==null) {
			number = ContentUtils.addCountryCode(ContentUtils.getAddressFromID(context, recipientId));
			store.setPhoneNumber(recipientId, number);
    	}		
    	else number = store.getPhoneNumber(recipientId);
    	
    	name = store.getName(number);
    	if(name==null || name.length() == 0 || name.equals(number)) {
    		name = ContentUtils.getContactDisplayNameByNumber(context, number);
    		store.setName(number, name);
    	}	
    	
    	//final String friend_name = name;
    	
		name_view.setText(store.getName(number));
		snippet_view.setText(snippet);
		if(!read) {
			read_indicator.setVisibility(View.VISIBLE);
		} else {
			read_indicator.setVisibility(View.GONE);
		}  

    	other_pic_mine.setVisibility(View.GONE);
		other_pic.setVisibility(View.VISIBLE);
		
		their_pic.setVisibility(View.GONE);
		
    	if(!store.isFriend(number)) {
    		other_pic.setBackgroundResource(R.drawable.add_ppl);
    		other_pic.setOnClickListener(new OnClickListener(){
	
				@Override
				public void onClick(View v) {
					Log.i("number", number);
					View addFriendView = View.inflate(mContext, R.layout.add_friend, null);
					AlertDialog.Builder builder = new AlertDialog.Builder(activity);
				    builder.setTitle("Invite Friend");
				    builder.setMessage("Invite " + store.getName(number) + " to be your friend on Pull?")
				           .setCancelable(true)
				           .setView(addFriendView)
				           .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				               public void onClick(DialogInterface dialog, int id)
				               {
	
				            	   SendUtils.inviteFriend(number, mContext, activity);
	
				               	}
				           }) 
				           .setNegativeButton("No", new DialogInterface.OnClickListener() {
				               public void onClick(DialogInterface dialog, int id) 
				               {
				                    dialog.cancel();
				               }
				           }).show();		
	
				}
    		});
    	} else {
    		other_pic.setOnClickListener(null);
    		other_pic.setImageDrawable(Drawable.createFromPath(store.getPhotoPath(number)));
    	}
    	share.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				Intent ni = new Intent(mContext, MessageActivityCheckboxCursor.class);
				ni.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				ni.putExtra(Constants.EXTRA_NAME,store.getName(number));
				ni.putExtra(Constants.EXTRA_NUMBER,number);	
				ni.putExtra(Constants.EXTRA_SHARE_TO_NUMBER,"");	
				mContext.startActivity(ni);				

			}
    		
    	});    	

	}


	private void setSharedFields(Context context, Cursor threads, View v) {
		LinearLayout row = (LinearLayout) v.findViewById(R.id.row);
		TextView name_view = (TextView) v.findViewById(R.id.txt_title);
		TextView snippet_view = (TextView) v.findViewById(R.id.txt_message_info);
		ImageView read_indicator = (ImageView) v.findViewById(R.id.indicator);
		ImageView share = (ImageView) v.findViewById(R.id.share_button);
	    final ProfilePictureView their_pic = 
	    		(ProfilePictureView) v.findViewById(R.id.profile_pic);
	    final ProfilePictureView my_pic = 
	    		(ProfilePictureView) v.findViewById(R.id.profile_pic_mine);
	    final ImageView other_pic = 
	    		(ImageView) v.findViewById(R.id.other_pic);
	    final ImageView other_pic_mine = 
	    		(ImageView) v.findViewById(R.id.other_pic_mine);
	    
	    final String number, friend_name;
		String name="", facebookID="";
    	String confidante = threads.getString(0);
    	String originalRecipientName = threads.getString(1);
    	String sharer = threads.getString(2);	
    	int type = Integer.valueOf(threads.getString(4));
    	
    	their_pic.setVisibility(View.GONE);
    	my_pic.setVisibility(View.GONE);
    	share.setVisibility(View.GONE);
    	snippet_view.setVisibility(View.VISIBLE);
    	if(type == TextBasedSmsColumns.MESSAGE_TYPE_SENT) {
    		number = confidante;
    		friend_name = ContentUtils.getContactDisplayNameByNumber(context, number);
    		other_pic.setVisibility(View.GONE);
    		other_pic_mine.setVisibility(View.VISIBLE);
    		row.setGravity(Gravity.RIGHT);
    		name_view.setGravity(Gravity.RIGHT);
    		snippet_view.setGravity(Gravity.RIGHT);
    		snippet_view.setText(originalRecipientName + " doesn't know you've shared");
    		
    	} else {
    		number = sharer;
    		friend_name = ContentUtils.getContactDisplayNameByNumber(context, number);
    		other_pic_mine.setVisibility(View.GONE);
    		other_pic.setVisibility(View.VISIBLE);
    		name_view.setGravity(Gravity.LEFT);
    		snippet_view.setGravity(Gravity.LEFT);    		
    		snippet_view.setText(originalRecipientName + " doesn't know " 
    		+ friend_name + " shared");
    	}
    	
    	
    	
    	name = store.getName(number);
    	if(name.length()==0 || name.equals(number)) {
    		name = ContentUtils.getContactDisplayNameByNumber(context, number);
    		store.setName(number, name);
    	}		
    	
    	name_view.setText(name);
    	
    	if(!store.isFriend(number)) {
    		other_pic.setBackgroundResource(R.drawable.add_ppl);
    		other_pic.setOnClickListener(new OnClickListener(){
	
				@Override
				public void onClick(View v) {
					View addFriendView = View.inflate(mContext, R.layout.add_friend, null);
					AlertDialog.Builder builder = new AlertDialog.Builder(activity);
				    builder.setTitle("Invite Friend");
				    builder.setMessage("Invite " + store.getName(number) + " to be your friend on Pull?")
				           .setCancelable(true)
				           .setView(addFriendView)
				           .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				               public void onClick(DialogInterface dialog, int id)
				               {
	
				            	   SendUtils.inviteFriend(number, mContext, activity);
	
				               	}
				           }) 
				           .setNegativeButton("No", new DialogInterface.OnClickListener() {
				               public void onClick(DialogInterface dialog, int id) 
				               {
				                    dialog.cancel();
				               }
				           }).show();		
	
				}
    		});
    	} else {
    		other_pic.setOnClickListener(null);
    		other_pic.setImageDrawable(Drawable.createFromPath(store.getPhotoPath(number)));
    	}
    	   	
    	
    	//TODO: ADD DATA TO SHOW IF IT IS READ OR NOT
    	read_indicator.setVisibility(View.GONE);
    	
	}

}
