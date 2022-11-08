package io.dtonic.dhubingestmodule.pipeline.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class PipelineDraftSVCTest {

    @Autowired
    private PipelineDraftSVC pipelineDraftSVC;

    @Test
    void testGetDataCollector() {
        List<String> result = pipelineDraftSVC.getDataCollector();
        assertNotNull(result);
    }
}
