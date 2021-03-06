package com.likya.tlossw.transform;

/**
 [Class description.  This class is being used for parameter pass between jobs.]

 [Other notes, including guaranteed invariants, usage instructions and/or examples, reminders
 about desired improvements, etc.]

 @author <A HREF="mailto:hakan.saribiyik@likyateknoloji.com">Hakan Saribiyik</A>
 @version $Revision: 1.1.1.1 $ $Date: 2012/08/17 15:15:25 $
 @see [String]
 @see [URL]
 @see [ParameterPassing#passParameter]
 **/

import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.collections.iterators.ArrayIterator;
import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;

import com.likya.tlos.model.xmlbeans.common.InParamDocument.InParam;
import com.likya.tlos.model.xmlbeans.common.LocalParametersDocument.LocalParameters;
import com.likya.tlos.model.xmlbeans.common.OutParamDocument.OutParam;
import com.likya.tlos.model.xmlbeans.data.DependencyListDocument.DependencyList;
import com.likya.tlos.model.xmlbeans.data.ItemDocument.Item;
import com.likya.tlos.model.xmlbeans.data.JobPropertiesDocument.JobProperties;
import com.likya.tlos.model.xmlbeans.parameters.ParameterDocument;
import com.likya.tlos.model.xmlbeans.parameters.ParameterDocument.Parameter;
import com.likya.tlos.model.xmlbeans.parameters.PreValueDocument.PreValue;
import com.likya.tlossw.TlosSpaceWide;
import com.likya.tlossw.core.cpc.Cpc;
import com.likya.tlossw.core.cpc.model.RunInfoType;
import com.likya.tlossw.core.cpc.model.SpcInfoType;
import com.likya.tlossw.core.spc.Spc;
import com.likya.tlossw.core.spc.jobs.Job;
import com.likya.tlossw.core.spc.model.JobRuntimeProperties;
import com.likya.tlossw.exceptions.TlosFatalException;
import com.likya.tlossw.exceptions.UnresolvedDependencyException;
import com.likya.tlossw.model.SpcLookupTable;
import com.likya.tlossw.model.path.BasePathType;
import com.likya.tlossw.utils.SpaceWideRegistry;
import com.likya.tlossw.utils.xml.ApplyXPath;

public class InputParameterPassing {

	transient private Logger myLogger;
	transient private SpaceWideRegistry spaceWideRegistry;
	private String runId;

	public InputParameterPassing(SpaceWideRegistry spaceWideRegistry, String runId) {

		myLogger = Logger.getLogger(InputParameterPassing.class);
		this.spaceWideRegistry = spaceWideRegistry;
		this.runId = runId;
	}

	private String[] findInputValue(String runId, String xpath) throws TlosFatalException {
		String[] result;
		RunInfoType runInfoType = TlosSpaceWide.getSpaceWideRegistry().getRunLookupTable().get(runId);

		HashMap<String, SpcInfoType> spcLookupTable = runInfoType.getSpcLookupTable().getTable();

		for (String spcId : spcLookupTable.keySet()) {

			Spc spc = spcLookupTable.get(spcId).getSpcReferance();
			
			if (spc == null)
				continue;	
			
			try {
				result = findInputValue(spc, xpath);
				if (result != null && result.length > 0)
					return result;
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}

		return null;
	}

	private String[] findInputValue(Spc spcc, String sorgu) {

		JobProperties xmlDoc = null;
		String[] result;

		Iterator<Job> jobsIterator = spcc.getJobQueue().values().iterator();

		while (jobsIterator.hasNext()) {
			Job job = jobsIterator.next();

			xmlDoc = job.getJobRuntimeProperties().getJobProperties();
			result = null;
			try {
				result = ApplyXPath.queryXmlWithXPath(xmlDoc, sorgu);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (result != null && result.length > 0)
				return result;
			// applyXPath.queryXmlWithXPath(xmlDoc, xpath);
		}
		return null;
	}

	public synchronized boolean setInputParameterViaDependency(HashMap<String, Job> jobQueue, Job ownerJob, SpcLookupTable spcLookupTable) throws UnresolvedDependencyException {

		JobProperties ownerJobProperties = ownerJob.getJobRuntimeProperties().getJobProperties();
		DependencyList dependentJobList = ownerJobProperties.getDependencyList();

		if (dependentJobList == null) {
			return false;
		}

		String dependencyExpression = dependentJobList.getDependencyExpression().trim().toUpperCase();
		Item[] dependencyArray = ownerJobProperties.getDependencyList().getItemArray();

		dependencyExpression = dependencyExpression.replace("AND", "&&");
		dependencyExpression = dependencyExpression.replace("OR", "||");

		ArrayIterator dependencyArrayIterator = new ArrayIterator(dependencyArray);

		while (dependencyArrayIterator.hasNext()) {

			Item item = (Item) (dependencyArrayIterator.next());
			String jobId = item.getJsId();

			if (dependencyExpression.indexOf(item.getDependencyID().toUpperCase()) < 0) {
				String errorMessage = "     > " + ownerJob.getJobRuntimeProperties().getJobProperties().getBaseJobInfos().getJsName() + " isi icin hatali bagimlilik tanimlamasi yapilmis ! (" + dependencyExpression + ") kontrol ediniz.";
				getMyLogger().info(errorMessage);
				getMyLogger().error(errorMessage);
				throw new UnresolvedDependencyException(errorMessage);
			}

			JobProperties jobProperties = ownerJobProperties;

			if (item.getJsPath() == null || item.getJsPath() == "") {// Lokal bir bagimlilik

				Job depJob = jobQueue.get(jobId);

				if (depJob == null) {
					getMyLogger().error("     > Yerel bagimlilik tanimi yapilan is bulunamadi : " + item.getJsName());
					getMyLogger().error("     > Ana is adi : " + ownerJob.getJobRuntimeProperties().getJobProperties().getBaseJobInfos().getJsName());
					getMyLogger().error("     > Ana senaryo yolu : " + ownerJob.getJobRuntimeProperties().getAbsoluteJobPath());
					getMyLogger().info("     > Bagimlilikla ilgili bir problemden dolayi uygulama sona eriyor !");
					throw new UnresolvedDependencyException("     > Yerel bagimlilik tanimi yapilan is bulunamadi : " + item.getJsName());
				}

				JobRuntimeProperties depJobRuntimeProperties = depJob.getJobRuntimeProperties();

				JobProperties depJobProperties = depJobRuntimeProperties.getJobProperties();

				if (depJobProperties == null) {
					getMyLogger().error("     > Genel bagimlilik tanimi yapilan :");
					getMyLogger().error("     > Ana is adi : " + ownerJob.getJobRuntimeProperties().getJobProperties().getBaseJobInfos().getJsName());
					getMyLogger().error("     > Bagli is : " + item.getJsName() + " tanimli mi? Tanimli ise bagimlilik ile ilgili bir problem olabilir! (Problem no:1045)");
					getMyLogger().error("     >    Dizin : " + BasePathType.getRootPath() + "." + getPlanId() + "." + item.getJsPath());
					getMyLogger().error("     > 	Yukaridaki is  yerel senaryoda bulunamadi !");
					getMyLogger().error("     > Uygulama sona eriyor !");
					getMyLogger().info("     > Bagimlilikla ilgili bir problemden dolayi uygulama sona eriyor !");
					throw new UnresolvedDependencyException("     > Bagimlilikla ilgili bir problemden dolayi uygulama sona eriyor !");
				}

				boolean retValue = assignParameter(jobProperties, depJobProperties);

				System.out.println("Parametre gecisi yapiliyor.1.");

				return retValue;

			} else { // Global bir bağımlılık

				SpcInfoType spcInfoType = spcLookupTable.getTable().get(BasePathType.getRootPath() + "." + item.getJsPath());
				// SpcInfoType spcInfoType = getSpaceWideRegistry().getPlanLookupTable().get(getInstanceId()).getSpcLookupTable().get(Cpc.getRootPath() + "." + getInstanceId() + "." + item.getJsPath());
				if (spcInfoType == null) {
					getMyLogger().error("     > Genel bagimlilik tanimi yapilan senaryo bulunamadi : " + BasePathType.getRootPath() + "." + getPlanId() + "." + item.getJsPath());
					getMyLogger().error("     > Ana is adi : " + ownerJob.getJobRuntimeProperties().getJobProperties().getBaseJobInfos().getJsName());
					getMyLogger().error("     > Ana senaryo yolu : " + ownerJob.getJobRuntimeProperties().getAbsoluteJobPath());
					getMyLogger().error("     > Uygulama sona eriyor !");
					getMyLogger().info("     > Bagimlilikla ilgili bir problemden dolayi uygulama sona eriyor !");
					Cpc.dumpSpcLookupTable(getPlanId(), getSpaceWideRegistry().getRunLookupTable().get(getPlanId()).getSpcLookupTable());
					throw new UnresolvedDependencyException("     > Genel bagimlilik tanimi yapilan senaryo bulunamadi : " + BasePathType.getRootPath() + "." + getPlanId() + "." + item.getJsPath());
				}

				Job depJob /* jobb */= spcInfoType.getSpcReferance().getJobQueue().get(jobId);
				JobRuntimeProperties depJobRuntimeProperties = depJob.getJobRuntimeProperties();

				JobProperties depJobProperties /* job */= depJobRuntimeProperties.getJobProperties();

				if (depJobProperties == null) {
					getMyLogger().error("     > Genel bagimlilik tanimi yapilan :");
					getMyLogger().error("     > Ana is adi : " + ownerJob.getJobRuntimeProperties().getJobProperties().getBaseJobInfos().getJsName());
					getMyLogger().error("     > Bagli is : " + item.getJsName() + " tanimli mi? Tanimli ise bagimlilik ile ilgili bir problem olabilir! (Problem no:1045)");
					getMyLogger().error("     > 	Yukaridaki is  " + spcInfoType.getSpcReferance().getSpcAbsolutePath() + " adli senaryoda bulunamadi !");
					getMyLogger().error("     > Uygulama sona eriyor !");
					getMyLogger().info("     > Bagimlilikla ilgili bir problemden dolayi uygulama sona eriyor !");
					throw new UnresolvedDependencyException("     > Bagimlilikla ilgili bir problemden dolayi uygulama sona eriyor !");
				}

				boolean retValue = assignParameter(jobProperties, depJobProperties);

				System.out.println("Parametre gecisi yapiliyor.2.");

				return retValue;
			}

		}

		return false;
	}

	private boolean assignParameter(JobProperties jobProperties, JobProperties depJobProperties) {

		boolean assignmentOk = false;

		LocalParameters depJobLocalParameters = depJobProperties.getLocalParameters();

//		if (depJobTypeDetails.getSpecialParameters() == null) {
//			return assignmentOk;
//		}

		OutParam outParameter = depJobLocalParameters.getOutParam();

		if (outParameter == null) {
			return assignmentOk;
		}

		LocalParameters localParameter = jobProperties.getLocalParameters();

		// Durum 2: icin
		if (localParameter == null) {
			jobProperties.addNewLocalParameters();
			jobProperties.getLocalParameters().addNewInParam();
		} else if (localParameter.getInParam() == null) {
			localParameter.addNewInParam();
		}

		// localParameter = jobProperties.getLocalParameters();
		InParam inParam = localParameter.getInParam();
		boolean paramF = false;

		// Durum 2
		for (int i = 0; i < outParameter.sizeOfParameterArray(); i++) {
			for (int j = 0; j < inParam.sizeOfParameterArray(); j++) {
				if (!paramF) {
					// inParam.addNewParameter();
					//if (inParam.getParameterArray(j).getName().equalsIgnoreCase(outParameter.getParameterArray(i).getName()) || outParameter.getParameterArray(i).getActive()) {
					Parameter inParamElement = inParam.getParameterArray(j);
					Parameter outParamElement = outParameter.getParameterArray(i);
					
					if ( outParamElement.getActive() && inParamElement.getConnectedId() != null && inParamElement.getConnectedId().equals(outParamElement.getId()) )
//						( (inParamElement.getIoName() != null && inParamElement.getIoName().equalsIgnoreCase(outParamElement.getIoName()) ) ||
//						  (inParamElement.getName() != null && inParamElement.getName().equalsIgnoreCase(outParamElement.getName()) )) ) 
					{
						// inParam.getParameterArray(0).setName(outParameter.getParameterArray(i).getName());

						PreValue preValue = PreValue.Factory.newInstance();
						preValue.setStringValue(outParamElement.getPreValue().getStringValue());
						preValue.setType(outParamElement.getPreValue().getType());

						// preValue.setStringValue(" Bu parametre " + depJobProperties.getBaseJobInfos().getJsName() + " isine olan bagimliliktan geliyor.");

						inParamElement.setPreValue(preValue);
						
						if(outParamElement.getPreValue().getType() == 2)
							inParamElement.setValueString(outParamElement.getValueString());
						
						inParamElement.setJsId(depJobProperties.getID());
						inParamElement.setFromUser(false);
						inParamElement.setIoType(false);

						assignmentOk = true;
					}
				}
			}
		}

		if (SpaceWideRegistry.isDebug) {
			if (assignmentOk) {
				System.out.println("Parametre gecisi yapildi.");
			} else {
				System.out.println("Parametre gecisi yapilMAdi.");
			}
		}
		return assignmentOk;
	}

	public boolean setInputParameter(JobProperties job) {

		boolean assignmentOk = false;
		/**** parametre gecisi varsa yapalim **************/

		// Once job in kullandigi parametreler icinde
		// Durum 1: baska bir job in output u olan parametre var mi diye
		// bakalim.
		// Bunun icin
		// com:specialParameters/com:inParam/par:parameter[par:preValue/@type=\"6\"]
		// kosulu aranir.

		String inputs[] = null, inputPar[] = null;
		String xpath = "/dat:jobProperties/com:localParameters/com:inParam/par:parameter[par:preValue/@type=\"6\"]";
		try {
			inputs = ApplyXPath.queryXmlWithXPath(job, xpath);
			Parameter parameterList[] = new Parameter[inputs.length];

			if (inputs != null && inputs.length > 0) {
				try {
					// Degeri baska bir isin outputu olan input parametrelerin
					// degerlerini ilgili islerden bulalim.
					for (int i = 0; i < inputs.length; i++) {

						parameterList[i] = ParameterDocument.Factory.parse(inputs[i]).getParameter();
						String sorgu = "/" + parameterList[i].getPreValue().getStringValue();
						// calisacak job daki parametre ismi (Input)
						String paramNameInI2 = parameterList[i].getName();

						inputPar = findInputValue(SpaceWideRegistry.getInstance().getTlosProcessData().getRunId(), sorgu);
						if (inputPar != null && inputPar.length > 0) {
							System.out.println("Gecen parametre = " + inputPar[0].toString());

							Parameter parameterInput = ParameterDocument.Factory.parse(inputPar[0]).getParameter();
							// Geldigi job daki ismi (outputName)
							String paramNameInI1 = parameterInput.getName();
							String result = parameterInput.getValueString();
							System.out.println("Parametre mapping i : " + paramNameInI1 + " --> " + paramNameInI2 + " = " + result);
							if (job instanceof JobProperties) {
								LocalParameters localParameter = job.getLocalParameters();
								if (localParameter == null) {
									job.addNewLocalParameters();
									job.getLocalParameters().addNewInParam();
								} else if (localParameter.getInParam() == null) {
									localParameter.addNewInParam();
								}

								// ///////////////////////////////////////////////////
								localParameter = job.getLocalParameters();
								InParam inParam = localParameter.getInParam();
								Boolean paramF = false;
								// Durum 1:
								for (int j = 0; j < inParam.sizeOfParameterArray(); j++) {
									paramF = inParam.getParameterArray(j).getName().equalsIgnoreCase(paramNameInI2);
									if (paramF) {
										inParam.getParameterArray(j).getPreValue().setStringValue(sorgu);
										inParam.getParameterArray(j).getPreValue().setType((short) 2);
										inParam.getParameterArray(j).setValueString(result);
										assignmentOk = true;
										break;
									}
								}
							}

						}

					}

				} catch (XmlException e) {
					e.printStackTrace();
					return false;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (assignmentOk) {
			if (SpaceWideRegistry.isDebug) {
				System.out.println("Parametre gecisi yapildi.");
			}
			return true;
		} else {
			if (SpaceWideRegistry.isDebug) {
				System.out.println("Parametre gecisi yapilMAdi.");
			}
			return false;
		}
	}

	public Logger getMyLogger() {
		return myLogger;
	}

	public SpaceWideRegistry getSpaceWideRegistry() {
		return spaceWideRegistry;
	}

	public String getPlanId() {
		return runId;
	}

}
