package com.likya.tlossw.web.definitions;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.log4j.Logger;

import com.likya.tlos.model.xmlbeans.common.JobTypeDetailsDocument.JobTypeDetails;

@ManagedBean
@ViewScoped
public class RemoteShellPanelMBean extends JobBaseBean implements Serializable {

	private static final Logger logger = Logger.getLogger(RemoteShellPanelMBean.class);

	private static final long serialVersionUID = 9209882592345778758L;

	private String jobPath;
	private String jobCommand;
	
	private String ipAddress;
	private String port;
	private String userName;
	private String password;
	private String confirmPassword;
	
	public void dispose() {

	}

	@PostConstruct
	public void init() {
		initJobPanel();
	}

	public void fillTabs() {
		fillJobPanel();
		resetRemoteShellProperties();
		fillRemoteShellProperties();
	}
	
	private void resetRemoteShellProperties() {
		jobPath = "";
		jobCommand = "";
		ipAddress = "";
		port = "";
		userName = "";
		password = "";
		confirmPassword = "";
	}

	private void fillRemoteShellProperties() {
		if (getJobProperties() != null) {
			JobTypeDetails jobTypeDetails = getJobProperties().getBaseJobInfos().getJobInfos().getJobTypeDetails();

			jobPath = jobTypeDetails.getJobPath();
			jobCommand = jobTypeDetails.getJobCommand();
			
			//TODO doldur
		} else {
			System.out.println("getJobProperties() is NULL in fillRemoteShellProperties !!");
		}
	}

	public void insertJsAction() {
		if (validateTimeManagement()) {
			fillJobProperties();
			fillRemoteShellPropertyDetails();

			insertJobDefinition();
		}
	}

	public void updateJsAction() {
		fillJobProperties();
		fillRemoteShellPropertyDetails();

		updateJobDefinition();
	}

	private void fillRemoteShellPropertyDetails() {
		JobTypeDetails jobTypeDetails = getJobProperties().getBaseJobInfos().getJobInfos().getJobTypeDetails();
		jobTypeDetails.setJobCommand(jobCommand);
		jobTypeDetails.setJobPath(jobPath);
		
		//TODO doldur
	}

	public static Logger getLogger() {
		return logger;
	}

	public String getJobPath() {
		return jobPath;
	}

	public void setJobPath(String jobPath) {
		this.jobPath = jobPath;
	}

	public String getJobCommand() {
		return jobCommand;
	}

	public void setJobCommand(String jobCommand) {
		this.jobCommand = jobCommand;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getConfirmPassword() {
		return confirmPassword;
	}

	public void setConfirmPassword(String confirmPassword) {
		this.confirmPassword = confirmPassword;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

}