package com.Pull.pullapp.threads;

import android.content.Context;

import com.Pull.pullapp.MessageActivityCheckboxCursor;

public class SendMMS extends Thread {

    
    private Context context;
	private String[] recipients;
	private String message;
	private long launchedOn;
	private long scheduledFor;
	private boolean addToSent;

	public SendMMS(Context parent, String[] recipients,
			String message, long launchedOn, long scheduledFor, boolean b) {
		this.context = parent;
		this.recipients = recipients;
		this.message= message;
		this.launchedOn = launchedOn;
		this.scheduledFor = scheduledFor;
		this.addToSent = b;
	}

    @Override
    public void run() {
    	
    	MessageActivityCheckboxCursor.sendmms(context, recipients, message, launchedOn, scheduledFor, addToSent);

        
    }
    
}
