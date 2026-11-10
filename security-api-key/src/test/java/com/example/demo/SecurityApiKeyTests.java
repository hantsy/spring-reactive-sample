package com.example.demo;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = Application.class)
@ActiveProfiles("test")
class SecurityApiKeyTests {

    @Autowired
    ApplicationContext context;

    WebTestClient client;

    @BeforeEach
    void setUp() {
        this.client = WebTestClient.bindToApplicationContext(this.context).build();
    }

    @Test
    void requestWithoutCredentialsIsUnauthorized() {
        this.client.get().uri("/user").exchange()
            .expectStatus().isUnauthorized();
    }

    @Test
    void basicCredentialsAuthenticate() {
        this.client.get().uri("/user").headers(h -> h.setBasicAuth("user", "password")).exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.name").isEqualTo("user")
            .jsonPath("$.authorities[0]").isEqualTo("ROLE_USER");
    }

    @Test
    void wrongBasicPasswordIsUnauthorized() {
        this.client.get().uri("/user").headers(h -> h.setBasicAuth("user", "wrong")).exchange()
            .expectStatus().isUnauthorized();
    }

    @Test
    void validApiKeyAccessesApiPath() {
        this.client.get().uri("/api/posts").header("X-API-KEY", "web-client-api-key").exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$[0].title").isEqualTo("post one");
    }

    @Test
    void apiPathWithoutApiKeyIsUnauthorized() {
        this.client.get().uri("/api/posts").exchange()
            .expectStatus().isUnauthorized();
    }

    @Test
    void invalidApiKeyIsUnauthorized() {
        this.client.get().uri("/api/posts").header("X-API-KEY", "no-such-key").exchange()
            .expectStatus().isUnauthorized();
    }

    @Test
    void invalidApiKeyChallengeDoesNotAdvertiseBasic() {
        this.client.get().uri("/api/posts").header("X-API-KEY", "no-such-key").exchange()
            .expectHeader().doesNotExist("WWW-Authenticate");
    }

    @Test
    void basicCredentialsAreNotCredentialsForTheApiPath() {
        // The /api chain carries no HTTP Basic mechanism, so an Authorization header
        // there is not a rejected identity — it is no identity at all, hence 401 not 403.
        this.client.get().uri("/api/posts").headers(h -> h.setBasicAuth("admin", "password")).exchange()
            .expectStatus().isUnauthorized();
    }

    @Test
    void validApiKeyIsIgnoredOutsideApiPath() {
        this.client.get().uri("/user").header("X-API-KEY", "web-client-api-key").exchange()
            .expectStatus().isUnauthorized();
    }

    @Test
    void validApiKeyDecidesApiPathWhenBasicCredentialsAreAlsoSent() {
        this.client.get().uri("/api/posts")
            .header("X-API-KEY", "web-client-api-key")
            .headers(h -> h.setBasicAuth("user", "password"))
            .exchange()
            .expectStatus().isOk();
    }

    @Test
    void apiKeyAuthenticationDoesNotCreateSession() {
        this.client.get().uri("/api/posts").header("X-API-KEY", "web-client-api-key").exchange()
            .expectStatus().isOk()
            .expectHeader().doesNotExist("Set-Cookie");
    }

    @Test
    void anonymousCannotListClientApps() {
        this.client.get().uri("/clients").exchange()
            .expectStatus().isUnauthorized();
    }

    @Test
    void nonAdminCannotListClientApps() {
        this.client.get().uri("/clients").headers(h -> h.setBasicAuth("user", "password")).exchange()
            .expectStatus().isForbidden();
    }

    @Test
    void adminCanListClientApps() {
        List<Map<String, String>> apps = this.client.get().uri("/clients")
            .headers(h -> h.setBasicAuth("admin", "password"))
            .exchange()
            .expectStatus().isOk()
            .expectBody(new ParameterizedTypeReference<List<Map<String, String>>>() {})
            .returnResult()
            .getResponseBody();

        Map<String, String> keysByAppName = apps.stream()
            .collect(Collectors.toMap(app -> app.get("name"), app -> app.get("apiKey")));
        assertEquals("web-client-api-key", keysByAppName.get("web-client"));
        assertEquals("mobile-client-api-key", keysByAppName.get("mobile-client"));
    }

    @Test
    void nonAdminCannotRegisterClientApp() {
        this.client.post().uri("/clients")
            .headers(h -> h.setBasicAuth("user", "password"))
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(Map.of("name", "batch-worker"))
            .exchange()
            .expectStatus().isForbidden();
    }

    @Test
    void adminRegistersClientAppAndNewKeyAccessesApiPath() {
        EntityExchangeResult<Map<String, Object>> created = this.client.post().uri("/clients")
            .headers(h -> h.setBasicAuth("admin", "password"))
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(Map.of("name", "batch-worker"))
            .exchange()
            .expectStatus().isCreated()
            .expectBody(new ParameterizedTypeReference<Map<String, Object>>() {})
            .returnResult();

        String apiKey = (String) created.getResponseBody().get("apiKey");
        this.client.get().uri("/api/posts").header("X-API-KEY", apiKey).exchange()
            .expectStatus().isOk();
    }

}
