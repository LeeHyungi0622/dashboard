package io.dtonic.dhubingestmodule.security.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.dtonic.dhubingestmodule.common.component.Properties;
import io.dtonic.dhubingestmodule.common.exception.DataCoreUIException;
import io.dtonic.dhubingestmodule.common.service.DataCoreRestSVC;
import io.dtonic.dhubingestmodule.security.vo.AccessTokenFormVO;
import io.dtonic.dhubingestmodule.security.vo.RefreshTokenFormVO;
import io.dtonic.dhubingestmodule.security.vo.UserVO;
import io.dtonic.dhubingestmodule.util.ConvertUtil;
import io.dtonic.dhubingestmodule.util.CryptoUtil;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;

/**
 * Class for Ingest Manager service.
 *
 * @FileName IngestManagerSVC.java
 * @Project D.Hub Ingest Manager
 * @Brief
 * @Version 1.0
 * @Date 2022. 8. 1.
 * @Author Victor
 */
@Slf4j
@Service
public class IngestManagerSVC {

    static final String AUTHORIZATION = "Authorization";
    static final String AUTHORIZATION_CODE = "authorization_code";
    static final String CODE = "code";
    static final String STATE = "state";
    static final String ACCESS_TOKEN = "access_token";
    static final String AUTHTOKEN = "authToken";
    static final String REFRESHTOKEN = "refreshtoken";
    static final String REFRESH_TOKEN = "refresh_token";
    static final String CHAUT = "chaut";
    static final Integer COOKIE_MAX_AGE = 60 * 60 * 1; // 1 hours

    @Autowired
    private Properties properties;

    @Autowired
    private DataCoreRestSVC dataCoreRestSVC;

    private List<String> paths;

    /**
     * Create login uri for SSO authentication.
     *
     * @return Login uri
     */
    public String getLoginUri(HttpServletRequest request) {
        String authorizeUri = properties.getUserAuthorizationUri();
        String redirectUri = properties.getRedirectUri();
        String clientId = properties.getClientId();
        String state = "";
        String loginUri = "";
        String sessionId = request.getSession().getId();

        try {
            if (sessionId != null) {
                state = CryptoUtil.stringToSHA256(sessionId);
            }
        } catch (NoSuchAlgorithmException e) {
            log.error("Fail to create state.", e);
        }

        loginUri =
            authorizeUri +
            "?response_type=code" +
            "&redirect_uri=" +
            redirectUri +
            "&client_id=" +
            clientId +
            "&state=" +
            state;

        log.debug("getLoginUri() - loginUri:{}", loginUri);

        return loginUri;
    }

    /**
     * Set the access token to the cookie
     */
    public void getAccessToken(HttpServletRequest request, HttpServletResponse response) {
        AccessTokenFormVO form = new AccessTokenFormVO();
        form.setGrant_type(AUTHORIZATION_CODE);
        form.setCode(request.getParameter(CODE));
        form.setRedirect_uri(properties.getRedirectUri());
        form.setClient_id(properties.getClientId());
        form.setClient_secret(properties.getClientSecret());

        try {
            ResponseEntity<String> result = dataCoreRestSVC.post(
                properties.getAccessTokenUri(),
                null,
                null,
                form,
                null,
                null,
                String.class
            );

            if (result != null && result.getBody() != null) {
                String tokenJson = result.getBody();
                setTokenToSessionAndCookie(request, response, tokenJson);
            }
        } catch (Exception e) {
            log.error("Failed to get access_token.", e);
        }
    }

    /**
     * Get a refresh token from the SSO server.
     *
     * @return Success: true, Failed: false
     */
    public boolean getRefreshToken(HttpServletRequest request, HttpServletResponse response) {
        Map<String, String> header = new HashMap<String, String>();
        RefreshTokenFormVO form = new RefreshTokenFormVO();
        String authorization = getAuthorization();

        header.put(AUTHORIZATION, "Basic " + authorization);
        form.setGrant_type(REFRESH_TOKEN);
        form.setRefresh_token((String) request.getSession().getAttribute(REFRESHTOKEN));

        try {
            ResponseEntity<String> result = dataCoreRestSVC.post(
                properties.getAccessTokenUri(),
                null,
                header,
                form,
                null,
                null,
                String.class
            );

            if (result != null && result.getBody() != null) {
                String tokenJson = result.getBody();
                setTokenToSessionAndCookie(request, response, tokenJson);
                return true;
            }
        } catch (Exception e) {
            log.error("Failed to get refresh_token.", e);
            return false;
        }

        return false;
    }

    /**
     * Set token to session and cookie
     *
     * @param tokenJson Json type token
     * @throws JsonProcessingException
     */
    private void setTokenToSessionAndCookie(
        HttpServletRequest request,
        HttpServletResponse response,
        String tokenJson
    )
        throws JsonProcessingException {
        Map<String, Object> tokenMap = ConvertUtil.jsonToMap(tokenJson);
        String accessToken = (String) tokenMap.get(ACCESS_TOKEN);
        request.getSession().setAttribute(AUTHTOKEN, accessToken);
        request.getSession().setAttribute(REFRESHTOKEN, (String) tokenMap.get(REFRESH_TOKEN));

        Cookie setCookie = new Cookie(CHAUT, accessToken);
        setCookie.setPath("/");
        setCookie.setMaxAge(COOKIE_MAX_AGE);
        response.addCookie(setCookie);
    }

    /**
     * Get public key or JWT
     *
     * @param jwtToken Json type token
     * @return Public key
     */
    public ResponseEntity<String> getPublicKey(String jwt) {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put(HttpHeaders.AUTHORIZATION, "Bearer " + jwt);

        return dataCoreRestSVC.get(
            properties.getPublicKeyUri(),
            null,
            headers,
            null,
            null,
            null,
            String.class
        );
    }

    /**
     * Get security Enable Info
     *
     * @param jwtToken Json type token
     * @return Public key
     */
    public ResponseEntity<Boolean> getSecurityInfo() {
        return ResponseEntity.ok().body(properties.getSpringSecurityEnabled());
    }

    /**
     * For logout, logout is processed to the SSO server and the session and cookie
     * are cleared.
     *
     * @param object User ID
     * @throws JSONException Throw an exception when a json parsing error occurs.
     * @throws IOException   Throw an exception when an IO error occurs.
     */
    public void logout(HttpServletRequest request, HttpServletResponse response, Object object)
        throws IOException {
        Object principal = getPrincipal(request);
        if (principal != null) {
            UserVO user = new UserVO();
            user.setUserId(principal.toString());
            Map<String, String> headers = new HashMap<String, String>();
            headers.put(
                HttpHeaders.AUTHORIZATION,
                "Bearer " + request.getSession().getAttribute(AUTHTOKEN)
            );
            // SSO Logout
            dataCoreRestSVC.post(
                properties.getLogoutUri(),
                null,
                headers,
                user,
                null,
                null,
                Void.class
            );
        } else if (object != null) {
            UserVO user = new UserVO();

            user.setUserId(object.toString());
            Map<String, String> headers = new HashMap<String, String>();
            headers.put(
                HttpHeaders.AUTHORIZATION,
                "Bearer " + request.getSession().getAttribute(AUTHTOKEN)
            );

            // SSO Logout
            dataCoreRestSVC.post(
                properties.getLogoutUri(),
                null,
                headers,
                user,
                null,
                null,
                Void.class
            );
        }

        // Clear cookie and session
        Cookie setCookie = new Cookie(CHAUT, null);
        setCookie.setPath("/");
        setCookie.setMaxAge(0);
        response.addCookie(setCookie);
        request.getSession().invalidate();
        response.sendRedirect("/");
    }

    /**
     * Get user information
     *
     * @return User information
     */
    public ResponseEntity<UserVO> getUser(HttpServletRequest request) {
        ResponseEntity<UserVO> user = null;

        // test data
        if (!properties.getSpringSecurityEnabled()) {
            return ResponseEntity.ok().body(getTestUser());
        }

        Object principal = getPrincipal(request);

        if (principal != null) {
            String userId = principal.toString();
            List<String> paths = new ArrayList<>();
            paths.add(userId);
            Map<String, String> headers = new HashMap<String, String>();
            headers.put(
                HttpHeaders.AUTHORIZATION,
                "Bearer " + request.getSession().getAttribute(AUTHTOKEN)
            );

            user =
                dataCoreRestSVC.get(
                    properties.getUserInfoUri(),
                    paths,
                    headers,
                    null,
                    null,
                    null,
                    UserVO.class
                );
        } else {
            return null;
        }

        return ResponseEntity.ok().body(user.getBody());
    }

    /**
     * Get user ID
     *
     * @return User ID
     */
    public ResponseEntity<String> getUserId(HttpServletRequest request) {
        // test data
        if (!properties.getSpringSecurityEnabled()) {
            return ResponseEntity.ok().body("cityhub10");
        }

        Object securityContextObject = request
            .getSession()
            .getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
        if (securityContextObject != null) {
            SecurityContext securityContext = (SecurityContext) securityContextObject;
            Authentication authentication = securityContext.getAuthentication();
            if (authentication != null && authentication.getPrincipal() != null) {
                return ResponseEntity.ok().body(authentication.getPrincipal().toString());
            }
        }

        return ResponseEntity.badRequest().build();
    }

    /**
     * Get principal information from request
     *
     * @return Principal
     */
    public Object getPrincipal(HttpServletRequest request) {
        Object securityContextObject = request
            .getSession()
            .getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);

        if (securityContextObject != null) {
            SecurityContext securityContext = (SecurityContext) securityContextObject;
            Authentication authentication = securityContext.getAuthentication();

            if (authentication != null && authentication.getPrincipal() != null) {
                return authentication.getPrincipal();
            }
        }

        return null;
    }

    /**
     * Test user data
     *
     * @return Test user
     */
    private UserVO getTestUser() {
        UserVO user = new UserVO();
        user.setUserId("cityhub10");
        user.setName("홍길동");
        user.setNickname("홍길동");
        user.setPhone("010-1234-5678");

        return user;
    }

    /**
     * Get information of authorization
     *
     * @return Authorization information
     */
    private String getAuthorization() {
        Encoder encoder = Base64.getEncoder();
        String authorization = properties.getClientId() + ":" + properties.getClientSecret();

        return encoder.encodeToString(authorization.getBytes());
    }
}
