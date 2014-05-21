package com.almende.demo.conferenceApp;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

public class SettingsFragment extends PreferenceFragment {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		PreferenceManager.setDefaultValues(this.getActivity(),
				R.xml.preferences, false);
		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.preferences);
	}
	
}
