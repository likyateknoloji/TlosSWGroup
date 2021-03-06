/*
 * TlosFaz_V2.0_Model
 * com.likya.tlos.core.cpc.model : SpcInfoTypeClient.java
 * @author Serkan Tas
 * Tarih : Apr 20, 2009 11:15:58 AM
 */

package com.likya.tlossw.model.client.spc;

import java.util.ArrayList;

import com.likya.tlos.model.xmlbeans.parameters.ParameterDocument.Parameter;
import com.likya.tlos.model.xmlbeans.state.LiveStateInfoDocument.LiveStateInfo;

public class JobInfoTypeClient extends JobInfoTypeClientBase {

	// Paramaters from JobPropertiesType

	/**
	 * 
	 */
	private static final long serialVersionUID = -6027037594587545575L;

	private String jobCommandType;
	private ArrayList<String> jobDependencyList;
	private boolean jobAutoRetry;
	private ArrayList<Integer> jobReturnCodeIgnoreList;

	// Paramaters from JobRunPropertiesType
	private String plannedExecutionDate = "-";
	private String completionDate = "-";
	private String workDuration = "-";
	private boolean isOver;
	private LiveStateInfo liveStateInfo;
	private String SSSName;

	private String resourceName;
	private int agentId;
	private String nativeRunId;
	private String currentRunId;
	private String fullPath;
	private String relativePath;
	private String LSIDateTime;

	private String outParameterName;
	private int outParameterType;
	private String outParameterValue;

	private String inParameterName;
	private int inParameterType;
	private String inParameterValue;
	
	// Stats
	private double max;
	private double min;
	private double avg;

	private ArrayList<Parameter> parameterList = new ArrayList<Parameter>();
	private Parameter selectedRow;
	
	// Paramaters for webpage
	// private String statusStr;

	// private int dependJobNumber;

	public String getPlannedExecutionDate() {
		return plannedExecutionDate;
	}

	public void setPlannedExecutionDate(String plannedExecutionDate) {
		this.plannedExecutionDate = plannedExecutionDate;
	}

	public String getCompletionDate() {
		return completionDate;
	}

	public void setCompletionDate(String completionDate) {
		this.completionDate = completionDate;
	}

	public String getWorkDuration() {
		return workDuration;
	}

	public void setWorkDuration(String workDuration) {
		this.workDuration = workDuration;
	}

	public boolean isOver() {
		return isOver;
	}

	public void setOver(boolean isOver) {
		this.isOver = isOver;
	}

	public java.lang.String getJobCommandType() {
		return jobCommandType;
	}

	public void setJobCommandType(java.lang.String jobCommandType) {
		this.jobCommandType = jobCommandType;
	}

	public ArrayList<String> getJobDependencyList() {
		return jobDependencyList;
	}

	public void setJobDependencyList(ArrayList<String> jobDependencyList) {
		this.jobDependencyList = jobDependencyList;
	}

	public boolean getJobAutoRetry() {
		return jobAutoRetry;
	}

	public void setJobAutoRetry(boolean jobAutoRetry) {
		this.jobAutoRetry = jobAutoRetry;
	}

	public ArrayList<Integer> getJobReturnCodeIgnoreList() {
		return jobReturnCodeIgnoreList;
	}

	public void setJobReturnCodeIgnoreList(ArrayList<Integer> jobReturnCodeIgnoreList) {
		this.jobReturnCodeIgnoreList = jobReturnCodeIgnoreList;
	}

	// public void setStatusStr(String statusStr) {
	// this.statusStr = statusStr;
	// }

	// public int getDependJobNumber() {
	// return dependJobNumber;
	// }
	//
	// public void setDependJobNumber(int dependJobNumber) {
	// this.dependJobNumber = dependJobNumber;
	// }

	public LiveStateInfo getLiveStateInfo() {
		return liveStateInfo;
	}

	public void setLiveStateInfo(LiveStateInfo liveStateInfo) {
		this.liveStateInfo = liveStateInfo;
	}

	public void setAgentId(int agentId) {
		this.agentId = agentId;
	}

	public int getAgentId() {
		return agentId;
	}

	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}

	public String getResourceName() {
		return resourceName;
	}

	public void setLSIDateTime(String lSIDateTime) {
		LSIDateTime = lSIDateTime;
	}

	public String getLSIDateTime() {
		return LSIDateTime;
	}

	// public String getStatusStr() {
	// return statusStr;
	// }

	public String getOutParameterName() {
		return outParameterName;
	}

	public void setOutParameterName(String outParameterName) {
		this.outParameterName = outParameterName;
	}

	public String getOutParameterValue() {
		return outParameterValue;
	}

	public void setOutParameterValue(String outParameterValue) {
		this.outParameterValue = outParameterValue;
	}

	public String getInParameterName() {
		return inParameterName;
	}

	public void setInParameterName(String inParameterName) {
		this.inParameterName = inParameterName;
	}

	public String getInParameterValue() {
		return inParameterValue;
	}

	public void setInParameterValue(String inParameterValue) {
		this.inParameterValue = inParameterValue;
	}

	public int getOutParameterType() {
		return outParameterType;
	}

	public void setOutParameterType(int outParameterType) {
		this.outParameterType = outParameterType;
	}

	public int getInParameterType() {
		return inParameterType;
	}

	public void setInParameterType(int inParameterType) {
		this.inParameterType = inParameterType;
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

	public String getSSSName() {
		return SSSName;
	}

	public void setSSSName(String sSSName) {
		SSSName = sSSName;
	}

	public ArrayList<Parameter> getParameterList() {
		return parameterList;
	}

	public void setParameterList(ArrayList<Parameter> parameterList) {
		this.parameterList = parameterList;
	}

	public Parameter getSelectedRow() {
		return selectedRow;
	}

	public void setSelectedRow(Parameter selectedRow) {
		this.selectedRow = selectedRow;
	}

	public String getFullPath() {
		return fullPath;
	}

	public void setFullPath(String fullPath) {
		this.fullPath = fullPath;
	}

	public String getRelativePath() {
		return relativePath;
	}

	public void setRelativePath(String relativePath) {
		this.relativePath = relativePath;
	}

	public double getMax() {
		return max;
	}

	public void setMax(double max) {
		this.max = max;
	}

	public double getMin() {
		return min;
	}

	public void setMin(double min) {
		this.min = min;
	}

	public double getAvg() {
		return avg;
	}

	public void setAvg(double avg) {
		this.avg = avg;
	}

}
