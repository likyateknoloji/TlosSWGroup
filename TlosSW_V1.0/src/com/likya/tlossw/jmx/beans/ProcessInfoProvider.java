/*
 * TlosFaz_V2.0
 * com.likya.tlos.jmx.mp.helper : ProcessInfoProvider.java
 * @author Serkan Taş
 * Tarih : Apr 6, 2009 2:19:17 PM
 */

package com.likya.tlossw.jmx.beans;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.xmldb.api.base.XMLDBException;

import com.likya.tlos.model.xmlbeans.agent.SWAgentDocument.SWAgent;
import com.likya.tlos.model.xmlbeans.agent.UserStopRequestDocument.UserStopRequest;
import com.likya.tlos.model.xmlbeans.common.InParamDocument.InParam;
import com.likya.tlos.model.xmlbeans.common.LocalParametersDocument.LocalParameters;
import com.likya.tlos.model.xmlbeans.common.OutParamDocument.OutParam;
import com.likya.tlos.model.xmlbeans.common.SpecialParametersDocument.SpecialParameters;
import com.likya.tlos.model.xmlbeans.data.BaseJobInfosDocument.BaseJobInfos;
import com.likya.tlos.model.xmlbeans.data.ItemDocument.Item;
import com.likya.tlos.model.xmlbeans.data.JobPropertiesDocument.JobProperties;
import com.likya.tlos.model.xmlbeans.data.TimeControlDocument.TimeControl;
import com.likya.tlos.model.xmlbeans.data.TimeManagementDocument.TimeManagement;
import com.likya.tlos.model.xmlbeans.dbconnections.DbConnectionProfileDocument.DbConnectionProfile;
import com.likya.tlos.model.xmlbeans.dbconnections.DbPropertiesDocument.DbProperties;
import com.likya.tlos.model.xmlbeans.dbconnections.DbTypeDocument.DbType;
import com.likya.tlos.model.xmlbeans.parameters.ParameterDocument.Parameter;
import com.likya.tlos.model.xmlbeans.state.StateNameDocument.StateName;
import com.likya.tlossw.TlosSpaceWide;
import com.likya.tlossw.core.agents.AgentOperations;
import com.likya.tlossw.core.cpc.model.RunInfoType;
import com.likya.tlossw.core.cpc.model.SpcInfoType;
import com.likya.tlossw.core.spc.Spc;
import com.likya.tlossw.core.spc.helpers.RunMapHelper;
import com.likya.tlossw.core.spc.helpers.JobQueueOperations;
import com.likya.tlossw.core.spc.helpers.SortType;
import com.likya.tlossw.core.spc.jobs.Job;
import com.likya.tlossw.core.spc.model.JobRuntimeProperties;
import com.likya.tlossw.db.utils.DBUtils;
import com.likya.tlossw.jmx.JMXTLSServer;
import com.likya.tlossw.model.JobStatusSummary;
import com.likya.tlossw.model.ScenarioStatus;
import com.likya.tlossw.model.WebSpaceWideRegistery;
import com.likya.tlossw.model.client.resource.AgentLookUpTableTypeClient;
import com.likya.tlossw.model.client.resource.MonitorAgentInfoTypeClient;
import com.likya.tlossw.model.client.resource.ResourceInfoTypeClient;
import com.likya.tlossw.model.client.resource.TlosAgentInfoTypeClient;
import com.likya.tlossw.model.client.spc.JobInfoTypeClient;
import com.likya.tlossw.model.client.spc.SpcInfoTypeClient;
import com.likya.tlossw.model.client.spc.TreeInfoType;
import com.likya.tlossw.model.jmx.JmxAgentUser;
import com.likya.tlossw.model.jmx.JmxUser;
import com.likya.tlossw.model.path.TlosSWPathType;
import com.likya.tlossw.model.tree.resource.MonitorAgentNode;
import com.likya.tlossw.model.tree.resource.ResourceListNode;
import com.likya.tlossw.model.tree.resource.ResourceNode;
import com.likya.tlossw.model.tree.resource.TlosAgentNode;
import com.likya.tlossw.model.tree.resource.TlosSWResourceNode;
import com.likya.tlossw.utils.CommonConstantDefinitions;
import com.likya.tlossw.utils.CpcUtils;
import com.likya.tlossw.utils.RunUtils;
import com.likya.tlossw.utils.XmlUtils;
import com.likya.tlossw.utils.date.DateUtils;
import com.likya.tlossw.utils.transform.TransformUtils;

public class ProcessInfoProvider implements ProcessInfoProviderMBean {

	public static final String SERVER_TYPE = "server";

	private boolean isTester(JmxUser jmxUser) {

		if (CommonConstantDefinitions.EXIST_MYDATA.equals(jmxUser.getViewRoleId())) {
			return true;
		}

		return false;
	}

	public boolean retrieveWaitConfirmOfGUI(JmxUser jmxUser) {
		return TlosSpaceWide.getSpaceWideRegistry().isWaitConfirmOfGUI();
	}

	public WebSpaceWideRegistery retrieveSpaceWideRegistery(JmxUser jmxUser) {

		if (!JMXTLSServer.authorizeWeb(jmxUser)) {
			return null;
		}

		WebSpaceWideRegistery webSpaceWideRegistery = new WebSpaceWideRegistery();

		webSpaceWideRegistery.setFirstTime(TlosSpaceWide.getSpaceWideRegistry().isFIRST_TIME());
		webSpaceWideRegistery.setWaitConfirmOfGUI(TlosSpaceWide.getSpaceWideRegistry().isWaitConfirmOfGUI());
		webSpaceWideRegistery.setPersistent(TlosSpaceWide.getSpaceWideRegistry().getServerConfig().getServerParams().getIsPersistent().getUse());
		webSpaceWideRegistery.setInstanceCount(TlosSpaceWide.getSpaceWideRegistry().getRunLookupTable().size());

		return webSpaceWideRegistery;
	}

	public JobInfoTypeClient retrieveJobDetails(JmxUser jmxUser, String spcFullPath, String jobId, Boolean transformToLocalTime) {

		if (!JMXTLSServer.authorizeWeb(jmxUser)) {
			return null;
		}

		JobInfoTypeClient jobInfoTypeClient = new JobInfoTypeClient();

		SpcInfoType spcInfoType = null;

		if (isTester(jmxUser)) {
			spcInfoType = TlosSpaceWide.getSpaceWideRegistry().getCpcTesterReference().getSpcLookupTable(jmxUser.getId() + "").getTable().get(spcFullPath);
		} else {
			spcInfoType = RunMapHelper.findSpc(spcFullPath, TlosSpaceWide.getSpaceWideRegistry().getRunLookupTable());
		}

		JobRuntimeProperties jobRuntimeProperties = null;

		Iterator<SortType> jobQueueIndexIterator = spcInfoType.getSpcReferance().getJobQueueIndex().iterator();

		while (jobQueueIndexIterator.hasNext()) {

			SortType sortType = jobQueueIndexIterator.next();
			Job scheduledJob = spcInfoType.getSpcReferance().getJobQueue().get(sortType.getJobId());
			jobRuntimeProperties = scheduledJob.getJobRuntimeProperties();
			if (jobRuntimeProperties.getJobProperties().getID().equals(jobId)) {
				break;
			}
		}

		if (jobRuntimeProperties == null) {
			return null;
		}

		JobProperties jobProperties = jobRuntimeProperties.getJobProperties();
		BaseJobInfos baseJobInfos = jobProperties.getBaseJobInfos();

		jobInfoTypeClient.setCurrentRunId(spcInfoType.getSpcReferance().getCurrentRunId());
		jobInfoTypeClient.setJobId(jobProperties.getID());
		jobInfoTypeClient.setJobName(baseJobInfos.getJsName());
		jobInfoTypeClient.setJobCommand(baseJobInfos.getJobTypeDetails().getJobCommand());
		jobInfoTypeClient.setJobCommandType(baseJobInfos.getJobTypeDetails().getJobCommandType().toString());
		jobInfoTypeClient.setTreePath(jobRuntimeProperties.getAbsoluteJobPath());
		jobInfoTypeClient.setJobPath(baseJobInfos.getJobTypeDetails().getJobPath());
		jobInfoTypeClient.setJobLogPath(baseJobInfos.getJobLogPath());
		jobInfoTypeClient.setJobLogName(baseJobInfos.getJobLogFile());
		jobInfoTypeClient.setoSystem(baseJobInfos.getOSystem().toString());
		jobInfoTypeClient.setFullPath(spcInfoType.getSpcId().getFullPath());
		jobInfoTypeClient.setRelativePath(spcInfoType.getSpcId().getAbsolutePath());
		
		// TODO Geçici olarak tip dönüşümü yaptım.
		jobInfoTypeClient.setJobPriority(baseJobInfos.getJobPriority().intValue());

		TimeManagement timeManagement = jobProperties.getManagement().getTimeManagement();
		TimeControl timeControl = jobProperties.getManagement().getTimeControl();
		
		jobInfoTypeClient.setJobPlanTime(DateUtils.jobTimeToString(timeManagement.getJsPlannedTime().getStartTime().getTime(), false));
		jobInfoTypeClient.setJobTimeOut(timeControl.getJsTimeOut().toString() );

		// agentlarda calisan joblarin PlannedExecutionDate, CompletionDate ve WorkDuration alanlari set edilmediginden onlari jobRealTime kismindan set ediyoruz
		if (jobRuntimeProperties.getPlannedExecutionDate() == null && (timeManagement.getJsRealTime() != null)) {
			jobInfoTypeClient.setPlannedExecutionDate(DateUtils.jobRealTimeToString(timeManagement.getJsRealTime(), true, transformToLocalTime));

			// is hala calisiyorsa
			if (timeManagement.getJsRealTime().getStopTime() != null) {
				jobInfoTypeClient.setCompletionDate(DateUtils.jobRealTimeToString(timeManagement.getJsRealTime(), false, transformToLocalTime));
			}

		} else if (jobRuntimeProperties.getPlannedExecutionDate() != null) {
			jobInfoTypeClient.setPlannedExecutionDate(DateUtils.calendarToString(jobRuntimeProperties.getPlannedExecutionDate(), false));

			if (jobRuntimeProperties.getCompletionDate() != null) {
				jobInfoTypeClient.setCompletionDate(DateUtils.calendarToString(jobRuntimeProperties.getCompletionDate(), false));
			}
		}

		jobInfoTypeClient.setWorkDuration(DateUtils.getJobWorkDuration(timeManagement.getJsRealTime(), false));

		// jobInfoTypeClient.setOver(jobRuntimeProperties.getJobProperties().getLiveStateInfo().getStateName().equals(StateName.FINISHED));
		// jobInfoTypeClient.setLiveStateInfo(jobRuntimeProperties.getJobProperties().getLiveStateInfo());

		// LiveStateInfo listesindeki ilk eleman alinarak islem yapildi, yani guncel state i alindi
		jobInfoTypeClient.setOver(jobProperties.getStateInfos().getLiveStateInfos().getLiveStateInfoArray(0).getStateName().equals(StateName.FINISHED));
		jobInfoTypeClient.setLiveStateInfo(jobProperties.getStateInfos().getLiveStateInfos().getLiveStateInfoArray(0));

		String errorMessage = null;
		if (jobProperties.getStateInfos().getLiveStateInfos().getLiveStateInfoArray(0).getReturnCode() != null) {
			errorMessage = jobProperties.getStateInfos().getLiveStateInfos().getLiveStateInfoArray(0).getReturnCode().getDesc() != null ? 
					         jobProperties.getStateInfos().getLiveStateInfos().getLiveStateInfoArray(0).getReturnCode().getDesc().toString() : "Unknown Error ...";
		}

		jobInfoTypeClient.setErrorMessage(errorMessage);

		jobInfoTypeClient.setJobAutoRetry(jobProperties.getManagement().getCascadingConditions().getJobAutoRetry().getBooleanValue());
		// TODO geçici olarak dönüşüm yaptım ama xsd de problem var ????
		jobInfoTypeClient.setSafeRestart(jobProperties.getManagement().getCascadingConditions().getJobSafeToRestart());

		jobInfoTypeClient.setRetriable(jobRuntimeProperties.isRetriable());
		jobInfoTypeClient.setSuccessable(jobRuntimeProperties.isSuccessable());
		jobInfoTypeClient.setSkippable(jobRuntimeProperties.isSkippable());
		jobInfoTypeClient.setStopable(jobRuntimeProperties.isStopable());
		jobInfoTypeClient.setPausable(jobRuntimeProperties.isPausable());
		jobInfoTypeClient.setResumable(jobRuntimeProperties.isResumable());
		jobInfoTypeClient.setStartable(jobRuntimeProperties.isStartable());

		if (jobRuntimeProperties.getJobProperties().getDependencyList() != null) {
			List<Item> myList = Arrays.asList(jobProperties.getDependencyList().getItemArray());
			ArrayList<Item> dependencyList = new ArrayList<Item>(myList);
			Iterator<Item> dependencyListIterator = dependencyList.iterator();
			ArrayList<String> depenArrayList = new ArrayList<String>();
			while (dependencyListIterator.hasNext()) {
				depenArrayList.add(dependencyListIterator.next().getJsId());
			}
			jobInfoTypeClient.setJobDependencyList(depenArrayList);
			// jobInfoTypeClient.setDependJobNumber(depenArrayList.size());
		}

		jobInfoTypeClient.setAgentId(jobProperties.getAgentId());

		if (jobInfoTypeClient.getAgentId() > 0) {
			SWAgent agent = TlosSpaceWide.getSpaceWideRegistry().getAgentManagerReference().getSwAgentsCache().get(jobInfoTypeClient.getAgentId() + "");

			jobInfoTypeClient.setResourceName(agent.getResource().getStringValue());
		}

		// output parametre kısmına parametre yazıldıysa ekrandan gösterilmek üzere burada dolduruluyor
		LocalParameters localParameters = jobProperties.getLocalParameters();
		
		if (localParameters != null) {

			if (localParameters.getInParam() != null) {
				InParam inParam = localParameters.getInParam();

				for (Parameter parameter : inParam.getParameterArray()) {
					parameter.setIoType(false);
					jobInfoTypeClient.getParameterList().add(parameter);
				}
			}

			if (localParameters.getOutParam() != null) {
				OutParam outParam = localParameters.getOutParam();

				for (Parameter parameter : outParam.getParameterArray()) {
					parameter.setIoType(true);
					jobInfoTypeClient.getParameterList().add(parameter);
				}
			}

		}

		if (localParameters.getInParam() == null && localParameters.getOutParam() == null) {
			jobInfoTypeClient.setParameterList(new ArrayList<Parameter>());
		}

		

		if (localParameters != null && localParameters.getOutParam() != null && localParameters.getOutParam().sizeOfParameterArray() > 0) {

			OutParam outParam = localParameters.getOutParam();

			for (Parameter param : outParam.getParameterArray()) {
				jobInfoTypeClient.setOutParameterName(param.getName());
				jobInfoTypeClient.setOutParameterType(param.getPreValue().getType());

				jobInfoTypeClient.setOutParameterValue(TransformUtils.typeSelector(param));
			}
		}

		// input parametre kısmına parametre yazıldıysa ekrandan gösterilmek üzere burada dolduruluyor
		if (localParameters != null && localParameters.getInParam() != null && localParameters.getInParam().sizeOfParameterArray() > 0) {

			InParam inParam = localParameters.getInParam();

			for (Parameter param : inParam.getParameterArray()) {
				jobInfoTypeClient.setInParameterName(param.getName());
				jobInfoTypeClient.setInParameterType(param.getPreValue().getType());

				jobInfoTypeClient.setInParameterValue(TransformUtils.typeSelector(param));
			}
		}

		return jobInfoTypeClient;
	}

	private ArrayList<String> retrievePlanIds() {

		HashMap<String, RunInfoType> runLookUpTable = TlosSpaceWide.getSpaceWideRegistry().getRunLookupTable();
		Iterator<String> keyIterator = runLookUpTable.keySet().iterator();

		ArrayList<String> runIds = new ArrayList<String>();

		while (keyIterator.hasNext()) {
			String runId = keyIterator.next();
			runIds.add(runId);
		}

		return runIds;
	}

	public ArrayList<String> retrievePlanIds(JmxUser jmxUser) {

		// if (!JMXTLSServer.authorizeWeb(jmxUser)) {
		// return null;
		// }

		return retrievePlanIds();

	}

	private String retrieveMaxRunId() {

		HashMap<String, RunInfoType> runLookUpTable = TlosSpaceWide.getSpaceWideRegistry().getRunLookupTable();
		Iterator<String> keyIterator = runLookUpTable.keySet().iterator();
		String maxId = null;
		int maxRunId = -1;

		while (keyIterator.hasNext()) {
			String runId = keyIterator.next();
			int tmpMaxRunId = Integer.parseInt(runId);
			if (tmpMaxRunId > maxRunId) {
				maxRunId = tmpMaxRunId;
				maxId = tmpMaxRunId + "";
			}
		}

		return maxId;
	}

	public String retrieveMaxRunId(JmxUser jmxUser) {

		if (!JMXTLSServer.authorizeWeb(jmxUser)) {
			return null;
		}

		return retrieveMaxRunId();

	}

	@Override
	public int getNbChanges() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getState() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setState(String s) {
		// TODO Auto-generated method stub

	}

	@SuppressWarnings("rawtypes")
	public ArrayList<String> retrieveViewFiles(JmxUser jmxUser) {

		ArrayList<String> appenderList = new ArrayList<String>();

		for (Enumeration appenders = Logger.getLogger("com.likya.tlos").getAllAppenders(); appenders.hasMoreElements();) {
			Object o = appenders.nextElement();
			if (o instanceof FileAppender) {
				FileAppender newName = (FileAppender) o;
				appenderList.add(newName.getFile());
			}
		}

		for (Enumeration appenders = Logger.getRootLogger().getAllAppenders(); appenders.hasMoreElements();) {
			Object o = appenders.nextElement();
			if (o instanceof FileAppender) {
				FileAppender newName = (FileAppender) o;
				appenderList.add(newName.getFile());
			}
		}

		return appenderList;
	}

	public TreeInfoType retrieveTreeInfo(JmxUser jmxUser, String runId, ArrayList<String> scenariodIdList) {
		if (!JMXTLSServer.authorizeWeb(jmxUser)) {
			return null;
		}

		TreeInfoType treeInfoType = new TreeInfoType();
		HashMap<String, ScenarioStatus> scenarioList = new HashMap<String, ScenarioStatus>();

		HashMap<String, SpcInfoType> spcLookUpTable = TlosSpaceWide.getSpaceWideRegistry().getRunLookupTable().get(runId).getSpcLookupTable().getTable();
		Iterator<String> keyIterator = spcLookUpTable.keySet().iterator();

		while (keyIterator.hasNext()) {

			ScenarioStatus scenarioStatus = new ScenarioStatus();

			String scenarioId = keyIterator.next();
			SpcInfoType spcInfoType = spcLookUpTable.get(scenarioId);

			if ((scenariodIdList != null) && scenariodIdList.indexOf(spcInfoType.getSpcReferance().getSpcAbsolutePath()) > 0) {

				ArrayList<JobStatusSummary> jobList = new ArrayList<JobStatusSummary>();

				Iterator<SortType> jobQueueIndexIterator = spcInfoType.getSpcReferance().getJobQueueIndex().iterator();
				while (jobQueueIndexIterator.hasNext()) {
					SortType sortType = jobQueueIndexIterator.next();
					Job scheduledJob = spcInfoType.getSpcReferance().getJobQueue().get(sortType.getJobId());
					JobStatusSummary jobStatus = new JobStatusSummary();
					jobStatus.setJobId(sortType.getJobId() + "");
					// TODO Serkan : Burası kontrol edilecek !!!!!!
					// jobStatus.setJobStatus(scheduledJob.getJobRuntimeProperties().getJobProperties().getLiveStateInfo().getSubstateName().intValue());

					// LiveStateInfo listesindeki ilk eleman alinarak islem yapildi, yani guncel state i alindi
					jobStatus.setJobStatus(scheduledJob.getJobRuntimeProperties().getJobProperties().getStateInfos().getLiveStateInfos().getLiveStateInfoArray(0).getSubstateName().intValue());
					jobList.add(jobStatus);
				}

				scenarioStatus.setJobList(jobList);
			}

			scenarioStatus.setScenarioStatus(JobQueueOperations.isJobQueueOver(spcInfoType.getSpcReferance().getJobQueue()).isJobQueueOver());
			scenarioList.put(scenarioId, scenarioStatus);

		}

		treeInfoType.setScenarioStatusList(scenarioList);

		return treeInfoType;
	}

	public SpcInfoTypeClient retrieveSpcInfo(JmxUser jmxUser, String treePath) {

		if (!JMXTLSServer.authorizeWeb(jmxUser)) {
			return null;
		}

		SpcInfoType spcInfoType = null;

		if (isTester(jmxUser)) {
			spcInfoType = TlosSpaceWide.getSpaceWideRegistry().getCpcTesterReference().getSpcLookupTable(jmxUser.getId() + "").getTable().get(treePath);
		} else {
			spcInfoType = RunMapHelper.findSpc(treePath, TlosSpaceWide.getSpaceWideRegistry().getRunLookupTable());
		}

		TlosSWPathType scenarioId = spcInfoType.getSpcId();

		SpcInfoTypeClient spcInfoTypeClient = new SpcInfoTypeClient();
		spcInfoTypeClient.setSpcId(scenarioId.getFullPath());

		if (spcInfoType.getSpcReferance() != null) {
			String runId = spcInfoType.getSpcReferance().getManagement().getConcurrencyManagement().getRunningId();
			if (scenarioId.equals(CpcUtils.getRootScenarioPath(runId))) {
				spcInfoTypeClient.setJsName(scenarioId.getFullPath());
			} else {
				spcInfoTypeClient.setJsName(spcInfoType.getSpcReferance().getBaseScenarioInfos().getJsName());
			}

			spcInfoTypeClient.setNumOfJobs(JobQueueOperations.getNumOfJobs(spcInfoType.getSpcReferance().getJobQueue()));
			spcInfoTypeClient.setNumOfActiveJobs(JobQueueOperations.getNumOfActiveJobs(spcInfoType.getSpcReferance().getJobQueue()));

			spcInfoTypeClient.setPausable(spcInfoType.getSpcReferance().isPausable());
			spcInfoTypeClient.setResumable(spcInfoType.getSpcReferance().isResumable());
			spcInfoTypeClient.setStopable(spcInfoType.getSpcReferance().isStopable());
			spcInfoTypeClient.setStartable(spcInfoType.getSpcReferance().isStartable());
		}

		return spcInfoTypeClient;
	}

	public Object retrieveGlobalStates(JmxAgentUser jmxAgentUser) {
		if (!JMXTLSServer.authorizeWeb(jmxAgentUser)) {
			return false;
		}

		String globalStateDefinitionXML = XmlUtils.getGlobalStateDefinitionsXML(jmxAgentUser);

		return globalStateDefinitionXML;
	}

	@Override
	public Object runningJobs() {
		String jobList = "<jobList>";

		for (String runId : TlosSpaceWide.getSpaceWideRegistry().getRunLookupTable().keySet()) {

			RunInfoType runInfoType = TlosSpaceWide.getSpaceWideRegistry().getRunLookupTable().get(runId);
			HashMap<String, SpcInfoType> spcLookupTable = runInfoType.getSpcLookupTable().getTable();

			for (String spcId : spcLookupTable.keySet()) {
				Spc spc = spcLookupTable.get(spcId).getSpcReferance();
				Iterator<Job> jobsIterator = spc.getJobQueue().values().iterator();

				while (jobsIterator.hasNext()) {
					Job job = jobsIterator.next();
					jobList = jobList + job.getJobRuntimeProperties().getJobProperties().toString();
				}

			}
		}

		jobList += "</jobList>";
		return jobList.toString();
	}

	/**
	 * web ekranindaki kaynak listesinin oldugu agac render edilmeden once bu metodu cagirip guncel kaynak bilgilerini aliyor
	 */
	public TlosSWResourceNode getLiveResourceTreeInfo(JmxUser jmxUser, TlosSWResourceNode requestResourceNode) {
		if (!JMXTLSServer.authorizeWeb(jmxUser)) {
			return null;
		}

		TlosSWResourceNode responseResourceNode = new TlosSWResourceNode();

		ResourceListNode resourceListNode = new ResourceListNode();

		HashMap<String, ResourceNode> resourceList = AgentOperations.getResources(TlosSpaceWide.getSpaceWideRegistry().getAgentManagerReference().getSwAgentsCache());

		// ekranda ilgili makinenin dugumu acilmissa yani altindaki kisimlar aciktaysa buraya giriyor, yoksa icinde makine gelmedigi icin girmiyor
		// bunun amaci acilan dugumlerin agent bilgilerini de tlosSpaceWideServerNode'daki kaynak listesindeki ilgili kaynaklara eklemek

		Iterator<ResourceNode> resourceIterator = requestResourceNode.getResourceListNode().getResourceNodes().iterator();

		while (resourceIterator.hasNext()) {

			ResourceNode currentResourceNode = resourceIterator.next();
			String resourceName = currentResourceNode.getResourceInfoTypeClient().getResourceName();

			if (resourceList.containsKey(resourceName)) {

				ResourceNode tmpResourceNode = resourceList.get(resourceName);

				// makinenin altindaki tum agentlarin (hem tlos hem monitor) datasini AgentLookUpTableTypeClient nesnesi icine atiyor
				AgentLookUpTableTypeClient agentLookUpTableTypeClient = retrieveAgentLookupTable(resourceName);

				// tlos agentlarini aliyor
				HashMap<Integer, TlosAgentInfoTypeClient> tlosAgentInfoTypeClientList = agentLookUpTableTypeClient.getTAgentInfoTypeClientList();

				// Her bir tlosAgentNodu da makinenin TlosAgentNodes listesine atiyor
				for (Integer agentId : tlosAgentInfoTypeClientList.keySet()) {

					TlosAgentInfoTypeClient tlosAgentInfoTypeClient = tlosAgentInfoTypeClientList.get(agentId);

					TlosAgentNode tlosAgentNode = new TlosAgentNode();
					tlosAgentNode.setLabelText("Agent_" + agentId);
					tlosAgentNode.setAvailable(tlosAgentInfoTypeClient.isOutJmxAvailable());
					tlosAgentNode.setTlosAgentInfoTypeClient(tlosAgentInfoTypeClient);

					//
					if (currentResourceNode.getTlosAgentNodes().containsKey(agentId)) {
						tlosAgentNode.getJobInfoTypeClientList().addAll(getAgentsJobList(jmxUser, agentId, false));
					}

					tmpResourceNode.getTlosAgentNodes().put(agentId, tlosAgentNode);
				}

				// makinedeki monitor agent kullanilir durumdaysa ozelliklerini set ediyor
				if (agentLookUpTableTypeClient.getNAgentInfoTypeClient().isNrpeAvailable()) {
					MonitorAgentNode monitorAgentNode = new MonitorAgentNode();
					monitorAgentNode.setMonitorAgentInfoTypeClient(agentLookUpTableTypeClient.getNAgentInfoTypeClient());

					tmpResourceNode.setMonitorAgentNode(monitorAgentNode);
				}

			} else {
				// TODO Ekranlardan gelen ile motorda bulunan kaynak listesi arasında veri tutarsızlığı var ??????
				System.out.println("Ekranlardan gelen ile motorda bulunan kaynak listesi arasında veri tutarsızlığı var ??????");
			}
		}

		resourceListNode.setResourceNodes(new ArrayList<ResourceNode>(resourceList.values()));

		// iclerinde sadece makine bilgileri olan ama agent bilgileri olmayan resourceListNode, tlosSpaceWideServerNode'un kaynak listesine set ediliyor
		responseResourceNode.setResourceListNode(resourceListNode);

		return responseResourceNode;
	}

	/**
	 * web ekranindaki kaynak listesinin oldugu agac render edilmeden once bu metodu cagirip guncel kaynak bilgilerini aliyor
	 */
	public TlosSWResourceNode getLiveResourceTreeInfoEski(JmxUser jmxUser, TlosSWResourceNode tlosSWResourceNode) {
		if (!JMXTLSServer.authorizeWeb(jmxUser)) {
			return null;
		}

		TlosSWResourceNode tlosSpaceWideServerNode = new TlosSWResourceNode();

		ResourceListNode resourceListNode = new ResourceListNode();

		// tanimli tum agentlar taranip kaynak listesi cikartiliyor
		for (String agentId : TlosSpaceWide.getSpaceWideRegistry().getAgentManagerReference().getSwAgentsCache().keySet()) {

			int i;

			// kaynak listesi taranip ayni isimli bir makine var mi diye bakiliyor
			for (i = 0; i < resourceListNode.getResourceNodes().size(); i++) {
				String resourceName = resourceListNode.getResourceNodes().get(i).getResourceInfoTypeClient().getResourceName();

				if (resourceName.equals((TlosSpaceWide.getSpaceWideRegistry().getAgentManagerReference().getSwAgentsCache().get(agentId)).getResource().getStringValue())) {

					if ((TlosSpaceWide.getSpaceWideRegistry().getAgentManagerReference().getSwAgentsCache().get(agentId).getAgentType().toString()).equals(SERVER_TYPE)) {
						resourceListNode.getResourceNodes().get(i).getResourceInfoTypeClient().setIncludesServer(true);
					}
					break;
				}
			}

			// makine daha once tanimlanmamissa burada tanimlaniyor
			if (i == resourceListNode.getResourceNodes().size()) {
				ResourceNode resourceNode = setNewResourceParameters(TlosSpaceWide.getSpaceWideRegistry().getAgentManagerReference().getSwAgentsCache().get(agentId));

				resourceListNode.getResourceNodes().add(resourceNode);
			}
		}

		// iclerinde sadece makine bilgileri olan ama agent bilgileri olmayan resourceListNode, tlosSpaceWideServerNode'un kaynak listesine set ediliyor
		tlosSpaceWideServerNode.setResourceListNode(resourceListNode);

		// ekranda ilgili makinenin dugumu acilmissa yani altindaki kisimlar aciktaysa buraya giriyor, yoksa icinde makine gelmedigi icin girmiyor
		// bunun amaci acilan dugumlerin agent bilgilerini de tlosSpaceWideServerNode'daki kaynak listesindeki ilgili kaynaklara eklemek
		for (int i = 0; i < tlosSWResourceNode.getResourceListNode().getResourceNodes().size(); i++) {

			String resourceName = tlosSWResourceNode.getResourceListNode().getResourceNodes().get(i).getResourceInfoTypeClient().getResourceName();

			ResourceNode currentServerResource = new ResourceNode();

			for (int j = 0; j < tlosSpaceWideServerNode.getResourceListNode().getResourceNodes().size(); j++) {
				if (tlosSpaceWideServerNode.getResourceListNode().getResourceNodes().get(j).getResourceInfoTypeClient().getResourceName().equals(resourceName)) {
					currentServerResource = tlosSpaceWideServerNode.getResourceListNode().getResourceNodes().get(j);
					break;
				}
			}

			// makinenin altindaki tum agentlarin (hem tlos hem monitor) datasini AgentLookUpTableTypeClient nesnesi icine atiyor
			AgentLookUpTableTypeClient agentLookUpTableTypeClient = retrieveAgentLookupTable(resourceName);

			// tlos agentlarini aliyor
			HashMap<Integer, TlosAgentInfoTypeClient> tlosAgentInfoTypeClientList = agentLookUpTableTypeClient.getTAgentInfoTypeClientList();

			// Her bir tlosAgentNodu da makinenin TlosAgentNodes listesine atiyor
			for (Integer agentId : tlosAgentInfoTypeClientList.keySet()) {

				TlosAgentInfoTypeClient tlosAgentInfoTypeClient = tlosAgentInfoTypeClientList.get(agentId);

				TlosAgentNode tlosAgentNode = new TlosAgentNode();
				tlosAgentNode.setTlosAgentInfoTypeClient(tlosAgentInfoTypeClient);

				currentServerResource.getTlosAgentNodes().put(agentId, tlosAgentNode);
			}

			// makinedeki monitor agent kullanilir durumdaysa ozelliklerini set ediyor
			if (agentLookUpTableTypeClient.getNAgentInfoTypeClient().isNrpeAvailable()) {
				MonitorAgentNode monitorAgentNode = new MonitorAgentNode();
				monitorAgentNode.setMonitorAgentInfoTypeClient(agentLookUpTableTypeClient.getNAgentInfoTypeClient());

				currentServerResource.setMonitorAgentNode(monitorAgentNode);
			}

			// tlosSpaceWideServerNode icindeki kaynak listesindeki ilgili kaynagin altina agentlari set ediliyor
			for (int j = 0; j < tlosSpaceWideServerNode.getResourceListNode().getResourceNodes().size(); j++) {
				if (tlosSpaceWideServerNode.getResourceListNode().getResourceNodes().get(j).getResourceInfoTypeClient().getResourceName().equals(resourceName)) {
					tlosSpaceWideServerNode.getResourceListNode().getResourceNodes().get(j).setTlosAgentNodes(currentServerResource.getTlosAgentNodes());
					tlosSpaceWideServerNode.getResourceListNode().getResourceNodes().get(j).setMonitorAgentNode(currentServerResource.getMonitorAgentNode());
					break;
				}
			}
		}

		return tlosSpaceWideServerNode;
	}

	// verilen makine adina gore SpaceWideRegistry'deki agentlari alip datalarini AgentLookUpTableTypeClient nesnesine atiyor
	private AgentLookUpTableTypeClient retrieveAgentLookupTable(String resourceName) {

		AgentLookUpTableTypeClient agentLookUpTableTypeClient = new AgentLookUpTableTypeClient();

		HashMap<String, SWAgent> tAgentLookUpTable = TlosSpaceWide.getSpaceWideRegistry().getAgentManagerReference().getSwAgentsCache();
		Iterator<String> keyIterator = tAgentLookUpTable.keySet().iterator();

		// registry'deki tum agentlari tariyor
		while (keyIterator.hasNext()) {

			String tAgentId = keyIterator.next();

			SWAgent tAgent = tAgentLookUpTable.get(tAgentId);

			// verilen kaynakta tanimli agentlarla ilgileniyor
			if (tAgent.getResource().getStringValue().equals(resourceName)) {
				TlosAgentInfoTypeClient tlosAgentInfoTypeClient = fillTlosAgentInfoData(tAgent);

				agentLookUpTableTypeClient.getTAgentInfoTypeClientList().put(tAgent.getId(), tlosAgentInfoTypeClient);

				// nrpe agent calisir durumdaysa parametreleri set ediliyor
				if (tAgent.getNrpeAvailable()) {
					MonitorAgentInfoTypeClient nagiosAgentInfoTypeClient = new MonitorAgentInfoTypeClient();
					nagiosAgentInfoTypeClient.setNrpeAvailable(true);
					nagiosAgentInfoTypeClient.setNrpePort(tAgent.getNrpePort());
					nagiosAgentInfoTypeClient.setResourceName(tAgent.getResource().getStringValue());

					agentLookUpTableTypeClient.setNAgentInfoTypeClient(nagiosAgentInfoTypeClient);
				}
			}
		}
		return agentLookUpTableTypeClient;
	}

	// Yeni makine tanimi yapiliyor
	private ResourceNode setNewResourceParameters(SWAgent swAgent) {
		ResourceNode resourceNode = new ResourceNode();

		ResourceInfoTypeClient resourceInfoTypeClient = new ResourceInfoTypeClient();
		resourceInfoTypeClient.setResourceName(swAgent.getResource().getStringValue());
		resourceInfoTypeClient.setOsType(swAgent.getOsType().toString());

		if (swAgent.getAgentType().equals(SERVER_TYPE)) {
			resourceInfoTypeClient.setIncludesServer(true);
		}

		// kaynagin aktif olup olmadigini yani icerisinde herhangi bir agent ya da sunucu calisip calismadigi ile ilgili parametreyi set etmek icin
		// burada agentlara bakip hepsinin calisip calismadigini kontrol ediyor
		boolean hasAvailableAgent = false;

		HashMap<String, SWAgent> tAgentLookUpTable = TlosSpaceWide.getSpaceWideRegistry().getAgentManagerReference().getSwAgentsCache();
		Iterator<String> keyIterator = tAgentLookUpTable.keySet().iterator();

		// registry'deki tum agentlari tariyor
		while (keyIterator.hasNext()) {
			String tAgentId = keyIterator.next();

			SWAgent tAgent = tAgentLookUpTable.get(tAgentId);

			// verilen kaynakta tanimli agentlarla ilgileniyor
			if (tAgent.getResource().getStringValue().equals(resourceInfoTypeClient.getResourceName())) {

				if (tAgent.getOutJmxAvailable()) {
					hasAvailableAgent = true;
					break;
				}
			}
		}

		if (hasAvailableAgent) {
			resourceInfoTypeClient.setActive(true);
		} else {
			resourceInfoTypeClient.setActive(false);
		}

		resourceNode.setResourceInfoTypeClient(resourceInfoTypeClient);

		return resourceNode;
	}

	public TlosAgentInfoTypeClient fillTlosAgentInfoData(SWAgent agent) {

		TlosAgentInfoTypeClient tlosAgentInfoTypeClient = new TlosAgentInfoTypeClient();
		tlosAgentInfoTypeClient.setAgentId(agent.getId());
		tlosAgentInfoTypeClient.setAgentType(agent.getAgentType().toString());
		tlosAgentInfoTypeClient.setJmxPort(agent.getJmxTlsPort());
		tlosAgentInfoTypeClient.setInJmxAvailable(agent.getInJmxAvailable());
		tlosAgentInfoTypeClient.setOutJmxAvailable(agent.getOutJmxAvailable());
		tlosAgentInfoTypeClient.setJmxAvailable(agent.getJmxAvailable());
		tlosAgentInfoTypeClient.setUserStopRequest(agent.getUserStopRequest().toString());

		// agent devrede ise devre disina alma parametreleri true set ediliyor
		if (tlosAgentInfoTypeClient.getUserStopRequest().equals(UserStopRequest.NULL.toString())) {
			tlosAgentInfoTypeClient.setActivate(false);
			tlosAgentInfoTypeClient.setForcedDeactivate(true);
			tlosAgentInfoTypeClient.setNormalDeactivate(true);
		} else {
			tlosAgentInfoTypeClient.setActivate(true);
			tlosAgentInfoTypeClient.setForcedDeactivate(false);
			tlosAgentInfoTypeClient.setNormalDeactivate(false);
		}

		return tlosAgentInfoTypeClient;
	}

	// Web ekranindaki kaynak listesi agacinda herhangi bir Tlos Agent secildiginde buraya geliyor, sunucu da o Tlos Agent'in bilgilerini donuyor
	public TlosAgentInfoTypeClient retrieveTlosAgentInfo(JmxUser jmxUser, int tlosAgentId) {

		if (!JMXTLSServer.authorizeWeb(jmxUser)) {
			return null;
		}

		SWAgent agent = TlosSpaceWide.getSpaceWideRegistry().getAgentManagerReference().getSwAgentsCache().get(tlosAgentId + "");

		TlosAgentInfoTypeClient tlosAgentInfoTypeClient = fillTlosAgentInfoData(agent);

		return tlosAgentInfoTypeClient;
	}

	// Web ekranindaki kaynak listesi agacinda herhangi bir Tlos Agent secildiginde buraya geliyor, sunucu da o Tlos Agent'ta calisan joblari donuyor
	public ArrayList<JobInfoTypeClient> getAgentsJobList(JmxUser jmxUser, int tlosAgentId, Boolean transformToLocalTime) {

		if (!JMXTLSServer.authorizeWeb(jmxUser)) {
			return null;
		}

		ArrayList<Job> jobList = new ArrayList<Job>();

		// butun run'lara bakip, calisan tum senaryolari tariyor
		for (String runId : TlosSpaceWide.getSpaceWideRegistry().getRunLookupTable().keySet()) {

			RunInfoType runInfoType = TlosSpaceWide.getSpaceWideRegistry().getRunLookupTable().get(runId);
			HashMap<String, SpcInfoType> spcLookupTable = runInfoType.getSpcLookupTable().getTable();

			for (String spcId : spcLookupTable.keySet()) {
				Spc spc = spcLookupTable.get(spcId).getSpcReferance();

				if (spc == null) {
					// No jobs in scenario, but scenarios
					continue;
				}

				// verilen agent id'ye gore o agentta calisan, o senaryo icindeki isleri getiriyor
				ArrayList<Job> jobListTemp = spc.getJobListForAgent(tlosAgentId);

				// gelen isleri job listesine ekliyor
				for (int i = 0; i < jobListTemp.size(); i++) {
					Job tmpJob = jobListTemp.get(i);
					tmpJob.getJobRuntimeProperties().setCurrentRunId(spc.getCurrentRunId());
					tmpJob.getJobRuntimeProperties().setNativeFullJobPath(spc.getSpcFullPath());
					jobList.add(tmpJob);
				}
			}
		}

		ArrayList<JobInfoTypeClient> jobInfoTypeClientList = new ArrayList<JobInfoTypeClient>();

		for (int i = 0; i < jobList.size(); i++) {

			Job scheduledJob = jobList.get(i);

			JobRuntimeProperties jobRuntimeProperties = scheduledJob.getJobRuntimeProperties();
			JobInfoTypeClient jobInfoTypeClient = new JobInfoTypeClient();

			jobInfoTypeClient.setJobId(jobRuntimeProperties.getJobProperties().getID());
			jobInfoTypeClient.setJobName(jobRuntimeProperties.getJobProperties().getBaseJobInfos().getJsName());
			// jobInfoTypeClient.setJobKey(jobRuntimeProperties.getJobProperties().getID());
			jobInfoTypeClient.setJobCommand(jobRuntimeProperties.getJobProperties().getBaseJobInfos().getJobTypeDetails().getJobCommand());
			jobInfoTypeClient.setJobCommandType(jobRuntimeProperties.getJobProperties().getBaseJobInfos().getJobTypeDetails().getJobCommandType().toString());
			jobInfoTypeClient.setTreePath(jobRuntimeProperties.getAbsoluteJobPath());
			jobInfoTypeClient.setCurrentRunId(jobRuntimeProperties.getCurrentRunId());
			jobInfoTypeClient.setNativeRunId(jobRuntimeProperties.getJobProperties().getRunId());
			jobInfoTypeClient.setJobPath(jobRuntimeProperties.getJobProperties().getBaseJobInfos().getJobTypeDetails().getJobPath());
			jobInfoTypeClient.setJobLogPath(jobRuntimeProperties.getJobProperties().getBaseJobInfos().getJobLogPath());
			jobInfoTypeClient.setJobLogName(jobRuntimeProperties.getJobProperties().getBaseJobInfos().getJobLogFile());
			jobInfoTypeClient.setoSystem(jobRuntimeProperties.getJobProperties().getBaseJobInfos().getOSystem().toString());
			jobInfoTypeClient.setFullPath(jobRuntimeProperties.getNativeFullJobPath().getFullPath());
			jobInfoTypeClient.setRelativePath(jobRuntimeProperties.getNativeFullJobPath().getAbsolutePath());
			
			// TODO Gecici olarak tip donusumu yaptim.
			jobInfoTypeClient.setJobPriority(jobRuntimeProperties.getJobProperties().getBaseJobInfos().getJobPriority().intValue());

			TimeManagement timeManagement = jobRuntimeProperties.getJobProperties().getManagement().getTimeManagement();
			TimeControl timeControl = jobRuntimeProperties.getJobProperties().getManagement().getTimeControl();
			
			jobInfoTypeClient.setJobPlanTime(DateUtils.jobTimeToString(timeManagement.getJsPlannedTime().getStartTime().getTime(), transformToLocalTime));
			jobInfoTypeClient.setJobTimeOut(timeControl.getJsTimeOut().toString());

			// agentlarda calisan joblarin PlannedExecutionDate, CompletionDate ve WorkDuration alanlari set edilmediginden onlari jobRealTime kismindan set ediyoruz
			if (jobRuntimeProperties.getPlannedExecutionDate() == null && (timeManagement.getJsRealTime() != null)) {
				jobInfoTypeClient.setPlannedExecutionDate(DateUtils.jobRealTimeToString(timeManagement.getJsRealTime(), true, transformToLocalTime));

				// is hala calisiyorsa
				if (timeManagement.getJsRealTime().getStopTime() != null) {
					jobInfoTypeClient.setCompletionDate(DateUtils.jobRealTimeToString(timeManagement.getJsRealTime(), false, transformToLocalTime));
				}

			} else if (jobRuntimeProperties.getPlannedExecutionDate() != null) {
				jobInfoTypeClient.setPlannedExecutionDate(DateUtils.calendarToString(jobRuntimeProperties.getPlannedExecutionDate(), false));

				if (jobRuntimeProperties.getCompletionDate() != null) {
					jobInfoTypeClient.setCompletionDate(DateUtils.calendarToString(jobRuntimeProperties.getCompletionDate(), false));
				}
			}

			jobInfoTypeClient.setWorkDuration(DateUtils.getJobWorkDuration(timeManagement.getJsRealTime(), false));

			// LiveStateInfo listesindeki ilk eleman alinarak islem yapildi, yani guncel state i alindi
			jobInfoTypeClient.setOver(jobRuntimeProperties.getJobProperties().getStateInfos().getLiveStateInfos().getLiveStateInfoArray(0).getStateName().equals(StateName.FINISHED));
			jobInfoTypeClient.setLiveStateInfo(jobRuntimeProperties.getJobProperties().getStateInfos().getLiveStateInfos().getLiveStateInfoArray(0));

			// TODO geçici olarak dönüşüm yaptım ama xsd de problem var ????
			jobInfoTypeClient.setSafeRestart(jobRuntimeProperties.getJobProperties().getManagement().getCascadingConditions().getJobSafeToRestart());

			jobInfoTypeClient.setRetriable(jobRuntimeProperties.isRetriable());
			jobInfoTypeClient.setSuccessable(jobRuntimeProperties.isSuccessable());
			jobInfoTypeClient.setSkippable(jobRuntimeProperties.isSkippable());
			jobInfoTypeClient.setStopable(jobRuntimeProperties.isStopable());
			jobInfoTypeClient.setPausable(jobRuntimeProperties.isPausable());
			jobInfoTypeClient.setResumable(jobRuntimeProperties.isResumable());
			jobInfoTypeClient.setStartable(jobRuntimeProperties.isStartable());

			if (jobRuntimeProperties.getJobProperties().getDependencyList() != null) {
				ArrayList<Item> dependencyList = new ArrayList<Item>(Arrays.asList(jobRuntimeProperties.getJobProperties().getDependencyList().getItemArray()));
				Iterator<Item> dependencyListIterator = dependencyList.iterator();
				ArrayList<String> depenArrayList = new ArrayList<String>();
				while (dependencyListIterator.hasNext()) {
					depenArrayList.add(dependencyListIterator.next().getJsId());
				}
				jobInfoTypeClient.setJobDependencyList(depenArrayList);
			}

			jobInfoTypeClient.setAgentId(jobRuntimeProperties.getJobProperties().getAgentId());

			if (jobInfoTypeClient.getAgentId() > 0) {
				SWAgent agent = TlosSpaceWide.getSpaceWideRegistry().getAgentManagerReference().getSwAgentsCache().get(jobInfoTypeClient.getAgentId() + "");

				jobInfoTypeClient.setResourceName(agent.getResource().getStringValue());
			}

			jobInfoTypeClientList.add(jobInfoTypeClient);
		}

		return jobInfoTypeClientList;
	}

	@Override
	public ArrayList<SWAgent> getAgentList(JmxUser jmxUser) {

		if (!JMXTLSServer.authorizeWeb(jmxUser)) {
			return null;
		}

		ArrayList<SWAgent> agentList = new ArrayList<SWAgent>();

		for (SWAgent agent : TlosSpaceWide.getSpaceWideRegistry().getAgentManagerReference().getSwAgentsCache().values()) {
			agentList.add(agent);
		}

		return agentList;
	}

	@Override
	public boolean runningInstanceExists(JmxUser jmxUser) {
		return RunUtils.runningPlanExists(TlosSpaceWide.getSpaceWideRegistry().getRunLookupTable());
	}

	@Override
	public boolean testDBConnection(JmxUser jmxUser, DbConnectionProfile dbConnectionProfile) {
		String dbPropertiesID = dbConnectionProfile.getDbDefinitionId() + "";

		DbProperties dbProperties = null;

		try {
			dbProperties = DBUtils.searchDBPropertiesById(Integer.parseInt(dbPropertiesID));
		} catch (XMLDBException e) {
			e.printStackTrace();
		}

		if (dbProperties.getDbType().equals(DbType.ORACLE)) {
			return initOracleDbConnection(dbProperties, dbConnectionProfile);

		} else if (dbProperties.getDbType().equals(DbType.POSTGRE_SQL)) {
			return initDbConnection(dbProperties, dbConnectionProfile);
		}

		return false;
	}

	private static boolean initDbConnection(DbProperties dbProperties, DbConnectionProfile dbConnectionProfile) {
		boolean result = false;

		String url = dbProperties.getDbUrl();
		url += dbProperties.getHostName() + "/" + dbProperties.getDbName();
		Properties props = new Properties();

		String userName = dbConnectionProfile.getUserName(); // "postgres"; // Connection profile dan alacak.
		String password = dbConnectionProfile.getUserPassword(); // "ad0215"; // Connection profile dan alacak.

		props.setProperty("user", userName);
		props.setProperty("password", password);

		Connection connection;
		try {
			connection = DriverManager.getConnection(url, props);

			result = true;

			connection.close();

		} catch (SQLException sqle) {
			sqle.printStackTrace();
		}

		return result;
	}

	private static boolean initOracleDbConnection(DbProperties dbProperties, DbConnectionProfile dbConnectionProfile) {
		boolean result = false;

		try {
			Class.forName("oracle.jdbc.OracleDriver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		String url = dbProperties.getDbUrl();
		url += "@//" + dbProperties.getHostName() + ":" + dbProperties.getListenerPortNumber() + "/" + dbProperties.getDbName();

		Properties props = new Properties();

		String userName = dbConnectionProfile.getUserName();
		String password = dbConnectionProfile.getUserPassword();

		props.setProperty("user", userName);
		props.setProperty("password", password);

		Connection connection;
		try {
			connection = DriverManager.getConnection(url, props);

			result = true;

			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return result;
	}

	// Web ekranindaki kaynak listesi agacinda kaynak ağacı secildiginde buraya geliyor
	public ArrayList<ResourceInfoTypeClient> getResourceInfoTypeClientList(JmxUser jmxUser) {

		if (!JMXTLSServer.authorizeWeb(jmxUser)) {
			return null;
		}

		ArrayList<ResourceInfoTypeClient> resourceInfoList = new ArrayList<ResourceInfoTypeClient>();

		HashMap<String, ResourceNode> resourceList = AgentOperations.getResources(TlosSpaceWide.getSpaceWideRegistry().getAgentManagerReference().getSwAgentsCache());

		for (ResourceNode resourceNode : resourceList.values()) {
			resourceInfoList.add(resourceNode.getResourceInfoTypeClient());
		}

		return resourceInfoList;
	}

	// Web ekranindaki kaynak listesi agacinda herhangi bir kaynak secildiginde buraya geliyor
	public ArrayList<TlosAgentInfoTypeClient> getTlosAgentInfoTypeClientList(JmxUser jmxUser, String resourceName) {

		if (!JMXTLSServer.authorizeWeb(jmxUser)) {
			return null;
		}

		ArrayList<TlosAgentInfoTypeClient> agentInfoList = new ArrayList<TlosAgentInfoTypeClient>();

		HashMap<String, SWAgent> agentList = TlosSpaceWide.getSpaceWideRegistry().getAgentManagerReference().getSwAgentsCache();

		for (SWAgent agent : agentList.values()) {
			if (agent.getResource().getStringValue().equals(resourceName)) {
				agentInfoList.add(fillTlosAgentInfoData(agent));
			}
		}

		return agentInfoList;
	}
}
