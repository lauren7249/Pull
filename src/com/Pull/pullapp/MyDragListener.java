package com.Pull.pullapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.DragEvent;
import android.view.View;
import android.view.View.OnDragListener;
import android.widget.Toast;

import com.Pull.pullapp.SharedConversationCommentListAdapter.SMSViewHolder;
import com.Pull.pullapp.model.SharedConversation;
import com.Pull.pullapp.util.Constants;
import com.Pull.pullapp.util.ContentUtils;
import com.Pull.pullapp.util.SendMessages;

public class MyDragListener implements OnDragListener {
	
	final private SharedConversationActivity activity;
	private SharedConversation convo;
	private Intent broadcastIntent;
	private MainApplication mApp;
	

	public MyDragListener(SharedConversationActivity activity, SharedConversation convo) {
		this.activity = activity;
		this.convo = convo;
		this.mApp = (MainApplication) activity.getApplication();
	}
	@Override
	public boolean onDrag(View v, DragEvent event) {
	    int action = event.getAction();
	    switch (event.getAction()) {
	    case DragEvent.ACTION_DRAG_STARTED:
	   
	      break;
	    case DragEvent.ACTION_DRAG_ENTERED:
	    	// do nothing
	      break;
	    case DragEvent.ACTION_DRAG_EXITED:   
	    	 // do nothing
	      break;
	    case DragEvent.ACTION_DROP:
	      // Dropped in the send area - confirm with user
	    	View view = (View) event.getLocalState();
	    	SMSViewHolder holder = (SMSViewHolder) view.getTag();
	    	final String message = holder.message.getText().toString();

	    	AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		    builder.setTitle("Send this text to " + 
		    	ContentUtils.getContactDisplayNameByNumber(activity.getApplicationContext(),convo.getOriginalRecipient()) 
		    	+ "?");
		    builder.setMessage(message)
		           .setCancelable(true)
		           .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		               public void onClick(DialogInterface dialog, int id) {
		            	   dialog.cancel();
							broadcastIntent = new Intent();
							broadcastIntent.setAction(Constants.ACTION_SEND_COMMENT_CONFIRMED);
							activity.getApplicationContext().sendBroadcast(broadcastIntent);	
							if(convo.getSharer().equals(mApp.getUserName()))
								SendMessages.sendsms(activity.getApplicationContext(), 
									convo.getOriginalRecipient(), message, System.currentTimeMillis(), true);
							else {
								Toast.makeText(activity.getApplicationContext(), "not yet implemented for friends",
										Toast.LENGTH_LONG).show();
							}
		               	}
		           })
		           .setNegativeButton("No", new DialogInterface.OnClickListener() {
		               public void onClick(DialogInterface dialog, int id) {
		                    dialog.cancel();
							broadcastIntent = new Intent();
							broadcastIntent.setAction(Constants.ACTION_SEND_COMMENT_CANCELED);
							activity.getApplicationContext().sendBroadcast(broadcastIntent);			                    
		               }
		           }).show();		    	
	      break;
	    case DragEvent.ACTION_DRAG_ENDED:
			broadcastIntent = new Intent();
			broadcastIntent.setAction(Constants.ACTION_SEND_COMMENT_CANCELED);
			activity.getApplicationContext().sendBroadcast(broadcastIntent);	
	    default:
	    	break;
	    }
	    return true;
	}

}
