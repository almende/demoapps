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
	
	public static void loadSSIDs(String[] ssids) {
		SSIS.clear();
		for (int i = 0; i < ssids.length; i++) {
			SSIS.put(ssids[i], i);
		}
	}
	
	private PositionUtil() {
	}
	
	public static void setContext(Context ctx) {
		PositionUtil.ctx = ctx;
	}
	
	public static PositionUtil getInstance() {
		return instance;
	}
	
	public void doCompare(final String remoteList){
		
	}
	
	
	public Vector getCurrent() {
		return current;
	}

	public void startScan() {
		if (wm == null) {
			wm = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
			WifiScanReceiver wifiReciever = new WifiScanReceiver();
			ctx.registerReceiver(wifiReciever, new IntentFilter(
					WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		}
		if (!wm.isWifiEnabled()) {
			wm.setWifiEnabled(true);
		}
		wm.startScan();
	}
	
	class WifiScanReceiver extends BroadcastReceiver {
		private List<ScanResult>	wifiList;
		
		public void onReceive(Context c, Intent intent) {
			wifiList = wm.getScanResults();
			ArrayList<Double> levels = new ArrayList<Double>(SSIS.size());
			for (int i = 0; i < SSIS.size(); i++) {
				levels.add(-99.0);
			}
			
			for (int i = 0; i < wifiList.size(); i++) {
				ScanResult res = wifiList.get(i);
				LOG.severe("Found:" + res.SSID + " at level:" + res.level);
				if (SSIS.containsKey(res.SSID)) {
					levels.set(SSIS.get(res.SSID), Double.valueOf(res.level));
				}
			}
			double[] list = new double[levels.size()];
			for (int i = 0; i < levels.size(); i++) {
				list[i] = levels.get(i);
			}
			Vector vector = new BasicVector(list);
			double norm = vector.fold(Vectors.mkManhattanNormAccumulator());
			current = vector.divide(norm);
			
			EventBus.getDefault().post(
					new StateEvent(null, "ReceivedScan"));
		}
	}
}
