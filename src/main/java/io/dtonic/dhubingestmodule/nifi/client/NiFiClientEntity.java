package io.dtonic.dhubingestmodule.nifi.client;

import com.github.hermannpencole.nifi.swagger.ApiClient;
import com.github.hermannpencole.nifi.swagger.auth.OAuth;
import com.github.hermannpencole.nifi.swagger.client.AccessApi;
import io.dtonic.dhubingestmodule.common.component.Properties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.impl.DefaultJwtParser;

import java.util.Calendar;
import java.util.Date;
import javax.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * NiFi Default Client and Token Set up
 * @FileName NiFiClientEntity.java
 * @Project D.hub Ingest Manager
 * @Brief
 * @Version 1.0
 * @Date 2022. 9. 27.
 * @Author Justin
 */
@Slf4j
@Data
@Component
public class NiFiClientEntity {

    @Autowired
    private Properties properties;

    private ApiClient nifiSwaggerApiClient = new ApiClient();

    private AccessApi nifiSwaggerAccessApi = new AccessApi();

    private final String BASE_URL = "/nifi-api";

    private String accessToken;

    @PostConstruct
    public void init() {
        // Set Up Swagger nifi client
        nifiSwaggerApiClient.setBasePath(properties.getNifiUrl() + BASE_URL);
        nifiSwaggerApiClient.setVerifyingSsl(false);
        nifiSwaggerAccessApi.setApiClient(nifiSwaggerApiClient);
        // token Setting
        OAuth auth = (OAuth) nifiSwaggerApiClient.getAuthentication("auth");
        accessToken =
            nifiSwaggerAccessApi.createAccessToken(
                properties.getNifiUser(),
                properties.getNifiPassword()
            );
        auth.setAccessToken(accessToken);
    }

    /**
     * Get Expired Time in Token
     *
     * @param access token
     * @return expired time
     */
    public Date getExpiredTimeFromToken(String token) {
        String[] splitToken = token.split("\\.");
        String unsignedToken = splitToken[0] + "." + splitToken[1] + ".";

        DefaultJwtParser parser = new DefaultJwtParser();
        Jwt<?, ?> jwt = parser.parse(unsignedToken);
        Claims claims = (Claims) jwt.getBody();
        return claims.getExpiration();
    }

    /**
     * Managing NiFi Token
     * If access token issued NiFi is expired, refresh NiFi token.
     *
     * @param access token
     */
    public void manageToken() {
        Date tokenTime = getExpiredTimeFromToken(this.accessToken);
        Calendar systemCal = Calendar.getInstance();
        systemCal.setTime(new Date(System.currentTimeMillis()));
        systemCal.add(Calendar.HOUR, 2);
        if (tokenTime.compareTo(systemCal.getTime()) < 0) {
            log.info("NiFi Access Token is Expired");
            OAuth auth = (OAuth) nifiSwaggerApiClient.getAuthentication("auth");
            this.accessToken =
                nifiSwaggerAccessApi.createAccessToken(
                    properties.getNifiUser(),
                    properties.getNifiPassword()
                );
            auth.setAccessToken(accessToken);
            log.info("NiFi Access Token is Refreshed : AccessToken = {}", accessToken);
        } else {
            log.info("NiFi Access Token is Allowed");
        }
    }
}
