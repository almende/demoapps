/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.demo.conferenceApp;

import java.util.HashMap;
import java.util.Map.Entry;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import de.greenrobot.event.EventBus;

/**
 * The Class SettingsFragment.
 */
public class SettingsFragment extends PreferenceFragment {
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.preference.PreferenceFragment#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		PreferenceManager.setDefaultValues(getActivity().getApplication(), R.xml.preferences,
				false);
		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.preferences);
		listKnownNames();
		
		EventBus.getDefault().unregister(this);
		EventBus.getDefault().register(this);
	}
	
	public void onEventMainThread(final StateEvent event) {
		if (event.getValue().equals("addedKnownName")) {
			listKnownNames();
		}
	}
	
	public void listKnownNames() {
		final PreferenceCategory cat = (PreferenceCategory) this
				.findPreference("pref_knownNames");
		cat.removeAll();
		
		HashMap<String, String> knownNames = EveService.myAgent.getKnownNames();
		for (Entry<String, String> entry : knownNames.entrySet()) {
			final Preference pref = new Preference(this.getActivity());
			pref.setTitle(entry.getKey() + " - '" + entry.getValue() + "'");
			cat.addPreference(pref);
		}
	}
}
