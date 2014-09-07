package com.Pull.pullapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceChangeListener;
import android.provider.Telephony;
import android.support.v4.app.NavUtils;

import com.Pull.pullapp.util.Constants;
import com.Pull.pullapp.util.UserInfoStore;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.parse.ParseInstallation;
 
public class UserSettings extends SherlockPreferenceActivity {
	private MixpanelAPI mixpanel;
	private Activity activity;
	private int currentapiVersion;
	private Context mContext;
	private UserInfoStore store;
	private SharedPreferences sharedPrefs;
	private Editor editor;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM
                | ActionBar.DISPLAY_SHOW_HOME);
    	getActionBar().setDisplayHomeAsUpEnabled(true);	
        addPreferencesFromResource(R.layout.settings);
		mixpanel = MixpanelAPI.getInstance(getBaseContext(), Constants.MIXEDPANEL_TOKEN);
		mixpanel.identify(ParseInstallation.getCurrentInstallation().getObjectId());
		activity = this;
		mContext = getApplicationContext();
		mixpanel.track("UserSettings created", null);
		currentapiVersion = android.os.Build.VERSION.SDK_INT;
        sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(mContext);	
        editor = sharedPrefs.edit();
        final Preference otherpref = (Preference) findPreference("prefReceiveTexts");      
        
        if (currentapiVersion >= android.os.Build.VERSION_CODES.KITKAT) {
        	otherpref.setDefaultValue(false);
        }
        else 
        	otherpref.setDefaultValue(true);
        otherpref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				mixpanel.track(preference.getTitle() + " set to " + newValue.toString(), null);
				return true;
			}
        });

    }
	@Override
	protected void onDestroy() {
		mixpanel.flush();
	    super.onDestroy();
	}	  	
	

	// when a user selects a menu item
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
            NavUtils.navigateUpFromSameTask(this);
            return true;				         
		default:
			return false;
		}
	}		
}