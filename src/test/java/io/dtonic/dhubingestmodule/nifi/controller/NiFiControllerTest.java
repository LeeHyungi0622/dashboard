package io.dtonic.dhubingestmodule.nifi.controller;

import io.dtonic.dhubingestmodule.common.component.Properties;
import io.dtonic.dhubingestmodule.nifi.vo.AdaptorVO;
import io.dtonic.dhubingestmodule.nifi.vo.NiFiComponentVO;
import io.dtonic.dhubingestmodule.nifi.vo.PropertyVO;
import io.dtonic.dhubingestmodule.pipeline.vo.PipelineVO;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@Slf4j
@SpringBootTest
@ActiveProfiles("dtonic")
public class NiFiControllerTest {

    @Autowired
    private NiFiController niFiController;

    private PipelineVO dummyPipelineVO;

    private AdaptorVO dummyAdaptorVO;

    private NiFiComponentVO dummyNiFiComponentVO;

    private PropertyVO dummyPropertyVO;

    @Test
    public void createPipelineTest() {
        niFiController.createPipeline(dummyPipelineVO);
    }

    @Test
    public void createAdaptorTest() {
        // dummyPropertyVO.setName(name);
        // dummyPropertyVO.setInputValue(inputValue);
        // List<PropertyVO> properties = new ArrayList<>();
        // dummyAdaptorVO.setNiFiComponent();
    }

    @Test
    public void updateAdaptorTest() {}

    @Test
    public void deletePipelineTest() {}

    @Test
    public void updatePipelineTest() {}

    @Test
    public void stopPipelineTest() {}

    @Test
    public void runPipelineTest() {}
}
