package com.Pull.pullapp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.Pull.pullapp.model.SharedConversation;
import com.Pull.pullapp.model.ThreadItem;
import com.Pull.pullapp.R;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class SharedConversationsListAdapter extends ArrayAdapter<SharedConversation> {
    private List<SharedConversation> objects;
    private Context context;
    public SharedConversationsListAdapter(Context context, int textViewResourceId,
        List<SharedConversation> objects) {
      super(context, textViewResourceId, objects);
      this.objects = (List<SharedConversation>) objects;
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
	   SharedConversation th = objects.get(pos);
	   name.setText(th.getOriginalRecipient());
////	   if(!th.read) v.setBackgroundResource(R.drawable.unread_row);
//	   else v.setBackgroundResource(R.drawable.read_row);
	   return v;
    }
    @Override
    public long getItemId(int position) {
    	return 0;
    }
	public SharedConversation getItem(int position) {		
		return objects.get(position);
	}
    @Override
    public boolean hasStableIds() {
      return true;
    }
    public void setItemList(List<SharedConversation> itemList) {
        this.objects = itemList;
    }
    public void addItem(SharedConversation item) {
        this.objects.add(item);
    }
}
