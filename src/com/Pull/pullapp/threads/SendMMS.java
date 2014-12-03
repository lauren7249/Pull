package com.Pull.pullapp.threads;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.Pull.pullapp.mms.ItemLoadedCallback;
import com.Pull.pullapp.mms.ThumbnailManager;
import com.Pull.pullapp.mms.ThumbnailManager.ImageLoaded;
import com.Pull.pullapp.util.SendUtils;
import com.klinker.android.send_message.DeviceMessageAttachment;

public class SendMMS extends Thread {

    
    private Context context;
	private String[] recipients;
	private String message;
	private long launchedOn;
	private long scheduledFor;
	private boolean addToSent;
	private BroadcastReceiver mBroadcastReceiver;
	private String thread_id;
	private ArrayList<DeviceMessageAttachment> attachments;
	private MimeTypeMap mime = MimeTypeMap.getSingleton();
	public SendMMS(Context parent, String[] recipients,
			String message, long launchedOn, long scheduledFor, boolean b, 
			String threadID, ArrayList<Uri> mms_uris) {
		this.context = parent;
		this.recipients = recipients;
		this.message= message;
		this.launchedOn = launchedOn;
		this.scheduledFor = scheduledFor;
		this.addToSent = b;
		this.thread_id = threadID;	
		
		getImages(mms_uris);
		
	}
	
	private synchronized void getImages(ArrayList<Uri> mms_uris) {
		if(mms_uris!=null && mms_uris.size()>0) {
			final int total = mms_uris.size();
			attachments = new ArrayList<DeviceMessageAttachment>();
			ThumbnailManager t = new ThumbnailManager(context);
			for(final Uri u: mms_uris) {
				t.getThumbnail(u, new ItemLoadedCallback<ImageLoaded> (){

					@Override
					public void onItemLoaded(ImageLoaded result, Throwable exception) {
						if(exception!=null) {
							exception.printStackTrace();
							return;
						}
						if(result!=null) {
							byte[] bytes = compressBitmap(result.mBitmap);
							DeviceMessageAttachment d = new DeviceMessageAttachment(u.getLastPathSegment(), 
									context.getContentResolver().getType(u) , 
									bytes);	
							attachments.add(d);
							if(attachments.size()==total) {
						    	SendUtils.sendmms(context, recipients, message, 
						    			launchedOn, scheduledFor, addToSent, thread_id, attachments);

							}
						}
						
					}
					
				});
												
			}
				
		}		
		else {
	    	SendUtils.sendmms(context, recipients, message, 
	    			launchedOn, scheduledFor, addToSent, thread_id, attachments);
			
		}
	}
    @Override
    public void run() {

        
    }
    public static byte[] compressBitmap(Bitmap bitmap) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,
                90, os);
        return os.toByteArray();
    }	
}
