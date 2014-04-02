package com.Pull.smsTest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.Pull.smsTest.model.ThreadItem;
import com.Pull.smsTest.R;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ThreadItemsListAdapter extends ArrayAdapter<ThreadItem> {
    private ArrayList<ThreadItem> objects;
    private Context context;
    public ThreadItemsListAdapter(Context context, int textViewResourceId,
        List<ThreadItem> objects) {
      super(context, textViewResourceId, objects);
      this.objects = (ArrayList<ThreadItem>) objects;
      this.context = context;
    }
    @Override
    public View getView(int pos, View convertView, ViewGroup parent){    
    	View v = convertView;
    	if (v == null) {
	        LayoutInflater vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	        v = vi.inflate(R.layout.message_list_item, parent, false);
    	}
	   TextView name = (TextView) v.findViewById(R.id.txt_title);
	   ThreadItem th = objects.get(pos);
	   name.setText(th.displayName);
	   if(!th.read) v.setBackgroundResource(R.drawable.unread_row);
	   else v.setBackgroundResource(R.drawable.read_row);
	   return v;
    }
    @Override
    public long getItemId(int position) {
    	return 0;
    }
	public ThreadItem getItem(int position) {		
		return objects.get(position);
	}
    @Override
    public boolean hasStableIds() {
      return true;
    }
    public void setItemList(ArrayList<ThreadItem> itemList) {
        this.objects = itemList;
    }
    public void addItem(ThreadItem item) {
        this.objects.add(item);
    }
}
