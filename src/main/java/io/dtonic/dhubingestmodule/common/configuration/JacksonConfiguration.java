package io.dtonic.dhubingestmodule.common.configuration;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dtonic.dhubingestmodule.common.code.Constants;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfiguration {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        // SimpleModule module = new SimpleModule();
        // module.addDeserializer(Date.class, new MultiDateDeserializer());
        // objectMapper.registerModule(module);
        objectMapper.setSerializationInclusion(Include.NON_NULL);
        objectMapper.setDateFormat(new SimpleDateFormat(Constants.POSTGRES_TIMESTAMP_FORMAT));
        
        // objectMapper.setTimeZone(TimeZone.getDefault());
        objectMapper.setTimeZone(Calendar.getInstance().getTimeZone());
        return objectMapper;
    }

    // 결국에는 multiDateDeseializer는 안씀
    @Bean
    public ObjectMapper nifiObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        // objectMapper.setTimeZone(TimeZone.getDefault());
        objectMapper.setTimeZone(Calendar.getInstance().getTimeZone());
        objectMapper.setDateFormat(new SimpleDateFormat(Constants.NIFI_CONTENT_DATE_FORMAT));
        objectMapper.setSerializationInclusion(Include.NON_NULL);
        return objectMapper;
    }
}
