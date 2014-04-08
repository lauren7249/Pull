package com.Pull.pullapp;

import java.util.ArrayList;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.Pull.pullapp.model.Comment;

public class SharedConversationCommentListAdapter extends BaseAdapter {
	private Context mContext;
	private ArrayList<Comment> mComments;
	
	public SharedConversationCommentListAdapter(Context context, ArrayList<Comment> comments) {
		super();
		this.mContext = context;
		this.mComments = comments;
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
	public View getView(int position, View convertView, ViewGroup parent) {
		final Comment comment = mComments.get(position);

		final SMSViewHolder holder; 
		if(convertView == null)
		{
			holder = new SMSViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.shared_comment_row, parent, false);
			holder.message = (TextView) convertView.findViewById(R.id.message_text);
			holder.time = (TextView) convertView.findViewById(R.id.message_time);
			convertView.setTag(holder);
		}
		else holder = (SMSViewHolder) convertView.getTag();
		holder.message.setText(comment.getMessage());
	
		
		CharSequence relativeTime = DateUtils.getRelativeDateTimeString(mContext, comment.getDate(), DateUtils.SECOND_IN_MILLIS, DateUtils.DAY_IN_MILLIS, 0);
		holder.time.setText(relativeTime);		
		
		return convertView;
	}
	
	private static class SMSViewHolder
	{
		TextView time;
		TextView message;
	}
	
}
