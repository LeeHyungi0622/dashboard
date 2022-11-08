package io.dtonic.dhubingestmodule.nifi.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dtonic.dhubingestmodule.common.component.Properties;
import io.dtonic.dhubingestmodule.nifi.vo.AdaptorVO;
import io.dtonic.dhubingestmodule.nifi.vo.NiFiComponentVO;
import io.dtonic.dhubingestmodule.nifi.vo.PropertyVO;
import io.dtonic.dhubingestmodule.pipeline.vo.PipelineVO;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
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

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void createPostgresPipelineTest() throws JsonMappingException, JsonProcessingException {
        String dbPostgres =
            "{\"id\":4,\"creator\":\"admin\",\"name\":\"DB-Postgres achakey\",\"detail\":\"DB-Postgres pipeline\",\"dataSet\":\"achakeyDataSet\",\"dataModel\":\"achakeyData\",\"collector\":{\"name\":\"Database-Postgres\",\"isCompleted\":true,\"nifiComponents\":[{\"type\":\"processor\",\"name\":\"Database-Postgres\",\"requiredProps\":[{\"name\":\"Scheduling\",\"detail\":null,\"defaultValue\":null,\"inputValue\":\"30 sec\"},{\"name\":\"SQL select query\",\"detail\":null,\"defaultValue\":null,\"inputValue\":\"SELECT * FROM tuneit.ingest_test\"}],\"optionalProps\":[]},{\"type\":\"Controller\",\"name\":\"DBCPConnectionPool\",\"requiredProps\":[{\"name\":\"Database Connection URL\",\"detail\":null,\"defaultValue\":null,\"inputValue\":\"110.45.181.89:54321/tuneitDB\"},{\"name\":\"Database User\",\"detail\":null,\"defaultValue\":null,\"inputValue\":\"datalab\"},{\"name\":\"Password\",\"detail\":null,\"defaultValue\":null,\"inputValue\":\"datalab_1!\"}],\"optionalProps\":[]}]},\"filter\":{\"name\":\"filter\",\"isCompleted\":true,\"nifiComponents\":[{\"type\":\"processor\",\"name\":\"Base64Decoder\",\"requiredProps\":[{\"name\":\"isBase64\",\"detail\":null,\"defaultValue\":null,\"inputValue\":\"false\"}],\"optionalProps\":[]},{\"type\":\"processor\",\"name\":\"RootKeyFinder\",\"requiredProps\":[{\"name\":\"root_key\",\"detail\":null,\"defaultValue\":null,\"inputValue\":null}],\"optionalProps\":[]}]},\"converter\":{\"name\":\"converter\",\"isCompleted\":true,\"nifiComponents\":[{\"type\":\"processor\",\"name\":\"DataSetProps\",\"requiredProps\":[{\"name\":\"device\",\"detail\":\"Relationship\",\"defaultValue\":null,\"inputValue\":\"\\\"CAR_NUM\\\"\"},{\"name\":\"timestamp\",\"detail\":\"Property\",\"defaultValue\":null,\"inputValue\":\"\\\"SYS_UPDATE_DT\\\"\"},{\"name\":\"value\",\"detail\":\"Property\",\"defaultValue\":null,\"inputValue\":\"\\\"FLAG\\\"\"}],\"optionalProps\":[]},{\"type\":\"processor\",\"name\":\"IDGenerater\",\"requiredProps\":[{\"name\":\"level1\",\"detail\":null,\"defaultValue\":null,\"inputValue\":\"\\\"device\\\"\"},{\"name\":\"level2\",\"detail\":null,\"defaultValue\":null,\"inputValue\":\"\\\"value\\\"\"},{\"name\":\"level3\",\"detail\":null,\"defaultValue\":null,\"inputValue\":null}],\"optionalProps\":[]}]}}";
        PipelineVO dbPostVO = objectMapper.readValue(dbPostgres, PipelineVO.class);
        niFiController.createPipeline(dbPostVO);
    }

    @Test
    public void createRESTAPIPipelineTest() throws JsonMappingException, JsonProcessingException {
        // String pipelineStr =
        //     "{\"id\":2,\"creator\":\"admin\",\"name\":\"RESTAPIweather\",\"detail\":\"RESTAPITESTpipeline\",\"dataSet\":\"weatherDataSet\",\"dataModel\":\"weatherData\",\"collector\":{\"name\":\"REST API\",\"isCompleted\":true,\"nifiComponents\":[{\"type\":\"processor\",\"name\":\"REST API\",\"requiredProps\":[{\"name\":\"HTTP Method\",\"detail\":\"HTTPrequestmethod(GET,POST,PUT,PATCH,DELETE,HEAD,OPTIONS).Arbitrarymethodsarealsosupported.MethodsotherthanPOST,PUTandPATCHwillbesentwithoutamessagebody.\",\"defaultValue\":[\"POST\",\"GET\",\"PUT\",\"PATCH\",\"DELETE\"],\"inputValue\":\"GET\"},{\"name\":\"Remote URL\",\"detail\":\"RemoteURLwhichwillbeconnectedto,includingscheme,host,port,path.\",\"defaultValue\":null,\"inputValue\":\"http://apis.data.go.kr/1360000/WthrWrnInfoService/getWthrWrnList?ServiceKey=mYjXWWSNiY3YHINNdmXnkawILfGwP8sG1d609sVKP6b4t2FnR21yPwVPcRwk1o7BRVHjifwcgq%2Fcq9rcaqabUw%3D%3D&pageNo=1&numOfRows=5&dataType=JSON&fromTmFc=20220924&toTmFc=20220930\"}],\"optionalProps\":[{\"name\":\"SSLContextService\",\"detail\":\"TheSSLContextServiceusedtoprovideclientcertificateinformationforTLS/SSL(https)connections.ItisalsousedtoconnecttoHTTPSProxy.\",\"defaultValue\":null,\"inputValue\":null}]}]},\"filter\":{\"name\":\"filter\",\"isCompleted\":true,\"nifiComponents\":[{\"type\":\"processor\",\"name\":\"Base64Decoder\",\"requiredProps\":[{\"name\":\"isBase64\",\"detail\":null,\"defaultValue\":null,\"inputValue\":\"false\"}],\"optionalProps\":[]},{\"type\":\"processor\",\"name\":\"RootKeyFinder\",\"requiredProps\":[{\"name\":\"root_key\",\"detail\":null,\"defaultValue\":null,\"inputValue\":\"\\\"response\\\".\\\"body\\\".\\\"items\\\".\\\"item\\\"\"}],\"optionalProps\":[]}]},\"converter\":{\"name\":\"converter\",\"isCompleted\":true,\"nifiComponents\":[{\"type\":\"processor\",\"name\":\"DataSetProps\",\"requiredProps\":[{\"name\":\"locationId\",\"detail\":\"Relationship\",\"defaultValue\":null,\"inputValue\":\"\\\"stnId\\\"\"},{\"name\":\"title\",\"detail\":\"Property\",\"defaultValue\":null,\"inputValue\":\"\\\"title\\\"\"},{\"name\":\"timestamp\",\"detail\":\"Property\",\"defaultValue\":null,\"inputValue\":\"\\\"tmFc\\\"\"},{\"name\":\"value\",\"detail\":\"Property\",\"defaultValue\":null,\"inputValue\":\"\\\"tmSeq\\\"\"}],\"optionalProps\":[]},{\"type\":\"processor\",\"name\":\"IDGenerater\",\"requiredProps\":[{\"name\":\"level1\",\"detail\":null,\"defaultValue\":null,\"inputValue\":\"\\\"locationId\\\"\"},{\"name\":\"level2\",\"detail\":null,\"defaultValue\":null,\"inputValue\":null},{\"name\":\"level3\",\"detail\":null,\"defaultValue\":null,\"inputValue\":null}],\"optionalProps\":[]}]}}";
        String pipelineStr =
            "{\"id\":2,\"creator\":\"admin\",\"name\":\"REST API weather\",\"detail\":\"REST API TEST pipeline\",\"dataSet\":\"weatherDataSet\",\"dataModel\":\"weatherData\",\"collector\":{\"name\":\"REST API\",\"isCompleted\":true,\"nifiComponents\":[{\"type\":\"processor\",\"name\":\"REST API\",\"requiredProps\":[{\"name\":\"HTTP Method\",\"detail\":\"HTTP request method (GET, POST, PUT, PATCH, DELETE, HEAD, OPTIONS). Arbitrary methods are also supported. Methods other than POST, PUT and PATCH will be sent without a message body.\",\"defaultValue\":[\"POST\",\"GET\",\"PUT\",\"PATCH\",\"DELETE\"],\"inputValue\":\"GET\"},{\"name\":\"Remote URL\",\"detail\":\"Remote URL which will be connected to, including scheme, host, port, path.\",\"defaultValue\":null,\"inputValue\":\"http://apis.data.go.kr/1360000/WthrWrnInfoService/getWthrWrnList?ServiceKey=mYjXWWSNiY3YHINNdmXnkawILfGwP8sG1d609sVKP6b4t2FnR21yPwVPcRwk1o7BRVHjifwcgq%2Fcq9rcaqabUw%3D%3D&pageNo=1&numOfRows=5&dataType=JSON&fromTmFc=20220924&toTmFc=20220930\"}],\"optionalProps\":[{\"name\":\"SSL Context Service\",\"detail\":\"The SSL Context Service used to provide client certificate information for TLS/SSL (https) connections. It is also used to connect to HTTPS Proxy.\",\"defaultValue\":null,\"inputValue\":null}]}]},\"filter\":{\"name\":\"filter\",\"isCompleted\":true,\"nifiComponents\":[{\"type\":\"processor\",\"name\":\"Base64Decoder\",\"requiredProps\":[{\"name\":\"isBase64\",\"detail\":null,\"defaultValue\":null,\"inputValue\":\"false\"}],\"optionalProps\":[]},{\"type\":\"processor\",\"name\":\"RootKeyFinder\",\"requiredProps\":[{\"name\":\"root_key\",\"detail\":null,\"defaultValue\":null,\"inputValue\":\"\\\"response\\\".\\\"body\\\".\\\"items\\\".\\\"item\\\"\"}],\"optionalProps\":[]}]},\"converter\":{\"name\":\"converter\",\"isCompleted\":true,\"nifiComponents\":[{\"type\":\"processor\",\"name\":\"DataSetProps\",\"requiredProps\":[{\"name\":\"locationId\",\"detail\":\"Relationship\",\"defaultValue\":null,\"inputValue\":\"\\\"stnId\\\"\"},{\"name\":\"title\",\"detail\":\"Property\",\"defaultValue\":null,\"inputValue\":\"\\\"title\\\"\"},{\"name\":\"timestamp\",\"detail\":\"Property\",\"defaultValue\":null,\"inputValue\":\"\\\"tmFc\\\"\"},{\"name\":\"value\",\"detail\":\"Property\",\"defaultValue\":null,\"inputValue\":\"\\\"tmSeq\\\"\"}],\"optionalProps\":[]},{\"type\":\"processor\",\"name\":\"IDGenerater\",\"requiredProps\":[{\"name\":\"level1\",\"detail\":null,\"defaultValue\":null,\"inputValue\":\"\\\"locationId\\\"\"},{\"name\":\"level2\",\"detail\":null,\"defaultValue\":null,\"inputValue\":null},{\"name\":\"level3\",\"detail\":null,\"defaultValue\":null,\"inputValue\":null}],\"optionalProps\":[]},{\"type\":\"Processor\",\"name\":\"ConvertDateType\",\"requiredProps\":[{\"name\":\"DateFormat\",\"detail\":\"Property\",\"isRequired\":null,\"defaultValue\":null,\"inputValue\":\"yyyyMMddHHmm\"}],\"optionalProps\":[]}]}}";

        PipelineVO dbPostVO = objectMapper.readValue(pipelineStr, PipelineVO.class);
        niFiController.createPipeline(dbPostVO);
    }

    @Test
    public void createRESTServerPipelineTest()
        throws JsonMappingException, JsonProcessingException {
        String pipelineStr =
            "{\"id\":2,\"creator\":\"admin\",\"name\":\"RESTAPIweather\",\"detail\":\"RESTAPITESTpipeline\",\"dataSet\":\"weatherDataSet\",\"dataModel\":\"weatherData\",\"collector\":{\"name\":\"REST API\",\"isCompleted\":true,\"nifiComponents\":[{\"type\":\"processor\",\"name\":\"REST API\",\"requiredProps\":[{\"name\":\"HTTP Method\",\"detail\":\"HTTPrequestmethod(GET,POST,PUT,PATCH,DELETE,HEAD,OPTIONS).Arbitrarymethodsarealsosupported.MethodsotherthanPOST,PUTandPATCHwillbesentwithoutamessagebody.\",\"defaultValue\":[\"POST\",\"GET\",\"PUT\",\"PATCH\",\"DELETE\"],\"inputValue\":\"GET\"},{\"name\":\"Remote URL\",\"detail\":\"RemoteURLwhichwillbeconnectedto,includingscheme,host,port,path.\",\"defaultValue\":null,\"inputValue\":\"http://apis.data.go.kr/1360000/WthrWrnInfoService/getWthrWrnList?ServiceKey=mYjXWWSNiY3YHINNdmXnkawILfGwP8sG1d609sVKP6b4t2FnR21yPwVPcRwk1o7BRVHjifwcgq%2Fcq9rcaqabUw%3D%3D&pageNo=1&numOfRows=5&dataType=JSON&fromTmFc=20220924&toTmFc=20220930\"}],\"optionalProps\":[{\"name\":\"SSLContextService\",\"detail\":\"TheSSLContextServiceusedtoprovideclientcertificateinformationforTLS/SSL(https)connections.ItisalsousedtoconnecttoHTTPSProxy.\",\"defaultValue\":null,\"inputValue\":null}]}]},\"filter\":{\"name\":\"filter\",\"isCompleted\":true,\"nifiComponents\":[{\"type\":\"processor\",\"name\":\"Base64Decoder\",\"requiredProps\":[{\"name\":\"isBase64\",\"detail\":null,\"defaultValue\":null,\"inputValue\":\"false\"}],\"optionalProps\":[]},{\"type\":\"processor\",\"name\":\"RootKeyFinder\",\"requiredProps\":[{\"name\":\"root_key\",\"detail\":null,\"defaultValue\":null,\"inputValue\":\"\\\"response\\\".\\\"body\\\".\\\"items\\\".\\\"item\\\"\"}],\"optionalProps\":[]}]},\"converter\":{\"name\":\"converter\",\"isCompleted\":true,\"nifiComponents\":[{\"type\":\"processor\",\"name\":\"DataSetProps\",\"requiredProps\":[{\"name\":\"locationId\",\"detail\":\"Relationship\",\"defaultValue\":null,\"inputValue\":\"\\\"stnId\\\"\"},{\"name\":\"title\",\"detail\":\"Property\",\"defaultValue\":null,\"inputValue\":\"\\\"title\\\"\"},{\"name\":\"timestamp\",\"detail\":\"Property\",\"defaultValue\":null,\"inputValue\":\"\\\"tmFc\\\"\"},{\"name\":\"value\",\"detail\":\"Property\",\"defaultValue\":null,\"inputValue\":\"\\\"tmSeq\\\"\"}],\"optionalProps\":[]},{\"type\":\"processor\",\"name\":\"IDGenerater\",\"requiredProps\":[{\"name\":\"level1\",\"detail\":null,\"defaultValue\":null,\"inputValue\":\"\\\"stnId\\\"\"},{\"name\":\"level2\",\"detail\":null,\"defaultValue\":null,\"inputValue\":null},{\"name\":\"level3\",\"detail\":null,\"defaultValue\":null,\"inputValue\":null}],\"optionalProps\":[]}]}}";
        PipelineVO dbPostVO = objectMapper.readValue(pipelineStr, PipelineVO.class);
        niFiController.createPipeline(dbPostVO);
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
    public void deletePipelineTest() {
        niFiController.deletePipeline("8925cdfc-0183-1000-e030-02d1c22bc93b");
    }

    @Test
    public void updatePipelineTest() {}

    @Test
    public void stopPipelineTest() {
        niFiController.stopPipeline("8905ea68-0183-1000-3cdf-24a2f1708575");
    }

    @Test
    public void runPipelineTest() {
        niFiController.runPipeline("8c264f86-0183-1000-a82f-c30fe98ebf25");
    }
}
