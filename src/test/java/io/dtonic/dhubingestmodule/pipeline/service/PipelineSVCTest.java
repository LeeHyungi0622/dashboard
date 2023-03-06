package io.dtonic.dhubingestmodule.pipeline.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.json.JSONObject;

import io.dtonic.dhubingestmodule.common.code.AdaptorName;
import io.dtonic.dhubingestmodule.common.code.CommandStatusCode;
import io.dtonic.dhubingestmodule.common.code.PipelineStatusCode;
import io.dtonic.dhubingestmodule.common.thread.MultiThread;
import io.dtonic.dhubingestmodule.common.thread.DeletethreadTest;
import io.dtonic.dhubingestmodule.nifi.controller.NiFiController;
import io.dtonic.dhubingestmodule.nifi.vo.AdaptorVO;
import io.dtonic.dhubingestmodule.pipeline.mapper.PipelineMapper;
import io.dtonic.dhubingestmodule.pipeline.mapper.PipelineMapperTest;
import io.dtonic.dhubingestmodule.pipeline.vo.CommandVO;
import io.dtonic.dhubingestmodule.pipeline.vo.PipelineVO;
import io.dtonic.dhubingestmodule.pipeline.vo.PipelineVOtoDB;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Slf4j
@SpringBootTest
@ActiveProfiles("dtonic")
public class PipelineSVCTest {

    @Autowired
    private PipelineSVC pipelineSVC;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PipelineMapperTest pipelineMapperTest;


    @Test
    void testCreatePipeline() throws JsonMappingException, JsonProcessingException {
        // String requestBody =
        //     "{\"id\":14,\"creator\":\"admin\",\"name\":\"DummyTestTemp14\",\"detail\":\"detail14\",\"status\":\"Starting\",\"dataSet\":\"abcd\",\"dataModel\":null,\"processorGroupId\":null,\"collector\":{\"name\":\"RESTAPI\",\"completed\":false,\"nifiComponents\":[{\"type\":\"processor\",\"name\":\"RESTAPI\",\"requiredProps\":[{\"adaptorId\":null,\"name\":\"HTTPMethod\",\"detail\":\"HTTPrequestmethod(GET,POST,PUT,PATCH,DELETE,HEAD,OPTIONS).Arbitrarymethodsarealsosupported.MethodsotherthanPOST,PUTandPATCHwillbesentwithoutamessagebody.\",\"isRequired\":null,\"defaultValue\":[\"POST\",\"GET\",\"PUT\",\"PATCH\",\"DELETE\"],\"inputValue\":\"GET\"},{\"adaptorId\":null,\"name\":\"RemoteURL\",\"detail\":\"RemoteURLwhichwillbeconnectedto,includingscheme,host,port,path.\",\"isRequired\":null,\"defaultValue\":null,\"inputValue\":\"http://110.45.181:8080/test\"},{\"adaptorId\":null,\"name\":\"ConnectionTimeout\",\"detail\":\"Maxwaittimeforresponsefromremoteservice.\",\"isRequired\":null,\"defaultValue\":[\"5sec\"],\"inputValue\":\"5sec\"},{\"adaptorId\":null,\"name\":\"ReadTimeout\",\"detail\":\"Maxidletimebeforeclosingconnectiontotheremoteservice.\",\"isRequired\":null,\"defaultValue\":[\"15sec\"],\"inputValue\":\"15sec\"},{\"adaptorId\":null,\"name\":\"IdleTimeout\",\"detail\":\"Maxwaittimeforresponsefromremoteservice.\",\"isRequired\":null,\"defaultValue\":[\"5mins\"],\"inputValue\":\"5mins\"},{\"adaptorId\":null,\"name\":\"Scheduling\",\"detail\":\"SchedulingAPIcall(unit=sec)\",\"isRequired\":null,\"defaultValue\":[\"30sec\"],\"inputValue\":\"30sec\"}],\"optionalProps\":[{\"adaptorId\":null,\"name\":\"SSLContextService\",\"detail\":\"TheSSLContextServiceusedtoprovideclientcertificateinformationforTLS/SSL(https)connections.ItisalsousedtoconnecttoHTTPSProxy.\",\"isRequired\":null,\"defaultValue\":null,\"inputValue\":null},{\"adaptorId\":null,\"name\":\"AttributestoSend\",\"detail\":\"RegularexpressionthatdefineswhichattributestosendasHTTPheadersintherequest.Ifnotdefined,noattributesaresentasheaders.Alsoanydynamicpropertiessetwillbesentasheaders.Thedynamicpropertykeywillbetheheaderkeyandthedynamicpropertyvaluewillbeinterpretedasexpressionlanguagewillbetheheadervalue.\",\"isRequired\":null,\"defaultValue\":null,\"inputValue\":null},{\"adaptorId\":null,\"name\":\"Useragent\",\"detail\":\"TheUseragentidentifiersentalongwitheachrequest\",\"isRequired\":null,\"defaultValue\":null,\"inputValue\":null},{\"adaptorId\":null,\"name\":\"BasicAuthenticationUsername\",\"detail\":\"TheusernametobeusedbytheclienttoauthenticateagainsttheRemoteURL.Cannotincludecontrolcharacters(0-31),':',orDEL(127).\",\"isRequired\":null,\"defaultValue\":null,\"inputValue\":null},{\"adaptorId\":null,\"name\":\"BasicAuthenticationPassword\",\"detail\":\"ThepasswordtobeusedbytheclienttoauthenticateagainsttheRemoteURL.\",\"isRequired\":null,\"defaultValue\":null,\"inputValue\":null},{\"adaptorId\":null,\"name\":\"oauth2-access-token-provider\",\"detail\":\"NULL\",\"isRequired\":null,\"defaultValue\":null,\"inputValue\":null}]}]},\"filter\":{\"name\":\"filter\",\"completed\":false,\"nifiComponents\":[{\"type\":\"processor\",\"name\":\"Base64Decoder\",\"requiredProps\":[{\"adaptorId\":null,\"name\":\"isBase64\",\"detail\":null,\"isRequired\":null,\"defaultValue\":null,\"inputValue\":\"false\"}],\"optionalProps\":[]},{\"type\":\"processor\",\"name\":\"RootKeyFinder\",\"requiredProps\":[{\"adaptorId\":null,\"name\":\"root_key\",\"detail\":null,\"isRequired\":null,\"defaultValue\":null,\"inputValue\":\"\\\"parkingLot\\\".\\\"sensorId\\\"\"}],\"optionalProps\":[]}]},\"converter\":{\"name\":\"converter\",\"completed\":false,\"nifiComponents\":[{\"type\":\"processor\",\"name\":\"DataSetMapper\",\"requiredProps\":[{\"adaptorId\":null,\"name\":\"dataSet\",\"detail\":null,\"isRequired\":null,\"defaultValue\":null,\"inputValue\":\"ParkingSensor\"},{\"adaptorId\":null,\"name\":\"dataModel\",\"detail\":null,\"isRequired\":null,\"defaultValue\":null,\"inputValue\":\"ParkingSensorModel\"}],\"optionalProps\":[]},{\"type\":\"processor\",\"name\":\"DataSetProperties\",\"requiredProps\":[{\"adaptorId\":null,\"name\":\"location\",\"detail\":\"GeoProperty\",\"isRequired\":null,\"defaultValue\":null,\"inputValue\":\"\\\"parking_sensor\\\".\\\"timestamp\\\"\"},{\"adaptorId\":null,\"name\":\"parkingAreaId\",\"detail\":\"Property\",\"isRequired\":null,\"defaultValue\":null,\"inputValue\":\"\\\"timestamp\\\"\"},{\"adaptorId\":null,\"name\":\"parkingLotId\",\"detail\":\"Property\",\"isRequired\":null,\"defaultValue\":null,\"inputValue\":\"\\\"protocolversion\\\"\"}],\"optionalProps\":[]},{\"type\":\"processor\",\"name\":\"IDGenerater\",\"requiredProps\":[{\"adaptorId\":null,\"name\":\"level1\",\"detail\":null,\"isRequired\":null,\"defaultValue\":null,\"inputValue\":\"\\\"uniqueRole\\\"\"},{\"adaptorId\":null,\"name\":\"level2\",\"detail\":null,\"isRequired\":null,\"defaultValue\":null,\"inputValue\":\"\\\"timestamp\\\"\"},{\"adaptorId\":null,\"name\":\"level3\",\"detail\":null,\"isRequired\":null,\"defaultValue\":null,\"inputValue\":\"\\\"protocolversion\\\"\"}],\"optionalProps\":[]}]},\"createdAt\":\"2022-09-2709:53:48.251Z\",\"modifiedAt\":\"2022-09-2904:14:28.498Z\"}";

        // log.debug(requestBody);

        // try {
        //     //PipelineVO pipelineVO = objectMapper.readValue(requestBody, PipelineVO.class);
        //     log.debug(pipelineVO.toString());
        // } catch (JsonProcessingException e) {
        //     // TODO Auto-generated catch block
        //     log.error("error : ", e);
        // }
        String data = "{\"collector\":{\"completed\":true,\"name\":\"RESTAPI\",\"nifiComponents\":[{\"name\":\"RESTAPI\",\"optionalProps\":[{\"adaptorId\":2,\"defaultValue\":[],\"detail\":\"요청에서HTTP헤더로보낼특성을정의하는정규식입니다.정의하지않으면특성이헤더로전송되지않습니다.또한동적속성집합은헤더로전송됩니다.동적속성키는헤더키가되고동적속성값은표현식언어가헤더값이되는것으로해석됩니다.\",\"isRequired\":false,\"name\":\"AttributestoSend\"},{\"adaptorId\":2,\"defaultValue\":[],\"detail\":\"각요청과함께전송된사용자에이전트식별자\",\"isRequired\":false,\"name\":\"Useragent\"},{\"adaptorId\":2,\"defaultValue\":[],\"detail\":\"클라이언트가원격URL에대해인증하는데사용할사용자이름입니다.제어문자(0-31),':'또는DEL(127)을포함할수없습니다.\",\"isRequired\":false,\"name\":\"BasicAuthenticationUsername\"},{\"adaptorId\":2,\"defaultValue\":[],\"detail\":\"클라이언트가원격URL에대해인증하는데사용할암호입니다.\",\"isRequired\":false,\"name\":\"BasicAuthenticationPassword\"},{\"adaptorId\":2,\"defaultValue\":[],\"detail\":\"설정된경우,수신된응답본문은별도의FlowFile대신원래FlowFile속성에저장됩니다.이속성의값을평가하여넣을속성키를결정합니다.\",\"isRequired\":false,\"name\":\"PutResponseBodyInAttribute\"},{\"adaptorId\":2,\"defaultValue\":[],\"detail\":\"응답본문을원본속성에라우팅하는경우(\\\\\\\"Putresponsebodyinattribute\\\\\\\"속성을설정하거나오류상태코드를수신하여),속성값에입력되는문자수는최대이양이될것이다.속성은메모리에보관되며큰속성은메모리부족문제를빠르게발생시키기때문에이는중요합니다.출력이이값보다길어지면크기에맞게잘립니다.가능하면이크기를작게만드는것을고려해보십시오.\",\"isRequired\":false,\"name\":\"MaxLengthToPutInAttribute\"},{\"adaptorId\":2,\"defaultValue\":[\"false\",\"true\"],\"detail\":\"요약인증을사용하여웹사이트와통신할지여부입니다.인증에는'기본인증사용자이름'과'기본인증암호'를사용합니다.\",\"inputValue\":\"false\",\"isRequired\":false,\"name\":\"DigestAuthentication\"},{\"adaptorId\":2,\"defaultValue\":[\"false\",\"true\"],\"detail\":\"수신된서버상태코드또는프로세서가요청속성에서버응답본문을넣도록구성되어있는지여부에관계없이응답흐름파일을생성하고'Response'관계로라우팅합니다.후자의구성에서는특성에응답본문과일반적인응답FlowFile이포함된요청FlowFile이각각의관계로전송됩니다.\",\"inputValue\":\"false\",\"isRequired\":false,\"name\":\"AlwaysOutputResponse\"},{\"adaptorId\":2,\"defaultValue\":[\"false\",\"true\"],\"detail\":\"이속성을사용하도록설정하면모든응답헤더가원래요청에저장됩니다.이는응답헤더가필요하지만수신된상태코드로인해응답이생성되지않는경우일수있습니다.\",\"inputValue\":\"false\",\"isRequired\":false,\"name\":\"AddResponseHeaderstoRequest\"},{\"adaptorId\":2,\"defaultValue\":[\"true\",\"false\"],\"detail\":\"참일경우,POST/PUT/PATCH요청시HTTP메시지본문을보냅니다(기본값).false인경우이요청에대한메시지본문및내용유형헤더를표시하지않습니다.\",\"inputValue\":\"true\",\"isRequired\":false,\"name\":\"send-message-body\"},{\"adaptorId\":2,\"defaultValue\":[\"false\"],\"detail\":\"참일경우프로세서는응답내용을흐름파일에쓰지않습니다.\",\"inputValue\":\"false\",\"isRequired\":false,\"name\":\"ignore-response-content\"},{\"adaptorId\":2,\"defaultValue\":[],\"detail\":\"SendMessageBody가true이고FlowFileFormDataName(흐름파일양식데이터이름)이설정된경우이값을양식데이터이름으로하는다중부분/양식형식으로FlowFile이메시지본문으로전송됩니다.\",\"isRequired\":false,\"name\":\"form-body-form-name\"},{\"adaptorId\":2,\"defaultValue\":[\"true\",\"false\"],\"detail\":\"SendMessageBody가true이고FlowFileFormDataName이설정되어있고SetFlowFileFormDataFileName이true인경우FlowFile의fileName값이폼데이터의파일이름속성으로설정됩니다.\",\"inputValue\":\"true\",\"isRequired\":false,\"name\":\"set-form-filename\"}],\"requiredProps\":[{\"adaptorId\":2,\"defaultValue\":[\"POST\",\"GET\",\"PUT\",\"DELETE\"],\"detail\":\"HTTP요청방법(GET,POST,PUT,PATCH,DELETE,HEAD,OPTIONS).임의메서드도지원됩니다.POST,PUT및PATCH이외의메서드는메시지본문없이전송됩니다.\",\"inputValue\":\"POST\",\"isRequired\":true,\"name\":\"HTTPMethod\"},{\"adaptorId\":2,\"defaultValue\":[],\"detail\":\"연결할원격URL(구성표,호스트,포트,경로포함)입니다.\",\"inputValue\":\"localhost:8083\",\"isRequired\":true,\"name\":\"RemoteURL\"},{\"adaptorId\":2,\"defaultValue\":[\"5sec\"],\"detail\":\"원격서비스에연결하기위한최대대기시간입니다.\",\"inputValue\":\"5sec\",\"isRequired\":true,\"name\":\"ConnectionTimeout\"},{\"adaptorId\":2,\"defaultValue\":[\"15sec\"],\"detail\":\"원격서비스의응답에대한최대대기시간입니다.\",\"inputValue\":\"15sec\",\"isRequired\":true,\"name\":\"ReadTimeout\"},{\"adaptorId\":2,\"defaultValue\":[\"5mins\"],\"detail\":\"원격서비스에대한연결을닫기전의최대유휴시간입니다.\",\"inputValue\":\"5mins\",\"isRequired\":true,\"name\":\"idle-timeout\"},{\"adaptorId\":2,\"defaultValue\":[\"5\"],\"detail\":\"열려있는상태로유지할최대유휴연결수입니다.\",\"inputValue\":\"5\",\"isRequired\":true,\"name\":\"max-idle-connections\"},{\"adaptorId\":2,\"defaultValue\":[\"True\",\"False\"],\"detail\":\"요청에RFC-2616Date헤더를포함합니다.\",\"inputValue\":\"True\",\"isRequired\":true,\"name\":\"IncludeDateHeader\"},{\"adaptorId\":2,\"defaultValue\":[\"True\",\"False\"],\"detail\":\"원격서버에서발급한HTTP리디렉션을따릅니다.\",\"inputValue\":\"True\",\"isRequired\":true,\"name\":\"FollowRedirects\"},{\"adaptorId\":2,\"defaultValue\":[\"DISABLED\",\"ACCEPT_ALL\"],\"detail\":\"HTTP쿠키를허용하고유지하기위한전략입니다.쿠키를허용하면여러요청에대해지속성을유지할수있습니다.\",\"inputValue\":\"DISABLED\",\"isRequired\":true,\"name\":\"cookie-strategy\"},{\"adaptorId\":2,\"defaultValue\":[\"False\",\"True\"],\"detail\":\"HTTP/2프로토콜버전의사용을금지할지여부를결정합니다.비활성화된경우HTTP/1.1만지원됩니다.\",\"inputValue\":\"False\",\"isRequired\":true,\"name\":\"disable-http2\"},{\"adaptorId\":2,\"defaultValue\":[\"RANDOM\",\"URL_PATH\"],\"detail\":\"FlowFile의파일이름특성을설정하는데사용되는전략을결정합니다.\",\"inputValue\":\"RANDOM\",\"isRequired\":true,\"name\":\"flow-file-naming-strategy\"},{\"adaptorId\":2,\"defaultValue\":[\"${mime.type}\"],\"detail\":\"콘텐츠가PUT,POST또는패치를통해전송되는시기를지정하는콘텐츠유형입니다.표현식언어표현식을평가한후값이비어있는경우내용유형은기본적으로응용프로그램/옥텟스트림으로설정됩니다.\",\"inputValue\":\"${mime.type}\",\"isRequired\":true,\"name\":\"Content-Type\"},{\"adaptorId\":2,\"defaultValue\":[\"false\",\"true\"],\"detail\":\"POST,PUTing또는PATCHing콘텐츠가이속성을true로설정하여'Content-length'헤더를통과하지않고'chunked'값으로'Transfer-Encoding'을전송합니다.이를통해HTTP1.1에도입된데이터전송메커니즘이길이를알수없는데이터를청크단위로전달할수있습니다.\",\"inputValue\":\"false\",\"isRequired\":true,\"name\":\"UseChunkedEncoding\"},{\"adaptorId\":2,\"defaultValue\":[\"false\",\"true\"],\"detail\":\"HTTP요청에대해ETAG(HTTP엔티티태그)지원을사용합니다.\",\"inputValue\":\"false\",\"isRequired\":true,\"name\":\"use-etag\"},{\"adaptorId\":2,\"defaultValue\":[\"10MB\"],\"detail\":\"ETAG캐시가증가할수있는최대크기입니다.기본크기는10MB입니다.\",\"inputValue\":\"10MB\",\"isRequired\":true,\"name\":\"etag-max-cache-size\"},{\"adaptorId\":2,\"defaultValue\":[\"120sec\"],\"detail\":\"TIMER_DRIVEN\",\"inputValue\":\"15sec\",\"isRequired\":true,\"name\":\"Scheduling\"}],\"type\":\"Processor\"}]},\"converter\":{\"completed\":false,\"name\":\"converter\",\"nifiComponents\":[{\"name\":\"DataSetProps\",\"optionalProps\":[],\"requiredProps\":[{\"defaultValue\":[],\"detail\":\"Property\",\"inputValue\":\"\\\"test\\\"\",\"name\":\"objects\"},{\"defaultValue\":[],\"detail\":\"GeoProperty\",\"inputValue\":\"\\\"test\\\"\",\"name\":\"observationSpace\"},{\"defaultValue\":[],\"detail\":\"Property\",\"inputValue\":\"\\\"test\\\"\",\"name\":\"testArrayBoolean\"},{\"defaultValue\":[],\"detail\":\"Property\",\"inputValue\":\"\\\"test\\\"\",\"name\":\"testArrayDouble\"},{\"defaultValue\":[],\"detail\":\"Property\",\"inputValue\":\"\\\"test\\\"\",\"name\":\"testArrayInteger\"},{\"defaultValue\":[],\"detail\":\"Property\",\"inputValue\":\"\\\"test\\\"\",\"name\":\"testArrayObject\"},{\"defaultValue\":[],\"detail\":\"Property\",\"inputValue\":\"\\\"test\\\"\",\"name\":\"testArrayString\"},{\"defaultValue\":[],\"detail\":\"Property\",\"inputValue\":\"\\\"test\\\"\",\"name\":\"testBoolean\"},{\"defaultValue\":[],\"detail\":\"DateFormat\",\"inputValue\":\"${testDate:toDate(\\\"test\\\",\\\"GMT\\\"):format(\\\"yyyy-MM-dd'T'HH:mm:ssXXX\\\",\\\"GMT\\\")}\",\"name\":\"testDate\"},{\"defaultValue\":[],\"detail\":\"Property\",\"inputValue\":\"\\\"test\\\"\",\"name\":\"testDate\"},{\"defaultValue\":[],\"detail\":\"Property\",\"inputValue\":\"\\\"test\\\"\",\"name\":\"testDouble\"},{\"defaultValue\":[],\"detail\":\"GeoProperty\",\"inputValue\":\"\\\"test\\\"\",\"name\":\"testGeoJson\"},{\"defaultValue\":[],\"detail\":\"Property\",\"inputValue\":\"\\\"test\\\"\",\"name\":\"testInteger\"},{\"defaultValue\":[],\"detail\":\"Relationship\",\"inputValue\":\"\\\"test\\\"\",\"name\":\"testRelationshipString\"},{\"defaultValue\":[],\"detail\":\"Property\",\"inputValue\":\"\\\"test\\\"\",\"name\":\"testString\"}],\"type\":\"Processor\"},{\"name\":\"IDGenerater\",\"optionalProps\":[],\"requiredProps\":[{\"adaptorId\":11,\"defaultValue\":[],\"inputValue\":\"\\\"test\\\"\",\"isRequired\":true,\"name\":\"level1\"},{\"adaptorId\":11,\"defaultValue\":[],\"isRequired\":true,\"name\":\"level2\"},{\"adaptorId\":11,\"defaultValue\":[],\"isRequired\":true,\"name\":\"level3\"}],\"type\":\"Processor\"}]},\"creator\":\"ingest\",\"dataModel\":\"zzTestModelDJ52\",\"dataSet\":\"TestModel2\",\"detail\":\"test\",\"filter\":{\"completed\":true,\"name\":\"filter\",\"nifiComponents\":[{\"name\":\"Base64Decoder\",\"optionalProps\":[],\"requiredProps\":[{\"adaptorId\":9,\"defaultValue\":[\"true\",\"false\"],\"inputValue\":\"false\",\"isRequired\":true,\"name\":\"isBase64\"}],\"type\":\"Processor\"},{\"name\":\"RootKeyFinder\",\"optionalProps\":[],\"requiredProps\":[{\"adaptorId\":10,\"defaultValue\":[],\"inputValue\":\"origin\",\"isRequired\":true,\"name\":\"root_key\"}],\"type\":\"Processor\"}]},\"name\":\"testdj3\"}";
        PipelineVO pipelineVO = new PipelineVO();
        pipelineVO = objectMapper.readValue(data, PipelineVO.class);

        PipelineVOtoDB pipelineDTO = new PipelineVOtoDB();
        pipelineDTO.setName(pipelineVO.getName());
        pipelineDTO.setDataModel(pipelineVO.getDataModel());
        pipelineDTO.setDataSet(pipelineVO.getDataSet());
        pipelineDTO.setDetail(pipelineVO.getDetail());
        pipelineDTO.setCreator(pipelineVO.getCreator());

        JSONObject jsonObject = new JSONObject(pipelineVO);
        pipelineDTO.setCollector(jsonObject.getJSONObject(AdaptorName.ADAPTOR_NAME_COLLECTOR.getCode()).toString());
        pipelineDTO.setFilter(jsonObject.getJSONObject(AdaptorName.ADAPTOR_NAME_FILTER.getCode()).toString());
        pipelineDTO.setConverter(jsonObject.getJSONObject(AdaptorName.ADAPTOR_NAME_CONVERTER.getCode()).toString());

        pipelineMapperTest.createPipelinetest(pipelineDTO);
        log.info("### pipelineVO id : " + pipelineDTO.getId());

    }

    @Test
    void testgetPipelineVOById() {
        Integer id = 14;
        ResponseEntity pipelineVO = pipelineSVC.getPipelineVOById(id);
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
        //pipelineSVC.deletePipeline(id);
    }

    @Test
    void testisExists() {
        Integer id = 14;
        Boolean result = pipelineSVC.isExists(id);
        assertTrue(result);
    }

    @Test
    void testupdatePipeline() {}

    @Test
    @Transactional
    ResponseEntity testTransactional() throws JsonMappingException, JsonProcessingException {
        Integer id = 1;
        PipelineVO pipelineVO = (PipelineVO) pipelineSVC.getPipelineVOById(id).getBody();
        
        //MultiThread
        // Runnable r = new DeletethreadTest(pipelineVO, id);
        // Thread t = new Thread(r);
        // t.start();

        //DB "Deleting" 상태로  변경
        ResponseEntity result =  pipelineSVC.changePipelineStatus(id, PipelineStatusCode.PIPELINE_STATUS_DELETING.getCode());
        log.info("== Finish Change Pipeline DB to \"Deleting\" ");
        return result;
        

    }

    @Test
    void testCreateCommand() {
        int id = 1;
        //PipelineVO pipelineVO = (PipelineVO) pipelineSVC.getPipelineVOById(id).getBody();
        
        CommandVO commandVO = new CommandVO();
        commandVO.setPipelineId(id);
        commandVO.setCommand(CommandStatusCode.COMMAND_DELETE.getCode());
        commandVO.setStatus(CommandStatusCode.COMMAND_STATUS_DELETING.getCode());
        commandVO.setUserId("나다.오태식이");
        
        pipelineMapperTest.createCommandtest(commandVO);
        // commandVO = pipelineMapperTest.createCommandtest(commandVO.getPipelineId(), commandVO.getCommand(), commandVO.getStatus(), commandVO.getUserId());
        
        log.info("###1 : result Id : ", commandVO);
        log.info("###2 : commandVO.id : ", commandVO.getId());
    }
}

