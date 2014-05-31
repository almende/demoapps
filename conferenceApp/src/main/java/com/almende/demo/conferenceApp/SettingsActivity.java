/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.demo.conferenceApp;

import android.app.Activity;
import android.os.Bundle;
import de.greenrobot.event.EventBus;

/**
 * The Class SettingsActivity.
 */
public class SettingsActivity extends Activity {
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Display the fragment as the main content.
		getFragmentManager().beginTransaction()
				.replace(android.R.id.content, new SettingsFragment()).commit();
	}
	
	@Override
	protected void onPause() {
		EventBus.getDefault().post(new StateEvent(null,"settingsUpdated"));
		super.onPause();
	}
}