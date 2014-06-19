package com.Pull.pullapp;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.Pull.pullapp.model.Comment;
import com.Pull.pullapp.model.SharedConversation;
import com.Pull.pullapp.util.Constants;
import com.Pull.pullapp.util.ContentUtils;
import com.Pull.pullapp.util.DatabaseHandler;
import com.Pull.pullapp.util.SendMessages;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class SharedConversationActivity extends SherlockActivity implements
							GestureDetector.OnGestureListener,
							GestureDetector.OnDoubleTapListener {
	
	
	private Context mContext;
	private LinearLayout mLayout;
	private ListView sharedConversationListView;
	public ListView sharedConversationCommentListView;
	private DatabaseHandler dbHandler;
	private SharedConversation sharedConversation;
	private SharedConversationMessageListAdapter sharedConversationMessageListAdapter;
	private String sharedConversationId, confidanteName;
	private EditText sharedConversationCommentEditText;
	private Button sharedConversationCommentSendButton, proposeButton;
	public SharedConversationCommentListAdapter sharedConversationCommentListAdapter;
	private View separatorView;
	private Handler mHandler = new Handler(); 
	private ArrayList<Comment> commentList = new ArrayList<Comment>();
	private BroadcastReceiver tickReceiver;
	private boolean isEmpty;
	private String commentText;
	private TextView hint;
	protected String recipient;
	protected ParseUser parseRecipient;
	protected Comment mCurrentComment;
	private MainApplication mApp;
	private String mOriginalRecipientName;
	private GestureDetector mGestureDetector;
	private String recipientName;
	protected ProgressDialog progress;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent i = getIntent();
		sharedConversationId =  i.getStringExtra(Constants.EXTRA_SHARED_CONVERSATION_ID); 
		mApp = (MainApplication) this.getApplication();
		
		if(sharedConversationId.equals("")){
			finish();
			return;
		}
		
		
		setContentView(R.layout.shared_conversation_thread_activity);
		isEmpty = true;
		mContext = getApplicationContext();
		mLayout = (LinearLayout) findViewById(R.id.main_layout);
		dbHandler = new DatabaseHandler(mContext);
	    sharedConversationListView = (ListView) findViewById(R.id.shared_conversation_list_view);
	    
	    separatorView = findViewById(R.id.separator);
	    sharedConversationCommentListView = (ListView) findViewById(R.id.shared_conversation_comment_list_view);
	    sharedConversationCommentEditText = (EditText) findViewById(R.id.shared_conversation_comment_edit_text);
	    sharedConversationCommentSendButton = (Button) findViewById(R.id.shared_conversation_comment_send_button);
	    proposeButton = (Button) findViewById(R.id.shared_conversation_propose_button);
	    hint = (TextView) findViewById(R.id.hint);
	    
	    sharedConversation = dbHandler.getSharedConversation(sharedConversationId);
	    if(sharedConversation == null) {
	    	Log.i("shared convo is null", sharedConversationId);
	    }
	    //if (sharedConversation.getType()==TextBasedSmsColumns.MESSAGE_TYPE_SENT) {
	    if(ContentUtils.addCountryCode(sharedConversation.getSharer()) 
	    		.equals(ContentUtils.addCountryCode(mApp.getUserName()))) { 
	    	 Log.i("convo id", sharedConversationId);
	    	recipient = sharedConversation.getConfidante();

	    } else {
	    	recipient = sharedConversation.getSharer();             
	    	
	    }
	   	    
	    mOriginalRecipientName = sharedConversation.getOriginalRecipientName();
	    sharedConversationListView.setOnDragListener(new MyDragListener(this, sharedConversation));
	    
	    Log.i("sharedconversation recipient", recipient);
	    // Dirty Hack to detect keyboard
		mLayout.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			
			@Override
			public void onGlobalLayout() {
				if ((mLayout.getRootView().getHeight() - mLayout.getHeight()) > mLayout.getRootView().getHeight() / 3) {
					sharedConversationListView.setVisibility(View.GONE);
					separatorView.setVisibility(View.GONE);
				} else {
					sharedConversationListView.setVisibility(View.VISIBLE);
					separatorView.setVisibility(View.VISIBLE);
				}
			}
		});
		
		progress = new ProgressDialog(this);
		sharedConversationCommentSendButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				commentText = sharedConversationCommentEditText.getText().toString().trim();
				if(commentText.length()>0){
			        progress.setTitle("Sending comment");
			        progress.setMessage("Sending...");
			        progress.show();    			
			        sharedConversationCommentSendButton.setClickable(false);
					if(isEmpty) {
						isEmpty = false;
						hint.setVisibility(View.GONE);
						sharedConversationCommentListView.setVisibility(View.VISIBLE);
					}
					mCurrentComment = new Comment(commentText, mApp.getUserName(), System.currentTimeMillis());

					attemptSend(recipient);
					
				}
				else {
					Toast.makeText(mContext, "Comment field is empty", Toast.LENGTH_LONG).show();
				}
			}
		});
		
	    sharedConversationMessageListAdapter = new SharedConversationMessageListAdapter(mContext, 
	    		sharedConversation.getMessages());
	    sharedConversationListView.setAdapter(sharedConversationMessageListAdapter);
	    
	    sharedConversationCommentListAdapter = new SharedConversationCommentListAdapter(mContext, sharedConversation.getComments(), 
	    			sharedConversation.getConfidante(), mOriginalRecipientName);
	    sharedConversationCommentListView.setAdapter(sharedConversationCommentListAdapter);
	    
	    
	    confidanteName = ContentUtils.getContactDisplayNameByNumber(mContext,sharedConversation.getConfidante());
	    recipientName = ContentUtils.getContactDisplayNameByNumber(mContext,recipient);

	    sharedConversationCommentEditText.setHint("Write to " + recipientName);
	    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	    
		tickReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				
				if(action.equals(Constants.ACTION_SEND_COMMENT_CANCELED)) {
					//return comment to its previous position
					sharedConversationCommentListAdapter = new SharedConversationCommentListAdapter(mContext, 
							sharedConversation.getComments(), sharedConversation.getConfidante(), mOriginalRecipientName);
				    sharedConversationCommentListView.setAdapter(sharedConversationCommentListAdapter);
				} else if(action.equals(Constants.ACTION_SEND_COMMENT_CONFIRMED)) {
					//send comment
					
				} else if (action.equals(Constants.ACTION_TIME_TICK)) {
					updateTime();
				} else if(action.equals(Constants.ACTION_SHARE_COMPLETE)) {
					int resultCode = intent.getIntExtra(Constants.EXTRA_SHARE_RESULT_CODE, 0);
					int commentNum = intent.getIntExtra(Constants.EXTRA_COMMENT_NUMBER, -1);
					progress.cancel();
					switch(resultCode) {
					case(0):	
			            break;
					/*case(ParseException.CONNECTION_FAILED):
						Toast.makeText(mContext, "Share failed: not connected", 
								Toast.LENGTH_LONG).show();	
						break;
					case(ParseException.INCORRECT_TYPE):
						Toast.makeText(mContext, "Share failed: incorrect type", 
								Toast.LENGTH_LONG).show();	
						break;*/						
					default:
						/*Toast.makeText(mContext, "Share failed with error code " + resultCode, 
								Toast.LENGTH_LONG).show();*/
						
						break;
					}
					return;
				}
			}
		};
		
		mGestureDetector = new GestureDetector(this,this);

	}
	
	
	private void hideKeyboard(){
		InputMethodManager inputManager = (InputMethodManager) this
	            .getSystemService(Context.INPUT_METHOD_SERVICE);
	    View v=getCurrentFocus();
	    if(v==null)return;
	    inputManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		if(commentList.size()>0) sharedConversationCommentListView.setSelection(commentList.size()-1);        			
	
	}
	
	private void updateTime(){
		sharedConversationMessageListAdapter.notifyDataSetChanged();
		sharedConversationCommentListAdapter.notifyDataSetChanged();
	}
	
	
   private Runnable mFakeReplayTask = new Runnable() {
       public void run() {
    	   DatabaseHandler db = new DatabaseHandler(mContext);
			commentList = db.getComments(sharedConversationId);
			String reply;
			if(commentList.get(commentList.size()-1).getMessage().trim().endsWith("?")){
				reply = "I don't know, what do you think?";
			}else{
				reply = "Very true!!!";
			}
			Comment c = new Comment(reply, sharedConversation.getConfidante(), System.currentTimeMillis());
			db.addComment(sharedConversationId, c);
			commentList.add(c);
			sharedConversation.setComments(commentList);
			updateViews();
       }
    };




	
	
	@Override
	protected void onResume() {
		super.onResume();
		
		sharedConversation = dbHandler.getSharedConversation(sharedConversationId);
		if(sharedConversation == null) Log.i("shared convo is null", sharedConversationId);
		mOriginalRecipientName = sharedConversation.getOriginalRecipientName();
		commentList = dbHandler.getComments(sharedConversationId);
		if(commentList.size()>0) {
			isEmpty = false;
			hint.setVisibility(View.GONE);
			sharedConversationCommentListView.setVisibility(View.VISIBLE);			
		}
		sharedConversation.setComments(commentList);
		updateViews();
				
		sharedConversationMessageListAdapter.setItemList(sharedConversation.getMessages());
		this.setTitle("Comments re: " + mOriginalRecipientName);
		
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Constants.ACTION_TIME_TICK);
		intentFilter.addAction(Constants.ACTION_SEND_COMMENT_CANCELED);		
		intentFilter.addAction(Constants.ACTION_SHARE_COMPLETE);	
		registerReceiver(tickReceiver,intentFilter);	
		
		if(isEmpty) {
			hint.setText("Write a comment to " + recipientName);
			hint.setVisibility(View.VISIBLE);
			sharedConversationCommentListView.setVisibility(View.GONE);
		}
		if(commentList.size()>0) sharedConversationCommentListView.setSelection(commentList.size()-1);        			
		
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(tickReceiver);
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
            //NavUtils.navigateUpFromSameTask(this);
	    	Intent mIntent = new Intent(mContext, SharedListActivity.class);
	    	mIntent.putExtra(Constants.EXTRA_SHARE_TYPE, sharedConversation.getType());
	    	startActivity(mIntent);						
            return true;
        default:
        	return false;
		}
	}
	
	
	private void attemptSend(final String confidante) {
		Log.i("about to query","about to query channel " + ContentUtils.setChannel(confidante));
		ParseQuery<ParseObject> query = ParseQuery.getQuery("Channels");
		query.whereEqualTo("channel", ContentUtils.setChannel(confidante));
		query.findInBackground(new FindCallback<ParseObject>() {
    	  public void done(List<ParseObject> objects, ParseException e) {
    		  Log.i("done with query","finished query on channels");
    		  if (e == null && objects.size()>0) {
    	    	sendComment();
    	    	Log.i("sending comment","to a real user");
    	    } else {
    	    	progress.cancel();
    	    	askToInviteFriend();
    	    }
    	  }
    	});
	}

	protected void askToInviteFriend() {
		Log.i("ask to invite friend","about to fire");
		String recipient_name = ContentUtils.getContactDisplayNameByNumber(mContext, recipient);
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    builder.setTitle("Get " + recipient_name + " on Pull"); 
	    builder.setMessage("Before " + recipient_name + " can get your comments, " +
	    		"they need to download the app! Do you want to send them a link?")
	           .setCancelable(true)
	           .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int id) {
	            	   dialog.cancel();
	            	   Intent sendIntent = new Intent(Intent.ACTION_VIEW);     
	            	   sendIntent.putExtra("address", recipient);
	            	   sendIntent.putExtra("sms_body", Constants.GOOGLE_PLAY_LINK); 
	            	   sendIntent.setData(Uri.parse("smsto:" + recipient));
	            	   startActivity(sendIntent);
		            	   
	               	}
	           })
	           .setNegativeButton("No", new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int id) {
	                    dialog.cancel();
	               }
	           }).show();	
	}


	protected void sendComment() {
		// This will save both message and conversation to Parse
		sharedConversation.addComment(mCurrentComment);
		commentList = sharedConversation.getComments();
		final int commentNum = commentList.size()-1;
		mCurrentComment.put("parent",sharedConversation);
		updateViews();
		mCurrentComment.saveInBackground(new SaveCallback(){
        	public void done(ParseException e) {
        		if (e == null) {
        			//everything is good
        			Log.i("comment saved","comment saved");
        			progress.cancel();
					DatabaseHandler db = new DatabaseHandler(mContext);
					db.addComment(sharedConversationId, mCurrentComment);
					String comment_id = mCurrentComment.getObjectId();
					sendNotifications(comment_id);
        		} else {
        			progress.cancel();
        			Log.i("comment not saved",e.getMessage());
					Intent broadcastIntent = new Intent();
					broadcastIntent.setAction(Constants.ACTION_SHARE_COMPLETE);
					broadcastIntent.putExtra(Constants.EXTRA_SHARE_RESULT_CODE, e.getCode());
					broadcastIntent.putExtra(Constants.EXTRA_COMMENT_NUMBER, commentNum);
					mContext.sendBroadcast(broadcastIntent);	
			    }
        		
			 }
		 });
		
	}


	protected void updateViews() {
		sharedConversationCommentListAdapter.setItemList(commentList);
		sharedConversationCommentListView.invalidateViews();
		sharedConversationCommentListView.refreshDrawableState();					
		sharedConversationCommentEditText.setText("");
		sharedConversationCommentSendButton.setClickable(true);
		hideKeyboard();
		sharedConversationCommentEditText.clearFocus();
		if(commentList.size()>0) sharedConversationCommentListView.setSelection(commentList.size()-1);     
		
	}


	protected void sendNotifications(String comment_id) {
		JSONObject data = new JSONObject();
		try {
			data.put("action", Constants.ACTION_RECEIVE_COMMENT);
			data.put("commentID", comment_id);
			data.put("convoID", this.sharedConversationId);
			data.put("type", Constants.NOTIFICATION_COMMENT);
			ParsePush push = new ParsePush();
			push.setChannel(ContentUtils.setChannel(recipient));
			push.setData(data);
			push.sendInBackground();				
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public void propose(View v) {
		commentText = sharedConversationCommentEditText.getText().toString().trim();					
		if(commentText.length()>0){
	    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
		    builder.setTitle("Propose sending this to " + 
		    	ContentUtils.getContactDisplayNameByNumber(mContext,sharedConversation.getOriginalRecipient()) 
		    	+ "?");
		    builder.setMessage(commentText)
		           .setCancelable(true)
		           .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		               public void onClick(DialogInterface dialog, int id) {
		            	   dialog.cancel();
							if(isEmpty) {
								isEmpty = false;
								hint.setVisibility(View.GONE);
								sharedConversationCommentListView.setVisibility(View.VISIBLE);
							}
							mCurrentComment = new Comment(commentText, mApp.getUserName(), System.currentTimeMillis());
							mCurrentComment.setProposal(true);
							attemptSend(recipient);				            	   
		               	}
		           })
		           .setNegativeButton("No", new DialogInterface.OnClickListener() {
		               public void onClick(DialogInterface dialog, int id) {
		                    dialog.cancel();
		                    commentText = "";
		               }
		           }).show();					

		}
				
	}
	protected void commentViaSMS(String recipient) {
		SendMessages.sendsms(mContext, recipient, commentText, 0, false);
	}	

	private void createFakeConvo(){

		Comment c;
		c = new Comment("he didn't say he flaked", sharedConversation.getConfidante(), System.currentTimeMillis());
		commentList.add(c);
		c = new Comment("look at the timestamps! thats the last message. tonight is clearly not happening", sharedConversation.getSharer(), System.currentTimeMillis());
		commentList.add(c);		
		c = new Comment("i know, i can see the timestamps because you turned on auto-forwarding. he would not flake, he totally likes you", sharedConversation.getConfidante(), System.currentTimeMillis());
		commentList.add(c);
		c = new Comment("if i dont start getting ready now, i wont make it on time ", sharedConversation.getSharer(), System.currentTimeMillis());
		commentList.add(c);	
		
		c = new Comment("so text him!", sharedConversation.getConfidante(), System.currentTimeMillis());
		commentList.add(c);
		c = new Comment("no. that would be clingy", sharedConversation.getSharer(), System.currentTimeMillis());
		commentList.add(c);		
		c = new Comment("im looking at your message history and the last time this happened, it turned out he he was at the gym", sharedConversation.getConfidante(), System.currentTimeMillis());
		commentList.add(c);
		c = new Comment("true", sharedConversation.getSharer(), System.currentTimeMillis());
		commentList.add(c);		
		
		c = new Comment(" hi andrew, since you still haven't texted me i assume we are not meeting tonight.", sharedConversation.getSharer(), System.currentTimeMillis());
		c.setProposal(true);
		commentList.add(c);	
		c = new Comment("hey, just got out of class. running a little late! see you at 6:30?", sharedConversation.getConfidante(), System.currentTimeMillis());
		c.setProposal(true);
		commentList.add(c);		
		sharedConversation.setComments(commentList);
		updateViews();	
	}

	@Override
	public boolean onSingleTapConfirmed(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onDoubleTap(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onDoubleTapEvent(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		// TODO Auto-generated method stub
		return false;
	}
}
