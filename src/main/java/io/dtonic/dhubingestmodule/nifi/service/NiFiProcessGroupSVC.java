package io.dtonic.dhubingestmodule.nifi.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.dtonic.dhubingestmodule.common.code.MonitoringCode;
import io.dtonic.dhubingestmodule.history.aop.task.TaskHistory;
import io.dtonic.dhubingestmodule.nifi.client.NiFiApiClient;
import io.swagger.client.model.ProcessGroupDTO;
import io.swagger.client.model.ProcessGroupEntity;
import io.swagger.client.model.ProcessGroupFlowEntity;
import io.swagger.client.model.RemotePortRunStatusEntity;
import io.swagger.client.model.RemoteProcessGroupEntity;
import io.swagger.client.model.RevisionDTO;
import io.swagger.client.model.ScheduleComponentsEntity;
import io.swagger.client.model.ScheduleComponentsEntity.StateEnum;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class NiFiProcessGroupSVC {

    @Autowired
    private NiFiApiClient niFiClient;

    /**
     * Create Pipeline Processor Group
     *
     * @param processGroupName processGroupName
     * @param rootProcessGroupId root
     * @return ProcessGroupEntity
     */
    @TaskHistory(taskName = MonitoringCode.CREATE_PROCESSGROUP)
    public ProcessGroupEntity createProcessGroup(Integer commandId, String processGroupName, String rootProcessGroupId) throws Exception {
        try {
            ProcessGroupEntity body = new ProcessGroupEntity();
            ProcessGroupDTO processGroupSetting = new ProcessGroupDTO();
            RevisionDTO processGroupRevision = new RevisionDTO();

            processGroupSetting.setName(processGroupName);
            processGroupRevision.setVersion(0L);
            processGroupRevision.setClientId(rootProcessGroupId);
            body.setComponent(processGroupSetting);
            body.setRevision(processGroupRevision);
            ProcessGroupEntity result = niFiClient
                .getProcessGroups()
                .createProcessGroup(rootProcessGroupId, body, null);
            log.info(
                "Success Create Process Group : Process Group ID = [{}]",
                result.getId()
            );
            return result;
        } catch (Exception e) {
            log.error("Fail to Create Process Group : {}", processGroupName, e);
            return null;
        }
        
    }
    @TaskHistory(taskName = MonitoringCode.DELETE_PROCESSGROUP)
    public boolean deleteProcessGroup(Integer commandId, String processGroupId) {
        try {
            String version = niFiClient
                .getProcessGroups()
                .getProcessGroup(processGroupId)
                .getRevision()
                .getVersion()
                .toString();
            niFiClient
                .getProcessGroups()
                .removeProcessGroup(processGroupId, version, null, null);
            log.info("Success Delete Process Group : Process Group ID = [{}]", processGroupId);
            return true;
        } catch (Exception e) {
            log.error("Fail to Delete Process Group : Process Group ID = [{}]", processGroupId);
            return false;
        }
    }

    
    public boolean updateStatusprocessGroup(String processGroupId, StateEnum status) {
        try {
            ScheduleComponentsEntity body = new ScheduleComponentsEntity();
            body.setState(status);
            body.setId(processGroupId);
            body.setDisconnectedNodeAcknowledged(false);
            ScheduleComponentsEntity result = niFiClient.getFlow().scheduleComponents(processGroupId, body);
            if (result != null){
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            log.error("Fail to update status [{}] of Processor Group [{}]", status.getValue(), processGroupId, e);
            return false;
        }
    }
    @TaskHistory(taskName = MonitoringCode.RUN_PIPELINE)
    public RemoteProcessGroupEntity startProcessorGroup(Integer commandId, String processorGroupId){
        try {
            RemotePortRunStatusEntity body = new RemotePortRunStatusEntity();
            body.setState(RemotePortRunStatusEntity.StateEnum.TRANSMITTING);
            body.setDisconnectedNodeAcknowledged(false);
            ScheduleComponentsEntity bodySchedule = new ScheduleComponentsEntity();
            bodySchedule.setId(processorGroupId);
            bodySchedule.setState(ScheduleComponentsEntity.StateEnum.RUNNING);
            bodySchedule.setDisconnectedNodeAcknowledged(false);

            log.info("Success Request Run Pipeline : Processor Group ID = {}", processorGroupId);
            RemoteProcessGroupEntity res = niFiClient.getRemoteProcessGroups().updateRemoteProcessGroupRunStatuses(processorGroupId, body);
            niFiClient.getFlow().scheduleComponents(processorGroupId, bodySchedule);
            return res;
        } catch (Exception e) {
            if (e.getMessage().contains("Bad Request")){
                RemoteProcessGroupEntity alreadyRes = new RemoteProcessGroupEntity();
                log.info("Already Started Processor Group : [{}]",processorGroupId);
                return alreadyRes;
            }
            log.error("Fail to start Processor Group : [{}]", processorGroupId, e);
            return null;
        }
    }
    @TaskHistory(taskName = MonitoringCode.STOP_PIPELINE)
    public RemoteProcessGroupEntity stopProcessorGroup(Integer commandId, String processorGroupId){
        try {
            RemotePortRunStatusEntity body = new RemotePortRunStatusEntity();
            body.setState(RemotePortRunStatusEntity.StateEnum.STOPPED);
            body.setDisconnectedNodeAcknowledged(false);
            ScheduleComponentsEntity bodySchedule = new ScheduleComponentsEntity();
            bodySchedule.setId(processorGroupId);
            bodySchedule.setState(ScheduleComponentsEntity.StateEnum.STOPPED);
            bodySchedule.setDisconnectedNodeAcknowledged(false);

            log.info("Success Request Stop Pipeline : Processor Group ID = {}", processorGroupId);
            RemoteProcessGroupEntity res = niFiClient.getRemoteProcessGroups().updateRemoteProcessGroupRunStatuses(processorGroupId, body);
            niFiClient.getFlow().scheduleComponents(processorGroupId, bodySchedule);
            return res;
        } catch (Exception e) {
            if (e.getMessage().contains("Bad Request")){
                RemoteProcessGroupEntity alreadyRes = new RemoteProcessGroupEntity();
                log.info("Already Stopped Processor Group : [{}]",processorGroupId);
                return alreadyRes;
            }
            log.error("Fail to stop Processor Group : [{}]",processorGroupId, e);
            return null;
        }
    }

    
    public Map<String, Integer> getStatusProcessGroup(String processGroupId)
         {
            try {
                ProcessGroupFlowEntity resultEntity = niFiClient.getFlow().getFlow(processGroupId, true);
                log.debug("Success get Status Process Group : Process Group ID = [{}]", processGroupId);
                return getNumberOfProcessorStatus(resultEntity);
            } catch (Exception e) {
                log.error("Fail to Get Status Process Group : Process Group ID = [{}]", processGroupId, e);
                return null;
            }
    }

    protected Map<String, Integer> getNumberOfProcessorStatus(
        ProcessGroupFlowEntity processGroupStatus
    ) {
        Map<String, Integer> result = new HashMap<>();
        Integer runCnt = 0;
        Integer stopCnt = 0;
        Integer invaildCnt = 0;
        try {
            for (ProcessGroupEntity processorStatus : processGroupStatus
                .getProcessGroupFlow()
                .getFlow()
                .getProcessGroups()) {
                runCnt += processorStatus.getRunningCount();
                stopCnt += processorStatus.getStoppedCount();
                invaildCnt += processorStatus.getInvalidCount();
            }
            result.put("Running", runCnt);
            result.put("Stopped", stopCnt);
            result.put("Invaild", invaildCnt);
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Search Process Group In Ingest Manager
     * When search funnel, get first element of funnels array because one funnel exist in Ingest Manager
     *
     * @param sourceprocessGroupID pipeline processor group id
     * @return FunnelID
     */
    public String searchProcessGroupInProcessGroup(String processGroupId, String findProcessGroupName) {
        try {
            for (ProcessGroupEntity processGroup : niFiClient
                .getProcessGroups()
                .getProcessGroups(processGroupId)
                .getProcessGroups()) {
                log.info(
                    "Search Process Group In Ingest Manager : processGroupId = [{}]",
                    processGroup.getId()
                );
                if (processGroup.getComponent().getName().equals(findProcessGroupName)) {
                    return processGroup.getId();
                }
            }
            log.info("Empty process Group In Ingest Manager");
            return null;
            
        } catch (Exception e) {
            return null;
        }
    }

}
