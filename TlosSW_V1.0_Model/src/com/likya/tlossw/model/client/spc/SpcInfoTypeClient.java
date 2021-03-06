/*
 * TlosFaz_V2.0_Model
 * com.likya.tlos.core.cpc.model : SpcInfoTypeClient.java
 * @author Serkan Tas
 * Tarih : Apr 20, 2009 11:15:58 AM
 */

package com.likya.tlossw.model.client.spc;

import java.io.Serializable;

public class SpcInfoTypeClient implements Serializable {

	private static final long serialVersionUID = 1L;

	private SpcInfoTypeClient [] spcInfoTypeClients;
	
	private String spcId;
	private String jsName;
	private int numOfJobs;
	private int numOfActiveJobs;

	private boolean stopable;
	private boolean pausable;
	private boolean resumable;
	private boolean startable;
	
	private String nativeRunId;
	private String currentRunId;
	
	public SpcInfoTypeClient() {
		super();
	}
	
	public SpcInfoTypeClient(SpcInfoTypeClient cloneSpcInfoTypeClient) {
		super();

		if(cloneSpcInfoTypeClient.getSpcId() != null) {
			spcId = new String(cloneSpcInfoTypeClient.getSpcId());
		}else {
			spcId = null;
		}
		if(cloneSpcInfoTypeClient.getJsName() != null) {
			jsName = new String(cloneSpcInfoTypeClient.getJsName());
		}else {
			jsName = null;
		}
		
		if(cloneSpcInfoTypeClient.getSpcInfoTypeClients() != null) {
			spcInfoTypeClients =  cloneSpcInfoTypeClient.getSpcInfoTypeClients().clone();
		}else {
			spcInfoTypeClients =  null;
		}
		
		numOfJobs = cloneSpcInfoTypeClient.getNumOfJobs();
		numOfActiveJobs = cloneSpcInfoTypeClient.getNumOfActiveJobs();

		stopable = cloneSpcInfoTypeClient.getStopable();
		pausable = cloneSpcInfoTypeClient.getPausable();
		resumable = cloneSpcInfoTypeClient.getResumable();
		startable = cloneSpcInfoTypeClient.getStartable();
		
		currentRunId = cloneSpcInfoTypeClient.getCurrentRunId();
		nativeRunId = cloneSpcInfoTypeClient.getNativeRunId();
				
	}

	public String getSpcId() {
		return spcId;
	}

	public void setSpcId(String spcId) {
		this.spcId = spcId;
	}

	public int getNumOfActiveJobs() {
		return numOfActiveJobs;
	}

	public void setNumOfActiveJobs(int numOfActiveJobs) {
		this.numOfActiveJobs = numOfActiveJobs;
	}

	public int getNumOfJobs() {
		return numOfJobs;
	}

	public void setNumOfJobs(int numOfJobs) {
		this.numOfJobs = numOfJobs;
	}

	public boolean getStopable() {
		return stopable;
	}

	public void setStopable(boolean stopable) {
		this.stopable = stopable;
	}

	public boolean getPausable() {
		return pausable;
	}

	public void setPausable(boolean pausable) {
		this.pausable = pausable;
	}

	public boolean getResumable() {
		return resumable;
	}

	public void setResumable(boolean resumable) {
		this.resumable = resumable;
	}

	public boolean getStartable() {
		return startable;
	}

	public void setStartable(boolean startable) {
		this.startable = startable;
	}

	public SpcInfoTypeClient[] getSpcInfoTypeClients() {
		return spcInfoTypeClients;
	}

	public void setSpcInfoTypeClients(SpcInfoTypeClient[] spcInfoTypeClients) {
		this.spcInfoTypeClients = spcInfoTypeClients;
	}

	public String getJsName() {
		return jsName;
	}

	public void setJsName(String jsName) {
		this.jsName = jsName;
	}

	public String getNativeRunId() {
		return nativeRunId;
	}

	public void setNativeRunId(String nativeRunId) {
		this.nativeRunId = nativeRunId;
	}

	public String getCurrentRunId() {
		return currentRunId;
	}

	public void setCurrentRunId(String currentRunId) {
		this.currentRunId = currentRunId;
	}
	
}
