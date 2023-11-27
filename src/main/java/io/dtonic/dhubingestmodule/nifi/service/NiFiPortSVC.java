package io.dtonic.dhubingestmodule.nifi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.dtonic.dhubingestmodule.nifi.client.NiFiApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.model.FunnelEntity;
import io.swagger.client.model.InputPortsEntity;
import io.swagger.client.model.OutputPortsEntity;
import io.swagger.client.model.PortDTO;
import io.swagger.client.model.PortEntity;
import lombok.extern.slf4j.Slf4j;
/**
 * Served Pipeline Service
 * @FileName NiFiPortSVC.java
 * @Project D.hub Ingest Manager
 * @Brief
 * @Version 1.2
 * @Date 2023. 08. 01.
 * @Author Justin
 */

@Slf4j
@Service
public class NiFiPortSVC {
    @Autowired
    NiFiApiClient niFiClient;

    /**
     * Search Output Port In Process Group.
     *
     * @param processGroupId pipeline processor group id
     * @return Output Port Entity
     */
    public PortDTO searchOutputInProcessorGroup(String processGroupId) {
        try {
            OutputPortsEntity outputPorts = niFiClient
                .getProcessGroups()
                .getOutputPorts(processGroupId);
            if (outputPorts.getOutputPorts().size() == 0) {
                log.error(
                    "Empty Output Port in Processor Group : Processor Group ID = {}",
                    processGroupId
                );
                return null;
            } else if (outputPorts.getOutputPorts().size() > 1) {
                log.error(
                    "Too Many (2 or more) Output Port in Processor Group : Processor Group ID = {}",
                    processGroupId
                );
                return null;
            } else {
                for (PortEntity port : outputPorts.getOutputPorts()) {
                    PortDTO portComponent = port.getComponent();
                    log.info(
                        "Success Search Output Port in {} : Output Port ID = [{}]",
                        processGroupId,
                        portComponent.getId()
                    );
                    return portComponent;
                }
            }
            return null;
        } catch (ApiException e) {
            log.error("Fail to Search output port in {}.", processGroupId, e);
            return null;
        }
    }

    /**
     * Search Input Port In Process Group.
     *
     * @param processGroupId pipeline processor group id
     * @return Input Port Entity
     */
    public PortDTO searchInputInProcessorGroup(String processGroupId) {
        try {
            InputPortsEntity inputPorts = niFiClient
                .getProcessGroups()
                .getInputPorts(processGroupId);
            if (inputPorts.getInputPorts().size() == 0) {
                log.error(
                    "Empty Input Port in Processor Group : Processor Group ID = {}",
                    processGroupId
                );
                return null;
            } else if (inputPorts.getInputPorts().size() > 1) {
                log.error(
                    "Too Many (2 or more) Input Port in Processor Group : Processor Group ID = {}",
                    processGroupId
                );
                return null;
            } else {
                for (PortEntity port : inputPorts.getInputPorts()) {
                    PortDTO portComponent = port.getComponent();
                    log.info(
                        "Success Search Input Port in {} : Input Port ID = [{}]",
                        processGroupId,
                        portComponent.getId()
                    );
                    return portComponent;
                }
            }
            return null;
        } catch (ApiException e) {
            log.error("Fail to Search input port in {}.", processGroupId, e);
            return null;
        }
    }

    /**
     * Search Funnel In Ingest Manager
     * When search funnel, get first element of funnels array because one funnel exist in Ingest Manager
     *
     * @param sourceProcessorGroupID pipeline processor group id
     * @return FunnelID
     */
    public FunnelEntity searchFunnelInProcessGroup(String processGroupId) {
        try {
            
            for (FunnelEntity funnel : niFiClient
                .getProcessGroups()
                .getFunnels(processGroupId)
                .getFunnels()) {
                log.info("Search Funnel In Ingest Manager : Funnel ID = [{}]", funnel.getId());
                return funnel;
            }
            log.info("Empty Funnel In Ingest Manager");
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}
