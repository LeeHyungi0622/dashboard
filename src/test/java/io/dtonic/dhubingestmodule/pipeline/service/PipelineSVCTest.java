package io.dtonic.dhubingestmodule.pipeline.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.shaded.json.JSONObject;
import io.dtonic.dhubingestmodule.pipeline.vo.PipelineVO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@Slf4j
@SpringBootTest
@ActiveProfiles("dtonic")
public class PipelineSVCTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PipelineSVC pipelineSVC;

    @Test
    void testCreatePipeline() {
        String requestBody =
            "{\"id\":14,\"creator\":\"admin\",\"name\":\"DummyTestTemp14\",\"detail\":\"detail14\",\"status\":\"Starting\",\"dataSet\":\"abcd\",\"dataModel\":null,\"processorGroupId\":null,\"collector\":{\"name\":\"RESTAPI\",\"completed\":false,\"nifiComponents\":[{\"type\":\"processor\",\"name\":\"RESTAPI\",\"requiredProps\":[{\"adaptorId\":null,\"name\":\"HTTPMethod\",\"detail\":\"HTTPrequestmethod(GET,POST,PUT,PATCH,DELETE,HEAD,OPTIONS).Arbitrarymethodsarealsosupported.MethodsotherthanPOST,PUTandPATCHwillbesentwithoutamessagebody.\",\"isRequired\":null,\"defaultValue\":[\"POST\",\"GET\",\"PUT\",\"PATCH\",\"DELETE\"],\"inputValue\":\"GET\"},{\"adaptorId\":null,\"name\":\"RemoteURL\",\"detail\":\"RemoteURLwhichwillbeconnectedto,includingscheme,host,port,path.\",\"isRequired\":null,\"defaultValue\":null,\"inputValue\":\"http://110.45.181:8080/test\"},{\"adaptorId\":null,\"name\":\"ConnectionTimeout\",\"detail\":\"Maxwaittimeforresponsefromremoteservice.\",\"isRequired\":null,\"defaultValue\":[\"5sec\"],\"inputValue\":\"5sec\"},{\"adaptorId\":null,\"name\":\"ReadTimeout\",\"detail\":\"Maxidletimebeforeclosingconnectiontotheremoteservice.\",\"isRequired\":null,\"defaultValue\":[\"15sec\"],\"inputValue\":\"15sec\"},{\"adaptorId\":null,\"name\":\"IdleTimeout\",\"detail\":\"Maxwaittimeforresponsefromremoteservice.\",\"isRequired\":null,\"defaultValue\":[\"5mins\"],\"inputValue\":\"5mins\"},{\"adaptorId\":null,\"name\":\"Scheduling\",\"detail\":\"SchedulingAPIcall(unit=sec)\",\"isRequired\":null,\"defaultValue\":[\"30sec\"],\"inputValue\":\"30sec\"}],\"optionalProps\":[{\"adaptorId\":null,\"name\":\"SSLContextService\",\"detail\":\"TheSSLContextServiceusedtoprovideclientcertificateinformationforTLS/SSL(https)connections.ItisalsousedtoconnecttoHTTPSProxy.\",\"isRequired\":null,\"defaultValue\":null,\"inputValue\":null},{\"adaptorId\":null,\"name\":\"AttributestoSend\",\"detail\":\"RegularexpressionthatdefineswhichattributestosendasHTTPheadersintherequest.Ifnotdefined,noattributesaresentasheaders.Alsoanydynamicpropertiessetwillbesentasheaders.Thedynamicpropertykeywillbetheheaderkeyandthedynamicpropertyvaluewillbeinterpretedasexpressionlanguagewillbetheheadervalue.\",\"isRequired\":null,\"defaultValue\":null,\"inputValue\":null},{\"adaptorId\":null,\"name\":\"Useragent\",\"detail\":\"TheUseragentidentifiersentalongwitheachrequest\",\"isRequired\":null,\"defaultValue\":null,\"inputValue\":null},{\"adaptorId\":null,\"name\":\"BasicAuthenticationUsername\",\"detail\":\"TheusernametobeusedbytheclienttoauthenticateagainsttheRemoteURL.Cannotincludecontrolcharacters(0-31),':',orDEL(127).\",\"isRequired\":null,\"defaultValue\":null,\"inputValue\":null},{\"adaptorId\":null,\"name\":\"BasicAuthenticationPassword\",\"detail\":\"ThepasswordtobeusedbytheclienttoauthenticateagainsttheRemoteURL.\",\"isRequired\":null,\"defaultValue\":null,\"inputValue\":null},{\"adaptorId\":null,\"name\":\"oauth2-access-token-provider\",\"detail\":\"NULL\",\"isRequired\":null,\"defaultValue\":null,\"inputValue\":null}]}]},\"filter\":{\"name\":\"filter\",\"completed\":false,\"nifiComponents\":[{\"type\":\"processor\",\"name\":\"Base64Decoder\",\"requiredProps\":[{\"adaptorId\":null,\"name\":\"isBase64\",\"detail\":null,\"isRequired\":null,\"defaultValue\":null,\"inputValue\":\"false\"}],\"optionalProps\":[]},{\"type\":\"processor\",\"name\":\"RootKeyFinder\",\"requiredProps\":[{\"adaptorId\":null,\"name\":\"root_key\",\"detail\":null,\"isRequired\":null,\"defaultValue\":null,\"inputValue\":\"\\\"parkingLot\\\".\\\"sensorId\\\"\"}],\"optionalProps\":[]}]},\"converter\":{\"name\":\"converter\",\"completed\":false,\"nifiComponents\":[{\"type\":\"processor\",\"name\":\"DataSetMapper\",\"requiredProps\":[{\"adaptorId\":null,\"name\":\"dataSet\",\"detail\":null,\"isRequired\":null,\"defaultValue\":null,\"inputValue\":\"ParkingSensor\"},{\"adaptorId\":null,\"name\":\"dataModel\",\"detail\":null,\"isRequired\":null,\"defaultValue\":null,\"inputValue\":\"ParkingSensorModel\"}],\"optionalProps\":[]},{\"type\":\"processor\",\"name\":\"DataSetProperties\",\"requiredProps\":[{\"adaptorId\":null,\"name\":\"location\",\"detail\":\"GeoProperty\",\"isRequired\":null,\"defaultValue\":null,\"inputValue\":\"\\\"parking_sensor\\\".\\\"timestamp\\\"\"},{\"adaptorId\":null,\"name\":\"parkingAreaId\",\"detail\":\"Property\",\"isRequired\":null,\"defaultValue\":null,\"inputValue\":\"\\\"timestamp\\\"\"},{\"adaptorId\":null,\"name\":\"parkingLotId\",\"detail\":\"Property\",\"isRequired\":null,\"defaultValue\":null,\"inputValue\":\"\\\"protocolversion\\\"\"}],\"optionalProps\":[]},{\"type\":\"processor\",\"name\":\"IDGenerater\",\"requiredProps\":[{\"adaptorId\":null,\"name\":\"level1\",\"detail\":null,\"isRequired\":null,\"defaultValue\":null,\"inputValue\":\"\\\"uniqueRole\\\"\"},{\"adaptorId\":null,\"name\":\"level2\",\"detail\":null,\"isRequired\":null,\"defaultValue\":null,\"inputValue\":\"\\\"timestamp\\\"\"},{\"adaptorId\":null,\"name\":\"level3\",\"detail\":null,\"isRequired\":null,\"defaultValue\":null,\"inputValue\":\"\\\"protocolversion\\\"\"}],\"optionalProps\":[]}]},\"createdAt\":\"2022-09-2709:53:48.251Z\",\"modifiedAt\":\"2022-09-2904:14:28.498Z\"}";

        log.info(requestBody);
        // try {
        //     //PipelineVO pipelineVO = objectMapper.readValue(requestBody, PipelineVO.class);
        //     // log.info(pipelineVO.toString());
        // } catch (JsonProcessingException e) {
        //     // TODO Auto-generated catch block
        //     e.printStackTrace();
        // }
    }

    @Test
    void testgetPipelineVOById() {
        Integer id = 14;
        PipelineVO pipelineVO = pipelineSVC.getPipelineVOById(id);
        log.debug("{}", pipelineVO.toString());
        assertNotNull(pipelineVO);
    }

    @Test
    void testchangePipelineStatus() {
        Integer id = 14;
        String status = "Starting";
        pipelineSVC.changePipelineStatus(id, status);
    }

    @Test
    void testdeletePipeline() {
        Integer id = 15;
        pipelineSVC.deletePipeline(id);
    }

    @Test
    void testisExists() {
        Integer id = 14;
        Boolean result = pipelineSVC.isExists(id);
        assertTrue(result);
    }

    @Test
    void testupdatePipeline() {
        Integer id = 14;
        PipelineVO pipelineVO = pipelineSVC.getPipelineVOById(id);
        pipelineSVC.updatePipeline(id, pipelineVO);
    }
}
