package com.likya.tlossw.core.cpc.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;

import com.likya.tlos.model.xmlbeans.data.JobPropertiesDocument.JobProperties;
import com.likya.tlos.model.xmlbeans.state.LiveStateInfoDocument.LiveStateInfo;
import com.likya.tlos.model.xmlbeans.state.StateNameDocument.StateName;
import com.likya.tlos.model.xmlbeans.state.StatusNameDocument.StatusName;
import com.likya.tlos.model.xmlbeans.state.SubstateNameDocument.SubstateName;
import com.likya.tlossw.core.cpc.model.SpcInfoType;
import com.likya.tlossw.core.spc.Spc;
import com.likya.tlossw.core.spc.helpers.JobQueueOperations;
import com.likya.tlossw.core.spc.helpers.SortType;
import com.likya.tlossw.core.spc.jobs.Job;
import com.likya.tlossw.model.JobQueueResult;
import com.likya.tlossw.model.path.TlosSWPathType;
import com.likya.tlossw.utils.JobIndexUtils;
import com.likya.tlossw.utils.LiveStateInfoUtils;
import com.likya.tlossw.utils.SpaceWideRegistry;

public class Consolidator {

	private static Logger logger = Logger.getLogger(Consolidator.class);
	
	public static void compareAndConsolidateTwoTables(String runIdOld, HashMap<String, SpcInfoType> spcLookupTableNew, HashMap<String, SpcInfoType> spcLookupTableOld) {

		logger.info("Konsolidasyon öncesi kuyruk boyları >> Dünün kuyruğu : " + spcLookupTableOld.size() + " Bugünün kuyruğu : " + spcLookupTableNew.size());

		String newSpcIdSample = spcLookupTableNew.keySet().toArray()[0].toString();
		String runIdNew = new TlosSWPathType(newSpcIdSample).getRunId();

		Set<String> itarationSet = new HashSet<String>(spcLookupTableOld.keySet());

		for (String spcIdOld : itarationSet) {

			TlosSWPathType tlosSWPathType = new TlosSWPathType(spcIdOld);
			tlosSWPathType.setRunId(runIdNew);

			SpcInfoType spcInfoTypeOld = spcLookupTableOld.get(spcIdOld);
			// System.out.println("Before : " + spcInfoTypeOld.getSpcId());
			// System.out.println("After : " + spcInfoTypeOld.getSpcId());
			Spc spcReferanceOld = spcInfoTypeOld.getSpcReferance();
			spcInfoTypeOld.setSpcId(tlosSWPathType);
			String tempSpcIdOld = tlosSWPathType.getFullPath();

			if (spcLookupTableNew.containsKey(tempSpcIdOld)) { // Bugünün listesinde de var ise

				if (spcReferanceOld == null) {
					// Empty scenario
					spcLookupTableNew.put(tlosSWPathType.getFullPath(), spcInfoTypeOld);
				} else if (!isScenarioEnsuresTheConditions(spcReferanceOld)) {

					if (spcReferanceOld.isConcurrent()) {
						// Yenisini listeden çıkarıp kopyasını al
						// SpcInfoType tmpSpcInfoType = spcLookupTableNew.remove(tlosSWPathType.getFullPath());
						// eskisini yeni listeye taşı
						spcReferanceOld.setCurrentRunId(runIdNew);
						
						// Yenisinin instance id sini bir arttır ve öylece listeye ekle,
						// Örnek : scenarioId = 3245:13
						// Yeni scenarioId = 3245:14

//						TlosSWPathType tmpTlosSWPathType = new TlosSWPathType(spcIdOld);
//						tmpTlosSWPathType.setRunId(runIdNew);
						while (spcLookupTableNew.containsKey(tlosSWPathType.getFullPath())) {
							tlosSWPathType.incrementRuId();
						}
						spcInfoTypeOld.setSpcId(tlosSWPathType);
						// spcLookupTableNew.put(tmpTlosSWPathType.getFullPath(), tmpSpcInfoType);
						spcLookupTableNew.put(tlosSWPathType.getFullPath(), spcInfoTypeOld);

					} else {
						
						JobQueueResult jobQueueResult = JobQueueOperations.isJobQueueOver(spcInfoTypeOld.getSpcReferance().getJobQueue());
						
						if (jobQueueResult.getNumOfDailyJobsNotOver() == 0) {

							// Spc'ye suspend ol deyip, olana kadar da bekliyoruz ki,
							// Kuyruk üzerinde çalışalım.
							spcInfoTypeOld.getSpcReferance().setSpcSuspended(true);
							while(!LiveStateInfoUtils.equalStates(spcInfoTypeOld.getSpcReferance().getLiveStateInfo(), StateName.PENDING)) {
								try {
									Thread.sleep(100);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
							
							SpcInfoType spcInfoTypeNew = spcLookupTableNew.get(tempSpcIdOld);
							
							HashMap<String, Job> jobQueueOld = spcInfoTypeOld.getSpcReferance().getJobQueue();
							HashMap<String, Job> jobQueueNew = spcInfoTypeNew.getSpcReferance().getJobQueue();
								
							JobQueueOperations.setAllNonNormalJobsUpdateMySelfAfterMe(spcReferanceOld, true);
							
							synchronized (jobQueueNew) {
								ArrayList<SortType> nonDailJobQueueIndex = spcInfoTypeOld.getSpcReferance().getNonDailyJobQueueIndex();
								Iterator<SortType> nonDailJobQueueIndexIterator = nonDailJobQueueIndex.iterator();
								while (nonDailJobQueueIndexIterator.hasNext()) {
									SortType sortType = nonDailJobQueueIndexIterator.next();
									Job oldJob = jobQueueOld.remove(sortType.getJobId());
									jobQueueNew.put(sortType.getJobId(), oldJob);
								}
								JobIndexUtils.reIndexJobQueue(spcInfoTypeNew.getSpcReferance());
							}
							
							spcInfoTypeOld.getSpcReferance().setSpcSuspended(false);
							
//							spcInfoTypeOld.getSpcReferance().setExecutionPermission(true, false);
//							
							while (spcInfoTypeOld.getSpcReferance().getExecuterThread() != null && spcInfoTypeOld.getSpcReferance().getExecuterThread().isAlive()) {
								try {
									Thread.sleep(100);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
								
						} else {
							// Bitince kendini VT'den yenilesin değerini set et.
							spcReferanceOld.setCurrentRunId(runIdNew);
							spcReferanceOld.setUpdateMySelfAfterMe(true);
							// all T< 1 jobs.setUpdateMySelfAfterMe(true);
							JobQueueOperations.setAllNonNormalJobsUpdateMySelfAfterMe(spcReferanceOld, true);
							spcLookupTableNew.put(tlosSWPathType.getFullPath(), spcInfoTypeOld);
						}

					}

				}

				// System.out.println("Referance : " + spcLookupTableNew.get(tempSpcIdOld).getSpcReferance() + "   Yeni  durum : " + spcLookupTableNew.get(tempSpcIdOld).getSpcId().getPlanId());

			} else {

				if ((spcReferanceOld != null) && !isScenarioEnsuresTheConditions(spcReferanceOld)) {
					spcReferanceOld.setCurrentRunId(runIdNew);
					spcLookupTableNew.put(tlosSWPathType.getFullPath(), spcInfoTypeOld);
				}

			}

			spcLookupTableOld.remove(spcIdOld);

		}

		logger.info("Konsolidasyon sonrası kuyruk boyları >> Dünün kuyruğu : " + spcLookupTableOld.size() + " Bıugünün kuyruğu : " + spcLookupTableNew.size());
	}

	public static void compareAndConsolidateTwoTablesOld(String runIdOld, HashMap<String, SpcInfoType> spcLookupTableNew, HashMap<String, SpcInfoType> spcLookupTableOld) {

		logger.info("Konsolidasyon öncesi kuyruk boyları >> Dünün kuyruğu : " + spcLookupTableOld.size() + " Bugünün kuyruğu : " + spcLookupTableNew.size());

		String newSpcIdSample = spcLookupTableNew.keySet().toArray()[0].toString();
		String runIdNew = new TlosSWPathType(newSpcIdSample).getRunId();

		Set<String> itarationSet = new HashSet<String>(spcLookupTableOld.keySet());

		for (String spcIdOld : itarationSet) {

			TlosSWPathType tlosSWPathType = new TlosSWPathType(spcIdOld);
			tlosSWPathType.setRunId(runIdNew);

			SpcInfoType spcInfoTypeOld = spcLookupTableOld.get(spcIdOld);
			// System.out.println("Before : " + spcInfoTypeOld.getSpcId());
			// System.out.println("After : " + spcInfoTypeOld.getSpcId());
			Spc spcReferanceOld = spcInfoTypeOld.getSpcReferance();
			spcInfoTypeOld.setSpcId(tlosSWPathType);
			String tempSpcIdOld = tlosSWPathType.getFullPath();

			if (spcLookupTableNew.containsKey(tempSpcIdOld)) { // Bugünün listesinde de var ise

				if (spcReferanceOld == null) {
					// Empty scenario
					spcLookupTableNew.put(tlosSWPathType.getFullPath(), spcInfoTypeOld);
				} else if (!isScenarioEnsuresTheConditions(spcReferanceOld)) {

					if (spcReferanceOld.isConcurrent()) {
						// Yenisini listeden çıkarıp kopyasını al
						SpcInfoType tmpSpcInfoType = spcLookupTableNew.remove(tlosSWPathType.getFullPath());
						// eskisini yeni listeye taşı
						spcReferanceOld.setCurrentRunId(runIdNew);
						spcLookupTableNew.put(tlosSWPathType.getFullPath(), spcInfoTypeOld);
						// Yenisinin instance id sini bir arttır ve öylece listeye ekle,
						// Örnek : scenarioId = 3245:13
						// Yeni scenarioId = 3245:14

						TlosSWPathType tmpTlosSWPathType = new TlosSWPathType(tlosSWPathType);
						while (spcLookupTableNew.containsKey(tmpTlosSWPathType.getFullPath())) {
							tmpTlosSWPathType.incrementRuId();
						}
						tmpSpcInfoType.setSpcId(tmpTlosSWPathType);
						spcLookupTableNew.put(tmpTlosSWPathType.getFullPath(), tmpSpcInfoType);

					} else {
						// Bitince kendini VT'den yenilesin değerini set et.
						spcReferanceOld.setCurrentRunId(runIdNew);
						spcReferanceOld.setUpdateMySelfAfterMe(true);
						// all T< 1 jobs.setUpdateMySelfAfterMe(true);
						JobQueueOperations.setAllNonNormalJobsUpdateMySelfAfterMe(spcReferanceOld, true);
						spcLookupTableNew.put(tlosSWPathType.getFullPath(), spcInfoTypeOld);
					}

				}

				// System.out.println("Referance : " + spcLookupTableNew.get(tempSpcIdOld).getSpcReferance() + "   Yeni  durum : " + spcLookupTableNew.get(tempSpcIdOld).getSpcId().getPlanId());

			} else {

				if ((spcReferanceOld != null) && !isScenarioEnsuresTheConditions(spcReferanceOld)) {
					spcReferanceOld.setCurrentRunId(runIdNew);
					spcLookupTableNew.put(tlosSWPathType.getFullPath(), spcInfoTypeOld);
				}

			}

			spcLookupTableOld.remove(spcIdOld);

		}

		logger.info("Konsolidasyon sonrası kuyruk boyları >> Dünün kuyruğu : " + spcLookupTableOld.size() + " Bıugünün kuyruğu : " + spcLookupTableNew.size());
	}
	
	public static void analyzeJobsInScenario(SpcInfoType spcInfoType) {

		HashMap<String, Job> jobQueue = spcInfoType.getSpcReferance().getJobQueue();
		
		if (jobQueue != null) {
			
			Iterator<Job> jobsIterator = jobQueue.values().iterator();

			while (jobsIterator.hasNext()) {

				Job job = jobsIterator.next();

				JobProperties jobProperties = job.getJobRuntimeProperties().getJobProperties();

				// String jobId = jobProperties.getID();
				
				boolean isRunning = LiveStateInfoUtils.equalStates(jobProperties, StateName.RUNNING);
				
				boolean isPeriodic = jobProperties.getManagement().getPeriodInfo() != null ? true : false;
				
				boolean isUpdated = true;
				
				if (isPeriodic && isRunning && !isUpdated) {
					job.setUpdateMySelfAfterMe(true);
				}
			}
			
		}
	}

	public static void compareAndConsolidateTwoTables2(String runIdOld, HashMap<String, SpcInfoType> spcLookupTableNew, HashMap<String, SpcInfoType> spcLookupTableOld) {

		for (String spcIdNew : spcLookupTableNew.keySet()) {

			String spcIdOld = ConcurrencyAnalyzer.containsScenario(spcIdNew, runIdOld, spcLookupTableOld);

			// Dünün listesinde de var ise
			if (spcIdOld != null) {

				SpcInfoType spcInfoTypeOld = spcLookupTableOld.get(spcIdOld);

				if (isScenarioEnsuresTheConditions(spcInfoTypeOld.getSpcReferance())/* Completed */) {
					// Eskisini sil, yenisi listede var...
				} else /* Running */{

				}
				spcLookupTableOld.remove(spcIdOld);
			}
		}

		if (!spcLookupTableOld.isEmpty()) {
			// Neden acaba, kimsenin kalmaması lazım ?
			spcLookupTableOld.clear();
		}

	}

	public static void compareAndConsolidateTwoTables1(String runIdOld, HashMap<String, SpcInfoType> spcLookupTableNew, HashMap<String, SpcInfoType> spcLookupTableOld) {

		ArrayList<String> spcLookupTableIntersection = new ArrayList<String>();

		for (String spcIdNew : spcLookupTableNew.keySet()) {

			String spcIdOld = ConcurrencyAnalyzer.containsScenario(spcIdNew, runIdOld, spcLookupTableOld);

			if (spcIdOld == null) {
				/**
				 * Bugün gelen bir senaryo gönül rahatlığı ile bir sonrakine geçebiliriz.
				 * Ancak bütün iş bittiğinde eski listede olupta yeni listede olmayan senaryoları da
				 * gözden geçirip temizlik yapmak gerekecek !!!!!!!
				 */
				continue;
			} else {
				spcLookupTableIntersection.add(spcIdOld);
				checkAndPerformStabilityConditionsOfJobsInScenario((SpcInfoType) spcLookupTableNew.get(spcIdNew), (SpcInfoType) spcLookupTableOld.get(spcIdOld));
			}
		}

		String newInstanceId = new TlosSWPathType(spcLookupTableNew.keySet().toArray()[0].toString()).getRunId();

		/**
		 * Yenilistede olmayıp sadece eski listede olup da tamamlanmamış senaryo
		 * var ise bunlar yeni listeye olduğu gibi taşınıyor.
		 * Öyle mi olmalı ????
		 * 
		 * @author serkan taş
		 */
		for (String spcIdOld : spcLookupTableOld.keySet()) {
			if (!spcLookupTableIntersection.contains(spcIdOld)) {
				if (!isScenarioEnsuresTheConditions(spcLookupTableOld.get(spcIdOld).getSpcReferance())) {
					/**
					 * taşınan işlerin instaneId leri de güncelleniyor.
					 * tasarım kararı netleşirse, eski id de native runid olarak
					 * saklanacak
					 * 
					 * @author serkan taş
					 */
					TlosSWPathType scenarioPathType = new TlosSWPathType(spcIdOld);
					scenarioPathType.setRunId(newInstanceId);
					spcLookupTableNew.put(scenarioPathType.getFullPath(), spcLookupTableOld.get(spcIdOld));
				}
			}
		}
	}

	public static void checkAndPerformStabilityConditionsOfJobsInScenario(SpcInfoType spcInfoTypeNew, SpcInfoType spcInfoTypeOld) {

		HashMap<String, Job> jobQueueNew = spcInfoTypeNew.getSpcReferance().getJobQueue();
		HashMap<String, Job> jobQueueOld = spcInfoTypeOld.getSpcReferance().getJobQueue();

		if (jobQueueNew != null && jobQueueOld != null) {

			Iterator<Job> jobsIteratorOld = jobQueueOld.values().iterator();

			while (jobsIteratorOld.hasNext()) {

				Job jobOld = jobsIteratorOld.next();

				JobProperties jobPropertiesOld = jobOld.getJobRuntimeProperties().getJobProperties();

				String jobIdOld = jobPropertiesOld.getID();
	
				boolean isPeriodic = jobPropertiesOld.getManagement().getPeriodInfo() != null ? true : false;
				
				if (jobQueueNew.containsKey(jobIdOld)) {
					if ( isPeriodic ) {
						if (LiveStateInfoUtils.equalStates(jobPropertiesOld, StateName.RUNNING)) {
							// iş bittikten sonra aşağıdaki adımları yapacaz
							if (!identical(jobQueueNew.get(jobIdOld), jobOld)) {
								// iş bitince yenisini devreye al, güncelleme yok !//güncelleme yapacak şekilde ayarla ama nasıl ????
							}
							jobQueueNew.put(jobIdOld, jobQueueOld.get(jobIdOld));
						} else {
							// eskisini bırak yenisi ile devam et
						}

					} else {
						if (LiveStateInfoUtils.equalStates(jobPropertiesOld, StateName.RUNNING)) {
							// Eğer önceki iş ile aynı anda çalışma izni var ise,
							// eskisini taşıyıp, yanına yenisini eklemek gerekecek
							// Eğer aynı anda çalışma izni yok ise,
							// Eskisinin bitmesini bekleyip sonra çalışacak ama nasıl ????

							// 1. yol : İki iş arasına sanal bağımlılık tanımlama ... daha önce yapıldı
							// 2. yol : Eski işe işini bitirdikten sonra yeni sürümünü de devreye aldırma.... ama nasıl ?
							// yeni kuyruğa taşıyoruz
							jobQueueNew.put(jobIdOld, jobQueueOld.get(jobIdOld));
						} else {
							// eskisini bırak yenisi ile devam et
						}
					}

				} else {
					if (LiveStateInfoUtils.equalStates(jobPropertiesOld, StateName.RUNNING)) {
						if ( isPeriodic ) {
							// set old job to terminate after execution
							//jobQueueNew.get(jobIdOld).setStopRepeatativity(true);
						}
						// yeni kuyruğa taşıyoruz
						jobQueueNew.put(jobIdOld, jobQueueOld.get(jobIdOld));
					} else {
						// Eğer çalışmıyorsa beklemede ise
						// yeni kuyruğa taşıma
					}
				}

			}

		}

		return;

	}

	public static boolean checkStabilityConditions01(SpcInfoType spcInfoTypeNew, SpcInfoType spcInfoTypeOld) {

		boolean isScenaroFinished = true;

		HashMap<String, Job> jobQueueNew = spcInfoTypeNew.getSpcReferance().getJobQueue();
		HashMap<String, Job> jobQueueOld = spcInfoTypeOld.getSpcReferance().getJobQueue();

		if (jobQueueNew != null && jobQueueOld != null) {

			Iterator<Job> jobsIteratorOld = jobQueueOld.values().iterator();

			while (jobsIteratorOld.hasNext()) {

				Job jobOld = jobsIteratorOld.next();

				JobProperties jobPropertiesOld = jobOld.getJobRuntimeProperties().getJobProperties();
				
				boolean isPeriodic = jobPropertiesOld.getManagement().getPeriodInfo() != null ? true : false;
				
				if ( isPeriodic ) {
					String jobIdOld = jobOld.getJobKey();
					if (jobQueueNew.containsKey(jobIdOld)) {
						if (!identical(jobQueueNew.get(jobIdOld), jobOld)) {
							// Aynı yap, yani eşelleştir
						}
						// yeni kuyruğa taşıyoruz
						jobQueueNew.put(jobIdOld, jobQueueOld.get(jobIdOld));
					} else {
						// Eğer çalışıyorsa
						// set old job to terminate self remove after execution
						// Eğer çalışmıyorsa beklemede ise
						// yeni kuyruğa taşıma
					}

					isScenaroFinished = false;
				} else {

					try {
						if (jobPropertiesOld.getStateInfos() != null) {
							if (!LiveStateInfoUtils.equalStates(jobPropertiesOld, StateName.FINISHED)) {
								isScenaroFinished = false;
								break;
							}
						} else {
							SpaceWideRegistry.getGlobalLogger().error("  > isJobQueueOver : jobProperties.getStateInfos() is null !");
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

		}

		return isScenaroFinished;

	}

	// private static boolean checkStabilityConditions(SpcInfoType spcInfoType) {
	//
	// if(!isScenaroCompletedWithSuccess(spcInfoType.getSpcReferance())) {
	// return false;
	// }
	//
	// return true;
	// }

	public static boolean isScenarioEnsuresTheConditions(Spc spcReferance) {

		LiveStateInfo liveStateInfo = null;

		if (spcReferance.getScenario() == null || spcReferance.getScenario().getJsState() == null) {
			// Set to default value
			liveStateInfo = LiveStateInfoUtils.generateLiveStateInfo(StateName.FINISHED, SubstateName.COMPLETED, StatusName.SUCCESS);
		}

		if (!LiveStateInfoUtils.equalStates(spcReferance.getLiveStateInfo(), liveStateInfo)) {
			logger.info("     > SPC Lookup Table da bir onceki calistirmadan kalan " + spcReferance.getSpcAbsolutePath() + " isimli senaryo bitiş koşullarını sağlamıyor !.");
			return false;
		}

		return true;
	}

	public static boolean isJobQueueOver(HashMap<String, Job> jobQueue) {

		if (jobQueue != null) {
			Iterator<Job> jobsIterator = jobQueue.values().iterator();
			while (jobsIterator.hasNext()) {
				Job scheduledJob = jobsIterator.next();

				JobProperties jobProperties = scheduledJob.getJobRuntimeProperties().getJobProperties();
				boolean isPeriodic = jobProperties.getManagement().getPeriodInfo() != null ? true : false;
				
				if ( isPeriodic ) {
					return false;
				}
				// SpaceWideRegistry.getSpaceWideLogger().info("   > JobQueue element jobsIterator: " + jobsIterator);
				// SpaceWideRegistry.getSpaceWideLogger().info("   > JobQueue element scheduledJob: " + scheduledJob.getJobRuntimeProperties());
				try {
					if (jobProperties.getStateInfos() != null) {
						if (!jobProperties.getStateInfos().getLiveStateInfos().getLiveStateInfoArray(0).getStateName().equals(StateName.FINISHED)) {
							return false;
						}
					} else {
						SpaceWideRegistry.getGlobalLogger().error("  > isJobQueueOver fonksiyonunda problem2 : " + scheduledJob.getJobRuntimeProperties().getJobProperties());
					}
				} catch (Exception e) {
					SpaceWideRegistry.getGlobalLogger().error("  > isJobQueueOver fonksiyonunda problem : " + scheduledJob.getJobRuntimeProperties().getJobProperties().getStateInfos());
					e.printStackTrace();
				}
			}
		}
		return true;
	}

	private static boolean identical(Job jobNew, Job jobOld) {
		return jobNew.getJobRuntimeProperties().getJobProperties().toString().equals(jobOld.getJobRuntimeProperties().getJobProperties().toString());
	}
}
