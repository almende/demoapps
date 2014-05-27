/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.demo.conferenceApp;

import java.util.logging.Logger;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.TelephonyManager;
import de.greenrobot.event.EventBus;

/**
 * The Class PositionUtil.
 */
public class DetectionUtil {
	private final static Logger		LOG			= Logger.getLogger(DetectionUtil.class
														.getName());
	private static DetectionUtil	instance	= new DetectionUtil();
	private static Context			ctx			= null;
	private static BluetoothAdapter	bt			= null;
	private static final int		CLOSEBY		= 70;
	private BroadcastReceiver		btReceiver	= null;
	
	private DetectionUtil() {
	}
	
	/**
	 * Sets the context.
	 * 
	 * @param ctx
	 *            the new context
	 */
	public static void setContext(final Context ctx) {
		DetectionUtil.ctx = ctx;
	}
	
	/**
	 * Gets the single instance of DetectionUtil.
	 * 
	 * @return single instance of DetectionUtil
	 */
	public static DetectionUtil getInstance() {
		return instance;
	}
	
	/**
	 * Start scan.
	 */
	public void startScan() {
		if (bt == null) {
			bt = BluetoothAdapter.getDefaultAdapter();
			initBTReceiver();
		}
		if (!bt.isEnabled()) {
			Intent enableBtIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			ctx.startActivity(enableBtIntent);
		}
		final TelephonyManager tm = (TelephonyManager) ctx
				.getSystemService(Context.TELEPHONY_SERVICE);
		bt.setName("CapeDemo_" + tm.getDeviceId());
		BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();
		if (ba.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
			Intent discoverableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			discoverableIntent.putExtra(
					BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, -1);
			ctx.startActivity(discoverableIntent);
		}
		bt.startDiscovery();
	}
	
	private void initBTReceiver() {
		btReceiver = new BroadcastReceiver() {
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				// When discovery finds
				// a device
				if (BluetoothDevice.ACTION_FOUND.equals(action)) {
					// Get the
					// BluetoothDevice
					// object from the
					// Intent
					BluetoothDevice device = intent
							.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
					final String name = device.getName();
					final short rssi = intent.getShortExtra(
							BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
					if (name != null && name.startsWith("CapeDemo_")) {
						LOG.warning("Found BT:" + name + " at level:" + rssi);
						if (rssi > -CLOSEBY) {
							EventBus.getDefault().post(
									new StateEvent(name
											.replace("CapeDemo_", ""),
											"scanRes"));
						}
					}
				}
			}
		};
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		ctx.registerReceiver(btReceiver, filter); // Don't forget to unregister
													// during onDestroy
		
	}
	
	public void close() {
		bt.cancelDiscovery();
		ctx.unregisterReceiver(btReceiver);
	}
}
