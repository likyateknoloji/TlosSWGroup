package com.likya.tlossw.web.definitions.helpers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;

import com.likya.tlos.model.xmlbeans.data.JobPropertiesDocument.JobProperties;
import com.likya.tlos.model.xmlbeans.data.OSystemDocument.OSystem;
import com.likya.tlos.model.xmlbeans.state.JobStatusListDocument.JobStatusList;
import com.likya.tlos.model.xmlbeans.state.ReturnCodeDocument.ReturnCode;
import com.likya.tlos.model.xmlbeans.state.ReturnCodeListDocument.ReturnCodeList;
import com.likya.tlos.model.xmlbeans.state.ReturnCodeListDocument.ReturnCodeList.OsType;
import com.likya.tlos.model.xmlbeans.state.ScenarioStatusListDocument.ScenarioStatusList;
import com.likya.tlos.model.xmlbeans.state.Status;
import com.likya.tlos.model.xmlbeans.state.StatusNameDocument.StatusName;
import com.likya.tlossw.web.definitions.JobBasePanelBean;
import com.likya.tlossw.web.utils.WebInputUtils;

public class StateInfosTabBean extends BaseTabBean {

	private static final long serialVersionUID = 1283735807693574258L;

	private String[] selectedReturnCodeList;
	
	private String oSystem;
	
	private boolean jsUpdateButton;
	
	private Status jobStatus;

	private String jobStatusName;

	private List<SelectItem> manyJobStatusList;
	private String[] selectedJobStatusList;

	/* jsStatusPopup */
	private boolean statusDialogShow = false;

	private String osType;
	private ReturnCode returnCode;
	private List<SelectItem> manyReturnCodeList;
	
	public JobBasePanelBean jobBasePanelBean;

	public StateInfosTabBean(JobBasePanelBean jobBasePanelBean) {
		super();
		this.jobBasePanelBean = jobBasePanelBean;
	}

	public void resetTab() {
		returnCode = ReturnCode.Factory.newInstance();

		jobStatus = Status.Factory.newInstance();
		jobStatusName = "";
		manyJobStatusList = new ArrayList<SelectItem>();
	}

	public void initJobStatusPopup(ActionEvent e) {
		setStatusDialogShow(!checkDuplicateStateName());

		osType = OSystem.WINDOWS.toString();
		jobStatus = Status.Factory.newInstance();
		setReturnCode(ReturnCode.Factory.newInstance());
		manyReturnCodeList = new ArrayList<SelectItem>();

	}

	public boolean checkDuplicateStateName() {
		if (getManyJobStatusList() != null) {

			for (int i = 0; i < getManyJobStatusList().size(); i++) {

				if (getManyJobStatusList().get(i).getValue().equals(jobStatusName)) {
					addMessage("addReturnCode", FacesMessage.SEVERITY_ERROR, "tlos.info.job.status.duplicate", null);

					return true;
				}
			}
		}

		return false;
	}

	public void updateJobStatusAction(Status[] statusArray) {

		for (Status jStatus : statusArray) {
			if (jobStatus.getStatusName().toString().equals(jStatus.getStatusName().toString())) {
				jStatus = WebInputUtils.cloneJobStatus(jobStatus);

				addMessage("addReturnCode", FacesMessage.SEVERITY_INFO, "tlos.info.job.code.update", null);

				break;
			}
		}

		statusDialogShow = false;
	}

	public void jobStatusEditAction(Status[] statusArray) {

		if (selectedJobStatusList == null || selectedJobStatusList.length == 0) {
			addMessage("addReturnCode", FacesMessage.SEVERITY_ERROR, "tlos.info.job.status.choose", null);
			return;
		} else if (selectedJobStatusList.length > 1) {
			addMessage("addReturnCode", FacesMessage.SEVERITY_ERROR, "tlos.info.job.status.choose.one", null);
			return;
		}

		for (Status status : statusArray) {
			if (status.getStatusName().toString().equals(selectedJobStatusList[0])) {
				jobStatus = WebInputUtils.cloneJobStatus(status);
				jobStatusName = selectedJobStatusList[0];

				break;
			}
		}

		manyReturnCodeList = new ArrayList<SelectItem>();

		for (int i = 0; i < jobStatus.getReturnCodeListArray().length; i++) {
			ReturnCodeList returnCodeList = jobStatus.getReturnCodeListArray(i);

			for (int j = 0; j < returnCodeList.getReturnCodeArray().length; j++) {
				ReturnCode returnCode = returnCodeList.getReturnCodeArray(j);

				SelectItem item = new SelectItem();
				item.setValue(returnCode.getCode());
				item.setLabel(returnCodeList.getOsType().toString() + " : " + returnCode.getCode() + " -> " + jobStatusName);

				manyReturnCodeList.add(item);
			}
		}

		osType = OSystem.WINDOWS.toString();
		returnCode = ReturnCode.Factory.newInstance();

		statusDialogShow = true;
	}
	
	private void addToJobStatusList(Status tmpJobStatus) {
		
		JobStatusList jobStatusList = null;

		JobProperties jobProperties = jobBasePanelBean.getJobProperties();
		
		if (jobProperties.getStateInfos().getJobStatusList() == null || jobProperties.getStateInfos().getJobStatusList().sizeOfJobStatusArray() == 0) {
			jobProperties.getStateInfos().setJobStatusList(JobStatusList.Factory.newInstance());

			jobStatusList = jobProperties.getStateInfos().getJobStatusList();

			tmpJobStatus.setStsId("1");
		} else {
			jobStatusList = jobProperties.getStateInfos().getJobStatusList();

			int lastStatusIndex = jobStatusList.sizeOfJobStatusArray() - 1;
			String id = jobStatusList.getJobStatusArray(lastStatusIndex).getStsId();

			tmpJobStatus.setStsId((Integer.parseInt(id) + 1) + "");
		}

		Status newStatus = jobStatusList.addNewJobStatus();
		newStatus.set(tmpJobStatus);
	}
	
	public void saveJobStatusAction() {

		if (getManyReturnCodeList() == null || getManyReturnCodeList().size() == 0) {
			addMessage("addReturnCode", FacesMessage.SEVERITY_ERROR, "tlos.validation.job.codeList", null);

			return;
		}

		Status tmpJobStatus = WebInputUtils.cloneJobStatus(getJobStatus());

		/**
		 * @author serkan taş
		 * @date 14.07.2013 Aşağıdaki kımsa gerek yok gibi geldi bana
		 */
		// if (isScenario) {
		// addToScenarioStatusList(tmpJobStatus);
		// } else {
		addToJobStatusList(tmpJobStatus);
		// }

		if (getManyJobStatusList() == null) {
			setManyJobStatusList(new ArrayList<SelectItem>());
		}

		getManyJobStatusList().add(new SelectItem(getJobStatusName(), getJobStatusName()));

		addMessage("addReturnCode", FacesMessage.SEVERITY_INFO, "tlos.info.job.code.add", null);

		setStatusDialogShow(false);
	}
	
	public void addJReturnCodeAction() {
		addJReturnCodeAction(false, jobBasePanelBean.getJobStatusList());
	}

	public void addJReturnCodeAction(boolean isScenario, Object refObject) {

		// TODO donus kodu eklerken ayni is donus statusu icin ayni isletim
		// sistemi secilerek
		// ayni kod birden fazla tanimlanabiliyor
		// bu kontrol yapilip ayni kodun eklenmesi engellenecek

		// guncelleme icin acildiginda duplicate kontrolunu yapmiyor
		if (jobStatus.getStsId() == null || jobStatus.getStsId().equals("")) {
			if (!checkDuplicateStateName()) {
				statusDialogShow = true;
			} else {
				statusDialogShow = false;

				return;
			}
		}

		jobStatus.setStatusName(StatusName.Enum.forString(jobStatusName));

		ReturnCode tmpReturnCode = WebInputUtils.cloneReturnCode(getReturnCode());

		// girilen statu icin onceden kayit yapilmamissa gerekli bilesenler
		// olusturuluyor
		if (jobStatus.getReturnCodeListArray() == null || jobStatus.sizeOfReturnCodeListArray() == 0) {
			ReturnCodeList returnCodeList = jobStatus.addNewReturnCodeList();
			returnCodeList.setOsType(OsType.Enum.forString(osType));

			tmpReturnCode.setCdId("1");

			ReturnCode returnCode = (jobStatus.getReturnCodeListArray()[0]).addNewReturnCode();
			returnCode.set(tmpReturnCode);

			// girilen statu icin onceden kayit yapilmissa, girilen isletim
			// sistemi icin onceden kayit yapilmis mi diye kontrol ediyor
		} else {
			boolean osIsDefined = false;

			for (int j = 0; j < jobStatus.sizeOfReturnCodeListArray(); j++) {
				ReturnCodeList returnCodeList = jobStatus.getReturnCodeListArray()[j];

				if (returnCodeList.getOsType().toString().toLowerCase().equals(osType.toLowerCase())) {
					int lastElementIndex = returnCodeList.getReturnCodeArray().length - 1;

					String maxId = returnCodeList.getReturnCodeArray()[lastElementIndex].getCdId();

					tmpReturnCode.setCdId((Integer.parseInt(maxId) + 1) + "");

					returnCodeList.setOsType(OsType.Enum.forString(osType));

					ReturnCode returnCode = returnCodeList.addNewReturnCode();
					returnCode.set(tmpReturnCode);

					osIsDefined = true;
				}
			}

			// girilen isletim sistemi tanimlanmamissa gerekli bilesenler
			// olusturuluyor
			if (!osIsDefined) {
				ReturnCodeList returnCodeList = jobStatus.addNewReturnCodeList();
				returnCodeList.setOsType(OsType.Enum.forString(osType));

				tmpReturnCode.setCdId("1");

				ReturnCode returnCode = returnCodeList.addNewReturnCode();
				returnCode.set(tmpReturnCode);
			}

			if (isScenario) {
				ScenarioStatusList scenarioStatusList = (ScenarioStatusList) refObject;
				// hazirlanan status nesnesi scenariostatusList icine koyuluyor
				for (int i = 0; i < scenarioStatusList.sizeOfScenarioStatusArray(); i++) {
					if (scenarioStatusList.getScenarioStatusArray(i).getStatusName().equals(jobStatus.getStatusName())) {
						scenarioStatusList.getScenarioStatusArray(i).set(jobStatus);
					}
				}
			} else {
				JobStatusList jobStatusList = (JobStatusList) refObject;
				// hazirlanan job status nesnesi jobstatusList icine koyuluyor
				for (int i = 0; i < jobStatusList.sizeOfJobStatusArray(); i++) {
					if (jobStatusList.getJobStatusArray(i).getStatusName().equals(jobStatus.getStatusName())) {
						jobStatusList.getJobStatusArray(i).set(jobStatus);
					}
				}
			}
		}

		if (manyReturnCodeList == null) {
			manyReturnCodeList = new ArrayList<SelectItem>();
		}

		// islem yapilan job icin onceden herhangi bir statu tanimi yapilmis mi
		// diye kontrol ediyor, yapilmamissa job tanimindaki gerekli bilesenleri
		// ekliyor
		if (isScenario) {
			ScenarioStatusList scenarioStatusList = (ScenarioStatusList) refObject;
			if (scenarioStatusList == null || scenarioStatusList.sizeOfScenarioStatusArray() == 0) {
				scenarioStatusList = ScenarioStatusList.Factory.newInstance();

				Status status = scenarioStatusList.addNewScenarioStatus();
				status.set(jobStatus);
			}
		} else {
			JobStatusList jobStatusList = (JobStatusList) refObject;
			if (jobStatusList == null || jobStatusList.sizeOfJobStatusArray() == 0) {
				jobStatusList = JobStatusList.Factory.newInstance();

				Status status = jobStatusList.addNewJobStatus();
				status.set(jobStatus);
			}
		}

		SelectItem item = new SelectItem();
		item.setValue(tmpReturnCode.getCode());
		item.setLabel(osType + " : " + tmpReturnCode.getCode() + " -> " + jobStatusName);

		manyReturnCodeList.add(item);
	}

	public void deleteJReturnCodeAction() {
		if (selectedReturnCodeList.length == 0) {
			addMessage("addReturnCode", FacesMessage.SEVERITY_ERROR, "tlos.info.job.code.delete", null);
			return;
		}

		for (int i = 0; i < selectedReturnCodeList.length; i++) {
			for (int j = 0; j < getManyReturnCodeList().size(); j++) {
				if (getManyReturnCodeList().get(j).getValue().toString().equals(selectedReturnCodeList[i])) {

					// TODO job tanimi ya da senaryo tanimi icinden silme isi
					// burada yapilacak,
					// asagida sadece goruntu olarak ekrandaki listeden siliyor

					getManyReturnCodeList().remove(j);
					j = getManyReturnCodeList().size();
				}
			}
		}
	}

	public List<SelectItem> getManyJobStatusList() {
		return manyJobStatusList;
	}

	public void setManyJobStatusList(List<SelectItem> manyJobStatusList) {
		this.manyJobStatusList = manyJobStatusList;
	}

	public String[] getSelectedJobStatusList() {
		return selectedJobStatusList;
	}

	public void setSelectedJobStatusList(String[] selectedJobStatusList) {
		this.selectedJobStatusList = selectedJobStatusList;
	}

	public boolean isStatusDialogShow() {
		return statusDialogShow;
	}

	public void setStatusDialogShow(boolean statusDialogShow) {
		this.statusDialogShow = statusDialogShow;
	}

	public ReturnCode getReturnCode() {
		return returnCode;
	}

	public void setReturnCode(ReturnCode returnCode) {
		this.returnCode = returnCode;
	}

	public List<SelectItem> getManyReturnCodeList() {
		return manyReturnCodeList;
	}

	public void setManyReturnCodeList(List<SelectItem> manyReturnCodeList) {
		this.manyReturnCodeList = manyReturnCodeList;
	}

	public Status getJobStatus() {
		return jobStatus;
	}

	public void setJobStatus(Status jobStatus) {
		this.jobStatus = jobStatus;
	}

	public String getJobStatusName() {
		return jobStatusName;
	}

	public void setJobStatusName(String jobStatusName) {
		this.jobStatusName = jobStatusName;
	}

	public Collection<SelectItem> getJobStatusNameList() {
		return jobBasePanelBean.getJobStatusNameList();
	}

	public Collection<SelectItem> getoSystemList() {
		return jobBasePanelBean.getoSystemList();
	}

	public String getoSystem() {
		return oSystem;
	}

	public void setoSystem(String oSystem) {
		this.oSystem = oSystem;
	}

	public String getOsType() {
		return osType;
	}

	public void setOsType(String osType) {
		this.osType = osType;
	}

	public boolean isJsUpdateButton() {
		return jsUpdateButton;
	}

	public void setJsUpdateButton(boolean jsUpdateButton) {
		this.jsUpdateButton = jsUpdateButton;
	}

	public String[] getSelectedReturnCodeList() {
		return selectedReturnCodeList;
	}

	public void setSelectedReturnCodeList(String[] selectedReturnCodeList) {
		this.selectedReturnCodeList = selectedReturnCodeList;
	}

}
