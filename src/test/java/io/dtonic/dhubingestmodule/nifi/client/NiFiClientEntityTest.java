package io.dtonic.dhubingestmodule.nifi.client;

import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@Slf4j
@SpringBootTest
@ActiveProfiles("dtonic")
public class NiFiClientEntityTest {

    @Autowired
    private NiFiClientEntity niFiClientEntity;

    @Test
    public void getExpiredTimeFromTokenTest() {
        String accessToken = niFiClientEntity.getAccessToken();
        Date result = niFiClientEntity.getExpiredTimeFromToken(accessToken);
        log.debug("jwt payload : [{}]", result);
    }

    @Test
    public void isExpiredTokenTest() {}

    @Test
    public void manageTokenTest() {}
}
