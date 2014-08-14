package com.Pull.pullapp;

import com.Pull.pullapp.util.Constants;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.parse.ParseInstallation;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.widget.Toast;
 
public class UserSettings extends PreferenceActivity {
	private MixpanelAPI mixpanel;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
 
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
}