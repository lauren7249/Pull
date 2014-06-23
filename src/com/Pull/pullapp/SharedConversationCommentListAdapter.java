package com.Pull.pullapp;

import java.util.ArrayList;
import java.util.List;

import android.content.ClipData;
import android.content.Context;
import android.content.SharedPreferences;
import android.telephony.PhoneNumberUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.Pull.pullapp.model.Comment;
import com.Pull.pullapp.model.FacebookUser;
import com.Pull.pullapp.util.ContentUtils;
import com.facebook.widget.ProfilePictureView;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class SharedConversationCommentListAdapter extends BaseAdapter {
	private Context mContext;
	private ArrayList<Comment> mComments;
	private String mConfidante, mOriginalRecipientName;
	private SharedPreferences mPrefs;
	private MainApplication mApp;
	public SharedConversationCommentListAdapter(Context context, ArrayList<Comment> comments, String confidante, String orig_recipient) {
		super();
		this.mContext = context;
		this.mComments = comments;
		this.mConfidante = confidante;
		this.mOriginalRecipientName = orig_recipient;
		mApp = (MainApplication) context.getApplicationContext();
		//mPrefs = context.getSharedPreferences(MainApplication.class.getSimpleName(), Context.MODE_PRIVATE);
	}

	@Override
	public int getCount() {
		return mComments.size();
	}

	@Override
	public Object getItem(int position) {
		return mComments.get(position);
	}
	
	
	public void setItemList(ArrayList<Comment> comments) {
		this.mComments = comments;
		notifyDataSetChanged();
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public int getViewTypeCount() {
	    return 2;
	}
	
	@Override
	public int getItemViewType(int position) {
		final Comment comment = mComments.get(position);
		if(mConfidante.equals(comment.getSender())){
			return 0;
		}
		return 1;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final Comment comment = mComments.get(position);

		final SMSViewHolder holder; 
		if(convertView == null)
		{
			holder = new SMSViewHolder();
			Log.v("Display", mConfidante+":"+comment.getSender()+":"+comment.getMessage());
			if(!PhoneNumberUtils.compare(mApp.getUserName(),comment.getSender())){
				convertView = LayoutInflater.from(mContext).inflate(R.layout.shared_comment_incoming_row, parent, false);
				//Log.i("current user ", mApp.getUserName());
				//Log.i("comment sender ",comment.getSender());
			}else{
				convertView = LayoutInflater.from(mContext).inflate(R.layout.shared_comment_outgoing_row, parent, false);
				holder.retry = (Button) convertView.findViewById(R.id.retry_button);
				if(comment.failedToDeliver) holder.retry.setVisibility(View.VISIBLE);
				else holder.retry.setVisibility(View.GONE);
		        holder.retry.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						//do something
					};
		        });    				
			}
		    final ProfilePictureView profilePictureView = (ProfilePictureView) convertView.findViewById(R.id.contact_image);
			ParseQuery<FacebookUser> fbQuery = ParseQuery.getQuery("FacebookUser");
			fbQuery.whereEqualTo("phoneNumber", comment.getSender());
			fbQuery.findInBackground(new FindCallback<FacebookUser>() {
			public void done(List<FacebookUser> results, ParseException e) {
				if(e==null && results.size()>0) {
					profilePictureView.setProfileId(results.get(0).getString("facebookID"));
				} else {
					profilePictureView.setProfileId("");
				}
			  }
			});  	 
			holder.message = (TextView) convertView.findViewById(R.id.message_text);
			holder.time = (TextView) convertView.findViewById(R.id.message_time);
			holder.box = (LinearLayout) convertView.findViewById(R.id.message_box);
			
			convertView.setTag(holder);

		}
		else holder = (SMSViewHolder) convertView.getTag();
		holder.message.setText(comment.getMessage());
		
		if(comment.isProposal()) {
			holder.time.setText("Drag up to send to " + mOriginalRecipientName);
			convertView.setOnLongClickListener(new DragTouchListener());
			
		} else {
			CharSequence relativeTime;
			if(System.currentTimeMillis()-comment.getDate()>DateUtils.MINUTE_IN_MILLIS){
				relativeTime = DateUtils.getRelativeDateTimeString(mContext, comment.getDate(), DateUtils.MINUTE_IN_MILLIS, DateUtils.DAY_IN_MILLIS, 0);
			}else{
				relativeTime = "Just Now";
			}		
			holder.time.setText(relativeTime);
		}

		
		
		
		return convertView;
	}
	
	public static class SMSViewHolder
	{
		TextView time;
		TextView message;
		LinearLayout box;
		Button retry;
	}

   public static class DragTouchListener implements OnLongClickListener {
	  public boolean onLongClick(View view) {
	      ClipData data = ClipData.newPlainText("", "");
	      DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
	      view.startDrag(data, shadowBuilder, view, 0);
	      view.setVisibility(View.INVISIBLE);
	      return true;
	  } 
	} 	
}
