package com.likya.tlossw.core.dss;

import java.util.Random;

import org.apache.log4j.Logger;

import com.likya.tlos.model.xmlbeans.agent.SWAgentDocument.SWAgent;
import com.likya.tlos.model.xmlbeans.common.AgentChoiceMethodDocument.AgentChoiceMethod;
import com.likya.tlos.model.xmlbeans.common.ChoiceType;
import com.likya.tlos.model.xmlbeans.data.JobPropertiesDocument.JobProperties;
import com.likya.tlos.model.xmlbeans.state.LiveStateInfoDocument.LiveStateInfo;
import com.likya.tlos.model.xmlbeans.state.StateNameDocument.StateName;
import com.likya.tlos.model.xmlbeans.state.StatusNameDocument.StatusName;
import com.likya.tlos.model.xmlbeans.state.SubstateNameDocument.SubstateName;
import com.likya.tlos.model.xmlbeans.swresourcenagentresults.ResourceAgentListDocument.ResourceAgentList;
import com.likya.tlos.model.xmlbeans.swresourcenagentresults.ResourceDocument.Resource;
import com.likya.tlos.model.xmlbeans.swresourcenagentresults.ResourceType;
import com.likya.tlossw.TlosSpaceWide;
import com.likya.tlossw.core.spc.jobs.Job;
import com.likya.tlossw.db.utils.DBUtils;
import com.likya.tlossw.db.utils.DssDbUtils;
import com.likya.tlossw.utils.DebugParams;
import com.likya.tlossw.utils.LiveStateInfoUtils;
import com.likya.tlossw.utils.ParsingUtils;
import com.likya.tlossw.utils.SpaceWideRegistry;
import com.likya.tlossw.utils.date.DateUtils;

public class DssVisionaire extends DssBase {

	private static Logger myLogger = Logger.getLogger(DssVisionaire.class);

	public static DssResult evaluateDss(Job job) {

		JobProperties jobProperties = job.getJobRuntimeProperties().getJobProperties();

		glbinfo("   > evaluateDss.getResource : BEGIN");
		long startTime = System.currentTimeMillis();
		DssResult dssResult = getResource(job);
		glbinfo("   > evaluateDss.getResource : END : " + DateUtils.dateDiffWithNow(startTime) + " ms");
		
		if(dssResult.getResultCode() < 0) {
			return dssResult;
		}
		
		Resource resource = dssResult.getResource();

		SWAgent agent = TlosSpaceWide.getSpaceWideRegistry().getAgentManagerReference().getSwAgentCache(resource.getAgentid());

		if (resource.enumValue().equals(ResourceType.TRUE) && agent.getInJmxAvailable()) {

			// Job in hangi agent da calisacagi belli oldu. Tanimdaki agentId ye atayalim.
			
			jobProperties.setAgentId(resource.getAgentid());
			job.getJobRuntimeProperties().getJobProperties().setLSIDateTime(DateUtils.getServerW3CDateTime());
			
			/* TRANSFERING state i ekle */
			job.insertNewLiveStateInfo(StateName.INT_PENDING, SubstateName.INT_READY, StatusName.INT_TRANSFERING);
			/*
			 * Bu noktada ise kaynak ayrildi, artik gercek olarak
			 * var. O yuzden DailyScenarios a yeni bir is olarak
			 * kaydini yapalim.
			 */
			myLogger.info("     > ID : " + jobProperties.getID() + ":" +  jobProperties.getBaseJobInfos().getJsName() + " DB ye insert ediliyor !");

			glbinfo("   > DBUtils.insertJob : BEGIN");
			startTime = System.currentTimeMillis();
			DBUtils.insertJob(jobProperties, ParsingUtils.getJobXPath(job.getJobRuntimeProperties().getNativeFullJobPath().getFullPath()));
			glbinfo("   > DBUtils.insertJob : END : " + DateUtils.dateDiffWithNow(startTime) + " ms");
			glbinfo("     > DB ye insert edildi !");
			return dssResult;

		}

		return new DssResult(-1, "Kaynak ataması yapılamadı : Kaynak : FALSE ya da Jmx is not available !");
	}

	private static DssResult getResource(Job job) {

		DssResult dssResult = null;

		JobProperties jobProperties = job.getJobRuntimeProperties().getJobProperties();
		LiveStateInfo liveStateInfo = jobProperties.getStateInfos().getLiveStateInfos().getLiveStateInfoArray(0);
		

		if (!(LiveStateInfoUtils.equalStates(liveStateInfo, StateName.PENDING, SubstateName.READY, StatusName.LOOKFOR_RESOURCE))) {
			/* LOOKFOR-RESOURCE state i ekle */
			job.insertNewLiveStateInfo(StateName.INT_PENDING, SubstateName.INT_READY, StatusName.INT_LOOKFOR_RESOURCE);
		}

		AgentChoiceMethod agentChoiceMethod = jobProperties.getAdvancedJobInfos().getAgentChoiceMethod();

		int resourceSelectionAlgorithmId = agentChoiceMethod.enumValue().intValue();

		switch (resourceSelectionAlgorithmId) {

		case ChoiceType.INT_USER_INTERACTION_PREFERENCE:

			long startTime = System.currentTimeMillis();
			dssResult = applyUserInteractionPreference(job);
			sysErr(agentChoiceMethod.enumValue().toString() + " : " + DateUtils.dateDiffWithNow(startTime) + "ms");
			break;

		case ChoiceType.INT_SIMPLE_METASCHEDULER:
			
			startTime = System.currentTimeMillis();
			dssResult = applySimpleMetaScheduler(job);
			sysErr(agentChoiceMethod.enumValue().toString() + " : " + DateUtils.dateDiffWithNow(startTime) + "ms");
			break;

		case ChoiceType.INT_USER_MANDATORY_PREFERENCE:

			startTime = System.currentTimeMillis();
			dssResult = applyUserMendatoryPreference(job, agentChoiceMethod.getAgentId());
			sysErr(agentChoiceMethod.enumValue().toString() + " : " + DateUtils.dateDiffWithNow(startTime) + "ms");
			break;

		case ChoiceType.INT_ADVANCED_METASCHEDULER:

			startTime = System.currentTimeMillis();
			dssResult = applyAdvancedMetaScheduler();
			sysErr(agentChoiceMethod.enumValue().toString() + " : " + DateUtils.dateDiffWithNow(startTime) + "ms");
			break;

		default:
			myLogger.info("     > Kaynak atamasi icin ek yontem gerekiyor.");
			break;
		}

		return dssResult;

	}

	private static DssResult applyUserInteractionPreference(Job job) {

		if (job.getSelectedAgentId() == null) {

			LiveStateInfo liveStateInfo = job.getJobRuntimeProperties().getJobProperties().getStateInfos().getLiveStateInfos().getLiveStateInfoArray(0);

			if (!LiveStateInfoUtils.equalStates(liveStateInfo, StateName.PENDING, SubstateName.READY, StatusName.USER_CHOOSE_RESOURCE)) {
				job.insertNewLiveStateInfo(StateName.INT_PENDING, SubstateName.INT_READY, StatusName.INT_USER_CHOOSE_RESOURCE);
				// myLogger.info("     > Kaynak atamasi uygun olan " + numberOfAvailableResources + " kaynak icinden kullanici secsin (UserInteractionPreference).");
			}

			return null;

		}

		DssResult myDssResult = checkAvailabilityConditions(job);

		if (myDssResult.getResultCode() < 0) {
			return myDssResult;
		}

		Resource myResource = find(job.getSelectedAgentId(), myDssResult.getResourceAgentList());

		if (myResource != null) {
			
			String logStr = "     > Kaynak atamasi uygun olan " + myDssResult.getResourceAgentList().sizeOfResourceArray() + " kaynak icinden kullanici secimi ile yapildi";
			logStr += "\n       >" + myResource + " kaynak olarak secildi.";
			myLogger.info(logStr);
			
			myDssResult = new DssResult(0, logStr, myDssResult.getResourceAgentList(), myResource);
			
		} else {
			
			String logStr = "     > Kaynak listesi taram sonucunda uygun kaynak bulunamadı !";
			myDssResult = new DssResult(-1, logStr, myDssResult.getResourceAgentList(), myResource);

		}

		return myDssResult;

	}

	private static DssResult applySimpleMetaScheduler(Job job) {

		DssResult myDssResult = checkAvailabilityConditions(job);

		if (myDssResult.getResultCode() < 0) {
			return myDssResult;
		}

		int numberOfAvailableResources = myDssResult.getResourceAgentList().sizeOfResourceArray();

		Random randomGenerator = new Random();

		int selectedResourceIndex = randomGenerator.nextInt(numberOfAvailableResources);

		Resource myResource = myDssResult.getResourceAgentList().getResourceArray(selectedResourceIndex);

		myLogger.info("     > Kaynak atamasi uygun olan " + numberOfAvailableResources + " kaynak icinden rastgele secimle yapildi (SimpleMetascheduler)");
		myLogger.info("       > " + selectedResourceIndex + " ile " + myResource + " kaynak olarak secildi.");

		myDssResult.setResource(myResource);

		return myDssResult;

	}

	private static DssResult applyUserMendatoryPreference(Job job, String agentId) {

		DssResult myDssResult = checkAvailabilityConditions(job);

		if (myDssResult.getResultCode() < 0) {
			return myDssResult;
		}

		myLogger.info("     > Kaynak atamasi kullanicinin sectigi kaynak olacak (UserMandatoryPreference).");

		Resource myResource = find(agentId, myDssResult.getResourceAgentList());

		if (myResource != null) {
			
			String logStr = "     > Kaynak atamasi uygun olan kullanici secimi ile yapildi. " + agentId;
			myLogger.info(logStr);
			
			myDssResult = new DssResult(0, logStr, myDssResult.getResourceAgentList(), myResource);
		
		} else {
			
			String logStr = "     > Kaynak listesi tarama sonucunda uygun kaynak bulunamadı !";
			myDssResult = new DssResult(-1, logStr, myDssResult.getResourceAgentList(), myResource);

		}

		return myDssResult;

	}

	private static DssResult applyAdvancedMetaScheduler() {

		String logStr = "     > Kaynak atamasi ileri teknikler kullanilarak yapilacak(AdvancedMetascheduler).";
		
		myLogger.info(logStr);

		return new DssResult(-1, logStr);
	}

	private static DssResult checkAvailabilityConditions(Job job) {

		JobProperties jobProperties = job.getJobRuntimeProperties().getJobProperties();
		String jobId = jobProperties.getID();

		ResourceAgentList resourceAgentList = job.getResourceAgentList();

		long expireTimeAmount = TlosSpaceWide.getSpaceWideRegistry().getTlosSWConfigInfo().getSettings().getAgentOptions().getResourceListDuration().getDurationValue();

		if (resourceAgentList == null || isResourceListExpired(resourceAgentList.getTime(), expireTimeAmount)) {
			long startTime = System.currentTimeMillis();
			resourceAgentList = DssDbUtils.swFindResourcesForAJob(jobProperties);
			sysErr("DssDbUtils.swFindResourcesForAJob : " + DateUtils.dateDiffWithNow(startTime) + "ms");
		}

		int numberOfResources = resourceAgentList.sizeOfResourceArray();

		if (numberOfResources == 0) {

			String logStr = "     > JOB " + jobId + " icin kaynak listesi boş olduğundan !! KAYNAK ATANAMADI !!";
			myLogger.info(logStr);

			return new DssResult(-1, logStr);

		} else {
			job.setResourceAgentList(resourceAgentList);
			dumpResourceAvailability(jobId, resourceAgentList);
		}

		/* Kaynak listesi, uygun olmayanlardan temizlensin */

		ResourceAgentList availableResources = getAvailableResources(jobId, resourceAgentList);

		if (availableResources.sizeOfResourceArray() == 0) {

			String logStr = "JOB:" + jobProperties.getID() + "---> Kaynaklar arasında uygun kaynak bulunamadığından !! KAYNAK YINE ATANAMADI !!";

			return new DssResult(-1, logStr);

		}

		return new DssResult(0, "Başarılı kaynak ataması !", availableResources);

	}

	public static void glbinfo(String message) {
		if(DebugParams.isIsdssdebug()) {
			SpaceWideRegistry.getGlobalLogger().info(message);
		}
	}
	
	public static void sysErr(String message) {
		if(DebugParams.isIsdssdebug()) {
			System.err.println(message);
		}
	}
}
