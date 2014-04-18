package com.Pull.pullapp;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.Pull.pullapp.R;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.facebook.FacebookAuthorizationException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;

@Deprecated
public class SignInActivity extends SherlockFragmentActivity implements
		OnClickListener{

	public static final int REQUEST_CODE_PLUS_CLIENT_FRAGMENT = 0;
	private final String PENDING_ACTION_BUNDLE_KEY = "com.clearmessage.like.SignInActivity:PendingAction";

	private LoginButton loginButton;

	private TextView mSignInStatus;

	private UiLifecycleHelper uiHelper;

	private PendingAction pendingAction = PendingAction.NONE;

	private View splash;

	private MainApplication mApp;

	private enum PendingAction {
		NONE, POST_PHOTO, POST_STATUS_UPDATE
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mApp = (MainApplication) this.getApplication();

		PackageInfo info;
		try {
			info = getPackageManager().getPackageInfo("com.Pull.pullapp",
					PackageManager.GET_SIGNATURES);
			for (Signature signature : info.signatures)
		    {
		        MessageDigest md = MessageDigest.getInstance("SHA");
		        md.update(signature.toByteArray());
		        Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
		    }
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		
		uiHelper = new UiLifecycleHelper(this, callback);
		uiHelper.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			String name = savedInstanceState
					.getString(PENDING_ACTION_BUNDLE_KEY);
			pendingAction = PendingAction.valueOf(name);
		}

		setContentView(R.layout.activity_signin);

		splash = findViewById(R.id.splash);


		mSignInStatus = (TextView) findViewById(R.id.sign_in_status);

		loginButton = (LoginButton) findViewById(R.id.fb_login_button);
		loginButton
				.setUserInfoChangedCallback(new LoginButton.UserInfoChangedCallback() {
					@Override
					public void onUserInfoFetched(GraphUser user) {
						if (user != null) {
							//Toast.makeText(getApplicationContext(), "hi", Toast.LENGTH_LONG).show();
							mApp.setSignedIn(true, user.getId(), user.getUsername());
							
							 String greeting = getString(
							 R.string.greeting_status,
							 user.getFirstName());
							 Log.i("username", user.getFirstName());
							 mSignInStatus.setText(greeting);

							Intent intent = new Intent(SignInActivity.this,
									ThreadsListActivity.class);
							startActivity(intent);
							finish();
						}else{
							mApp.setSignedIn(false, null, null);
							splash.setVisibility(View.GONE);
						}
					}
				});

		if (!mApp.isSignedIn()) {
			splash.setVisibility(View.GONE);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		uiHelper.onSaveInstanceState(outState);

		outState.putString(PENDING_ACTION_BUNDLE_KEY, pendingAction.name());
	}

	@Override
	protected void onResume() {
		super.onResume();
	    // For scenarios where the main activity is launched and user
	    // session is not null, the session state change notification
	    // may not be triggered. Trigger it if it's open/closed.
	    Session session = Session.getActiveSession();
	    if (session != null &&
	           (session.isOpened() || session.isClosed()) ) {
	        onSessionStateChange(session, session.getState(), null);
	    }

	    uiHelper.onResume();		
	}

	@Override
	public void onPause() {
		super.onPause();
		uiHelper.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		uiHelper.onDestroy();
	}

	private Session.StatusCallback callback = new Session.StatusCallback() {
		@Override
		public void call(Session session, SessionState state,
				Exception exception) {
			onSessionStateChange(session, state, exception);
		}
	};

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int responseCode,
			Intent intent) {
		uiHelper.onActivityResult(requestCode, responseCode, intent);
		
		switch (responseCode) {
		case Activity.RESULT_CANCELED:
			mApp.setSignedIn(false, null, null);
			splash.setVisibility(View.GONE);
			break;
		}

	}


	private void resetAccountState() {
		mSignInStatus.setText(getString(R.string.signed_out_status));
		mApp.setSignedIn(false, null, null);
		splash.setVisibility(View.GONE);
	}
	private static final String TAG = "MainFragment";
	private void onSessionStateChange(Session session, SessionState state,
			Exception exception) {
	    if (state.isOpened()) {
	        Log.i(TAG, "Logged in...");
	    } else if (state.isClosed()) {
	        Log.i(TAG, "Logged out...");
	    } else {
	        Log.d(TAG, "Unknown state: " + state);
	    }	    
	}

}
