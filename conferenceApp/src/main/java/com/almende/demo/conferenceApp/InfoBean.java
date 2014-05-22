/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.demo.conferenceApp;

import java.io.Serializable;

/**
 * The Class InfoBean.
 */
public class InfoBean implements Serializable {
	private static final long	serialVersionUID	= 1580662192874181492L;
	private String				locVector			= null;
	private String				deviceId			= null;
	
	/**
	 * Instantiates a new info bean.
	 */
	public InfoBean() {
	}
	
	/**
	 * Gets the loc vector.
	 * 
	 * @return the loc vector
	 */
	public String getLocVector() {
		return locVector;
	}
	
	/**
	 * Sets the loc vector.
	 * 
	 * @param locVector
	 *            the new loc vector
	 */
	public void setLocVector(final String locVector) {
		this.locVector = locVector;
	}
	
	/**
	 * Gets the device id.
	 * 
	 * @return the device id
	 */
	public String getDeviceId() {
		return deviceId;
	}
	
	/**
	 * Sets the device id.
	 * 
	 * @param deviceId
	 *            the new device id
	 */
	public void setDeviceId(final String deviceId) {
		this.deviceId = deviceId;
	}
	
}
