/*
 * TlosSW_V1.0_Model
 * com.likya.tlossw.model.client.resource : AgentLookUpTableTypeClient.java
 * @author Merve Ozbey
 * Tarih : 20.Sub.2012 15:00:37
 */

package com.likya.tlossw.model.client.resource;

import java.io.Serializable;
import java.util.HashMap;

public class AgentLookUpTableTypeClient implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private HashMap<Integer, TlosAgentInfoTypeClient> tAgentInfoTypeClientList;

	private MonitorAgentInfoTypeClient nAgentInfoTypeClient;
	
	public AgentLookUpTableTypeClient() {
		this.tAgentInfoTypeClientList = new HashMap<Integer, TlosAgentInfoTypeClient>();
		this.nAgentInfoTypeClient = new MonitorAgentInfoTypeClient();
	}

	public void setTAgentInfoTypeClientList(HashMap<Integer, TlosAgentInfoTypeClient> tAgentInfoTypeClientList) {
		this.tAgentInfoTypeClientList = tAgentInfoTypeClientList;
	}

	public HashMap<Integer, TlosAgentInfoTypeClient> getTAgentInfoTypeClientList() {
		return tAgentInfoTypeClientList;
	}

	public void setNAgentInfoTypeClient(MonitorAgentInfoTypeClient nAgentInfoTypeClient) {
		this.nAgentInfoTypeClient = nAgentInfoTypeClient;
	}

	public MonitorAgentInfoTypeClient getNAgentInfoTypeClient() {
		return nAgentInfoTypeClient;
	}

	

}
