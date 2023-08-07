package io.dtonic.dhubingestmodule.nifi.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import io.dtonic.dhubingestmodule.nifi.client.NiFiClient;
import io.swagger.client.model.ProcessGroupDTO;
import io.swagger.client.model.ProcessGroupEntity;
import io.swagger.client.model.ProcessGroupFlowEntity;
import io.swagger.client.model.RevisionDTO;
import io.swagger.client.model.ScheduleComponentsEntity;
import io.swagger.client.model.ScheduleComponentsEntity.StateEnum;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class NiFiProcessGroupSVC {

    @Autowired
    private NiFiClient niFiClient;

    /**
     * Create Pipeline Processor Group
     *
     * @param processGroupName processGroupName
     * @param rootProcessGroupId root
     * @return ProcessGroupEntity
     */
    public ProcessGroupEntity createProcessGroup(String processGroupName, String rootProcessGroupId) throws Exception {
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
    
    public boolean deleteProcessGroup(String processGroupId) {
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
    
    public Map<String, Integer> getStatusProcessGroup(String processGroupId)
         {
            try {
                ProcessGroupFlowEntity resultEntity = niFiClient.getFlow().getFlow(processGroupId, true);
                log.info("Success get Status Process Group : Process Group ID = [{}]", processGroupId);
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
    public String searchProcessGroupInProcessGroup(String processGroupId) {
        try {
            for (ProcessGroupEntity processGroup : niFiClient
                .getProcessGroups()
                .getProcessGroups(processGroupId)
                .getProcessGroups()) {
                log.info(
                    "Search Process Group In Ingest Manager : processGroupId = [{}]",
                    processGroup.getId()
                );
                if (processGroup.getComponent().getName().equals("Ingest Manager"))
                return processGroup.getComponent().getName();
            }
            log.info("Not Exist Ingest Manager process Group In NiFi");
            return null;
            
        } catch (Exception e) {
            return null;
        }
    }


}
