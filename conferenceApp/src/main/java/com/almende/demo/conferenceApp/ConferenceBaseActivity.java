/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.demo.conferenceApp;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import de.greenrobot.event.EventBus;

/**
 * The Class ConferenceBaseActivity.
 */
public class ConferenceBaseActivity extends Activity {
	
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_conference_app);
		
		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
		
//		final TextView tv = (TextView) findViewById(R.id.closeBy);
//		tv.setText("Scanning, no devices nearby.");
		DetectionUtil.setContext(this);
		DetectionUtil.getInstance().startScan();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.conference_app, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	/**
	 * Refresh.
	 * 
	 * @param v
	 *            the v
	 */
	public void refresh(final View v) {
		DetectionUtil.getInstance().startScan();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		final int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {
		private View	view	= null;
		
		/**
		 * On event main thread.
		 * 
		 * @param event
		 *            the event
		 */
		public void onEventMainThread(final StateEvent event) {
			if (event.getValue().equals("closeBy")){
				final String id = event.getId();
				final TextView tv = (TextView) view.findViewById(R.id.closeBy);
				tv.setText("Device:"+id+" is closeby!");
			}
		}
		
		/**
		 * Instantiates a new placeholder fragment.
		 */
		public PlaceholderFragment() {
			EventBus.getDefault().unregister(this);
			EventBus.getDefault().register(this);
		}
		
		/*
		 * (non-Javadoc)
		 * 
		 * @see android.app.Fragment#onCreateView(android.view.LayoutInflater,
		 * android.view.ViewGroup, android.os.Bundle)
		 */
		@Override
		public View onCreateView(final LayoutInflater inflater,
				final ViewGroup container, final Bundle savedInstanceState) {
			view = inflater.inflate(R.layout.fragment_conference_app,
					container, false);
			return view;
		}
		
	}
	
}
