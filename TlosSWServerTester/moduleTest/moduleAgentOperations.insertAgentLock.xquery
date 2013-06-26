xquery version "1.0";
declare namespace res = "http://www.likyateknoloji.com/resource-extension-defs";
import module namespace lk="http://likya.tlos.com/" at "xmldb:exist://db/TLOSSW/modules//moduleAgentOperations.xquery";
lk:insertAgentLock("//db/TLOSSW", <agnt:SWAgent xmlns:agnt="http://www.likyateknoloji.com/XML_agent_types" xmlns:res="http://www.likyateknoloji.com/resource-extension-defs">
  <res:Resource>merve-laptop</res:Resource>
  <agnt:osType>Windows</agnt:osType>
  <agnt:agentType>agent</agnt:agentType>
  <agnt:nrpePort>2345</agnt:nrpePort>
  <agnt:jmxTlsPort>1234</agnt:jmxTlsPort>
  <agnt:jmxUser>test</agnt:jmxUser>
  <agnt:jmxPassword>test</agnt:jmxPassword>
  <agnt:inJmxAvailable>false</agnt:inJmxAvailable>
  <agnt:outJmxAvailable>false</agnt:outJmxAvailable>
  <agnt:jmxAvailable>false</agnt:jmxAvailable>
  <agnt:userStopRequest>null</agnt:userStopRequest>
  <agnt:nrpeAvailable>false</agnt:nrpeAvailable>
  <agnt:lastHeartBeatTime>0</agnt:lastHeartBeatTime>
  <agnt:durationForUnavailability>900</agnt:durationForUnavailability>
  <agnt:lastJobTransfer>false</agnt:lastJobTransfer>
  <agnt:jobTransferFailureTime>0</agnt:jobTransferFailureTime>
  <agnt:workspacePath>c:/</agnt:workspacePath>
</agnt:SWAgent>)