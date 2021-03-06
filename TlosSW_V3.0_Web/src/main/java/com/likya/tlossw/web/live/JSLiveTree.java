package com.likya.tlossw.web.live;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.event.AjaxBehaviorEvent;

import org.primefaces.event.NodeCollapseEvent;
import org.primefaces.event.NodeExpandEvent;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.event.NodeUnselectEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import com.likya.tlos.model.xmlbeans.common.JobCommandTypeDocument.JobCommandType;
import com.likya.tlos.model.xmlbeans.data.JobPropertiesDocument.JobProperties;
import com.likya.tlossw.model.client.spc.JobInfoTypeClient;
import com.likya.tlossw.model.client.spc.SpcInfoTypeClient;
import com.likya.tlossw.model.tree.GunlukIslerNode;
import com.likya.tlossw.model.tree.JobNode;
import com.likya.tlossw.model.tree.RunNode;
import com.likya.tlossw.model.tree.ScenarioNode;
import com.likya.tlossw.model.tree.TlosSpaceWideNode;
import com.likya.tlossw.utils.CommonConstantDefinitions;
import com.likya.tlossw.web.TlosSWBaseBean;
import com.likya.tlossw.web.appmng.SessionMediator;
import com.likya.tlossw.web.common.Security;
import com.likya.tlossw.web.utils.ConstantDefinitions;
import com.likya.tlossw.web.utils.DecorationUtils;
import com.likya.tlossw.webclient.TEJmxMpClient;

@ManagedBean(name = "jSLiveTree")
@ViewScoped
public class JSLiveTree extends TlosSWBaseBean implements Serializable {

	private static final long serialVersionUID = 2287729115524041857L;

	@ManagedProperty(value = "#{scenarioMBean}")
	private ScenarioMBean scenarioMBean;
	
	@ManagedProperty(value = "#{sessionMediator}")
	private SessionMediator sessionMediator;
	
	private TreeNode root;

	// private TreeNode calisanIsler;

	private TreeNode selectedJS;

	private TreeNode selectedTreeNode;

	private TlosSpaceWideNode tlosSpaceWideNode = null;

	private CacheBase liveTreeCache = null;

	private DefaultTreeNode dummyNode = new DefaultTreeNode(ConstantDefinitions.TREE_DUMMY, null);

	@ManagedProperty(value = "#{security}")
	private Security security;

	@PostConstruct
	public void initJSLiveTree() {

		setPassedParameters();
		
		if (getPassedParameter().get(CommonConstantDefinitions.FIRST_COLUMN_STR) != null) {
			//getSessionMediator().getScopeText( getPassedParameter().get(CommonConstantDefinitions.EXIST_SCOPEID1) )
			getSessionMediator().getWebAppUser().setViewRoleId(CommonConstantDefinitions.EXIST_GLOBALDATA); // MetaDataType.GLOBAL
		}

		constructJSTree();

	}

	public void constructJSTree() {

		String testString = resolveMessage("tlos.workspace.pannel.job.management");
		System.out.println(testString);

		root = new DefaultTreeNode(ConstantDefinitions.TREE_ROOT, resolveMessage("root"), null);
		DefaultTreeNode calisanIsler = new DefaultTreeNode(ConstantDefinitions.TREE_CALISANISLER, resolveMessage("likya.agac.calisan.isler"), root);

		calisanIsler.getChildren().add(dummyNode);
		calisanIsler.setExpanded(false);

		constructRunNodes();

	}

	private void constructRunNodes() {
		// if (security.get("Instance").equals(Boolean.TRUE)) {

		// ArrayList<String> instanceIds = TEJmxMpClient.retrieveInstanceIds(new JmxUser());

		// constructInstanceNodes(new HashSet<String>(instanceIds));

		liveTreeCache = new CacheBase();
		tlosSpaceWideNode = new TlosSpaceWideNode();
		// }
	}

	private void constructRunNodes(Set<String> runIds) {

		Iterator<String> keyIterator = runIds.iterator();

		while (keyIterator.hasNext()) {
			String runId = keyIterator.next();
			addRun(runId);
		}
	}

	public void addRun(String runId) {
		DefaultTreeNode calisanIsler = (DefaultTreeNode) root.getChildren().get(0);
		DefaultTreeNode runNode = new DefaultTreeNode(ConstantDefinitions.TREE_INSTANCE, new RunNode(runId), calisanIsler);
		runNode.getChildren().add(dummyNode);
		runNode.setExpanded(false);
	}

	public void addJobNode(JobProperties jobProperties, TreeNode selectedNode) {
		@SuppressWarnings("unused")
		TreeNode jobNode = new DefaultTreeNode(ConstantDefinitions.TREE_JOB, jobProperties.getBaseJobInfos().getJsName() + " | " + jobProperties.getID(), selectedNode);
	}

	public void treeAction(AjaxBehaviorEvent event) {
		// log.info("LiveNavigationTree2 : treeAction  Begin :" + Utils.getCurrentTimeWithMilliseconds());
		renderLiveTree();
		// log.info("LiveNavigationTree2 : treeAction  End :" + Utils.getCurrentTimeWithMilliseconds());
	}

	public void renderLiveTree() {

		Object liveTree = null;

		if (liveTreeCache != null && tlosSpaceWideNode != null) {
			liveTree = liveTreeCache.get(((Object) tlosSpaceWideNode).hashCode());
		}

		if (liveTree != null) {
			tlosSpaceWideNode = (TlosSpaceWideNode) liveTree;
			// log.debug("LiveNavigationTree2 : renderLiveTree  CacheData :" + Utils.getCurrentTimeWithMilliseconds());
		} else {
			if (root.getChildCount() > 0) {
				DefaultTreeNode calisanIsler = (DefaultTreeNode) root.getChildren().get(0);
				TlosSpaceWideNode tlosSpaceWideInputNode = preparePreRenderLiveTreeData(calisanIsler);

				// sunucudan guncel is listelerini aliyor
				tlosSpaceWideNode = TEJmxMpClient.getLiveTreeInfo(getWebAppUser(), tlosSpaceWideInputNode);
				if (tlosSpaceWideNode == null) {
					System.out.println("Çalışma ağacı bilgileri Sunucudan alınamdı! Sunucu kapalı olabilir veya bağlantıda problem var.");
					return;
				}
				if (liveTreeCache == null) {
					System.out.println("liveTreeCache == null");
					return;
				}
				liveTreeCache.put(((Object) tlosSpaceWideNode).hashCode(), tlosSpaceWideNode);
				// log.debug("LiveNavigationTree2 : renderLiveTree  EngineData :" + Utils.getCurrentTimeWithMilliseconds());
			}
		}

		if (root.getChildCount() > 0) {
			DefaultTreeNode calisanIsler = (DefaultTreeNode) root.getChildren().get(0);
			prepareRenderLiveTree(calisanIsler, tlosSpaceWideNode);
		}

	}

	// agacin datasi alinip render icin hazirlaniyor
	private TlosSpaceWideNode preparePreRenderLiveTreeData(TreeNode calisanIslerNode) {

		TlosSpaceWideNode tlosSpaceWideNode = new TlosSpaceWideNode();
		GunlukIslerNode gunlukIslerNode = new GunlukIslerNode();
		tlosSpaceWideNode.setGunlukIslerNode(gunlukIslerNode);

		// Gelen senaryoNode Gunluk Isler

		// Gunluk Isler expanded
		if (calisanIslerNode.isExpanded()) {

			calisanIslerNode.getChildren().remove(dummyNode);
			// Bunlar instance listesi
			int size = calisanIslerNode.getChildCount();

			for (int i = 0; i < size; i++) {

				// instance aliniyor
				TreeNode tmpPlanFolder = calisanIslerNode.getChildren().get(i);

				tmpPlanFolder.getChildren().remove(dummyNode);

				if (tmpPlanFolder.isExpanded()) {

					String runId = ((RunNode) tmpPlanFolder.getData()).getRunId();
					RunNode runNode = new RunNode(runId);

					// instance icindeki senaryo sayisina bakiyoruz
					int numberOfScenariosInPlan = tmpPlanFolder.getChildCount();

					for (int j = 0; j < numberOfScenariosInPlan; j++) {

						// Her bir senaryoyu aliyoruz
						TreeNode tmpScenario = tmpPlanFolder.getChildren().get(j);
						tmpScenario.getChildren().remove(dummyNode);
						
						ScenarioNode expandedNode = preRenderLiveTreeRecursive(tmpScenario);

						if (expandedNode != null) {
							runNode.getScenarioNodeMap().put(expandedNode.getSpcInfoTypeClient().getSpcId(), expandedNode);
						}
					}
					gunlukIslerNode.getRunNodes().put(runId, runNode);
				}

			}
		}
		return tlosSpaceWideNode;
	}

	private ScenarioNode preRenderLiveTreeRecursive(TreeNode scenarioNode) {

		ScenarioNode myScenarioNode = null;

		if (scenarioNode.isExpanded()) {
			scenarioNode.getChildren().remove(dummyNode);
//			myScenarioNode = new ScenarioNode((ScenarioNode) scenarioNode.getData());

			myScenarioNode = new ScenarioNode();
			myScenarioNode.setSpcInfoTypeClient(((ScenarioNode) scenarioNode.getData()).getSpcInfoTypeClient());

			for (int i = 0; i < scenarioNode.getChildCount(); i++) {
				DefaultTreeNode tmpScenario = (DefaultTreeNode) scenarioNode.getChildren().get(i);
				if (!(scenarioNode.getData() instanceof ScenarioNode)) {
					continue;
				}
				ScenarioNode expandedNode = preRenderLiveTreeRecursive(tmpScenario);
				if (expandedNode != null) {
					myScenarioNode.getScenarioNodes().add(expandedNode);
				}
				
//				if (tmpScenario.getType().equalsIgnoreCase("job")) {
//					myScenarioNode.getJobNodes().add( (JobNode) tmpScenario.getData() );
//				}
//				if (tmpScenario.getType().equalsIgnoreCase("scenario")) {
//					myScenarioNode.getScenarioNodes().add( (ScenarioNode) tmpScenario.getData() );
//				}

			}
		}

		return myScenarioNode;
	}

	private TreeNode prepareRenderLiveTree(TreeNode calisanIsler, TlosSpaceWideNode tlosSpaceWideNode) {

		if (calisanIsler.getChildCount() > 0) {
			// treeRendered = true;
		}

		GunlukIslerNode serverGunlukIslerNode = tlosSpaceWideNode.getGunlukIslerNode();

		// 1. asama instance lar eklenecek
		calisanIsler.getChildren().clear();
		constructRunNodes(serverGunlukIslerNode.getRunNodes().keySet());

		if (calisanIsler.getChildren().size() == 0) {
			calisanIsler.getChildren().add(dummyNode);
			calisanIsler.setExpanded(false);
		} else {
			Iterator<TreeNode> gunlukIslerIterator = calisanIsler.getChildren().iterator();

			while (gunlukIslerIterator.hasNext()) {
				DefaultTreeNode runTreeNode = (DefaultTreeNode) gunlukIslerIterator.next();

				String instanceId = ((RunNode) runTreeNode.getData()).getRunId();
				if (serverGunlukIslerNode.getRunNodes().get(instanceId).getScenarioNodeMap().size() > 0) {
					constructLiveTree(runTreeNode, serverGunlukIslerNode.getRunNodes().get(instanceId).getScenarioNodeMap());
					runTreeNode.setExpanded(true);
				}
			}
		}
		return calisanIsler;
	}

	private void constructLiveTree(TreeNode runNode, HashMap<String, ScenarioNode> serverScenarioNodes) {

		runNode.getChildren().clear();

		Iterator<String> keyIterator = serverScenarioNodes.keySet().iterator();

		while (keyIterator.hasNext()) {

			String scenarioId = keyIterator.next();

			ScenarioNode serverScenarioNode = serverScenarioNodes.get(scenarioId);

			SpcInfoTypeClient spcInfoTypeClient = new SpcInfoTypeClient(serverScenarioNode.getSpcInfoTypeClient());

			ScenarioNode scenarioNode = new ScenarioNode();

			scenarioNode.setId(serverScenarioNode.getId());
			scenarioNode.setLabelText(serverScenarioNode.getLabelText());
			scenarioNode.setName(spcInfoTypeClient.getJsName());

//			if (spcInfoTypeClient.getJsName() == null) {
//				scenarioNode.setId(spcInfoTypeClient.getSpcId());
//			}
//			spcInfoTypeClient.setCurrentRunId(((RunNode) runNode.getData()).getRunId());
			scenarioNode.setSpcInfoTypeClient(spcInfoTypeClient);

			TreeNode scenarioNodeTree = new DefaultTreeNode(ConstantDefinitions.TREE_SCENARIO, scenarioNode, runNode);
			scenarioNodeTree.getChildren().add(dummyNode);
			scenarioNodeTree.setExpanded(false);

			System.out.println();
			System.out.println("************************************************************");
			System.out.println();
			System.out.println("Working for scenario : " + spcInfoTypeClient.getJsName());

			if (serverScenarioNode.getScenarioNodes().size() > 0 || serverScenarioNode.getJobNodes().size() > 0) {
				System.out.println("Cleaning children of scenario : " + scenarioNode.getName());
				scenarioNodeTree.getChildren().clear();
				scenarioNodeTree.setExpanded(true);
				System.out.println("Recursing for scenario : " + spcInfoTypeClient.getJsName());
				System.out.println("Scenario : " + serverScenarioNode.getName() + " Job List Size : " + serverScenarioNode.getJobNodes().size() + " Scenario List Size " + serverScenarioNode.getScenarioNodes().size());
				renderLiveTreeRecursive(scenarioNodeTree, serverScenarioNode);
			}
		}
	}

	private void renderLiveTreeRecursive(TreeNode scenarioNode, ScenarioNode serverScenarioNode) {

		constructJobNodes(scenarioNode, serverScenarioNode.getJobNodes());

		for (ScenarioNode tmpScenarioNode : serverScenarioNode.getScenarioNodes()) {
			SpcInfoTypeClient spcInfoTypeClient = new SpcInfoTypeClient(tmpScenarioNode.getSpcInfoTypeClient());

			tmpScenarioNode.setName(spcInfoTypeClient.getJsName());

			TreeNode scenarioNodeTree = new DefaultTreeNode(ConstantDefinitions.TREE_SCENARIO, tmpScenarioNode, scenarioNode);
			scenarioNodeTree.getChildren().add(dummyNode);
			scenarioNodeTree.setExpanded(false);

			if (tmpScenarioNode.getScenarioNodes().size() > 0 || tmpScenarioNode.getJobNodes().size() > 0) {
				scenarioNodeTree.getChildren().clear();
				scenarioNodeTree.setExpanded(true);
				renderLiveTreeRecursive(scenarioNodeTree, tmpScenarioNode);
			}

		}

	}

	public void constructJobNodes(TreeNode scenarioNode, ArrayList<JobNode> jobNodes) {

		Iterator<JobNode> jobNodeIterator = jobNodes.iterator();

		while (jobNodeIterator.hasNext()) {

			JobNode tmpJobNode = jobNodeIterator.next();
			JobInfoTypeClient jobInfoTypeClient = tmpJobNode.getJobInfoTypeClient();

			// String jobText = jobInfoTypeClient.getJobKey();
			JobNode jobNode = new JobNode();
			jobNode.setId(tmpJobNode.getId());
			jobNode.setLabelText(tmpJobNode.getLabelText());
			jobNode.setJobInfoTypeClient(jobInfoTypeClient);
			// job.setLeafIcon(jobImageSetter(jobInfoTypeClient.getLiveStateInfo()));
			jobNode.setName(jobInfoTypeClient.getJobName());
			jobNode.setJobType(JobCommandType.Enum.forString(jobInfoTypeClient.getJobCommandType().toUpperCase()).intValue());
			
			if (jobInfoTypeClient.getLiveStateInfo() == null) {
				System.out.println("jobInfoTypeClient.getLiveStateInfo() == null");
			}
			jobNode.setLeafIcon(DecorationUtils.jobStateIconMappings(jobInfoTypeClient.getLiveStateInfo()));
			jobNode.setPath(jobInfoTypeClient.getTreePath());
			TreeNode scenarioNodeTree = new DefaultTreeNode(ConstantDefinitions.TREE_JOB, jobNode, scenarioNode);
			scenarioNodeTree.setExpanded(false);

		}

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

	public void onNodeExpand(NodeExpandEvent event) {
		event.getTreeNode().setExpanded(true);
//		ScenarioNode scenarioNode = null;
//		if (event.getTreeNode().getData() instanceof ScenarioNode) {
//				scenarioNode = (ScenarioNode) event.getTreeNode().getData(); 
//				selectedSpcId = scenarioNode.getSpcInfoTypeClient().getSpcId();
//				System.out.println(scenarioNode.getName());
//				System.out.println(scenarioNode.getId());
//				System.out.println(scenarioNode.getSpcInfoTypeClient().getSpcId());
//				System.out.println(scenarioNode.getSpcInfoTypeClient().getJsName());
//				System.out.println(scenarioNode.getSpcInfoTypeClient().getJsId());
//		} else if (event.getTreeNode().getData() instanceof InstanceNode) {
//			InstanceNode instanceNode = null;
//			instanceNode = (InstanceNode) event.getTreeNode().getData(); 
//			String instanceId = instanceNode.getInstanceId().toString();
////		    addMessage("jobTree", FacesMessage.SEVERITY_INFO,
////				instanceId + " expanded", null);
//		}
//		else {
//			selectedSpcId = null;
//		}
		treeAction(event);

	}

	public void onNodeCollapse(NodeCollapseEvent event) {
		event.getTreeNode().setExpanded(false);
//		ScenarioNode scenarioNode = null;
//		if (event.getTreeNode().getData() instanceof ScenarioNode) {
//				scenarioNode = (ScenarioNode) event.getTreeNode().getData(); 
//				selectedSpcId = scenarioNode.getSpcInfoTypeClient().getSpcId();
//				System.out.println(scenarioNode.getName());
//				System.out.println(scenarioNode.getId());
//				System.out.println(scenarioNode.getSpcInfoTypeClient().getSpcId());
//				System.out.println(scenarioNode.getSpcInfoTypeClient().getJsName());
//				System.out.println(scenarioNode.getSpcInfoTypeClient().getJsId());
//		}
//		else {
//			selectedSpcId = null;
//		}
		treeAction(event);
		// addMessage("jobTree", FacesMessage.SEVERITY_INFO,
		// event.getTreeNode().toString() + " collapsed", null);
	}

	public void onNodeSelect(NodeSelectEvent event) {
		// addMessage("jobTree", FacesMessage.SEVERITY_INFO,
		// event.getTreeNode().toString() + " selected", null);
	}

	public void onNodeUnselect(NodeUnselectEvent event) {
		// addMessage("jobTree", FacesMessage.SEVERITY_INFO,
		// event.getTreeNode().toString() + " unselected", null);
	}

	public TreeNode getSelectedTreeNode() {
		return selectedTreeNode;
	}

	public void setSelectedTreeNode(TreeNode selectedTreeNode) {
		this.selectedTreeNode = selectedTreeNode;
	}

	public Security getSecurity() {
		return security;
	}

	public void setSecurity(Security security) {
		this.security = security;
	}

	public TlosSpaceWideNode getTlosSpaceWideNode() {
		return tlosSpaceWideNode;
	}

	public ScenarioMBean getScenarioMBean() {
		return scenarioMBean;
	}

	public void setScenarioMBean(ScenarioMBean scenarioMBean) {
		this.scenarioMBean = scenarioMBean;
	}

}
