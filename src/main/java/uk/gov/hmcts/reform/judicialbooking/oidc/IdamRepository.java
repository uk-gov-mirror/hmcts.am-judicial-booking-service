package uk.gov.hmcts.reform.judicialbooking.oidc;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.idam.client.IdamApi;
import uk.gov.hmcts.reform.idam.client.OAuth2Configuration;
import uk.gov.hmcts.reform.idam.client.models.TokenRequest;
import uk.gov.hmcts.reform.idam.client.models.TokenResponse;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.List;

import static java.util.Objects.requireNonNull;
import static org.springframework.http.HttpMethod.GET;

@Component
@Slf4j
public class IdamRepository {

    private IdamApi idamApi;
    private OIdcAdminConfiguration oidcAdminConfiguration;
    private OAuth2Configuration oauth2Configuration;
    private RestTemplate restTemplate;
    public static final String BEARER = "Bearer ";
    @Value("${idam.api.url}")
    protected String idamUrl;

    @Value("${spring.cache.type}")
    protected String cacheType;

    private CacheManager cacheManager;


    @Autowired
    public IdamRepository(IdamApi idamApi,
                          OIdcAdminConfiguration oidcAdminConfiguration,
                          OAuth2Configuration oauth2Configuration,
                          RestTemplate restTemplate, CacheManager cacheManager) {
        this.idamApi = idamApi;
        this.oidcAdminConfiguration = oidcAdminConfiguration;
        this.oauth2Configuration = oauth2Configuration;
        this.restTemplate = restTemplate;

        this.cacheManager = cacheManager;
    }

    @Cacheable(value = "token")
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 2000, multiplier = 3))
    public UserInfo getUserInfo(String jwtToken) {
        if (cacheType != null && !cacheType.equals("none")) {
            CaffeineCache caffeineCache = (CaffeineCache) cacheManager.getCache("token");
            com.github.benmanes.caffeine.cache.Cache<Object, Object> nativeCache = requireNonNull(caffeineCache)
                .getNativeCache();
            log.info("generating Bearer Token, current size of cache: {}", nativeCache.estimatedSize());
        }
        return idamApi.retrieveUserInfo(BEARER + jwtToken);
    }

    public UserDetails getUserByUserId(String jwtToken, String userId) {
        return idamApi.getUserByUserId(BEARER + jwtToken, userId);
    }

    public ResponseEntity<List<Object>> searchUserByUserId(String jwtToken, String userId) {
        try {
            String url = String.format("%s/api/v1/users?query=%s", idamUrl, userId);
            ResponseEntity<List<Object>> response = restTemplate.exchange(
                url,
                GET,
                new HttpEntity<>(getHttpHeaders(jwtToken)),
                new ParameterizedTypeReference<List<Object>>() {
                }
            );
            if (HttpStatus.OK.equals(response.getStatusCode())) {
                return response;
            }
        } catch (Exception exception) {
            log.info(exception.getMessage());
            throw exception;
        }
        return null;
    }

    private static HttpHeaders getHttpHeaders(String jwtToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken);
        return headers;
    }


    public String getManageUserToken() {
        TokenRequest tokenRequest = new TokenRequest(
            oauth2Configuration.getClientId(),
            oauth2Configuration.getClientSecret(),
            "password",
            "",
            oidcAdminConfiguration.getUserId(),
            oidcAdminConfiguration.getSecret(),
            oidcAdminConfiguration.getScope(),
            "4",
            ""
        );
        TokenResponse tokenResponse = idamApi.generateOpenIdToken(tokenRequest);
        return tokenResponse.accessToken;
    }

}
