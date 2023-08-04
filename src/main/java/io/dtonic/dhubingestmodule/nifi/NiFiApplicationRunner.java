package io.dtonic.dhubingestmodule.nifi;

import io.dtonic.dhubingestmodule.common.component.Properties;
import io.dtonic.dhubingestmodule.nifi.service.NiFiProcessGroupSVC;
import io.dtonic.dhubingestmodule.nifi.service.NiFiProcessorSVC;
import io.dtonic.dhubingestmodule.nifi.service.NiFiTemplateSVC;
import io.swagger.client.model.ProcessorRunStatusEntity.StateEnum;
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
    private Properties properties;

    private static String ingestProcessGroupId;

    public String getIngestProcessGroupId() {
        return ingestProcessGroupId;
    }

    @Override
    public void run(ApplicationArguments args) {
        /* Optional Already Exist Adaptor Template Delete */
        if (properties.getIsNifiTemplateInit()) {
            niFiTemplateSVC.deleteTemplate();
        }
        /* Upload Templates */
        niFiTemplateSVC.uploadTemplate();
        /* Check Ingest Manager Process Group */
        ingestProcessGroupId = niFiProcessGroupSVC.searchProcessGroupInProcessGroup("root", "Ingest Manager");
        if (ingestProcessGroupId == null) {
            try {
                /* Create Ingest Manager Process Group */
                ingestProcessGroupId = niFiProcessGroupSVC.createProcessGroup("Ingest Manager", "root").getId();
            }catch (Exception e){
                log.error("Fail to Create Process Group : [Ingest Manager] error", e);
            }
        }

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

        String funnelId = "";
        /* Check Funnel */
        if (niFiSwaggerSVC.searchFunnelInProcessGroup(ingestProcessGroupId) == null) {
            /* Create Funnel */
            funnelId = niFiRestSVC.createFunnel(ingestProcessGroupId);
        } else {
            funnelId = niFiSwaggerSVC.searchFunnelInProcessGroup(ingestProcessGroupId).getId();
        }

        /* Check connection */
        if (!niFiSwaggerSVC.isExistedConnectionToProcessor(ingestProcessGroupId, transmitterId)) {
            /* Create Connection From Funnel to Transmitter */
            niFiRestSVC.createConnectionFromFunnelToTransmitter(
                ingestProcessGroupId,
                funnelId,
                transmitterId
            );
        }
        /* Start Transmitter */
        niFiProcessorSVC.updateStatusProcessor(transmitterId, StateEnum.RUNNING);
    }
}
