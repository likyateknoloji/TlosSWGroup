package com.likya.tlossw.webclient;

import java.util.ArrayList;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;

import com.likya.tlos.model.xmlbeans.agent.SWAgentDocument.SWAgent;
import com.likya.tlos.model.xmlbeans.swresourcenagentresults.ResourceDocument.Resource;
import com.likya.tlossw.model.MessagesCodeMapping;
import com.likya.tlossw.model.TlosJmxReturnValue;
import com.likya.tlossw.model.WebSpaceWideRegistery;
import com.likya.tlossw.model.client.resource.TlosAgentInfoTypeClient;
import com.likya.tlossw.model.client.spc.InfoTypeClient;
import com.likya.tlossw.model.client.spc.JobInfoTypeClient;
import com.likya.tlossw.model.client.spc.SpcInfoTypeClient;
import com.likya.tlossw.model.client.spc.SpcLookUpTableTypeClient;
import com.likya.tlossw.model.client.spc.TreeInfoType;
import com.likya.tlossw.model.jmx.JmxUser;
import com.likya.tlossw.model.tree.ScenarioNode;
import com.likya.tlossw.model.tree.TlosSpaceWideNode;
import com.likya.tlossw.model.tree.resource.TlosSWResourceNode;

public class TEJmxMpClient extends TEJmxMpClientBase {

	private TEJmxMpClient() {
		// initCommanderInstance();
	}

/*	private static TEJmxMpClientBase initInstance() {
		if (getSelfInstance() == null) {
			setSelfInstance(new TEJmxMpClient());
		}

		return getSelfInstance();
	}
*/
	public static JobInfoTypeClient getJobInfoTypeClient(JmxUser jmxUser, String groupId, String jobId, boolean transformToLocalTime) {
		
		JMXConnector jmxConnector = TEJmxMpClient.getJMXConnection();

		Object[] paramList = { jmxUser, groupId, jobId, transformToLocalTime };
		String[] signature = { "com.likya.tlossw.model.jmx.JmxUser", "java.lang.String", "java.lang.String", "java.lang.Boolean" };
		Object o;

		try {
			MBeanServerConnection mbeanServerConnection = jmxConnector.getMBeanServerConnection();
			o = mbeanServerConnection.invoke(new ObjectName("MBeans:type=1"), "retrieveJobDetails", paramList, signature);
			TEJmxMpClient.disconnect(jmxConnector);
			return (JobInfoTypeClient) o;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static ArrayList<JobInfoTypeClient> getJobInfoTypeClientList(JmxUser jmxUser, String groupId) {
		return getJobInfoTypeClientList(jmxUser, groupId, false);
	}
	
	@SuppressWarnings("unchecked")
	public static ArrayList<JobInfoTypeClient> getJobInfoTypeClientList(JmxUser jmxUser, String groupId, boolean transformToLocalTime) {
		
		JMXConnector jmxConnector = TEJmxMpClient.getJMXConnection();

		Object[] paramList = { jmxUser, groupId, transformToLocalTime };
		String[] signature = { "com.likya.tlossw.model.jmx.JmxUser", "java.lang.String", "java.lang.Boolean" };
		Object o;

		try {
			MBeanServerConnection mbeanServerConnection = jmxConnector.getMBeanServerConnection();
			o = mbeanServerConnection.invoke(new ObjectName("MBeans:type=1"), "retrieveJobListDetails", paramList, signature);
			TEJmxMpClient.disconnect(jmxConnector);
			return (ArrayList<JobInfoTypeClient>) o;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static SpcLookUpTableTypeClient getSpsLookUpTable(JmxUser jmxUser, String instanceId, String treePath) {

		JMXConnector jmxConnector = TEJmxMpClient.getJMXConnection();

		Object[] paramList = { jmxUser, instanceId, treePath };
		String[] signature = { "com.likya.tlossw.model.jmx.JmxUser", "java.lang.String", "java.lang.String" };
		Object o;

		try {
			MBeanServerConnection mbeanServerConnection = jmxConnector.getMBeanServerConnection();
			o = mbeanServerConnection.invoke(new ObjectName("MBeans:type=1"), "retrieveSpcLookupTable", paramList, signature);
			TEJmxMpClient.disconnect(jmxConnector);
			return (SpcLookUpTableTypeClient) o;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static SpcInfoTypeClient retrieveSpcInfo(JmxUser jmxUser, String treePath) {

		JMXConnector jmxConnector = TEJmxMpClient.getJMXConnection();

		Object[] paramList = { jmxUser, treePath };
		String[] signature = { "com.likya.tlossw.model.jmx.JmxUser", "java.lang.String" };
		Object o;

		try {
			MBeanServerConnection mbeanServerConnection = jmxConnector.getMBeanServerConnection();
			o = mbeanServerConnection.invoke(new ObjectName("MBeans:type=1"), "retrieveSpcInfo", paramList, signature);
			TEJmxMpClient.disconnect(jmxConnector);
			return (SpcInfoTypeClient) o;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Management Functions
	 */
	public static void shutDown(JmxUser jmxUser) {

		JMXConnector jmxConnector = TEJmxMpClient.getJMXConnection();

		Object[] paramList = { jmxUser };
		String[] signature = { "com.likya.tlossw.model.jmx.JmxUser" };

		try {
			MBeanServerConnection mbeanServerConnection = jmxConnector.getMBeanServerConnection();
			mbeanServerConnection.invoke(new ObjectName("MBeans:type=2"), "shutdown", paramList, signature);
			TEJmxMpClient.disconnect(jmxConnector);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return;
	}

	public static void stopJob(JmxUser jmxUser, String jobPath) {

		JMXConnector jmxConnector = TEJmxMpClient.getJMXConnection();

		Object[] paramList = { jmxUser, jobPath };
		String[] signature = { "com.likya.tlossw.model.jmx.JmxUser", "java.lang.String" };
		
		try {
			MBeanServerConnection mbeanServerConnection = jmxConnector.getMBeanServerConnection();
			mbeanServerConnection.invoke(new ObjectName("MBeans:type=2"), "stopJob", paramList, signature);
			TEJmxMpClient.disconnect(jmxConnector);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return;
	}

	public static void retryJob(JmxUser jmxUser, String jobPath) {

		JMXConnector jmxConnector = TEJmxMpClient.getJMXConnection();

		Object[] paramList = { jmxUser, jobPath };
		String[] signature = { "com.likya.tlossw.model.jmx.JmxUser", "java.lang.String" };
		
		try {
			MBeanServerConnection mbeanServerConnection = jmxConnector.getMBeanServerConnection();
			mbeanServerConnection.invoke(new ObjectName("MBeans:type=2"), "retryJob", paramList, signature);
			TEJmxMpClient.disconnect(jmxConnector);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return;
	}

	public static void doSuccess(JmxUser jmxUser, String jobPath) {

		JMXConnector jmxConnector = TEJmxMpClient.getJMXConnection();

		Object[] paramList = { jmxUser, jobPath };
		String[] signature = { "com.likya.tlossw.model.jmx.JmxUser", "java.lang.String" };
		try {
			MBeanServerConnection mbeanServerConnection = jmxConnector.getMBeanServerConnection();
			mbeanServerConnection.invoke(new ObjectName("MBeans:type=2"), "doSuccess", paramList, signature);
			TEJmxMpClient.disconnect(jmxConnector);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return;
	}

	public static void skipJob(JmxUser jmxUser, String jobPath) {

		JMXConnector jmxConnector = TEJmxMpClient.getJMXConnection();

		Object[] paramList = { jmxUser, jobPath };
		String[] signature = { "com.likya.tlossw.model.jmx.JmxUser", "java.lang.String" };
		try {
			MBeanServerConnection mbeanServerConnection = jmxConnector.getMBeanServerConnection();
			mbeanServerConnection.invoke(new ObjectName("MBeans:type=2"), "skipJob", paramList, signature);
			TEJmxMpClient.disconnect(jmxConnector);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return;
	}

	public static void pauseJob(JmxUser jmxUser, String jobPath) {

		JMXConnector jmxConnector = TEJmxMpClient.getJMXConnection();

		Object[] paramList = { jmxUser, jobPath };
		String[] signature = { "com.likya.tlossw.model.jmx.JmxUser", "java.lang.String" };
		try {
			MBeanServerConnection mbeanServerConnection = jmxConnector.getMBeanServerConnection();
			mbeanServerConnection.invoke(new ObjectName("MBeans:type=2"), "pauseJob", paramList, signature);
			TEJmxMpClient.disconnect(jmxConnector);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return;
	}

	public static void resumeJob(JmxUser jmxUser, String jobPath) {

		JMXConnector jmxConnector = TEJmxMpClient.getJMXConnection();

		Object[] paramList = { jmxUser, jobPath };
		String[] signature = { "com.likya.tlossw.model.jmx.JmxUser", "java.lang.String" };
		try {
			MBeanServerConnection mbeanServerConnection = jmxConnector.getMBeanServerConnection();
			mbeanServerConnection.invoke(new ObjectName("MBeans:type=2"), "resumeJob", paramList, signature);
			TEJmxMpClient.disconnect(jmxConnector);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return;
	}

	public static void startJob(JmxUser jmxUser, String jobPath) {

		JMXConnector jmxConnector = TEJmxMpClient.getJMXConnection();

		Object[] paramList = { jmxUser, jobPath };
		String[] signature = { "com.likya.tlossw.model.jmx.JmxUser", "java.lang.String" };
		try {
			MBeanServerConnection mbeanServerConnection = jmxConnector.getMBeanServerConnection();
			mbeanServerConnection.invoke(new ObjectName("MBeans:type=2"), "startJob", paramList, signature);
			TEJmxMpClient.disconnect(jmxConnector);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return;
	}
	
	public static void startUserBasedJob(JmxUser jmxUser, String jobPath) {

		JMXConnector jmxConnector = TEJmxMpClient.getJMXConnection();

		Object[] paramList = { jmxUser, jobPath };
		String[] signature = { "com.likya.tlossw.model.jmx.JmxUser", "java.lang.String" };
		try {
			MBeanServerConnection mbeanServerConnection = jmxConnector.getMBeanServerConnection();
			mbeanServerConnection.invoke(new ObjectName("MBeans:type=2"), "startUserBasedJob", paramList, signature);
			TEJmxMpClient.disconnect(jmxConnector);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return;
	}
	
	@SuppressWarnings("unchecked")
	public static ArrayList<Resource> getAvailableResourcesForJob(JmxUser jmxUser, String jobPath) {

		JMXConnector jmxConnector = TEJmxMpClient.getJMXConnection();

		Object[] paramList = { jmxUser, jobPath };
		String[] signature = { "com.likya.tlossw.model.jmx.JmxUser", "java.lang.String" };
		Object o;
		try {
			MBeanServerConnection mbeanServerConnection = jmxConnector.getMBeanServerConnection();
			o = mbeanServerConnection.invoke(new ObjectName("MBeans:type=2"), "getAvailableResourcesForJob", paramList, signature);
			TEJmxMpClient.disconnect(jmxConnector);
			return (ArrayList<Resource>)o;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static boolean assignAgentForJob(JmxUser jmxUser, String jobPath, String agentId) {

		JMXConnector jmxConnector = TEJmxMpClient.getJMXConnection();

		Object[] paramList = { jmxUser, jobPath, agentId };
		String[] signature = { "com.likya.tlossw.model.jmx.JmxUser", "java.lang.String", "java.lang.String" };
		Object o;
		try {
			MBeanServerConnection mbeanServerConnection = jmxConnector.getMBeanServerConnection();
			o = mbeanServerConnection.invoke(new ObjectName("MBeans:type=2"), "assignAgentForJob", paramList, signature);
			TEJmxMpClient.disconnect(jmxConnector);
			return ((Boolean) o).booleanValue();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * File operations
	 */

	public static boolean checkFile(JmxUser jmxUser, String fileName) {

		JMXConnector jmxConnector = TEJmxMpClient.getJMXConnection();		

		Object[] paramList = { jmxUser, fileName };
		String[] signature = { "com.likya.tlossw.model.jmx.JmxUser", "java.lang.String" };
		Object o;
		try {
			MBeanServerConnection mbeanServerConnection = jmxConnector.getMBeanServerConnection();
			o = mbeanServerConnection.invoke(new ObjectName("MBeans:type=3"), "checkFile", paramList, signature);
			TEJmxMpClient.disconnect(jmxConnector);
			return ((Boolean) o).booleanValue();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public static StringBuffer readFile(JmxUser jmxUser, String fileName) {

		JMXConnector jmxConnector = TEJmxMpClient.getJMXConnection();

		Object[] paramList = { jmxUser, fileName };
		String[] signature = { "com.likya.tlossw.model.jmx.JmxUser", "java.lang.String" };
		Object o;
		try {
			MBeanServerConnection mbeanServerConnection = jmxConnector.getMBeanServerConnection();
			o = mbeanServerConnection.invoke(new ObjectName("MBeans:type=3"), "readFile", paramList, signature);
			TEJmxMpClient.disconnect(jmxConnector);
			return (StringBuffer) o;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static StringBuffer readFile(JmxUser jmxUser, String fileName, String coloredLineIndicator, boolean useSections, boolean isXML) {

		JMXConnector jmxConnector = TEJmxMpClient.getJMXConnection();

		Object[] paramList = { jmxUser, fileName, coloredLineIndicator, useSections, isXML };
		String[] signature = { "com.likya.tlossw.model.jmx.JmxUser", "java.lang.String", "java.lang.String", "java.lang.Boolean", "java.lang.Boolean" };
		Object o;
		try {
			MBeanServerConnection mbeanServerConnection = jmxConnector.getMBeanServerConnection();
			o = mbeanServerConnection.invoke(new ObjectName("MBeans:type=3"), "checkFile", paramList, signature);
			TEJmxMpClient.disconnect(jmxConnector);
			return (StringBuffer) o;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public static ArrayList<String> retrieveViewFiles(JmxUser jmxUser) {

		JMXConnector jmxConnector = TEJmxMpClient.getJMXConnection();

		Object[] paramList = { jmxUser };
		String[] signature = { "com.likya.tlossw.model.jmx.JmxUser" };
		Object o;
		try {
			MBeanServerConnection mbeanServerConnection = jmxConnector.getMBeanServerConnection();
			o = mbeanServerConnection.invoke(new ObjectName("MBeans:type=1"), "retrieveViewFiles", paramList, signature);
			TEJmxMpClient.disconnect(jmxConnector);
			return (ArrayList<String>) o;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void stopScenario(JmxUser jmxUser, String scenarioId, boolean isForced) {

		JMXConnector jmxConnector = TEJmxMpClient.getJMXConnection();

		Object[] paramList = { jmxUser, scenarioId, new Boolean(isForced) };
		String[] signature = { "com.likya.tlossw.model.jmx.JmxUser", "java.lang.String", "java.lang.Boolean" };

		try {
			MBeanServerConnection mbeanServerConnection = jmxConnector.getMBeanServerConnection();
			mbeanServerConnection.invoke(new ObjectName("MBeans:type=2"), "stopScenario", paramList, signature);
			TEJmxMpClient.disconnect(jmxConnector);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return;
	}

	public static void restartScenario(JmxUser jmxUser, String scenarioId) {
		JMXConnector jmxConnector = TEJmxMpClient.getJMXConnection();

		Object[] paramList = { jmxUser, scenarioId };
		String[] signature = { "com.likya.tlossw.model.jmx.JmxUser", "java.lang.String" };

		try {
			MBeanServerConnection mbeanServerConnection = jmxConnector.getMBeanServerConnection();
			mbeanServerConnection.invoke(new ObjectName("MBeans:type=2"), "restartScenario", paramList, signature);
			TEJmxMpClient.disconnect(jmxConnector);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return;
	}

	public static void resumeScenario(JmxUser jmxUser, String scenarioId) {
		JMXConnector jmxConnector = TEJmxMpClient.getJMXConnection();

		Object[] paramList = { jmxUser, scenarioId };
		String[] signature = { "com.likya.tlossw.model.jmx.JmxUser", "java.lang.String" };

		try {
			MBeanServerConnection mbeanServerConnection = jmxConnector.getMBeanServerConnection();
			mbeanServerConnection.invoke(new ObjectName("MBeans:type=2"), "resumeScenario", paramList, signature);
			TEJmxMpClient.disconnect(jmxConnector);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return;
	}

	public static void suspendScenario(JmxUser jmxUser, String scenarioId) {
		JMXConnector jmxConnector = TEJmxMpClient.getJMXConnection();

		Object[] paramList = { jmxUser, scenarioId };
		String[] signature = { "com.likya.tlossw.model.jmx.JmxUser", "java.lang.String" };

		try {
			MBeanServerConnection mbeanServerConnection = jmxConnector.getMBeanServerConnection();
			mbeanServerConnection.invoke(new ObjectName("MBeans:type=2"), "suspendScenario", paramList, signature);
			TEJmxMpClient.disconnect(jmxConnector);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return;
	}

	public static TlosJmxReturnValue addJob(JmxUser jmxUser, String jobPropertiesXML) {

		JMXConnector jmxConnector = TEJmxMpClient.getJMXConnection();

		Object[] paramList = { jmxUser, jobPropertiesXML };
		String[] signature = { "com.likya.tlossw.model.jmx.JmxUser", "java.lang.String" };
		Object o;
		try {
			MBeanServerConnection mbeanServerConnection = jmxConnector.getMBeanServerConnection();
			o = mbeanServerConnection.invoke(new ObjectName("MBeans:type=2"), "addJob", paramList, signature);
			TEJmxMpClient.disconnect(jmxConnector);
			return  (TlosJmxReturnValue) o;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new TlosJmxReturnValue(MessagesCodeMapping.fetchTlosGuiMessage(MessagesCodeMapping.JMX_ERROR), null);
	}

	public static TreeInfoType retrieveTreeInfo(JmxUser jmxUser, String instanceId, ArrayList<String> scenariodIdList) {

		JMXConnector jmxConnector = TEJmxMpClient.getJMXConnection();

		Object[] paramList = { jmxUser, instanceId, scenariodIdList };
		String[] signature = { "com.likya.tlossw.model.jmx.JmxUser", "java.lang.String", "java.util.ArrayList" };
		Object o;
		try {
			MBeanServerConnection mbeanServerConnection = jmxConnector.getMBeanServerConnection();
			o = mbeanServerConnection.invoke(new ObjectName("MBeans:type=1"), "retrieveTreeInfo", paramList, signature);
			TEJmxMpClient.disconnect(jmxConnector);
			return (TreeInfoType) o;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;

	}

	public static boolean retrieveWaitConfirmOfGUI(JmxUser jmxUser) {

		JMXConnector jmxConnector = TEJmxMpClient.getJMXConnection();

		Object[] paramList = { jmxUser };
		String[] signature = { "com.likya.tlossw.model.jmx.JmxUser" };
		Object o;
		try {
			MBeanServerConnection mbeanServerConnection = jmxConnector.getMBeanServerConnection();
			o = mbeanServerConnection.invoke(new ObjectName("MBeans:type=1"), "retrieveWaitConfirmOfGUI", paramList, signature);
			TEJmxMpClient.disconnect(jmxConnector);
			return ((Boolean) o).booleanValue();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;

	}

	@SuppressWarnings("unchecked")
	public static ArrayList<String> retrieveInstanceIds(JmxUser jmxUser) {

		JMXConnector jmxConnector = TEJmxMpClient.getJMXConnection();

		Object[] paramList = { jmxUser };
		String[] signature = { "com.likya.tlossw.model.jmx.JmxUser" };
		Object o;

		try {
			MBeanServerConnection mbeanServerConnection = jmxConnector.getMBeanServerConnection();
			o = mbeanServerConnection.invoke(new ObjectName("MBeans:type=1"), "retrieveInstanceIds", paramList, signature);
			TEJmxMpClient.disconnect(jmxConnector);
			return (ArrayList<String>) o;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static InfoTypeClient getInfoTypeClient(JmxUser jmxUser, String instanceId, String treePath) {
		JMXConnector jmxConnector = TEJmxMpClient.getJMXConnection();

		Object[] paramList = { jmxUser, instanceId, treePath };
		String[] signature = { "com.likya.tlossw.model.jmx.JmxUser", "java.lang.String", "java.lang.String" };
		Object o;

		try {
			MBeanServerConnection mbeanServerConnection = jmxConnector.getMBeanServerConnection();
			o = mbeanServerConnection.invoke(new ObjectName("MBeans:type=1"), "getInfoTypeClient", paramList, signature);
			TEJmxMpClient.disconnect(jmxConnector);
			return (InfoTypeClient) o;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static ScenarioNode getLiveTreeInfo(JmxUser jmxUser, ScenarioNode scenarioNode) {

		JMXConnector jmxConnector = TEJmxMpClient.getJMXConnection();

		Object[] paramList = { jmxUser, scenarioNode};
		String[] signature = { "com.likya.tlossw.model.jmx.JmxUser", "com.likya.tlossw.model.tree.ScenarioNode"};
		Object o;

		try {
			MBeanServerConnection mbeanServerConnection = jmxConnector.getMBeanServerConnection();
			o = mbeanServerConnection.invoke(new ObjectName("MBeans:type=1"), "getLiveTreeInfo", paramList, signature);
			TEJmxMpClient.disconnect(jmxConnector);
			return (ScenarioNode) o;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static TlosSpaceWideNode getLiveTreeInfo(JmxUser jmxUser, TlosSpaceWideNode tlosSpaceWideNode) {

		JMXConnector jmxConnector = TEJmxMpClient.getJMXConnection();

		Object[] paramList = { jmxUser, tlosSpaceWideNode};
		String[] signature = { "com.likya.tlossw.model.jmx.JmxUser", "com.likya.tlossw.model.tree.TlosSpaceWideNode"};

		try {
			MBeanServerConnection mbeanServerConnection = jmxConnector.getMBeanServerConnection();
			Object o = mbeanServerConnection.invoke(new ObjectName("MBeans:type=1"), "getLiveTreeInfo", paramList, signature);
			TEJmxMpClient.disconnect(jmxConnector);
			
			TlosSpaceWideNode tmp = (TlosSpaceWideNode) o;
			
			return tmp;
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("NULL olmamali !!");
		return null;
	}
	/**
	 * Management Functions
	 */
	public static void recover(JmxUser jmxUser) {

		JMXConnector jmxConnector = TEJmxMpClient.getJMXConnection();

		Object[] paramList = { jmxUser };
		String[] signature = { "com.likya.tlossw.model.jmx.JmxUser" };

		try {
			MBeanServerConnection mbeanServerConnection = jmxConnector.getMBeanServerConnection();
			mbeanServerConnection.invoke(new ObjectName("MBeans:type=2"), "recover", paramList, signature);
			TEJmxMpClient.disconnect(jmxConnector);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return;
	}
	
	public static void shiftSolstice(JmxUser jmxUser, boolean backupReports) {

		JMXConnector jmxConnector = TEJmxMpClient.getJMXConnection();

		Object[] paramList = { jmxUser, backupReports };
		String[] signature = { "com.likya.tlossw.model.jmx.JmxUser", "boolean" };

		try {
			MBeanServerConnection mbeanServerConnection = jmxConnector.getMBeanServerConnection();
			mbeanServerConnection.invoke(new ObjectName("MBeans:type=2"), "shiftSolstice", paramList, signature);
			TEJmxMpClient.disconnect(jmxConnector);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return;
	}
	
	public static void startOver(JmxUser jmxUser, boolean backupReports) {

		JMXConnector jmxConnector = TEJmxMpClient.getJMXConnection();

		Object[] paramList = { jmxUser, backupReports };
		String[] signature = { "com.likya.tlossw.model.jmx.JmxUser", "boolean" };

		try {
			MBeanServerConnection mbeanServerConnection = jmxConnector.getMBeanServerConnection();
			mbeanServerConnection.invoke(new ObjectName("MBeans:type=2"), "startOver", paramList, signature);
			TEJmxMpClient.disconnect(jmxConnector);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return;
	}
	
	public static WebSpaceWideRegistery retrieveWebSpaceWideRegistery(JmxUser jmxUser) {

		JMXConnector jmxConnector = TEJmxMpClient.getJMXConnection();
		
		Object[] paramList = { jmxUser };
		String[] signature = { "com.likya.tlossw.model.jmx.JmxUser" };
		Object o;
		try {
			MBeanServerConnection mbeanServerConnection = jmxConnector.getMBeanServerConnection();
			o = mbeanServerConnection.invoke(new ObjectName("MBeans:type=1"), "retrieveSpaceWideRegistery", paramList, signature);
			TEJmxMpClient.disconnect(jmxConnector);
			return (WebSpaceWideRegistery) o;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;

	}

	public static void forceCpcStart(JmxUser jmxUser) {

		JMXConnector jmxConnector = TEJmxMpClient.getJMXConnection();

		Object[] paramList = { jmxUser };
		String[] signature = { "com.likya.tlossw.model.jmx.JmxUser" };

		try {
			MBeanServerConnection mbeanServerConnection = jmxConnector.getMBeanServerConnection();
			mbeanServerConnection.invoke(new ObjectName("MBeans:type=2"), "forceCpcStart", paramList, signature);
			TEJmxMpClient.disconnect(jmxConnector);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return;
	}
	
	
	/**
	 * Live Resource Operations
	 */
	
	//Web ekranindaki kaynak listesi agaci render edilmeden once guncel data isteniyor
	/**
	 * Sunucudan kullanilabilir makine bilgilerini istiyor
	 * 
	 * @param jmxUser Jmx sunucusuna baglanmak icin gerekli kullanici bilgileri
	 * @param tlosSpaceWideNode Web ekranindaki agacta acik olan makine bilgileri
	 * @return Sunucudan aldigi kullanilabilir makine bilgilerini donuyor
	 */
	public static TlosSWResourceNode getLiveResourceTreeInfo(JmxUser jmxUser, TlosSWResourceNode tlosSpaceWideNode) {

		JMXConnector jmxConnector = TEJmxMpClient.getJMXConnection();

		Object[] paramList = { jmxUser, tlosSpaceWideNode};
		String[] signature = { "com.likya.tlossw.model.jmx.JmxUser", "com.likya.tlossw.model.tree.resource.TlosSWResourceNode"};
		Object o;

		try {
			MBeanServerConnection mbeanServerConnection = jmxConnector.getMBeanServerConnection();
			o = mbeanServerConnection.invoke(new ObjectName("MBeans:type=1"), "getLiveResourceTreeInfo", paramList, signature);
			TEJmxMpClient.disconnect(jmxConnector);
			return (TlosSWResourceNode) o;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	//Web ekranindaki kaynak listesi agacinda herhangi bir Tlos Agent secildiginde buraya gelip agent bilgilerini aliyor
	/**
	 * Sunucudan Tlos Agent bilgilerini istiyor
	 * 
	 * @param jmxUser Jmx sunucusuna baglanmak icin gerekli kullanici bilgileri
	 * @param tlosAgentId Bilgileri istenen Tlos Agent'in id numarasi
	 * @return Sunucudan aldigi Tlos Agent bilgilerini donuyor
	 */
	public static TlosAgentInfoTypeClient retrieveTlosAgentInfo(JmxUser jmxUser, int tlosAgentId) {

		JMXConnector jmxConnector = TEJmxMpClient.getJMXConnection();

		Object[] paramList = { jmxUser, tlosAgentId };
		String[] signature = { "com.likya.tlossw.model.jmx.JmxUser", "int" };
		Object o;

		try {
			MBeanServerConnection mbeanServerConnection = jmxConnector.getMBeanServerConnection();
			o = mbeanServerConnection.invoke(new ObjectName("MBeans:type=1"), "retrieveTlosAgentInfo", paramList, signature);
			TEJmxMpClient.disconnect(jmxConnector);
			return (TlosAgentInfoTypeClient) o;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	//Agent bilgilerinden sonra da o agentta calisan job bilgilerini aliyor
	/**
	 * Sunucudan Tlos Agent'ta calisan job bilgilerini istiyor
	 * 
	 * @param jmxUser Jmx sunucusuna baglanmak icin gerekli kullanici bilgileri
	 * @param tlosAgentId Bilgileri istenen Tlos Agent'in id numarasi
	 * @return Sunucudan aldigi job bilgilerini donuyor
	 */
	@SuppressWarnings("unchecked")
	public static ArrayList<JobInfoTypeClient> getAgentsJobList(JmxUser jmxUser, int tlosAgentId, boolean transformToLocalTime) {

		JMXConnector jmxConnector = TEJmxMpClient.getJMXConnection();

		Object[] paramList = { jmxUser, tlosAgentId, transformToLocalTime };
		String[] signature = { "com.likya.tlossw.model.jmx.JmxUser", "int", "java.lang.Boolean" };
		Object o;

		try {
			MBeanServerConnection mbeanServerConnection = jmxConnector.getMBeanServerConnection();
			o = mbeanServerConnection.invoke(new ObjectName("MBeans:type=1"), "getAgentsJobList", paramList, signature);
			TEJmxMpClient.disconnect(jmxConnector);
			return (ArrayList<JobInfoTypeClient>) o;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Sunucuya ilgili Tlos Agent'i devre disi birakma istegi gonderiyor
	 * 
	 * @param jmxUser Jmx sunucusuna baglanmak icin gerekli kullanici bilgileri
	 * @param tlosAgentId Tlos Agent'in id numarasi
	 * @param isForced Hemen devre disi birakma parametresi. Tlos Agent'ta calisan islerin bitmesi beklenecekse false beklenmeyecekse true veriliyor
	 */
	public static void deactivateTlosAgent(JmxUser jmxUser, int tlosAgentId, boolean isForced) {

		JMXConnector jmxConnector = TEJmxMpClient.getJMXConnection();

		Object[] paramList = { jmxUser, tlosAgentId, new Boolean(isForced) };
		String[] signature = { "com.likya.tlossw.model.jmx.JmxUser", "int", "java.lang.Boolean" };

		try {
			MBeanServerConnection mbeanServerConnection = jmxConnector.getMBeanServerConnection();
			mbeanServerConnection.invoke(new ObjectName("MBeans:type=2"), "deactivateTlosAgent", paramList, signature);
			TEJmxMpClient.disconnect(jmxConnector);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return;
	}
	
	/**
	 * Sunucuya ilgili Tlos Agent'i devreye alma istegi gonderiyor
	 * 
	 * @param jmxUser Jmx sunucusuna baglanmak icin gerekli kullanici bilgileri
	 * @param tlosAgentId Tlos Agent'in id numarasi
	 */
	public static void activateTlosAgent(JmxUser jmxUser, int tlosAgentId) {

		JMXConnector jmxConnector = TEJmxMpClient.getJMXConnection();

		Object[] paramList = { jmxUser, tlosAgentId };
		String[] signature = { "com.likya.tlossw.model.jmx.JmxUser", "int" };

		try {
			MBeanServerConnection mbeanServerConnection = jmxConnector.getMBeanServerConnection();
			mbeanServerConnection.invoke(new ObjectName("MBeans:type=2"), "activateTlosAgent", paramList, signature);
			TEJmxMpClient.disconnect(jmxConnector);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return;
	}
	
	/**
	 * Sunucudan agent listesini istiyor
	 * 
	 * @param jmxUser Jmx sunucusuna baglanmak icin gerekli kullanici bilgileri
	 * @return Sunucudan aldigi agent listesini donuyor
	 */
	@SuppressWarnings("unchecked")
	public static ArrayList<SWAgent> getAgentList(JmxUser jmxUser) {

		JMXConnector jmxConnector = TEJmxMpClient.getJMXConnection();

		Object[] paramList = { jmxUser };
		String[] signature = { "com.likya.tlossw.model.jmx.JmxUser" };
		Object o;

		try {
			MBeanServerConnection mbeanServerConnection = jmxConnector.getMBeanServerConnection();
			o = mbeanServerConnection.invoke(new ObjectName("MBeans:type=1"), "getAgentList", paramList, signature);
			TEJmxMpClient.disconnect(jmxConnector);
			return (ArrayList<SWAgent>) o;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Senaryo agacini, gun donumunun gelmesini beklemeden o anda baslatmasi icin sunucudan istekte bulunuyor
	 * 
	 * @param jmxUser Jmx sunucusuna baglanmak icin gerekli kullanici bilgileri
	 */
	public static void restartScenarioTree(JmxUser jmxUser) {

		JMXConnector jmxConnector = TEJmxMpClient.getJMXConnection();

		Object[] paramList = { jmxUser };
		String[] signature = { "com.likya.tlossw.model.jmx.JmxUser" };
		
		try {
			MBeanServerConnection mbeanServerConnection = jmxConnector.getMBeanServerConnection();
			mbeanServerConnection.invoke(new ObjectName("MBeans:type=2"), "restartScenarioTree", paramList, signature);
			TEJmxMpClient.disconnect(jmxConnector);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return;
	}
	
	/**
	 * O gunku instance'lar icerisinde joblari bitmemis instance olup olmadigini sunucuya soruyor
	 * 
	 * @param jmxUser Jmx sunucusuna baglanmak icin gerekli kullanici bilgileri
	 * @return calisan instance varsa true yoksa false donuyor
	 */
	public static boolean runningInstanceExists(JmxUser jmxUser) {

		JMXConnector jmxConnector = TEJmxMpClient.getJMXConnection();

		Object[] paramList = { jmxUser };
		String[] signature = { "com.likya.tlossw.model.jmx.JmxUser" };
		Object o;
		try {
			MBeanServerConnection mbeanServerConnection = jmxConnector.getMBeanServerConnection();
			o = mbeanServerConnection.invoke(new ObjectName("MBeans:type=1"), "runningInstanceExists", paramList, signature);
			TEJmxMpClient.disconnect(jmxConnector);
			return ((Boolean) o).booleanValue();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return true;
	}
	
	
}
