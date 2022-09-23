package io.dtonic.dhubingestmodule.nifi.client;

import com.github.hermannpencole.nifi.swagger.ApiClient;
import com.github.hermannpencole.nifi.swagger.auth.OAuth;
import com.github.hermannpencole.nifi.swagger.client.AccessApi;
import io.dtonic.dhubingestmodule.common.component.Properties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.impl.DefaultJwtParser;
import java.util.Date;
import javax.annotation.PostConstruct;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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

    public void manageToken() {
        if (isExpiredToken(accessToken)) {
            OAuth auth = (OAuth) nifiSwaggerApiClient.getAuthentication("auth");
            accessToken =
                nifiSwaggerAccessApi.createAccessToken(
                    properties.getNifiUser(),
                    properties.getNifiPassword()
                );
            auth.setAccessToken(accessToken);
        }
    }

    public Date getExpiredTimeFromToken(String token) {
        String[] splitToken = token.split("\\.");
        String unsignedToken = splitToken[0] + "." + splitToken[1] + ".";

        DefaultJwtParser parser = new DefaultJwtParser();
        Jwt<?, ?> jwt = parser.parse(unsignedToken);
        Claims claims = (Claims) jwt.getBody();
        return claims.getExpiration();
    }

    public boolean isExpiredToken(String token) {
        Date tokenTime = getExpiredTimeFromToken(token);
        if (tokenTime.compareTo(new Date(System.currentTimeMillis())) < 0) {
            return true;
        } else return false;
    }
}
