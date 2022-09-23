package io.dtonic.dhubingestmodule;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan("io.dtonic.dhubingestmodule")
@SpringBootApplication
public class DHubIngestModuleApplication {

    public static void main(String[] args) {
        SpringApplication.run(DHubIngestModuleApplication.class, args);
    }
}
