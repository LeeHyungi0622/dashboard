package io.dtonic.dhubingestmodule.nifi;

import io.dtonic.dhubingestmodule.common.component.Properties;
import io.dtonic.dhubingestmodule.nifi.client.NiFiClientProperty;
import io.dtonic.dhubingestmodule.nifi.service.NiFiConnectionSVC;
import io.dtonic.dhubingestmodule.nifi.service.NiFiPortSVC;
import io.dtonic.dhubingestmodule.nifi.service.NiFiProcessGroupSVC;
import io.dtonic.dhubingestmodule.nifi.service.NiFiProcessorSVC;
import io.dtonic.dhubingestmodule.nifi.service.NiFiTemplateSVC;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.nimbusds.oauth2.sdk.id.State;

@Slf4j
@Component
public class NiFiApplicationRunner implements ApplicationRunner {

    @Autowired
    private NiFiProcessorSVC niFiProcessorSVC;

    @Autowired
    private NiFiProcessGroupSVC niFiProcessGroupSVC;

    @Autowired
    private NiFiTemplateSVC niFiTemplateSVC;

    @Autowired
    private NiFiConnectionSVC niFiConnectionSVC;

    @Autowired
    private NiFiPortSVC niFiPortSVC;

    @Autowired
    private Properties properties;

    @Autowired
    private NiFiClientProperty niFiClientProperty;

    @Override
    public void run(ApplicationArguments args) {
        try {
            initTemplate();
            initProcessGroup();


        } catch (Exception e) {
            log.error("Fail to Connect NiFi : {}",properties.getNifiUrl(), e);
        }
        
    }

    private void initTemplate(){
        try {
            /* Optional Already Exist Adaptor Template Delete */
            if (properties.getIsNifiTemplateInit()) {
                niFiTemplateSVC.deleteTemplate();
            }
            /* Upload Templates */
            niFiTemplateSVC.uploadTemplate();
        } catch (Exception e) {
            log.error("Fail to Upload Template.", e);
        }
    }

    private void initProcessGroup(){
        try {
            /* Check Ingest Manager Process Group */
            String ingestProcessGroupId = niFiProcessGroupSVC.searchProcessGroupInProcessGroup("root", "Ingest Manager");
            if (ingestProcessGroupId == null) {
                try {
                    /* Create Ingest Manager Process Group */
                    ingestProcessGroupId = niFiProcessGroupSVC.createProcessGroup("Ingest Manager", "root").getId();
                } catch (Exception e){
                    log.error("Fail to Create Process Group : [Ingest Manager] error");
                }
            }
            /* Set NiFi ingest manager process group id */
            niFiClientProperty.setIngestProcessGroupId(ingestProcessGroupId);
        } catch (Exception e) {
            log.error("Fail to init Process Group", e);
        }
    }

    private void initTransmitter(){
        String ingestProcessGroupId = niFiClientProperty.getIngestProcessGroupId();
        try {
            String transmitterId = niFiProcessorSVC.searchProcessorIdbyName(
                ingestProcessGroupId,
                "Transmitter"
            );
            /* Check Transmitter */
            if (transmitterId == null) {
                /* Create Transmitter */
                transmitterId = niFiProcessorSVC.createTransmitter(ingestProcessGroupId);
            }
            /* Update Transmitter */
            niFiProcessorSVC.updateStatusProcessor(transmitterId, StateEnum.STOPPED);
            niFiProcessorSVC.updateTransmitter(transmitterId);

            /* Set NiFi Transmitter Processor Id */
            niFiClientProperty.setTransmitterId(transmitterId);

            String funnelId = "";
            /* Check Funnel */
            if (niFiPortSVC.searchFunnelInProcessGroup(ingestProcessGroupId) == null) {
                /* Create Funnel */
                funnelId = niFiConnectionSVC.createFunnel(ingestProcessGroupId);
            } else {
                funnelId = niFiPortSVC.searchFunnelInProcessGroup(ingestProcessGroupId).getId();
            }

            /* Check connection */
            if (!niFiConnectionSVC.isExistedConnectionToProcessor(ingestProcessGroupId, transmitterId)) {
                /* Create Connection From Funnel to Transmitter */
                niFiConnectionSVC.createConnectionFromFunnelToTransmitter(
                    ingestProcessGroupId,
                    funnelId,
                    transmitterId
                );
            }
            /* Start Transmitter */
            niFiProcessorSVC.updateStatusProcessor(transmitterId, StateEnum.RUNNING);
        } catch (Exception e) {
            // TODO: handle exception
        }
    }
}
