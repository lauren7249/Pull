package com.Pull.pullapp.threads;
import java.util.ArrayList;
import java.util.Date;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

import com.Pull.pullapp.util.Constants;

public class DelayedMMSService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        super.onStartCommand(intent, flags, startId);
        if(intent==null)  return 0;
        String[] recipients = intent.getStringArrayExtra(Constants.EXTRA_RECIPIENTS);
        String message = intent.getStringExtra(Constants.EXTRA_MESSAGE_BODY);
        String thread_id = intent.getStringExtra(Constants.EXTRA_THREAD_ID);
        String approver = intent.getStringExtra(Constants.EXTRA_APPROVER);
        boolean isDelayed = intent.getBooleanExtra(Constants.EXTRA_IS_DELAYED, false);
        long launchedOn = intent.getLongExtra(Constants.EXTRA_TIME_LAUNCHED,0);
        long scheduledFor = intent.getLongExtra(Constants.EXTRA_TIME_SCHEDULED_FOR,0);        
        ArrayList<String> attachment_paths = intent.getStringArrayListExtra(Constants.EXTRA_ATTACHMENT_PATHS);
    	//Log.i("DelayedMMSService","DelayedMMSService attachments " + attachment_paths.size());
        ArrayList<Uri> uris = stringsToUris(attachment_paths);
		new SendMMS(this, recipients, message, launchedOn, scheduledFor, true, thread_id, uris ).run();
        return 1;

    }
    private ArrayList<Uri> stringsToUris(ArrayList<String> attachment_paths) {
		ArrayList<Uri> uris = new ArrayList<Uri>();
		for(String s : attachment_paths) {
			uris.add(Uri.parse(s));
		}
		// TODO Auto-generated method stub
		return uris ;
	}
	@Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}


}
