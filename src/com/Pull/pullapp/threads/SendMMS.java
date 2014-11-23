package com.Pull.pullapp.threads;

import java.net.URL;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;

import com.Pull.pullapp.MessageActivityCheckboxCursor;
import com.Pull.pullapp.util.SendUtils;
import com.klinker.android.send_message.Utils;

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
        // try sending after 3 seconds anyways if for some reason the receiver doesn't work
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
            	MessageActivityCheckboxCursor.sendmms(context, recipients, message, launchedOn, scheduledFor, addToSent);

            }
        }, 7000);    	
    	//new Send().execute();

        
    }
    private class Send extends AsyncTask<Void, Void, Void> {

        private Exception exception;

        protected Void doInBackground(Void... urls) {
        	MessageActivityCheckboxCursor.sendmms(context, recipients, message, launchedOn, scheduledFor, addToSent);
        	return null;
        }

        protected void onPostExecute(Void feed) {
            // TODO: check this.exception 
            // TODO: do something with the feed
        }
    }    
}
