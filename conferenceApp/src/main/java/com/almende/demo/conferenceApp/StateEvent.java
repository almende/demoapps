package com.almende.demo.conferenceApp;

public class StateEvent {
	private String	value	= "";
	private String  agentId = "";
	
	public StateEvent(String agentId,String value) {
		this.setValue(value);
		this.setAgentId(agentId);
	}
	
	public StateEvent(String agentId) {
		this.setAgentId(agentId);
	}
	
	public String getValue() {
		return value;
	}
	
	public void setValue(String value) {
		this.value = value;
	}

	public String getAgentId() {
		return agentId;
	}

	public void setAgentId(String agentId) {
		this.agentId = agentId;
	}
}
