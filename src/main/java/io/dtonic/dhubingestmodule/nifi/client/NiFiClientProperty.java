package io.dtonic.dhubingestmodule.nifi.client;

import lombok.Data;
import org.springframework.stereotype.Component;

/**
 * 
 * @FileName NiFiClientProperty.java
 * @Project D.hub Ingest Manager
 * @Brief
 * @Version 1.2
 * @Date 2023. 08. 08.
 * @Author Justin
 */
@Data
@Component
public class NiFiClientProperty {
    /* ingest Manager Process Group Id */
    private String ingestProcessGroupId;
    /* Transmitter Processor Id */
    private String transmitterId;
}
