package com.likya.tlossw.web.definitions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;
import javax.xml.namespace.QName;

import org.apache.xmlbeans.XmlOptions;
import org.primefaces.component.datatable.DataTable;

import com.likya.tlos.model.xmlbeans.swresourcens.ResourceListType;
import com.likya.tlos.model.xmlbeans.swresourcens.ResourceType;
import com.likya.tlossw.utils.xml.XMLNameSpaceTransformer;
import com.likya.tlossw.web.TlosSWBaseBean;

@ManagedBean(name = "resourceSearchPanelMBean")
@ViewScoped
public class ResourceSearchPanelMBean extends TlosSWBaseBean implements Serializable {

	private static final long serialVersionUID = 7899903706759759225L;

	private ResourceType resource;

	private ArrayList<ResourceType> searchResourceList;
	private transient DataTable searchResourceTable;
    private ResourceType selectedRow;
	private String resourceName;

	private List<ResourceType> filteredResourceList;

	public void dispose() {
		resetResourceAction();
	}

	@PostConstruct
	public void init() {
		resetResourceAction();
	}

	public String getResourceXML() {
		//QName qName = RNSEntryType.type.getName()OuterType().getDocumentElementName();
		//QName qName = RNSEntryType.type.getName();
		QName qName = ResourceType.type.getName();
		//QName qName = new QName("http://schemas.ogf.org/rns/2009/12/rns", "RNSEntryType", "rns");
		XmlOptions xmlOptions = XMLNameSpaceTransformer.transformXML(qName);
		String resourceXML = resource.xmlText(xmlOptions);

		return resourceXML;
	}

	public void resetResourceAction() {
		resource = ResourceType.Factory.newInstance();
		searchResourceList = new ArrayList<ResourceType>();
		resourceName = "";
	}

	public void searchResourceAction(ActionEvent e) {
		if (resourceName != null && !resourceName.equals("")) {
			resource.setEntryName(resourceName);
		}

		ResourceListType resourceListType = getDbOperations().searchResource(getResourceXML());
		searchResourceList = new ArrayList<ResourceType>();

		for (ResourceType resourceType : resourceListType.getResourceArray()) {
			searchResourceList.add(resourceType);
		}

		if (searchResourceList == null || searchResourceList.size() == 0) {
			addMessage("searchResource", FacesMessage.SEVERITY_INFO, "tlos.info.search.noRecord", null);
		}
	}

	public void deleteResourceAction(ActionEvent e) {
		//resource = (ResourceType) searchResourceTable.getRowData();
		resource = selectedRow;
		if (getDbOperations().deleteResource(getResourceXML())) {
			searchResourceList.remove(resource);
			resource = ResourceType.Factory.newInstance();

			addMessage("searchResource", FacesMessage.SEVERITY_INFO, "tlos.success.resource.delete", null);
		} else {
			addMessage("searchResource", FacesMessage.SEVERITY_ERROR, "tlos.error.resource.delete", null);
		}
	}

	public String getResourceName() {
		return resourceName;
	}

	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}

	public DataTable getSearchResourceTable() {
		return searchResourceTable;
	}

	public void setSearchResourceTable(DataTable searchResourceTable) {
		this.searchResourceTable = searchResourceTable;
	}

	public ResourceType getResource() {
		return resource;
	}

	public void setResource(ResourceType resource) {
		this.resource = resource;
	}

	public ArrayList<ResourceType> getSearchResourceList() {
		return searchResourceList;
	}

	public void setSearchResourceList(ArrayList<ResourceType> searchResourceList) {
		this.searchResourceList = searchResourceList;
	}

	public List<ResourceType> getFilteredResourceList() {
		return filteredResourceList;
	}

	public void setFilteredResourceList(List<ResourceType> filteredResourceList) {
		this.filteredResourceList = filteredResourceList;
	}

	public ResourceType getSelectedRow() {
		return selectedRow;
	}

	public void setSelectedRow(ResourceType selectedRow) {
		this.selectedRow = selectedRow;
	}

}
