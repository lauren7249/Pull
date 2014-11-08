package com.Pull.pullapp.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.provider.Telephony.TextBasedSmsColumns;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.Pull.pullapp.AllThreadsListActivity;
import com.Pull.pullapp.R;
import com.Pull.pullapp.fragment.SimplePopupWindow;
import com.Pull.pullapp.util.Constants;
import com.Pull.pullapp.util.ContentUtils;
import com.Pull.pullapp.util.UserInfoStore;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.parse.ParseUser;
public class SharedWithCursorAdapter extends CursorAdapter {
	
	private Context mContext;
	protected Activity activity;
	private UserInfoStore store;
	private ContentUtils cu;
	private String current_tab = "";
	private Cursor cursor;
    @SuppressWarnings("deprecation")
	public SharedWithCursorAdapter(Context context, Cursor cursor, Activity a) {
    	super(context, cursor);
    	mContext = context;  	
    	store = new UserInfoStore(context);
    	activity = a;
    	cu = new ContentUtils();
    	this.cursor = cursor;
    }

	@Override
	public void bindView(View v, Context context, Cursor threads) {
		populateFields(v, context, threads, true);
		
	}

	@Override
	public View newView(Context context, Cursor threads, ViewGroup parent) {
		
        final LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.shared_with_item, parent, false);
        
        populateFields(v, context, threads, false);
		return v;
	}

	private void populateFields(View v, Context context, Cursor threads, boolean isBindView) {
			setFields(context,threads,v, isBindView);
	}
	private void setFields(Context context, Cursor threads, View v, boolean isBindView) {
		final ViewHolder holder; 
		if(!isBindView) holder = new ViewHolder();
		else holder = (ViewHolder) v.getTag();		
		final int position = threads.getPosition();
		holder.image_view = (CircularImageView) v.findViewById(R.id.person_image);
		holder.initials_view = (TextView) v.findViewById(R.id.person_initials);
		holder.cell = (LinearLayout) v.findViewById(R.id.shared_with_cell);

		String name="";
		final String number, clueless_persons_number, clueless_persons_name;

    	number = threads.getString(0);
    	clueless_persons_number = threads.getString(1);
    	clueless_persons_name = threads.getString(2);
    	final String convoID = threads.getString(3);
    	
    	name = store.getName(number);
    	if(name==null || name.length() == 0 || name.equals(number)) {
    		name = ContentUtils.getContactDisplayNameByNumber(context, number);
    		store.setName(number, name);
    	}	
    //	Log.i("current tab",current_tab);
    	//Log.i("number tab",number);

    	if(!store.isFriend(number) || store.getPhotoPath(number)==null) {
    		holder.image_view.setVisibility(View.GONE);
    		holder.initials_view.setVisibility(View.VISIBLE);
    		holder.initials_view.setText(ContentUtils.getInitials(name, number));
        	if(current_tab.equals(number)) {
        		holder.initials_view.setBackgroundResource(R.drawable.circle_pressed);
        		holder.initials_view.setTypeface(null, Typeface.BOLD);
        		holder.initials_view.setSelected(true);
        	} else {
        		holder.initials_view.setBackgroundResource(R.drawable.circle);
        		holder.initials_view.setTypeface(null, Typeface.NORMAL);
        		holder.initials_view.setSelected(false);
        	}    		
    	} else {
    		holder.image_view.setVisibility(View.VISIBLE);
    		holder.initials_view.setVisibility(View.GONE);
    		cu.loadBitmap(mContext, store.getPhotoPath(number),holder.image_view, 0);	
    		
    	}
    	  	
    	final String final_name = name;
    	final String final_number = number;
    	holder.cell.setOnClickListener(new OnClickListener(){

			private SimplePopupWindow popup;

			@Override
			public void onClick(View v) {
				if(holder.initials_view.isSelected()) {
					popup = new SimplePopupWindow(v);
					popup.showLikePopDownMenu();
					popup.setMessage("Privately message " + final_name );	
				} else {
					current_tab = number;
					holder.initials_view.setBackgroundResource(R.drawable.circle_pressed);
        			if(activity.getLocalClassName().equals("MessageActivityCheckboxCursor")) {
        				Intent broadcastIntent = new Intent();
						broadcastIntent.setAction(Constants.ACTION_SHARE_TAB_CLICKED);
						broadcastIntent.putExtra(Constants.EXTRA_TAB_POSITION, position);
						mContext.sendBroadcast(broadcastIntent);	
        			} else  {	
        				
    					AllThreadsListActivity.openSharedConvo(activity,convoID, 
    							ParseUser.getCurrentUser().getUsername(), 
    							clueless_persons_number, clueless_persons_name, final_number, 
    							TextBasedSmsColumns.MESSAGE_TYPE_SENT);    				
        			}
					holder.initials_view.setSelected(true);
				}
			}
    		
    	});   

    	v.setTag(holder);
	}


	
	private static class ViewHolder
	{
		public TextView initials_view;
		public CircularImageView image_view;
	    public LinearLayout cell;
	}



	public void setCurrentTab(String shared_confidante) {
		current_tab = shared_confidante;
		
	}
	

}
