/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.demo.conferenceApp;

import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.almende.demo.conferenceCloud.Info;

import de.greenrobot.event.EventBus;

/**
 * The Class ConferenceBaseActivity.
 */
public class ConferenceBaseActivity extends Activity {
	static Context	ctx	= null;
	
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ctx = this;
		
		setContentView(R.layout.activity_conference_app);
		
		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new HomeFragment()).commit();
		}
		DetectionUtil.setContext(this);
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
	
	/**
	 * Refresh.
	 * 
	 * @param v
	 *            the v
	 */
	public void refresh() {
		DetectionUtil.getInstance().startScan();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
			case R.id.action_settings:
				System.err.println("Opening settings!");
				try {
					startActivity(new Intent(this, SettingsActivity.class));
				} catch (Throwable t) {
					t.printStackTrace();
				}
				return true;
			case R.id.action_refresh:
				refresh();
				return true;
			case android.R.id.home:
				getFragmentManager().popBackStack();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	/**
	 * The Class HomeFragment.
	 */
	public static class HomeFragment extends Fragment {
		private View				view			= null;
		private ArrayAdapter<Info>	arrayAdapter	= null;
		
		/**
		 * On event main thread.
		 * 
		 * @param event
		 *            the event
		 */
		public void onEventMainThread(final StateEvent event) {
			if (event.getValue().equals("listUpdated")) {
				if (arrayAdapter != null) {
					arrayAdapter.clear();
					arrayAdapter.addAll(EveService.myAgent.getList(true));
					arrayAdapter.notifyDataSetChanged();
				}
			}
		}
		
		/**
		 * Instantiates a new placeholder fragment.
		 */
		public HomeFragment() {
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
			try {
				view = inflater.inflate(R.layout.home_conference_app,
						container, false);
				
				List<Info> contacts = EveService.myAgent.getList(true);
				ListView contactsList = (ListView) view
						.findViewById(R.id.listContacts);
				
				arrayAdapter = new ArrayAdapter<Info>(ctx,
						android.R.layout.simple_list_item_1, contacts);
				
				contactsList.setAdapter(arrayAdapter);
				
				contactsList.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> adapter, View v,
							int position, long arg3) {
						try {
							final Info info = arrayAdapter.getItem(position);
							System.err.println("Clicked on:" + info.getId());
							Fragment fragment = new DetailsFragment(info
									.getId());
							
							FragmentManager fm = getFragmentManager();
							FragmentTransaction transaction = fm
									.beginTransaction();
							transaction.addToBackStack(null);
							transaction.replace(R.id.container, fragment);
							transaction.commit();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
				
			} catch (Throwable t) {
				System.err.println("Error!");
				t.printStackTrace();
			}
			
			return view;
		}
		
	}
	
	/**
	 * The Class HomeFragment.
	 */
	public static class DetailsFragment extends Fragment {
		private View	view	= null;
		private Info	info	= null;
		private String	id		= null;
		
		/**
		 * On event main thread.
		 * 
		 * @param event
		 *            the event
		 */
		public void onEventMainThread(final StateEvent event) {
			if (event.getValue().equals("listUpdated")) {
				refresh();
			}
		}
		
		private void refresh() {
			info = EveService.myAgent.get(id);
			if (info != null) {
				TextView details = (TextView) view.findViewById(R.id.details);
				details.setText(info.getLastSeen().toString());
			}
		}
		
		/**
		 * Instantiates a new placeholder fragment.
		 */
		public DetailsFragment() {
			EventBus.getDefault().unregister(this);
			EventBus.getDefault().register(this);
		}
		
		/**
		 * Instantiates a new placeholder fragment.
		 */
		public DetailsFragment(String id) {
			this.id = id;
			EventBus.getDefault().unregister(this);
			EventBus.getDefault().register(this);
		}
		
		public void setId(String id) {
			this.id = id;
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
			try {
				getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
				getActivity().getActionBar().setDisplayShowHomeEnabled(true);
				
				view = inflater.inflate(R.layout.details_conference_app,
						container, false);
				refresh();
			} catch (Throwable t) {
				t.printStackTrace();
			}
			return view;
		}
		
	}
}
