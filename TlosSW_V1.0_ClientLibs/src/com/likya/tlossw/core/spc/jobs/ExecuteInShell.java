package com.likya.tlossw.core.spc.jobs;

import java.util.Map;

import org.apache.log4j.Logger;

import com.likya.tlos.model.xmlbeans.data.JobPropertiesDocument.JobProperties;
import com.likya.tlos.model.xmlbeans.state.StateNameDocument.StateName;
import com.likya.tlos.model.xmlbeans.state.StatusNameDocument.StatusName;
import com.likya.tlos.model.xmlbeans.state.SubstateNameDocument.SubstateName;
import com.likya.tlossw.core.spc.model.JobRuntimeProperties;
import com.likya.tlossw.utils.GlobalRegistry;
import com.likya.tlossw.utils.LiveStateInfoUtils;
import com.likya.tlossw.utils.XmlBeansTransformer;

public class ExecuteInShell extends ExecuteOSComponent {

	private static final long serialVersionUID = 1L;

	transient Logger myLogger = Logger.getLogger(ExecuteInShell.class);

	transient Logger globalLogger = getGlobalLogger();

	private boolean retryFlag = true;

	boolean otherJobTypeResult = false;

	public ExecuteInShell(GlobalRegistry globalRegistry, Logger globalLogger, JobRuntimeProperties jobRuntimeProperties) {
		super(globalRegistry, globalLogger, jobRuntimeProperties);
	}

	public void run() {

		initStartUp(myLogger);

		JobProperties jobProperties = getJobRuntimeProperties().getJobProperties();

		while (true) {

			try {

				startWathcDogTimer();

				String jobPath = jobProperties.getBaseJobInfos().getJobInfos().getJobTypeDetails().getJobPath();
				String jobCommand = jobProperties.getBaseJobInfos().getJobInfos().getJobTypeDetails().getJobCommand();
				Map<String, String> env = XmlBeansTransformer.entryToMap(jobProperties);
				
				LiveStateInfoUtils.insertNewLiveStateInfo(jobProperties, StateName.INT_RUNNING, SubstateName.INT_ON_RESOURCE, StatusName.INT_TIME_IN);

				sendStatusChangeInfo();

				// TODO Burası eksik kaldı, 3. parametre tamamlanmalı
				// Serkan Taş 13.08.2012
				// Map<String, String> env = new HashMap<String, String>();
				// env.put("PGPASSWORD", password);

				startShellProcess(jobPath, jobCommand, env, this.getClass().getName(), myLogger);

			} catch (Exception err) {
				handleException(err, myLogger);
			}

			sendStatusChangeInfo();

			if (processJobResult(retryFlag, myLogger)) {
				retryFlag = false;
				continue;
			}

			break;
		}

		cleanUp(getProcess(), myLogger);

	}

}