/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.demo.conferenceCloud;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * The Class Info.
 */
public class Info implements Serializable, Comparable<Info> {
	private static final long	serialVersionUID	= 2772087473749148297L;
	private DateTime			lastSeen			= new DateTime(0);
	private boolean				known				= false;
	private boolean				ignored				= false;
	
	private Set<String>			phonenumbers		= new HashSet<String>();
	private Set<String>			emailAddresses		= new HashSet<String>();
	
	private String				id					= null;
	private String				name				= null;
	private String				why					= null;
	
	/**
	 * Instantiates a new info.
	 */
	public Info() {
	}
	
	/**
	 * Instantiates a new info.
	 * 
	 * @param id
	 *            the id
	 */
	public Info(final String id) {
		setId(id);
	}
	
	/**
	 * Merge.
	 * 
	 * @param other
	 *            the other
	 * @return the info
	 */
	public Info merge(Info other) {
		if (other.lastSeen.isAfter(lastSeen)) {
			lastSeen = other.lastSeen;
		}
		phonenumbers.addAll(other.phonenumbers);
		emailAddresses.addAll(other.emailAddresses);
		if (other.ignored) {
			ignored = true;
		}
		if (other.known) {
			known = true;
		}
		this.name = other.name;
		return this;
	}
	
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
	
	/**
	 * @return the lastSeen
	 */
	public DateTime getLastSeen() {
		return lastSeen;
	}
	
	/**
	 * @param lastSeen
	 *            the lastSeen to set
	 */
	public void setLastSeen(DateTime lastSeen) {
		this.lastSeen = lastSeen;
	}
	
	/**
	 * @return the ignored
	 */
	public boolean isIgnored() {
		return ignored;
	}
	
	/**
	 * @param ignored
	 *            the ignored to set
	 */
	public void setIgnored(boolean ignored) {
		this.ignored = ignored;
	}
	
	/**
	 * @return the phonenumbers
	 */
	public Set<String> getPhonenumbers() {
		return phonenumbers;
	}
	
	/**
	 * @param phonenumbers
	 *            the phonenumbers to set
	 */
	public void setPhonenumbers(Set<String> phonenumbers) {
		this.phonenumbers = phonenumbers;
	}
	
	/**
	 * @return the emailAddresses
	 */
	public Set<String> getEmailAddresses() {
		return emailAddresses;
	}
	
	/**
	 * @param emailAddresses
	 *            the emailAddresses to set
	 */
	public void setEmailAddresses(Set<String> emailAddresses) {
		this.emailAddresses = emailAddresses;
	}
	
	/**
	 * Gets the id.
	 * 
	 * @return the id
	 */
	public String getId() {
		return id;
	}
	
	/**
	 * Sets the id.
	 * 
	 * @param id
	 *            the new id
	 */
	public void setId(String id) {
		this.id = id;
	}
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * @return the why
	 */
	public String getWhy() {
		return why;
	}
	
	/**
	 * @param why
	 *            the why to set
	 */
	public void setWhy(String why) {
		this.why = why;
	}
	
	@Override
	public String toString() {
		DateTimeFormatter outputFormatter = 
	             DateTimeFormat.forPattern("HH:mm").withZone(DateTimeZone.getDefault());
		return getName() + (getWhy() != null ? " - " + getWhy() : "") + "("
				+ outputFormatter.print(lastSeen) + ")";
	}
	
	@Override
	public int compareTo(Info o) {
		return lastSeen.compareTo(o.lastSeen);
	}
}
