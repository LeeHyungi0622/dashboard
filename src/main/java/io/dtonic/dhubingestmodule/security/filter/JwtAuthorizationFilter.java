package io.dtonic.dhubingestmodule.security.filter;

import io.dtonic.dhubingestmodule.common.component.Properties;
import io.dtonic.dhubingestmodule.security.exception.JwtAuthentioncationException;
import io.dtonic.dhubingestmodule.security.exception.JwtAuthorizationException;
import io.dtonic.dhubingestmodule.security.service.IngestManagerSVC;
import java.io.IOException;
import java.util.Collection;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
// import org.codehaus.jettison.json.JSONException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Class for JWT authorization filter.
 * @FileName JwtAuthorizationFilter.java
 * @Project citydatahub_datacore_ui
 * @Brief
 * @Version 1.0
 * @Date 2022. 3. 24.
 * @Author Elvin
 */
@Slf4j
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private Properties properties;

    private AuthenticationEntryPoint entryPoint;

    private IngestManagerSVC ingestManagerSVC;

    /**
     * Constructor of JwtAuthorizationFilter class
     * @param entryPoint		AuthenticationEntryPoint
     * @param menuSVC			MenuSVC class
     * @param dataCoreUiSVC		DataCoreUiSVC class
     */
    public JwtAuthorizationFilter(
        AuthenticationEntryPoint entryPoint,
        IngestManagerSVC ingestManagerSVC,
        Properties properties
    ) {
        this.entryPoint = entryPoint;
        this.ingestManagerSVC = ingestManagerSVC;
        this.properties = properties;
    }

    /**
     * Check authentication
     */
    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain chain
    )
        throws ServletException, IOException {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null) {
                throw new JwtAuthentioncationException(
                    "JwtAuthorizationFilter : No Authentication Exist"
                );
            }

            if (!isAccessible(authentication.getAuthorities(), request, response)) {
                ingestManagerSVC.logout(request, response, authentication.getPrincipal());
                throw new JwtAuthorizationException(
                    authentication.getPrincipal() + " has not role about the request"
                );
            }

            chain.doFilter(request, response);
        } catch (AuthenticationException e) {
            entryPoint.commence(request, response, e);
            // } catch (JSONException e) {
            //   log.error("Logout failed.", e);
        }
    }

    /**
     * Check whether the user has access.
     * @param roles		GrantedAuthority
     * @return			accessible: true, no accessible: false
     */
    private boolean isAccessible(
        Collection<? extends GrantedAuthority> roles,
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        for (GrantedAuthority role : roles) {
            if (role.getAuthority().equals(properties.getSecurityRole())) {
                return true;
            } else if (properties.getSecurityRole() == null) {
                log.info("not Set up Access Role on ingestManager");
                return false;
            } else {
                log.info("{} is not Role name to access in ingestManager", role.getAuthority());
                return false;
            }
        }
        return false;
    }
}
