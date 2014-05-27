/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.demo.conferenceCloud;

import java.io.Serializable;

/**
 * The Class Info.
 */
public class Info implements Serializable {
	private static final long	serialVersionUID	= 2772087473749148297L;
	private boolean known = false;
	
	//Possible fields:
		//mobile phone number
		//Popular name
		//URL to photo?
	
	/**
	 * Instantiates a new info.
	 */
	public Info(){}

	/**
	 * Checks if is known.
	 * 
	 * @return true, if is known
	 */
	public boolean isKnown() {
		return known;
	}

	/**
	 * Sets the known.
	 * 
	 * @param known
	 *            the new known
	 */
	public void setKnown(final boolean known) {
		this.known = known;
	}
	
	
}
