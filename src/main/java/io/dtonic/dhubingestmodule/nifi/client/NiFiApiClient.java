package io.dtonic.dhubingestmodule.nifi.client;

import io.dtonic.dhubingestmodule.common.component.Properties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.impl.DefaultJwtParser;
import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.api.AccessApi;
import io.swagger.client.api.ConnectionsApi;
import io.swagger.client.api.ControllerServicesApi;
import io.swagger.client.api.FlowApi;
import io.swagger.client.api.FlowfileQueuesApi;
import io.swagger.client.api.ProcessGroupsApi;
import io.swagger.client.api.ProcessorsApi;
import io.swagger.client.api.RemoteProcessGroupsApi;
import io.swagger.client.api.TemplatesApi;
import io.swagger.client.auth.OAuth;

import java.util.Calendar;
import java.util.Date;

import javax.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * NiFi Swegger Client Setup and Token Management
 * @FileName NiFiApiClient.java
 * @Project D.hub Ingest Manager
 * @Brief
 * @Version 1.2
 * @Date 2023. 08. 08.
 * @Author Justin
 */
@Data
@Slf4j
@Component
public class NiFiApiClient {
    
    @Autowired
    private Properties properties;

    private ApiClient nifiSwaggerApiClient = new ApiClient();
    private AccessApi nifiSwaggerAccessApi = new AccessApi();
    private final String BASE_URL = "/nifi-api";
    private String accessToken;

    private FlowApi flow = new FlowApi();
    private ProcessorsApi processors = new ProcessorsApi();
    private ProcessGroupsApi processGroups = new ProcessGroupsApi();
    private RemoteProcessGroupsApi remoteProcessGroups = new RemoteProcessGroupsApi();
    private FlowfileQueuesApi flowfileQueues = new FlowfileQueuesApi();
    private ConnectionsApi connections = new ConnectionsApi();
    private TemplatesApi templates = new TemplatesApi();
    private ControllerServicesApi controllerServices = new ControllerServicesApi();

    @PostConstruct
    public void init() {
        try {
            // Set Up Swagger nifi client
            log.info("NiFi Client is Initialized : {}", properties.getNifiUrl() + BASE_URL);
            nifiSwaggerApiClient.setBasePath(properties.getNifiUrl() + BASE_URL);
            nifiSwaggerApiClient.setVerifyingSsl(false);
            nifiSwaggerAccessApi.setApiClient(nifiSwaggerApiClient);
            // token Setting
            if(properties.getNifiUrl().contains("https://")){
                OAuth auth = (OAuth) nifiSwaggerApiClient.getAuthentication("auth");
                accessToken =
                    nifiSwaggerAccessApi.createAccessToken(
                        properties.getNifiUser(),
                        properties.getNifiPassword()
                    );
                auth.setAccessToken(accessToken);
            } else {
                accessToken = null;
            }
        } catch (ApiException e) {
            log.error("Fail to initialized NiFi Client : {}", properties.getNifiUrl(), e);
        } catch (Exception e){
            log.error("Fail to Connection NiFi, Check NiFi URL : {}", properties.getNifiUrl(), e);
        }
        flow.setApiClient(nifiSwaggerApiClient);
        processors.setApiClient(nifiSwaggerApiClient);
        processGroups.setApiClient(nifiSwaggerApiClient);
        remoteProcessGroups.setApiClient(nifiSwaggerApiClient);
        flowfileQueues.setApiClient(nifiSwaggerApiClient);
        connections.setApiClient(nifiSwaggerApiClient);
        templates.setApiClient(nifiSwaggerApiClient);
        controllerServices.setApiClient(nifiSwaggerApiClient);
    }


    /**
     * Get Expired Time in Token
     *
     * @param access token
     * @return expired time
     */
    private Date getExpiredTimeFromToken(String token) {
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
    @Scheduled(cron = "0 */5 * * * *")
    public void manageToken() {
        try {
            if (properties.getNifiUrl().contains("https://")) {
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
                    log.debug("NiFi Access Token is Allowed");
                }
            } else {
                this.accessToken = null;
            }
        } catch (ApiException e) {
            log.error("Fail to Refresh NiFi Access Token", e);
        }
        
    }
}
