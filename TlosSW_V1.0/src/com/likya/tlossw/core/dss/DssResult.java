package com.likya.tlossw.core.dss;

import com.likya.tlos.model.xmlbeans.swresourcenagentresults.ResourceDocument.Resource;
import com.likya.tlos.model.xmlbeans.swresourcenagentresults.ResourceAgentListDocument.ResourceAgentList;

public class DssResult extends Result {

	/**
	 * resultCode : E�er bu de�er >= 0 ise i�lem ba�ar�l�d�r, 
	 * < 0 ise i�lem ba�ar�s�zd�r. Her iki durumda da farkl� de�erler alabilir.
	 * resultDescription : Ba�ar�l� veya ba�ar�s�z bitme durumunda a��klama 
	 * i�ermelidir. Ba�ar�l� ise ba�ar� ile ilgili bilgi, ba�ar�s�z ise 
	 * ba�ar�s�zl�k ile ilgili bilgi konmal�d�r. 
	 */
	
	private ResourceAgentList resourceAgentList;
	
	private Resource resource;

	public DssResult(int resultCode, String resultDescription, ResourceAgentList resourceAgentList) {
		super(resultCode, resultDescription);
		this.resourceAgentList = resourceAgentList;
		this.resource = null;
	}
	
	public DssResult(int resultCode, String resultDescription, ResourceAgentList resourceAgentList, Resource resource) {
		super(resultCode, resultDescription);
		this.resourceAgentList = resourceAgentList;
		this.resource = resource;
	}

	public DssResult(int resultCode, String resultDescription) {
		super(resultCode, resultDescription);
	}

	public ResourceAgentList getResourceAgentList() {
		return resourceAgentList;
	}

	public void setResourceAgentList(ResourceAgentList resourceAgentList) {
		this.resourceAgentList = resourceAgentList;
	}

	public Resource getResource() {
		return resource;
	}

	public void setResource(Resource myResource) {
		this.resource = myResource;
	}
	
}
