package com.likya.tlossw.utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import org.apache.commons.collections.iterators.ArrayIterator;
import org.apache.log4j.Logger;

import com.likya.tlos.model.xmlbeans.data.JobListDocument.JobList;
import com.likya.tlos.model.xmlbeans.data.JobPropertiesDocument.JobProperties;
import com.likya.tlos.model.xmlbeans.data.ScenarioDocument.Scenario;
import com.likya.tlos.model.xmlbeans.data.TlosProcessDataDocument.TlosProcessData;
import com.likya.tlos.model.xmlbeans.state.LiveStateInfoDocument.LiveStateInfo;
import com.likya.tlos.model.xmlbeans.state.StateNameDocument.StateName;
import com.likya.tlos.model.xmlbeans.state.StatusNameDocument.StatusName;
import com.likya.tlos.model.xmlbeans.state.SubstateNameDocument.SubstateName;
import com.likya.tlossw.TlosSpaceWide;
import com.likya.tlossw.core.cpc.CpcBase;
import com.likya.tlossw.core.cpc.model.SpcInfoType;
import com.likya.tlossw.core.spc.Spc;
import com.likya.tlossw.core.spc.helpers.JobQueueOperations;
import com.likya.tlossw.core.spc.model.JobRuntimeProperties;
import com.likya.tlossw.exceptions.TlosFatalException;
import com.likya.tlossw.model.SpcLookupTable;
import com.likya.tlossw.model.engine.EngineeConstants;
import com.likya.tlossw.model.path.BasePathType;
import com.likya.tlossw.model.path.TlosSWPathType;
import com.likya.tlossw.utils.validation.XMLValidations;

public class CpcUtils {

	public static Scenario getScenario(TlosProcessData tlosProcessData) {

		Scenario scenario = Scenario.Factory.newInstance();
		scenario.setJobList(tlosProcessData.getJobList());

		scenario.setScenarioArray(tlosProcessData.getScenarioArray());

		scenario.setBaseScenarioInfos(tlosProcessData.getBaseScenarioInfos());
		scenario.setDependencyList(tlosProcessData.getDependencyList());
		scenario.setScenarioStatusList(tlosProcessData.getScenarioStatusList());
		scenario.setAlarmPreference(tlosProcessData.getAlarmPreference());
		scenario.setManagement(tlosProcessData.getManagement());
		scenario.setAdvancedScenarioInfos(tlosProcessData.getAdvancedScenarioInfos());
		scenario.setLocalParameters(tlosProcessData.getLocalParameters());

		return scenario;
	}

	public static Scenario getScenario(TlosProcessData tlosProcessData, String runId) {

		Scenario scenario = CpcUtils.getScenario(tlosProcessData);

		scenario.getManagement().getConcurrencyManagement().setRunningId(runId);

		return scenario;
	}

	// public static Scenario getScenarioOrj(TlosProcessData tlosProcessData, String planId) {
	//
	// Scenario scenario = Scenario.Factory.newInstance();
	// scenario.setJobList(tlosProcessData.getJobList());
	//
	// scenario.setScenarioArray(tlosProcessData.getScenarioArray());
	//
	// tlosProcessData.getConcurrencyManagement().setPlanId(planId);
	//
	// scenario.setBaseScenarioInfos(tlosProcessData.getBaseScenarioInfos());
	// scenario.setDependencyList(tlosProcessData.getDependencyList());
	// scenario.setScenarioStatusList(tlosProcessData.getScenarioStatusList());
	// scenario.setAlarmPreference(tlosProcessData.getAlarmPreference());
	// scenario.setTimeManagement(tlosProcessData.getTimeManagement());
	// scenario.setAdvancedScenarioInfos(tlosProcessData.getAdvancedScenarioInfos());
	// scenario.setConcurrencyManagement(tlosProcessData.getConcurrencyManagement());
	// scenario.setLocalParameters(tlosProcessData.getLocalParameters());
	//
	// return scenario;
	// }

	public static Scenario getScenario(Spc spc) {

		Scenario scenario = Scenario.Factory.newInstance();

		scenario.setBaseScenarioInfos(spc.getBaseScenarioInfos());
		scenario.setDependencyList(spc.getDependencyList());
		scenario.setScenarioStatusList(spc.getScenarioStatusList());
		scenario.setAlarmPreference(spc.getAlarmPreference());
		scenario.setManagement(spc.getManagement());
		scenario.setAdvancedScenarioInfos(spc.getAdvancedScenarioInfos());
		scenario.setLocalParameters(spc.getLocalParameters());

		return scenario;
	}

	public static Scenario getScenario(Scenario tmpScenario) {

		Scenario scenario = Scenario.Factory.newInstance();

		scenario.setBaseScenarioInfos(tmpScenario.getBaseScenarioInfos());
		scenario.setDependencyList(tmpScenario.getDependencyList());
		scenario.setScenarioStatusList(tmpScenario.getScenarioStatusList());
		scenario.setAlarmPreference(tmpScenario.getAlarmPreference());
		scenario.setManagement(tmpScenario.getManagement());
		scenario.setAdvancedScenarioInfos(tmpScenario.getAdvancedScenarioInfos());
		scenario.setLocalParameters(tmpScenario.getLocalParameters());

		return scenario;

	}

	public static SpcInfoType getSpcInfo(Spc spc, String userId, String runId, Scenario tmpScenario) {

		LiveStateInfo myLiveStateInfo = LiveStateInfo.Factory.newInstance();

		myLiveStateInfo.setStateName(StateName.PENDING);
		myLiveStateInfo.setSubstateName(SubstateName.IDLED);
		myLiveStateInfo.setStatusName(StatusName.BYTIME);
		spc.setLiveStateInfo(myLiveStateInfo);

		Thread thread = new Thread(spc);

		spc.setExecuterThread(thread);

		spc.setJsName(tmpScenario.getBaseScenarioInfos().getJsName());
		spc.setConcurrent(tmpScenario.getManagement().getConcurrencyManagement().getConcurrent());
		spc.setComment(tmpScenario.getBaseScenarioInfos().getComment());
		spc.setUserId(userId);

		tmpScenario.getManagement().getConcurrencyManagement().setRunningId(runId);

		spc.setBaseScenarioInfos(tmpScenario.getBaseScenarioInfos());
		spc.setDependencyList(tmpScenario.getDependencyList());
		spc.setScenarioStatusList(tmpScenario.getScenarioStatusList());
		spc.setAlarmPreference(tmpScenario.getAlarmPreference());
		spc.setManagement(tmpScenario.getManagement());
		spc.setAdvancedScenarioInfos(tmpScenario.getAdvancedScenarioInfos());
		spc.setLocalParameters(tmpScenario.getLocalParameters());

		SpcInfoType spcInfoType = new SpcInfoType();

		spcInfoType.setJsName(spc.getBaseScenarioInfos().getJsName());
		spcInfoType.setConcurrent(spc.getManagement().getConcurrencyManagement().getConcurrent());
		spcInfoType.setComment(spc.getBaseScenarioInfos().getComment());
		spcInfoType.setUserId(userId);

		Scenario scenario = CpcUtils.getScenario(spc);

		spcInfoType.setScenario(scenario);
		spcInfoType.setSpcReferance(spc);

		return spcInfoType;
	}

	public static SpcInfoType getSpcInfo(String userId, String runId, Scenario tmpScenario) {

		SpcInfoType spcInfoType = new SpcInfoType();

		spcInfoType.setJsName(tmpScenario.getBaseScenarioInfos().getJsName());
		spcInfoType.setConcurrent(tmpScenario.getManagement().getConcurrencyManagement().getConcurrent());
		spcInfoType.setComment(tmpScenario.getBaseScenarioInfos().getComment());
		spcInfoType.setUserId(userId);

		Scenario scenario = CpcUtils.getScenario(tmpScenario);

		spcInfoType.setScenario(scenario);
		spcInfoType.setSpcReferance(null);

		return spcInfoType;
	}

	public static ArrayList<JobRuntimeProperties> transformJobList(JobList jobList, Logger myLogger) {

		myLogger.debug("start:transformJobList");

		ArrayList<JobRuntimeProperties> transformTable = new ArrayList<JobRuntimeProperties>();

		ArrayIterator jobListIterator = new ArrayIterator(jobList.getJobPropertiesArray());

		while (jobListIterator.hasNext()) {

			JobProperties jobProperties = (JobProperties) (jobListIterator.next());
			JobRuntimeProperties jobRuntimeProperties = new JobRuntimeProperties();

			/* IDLED state i ekle */
			LiveStateInfoUtils.insertNewLiveStateInfo(jobProperties, StateName.INT_PENDING, SubstateName.INT_IDLED, StatusName.INT_BYTIME);
			jobRuntimeProperties.setJobProperties(jobProperties);
			// TODO infoBusInfo Manager i bilgilendir.

			transformTable.add(jobRuntimeProperties);
		}

		myLogger.debug("end:transformJobList");

		return transformTable;
	}

	public static boolean validateJobList(JobList jobList, Logger myLogger) {

		XMLValidations.validateWithCode(jobList, myLogger);

		return true;
	}

	public static SpcInfoType prepareScenario(String runId, TlosSWPathType tlosSWPathType, Scenario myScenario, Logger myLogger) throws TlosFatalException {

		myLogger.info("");
		myLogger.info("  > Senaryo ismi : " + tlosSWPathType.getFullPath());

		JobList jobList = myScenario.getJobList();

		if (!validateJobList(jobList, myLogger)) {
			// TODO WAITING e nasil alacagiz?
			myLogger.info("     > is listesi validasyonunda problem oldugundan WAITING e alinarak problemin giderilmesi beklenmektedir.");
			myLogger.error("Cpc Scenario jobs validation failed, process state changed to WAITING !");

			return null; // 08.07.2013 Serkan
			// throw new TlosException("Cpc Job List validation failed, process state changed to WAITING !");
		}

		if (jobList.getJobPropertiesArray().length == 0 && myScenario.getScenarioArray().length == 0) {
			myLogger.error(tlosSWPathType.getFullPath() + " isimli senaryo bilgileri yüklenemedi ya da iş listesi bos geldi !");
			myLogger.error(tlosSWPathType.getFullPath() + " isimli senaryo için spc başlatılmıyor !");
			return null;
		}

		SpcInfoType spcInfoType = null;
		// TODO Henüz ayarlanmadı !
		String userId = null;

		if (jobList.getJobPropertiesArray().length == 0) {
			spcInfoType = CpcUtils.getSpcInfo(userId, runId, myScenario);
			spcInfoType.setSpcId(tlosSWPathType);
		} else {
			Spc spc = new Spc(tlosSWPathType.getRunId(), tlosSWPathType.getAbsolutePath(), TlosSpaceWide.getSpaceWideRegistry(), transformJobList(jobList, myLogger));

			spcInfoType = CpcUtils.getSpcInfo(spc, userId, runId, myScenario);
			spcInfoType.setSpcId(tlosSWPathType);

			if (!TlosSpaceWide.getSpaceWideRegistry().getServerConfig().getServerParams().getIsPersistent().getUse() || !JobQueueOperations.recoverJobQueue(spcInfoType.getSpcReferance().getSpcAbsolutePath(), spc.getJobQueue(), spc.getJobQueueIndex())) {
				if (!spc.initScenarioInfo()) {
					myLogger.warn(tlosSWPathType.getFullPath() + " isimli senaryo bilgileri yüklenemedi ya da iş listesi boş geldi !");
					Logger.getLogger(CpcBase.class).warn(" WARNING : " + tlosSWPathType.getFullPath() + " isimli senaryo bilgileri yüklenemedi ya da iş listesi boş geldi !");

					System.exit(-1);
				}
			}
		}

		return spcInfoType;

	}

	public static String getRunId(TlosProcessData tlosProcessData, boolean isTest, Logger myLogger) {

		String runId = null;

		if (isTest) {
			String userId = "" + tlosProcessData.getBaseScenarioInfos().getUserId();
			if (userId == null || userId.equals("")) {
				userId = "" + Calendar.getInstance().getTimeInMillis();
			}
			myLogger.info("   > InstanceID = " + userId + " olarak belirlenmistir.");
			runId = userId;
		} else {
			runId = tlosProcessData.getRunId();
			if (runId == null) {
				runId = "" + Calendar.getInstance().getTimeInMillis();
			}
			myLogger.info("   > InstanceID = " + runId + " olarak belirlenmiştir.");
		}

		return runId;
	}

	public static void startSpc(TlosSWPathType tlosSWPathType, Logger myLogger) {

		SpcLookupTable spcLookupTable = TlosSpaceWide.getSpaceWideRegistry().getRunLookupTable().get(tlosSWPathType.getRunId()).getSpcLookupTable();

		HashMap<String, SpcInfoType> table = spcLookupTable.getTable();

		SpcInfoType spcInfoType = table.get(tlosSWPathType.getFullPath());

		startSpc(spcInfoType, myLogger);

	}

	public static void startSpc(SpcInfoType spcInfoType, Logger myLogger) {
		/**
		 * Bu thread daha once calistirildi mi? Degilse thread i
		 * baslatabiliriz !!
		 **/
		Spc mySpc = spcInfoType.getSpcReferance();

		if (spcInfoType.isVirgin() && !mySpc.getExecuterThread().isAlive()) {

			spcInfoType.setVirgin(false); /* Artik baslattik */
			/** Statuleri set edelim **/
			mySpc.getLiveStateInfo().setStateName(StateName.RUNNING);
			mySpc.getLiveStateInfo().setSubstateName(SubstateName.STAGE_IN);

			myLogger.info("     > Senaryo " + mySpc.getSpcFullPath() + " aktive edildi !");

			mySpc.getExecuterThread().setName(mySpc.getCommonName());
			/** Senaryonun thread lerle calistirildigi yer !! **/
			mySpc.getExecuterThread().start();

			myLogger.info("     > OK");

		}
	}

	public static String getRootScenarioPath(String runId) {
		return getInstancePath(runId) + "." + EngineeConstants.LONELY_JOBS;
	}

	public static String getInstancePath(String runId) {
		return BasePathType.getRootPath() + "." + runId;
	}

}
