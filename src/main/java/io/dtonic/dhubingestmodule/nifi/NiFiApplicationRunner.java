package io.dtonic.dhubingestmodule.nifi;

import io.dtonic.dhubingestmodule.nifi.service.NiFiRestSVC;
import io.dtonic.dhubingestmodule.nifi.service.NiFiSwaggerSVC;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Data
@Component
public class NiFiApplicationRunner implements ApplicationRunner {

    @Autowired
    private NiFiRestSVC niFiRestSVC;

    @Autowired
    private NiFiSwaggerSVC niFiSwaggerSVC;

    private String ingestProcessGroupId;

    @Override
    public void run(ApplicationArguments args) {
        /* Upload Templates */
        niFiRestSVC.uploadTemplate();
        /* Check Ingest Manager Process Group */
        ingestProcessGroupId = niFiSwaggerSVC.searchProcessGroupInProcessGroup("root");
        if (ingestProcessGroupId == null) {
            /* Create Ingest Manager Process Group */
            ingestProcessGroupId = niFiSwaggerSVC.createProcessGroup("Ingest Manager", "root");
        }

        String transmitterId = niFiSwaggerSVC.searchProcessorIdbyName(
            ingestProcessGroupId,
            "Transmitter"
        );
        /* Check Transmitter */
        if (transmitterId == null) {
            /* Create Transmitter */
            transmitterId = niFiSwaggerSVC.createTransmitter(ingestProcessGroupId);
        }
        /* Update Transmitter */
        niFiRestSVC.stopTransmitter(transmitterId);
        niFiSwaggerSVC.updateTransmitter(transmitterId);

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
        niFiRestSVC.startTransmitter(transmitterId);
    }
}
