/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.demo.conferenceApp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import org.la4j.vector.Vector;
import org.la4j.vector.Vectors;
import org.la4j.vector.dense.BasicVector;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import de.greenrobot.event.EventBus;

/**
 * The Class PositionUtil.
 */
public class PositionUtil {
	private final static Logger						LOG			= Logger.getLogger(PositionUtil.class
																		.getName());
	private static PositionUtil						instance	= new PositionUtil();
	private static Context							ctx			= null;
	private static WifiManager						wm			= null;
	private Vector									current		= null;
	private static final HashMap<String, Integer>	SSIS		= new HashMap<String, Integer>();
	
	static {
		SSIS.put("HP-Print-13-LaserJet200", 0);
		SSIS.put("KLAASSEN", 1);
		SSIS.put("loosFM", 2);
		SSIS.put("Tigra", 3);
		SSIS.put("yarp", 4);
		SSIS.put("rlspot2", 5);
	}
	
	/**
	 * Load ssi ds.
	 * 
	 * @param ssids
	 *            the ssids
	 */
	public static void loadSSIDs(final String[] ssids) {
		SSIS.clear();
		for (int i = 0; i < ssids.length; i++) {
			SSIS.put(ssids[i], i);
		}
	}
	
	private PositionUtil() {
	}
	
	/**
	 * Sets the context.
	 * 
	 * @param ctx
	 *            the new context
	 */
	public static void setContext(final Context ctx) {
		PositionUtil.ctx = ctx;
	}
	
	/**
	 * Gets the single instance of PositionUtil.
	 * 
	 * @return single instance of PositionUtil
	 */
	public static PositionUtil getInstance() {
		return instance;
	}
	
	/**
	 * Do compare.
	 * 
	 * @param remoteList
	 *            the remote list
	 */
	public void doCompare(final String remoteList) {
		
	}
	
	/**
	 * Gets the current.
	 * 
	 * @return the current
	 */
	public Vector getCurrent() {
		return current;
	}
	
	/**
	 * Start scan.
	 */
	public void startScan() {
		if (wm == null) {
			wm = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
			final WifiScanReceiver wifiReciever = new WifiScanReceiver();
			ctx.registerReceiver(wifiReciever, new IntentFilter(
					WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		}
		if (!wm.isWifiEnabled()) {
			wm.setWifiEnabled(true);
		}
		wm.startScan();
	}
	
	/**
	 * The Class WifiScanReceiver.
	 */
	class WifiScanReceiver extends BroadcastReceiver {
		private List<ScanResult>	wifiList;
		
		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * android.content.BroadcastReceiver#onReceive(android.content.Context,
		 * android.content.Intent)
		 */
		@Override
		public void onReceive(final Context c, final Intent intent) {
			wifiList = wm.getScanResults();
			final ArrayList<Double> levels = new ArrayList<Double>(SSIS.size());
			for (int i = 0; i < SSIS.size(); i++) {
				levels.add(-99.0);
			}
			
			for (int i = 0; i < wifiList.size(); i++) {
				final ScanResult res = wifiList.get(i);
				LOG.severe("Found:" + res.SSID + " at level:" + res.level);
				if (SSIS.containsKey(res.SSID)) {
					levels.set(SSIS.get(res.SSID), Double.valueOf(res.level));
				}
			}
			final double[] list = new double[levels.size()];
			for (int i = 0; i < levels.size(); i++) {
				list[i] = levels.get(i);
			}
			final Vector vector = new BasicVector(list);
			final double norm = vector.fold(Vectors.mkManhattanNormAccumulator());
			current = vector.divide(norm);
			
			EventBus.getDefault().post(new StateEvent(null, "ReceivedScan"));
		}
	}
}
