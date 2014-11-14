package com.Pull.pullapp.adapter;
import it.sephiroth.android.library.widget.HListView;

import java.util.Map;
import java.util.Map.Entry;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.text.format.DateUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.Pull.pullapp.R;
import com.Pull.pullapp.model.MMSMessage;
import com.meetme.android.horizontallistview.HorizontalListView;

public class MMSAdapter extends BaseAdapter{
	private Context mContext;
	private Object[] mMessages;

	public MMSAdapter(Context context, Object[] mms_array) {
		super();
		this.mContext = context;
		this.mMessages = mms_array;
	}

	public int getCount() {
		return mMessages.length;
	}
	public MMSMessage getItem(int position) {		
		Map.Entry<Long,MMSMessage> entry = (Map.Entry<Long, MMSMessage>) mMessages[position];
		return entry.getValue();
	}

	@SuppressLint("ResourceAsColor")
	public View getView(final int position, View convertView, ViewGroup parent) {
		final MMSMessage message = getItem(position);

		final ViewHolder holder; 
		if(convertView == null)
		{
			holder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.mms_row, parent, false);
			holder.messageBox = (LinearLayout) convertView.findViewById(R.id.message_box);
			holder.message = (TextView) convertView.findViewById(R.id.message_text);
			holder.time = (TextView) convertView.findViewById(R.id.message_time);
			holder.pictures_list = (HListView) convertView.findViewById(R.id.pictures_list);
		}
		else holder = (ViewHolder) convertView.getTag();
		
		convertView.setTag(holder);		
		holder.message.setText(message.getMessage());
		holder.pictures_list.setAdapter(new PicturesAdapter(mContext,R.layout.picture_item, message.getImages()));
		LayoutParams lp = (LayoutParams) holder.messageBox.getLayoutParams();

		CharSequence relativeTime;
		if(System.currentTimeMillis()-message.getDate()>DateUtils.MINUTE_IN_MILLIS){
			relativeTime = DateUtils.getRelativeDateTimeString(mContext, message.getDate(), 
					DateUtils.MINUTE_IN_MILLIS, DateUtils.DAY_IN_MILLIS, 0);
		}else{
			relativeTime = "Just Now";
		}
		holder.time.setText(relativeTime);
		if(message.isSentByMe()) {
			convertView.setBackgroundResource(R.drawable.outgoing);
			holder.message.setTextColor(mContext.getResources().getColor(R.color.lightGreen));
			holder.time.setTextColor(mContext.getResources().getColor(R.color.lightGreen));
			holder.message.setPadding(50, 0, 10, 0);
			holder.time.setPadding(50, 0, 10, 0);
			lp.gravity = Gravity.LEFT;
			holder.message.setGravity(Gravity.RIGHT);
			holder.time.setGravity(Gravity.RIGHT);				
		}else {
			convertView.setBackgroundResource(R.drawable.incoming);  
			holder.message.setTextColor(Color.WHITE);
			holder.time.setTextColor(Color.WHITE);					
			holder.message.setPadding(10, 0, 50, 0);
			holder.time.setPadding(10, 0, 50, 0);
			lp.gravity = Gravity.RIGHT;
			holder.message.setGravity(Gravity.LEFT);
			holder.time.setGravity(Gravity.LEFT);					
		}
		holder.messageBox.setLayoutParams(lp);
		return convertView;
	}
	private static class ViewHolder
	{

		public HListView pictures_list;
		public TextView time;
		TextView message;
		LinearLayout messageBox;
	}
	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return (long) getItem(position).hashCode();
	}

 
}
