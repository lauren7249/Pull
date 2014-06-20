package com.Pull.pullapp;

import java.util.List;

import android.content.Context;
import android.provider.Telephony.TextBasedSmsColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.Pull.pullapp.model.FacebookUser;
import com.Pull.pullapp.model.SharedConversation;
import com.Pull.pullapp.util.ContentUtils;
import com.facebook.widget.ProfilePictureView;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class SharedConversationsListAdapter extends ArrayAdapter<SharedConversation> {
    private List<SharedConversation> objects;
    private Context context;
    private int type;
    public SharedConversationsListAdapter(Context context, int textViewResourceId,
        List<SharedConversation> objects, int type) {
      super(context, textViewResourceId, objects);
      this.objects = (List<SharedConversation>) objects;
      this.context = context;
      this.type = type;
    }
    @Override
    public View getView(int pos, View convertView, ViewGroup parent){
    	View v = convertView;
    	if (v == null) {
	        LayoutInflater vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	        v = vi.inflate(R.layout.message_list_item, parent, false);
    	}
  	
       final ProfilePictureView profilePictureView = (ProfilePictureView) v.findViewById(R.id.profile_pic);
	   final TextView name = (TextView) v.findViewById(R.id.txt_title);
	   TextView info = (TextView) v.findViewById(R.id.txt_message_info);
	   final SharedConversation th = objects.get(pos);
	   
	   //info.setText(th.getHashtags());
	   
	   final String conversant;
	   if(type == TextBasedSmsColumns.MESSAGE_TYPE_SENT) 
		   conversant = ContentUtils.addCountryCode(th.getConfidante());
	   else {
		   conversant = ContentUtils.addCountryCode(th.getSharer());
		   //Log.i("conversant",conversant);
	   }
	   
		ParseQuery<FacebookUser> fbQuery = ParseQuery.getQuery("FacebookUser");
		fbQuery.whereEqualTo("phoneNumber", conversant);
		fbQuery.findInBackground(new FindCallback<FacebookUser>() {
		public void done(List<FacebookUser> results, ParseException e) {
			if(e==null && results.size()>0) {
				profilePictureView.setProfileId(results.get(0).getString("facebookID"));
			} else {
				profilePictureView.setProfileId("");
				//profilePictureView.setVisibility(View.GONE);
			}
		  }
		});  	 
		name.setText("Talking about " + th.getOriginalRecipientName());
		info.setText("with " + ContentUtils.getContactDisplayNameByNumber(context, conversant));
		
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
