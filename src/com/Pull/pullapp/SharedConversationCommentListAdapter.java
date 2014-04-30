package com.Pull.pullapp;

import java.util.ArrayList;
import java.util.List;

import android.content.ClipData;
import android.content.Context;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.Pull.pullapp.model.Comment;
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
	
	public SharedConversationCommentListAdapter(Context context, ArrayList<Comment> comments, String confidante, String orig_recipient) {
		super();
		this.mContext = context;
		this.mComments = comments;
		this.mConfidante = confidante;
		this.mOriginalRecipientName = orig_recipient;
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
			if(mConfidante.equals(comment.getSender())){
				convertView = LayoutInflater.from(mContext).inflate(R.layout.shared_comment_incoming_row, parent, false);
			}else{
				convertView = LayoutInflater.from(mContext).inflate(R.layout.shared_comment_outgoing_row, parent, false);
			}
		    final ProfilePictureView profilePictureView = (ProfilePictureView) convertView.findViewById(R.id.contact_image);
			ParseQuery<ParseUser> userQuery = ParseUser.getQuery();
			userQuery.whereEqualTo("username", ContentUtils.addCountryCode(comment.getSender()));
			userQuery.findInBackground(new FindCallback<ParseUser>() {
			public void done(List<ParseUser> results, ParseException e) {
				if(e == null && results.size()>0) {
					profilePictureView.setProfileId(results.get(0).getString("facebookID"));
					profilePictureView.setPresetSize(ProfilePictureView.LARGE);
				}
			  }
			}); 
			holder.message = (TextView) convertView.findViewById(R.id.message_text);
			holder.time = (TextView) convertView.findViewById(R.id.message_time);
			
			convertView.setTag(holder);
			if(comment.isProposal()) {
				convertView.setOnTouchListener(new MyTouchListener());
				//profilePictureView.setVisibility(View.GONE);
				//LayoutParams lp = (LayoutParams) convertView.getLayoutParams();
				//lp.gravity = Gravity.CENTER;
			}
		}
		else holder = (SMSViewHolder) convertView.getTag();
		holder.message.setText(comment.getMessage());
		
		if(comment.isProposal()) {
			holder.time.setText("Drag up to send to " + mOriginalRecipientName);
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
	}
	// This defines your touch listener
	private final class MyTouchListener implements OnTouchListener {
	  public boolean onTouch(View view, MotionEvent motionEvent) {
	    if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
	      ClipData data = ClipData.newPlainText("", "");
	      DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
	      view.startDrag(data, shadowBuilder, view, 0);
	      view.setVisibility(View.INVISIBLE);
	      return true;
	    } else {
	    return false;
	    }
	  }
	} 	
}
