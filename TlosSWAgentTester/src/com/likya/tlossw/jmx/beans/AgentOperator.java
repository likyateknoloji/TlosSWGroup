package com.likya.tlossw.jmx.beans;
/*
 * TlosFaz_V2.0
 * com.likya.tlos.jmx.mp.helper : ProcessInfoProvider.java
 * @author Serkan Ta�
 * Tarih : Apr 6, 2009 2:19:17 PM
 */



import com.likya.tlos.model.xmlbeans.agent.TxMessageDocument.TxMessage;
import com.likya.tlos.model.xmlbeans.agent.TxMessageTypeEnumerationDocument.TxMessageTypeEnumeration;
import com.likya.tlossw.TlosSpaceWide;
import com.likya.tlossw.core.spc.jobs.Job;
import com.likya.tlossw.db.utils.AgentDbUtils;
import com.likya.tlossw.jmx.JMXTLSServer;
import com.likya.tlossw.model.engine.TxMessageIdBean;
import com.likya.tlossw.model.jmx.JmxUser;
import com.likya.tlossw.utils.XmlUtils;

public class AgentOperator implements AgentOperatorMBean {

	@Override
	public int getNbChanges() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getState() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setState(String s) {
		// TODO Auto-generated method stub

	}

	@Override
	public int intsquare(int x) {
		return x * x;
	}

	@Override
	public int checkJmxUser(JmxUser jmxUser) {
		int checkJmx = AgentDbUtils.checkJmxUser(jmxUser);
		return checkJmx;
	}

	@Override
	public boolean txMessageHandle(JmxUser jmxUser, String txMessageXML) {
//		if (!JMXServer.authorize(jmxUser)) {
//			return false;
//		}

		System.out.println(txMessageXML);
		TxMessage txMessage = XmlUtils.convertToTxMessage(txMessageXML);
		TxMessageIdBean txMessageIdBean = XmlUtils.tokenizeTxIds(txMessage.getId());
		boolean isAgentAvailable = TlosSpaceWide.getSpaceWideRegistry().getAgentManagerReference().getSwAgentCache(txMessageIdBean.getAgentId() + "").getJmxAvailable();

		if (isAgentAvailable) {

			if (txMessage.getTxMessageTypeEnumeration().equals(TxMessageTypeEnumeration.JOB_STATE)) {

				String instanceId = txMessageIdBean.getInstanceId();
				String spcId = txMessageIdBean.getSpcId();
				String jobId = txMessageIdBean.getJobKey();

				Job job = TlosSpaceWide.getSpaceWideRegistry().getInstanceLookupTable().get(instanceId).getSpcLookupTable().get(spcId).getSpcReferance().getJobQueue().get(jobId);
				job.addStateInfo(txMessage.getTxMessageBodyType().getLiveStateInfo());
				// job.changeStateInfo(txMessage.getTxMessageBodyType().getLiveStateInfo());
			} else if (txMessage.getTxMessageTypeEnumeration().equals(TxMessageTypeEnumeration.JOB)) {
				Job job = TlosSpaceWide.getSpaceWideRegistry().getInstanceLookupTable().get(txMessageIdBean.getInstanceId()).getSpcLookupTable().get(txMessageIdBean.getSpcId()).getSpcReferance().getJobQueue().get(txMessageIdBean.getJobKey());
				job.sendEndInfo(txMessageIdBean.getSpcId(), txMessage.getTxMessageBodyType().getJobProperties());
				if (txMessage.getTxMessageBodyType().getJobProperties().getAgentId() != 0) { // Baska
																								// ne
																								// olabilir
																								// ki?
					if (job.getJobRuntimeProperties().getJobProperties().getTimeManagement().getJsRealTime().getStartTime() == null) {
						job.getJobRuntimeProperties().getJobProperties().getTimeManagement().getJsRealTime().addNewStartTime().setTime(txMessage.getTxMessageBodyType().getJobProperties().getTimeManagement().getJsRealTime().getStartTime().getTime());
					} else {
						job.getJobRuntimeProperties().getJobProperties().getTimeManagement().getJsRealTime().getStartTime().setTime(txMessage.getTxMessageBodyType().getJobProperties().getTimeManagement().getJsRealTime().getStartTime().getTime());
					}
					
					if(txMessage.getTxMessageBodyType().getJobProperties().getTimeManagement().getJsRealTime().getStopTime() != null) {
						job.getJobRuntimeProperties().getJobProperties().getTimeManagement().getJsRealTime().addNewStopTime().setTime(txMessage.getTxMessageBodyType().getJobProperties().getTimeManagement().getJsRealTime().getStopTime().getTime());
					}
					/*
					 * job.getJobRuntimeProperties().setPlannedExecutionDate(
					 * txMessage
					 * .getTxMessageBodyType().getJobProperties().getJobRealTime
					 * ().getStartTime().getTime()+"");
					 * job.getJobRuntimeProperties
					 * ().setRealExecutionDate(txMessage
					 * .getTxMessageBodyType().getJobProperties
					 * ().getJobRealTime().getStartTime().getTime());
					 * job.getJobRuntimeProperties
					 * ().setPlannedExecutionDate(txMessage
					 * .getTxMessageBodyType().getPlannedExecutionDate());
					 * 
					 * addNewJobRealTime().addNewStartTime().setTime(txMessage.
					 * getTxMessageBodyType
					 * ().getJobProperties().getJobRealTime()
					 * .getStartTime().getTime());
					 */
				}
			}

		}

		return true;
	}

	@Override
	public void pulse(JmxUser jmxUser) {
		TlosSpaceWide.getSpaceWideRegistry().getAgentManagerReference().updateHeartBeatTime(jmxUser);
	}

	public Object retrieveGlobalStates(JmxUser jmxUser) {

		if (!JMXTLSServer.authorizeAgent(jmxUser)) {
			return false;
		}

		String globalStateDefinitionXML = XmlUtils.getGlobalStateDefinitionsXML(jmxUser);

		return globalStateDefinitionXML;
	}

}
