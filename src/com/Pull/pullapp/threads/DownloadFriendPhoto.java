package com.Pull.pullapp.threads;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.Pull.pullapp.util.ContentUtils;
import com.Pull.pullapp.util.UserInfoStore;
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class DownloadFriendPhoto extends Thread {

    private Context mContext;
    private String userID, number;
	private UserInfoStore store;

    
    public DownloadFriendPhoto(String userID, Context mContext, String number, UserInfoStore store) {
    	this.userID = userID;
    	this.mContext = mContext;
    	this.number = number;
    	this.store = store;
    }

    @Override
    public void run() {
       Log.i("userID",userID);
 	   ParseQuery<ParseUser> query = ParseUser.getQuery();
 	   query.getInBackground(userID, new GetCallback<ParseUser>(){

			@Override
			public void done(ParseUser user, ParseException e1) {
				if(e1==null && user !=null) {
					ParseFile photo = (ParseFile) user.get("phofilePhoto");
					if(photo!=null) {
						photo.getDataInBackground(new GetDataCallback() {
					        public void done(byte[] data, ParseException e2) {
					          if (e2 == null && data.length>0) {
					            // data has the bytes for the image

					              Bitmap bMap = BitmapFactory
					            		  .decodeByteArray(data, 0, data.length);
					              String photoPath = ContentUtils.saveToInternalStorage(mContext, bMap, number);
					              store.savePhotoPath(number,photoPath);
					          } else {
					        	  Log.e("getting profile photo",e2.getMessage());
					          }
					        }
					      });										
					} else Log.e("photo is null","null photo");
				}else {
		        	  Log.e("getting profile photo",e1.getMessage());
		          }				
				
			}
 		   
 	   });      
    }
    



    
}
