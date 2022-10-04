package io.dtonic.dhubingestmodule;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ComponentScan("io.dtonic.dhubingestmodule")
@ActiveProfiles("dtonic")
class DHubIngestModuleApplicationTests {

    @Test
    void contextLoads() {}
}
