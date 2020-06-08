package uk.gov.hmcts.reform.judicialbooking.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import uk.gov.hmcts.reform.auth.checker.spring.serviceanduser.ServiceAndUserDetails;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.judicialbooking.util.SecurityUtils;

class SecurityUtilsTest {

    private final AuthTokenGenerator authTokenGenerator = mock(AuthTokenGenerator.class);
    private final SecurityUtils securityUtils = new SecurityUtils(authTokenGenerator);
    private final String serviceAuthorization = "auth";
    private static final String USER_ID = "userId";

    @Test
    void shouldGetAuthorizationHeaders() {
        mockSecurityContextData();

        HttpHeaders httpHeaders = securityUtils.authorizationHeaders();
        assertEquals(2, httpHeaders.size());
    }

    private void mockSecurityContextData() {
        List<String> collection = new ArrayList<String>();
        collection.add("string");
        ServiceAndUserDetails serviceAndUserDetails = new ServiceAndUserDetails(
            USER_ID,
            serviceAuthorization,
            collection,
            "servicename"
        );
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication().getPrincipal()).thenReturn(serviceAndUserDetails);
    }

    @Test
    void getUserId() {
        mockSecurityContextData();
        assertEquals("userId", securityUtils.getUserId());
    }

    @Test
    void getUserToken() {
        mockSecurityContextData();
        assertEquals("auth", securityUtils.getUserToken());
    }

    @Test
    void getUserRolesHeader() {
        mockSecurityContextData();
        assertEquals("string", securityUtils.getUserRolesHeader());
    }

    @Test
    void getServiceId() {
        mockSecurityContextData();
        assertEquals("servicename", securityUtils.getServiceId());
    }

}