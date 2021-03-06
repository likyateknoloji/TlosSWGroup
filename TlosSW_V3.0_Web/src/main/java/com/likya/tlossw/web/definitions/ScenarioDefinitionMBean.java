package com.likya.tlossw.web.definitions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;
import javax.xml.namespace.QName;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlOptions;
import org.primefaces.context.RequestContext;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import com.likya.tlos.model.xmlbeans.common.AgentChoiceMethodDocument.AgentChoiceMethod;
import com.likya.tlos.model.xmlbeans.common.ChoiceType;
import com.likya.tlos.model.xmlbeans.common.SchedulingAlgorithmDocument.SchedulingAlgorithm;
import com.likya.tlos.model.xmlbeans.data.AdvancedScenarioInfosDocument.AdvancedScenarioInfos;
import com.likya.tlos.model.xmlbeans.data.BaseScenarioInfosDocument.BaseScenarioInfos;
import com.likya.tlos.model.xmlbeans.data.ConcurrencyManagementDocument.ConcurrencyManagement;
import com.likya.tlos.model.xmlbeans.data.DependencyListDocument.DependencyList;
import com.likya.tlos.model.xmlbeans.data.JobListDocument.JobList;
import com.likya.tlos.model.xmlbeans.data.ManagementDocument.Management;
import com.likya.tlos.model.xmlbeans.data.ScenarioDocument.Scenario;
import com.likya.tlossw.model.DocMetaDataHolder;
import com.likya.tlossw.model.tree.WsScenarioNode;
import com.likya.tlossw.utils.CommonConstantDefinitions;
import com.likya.tlossw.utils.xml.XMLNameSpaceTransformer;
import com.likya.tlossw.web.menu.JobTemplatesTree;
import com.likya.tlossw.web.model.JSBuffer;
import com.likya.tlossw.web.tree.JSTree;
import com.likya.tlossw.web.utils.ConstantDefinitions;

@ManagedBean(name = "scenarioDefinitionMBean")
@ViewScoped
public class ScenarioDefinitionMBean extends JSBasePanelMBean implements Serializable {

	private static final long serialVersionUID = -8027723328721838174L;

	@ManagedProperty(value = "#{jSTree}")
	private JSTree jsTree;
	
	@ManagedProperty(value = "#{jobTemplatesTree}")
	private JobTemplatesTree jobTemplatesTree;

	private Scenario scenario;

	private String scenarioName;
	private String comment;

	private boolean useCalendarDef = false;

	private Collection<SelectItem> schedulingAlgorithmList = null;
	private String selectedSchedulingAlgorithm;

	private String treePath;
	private String scenarioPath;

	// senaryonun senaryo ağacındaki pathinin sadece "isim | id" ve "/" ile oluşturulmuş hali
	private String scenarioPathInScenario;

	public void init() {

		super.init();

		setScenario(true);
		setJsInsertButton(true);
		initScenarioPanel();

		fillSchedulingAlgorithmList();
		fillConcurrencyManagement();
	}

	public void initScenarioPanel() {

		setScenario(Scenario.Factory.newInstance());

		BaseScenarioInfos baseScenarioInfos = BaseScenarioInfos.Factory.newInstance();
		getScenario().setBaseScenarioInfos(baseScenarioInfos);

		JobList jobList = JobList.Factory.newInstance();
		getScenario().setJobList(jobList);

		Management management = Management.Factory.newInstance();
		getScenario().setManagement(management);

		ConcurrencyManagement concurrencyManagement = ConcurrencyManagement.Factory.newInstance();
		getScenario().getManagement().setConcurrencyManagement(concurrencyManagement);

		resetScenarioPanelInputs();
	}

	private void resetScenarioPanelInputs() {

		super.resetPanelInputs();

		scenarioName = "";
		comment = "";
		useCalendarDef = false;
		selectedSchedulingAlgorithm = SchedulingAlgorithm.FIRST_COME_FIRST_SERVED.toString();
	}

	protected void fillConcurrencyManagement() {
		scenario.getManagement().getConcurrencyManagement().setConcurrent(isConcurrent());
	}

	protected void fillManagement() {

		Management management;

		if (!getManagementTabBean().isUseManagement() || scenario == null) {
			return;
		}

		management = scenario.getManagement();
		getManagementTabBean().fillManagement(management);
		
		scenario.getManagement().getConcurrencyManagement().setConcurrent(isConcurrent());
	}

	public void fillManagementTab() {

		Management management = null;

		if (scenario != null) {
			management = scenario.getManagement();
			getManagementTabBean().fillManagementTab(management);
		}

		if (getScenario() != null) {
			setConcurrent(getScenario().getManagement().getConcurrencyManagement().getConcurrent());
		} else {
			System.out.println("scenario is NULL in fillConcurrencyManagementTab !!");
		}
	}

	protected void fillAlarmPreference() {
		getAlarmPreferencesTabBean().fillAlarmPreference(true, scenario);
	}

	public void fillAlarmPreferenceTab() {
		getAlarmPreferencesTabBean().fillAlarmPreferenceTab(true, scenario);
	}

	protected void fillLocalParameters() {
		getLocalParametersTabBean().fillLocalParameters(true, scenario);
	}

	public void fillLocalParametersTab() {
		getLocalParametersTabBean().fillLocalParametersTab(true, scenario);
	}

	private void fillSchedulingAlgorithmList() {
		String algorithm = null;
		schedulingAlgorithmList = new ArrayList<SelectItem>();

		for (int i = 0; i < SchedulingAlgorithm.Enum.table.lastInt(); i++) {
			SelectItem item = new SelectItem();
			algorithm = SchedulingAlgorithm.Enum.forInt(i + 1).toString();
			item.setValue(algorithm);
			item.setLabel(algorithm);
			schedulingAlgorithmList.add(item);
		}
	}

	// sağ tıkla yeni senaryo ekle seçilince buraya geliyor
	public void addNewScenario() {
		init();

		setJsInsertButton(true);
		setJsUpdateButton(false);

		TreeNode selectedScenario = getJsTree().getSelectedJS();
		setScenarioTreePath(selectedScenario);
	}

	public void insertJsAction() {
		fillScenarioProperties();
		insertScenarioDefinition();
	}

	public void updateJsAction() {
		fillScenarioProperties();
		updateScenarioDefinition();
	}

	// ekrandan girilen degerler scenario icine dolduruluyor
	public void fillScenarioProperties() {
		fillBaseScenarioInfos();
		fillManagement();
		// şimdilik senaryolar arası bağımlılık yok
		// fillDependencyDefinitions();
		fillStateInfos();
		//fillConcurrencyManagement();
		fillAlarmPreference();
		fillLocalParameters();
		fillAdvancedScenarioInfos();
	}

	private void fillBaseScenarioInfos() {
		BaseScenarioInfos baseScenarioInfos = getScenario().getBaseScenarioInfos();

		baseScenarioInfos.setJsName(scenarioName);
		baseScenarioInfos.setComment(comment);

		if (isJsActive()) {
			baseScenarioInfos.setJsIsActive(true);
		} else {
			baseScenarioInfos.setJsIsActive(false);
		}

		baseScenarioInfos.setUserId(getWebAppUser().getId());
	}

	/*
	 * private void fillDependencyDefinitions() { // son durumda bagimlik tanimlanmamissa senaryo icindeki ilgili kismi // kaldiriyor if (getScenario().getDependencyList() != null && getScenario().getDependencyList().getItemArray().length == 0) { XmlCursor xmlCursor = getScenario().getDependencyList().newCursor(); xmlCursor.removeXml(); } }
	 */
	private void fillStateInfos() {
		// son durumda statu kodu tanimlanmamissa senaryo icindeki
		// ilgili kismi kaldiriyor
		if (getScenario().getScenarioStatusList() != null && getScenario().getScenarioStatusList().sizeOfScenarioStatusArray() == 0) {
			XmlCursor xmlCursor = getScenario().getScenarioStatusList().newCursor();
			xmlCursor.removeXml();
		}
	}

	private void fillAdvancedScenarioInfos() {
		AdvancedScenarioInfos advancedScenarioInfos = AdvancedScenarioInfos.Factory.newInstance();

		AgentChoiceMethod choiceMethod = AgentChoiceMethod.Factory.newInstance();
		choiceMethod.setStringValue(getAdvancedJobInfosTab().getAgentChoiceMethod());

		if (getAdvancedJobInfosTab().getAgentChoiceMethod().equals(ChoiceType.USER_MANDATORY_PREFERENCE.toString())) {
			choiceMethod.setAgentId(getAdvancedJobInfosTab().getSelectedAgent());
		}
		advancedScenarioInfos.setAgentChoiceMethod(choiceMethod);

		advancedScenarioInfos.setSchedulingAlgorithm(SchedulingAlgorithm.Enum.forString(selectedSchedulingAlgorithm));

		getScenario().setAdvancedScenarioInfos(advancedScenarioInfos);
	}

	public void insertJsWithDuplicateName(ActionEvent actionEvent) {
		insertScenarioDefinition();
	}

	public void updateJsWithDuplicateName(ActionEvent actionEvent) {
		updateScenarioDefinition();
	}

	public void insertScenarioDefinition() {
		if (!isJsNameConfirmDialog()) {
			if (!scenarioCheckUp() & getScenarioId()) {
				return;
			}
		} else {
			setJsNameConfirmDialog(false);
		}

		String docId = getDocId( DocMetaDataHolder.SECOND_COLUMN );
		
		if (getDbOperations().insertScenario( docId, getWebAppUser().getId(), getSessionMediator().getDocumentScope(docId), getScenarioXML(), scenarioPath)) {

			WsScenarioNode swScenarioNode = new WsScenarioNode();
			swScenarioNode.setName(scenarioName);
			swScenarioNode.setId(getScenario().getID());

			TreeNode scenarioNode = new DefaultTreeNode("scenario", swScenarioNode, getJsTree().getSelectedJS());
			scenarioNode.setExpanded(true);

			RequestContext context = RequestContext.getCurrentInstance();
			context.update("jsTreeForm:tree");

			addMessage("scenarioInsert", FacesMessage.SEVERITY_INFO, "tlos.success.scenario.insert", null);

			switchInsertUpdateButtons();

		} else {
			addMessage("scenarioInsert", FacesMessage.SEVERITY_ERROR, "tlos.error.scenario.insert", null);
		}
	}

	public void updateScenarioDefinition() {
		if (!isJsNameConfirmDialog()) {
			if (!scenarioCheckUpForUpdate()) {
				return;
			}
		} else {
			setJsNameConfirmDialog(false);
		}

		String docId = getDocId( DocMetaDataHolder.SECOND_COLUMN );
		
		if (getDbOperations().updateScenario( docId, getWebAppUser().getId(), getSessionMediator().getDocumentScope(docId), scenarioPath, getScenarioXML())) {
			jsTree.initJSTree();

			addMessage("scenarioUpdate", FacesMessage.SEVERITY_INFO, "tlos.success.scenario.update", null);
		} else {
			addMessage("scenarioUpdate", FacesMessage.SEVERITY_ERROR, "tlos.error.scenario.update", null);
		}
	}

	public void initializeScenarioPanel(boolean insert) {
		TreeNode selectedScenario = getJsTree().getSelectedJS();

		setJsInsertButton(insert);
		setJsUpdateButton(!insert);

		setScenarioTreePath(selectedScenario);

		resetScenarioPanelInputs();
		fillScenarioPanel();
	}

	public void setTemplateScenarioPath(boolean insert) {
		TreeNode selectedScenario = getJobTemplatesTree().getSelectedJS();

		setJsInsertButton(insert);
		setJsUpdateButton(!insert);

		setTemplateScenarioTreePath(selectedScenario);
	}

	public void fillScenarioPanel() {
		fillBaseScenarioInfosTab();
		fillManagementTab();
		// şimdilik senaryolar arası bağımlılık yok
		// fillDependencyDefinitionsTab();
		// fillStateInfosTab();
		fillConcurrencyManagementTab();
		fillAlarmPreferenceTab();
		fillLocalParametersTab();
		fillAdvancedScenarioInfosTab();
	}

	private void fillBaseScenarioInfosTab() {
		if (getScenario() != null) {
			BaseScenarioInfos baseScenarioInfos = getScenario().getBaseScenarioInfos();

			scenarioName = baseScenarioInfos.getJsName();
			comment = baseScenarioInfos.getComment();

			if (baseScenarioInfos.getJsIsActive()) {
				setJsActive(true);
			} else {
				setJsActive(false);
			}

		} else {
			System.out.println("scenario is NULL in fillBaseScenarioInfosTab !!");
		}
	}

	/**
	 * @author serkan
	 * 
	 * Aşağıdaki kısmı kaldırdım, çünkü state yapısı job için kurgulanmış.
	 *  Buna uyum sqğlamıyor.
	 *  
	 * 20.07.2013
	 */
	
//	private void fillStateInfosTab() {
//		if (getScenario() != null) {
//			// durum tanimi yapildiysa alanlari dolduruyor
//			if (getScenario().getScenarioStatusList() != null) {
//
//				getStateInfosTabBean().setManyJobStatusList(new ArrayList<SelectItem>());
//				for (Status scenarioStatus : getScenario().getScenarioStatusList().getScenarioStatusArray()) {
//					String statusName = scenarioStatus.getStatusName().toString();
//					getStateInfosTabBean().getManyJobStatusList().add(new SelectItem(statusName, statusName));
//				}
//			} else {
//				getStateInfosTabBean().setManyJobStatusList(null);
//			}
//		} else {
//			System.out.println("scenario is NULL in fillStateInfosTab !!");
//		}
//	}

	public void fillConcurrencyManagementTab() {
		if (getScenario() != null) {
			setConcurrent(getScenario().getManagement().getConcurrencyManagement().getConcurrent());
		} else {
			System.out.println("scenario is NULL in fillConcurrencyManagementTab !!");
		}
	}

	private void fillAdvancedScenarioInfosTab() {
		if (getScenario() != null) {
			if (getScenario().getAdvancedScenarioInfos() == null) {
				return;
			}

			AdvancedScenarioInfos advancedScenarioInfos = getScenario().getAdvancedScenarioInfos();

			// agent secme metodu
			if (advancedScenarioInfos.getAgentChoiceMethod() != null) {
				getAdvancedJobInfosTab().setAgentChoiceMethod(advancedScenarioInfos.getAgentChoiceMethod().getStringValue());

				if (getAdvancedJobInfosTab().getAgentChoiceMethod().equals(ChoiceType.USER_MANDATORY_PREFERENCE.toString())) {
					getAdvancedJobInfosTab().setSelectedAgent(advancedScenarioInfos.getAgentChoiceMethod().getAgentId());
				}
			}

			setSelectedSchedulingAlgorithm(advancedScenarioInfos.getSchedulingAlgorithm().toString());

		} else {
			System.out.println("scenario is NULL in fillAdvancedScenarioInfosTab !!");
		}
	}

	private boolean scenarioCheckUpForUpdate() {
		
		String docId = getDocId( DocMetaDataHolder.SECOND_COLUMN );
		
		String scenarioCheckResult = getDbOperations().getScenarioExistence( docId, getWebAppUser().getId(), getSessionMediator().getDocumentScope(docId), treePath, scenarioName);

		// bu isimde bir senaryo yoksa 0
		// ayni path de aynı isimde bir senaryo varsa 1
		// iç senaryolarda aynı isimde bir senaryo varsa 2
		// senaryonun dışında aynı isimde bir senaryo varsa 3
		if (scenarioCheckResult != null) {
			if (scenarioCheckResult.equalsIgnoreCase(ConstantDefinitions.DUPLICATE_NAME_AND_PATH)) {

				Scenario scenarioDefinition = getDbOperations().getScenario( docId, getWebAppUser().getId(), getSessionMediator().getDocumentScope(docId), scenarioPath, scenarioName);

				// id aynı ise kendi adını değiştirmeden güncellediği için uyarı vermiyor
				if (scenarioDefinition == null || !scenarioDefinition.getID().equals(getScenario().getID())) {
					addMessage("scenarioUpdate", FacesMessage.SEVERITY_ERROR, "tlos.info.scenario.name.duplicate", null);
					return false;
				}
			} else if (scenarioCheckResult.equalsIgnoreCase(ConstantDefinitions.INNER_DUPLICATE_NAME)) {
				setJsNameConfirmDialog(true);
				setInnerJsNameDuplicate(true);

				return false;

			} else if (scenarioCheckResult.equalsIgnoreCase(ConstantDefinitions.OUTER_DUPLICATE_NAME)) {
				setJsNameConfirmDialog(true);
				setInnerJsNameDuplicate(false);

				return false;
			}
		}

		return true;
	}

	private boolean scenarioCheckUp() {
		
		String docId = getDocId( DocMetaDataHolder.SECOND_COLUMN );
		
		String scenarioCheckResult = getDbOperations().getScenarioExistence( docId, getWebAppUser().getId(), getSessionMediator().getDocumentScope(docId), scenarioPath, scenarioName);

		// bu isimde bir senaryo yoksa 0
		// ayni path de aynı isimde bir senaryo varsa 1
		// iç senaryolarda aynı isimde bir senaryo varsa 2
		// senaryonun dışında aynı isimde bir senaryo varsa 3
		if (scenarioCheckResult != null) {
			if (scenarioCheckResult.equalsIgnoreCase(ConstantDefinitions.DUPLICATE_NAME_AND_PATH)) {
				addMessage("scenarioInsert", FacesMessage.SEVERITY_ERROR, "tlos.info.scenario.name.duplicate", null);
				return false;
			} else if (scenarioCheckResult.equalsIgnoreCase(ConstantDefinitions.INNER_DUPLICATE_NAME)) {
				setJsNameConfirmDialog(true);
				setInnerJsNameDuplicate(true);

				return false;

			} else if (scenarioCheckResult.equalsIgnoreCase(ConstantDefinitions.OUTER_DUPLICATE_NAME)) {
				setJsNameConfirmDialog(true);
				setInnerJsNameDuplicate(false);

				return false;
			}
		}

		return true;
	}

	private boolean getScenarioId() {
		int scenarioId = getDbOperations().getNextId(CommonConstantDefinitions.SCENARIO_ID);

		if (scenarioId < 0) {
			addMessage("scenarioInsert", FacesMessage.SEVERITY_ERROR, "tlos.error.scenario.getId", null);
			return false;
		}
		getScenario().setID(scenarioId + "");

		return true;
	}

	private String getScenarioXML() {
		QName qName = Scenario.type.getOuterType().getDocumentElementName();
		XmlOptions xmlOptions = XMLNameSpaceTransformer.transformXML(qName);
		String scenarioXML = getScenario().xmlText(xmlOptions);

		return scenarioXML;
	}

	private void setScenarioTreePath(TreeNode scenarioNode) {

		WsScenarioNode wsScenarioParentNode = (WsScenarioNode) scenarioNode.getParent().getData();

		WsScenarioNode wsScenarioNode = (WsScenarioNode) scenarioNode.getData();
		String path = "";
		treePath = ""; // Odaktaki senaryo path i
		scenarioPathInScenario = ""; // Ekrandaki ağaçtaki node isimleri path i

		// yeni kayıtta tıklanan yeri koruyor, güncellemede bir üstünden itibaren path'i tutuyor
		if (isJsUpdateButton()) {
			scenarioName = wsScenarioNode.getName(); // DefinitionUtils.getXFromNameId(scenarioPathInScenario, "Name");
		}

		if (!wsScenarioParentNode.getId().equalsIgnoreCase(ConstantDefinitions.TREE_ROOTID)) { // Seçilen nodun atası root
			String scenarioNodeId = ((WsScenarioNode) scenarioNode.getData()).getId(); // Referans senaryo nodunun id si

			path = "/dat:scenario[@ID = '" + scenarioNodeId + "']";
			scenarioPathInScenario = scenarioPathInScenario + "/" + scenarioNodeId;
			while (scenarioNode.getParent() != null && !((WsScenarioNode) scenarioNode.getParent().getData()).getId().equals(ConstantDefinitions.TREE_SCENARIOROOTID)) {
				scenarioNode = scenarioNode.getParent();
				scenarioNodeId = ((WsScenarioNode) scenarioNode.getData()).getId();

				treePath = "/dat:scenario[@ID = '" + scenarioNodeId + "']" + treePath;
				scenarioPathInScenario = "/" + scenarioNodeId + scenarioPathInScenario;
			}
		}

		scenarioPathInScenario = "/" + ConstantDefinitions.TREE_SCENARIOROOTID + scenarioPathInScenario;
		treePath = "/dat:TlosProcessData" + treePath;

		scenarioPath = treePath + path; // Odaktaki senaryo path i full
	}

	private void setTemplateScenarioTreePath(TreeNode scenarioNode) {

		WsScenarioNode wsScenarioNode = (WsScenarioNode) scenarioNode.getData();
		String path = "";
		treePath = ""; // Odaktaki senaryo path i
		scenarioPathInScenario = ""; // Ekrandaki ağaçtaki node isimleri path i

		if (!wsScenarioNode.getId().equalsIgnoreCase(ConstantDefinitions.TREE_ROOTID)) { // Seçilen nodun atası root
			String scenarioNodeId = ((WsScenarioNode) scenarioNode.getData()).getId(); // Referans senaryo nodunun id si

			path = "/dat:scenario[@ID = '" + scenarioNodeId + "']";
			scenarioPathInScenario = scenarioPathInScenario + "/" + scenarioNodeId;
			while (scenarioNode.getParent() != null && !((WsScenarioNode) scenarioNode.getParent().getData()).getId().equals(ConstantDefinitions.TREE_ROOTID)) {
				scenarioNode = scenarioNode.getParent();
				scenarioNodeId = ((WsScenarioNode) scenarioNode.getData()).getId();

				treePath = "/dat:scenario[@ID = '" + scenarioNodeId + "']" + treePath;
				scenarioPathInScenario = "/" + scenarioNodeId + scenarioPathInScenario;
			}
		}

		scenarioPathInScenario = "/" + ConstantDefinitions.TREE_SCENARIOROOTID + scenarioPathInScenario;
		treePath = "/dat:TlosProcessData" + treePath;

		scenarioPath = treePath + path; // Odaktaki senaryo path i full
	}

	// senaryoya sağ tıklayarak sil dediğimizde buraya geliyor
	public boolean deleteScenario() {
		boolean result = true;
		
		String docId = getDocId( DocMetaDataHolder.SECOND_COLUMN );
		
		if (getDbOperations().deleteScenario( docId, getWebAppUser().getId(), getSessionMediator().getDocumentScope(docId), scenarioPath, getScenarioXML())) {
			removeScenarioSubtree(scenarioPathInScenario);
			addMessage("scenarioDelete", FacesMessage.SEVERITY_INFO, "tlos.success.scenario.delete", null);
		} else {
			addMessage("scenarioDelete", FacesMessage.SEVERITY_ERROR, "tlos.error.scenario.delete", null);
			result = false;
		}

		return result;
	}

	// silinen senaryoyu ağaçtan kaldırıyor
	public void removeScenarioSubtree(String scenarioPath) {
		getJsTree().removeScenarioSubtree(scenarioPath);
	}

	public void fillDependencyDefinitionsTab() {

		DependencyList dependencyList = null;

		if (scenario != null && scenario.getDependencyList() != null) {
			dependencyList = scenario.getDependencyList();
			super.fillDependencyDefinitionsTab(dependencyList);
		}

	}

	public void copyScenario(String fromTree) {
		JSBuffer jsBuffer = new JSBuffer();
		jsBuffer.setJob(false);
		jsBuffer.setFromDocId( getSessionMediator().getCurrentDoc(Integer.valueOf(fromTree)) );
		jsBuffer.setFromScope( getSessionMediator().getScope(Integer.valueOf(fromTree)) );
		jsBuffer.setJsId(scenario.getID());
		jsBuffer.setJsName(scenario.getBaseScenarioInfos().getJsName());
	
		getSessionMediator().setJsBuffer(jsBuffer);
	}

	public void pasteJS(String toTree, String purpose) {
		JSBuffer jsBuffer = getSessionMediator().getJsBuffer();
		jsBuffer.setToDocId( getSessionMediator().getCurrentDoc(Integer.valueOf(toTree)) );
		jsBuffer.setToScope( getSessionMediator().getScope(Integer.valueOf(toTree)) );

//		if (jsBuffer.getFromDocId().equals(CommonConstantDefinitions.EXIST_DEPLOYMENTDATA) 
//				&& getSessionMediator().getScopeText(jsBuffer.getFromScope()).equals(CommonConstantDefinitions.EXIST_MYDATA)) {
//			scenarioPath = "/dat:TlosProcessData";
//		}

		if (getDbOperations().copyJSToJS(jsBuffer.getFromDocId(), jsBuffer.getToDocId(), jsBuffer.getFromScope(), jsBuffer.getToScope(), getWebAppUser().getId(), jsBuffer.isJob(), jsBuffer.getJsId(), scenarioPath, jsBuffer.getNewJSName())) {
			addMessage( purpose, FacesMessage.SEVERITY_INFO, "tlos.success.js."+purpose, null);
		} else {
			addMessage( purpose, FacesMessage.SEVERITY_ERROR, "tlos.error.js."+purpose, null);
		}
	}

	/*
	public void updateJobStatusAction() {

		Status[] statusArray = null;

		if (scenario != null) {
			statusArray = scenario.getScenarioStatusList().getScenarioStatusArray();
			getStateInfosTabBean().updateJobStatusAction(statusArray);
		}

	}

	public void jobStatusEditAction() {

		Status[] statusArray = null;

		if (scenario != null) {
			statusArray = scenario.getScenarioStatusList().getScenarioStatusArray();
			getStateInfosTabBean().jobStatusEditAction(statusArray);
		}
	}

	public void deleteStatusAction() {
		for (int i = 0; i < getStateInfosTabBean().getSelectedJobStatusList().length; i++) {
			for (int j = 0; j < getStateInfosTabBean().getManyJobStatusList().size(); j++) {
				if (getStateInfosTabBean().getManyJobStatusList().get(j).getValue().equals(getStateInfosTabBean().getSelectedJobStatusList()[i])) {

					ScenarioStatusList scenarioStatusList = scenario.getScenarioStatusList();

					for (int k = 0; k < scenarioStatusList.sizeOfScenarioStatusArray(); k++) {
						if (getStateInfosTabBean().getManyJobStatusList().get(j).getValue().equals(scenarioStatusList.getScenarioStatusArray(k).getStatusName().toString())) {
							scenarioStatusList.removeScenarioStatus(k);
							k = scenarioStatusList.sizeOfScenarioStatusArray();
						}
					}
					getStateInfosTabBean().getManyJobStatusList().remove(j);
					j = getStateInfosTabBean().getManyJobStatusList().size();
				}
			}
		}
	}
	*/

	public JSTree getJsTree() {
		return jsTree;
	}

	public void setJsTree(JSTree jsTree) {
		this.jsTree = jsTree;
	}

	public String getScenarioName() {
		return scenarioName;
	}

	public void setScenarioName(String scenarioName) {
		this.scenarioName = scenarioName;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public boolean isUseCalendarDef() {
		return useCalendarDef;
	}

	public void setUseCalendarDef(boolean useCalendarDef) {
		this.useCalendarDef = useCalendarDef;
	}

	public Collection<SelectItem> getSchedulingAlgorithmList() {
		return schedulingAlgorithmList;
	}

	public void setSchedulingAlgorithmList(Collection<SelectItem> schedulingAlgorithmList) {
		this.schedulingAlgorithmList = schedulingAlgorithmList;
	}

	public String getSelectedSchedulingAlgorithm() {
		return selectedSchedulingAlgorithm;
	}

	public void setSelectedSchedulingAlgorithm(String selectedSchedulingAlgorithm) {
		this.selectedSchedulingAlgorithm = selectedSchedulingAlgorithm;
	}

	public String getScenarioPathInScenario() {
		return scenarioPathInScenario;
	}

	public void setScenarioPathInScenario(String scenarioPathInScenario) {
		this.scenarioPathInScenario = scenarioPathInScenario;
	}

	public String getScenId() {
		return getScenario().getID();
	}

	public Scenario getScenario() {
		return scenario;
	}

	public void setScenario(Scenario scenario) {
		this.scenario = scenario;
	}

	public String getScenarioPath() {
		return scenarioPath;
	}

	public void setScenarioPath(String scenarioPath) {
		this.scenarioPath = scenarioPath;
	}

	public JobTemplatesTree getJobTemplatesTree() {
		return jobTemplatesTree;
	}

	public void setJobTemplatesTree(JobTemplatesTree jobTemplatesTree) {
		this.jobTemplatesTree = jobTemplatesTree;
	}

}
