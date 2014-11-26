package com.Pull.pullapp.threads;

import java.net.URL;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;

import com.Pull.pullapp.MessageActivityCheckboxCursor;
import com.Pull.pullapp.util.Constants;
import com.Pull.pullapp.util.SendUtils;
import com.klinker.android.send_message.Utils;

public class SendMMS extends Thread {

    
    private Context context;
	private String[] recipients;
	private String message;
	private long launchedOn;
	private long scheduledFor;
	private boolean addToSent;
	private BroadcastReceiver mBroadcastReceiver;
	private String thread_id;

	public SendMMS(Context parent, String[] recipients,
			String message, long launchedOn, long scheduledFor, boolean b, String threadID) {
		this.context = parent;
		this.recipients = recipients;
		this.message= message;
		this.launchedOn = launchedOn;
		this.scheduledFor = scheduledFor;
		this.addToSent = b;
		this.thread_id = threadID;		
	}

    @Override
    public void run() {

    	SendUtils.sendmms(context, recipients, message, launchedOn, scheduledFor, addToSent, thread_id);

        
    }

}
