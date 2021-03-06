package com.likya.tlossw.web.management;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;
import javax.xml.namespace.QName;

import org.apache.xmlbeans.XmlOptions;

import com.likya.tlos.model.xmlbeans.common.ActiveDocument.Active;
import com.likya.tlos.model.xmlbeans.dbconnections.DbConnectionProfileDocument.DbConnectionProfile;
import com.likya.tlos.model.xmlbeans.dbconnections.DbPropertiesDocument.DbProperties;
import com.likya.tlos.model.xmlbeans.dbconnections.DeployedDocument.Deployed;
import com.likya.tlos.model.xmlbeans.dbconnections.JdbcConnectionPoolParamsDocument.JdbcConnectionPoolParams;
import com.likya.tlossw.model.DBAccessInfoTypeClient;
import com.likya.tlossw.utils.CommonConstantDefinitions;
import com.likya.tlossw.utils.xml.XMLNameSpaceTransformer;
import com.likya.tlossw.web.TlosSWBaseBean;
import com.likya.tlossw.web.utils.TestUtils;

@ManagedBean(name = "dbAccessPanelMBean")
@ViewScoped
public class DBAccessPanelMBean extends TlosSWBaseBean implements Serializable {

//	@ManagedProperty(value = "#{param.selectedDBAccessID}")
	private String selectedDBAccessID;
//
//	@ManagedProperty(value = "#{param.insertCheck}")
	private String insertCheck;
//
//	@ManagedProperty(value = "#{param.iCheck}")
	private String iCheck;

	@ManagedProperty(value = "#{testUtils}")
	private TestUtils testUtils;

	private static final long serialVersionUID = 1L;

	private DbConnectionProfile dbConnectionProfile;
	private DBAccessInfoTypeClient dbAccessInfoTypeClient;

	private String dbConnectionName = null;
	private String dbDefinitionId = null;
	private String selectedUserName = null;
	private String dbCpID = null;
	
	private Collection<SelectItem> dbConnectionNameList = null;

	private String confirmUserPassword;

	private String active;

	private boolean insertButton;
	private JdbcConnectionPoolParams jdbcConnectionPoolParams = null;
			
	@PostConstruct
	public void init() {
		if(dbConnectionProfile == null) {
			dbConnectionProfile = DbConnectionProfile.Factory.newInstance();
		}

		insertCheck = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("insertCheck");
		iCheck = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("iCheck");
		selectedDBAccessID = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("selectedDBAccessID");
		selectedUserName = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("selectedUserName");
		dbCpID = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("dbCpID");
		
		if(jdbcConnectionPoolParams == null ) {
		  
		  jdbcConnectionPoolParams = dbConnectionProfile.addNewJdbcConnectionPoolParams();
		  
		  if (selectedDBAccessID != null) {
		     dbConnectionProfile.setDbDefinitionId(new BigInteger(selectedDBAccessID));
		  }
		  jdbcConnectionPoolParams.setInitialCapacity(new BigInteger("3"));
		  jdbcConnectionPoolParams.setMaxCapacity(new BigInteger("10"));
		  jdbcConnectionPoolParams.setMinIdle(new BigInteger("0"));
		  jdbcConnectionPoolParams.setMaxIdle(new BigInteger("0"));
		  jdbcConnectionPoolParams.setMaxWait(new BigInteger("10000"));

		}
		//dbConnectionProfile.setJdbcConnectionPoolParams(jdbcConnectionPoolParams);

		fillDBConnectionNameList();

		if (iCheck != null && iCheck.equals("insert"))
			insertButton = true;

		if (insertCheck != null) {

			if (insertCheck.equals("update")) {
				insertButton = false;

				dbConnectionProfile = getDbOperations().searchDBAccessByID(dbCpID);

				if (dbConnectionProfile != null) {
					setDbConnectionName(dbConnectionProfile.getDbDefinitionId() + "");
					setConfirmUserPassword(dbConnectionProfile.getUserPassword());
					setActive(dbConnectionProfile.getActive().toString());
				}

			} else {
				insertButton = true;
			}
		}
	}

	public void fillDBConnectionNameList() {
		dbConnectionNameList = new ArrayList<SelectItem>();

		for (DbProperties dbProperties : getDbOperations().getDBConnections()) {
			SelectItem item = new SelectItem();
			item.setValue(dbProperties.getID());
			item.setLabel(dbProperties.getConnectionName());

			dbConnectionNameList.add(item);
		}
	}

	public String getDBAccessXML() {
		QName qName = DbConnectionProfile.type.getOuterType().getDocumentElementName();
		XmlOptions xmlOptions = XMLNameSpaceTransformer.transformXML(qName);
		String dbProfileXML = dbConnectionProfile.xmlText(xmlOptions);

		return dbProfileXML;
	}

	public void resetDBAccessPropertiesAction() {
		dbConnectionProfile = DbConnectionProfile.Factory.newInstance();

		JdbcConnectionPoolParams jdbcConnectionPoolParams = JdbcConnectionPoolParams.Factory.newInstance();
		dbConnectionProfile.setJdbcConnectionPoolParams(jdbcConnectionPoolParams);

		active = Active.YES.toString();
		dbConnectionName = "";
	}

	public void updateDBAccessAction(ActionEvent e) {
		dbConnectionProfile.setDbDefinitionId(new BigInteger(getDbConnectionName()));
		dbConnectionProfile.setActive(Active.Enum.forString(getActive()));
		dbConnectionProfile.setDeployed(Deployed.NO);

		if (getDbOperations().updateDBAccessProfile(getDBAccessXML())) {
			addMessage("insertDBAccess", FacesMessage.SEVERITY_INFO, "tlos.success.dbAccessDef.update", null);
		} else {
			addMessage("insertDBAccess", FacesMessage.SEVERITY_ERROR, "tlos.error.dbConnection.update", null);
		}
	}

	public void insertDBAccessAction(ActionEvent e) {
		if (setDBAccessID()) {
			dbConnectionProfile.setDbDefinitionId(new BigInteger(getDbConnectionName()));
			dbConnectionProfile.setActive(Active.Enum.forString(getActive()));
			dbConnectionProfile.setDeployed(Deployed.NO);

			if (getDbOperations().insertDBAccessProfile(getDBAccessXML())) {
				addMessage("insertDBAccess", FacesMessage.SEVERITY_INFO, "tlos.success.dbAccessDef.insert", null);
				resetDBAccessPropertiesAction();
			} else {
				addMessage("insertDBAccess", FacesMessage.SEVERITY_ERROR, "tlos.error.dbConnection.insert", null);
			}
		}
	}

	// veri tabaninda kayitli siradaki id degerini set ediyor
	public boolean setDBAccessID() {
		int id = getDbOperations().getNextId(CommonConstantDefinitions.DBUSER_ID);

		if (id < 0) {
			addMessage("insertDBConnection", FacesMessage.SEVERITY_INFO, "tlos.info.dbConnectionProfile.getId", null);
			return false;
		}
		dbConnectionProfile.setID(new BigInteger(id + ""));

		return true;
	}

	public void testDBAccessAction(ActionEvent e) {
		dbConnectionProfile.setDbDefinitionId(new BigInteger(dbDefinitionId));
		if ( testUtils.testDBConnection( dbConnectionProfile ) ) {
			addMessage("insertDBConnection", FacesMessage.SEVERITY_INFO, "tlos.success.dbAccessDef.test", null);
		} else {
			addMessage("insertDBConnection", FacesMessage.SEVERITY_ERROR, "tlos.error.dbConnection.test", null);
		}
	}

	public boolean isInsertButton() {
		return insertButton;
	}

	public void setInsertButton(boolean insertButton) {
		this.insertButton = insertButton;
	}

	public String getInsertCheck() {
		return insertCheck;
	}

	public void setInsertCheck(String insertCheck) {
		this.insertCheck = insertCheck;
	}

	public String getiCheck() {
		return iCheck;
	}

	public void setiCheck(String iCheck) {
		this.iCheck = iCheck;
	}

	public String getSelectedDBAccessID() {
		return selectedDBAccessID;
	}

	public void setSelectedDBAccessID(String selectedDBAccessID) {
		this.selectedDBAccessID = selectedDBAccessID;
	}

	public DbConnectionProfile getDbConnectionProfile() {
		return dbConnectionProfile;
	}

	public void setDbConnectionProfile(DbConnectionProfile dbConnectionProfile) {
		this.dbConnectionProfile = dbConnectionProfile;
	}

	public DBAccessInfoTypeClient getDbAccessInfoTypeClient() {
		return dbAccessInfoTypeClient;
	}

	public void setDbAccessInfoTypeClient(DBAccessInfoTypeClient dbAccessInfoTypeClient) {
		this.dbAccessInfoTypeClient = dbAccessInfoTypeClient;
	}

	public String getDbConnectionName() {
		return dbConnectionName;
	}

	public void setDbConnectionName(String dbConnectionName) {
		this.dbConnectionName = dbConnectionName;
		dbDefinitionId = new BigInteger(dbConnectionName)+""; 
		dbConnectionProfile.setDbDefinitionId(new BigInteger(dbDefinitionId));
	}

	public Collection<SelectItem> getDbConnectionNameList() {
		return dbConnectionNameList;
	}

	public void setDbConnectionNameList(Collection<SelectItem> dbConnectionNameList) {
		this.dbConnectionNameList = dbConnectionNameList;
	}

	public String getConfirmUserPassword() {
		return confirmUserPassword;
	}

	public void setConfirmUserPassword(String confirmUserPassword) {
		this.confirmUserPassword = confirmUserPassword;
	}

	public String getActive() {
		return active;
	}

	public void setActive(String active) {
		this.active = active;
	}

	public TestUtils getTestUtils() {
		return testUtils;
	}

	public void setTestUtils(TestUtils testUtils) {
		this.testUtils = testUtils;
	}

	public JdbcConnectionPoolParams getJdbcConnectionPoolParams() {
		return jdbcConnectionPoolParams;
	}

	public void setJdbcConnectionPoolParams(JdbcConnectionPoolParams jdbcConnectionPoolParams) {
		this.jdbcConnectionPoolParams = jdbcConnectionPoolParams;
	}

	public String getDbDefinitionId() {
		return dbDefinitionId;
	}

	public void setDbDefinitionId(String dbDefinitionId) {
		this.dbDefinitionId = dbDefinitionId;
	}

	public String getSelectedUserName() {
		return selectedUserName;
	}

	public void setSelectedUserName(String selectedUserName) {
		this.selectedUserName = selectedUserName;
	}

	public String getDbCpID() {
		return dbCpID;
	}

	public void setDbCpID(String dbCpID) {
		this.dbCpID = dbCpID;
	}

}
