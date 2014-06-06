package com.Pull.pullapp;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.Telephony.ThreadsColumns;
import android.telephony.PhoneNumberUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.Pull.pullapp.model.ThreadItem;
import com.Pull.pullapp.util.Constants;
import com.Pull.pullapp.util.ContentUtils;

public class ThreadItemsListAdapter extends ArrayAdapter<ThreadItem> {
    private ArrayList<ThreadItem> objects;
    private Context context;
    private GetThreads loader;
    public ThreadItemsListAdapter(Context context, int textViewResourceId,
        List<ThreadItem> objects) {
      super(context, textViewResourceId, objects);
      this.objects = (ArrayList<ThreadItem>) objects;
      this.context = context;
      this.loader = new GetThreads();
      loader.execute(); 
    }
    @Override
    public synchronized View getView(int pos, View convertView, ViewGroup parent){    
    	View v = convertView;
    	if (v == null) {
	        LayoutInflater vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	        v = vi.inflate(R.layout.message_list_item, parent, false);
    	}
	   TextView name = (TextView) v.findViewById(R.id.txt_title);
	   TextView snippet = (TextView) v.findViewById(R.id.txt_message_info);
	   final ThreadItem th = objects.get(pos);
	   name.setText(th.displayName);
	   snippet.setText(th.snippet);
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
	private class GetThreads extends AsyncTask<Void,ThreadItem,Void> {
	  	Cursor threads;
	  	@Override
		protected Void doInBackground(Void... params) {
			threads = ContentUtils.getThreadsCursor(context);
			while (threads.moveToNext()) {
		    	String threadID = threads.getString(threads
		  		      .getColumnIndex(ThreadsColumns._ID));
		    	String snippet = threads.getString(threads
			  		      .getColumnIndex(ThreadsColumns.SNIPPET));			    	
		    	if(threadID == null) continue;
		    	if(threadID.length()==0) continue;
		    	boolean read = (!threads.getString(threads
			  		      .getColumnIndex(ThreadsColumns.READ)).equals("0"));	    	
				String[] recipientIDs = threads.getString(threads
			      .getColumnIndex(ThreadsColumns.RECIPIENT_IDS)).split(" ");

				if(recipientIDs.length == 1 && recipientIDs[0].length()>0) {
					String recipientId = recipientIDs[0];
					String number = ContentUtils.getAddressFromID(context, recipientId);
					String name = ContentUtils
							.getContactDisplayNameByNumber(context, number);
					ThreadItem t = new ThreadItem(threadID, name, number, snippet, read);
					publishProgress(t);						
					if (isCancelled()) break;
				}
				
		    }	
			return null;
		}
		@Override
	    protected void onProgressUpdate(ThreadItem... t) {
			addItem(t[0]);	
			notifyDataSetChanged();
	    }				
		
		@Override
	    protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			threads.close();
	    }			

  }    
}
