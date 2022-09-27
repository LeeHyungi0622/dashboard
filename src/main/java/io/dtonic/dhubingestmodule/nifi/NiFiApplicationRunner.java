package io.dtonic.dhubingestmodule.nifi;

import io.dtonic.dhubingestmodule.nifi.service.NiFiRestSVC;
import io.dtonic.dhubingestmodule.nifi.service.NiFiSwaggerSVC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class NiFiApplicationRunner implements ApplicationRunner {

    @Autowired
    private NiFiRestSVC niFiRestSVC;

    @Autowired
    private NiFiSwaggerSVC niFiSwaggerSVC;

    @Override
    public void run(ApplicationArguments args) {
        /* Upload Templates */
        niFiRestSVC.uploadTemplate();

        String transmitterId = niFiSwaggerSVC.searchProcessorIdbyName("root", "Transmitter");
        /* Check Transmitter */
        if (transmitterId == null) {
            /* Create Transmitter */
            transmitterId = niFiSwaggerSVC.createTransmitter();
        }
        /* Update Transmitter */
        niFiRestSVC.stopTransmitter(transmitterId);
        niFiSwaggerSVC.updateTransmitter(transmitterId);
        niFiRestSVC.startTransmitter(transmitterId);

        String funnelId = niFiSwaggerSVC.searchFunnelInProcessGroup("root");
        /* Check Funnel */
        if (funnelId == null) {
            /* Create Funnel */
            funnelId = niFiRestSVC.createFunnelInRoot();
        }
        /* Create Connection From Funnel to Transmitter */
        niFiSwaggerSVC.createConnectionFromFunnelToTransmitter(funnelId, transmitterId);
    }
}
