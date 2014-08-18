package com.Pull.pullapp.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.Pull.pullapp.R;
import com.Pull.pullapp.util.Constants;
import com.Pull.pullapp.util.ContentUtils;
import com.Pull.pullapp.util.UserInfoStore;
public class SharedWithCursorAdapter extends CursorAdapter {
	
	private Context mContext;
	protected Activity activity;
	private UserInfoStore store;
	private ContentUtils cu;
	public String current_tab = "";
    @SuppressWarnings("deprecation")
	public SharedWithCursorAdapter(Context context, Cursor cursor, Activity a) {
    	super(context, cursor);
    	mContext = context;  	
    	store = new UserInfoStore(context);
    	activity = a;
    	cu = new ContentUtils();
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

		holder.image_view = (ImageView) v.findViewById(R.id.person_image);
		holder.initials_view = (TextView) v.findViewById(R.id.person_initials);
		holder.cell = (LinearLayout) v.findViewById(R.id.shared_with_cell);

		String name="";
		final String number, person_shared, person_shared_name;

    	number = threads.getString(0);
    	person_shared = threads.getString(1);
    	person_shared_name = threads.getString(2);
    	
    	name = store.getName(number);
    	if(name==null || name.length() == 0 || name.equals(number)) {
    		name = ContentUtils.getContactDisplayNameByNumber(context, number);
    		store.setName(number, name);
    	}	
    //	Log.i("current tab",current_tab);
    	//Log.i("number tab",number);
    	if(current_tab.equals(number)) {
    		holder.initials_view.setBackgroundResource(R.drawable.circle_pressed);
    	} else {
    		holder.initials_view.setBackgroundResource(R.drawable.circle);
    	}
    	if(!store.isFriend(number)) {
    		holder.image_view.setVisibility(View.GONE);
    		holder.initials_view.setVisibility(View.VISIBLE);
    		holder.initials_view.setText(ContentUtils.getInitials(name));
    	} else {
    		holder.image_view.setVisibility(View.VISIBLE);
    		holder.initials_view.setVisibility(View.GONE);
    		cu.loadBitmap(mContext, store.getPhotoPath(number),holder.image_view, 0);
    	}
    	  	
    
    	holder.cell.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				current_tab = number;
				Intent broadcastIntent = new Intent();
				broadcastIntent.setAction(Constants.ACTION_SHARE_TAB_CLICKED);
				broadcastIntent.putExtra(Constants.EXTRA_SHARED_CONFIDANTE, number);
				broadcastIntent.putExtra(Constants.EXTRA_SHARED_NAME, person_shared_name);
				broadcastIntent.putExtra(Constants.EXTRA_SHARED_ADDRESS, person_shared);
				mContext.sendBroadcast(broadcastIntent);			

			}
    		
    	});   

    	v.setTag(holder);
	}


	
	private static class ViewHolder
	{
		public TextView initials_view;
		public ImageView image_view;
	    public LinearLayout cell;
	}
	

}
