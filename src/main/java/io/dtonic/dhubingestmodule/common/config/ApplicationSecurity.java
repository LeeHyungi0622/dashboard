package io.dtonic.dhubingestmodule.common.config;

import io.dtonic.dhubingestmodule.common.component.Properties;
import io.dtonic.dhubingestmodule.security.filter.JwtAuthenticationFilter;
import io.dtonic.dhubingestmodule.security.filter.JwtAuthorizationFilter;
import io.dtonic.dhubingestmodule.security.handler.IngestManagerAuthenticationEntryPoint;
import io.dtonic.dhubingestmodule.security.service.IngestManagerSVC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

/**
 * Class for application security
 * WebSecurityConfigurerAdapter 가 deprecated 됨에 따라
 * 새로운 방식으로 변경 필요
 * https://spring.io/blog/2022/02/21/spring-security-without-the-websecurityconfigureradapter
 *
 * @FileName ApplicationSecurity.java
 * @Project citydatahub_datacore_ui
 * @Brief
 * @Version 1.0
 * @Date 2022. 3. 24.
 * @Author Elvin
 */
@EnableWebSecurity
@Configuration
public class ApplicationSecurity {

    /**
     * Set up application http security configuration.
     */
    @Bean
    public SecurityFilterChain filterChain(
        HttpSecurity http,
        IngestManagerSVC ingestManagerSVC,
        Properties properties
    )
        throws Exception {
        if (Boolean.TRUE.equals(properties.getSpringSecurityEnabled())) {
            http
                .authorizeRequests()
                .antMatchers("/error**")
                .permitAll()
                .anyRequest()
                .authenticated()
                .and()
                .formLogin()
                .disable()
                .csrf()
                .disable()
                .headers()
                .disable()
                .httpBasic()
                .disable()
                .rememberMe()
                .disable()
                .logout()
                .disable()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .exceptionHandling()
                .and()
                .addFilterAfter(
                    new JwtAuthenticationFilter(authenticationEntryPoint(), ingestManagerSVC),
                    BasicAuthenticationFilter.class
                )
                .addFilterAfter(
                    new JwtAuthorizationFilter(
                        authenticationEntryPoint(),
                        ingestManagerSVC,
                        properties
                    ),
                    JwtAuthenticationFilter.class
                );
        } else {
            http.csrf().disable().authorizeRequests().anyRequest().permitAll();
        }

        return http.build();
    }

    /**
     * Set up application web security configuration.
     */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring().antMatchers("/accesstoken", "/logout");
    }

    /**
     * Create bean for DataCoreUIAuthenticationEntryPoint class
     *
     * @return
     */
    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return new IngestManagerAuthenticationEntryPoint();
    }
}
