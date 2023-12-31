package io.dtonic.dhubingestmodule.common.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dtonic.dhubingestmodule.pipeline.service.PipelineSVC;
import io.dtonic.dhubingestmodule.pipeline.vo.PipelineListResponseVO;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
@EnableScheduling
public class WebSocketHandler extends TextWebSocketHandler {

    @Autowired
    private PipelineSVC pipelineSVC;

    private ObjectMapper mapper = new ObjectMapper();

    private static final ConcurrentHashMap<String, WebSocketSession> CLIENTS = new ConcurrentHashMap<String, WebSocketSession>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        CLIENTS.put(session.getId(), session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status)
        throws Exception {
        CLIENTS.remove(session.getId());
    }

    @Scheduled(fixedDelay = 3000, initialDelay = 3000)
    public void updatePipelineStatus() throws Exception {
       List<PipelineListResponseVO> pipelinesList = pipelineSVC.getPipelineList();

        CLIENTS
            .entrySet()
            .forEach(
                arg -> {
                    try {
                        arg
                            .getValue()
                            .sendMessage(
                                new TextMessage(mapper.writeValueAsString(pipelinesList))
                            );
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            );
    }
}
