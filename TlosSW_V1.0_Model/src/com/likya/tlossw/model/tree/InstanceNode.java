/*
 * TlosFaz_V2.0_Model
 * com.likya.tlos.model.tree.serkan : InstanceNode.java
 * @author Serkan Tas
 * Tarih : 18.Nis.2010 23:48:22
 */

package com.likya.tlossw.model.tree;

import java.io.Serializable;
import java.util.HashMap;

public class InstanceNode implements Serializable {

	private static final long serialVersionUID = 1502653134173511164L;

	private String instanceId;
	
	public InstanceNode(String instanceId) {
		super();
		this.instanceId = instanceId;
	}

	HashMap<String, ScenarioNode> scenarioNodeMap = new HashMap<String, ScenarioNode>();

	public String getInstanceId() {
		return instanceId;
	}

	public HashMap<String, ScenarioNode> getScenarioNodeMap() {
		return scenarioNodeMap;
	}

}
