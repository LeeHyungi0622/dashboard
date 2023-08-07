package io.dtonic.dhubingestmodule.nifi.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.dtonic.dhubingestmodule.nifi.vo.NiFiComponentVO;
import io.dtonic.dhubingestmodule.nifi.vo.PropertyVO;
import io.swagger.client.model.ActivateControllerServicesEntity;
import io.swagger.client.model.ControllerServiceEntity;
import io.swagger.client.model.ControllerServicesEntity;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@Service
public class NiFiControllerSVC {
    
    public void updateControllersInAdaptor(String processorGroupId, NiFiComponentVO properies)
        throws JsonProcessingException {
        ControllerServicesEntity controllers = searchControllersInProcessorGroup(processorGroupId);
        if (controllers.getControllerServices().size() == 0) {
            log.error(
                "Empty Controller in Processor Group : Processor Group ID = {}",
                processorGroupId
            );
        } else {
            for (ControllerServiceEntity controller : controllers.getControllerServices()) {
                if (controller.getComponent().getName().equals(properies.getName())) {
                    log.info("Controller name : {}", controller.getComponent().getName());
                    if (properies.getRequiredProps().size() != 0) {
                        controller =
                            updateControllerProperties(controller, properies.getRequiredProps());
                    } else {
                        log.error("Empty Required Props Elements In {}", properies.getName());
                    }
                    if (properies.getOptionalProps().size() != 0) {
                        controller =
                            updateControllerProperties(controller, properies.getOptionalProps());
                    }
                    try {
                        List<String> paths = new ArrayList<String>();
                        paths.add("controller-services");
                        paths.add(controller.getId());

                        Map<String, String> headers = new HashMap<String, String>();
                        headers.put("Content-Type", "application/json");

                        ResponseEntity<String> result = dataCoreRestSVC.put(
                            niFiClientEntity.getProperties().getNifiUrl() +
                            niFiClientEntity.getBASE_URL(),
                            paths,
                            headers,
                            controller,
                            null,
                            niFiClientEntity.getAccessToken(),
                            String.class
                        );

                        ControllerServiceEntity resultEntity = nifiObjectMapper.readValue(
                            result.getBody(),
                            ControllerServiceEntity.class
                        );
                        log.info("Update Controllers In Adaptor : Controller = {}", resultEntity);
                    } catch (Exception e) {
                        log.error("Fail to Update Controllers in Adaptor.", e);
                    }
                }
            }
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
            List<String> paths = new ArrayList<String>();
            paths.add("flow");
            paths.add("process-groups");
            paths.add(processorGroupId);
            paths.add("controller-services");

            Map<String, String> headers = new HashMap<String, String>();
            headers.put("Content-Type", "application/json");

            Map<String, Object> params = new HashMap<>();
            params.put("includeAncestorGroups", false);
            params.put("includeDescendantGroups", true);

            ResponseEntity<String> result = dataCoreRestSVC.get(
                niFiClientEntity.getProperties().getNifiUrl() + niFiClientEntity.getBASE_URL(),
                paths,
                headers,
                null,
                params,
                niFiClientEntity.getAccessToken(),
                String.class
            );

            ControllerServicesEntity resultEntity = nifiObjectMapper.readValue(
                result.getBody(),
                ControllerServicesEntity.class
            );
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
    public boolean disableControllers(String processorGroupId) {
        try {
            ActivateControllerServicesEntity body = new ActivateControllerServicesEntity();
            body.setState("DISABLED");
            body.setId(processorGroupId);
            body.setDisconnectedNodeAcknowledged(false);

            List<String> paths = new ArrayList<String>();
            paths.add("flow");
            paths.add("process-groups");
            paths.add(processorGroupId);
            paths.add("controller-services");

            Map<String, String> headers = new HashMap<String, String>();
            headers.put("Content-Type", "application/json");

            dataCoreRestSVC.put(
                niFiClientEntity.getProperties().getNifiUrl() + niFiClientEntity.getBASE_URL(),
                paths,
                headers,
                body,
                null,
                niFiClientEntity.getAccessToken(),
                String.class
            );
            log.info(
                "Success Disable Controllers In Process Group : Process Group ID = [{}]",
                processorGroupId
            );
            return true;
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
    public Boolean enableControllers(String processorGroupId) throws Exception{
        ActivateControllerServicesEntity body = new ActivateControllerServicesEntity();
        body.setState("ENABLED");
        body.setId(processorGroupId);
        body.setDisconnectedNodeAcknowledged(false);

        List<String> paths = new ArrayList<String>();
        paths.add("flow");
        paths.add("process-groups");
        paths.add(processorGroupId);
        paths.add("controller-services");

        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/json");

        dataCoreRestSVC.put(
            niFiClientEntity.getProperties().getNifiUrl() + niFiClientEntity.getBASE_URL(),
            paths,
            headers,
            body,
            null,
            niFiClientEntity.getAccessToken(),
            String.class
        );
        log.info("Success Enable Controllers in {}", processorGroupId);
        return true;
    }
}
