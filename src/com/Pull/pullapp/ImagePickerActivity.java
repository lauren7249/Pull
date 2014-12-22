package com.Pull.pullapp;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;

import com.Pull.pullapp.fragment.SimplePopupWindow;
import com.Pull.pullapp.threads.UploadMyPhoto;
import com.Pull.pullapp.util.Constants;
import com.Pull.pullapp.util.Transformation;
import com.Pull.pullapp.util.data.UserInfoStore;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
 
public class ImagePickerActivity extends Activity {
 
	private final int SELECT_PHOTO = 1;
	private ImageButton imageView;
	private UserInfoStore store;
	private IntentFilter intentFilter;
	private BroadcastReceiver mBroadcastReceiver;
	private String username;
	private static Context mContext;
	private Bitmap bitmap;
	private Button pickImage;
	private MixpanelAPI mixpanel;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photopick);
        setTitle("Profile Picture");
        imageView = (ImageButton)findViewById(R.id.imageView);
        mContext = getApplicationContext();
        store = new UserInfoStore(mContext);
        username = ParseUser.getCurrentUser().getUsername();
        
        pickImage = (Button) findViewById(R.id.btn_pick);
        pickImage.setBackgroundResource(R.drawable.bad_indicator);
		mixpanel = MixpanelAPI.getInstance(getBaseContext(), Constants.MIXEDPANEL_TOKEN);
		mixpanel.identify(ParseInstallation.getCurrentInstallation().getObjectId());
		
		mixpanel.track("ImagePickerActivity created", null);        
        pickImage.setOnClickListener(new OnClickListener() {
       	 
			private SimplePopupWindow popup;

			@Override
			public void onClick(View view) {				
				if(bitmap!=null) {
					new UploadMyPhoto(mContext, bitmap, ParseUser.getCurrentUser()).start();
				    Intent mIntent = new Intent(mContext, AllThreadsListActivity.class);
				    
				    //together this means that when you press the back button in the new task you will not go back
				    //to the original task, but rather close out of the app
				    mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
				    mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			    	startActivity(mIntent); 						
				}
				else {
					popup = new SimplePopupWindow(view);
					popup.setMessage("Oops,  there's no photo here!");
					popup.showLikeQuickAction();
				}

			}
		});           
        if(store.getFriendBitmap(username) != null) {
        	bitmap = store.getFriendBitmap(username);
			imageView.setImageBitmap(bitmap);
			pickImage.setBackgroundResource(R.drawable.good_indicator);
			
        }
		mBroadcastReceiver = 
		new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				Log.i("received broadcast",action);
				if(action.equals(Constants.ACTION_FACEBOOK_PHOTO_OBTAINED)) {
					store = new UserInfoStore(mContext);
					bitmap = store.getFriendBitmap(username);
					if(bitmap!=null) {
						
						imageView.setImageBitmap(bitmap);
						pickImage.setBackgroundResource(R.drawable.good_indicator);
					}
				}				
				

			}
		};				
		intentFilter = new IntentFilter();	
		intentFilter.addAction(Constants.ACTION_FACEBOOK_PHOTO_OBTAINED);		
		registerReceiver(mBroadcastReceiver, intentFilter);	        
        
        
        imageView.setOnClickListener(new OnClickListener() {
 
			@Override
			public void onClick(View view) {				
				Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
				photoPickerIntent.setType("image/*");
				startActivityForResult(photoPickerIntent, SELECT_PHOTO);
			}
		});
             
    }
 
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) { 
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent); 
 
        switch(requestCode) { 
        case SELECT_PHOTO:
            if(resultCode == RESULT_OK){
				try {
					final Uri imageUri = imageReturnedIntent.getData();
					final InputStream imageStream = getContentResolver().openInputStream(imageUri);
					final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
					DocumentExifTransformation det = new DocumentExifTransformation(mContext, imageUri);
					if(selectedImage != null) {
						bitmap = det.transform(selectedImage);
						imageView.setImageBitmap(bitmap);
						pickImage.setBackgroundResource(R.drawable.good_indicator);
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
 
            }
        }
    }
    private static int getOrientationFromMediaStore(Context context, Uri imageUri) {
        if(imageUri == null) {
            return -1;
        }

        String[] projection = {MediaStore.Images.ImageColumns.ORIENTATION};
        Cursor cursor = context.getContentResolver().query(imageUri, projection, null, null, null);

        int orientation = -1;
        if (cursor != null && cursor.moveToFirst()) {
            orientation = cursor.getInt(0);
            cursor.close();
        }

        return orientation;
    }    
 
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }    
	@Override
	protected void onPause() {
		super.onPause();
		if(mBroadcastReceiver!=null) unregisterReceiver(mBroadcastReceiver);
	}	
	    
    
	protected void onResume() {
		super.onResume();
		if(mBroadcastReceiver!=null) registerReceiver(mBroadcastReceiver, intentFilter);
	}	    
    static class DocumentExifTransformation implements Transformation {

        final Context context;
        final Uri uri;
     
        DocumentExifTransformation(Context context, Uri uri) {
          this.context = context;
          this.uri = uri;
        }
     
        @Override 
        public Bitmap transform(Bitmap source) throws IOException {
            Matrix matrix = new Matrix();
            int exifRotation = getOrientationFromMediaStore(context, uri);
            if (exifRotation != 0) matrix.preRotate(exifRotation);
            Bitmap rotated =
                  Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
            if (rotated != source) source.recycle();
            return rotated;     
        }
     
        @Override 
        public String key() {
          return "documentExifTransform(" + uri.getLastPathSegment() + ")";
        }
     

      }    
	@Override
	protected void onDestroy() {
		mixpanel.flush();
	    super.onDestroy();
	}	    
}
