/*
 * TlosFaz_V2.0
 * com.likya.tlos.core.spc.helpers : StateUtils.java
 * @author Serkan Ta�
 * Tarih : 10.�ub.2010 12:40:09
 */

package com.likya.tlossw.core.spc.helpers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import com.likya.tlos.model.xmlbeans.state.JobStatusListDocument.JobStatusList;
import com.likya.tlos.model.xmlbeans.state.ReturnCodeDocument.ReturnCode;
import com.likya.tlos.model.xmlbeans.state.State;
import com.likya.tlos.model.xmlbeans.state.StateNameDocument.StateName;
import com.likya.tlos.model.xmlbeans.state.Status;
import com.likya.tlos.model.xmlbeans.state.SubstateDocument.Substate;
import com.likya.tlos.model.xmlbeans.state.SubstateNameDocument.SubstateName;
import com.likya.tlossw.utils.GlobalRegistry;

public class StateUtils {

	/**
	 * Bu foksiyon, yerel tanımlar için düşünüldüğünde
	 * sadece ve sadece StateNameType.FINISHED ve SubstateNameType.COMPLETED
	 * olarak kabul edilerek statu tanımı yapılmasına müsade ediyor.
	 * 
	 * @param mySubstateNameType
	 * @param jobStatusList
	 * @param returnCode
	 * @return
	 */

	/**
	 * Bir işin statu listesi içindeki returncode listeleri taranıp, verilen returncode
	 * değeri aranıyor. Bulunur ise, hangi status içinde bulunduysa o geri dönüyor.
	 * 
	 * @param jobStatusList
	 * @param returnCode
	 * @return
	 */

	public static Status contains(JobStatusList jobStatusList, int returnCode) {

		Status returnValue = null;

		for (Status myStatus : jobStatusList.getJobStatusArray()) {

			for (ReturnCode myReturnCode : myStatus.getReturnCodeListArray(0).getReturnCodeArray()) {

				if (myReturnCode.getCode() == returnCode) {
					returnValue = myStatus;
				}

			}

		}

		return returnValue;
	}

	public static Status containsOld(JobStatusList jobStatusList, int returnCode) {

		Status returnValue = null;
		Iterator<Status> myjobStatusListIterator = new ArrayList<Status>(Arrays.asList(jobStatusList.getJobStatusArray())).iterator();

		while (myjobStatusListIterator.hasNext()) {

			Status myStatus = (Status) myjobStatusListIterator.next();

			Iterator<ReturnCode> myStatusIterator = new ArrayList<ReturnCode>(Arrays.asList(myStatus.getReturnCodeListArray(0).getReturnCodeArray())).iterator();
			while (myStatusIterator.hasNext()) {
				ReturnCode myReturnCode = (ReturnCode) myStatusIterator.next();
				if (myReturnCode.getCode() == returnCode) {
					returnValue = myStatus;
				}
			}
		}

		return returnValue;
	}

	/**
	 * Yukarıdaki taramada bulunmaz ise, global değişkenler içinde böyle bir
	 * tanım var mı ona bakıyor.
	 * 
	 * @param myNameType
	 * @param mySubstateNameType
	 * @param globalRegistry
	 * @param returnCode
	 * @return
	 */

	public static Status globalContains(StateName.Enum myNameType, SubstateName.Enum mySubstateNameType, GlobalRegistry globalRegistry, int returnCode) {

		Status returnValue = null;

		for (State myGlobalState : globalRegistry.getGlobalStateDefinition().getGlobalStateArray()) {

			if (!myGlobalState.getStateName().equals(myNameType)) {
				continue;
			}

			for (Substate mySubstate : myGlobalState.getSubstateArray()) {

				if (!mySubstate.getSubstateName().equals(mySubstateNameType)) {
					continue;
				}

				for (Status mySubStateStatuses : mySubstate.getSubStateStatusesArray()) {

					for (ReturnCode myReturnCode : mySubStateStatuses.getReturnCodeListArray(0).getReturnCodeArray()) {

						if (myReturnCode.getCode() == returnCode) {
							returnValue = mySubStateStatuses;
						}

					}

				}
			}

		}

		return returnValue;
	}

	public static Status globalContainsOld(com.likya.tlos.model.xmlbeans.state.StateNameDocument.StateName.Enum myNameType, com.likya.tlos.model.xmlbeans.state.SubstateNameDocument.SubstateName.Enum mySubstateNameType, GlobalRegistry globalRegistry, int returnCode) {

		Status returnValue = null;

		Iterator<State> myGlobalStateIterator = new ArrayList<State>(Arrays.asList(globalRegistry.getGlobalStateDefinition().getGlobalStateArray())).iterator();

		while (myGlobalStateIterator.hasNext()) {

			State myGlobalState = (State) myGlobalStateIterator.next();

			if (!myGlobalState.getStateName().equals(myNameType)) {
				continue;
			}

			Iterator<Substate> mySubstateIterator = new ArrayList<Substate>(Arrays.asList(myGlobalState.getSubstateArray())).iterator();

			while (mySubstateIterator.hasNext()) {

				Substate mySubstate = (Substate) mySubstateIterator.next();

				if (!mySubstate.getSubstateName().equals(mySubstateNameType)) {
					continue;
				}

				Iterator<Status> mySubStateStatusesIterator = new ArrayList<Status>(Arrays.asList(mySubstate.getSubStateStatusesArray())).iterator();

				while (mySubStateStatusesIterator.hasNext()) {
					Status mySubStateStatuses = (Status) mySubStateStatusesIterator.next();

					Iterator<ReturnCode> myStatusIterator = new ArrayList<ReturnCode>(Arrays.asList(mySubStateStatuses.getReturnCodeListArray(0).getReturnCodeArray())).iterator();
					while (myStatusIterator.hasNext()) {
						ReturnCode myReturnCode = (ReturnCode) myStatusIterator.next();
						if (myReturnCode.getCode() == returnCode) {
							returnValue = mySubStateStatuses;
						}
					}
				}
			}
		}

		return returnValue;
	}
}
