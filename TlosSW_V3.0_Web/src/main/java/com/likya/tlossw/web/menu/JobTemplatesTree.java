package com.likya.tlossw.web.menu;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.primefaces.event.NodeCollapseEvent;
import org.primefaces.event.NodeExpandEvent;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.event.NodeUnselectEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import com.likya.tlos.model.xmlbeans.data.JobPropertiesDocument.JobProperties;
import com.likya.tlos.model.xmlbeans.data.ScenarioDocument.Scenario;
import com.likya.tlos.model.xmlbeans.data.TlosProcessDataDocument.TlosProcessData;
import com.likya.tlossw.model.DocMetaDataHolder;
import com.likya.tlossw.model.tree.WsJobNode;
import com.likya.tlossw.model.tree.WsScenarioNode;
import com.likya.tlossw.utils.CommonConstantDefinitions;
import com.likya.tlossw.web.TreeBaseBean;
import com.likya.tlossw.web.db.DBOperations;
import com.likya.tlossw.web.live.ScenarioMBean;
import com.likya.tlossw.web.utils.ComboListUtils;
import com.likya.tlossw.web.utils.ConstantDefinitions;

@ManagedBean
@ViewScoped
public class JobTemplatesTree  extends TreeBaseBean implements Serializable {
	
	private static final long serialVersionUID = -8098932616833921105L;

	@ManagedProperty(value = "#{dbOperations}")
	private DBOperations dbOperations;
	
	@ManagedProperty(value = "#{scenarioMBean}")
	private ScenarioMBean scenarioMBean;
	
	private TreeNode root;

	private TreeNode selectedJS;

	private TreeNode selectedTreeNode;

	@PostConstruct
	public void initJSTree() {
		
		long startTime = System.currentTimeMillis();
		
		setPassedParameters();
		
		TlosProcessData tlosProcessData = getTlosProcessData(CommonConstantDefinitions.EXIST_TEMPLATEDATA, getDocumentScope(CommonConstantDefinitions.EXIST_TEMPLATEDATA));
		
		ComboListUtils.logTimeInfo("JobTemplatesTree.initJSTree.dbOperations.getTlosTemplateDataXml() Süre : " , startTime);

		startTime = System.currentTimeMillis();
		System.out.println("Job Template Tree olusturuluyor ..");
		constructJSTree(tlosProcessData);
		ComboListUtils.logTimeInfo("JobTemplatesTree.initJSTree.dbOperations.getTlosTemplateDataXml() Süre : ", startTime);
		
		// addMessage("Job Template Tree olusturuldu !");
		
	}

	public void constructJSTree(TlosProcessData tlosProcessData) {
		if(tlosProcessData==null) return;
		WsScenarioNode rootNode = new WsScenarioNode();
		rootNode.setId(ConstantDefinitions.TREE_ROOTID);
		rootNode.setName(ConstantDefinitions.TREE_ROOT);

		root = new DefaultTreeNode(rootNode, null);

		setSelectedTreeNode(root);

		if (tlosProcessData.getJobList() != null) {
			for (JobProperties jobProperties : tlosProcessData.getJobList().getJobPropertiesArray()) {
				addJobNode(jobProperties, selectedTreeNode);
			}
		}
		constructTree(tlosProcessData.getScenarioArray());
	}

	public void reconstructJSTree() {

		setPassedParameters();
		
		TlosProcessData tlosProcessData = getTlosProcessData(CommonConstantDefinitions.EXIST_TEMPLATEDATA, getDocumentScope(CommonConstantDefinitions.EXIST_TEMPLATEDATA));
		
		constructJSTree(tlosProcessData);
	}

	public void addJobNode(JobProperties jobProperties, TreeNode selectedNode) {

		WsJobNode wsJobNode = new WsJobNode();
		wsJobNode.setId(jobProperties.getID());
		wsJobNode.setJobType(jobProperties.getBaseJobInfos().getJobTypeDetails().getJobCommandType().intValue());
		wsJobNode.setName(jobProperties.getBaseJobInfos().getJsName());
		wsJobNode.setJobType(jobProperties.getBaseJobInfos().getJobTypeDetails().getJobCommandType().intValue());
		
		wsJobNode.setLabelText(jobProperties.getBaseJobInfos().getJsName());
		wsJobNode.setLeafIcon( getScenarioMBean().getJobIconsElement( jobProperties.getBaseJobInfos().getJobTypeDetails().getJobCommandType().toString() ));
		
		new DefaultTreeNode(ConstantDefinitions.TREE_JOB, wsJobNode, selectedNode);
	}

	public void constructTree(Scenario[] scenario) {
		for (Scenario children : scenario) {
			TreeNode scenarioNode = addScenario(children);

			if (children.getScenarioArray().length > 0) {
				selectedTreeNode = scenarioNode;
				constructTree(children.getScenarioArray());
			}
		}
	}

	public TreeNode addScenario(Scenario scenario) {

		WsScenarioNode wsScenarioNode = new WsScenarioNode();
		wsScenarioNode.setId(scenario.getID());
		wsScenarioNode.setName(scenario.getBaseScenarioInfos().getJsName());

		TreeNode scenarioNode = new DefaultTreeNode(ConstantDefinitions.TREE_JOBGROUP, wsScenarioNode, selectedTreeNode);

		scenarioNode.setExpanded(true);

		if (scenario.getJobList() != null) {
			for (JobProperties jobProperties : scenario.getJobList().getJobPropertiesArray()) {
				addJobNode(jobProperties, scenarioNode);
			}
		}

		return scenarioNode;
	}

	public void addMessage(String summary) {
		FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, summary, null);
		FacesContext.getCurrentInstance().addMessage(null, message);
	}

	public void onNodeExpand(NodeExpandEvent event) {
		/*
		 * FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Expanded", event.getTreeNode().toString());
		 * 
		 * FacesContext.getCurrentInstance().addMessage(null, message);
		 */
	}

	public void onNodeCollapse(NodeCollapseEvent event) {
		/*
		 * FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Collapsed", event.getTreeNode().toString());
		 * 
		 * FacesContext.getCurrentInstance().addMessage(null, message);
		 */
	}

	

	public void onNodeUnselect(NodeUnselectEvent event) {
		/*
		 * FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Unselected", event.getTreeNode().toString());
		 * 
		 * FacesContext.getCurrentInstance().addMessage(null, message);
		 */
	}

	public void onNodeSelect(NodeSelectEvent event) {
		selectedTreeNode = event.getTreeNode();
		/*
		 * FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Unselected", event.getTreeNode().toString());
		 * 
		 * FacesContext.getCurrentInstance().addMessage(null, message);
		 */
	}
	
	// işe sağ tıklayarak sil dediğimizde buraya geliyor
	public boolean deleteJob() {
		boolean result = true;
		
		String docId = getDocId( DocMetaDataHolder.FIRST_COLUMN );
		System.err.println("Job Template Tree den is silinemedi !! TODO listesinde");


		return result;
	}
	
	public DBOperations getDbOperations() {
		return dbOperations;
	}

	public void setDbOperations(DBOperations dbOperations) {
		this.dbOperations = dbOperations;
	}

	public TreeNode getRoot() {
		return root;
	}

	public void setRoot(TreeNode root) {
		this.root = root;
	}

	public TreeNode getSelectedJS() {
		return selectedJS;
	}

	public void setSelectedJS(TreeNode selectedJS) {
		this.selectedJS = selectedJS;
	}

	public TreeNode getSelectedTreeNode() {
		return selectedTreeNode;
	}

	public void setSelectedTreeNode(TreeNode selectedTreeNode) {
		this.selectedTreeNode = selectedTreeNode;
	}

	public ScenarioMBean getScenarioMBean() {
		return scenarioMBean;
	}

	public void setScenarioMBean(ScenarioMBean scenarioMBean) {
		this.scenarioMBean = scenarioMBean;
	}

}