package com.Pull.pullapp.adapter;

import it.sephiroth.android.library.widget.HListView;

import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
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
import com.Pull.pullapp.util.DatabaseHandler;
import com.Pull.pullapp.util.SendUtils;
import com.Pull.pullapp.util.UserInfoStore;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.mikhaellopez.circularimageview.CircularImageView;
public class ThreadItemsCursorAdapter extends CursorAdapter {
	
	public HashMap<Integer,String> recipientID_hash;
	private int cursorType;
	private Context mContext;
	protected Activity activity;
	private UserInfoStore store;
	private ContentUtils cu;
	private Cursor shared_with_cursor;
	private DatabaseHandler dh;
	private HListView sharedWithListView;
	private SharedWithCursorAdapter sharedWithAdapter;
	
    @SuppressWarnings("deprecation")
	public ThreadItemsCursorAdapter(Context context, Cursor cursor, int cursorType, Activity a) {
    	super(context, cursor);
    	mContext = context;  	
    	this.store = new UserInfoStore(context);
    	recipientID_hash = new HashMap<Integer,String>();
    	this.cursorType = cursorType;
    	activity = a;
    	dh = new DatabaseHandler(mContext);
    	cu = new ContentUtils();
    }

	@Override
	public void bindView(View v, Context context, Cursor threads) {
		populateFields(v, context, threads, true);
		
	}

	@Override
	public View newView(Context context, Cursor threads, ViewGroup parent) {
		
        final LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.list_item_thread, parent, false);
        
        populateFields(v, context, threads, false);
		return v;
	}

	private void populateFields(View v, Context context, Cursor threads, boolean isBindView) {
		switch(cursorType){
		case R.id.my_conversation_tab:
			setTextMessageFields(context,threads,v, isBindView);
			return;
		default:
			setSharedFields(context,threads,v, isBindView);
			return;
		}
	}
	private void setTextMessageFields(Context context, Cursor threads, View v, boolean isBindView) {
		final ViewHolder holder; 
		if(!isBindView) holder = new ViewHolder();
		else holder = (ViewHolder) v.getTag();		

		holder.name_view = (TextView) v.findViewById(R.id.txt_title);
		holder.snippet_view = (TextView) v.findViewById(R.id.txt_message_info);
		holder.read_indicator = (ImageView) v.findViewById(R.id.indicator);
		//holder.share = (ImageView) v.findViewById(R.id.share_button);
	    holder.other_pic_mine = (ImageView) v.findViewById(R.id.other_pic_mine);
	    holder.other_pic = (ImageView) v.findViewById(R.id.other_pic);
		holder.image_view = (CircularImageView) v.findViewById(R.id.original_person_image);
		holder.initials_view = (TextView) v.findViewById(R.id.original_person_initials);			
		holder.shared_with = (LinearLayout) v.findViewById(R.id.shared_with);
		holder.graphButton = (ImageView) v.findViewById(R.id.graph_button);
		holder.addPerson = (ImageView) v.findViewById(R.id.add_person);
		
		String name="", snippet="", recipientId="";
		final String number;
		boolean read = true;        

        final int position= threads.getPosition();

    	read = (!threads.getString(1).equals("0"));	 
    	recipientId = threads.getString(2);
    	snippet = threads.getString(4);		    		    	

    	recipientID_hash.put(position, recipientId);
    	
    	holder.initials_view.setTypeface(null, Typeface.BOLD);	
    	holder.initials_view.setBackgroundResource(R.drawable.circle_blue);
    	if(store.getPhoneNumber(recipientId)==null) {
			number = ContentUtils.addCountryCode(ContentUtils.getAddressFromID(context, recipientId));
			store.setPhoneNumber(recipientId, number);
    	}		
    	else number = store.getPhoneNumber(recipientId);
    	
    	name = store.getName(number);
    	if(name==null || name.length() == 0 || ContentUtils.addCountryCode(name).equals(ContentUtils.addCountryCode(number))) {
    		name = ContentUtils.getContactDisplayNameByNumber(context, number);
    		store.setName(number, name);
    	}	
    	
    	final String friend_name = name;
    	final String friend_number = number;
    	holder.shared_with.setVisibility(View.VISIBLE);
    	
		holder.name_view.setText(store.getName(number));
		holder.snippet_view.setText(snippet);
		if(!read) {
			holder.read_indicator.setVisibility(View.VISIBLE);
		} else {
			holder.read_indicator.setVisibility(View.GONE);
		}  

    	holder.other_pic_mine.setVisibility(View.GONE);
		holder.other_pic.setVisibility(View.GONE);

    	if(!store.isFriend(number)) {
    		holder.image_view.setVisibility(View.GONE);
    		holder.initials_view.setVisibility(View.VISIBLE);
    		holder.initials_view.setText(ContentUtils.getInitials(name, number));    		
    		holder.initials_view.setOnClickListener(new OnClickListener(){
	
				@Override
				public void onClick(View v) {
					Log.i("number", number);
					View addFriendView = View.inflate(mContext, R.layout.add_friend, null);
					AlertDialog.Builder builder = new AlertDialog.Builder(activity);
				    builder.setTitle("Invite Friend");
				    builder.setMessage("Invite " + store.getName(number) + " to be your friend on Pull?")
				           .setCancelable(true)
				           .setView(addFriendView)
				           .setPositiveButton("No", new DialogInterface.OnClickListener() {
				               public void onClick(DialogInterface dialog, int id) 
				               {
				                    dialog.cancel();
				               }
				           })				           
				           .setNegativeButton("Yes", new DialogInterface.OnClickListener() {
				               public void onClick(DialogInterface dialog, int id)
				               {
	
				            	   SendUtils.inviteFriend(number, mContext, activity);
	
				               	}
				           }) 
				           .show();		
	
				}
    		});
    	} else if(isBindView && store.getPhotoPath(number)!=null) {
    		holder.initials_view.setVisibility(View.GONE);
    		holder.image_view.setVisibility(View.VISIBLE);
    		cu.loadBitmap(mContext, store.getPhotoPath(number),holder.image_view, 0);        		
    	}
  
    	shared_with_cursor = dh.getSharedWithCursor(number);
		sharedWithListView = (HListView) v.findViewById(R.id.shared_with_list);
		sharedWithAdapter = new SharedWithCursorAdapter(mContext,shared_with_cursor,activity);
		sharedWithListView.setAdapter(sharedWithAdapter);
		
		holder.graphButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				holder.graphButton.setBackgroundResource(R.drawable.graph_pressed);
				Intent broadcastIntent = new Intent();
				broadcastIntent.setAction(Constants.ACTION_GRAPH_TAB_CLICKED);
				broadcastIntent.putExtra(Constants.EXTRA_NAME, friend_name);
				broadcastIntent.putExtra(Constants.EXTRA_NUMBER, friend_number);
				mContext.sendBroadcast(broadcastIntent);	
			}
			
		});
		holder.addPerson.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				holder.addPerson.setBackgroundResource(R.drawable.add_pressed);
				Intent broadcastIntent = new Intent();
				broadcastIntent.setAction(Constants.ACTION_ADDPPL_TAB_CLICKED);
				broadcastIntent.putExtra(Constants.EXTRA_NAME, friend_name);
				broadcastIntent.putExtra(Constants.EXTRA_NUMBER, friend_number);
				mContext.sendBroadcast(broadcastIntent);	
			}
			
		});		
    	v.setTag(holder);
	}


	private void setSharedFields(Context context, Cursor threads, View v, boolean isBindView) {
		LinearLayout row = (LinearLayout) v.findViewById(R.id.row);
		TextView name_view = (TextView) v.findViewById(R.id.txt_title);
		TextView snippet_view = (TextView) v.findViewById(R.id.txt_message_info);
		ImageView read_indicator = (ImageView) v.findViewById(R.id.indicator);
		ImageView share = (ImageView) v.findViewById(R.id.share_button);

	    final ImageView other_pic_theirs = 
	    		(ImageView) v.findViewById(R.id.other_pic);
	    final ImageView other_pic_mine = 
	    		(ImageView) v.findViewById(R.id.other_pic_mine);
	    final ImageView other_pic;
	    
	    final String number, friend_name;
		String name="", facebookID="";
    	String confidante = threads.getString(0);
    	String originalRecipientName = threads.getString(1);
    	String sharer = threads.getString(2);	
    	int type = Integer.valueOf(threads.getString(4));

    	snippet_view.setVisibility(View.VISIBLE);
    	if(type == TextBasedSmsColumns.MESSAGE_TYPE_SENT) {
    		number = confidante;
    		other_pic = other_pic_mine;
    		friend_name = store.getName(number);
    		other_pic_theirs.setVisibility(View.GONE);
    		other_pic.setVisibility(View.VISIBLE);
    		row.setGravity(Gravity.RIGHT);
    		name_view.setGravity(Gravity.RIGHT);
    		snippet_view.setGravity(Gravity.RIGHT);
    		snippet_view.setText(originalRecipientName + " doesn't know you've shared");
    		
    	} else {
    		number = sharer;
    		other_pic = other_pic_theirs;
    		friend_name = store.getName(number);
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
    		//other_pic.setImageBitmap(null);
    		other_pic.setImageResource(R.drawable.add_ppl);
    		other_pic.setOnClickListener(new OnClickListener(){
	
				@Override
				public void onClick(View v) {
					View addFriendView = View.inflate(mContext, R.layout.add_friend, null);
					AlertDialog.Builder builder = new AlertDialog.Builder(activity);
				    builder.setTitle("Invite Friend");
				    builder.setMessage("Invite " + store.getName(number) + " to be your friend on Pull?")
				           .setCancelable(true)
				           .setView(addFriendView)
				           .setNegativeButton("Yes", new DialogInterface.OnClickListener() {
				               public void onClick(DialogInterface dialog, int id)
				               {
	
				            	   SendUtils.inviteFriend(number, mContext, activity);
	
				               	}
				           }) 
				           .setPositiveButton("No", new DialogInterface.OnClickListener() {
				               public void onClick(DialogInterface dialog, int id) 
				               {
				                    dialog.cancel();
				               }
				           }).show();		
	
				}
    		});
    	} else {
    		other_pic.setOnClickListener(null);
    		if(isBindView) cu.loadBitmap(mContext, store.getPhotoPath(number), other_pic ,R.drawable.add_ppl);
    	}
    	
    	
    	
    	//TODO: ADD DATA TO SHOW IF IT IS READ OR NOT
    	read_indicator.setVisibility(View.GONE);
    	
	}
	
	private static class ViewHolder
	{
		public ImageView addPerson;
		public ImageView graphButton;
		public LinearLayout shared_with;
		public TextView initials_view;
		public CircularImageView image_view;
		public LinearLayout graphArea;
		LinearLayout row;
		TextView name_view;
		TextView snippet_view;
		ImageView read_indicator;
		ImageView share;

	    ImageView other_pic_theirs;
	    ImageView other_pic_mine;
	    ImageView other_pic;
	    
	}
	

}
