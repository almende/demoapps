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

public class ConferenceBaseActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_conference_app);
		
		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
		
		PositionUtil.setContext(this);
		PositionUtil.getInstance().startScan();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.conference_app, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	public void refresh(View v){
		PositionUtil.getInstance().startScan();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
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
		
		public void onEventMainThread(StateEvent event) {
			if (event.getValue().equals("ReceivedScan")) {
				TextView tv = (TextView) view.findViewById(R.id.hello_world);
				tv.setText(PositionUtil.getInstance().getCurrent().toString());
				
			}
		}
		
		public PlaceholderFragment() {
			EventBus.getDefault().unregister(this);
			EventBus.getDefault().register(this);
		}
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			view = inflater.inflate(R.layout.fragment_conference_app,
					container, false);
			return view;
		}
		
	}
	
}
