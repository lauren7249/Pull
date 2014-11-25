package com.Pull.pullapp.threads;
import java.util.Date;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.Pull.pullapp.util.Constants;

public class DelayedMMSService extends Service {

    private static final String ACTION=Constants.ACTION_SEND_DELAYED_MMS;
    private BroadcastReceiver yourReceiver;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
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
        Log.i("service started","service started + " + recipients[0]);
        new SendMMS(this, recipients, message, launchedOn, scheduledFor, true, thread_id).run();
        return 1;

    }
    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("service created","service created + ");
        //new SendMMS(parent, recipients, message, launchedOn, sendOn, true);
       /* final IntentFilter theFilter = new IntentFilter();
        theFilter.addAction(ACTION);
        this.yourReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                // Do whatever you need it to do when it receives the broadcast
                // Example show a Toast message...
                showSuccessfulBroadcast(context);
            }
        };
        // Registers the receiver so that your service will listen for
        // broadcasts
        this.registerReceiver(this.yourReceiver, theFilter);*/
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Do not forget to unregister the receiver!!!
       // this.unregisterReceiver(this.yourReceiver);
    }

    private void showSuccessfulBroadcast(Context parent) {
    	
    }
}
