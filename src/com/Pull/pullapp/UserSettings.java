package com.Pull.pullapp;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.support.v4.app.NavUtils;

import com.Pull.pullapp.util.Constants;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.parse.ParseInstallation;
 
public class UserSettings extends SherlockPreferenceActivity {
	private MixpanelAPI mixpanel;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM
                | ActionBar.DISPLAY_SHOW_HOME);
    	getActionBar().setDisplayHomeAsUpEnabled(true);	
        addPreferencesFromResource(R.layout.settings);
		mixpanel = MixpanelAPI.getInstance(getBaseContext(), Constants.MIXEDPANEL_TOKEN);
		mixpanel.identify(ParseInstallation.getCurrentInstallation().getObjectId());
		
		mixpanel.track("UserSettings created", null);
        final Preference otherpref = (Preference) findPreference("prefReceiveTexts");      

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