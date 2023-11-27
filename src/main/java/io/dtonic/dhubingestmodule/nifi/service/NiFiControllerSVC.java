package io.dtonic.dhubingestmodule.nifi.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import io.dtonic.dhubingestmodule.common.code.MonitoringCode;
import io.dtonic.dhubingestmodule.history.aop.task.TaskHistory;
import io.dtonic.dhubingestmodule.nifi.client.NiFiApiClient;
import io.dtonic.dhubingestmodule.nifi.vo.PropertyVO;
import io.swagger.client.model.ActivateControllerServicesEntity;
import io.swagger.client.model.ControllerServiceEntity;
import io.swagger.client.model.ControllerServicesEntity;
import io.swagger.client.model.ActivateControllerServicesEntity.StateEnum;
import io.swagger.client.model.ControllerServiceStatusDTO.RunStatusEnum;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@Service
public class NiFiControllerSVC {

    @Autowired
    private NiFiApiClient niFiClient;
    
    public ControllerServiceEntity updateController(String controllerId, ControllerServiceEntity controllerEntity){
        try {
            return niFiClient.getControllerServices().updateControllerService(controllerId, controllerEntity);
        } catch (Exception e) {
            log.error("Fail to update Controller id : [{}]", controllerId, e);
            return null;
        }
    }

    public ControllerServiceEntity updateControllerProperties(
        ControllerServiceEntity controller,
        List<PropertyVO> properies
    ) {
        Map<String, String> setUpProperties = controller.getComponent().getProperties();
        for (PropertyVO property : properies) {
            setUpProperties.replace(property.getName(), property.getInputValue());
        }
        log.info("Properties : {}", setUpProperties);
        controller.getComponent().setProperties(setUpProperties);

        return controller;
    }

    public ControllerServicesEntity searchControllersInProcessorGroup(String processorGroupId) {
        try {
            ControllerServicesEntity resultEntity = niFiClient.getFlow().getControllerServicesFromGroup(processorGroupId, false, true, null, true);
            return resultEntity;
        } catch (Exception e) {
            log.error("Fail to Create Dummy Template.", e);
            return null;
        }
    }

    /**
     * Disable Controllers in process group to delete pipeline
     *
     * @param processGroupId
     * @return success/fail boolean
     */
    @TaskHistory(taskName = MonitoringCode.DISABLE_CONTROLLER)
    public boolean disableControllers(Integer commandId, String processorGroupId) {
        try {
            ActivateControllerServicesEntity body = new ActivateControllerServicesEntity();
            body.setState(StateEnum.DISABLED);
            body.setId(processorGroupId);
            body.setDisconnectedNodeAcknowledged(false);
            
            niFiClient.getFlow().activateControllerServices(processorGroupId, body);
            log.info(
                "Request Disable Controllers In Process Group : Process Group ID = [{}]",
                processorGroupId
            );
            try {
                Integer retryCnt = 0;
                while (retryCnt < 3) {
                    Integer disableControllerCnt = 0;
                    ControllerServicesEntity controllers = searchControllersInProcessorGroup(processorGroupId);
                    for (ControllerServiceEntity controller : controllers.getControllerServices()) {
                        if (controller.getStatus().getRunStatus().equals(RunStatusEnum.DISABLED)){
                            disableControllerCnt++;
                        }
                    }
                    if (disableControllerCnt == controllers.getControllerServices().size()) {
                        log.info("Success Disable Controllers In Process Group : Process Group ID = [{}]", processorGroupId);
                        return true;
                    }
                    /* Sleep 1.0s */
                    Thread.sleep(500);
                    retryCnt++;
                } 
                return false;
            } catch (InterruptedException e) {
                log.error("Interrupt Exception", e);
                return false;
            }
        } catch (Exception e) {
            log.error(
                "Fail to Disable Controllers In Process Group : Process Group ID = [{}]",
                processorGroupId,
                e
            );
            return false;
        }
    }

    /**
     * Enable Controllers in process group
     *
     * @param processGroupId
     */
    @TaskHistory(taskName = MonitoringCode.ENABLE_CONTROLLER)
    public Boolean enableControllers(Integer commandId, String processorGroupId) throws Exception{
        ActivateControllerServicesEntity body = new ActivateControllerServicesEntity();
        body.setState(StateEnum.ENABLED);
        body.setId(processorGroupId);
        body.setDisconnectedNodeAcknowledged(false);

        niFiClient.getFlow().activateControllerServices(processorGroupId, body);
        log.info(
                "Request Enable Controllers In Process Group : Process Group ID = [{}]",
                processorGroupId
            );
        try {
            Integer retryCnt = 0;
            while (retryCnt < 3) {
                Integer enableControllerCnt = 0;
                ControllerServicesEntity controllers = searchControllersInProcessorGroup(processorGroupId);
                for (ControllerServiceEntity controller : controllers.getControllerServices()) {
                    if (controller.getStatus().getRunStatus().equals(RunStatusEnum.ENABLED)){
                        enableControllerCnt++;
                    }
                }
                if (enableControllerCnt == controllers.getControllerServices().size()) {
                    log.info("Success Enable Controllers In Process Group : Process Group ID = [{}]", processorGroupId);
                    return true;
                }
                /* Sleep 1.0s */
                Thread.sleep(500);
                retryCnt++;
            } 
            return false;
        } catch (InterruptedException e) {
            log.error("Interrupt Exception", e);
            return false;
        }
    }
}
