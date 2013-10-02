package com.likya.tlossw.core.cpc.helper;

import java.util.HashMap;
import java.util.Iterator;

import org.apache.log4j.Logger;

import com.likya.tlos.model.xmlbeans.common.JsTypeDocument.JsType;
import com.likya.tlos.model.xmlbeans.data.DependencyListDocument.DependencyList;
import com.likya.tlos.model.xmlbeans.data.ItemDocument.Item;
import com.likya.tlos.model.xmlbeans.state.JsDependencyRuleDocument.JsDependencyRule;
import com.likya.tlos.model.xmlbeans.state.StateNameDocument.StateName;
import com.likya.tlossw.core.cpc.model.InstanceInfoType;
import com.likya.tlossw.core.cpc.model.SpcInfoType;
import com.likya.tlossw.core.spc.Spc;
import com.likya.tlossw.exceptions.TlosException;
import com.likya.tlossw.model.path.ScenarioPathType;

public class ConcurrencyAnalyzer {

	public static void checkConcurrency(String instanceIdOld, HashMap<String, SpcInfoType> spcLookupTableNew, HashMap<String, SpcInfoType> spcLookupTableOld) throws TlosException {

		for (String spcId : spcLookupTableNew.keySet()) {

			String keyStr = containsScenario(spcId, instanceIdOld, spcLookupTableOld);

			if (keyStr != null) {

				SpcInfoType spcInfoTypeMaster = spcLookupTableOld.get(keyStr);
				SpcInfoType spcInfoTypeNew = spcLookupTableNew.get(spcId);

				if (!spcInfoTypeNew.getSpcReferance().getConcurrencyManagement().getConcurrent()) {
					// if (!spcInfoTypeNew.getSpcReferance().isConcurrent()) {

					// ** Biraz parsing laz�m :( *//*
					Item mydependencyItem = Item.Factory.newInstance();
					mydependencyItem.setDependencyID("VD1");
					mydependencyItem.setComment("Bu sanal bir bagimlilik tanimidir. Bagli oldugu senaryo  ise budur : " + spcInfoTypeMaster.getSpcReferance().getSpcId());

					JsDependencyRule myDependencyRule = JsDependencyRule.Factory.newInstance();
					myDependencyRule.setStateName(StateName.FINISHED);
					mydependencyItem.setJsDependencyRule(myDependencyRule);

					mydependencyItem.setJsName(spcInfoTypeMaster.getSpcReferance().getBaseScenarioInfos().getJsName());
					mydependencyItem.setJsPath(spcInfoTypeMaster.getSpcReferance().getSpcId().getFullPath());
					mydependencyItem.setJsType(JsType.SCENARIO);
					
					DependencyList myDependencyList = DependencyList.Factory.newInstance();
					
					String dependecyExpression = null;
					
					if ((spcInfoTypeNew.getSpcReferance().getDependencyList() != null) && spcInfoTypeNew.getSpcReferance().getDependencyList().getItemArray().length > 0) {

						myDependencyList.addNewItem();
						myDependencyList.setItemArray(myDependencyList.sizeOfItemArray() - 1, mydependencyItem);

						dependecyExpression = spcInfoTypeNew.getSpcReferance().getDependencyList().getDependencyExpression();
						dependecyExpression = mydependencyItem.getDependencyID() + " and ( " + dependecyExpression + " )";

						myDependencyList.setDependencyExpression(dependecyExpression);
						spcInfoTypeNew.getSpcReferance().setDependencyList(myDependencyList);

					} else {
						
						dependecyExpression = mydependencyItem.getDependencyID();
						myDependencyList.addNewItem();
						myDependencyList.setItemArray(myDependencyList.sizeOfItemArray() - 1, mydependencyItem);

					}
					
					myDependencyList.setDependencyExpression(dependecyExpression);
					spcInfoTypeNew.getSpcReferance().setDependencyList(myDependencyList);
				}
			}
		}

	}
	
	public static String containsScenario(String newScenarioId, String instanceIdOld, HashMap<String, SpcInfoType> spcLookupTableOld) {

		ScenarioPathType masterPathType = new ScenarioPathType(newScenarioId);
		
		masterPathType.setInstanceId(instanceIdOld);
		
		Object retValue = spcLookupTableOld.get(masterPathType.getFullPath());
		
		return ( retValue == null ? null : retValue.toString());
		
	}
	
	public static String containsScenarioEskisi(String scenarioId, HashMap<String, SpcInfoType> spcLookupTableMaster) {

		Iterator<String> keyIterator = spcLookupTableMaster.keySet().iterator();

		ScenarioPathType tmpPath = new ScenarioPathType(scenarioId);
		
		while (keyIterator.hasNext()) {
			String masterScenarioId = keyIterator.next();
			ScenarioPathType masterPath = new ScenarioPathType(masterScenarioId);
			if (tmpPath.getAbsolutePath().equals(masterPath.getAbsolutePath())) {
				return masterScenarioId;
			}
		}

		return null;
	}
	
	public static boolean isSpcLookUpTableClean(HashMap<String, SpcInfoType> spcLookupTable, Logger logger) {
		
		for (String spcId : spcLookupTable.keySet()) {
			Spc spc = spcLookupTable.get(spcId).getSpcReferance();

			if (!spc.getLiveStateInfo().getStateName().equals(StateName.FINISHED)) {
				logger.info("     > SPC Lookup Table da bir onceki calistirmadan kalan " + spcId + " isimli senaryo bitirilmemis.");
				return false;
			}
		}
		
		return true;
		
	}
	
	public static void checkAndCleanSpcLookUpTables(HashMap<String, InstanceInfoType> instanceLookupTable, Logger logger) {

		boolean checkValue = true;

		for (String instanceId : instanceLookupTable.keySet()) {

			InstanceInfoType instanceInfoType = instanceLookupTable.get(instanceId);
			HashMap<String, SpcInfoType> spcLookupTable = instanceInfoType.getSpcLookupTable().getTable();

			checkValue = isSpcLookUpTableClean(spcLookupTable, logger);

			if (checkValue) {
				logger.info("     > SPC Lookup Table da bir onceki calistirmadan kalan isler tamamen bitirilmis, tablodan temizleniyor ...");
				spcLookupTable.clear();
				instanceLookupTable.remove(instanceId);
				logger.info("     > Temizlendi.");
			} else {
				logger.info("     > SPC Lookup Table da bir onceki calistirmadan kalan bazi isler bitirilmemis.");
			}
		}

		return;
	}

}