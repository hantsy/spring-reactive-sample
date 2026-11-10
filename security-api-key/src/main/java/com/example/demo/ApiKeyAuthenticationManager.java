package com.example.demo;

import java.util.List;
import java.util.Map;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import reactor.core.publisher.Mono;

class ApiKeyAuthenticationManager implements ReactiveAuthenticationManager {

    private final Map<String, String> apiKeys;

    ApiKeyAuthenticationManager(Map<String, String> apiKeys) {
        this.apiKeys = apiKeys;
    }

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String apiKey = (String) authentication.getCredentials();
        String username = this.apiKeys.get(apiKey);
        if (username == null) {
            return Mono.error(new BadCredentialsException("Invalid API key"));
        }
        List<GrantedAuthority> authorities = "admin".equals(username)
            ? AuthorityUtils.createAuthorityList("ROLE_USER", "ROLE_ADMIN")
            : AuthorityUtils.createAuthorityList("ROLE_USER");
        return Mono.just(ApiKeyAuthentication.authenticated(username, apiKey, authorities));
    }

}