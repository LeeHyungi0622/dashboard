package io.dtonic.dhubingestmodule.security.filter;

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.dtonic.dhubingestmodule.common.component.Properties;
import java.util.Collection;
import java.util.LinkedList;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Slf4j
public class JwtAuthorizationFilterTest {

    private Properties properties;
    private LinkedList<GrantedAuthority> grantedAuthorities;

    @BeforeEach
    void init() {
        properties = new Properties();
        properties.setAccessRoleUser("DHUB_ADMIN");

        grantedAuthorities = new LinkedList<>();
        // grantedAuthorities.add(new SimpleGrantedAuthority("DHUB_ADMIN"));
    }

    @Test
    public void roleTest() {
        // 성공
        grantedAuthorities.add(new SimpleGrantedAuthority("DHUB_ADMIN"));

        assertTrue(isAccessible(grantedAuthorities));

        // 실패
        grantedAuthorities.addFirst(new SimpleGrantedAuthority("DHUB_ADMIN0"));
        grantedAuthorities.addFirst(new SimpleGrantedAuthority("DHUB_ADMIN1"));
        assertTrue(isAccessible(grantedAuthorities));
    }

    private boolean isAccessible(Collection<? extends GrantedAuthority> roles) {
        if (roles != null && !roles.isEmpty()) {
            if (properties.getAccessRoleUser() == null) {
                log.warn("not Set up Access Role on ingestManager");
                return false;
            } else {
                for (GrantedAuthority role : roles) {
                    if (role.getAuthority().equals(properties.getAccessRoleUser())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
