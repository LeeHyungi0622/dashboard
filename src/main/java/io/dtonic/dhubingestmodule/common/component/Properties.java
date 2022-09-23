package io.dtonic.dhubingestmodule.common.component;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Properties component class
 * @FileName Properties.java
 * @Project citydatahub_datacore_ui
 * @Brief
 * @Version 1.0
 * @Date 2022. 3. 24.
 * @Author Elvin
 */
@Data
@Component
public class Properties {

    @Value("${cityhub.client.clientId}")
    private String clientId;

    @Value("${cityhub.client.clientSecret}")
    private String clientSecret;

    @Value("${cityhub.client.accessTokenUri}")
    private String accessTokenUri;

    @Value("${cityhub.client.userAuthorizationUri}")
    private String userAuthorizationUri;

    @Value("${cityhub.client.publicKeyUri}")
    private String publicKeyUri;

    @Value("${cityhub.client.userInfoUri}")
    private String userInfoUri;

    @Value("${cityhub.client.logoutUri}")
    private String logoutUri;

    @Value("${cityhub.client.redirectUri}")
    private String redirectUri;

    @Value("${cityhub.datacore.manager.url}")
    private String datacoreManagerUrl;

    @Value("${cityhub.security.accessRole.user}")
    private String accessRoleUser;

    @Value("${spring.security.enabled}")
    private Boolean springSecurityEnabled;

    @Value("${nifi.url}")
    private String nifiUrl;

    @Value("${nifi.user}")
    private String nifiUser;

    @Value("${nifi.password}")
    private String nifiPassword;
}
