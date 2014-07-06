package com.Pull.pullapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.Pull.pullapp.util.Constants;
import com.Pull.pullapp.util.SendUtils;
import com.Pull.pullapp.util.UserInfoStore;
import com.actionbarsherlock.app.SherlockListActivity;

public class FriendsActivity extends SherlockListActivity {
	
	private Context mContext;
	private UserInfoStore store;
	private ListView listview;
	private String userID;
	private String name;
	private String number;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.friends_listactivity);
	    mContext = getApplicationContext();
	    store = new UserInfoStore(mContext); 
	    listview = getListView();
	    
	    userID = getIntent().getStringExtra(Constants.EXTRA_USER_ID);
	    name = getIntent().getStringExtra(Constants.EXTRA_NAME);
	    number = getIntent().getStringExtra(Constants.EXTRA_NUMBER);
	    
	    if(userID!=null) {
	    	//Toast.makeText(getApplicationContext(), userID, Toast.LENGTH_LONG).show();
			View addFriendView = View.inflate(mContext, R.layout.add_friend, null);
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
		    builder.setTitle("Friend Request");
		    builder.setMessage(name + " asked to be your friend on Pull. Your use of Pull is still our secret, " +
		    " so feel free to reject. " + name + " will not know the difference.")
		           .setCancelable(true)
		           .setView(addFriendView)
		           .setPositiveButton("Accept", new DialogInterface.OnClickListener() {
		               public void onClick(DialogInterface dialog, int id)
		               {

		            	   store.saveFriend(number, userID);
		            	   SendUtils.confirmFriend(number, userID);
		            	   Intent i = new Intent(mContext, AllThreadsListActivity.class);
		            	   startActivity(i);

		               	}
		           }) 
		           .setNegativeButton("Reject", new DialogInterface.OnClickListener() {
		               public void onClick(DialogInterface dialog, int id) 
		               {
		                    dialog.cancel();
			            	Intent i = new Intent(mContext, AllThreadsListActivity.class);
			            	startActivity(i);		                    
		               }
		           }).show();		    	
	    }
	}
	
}
