package com.example.demo;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import reactor.core.publisher.Mono;

class ApiKeyAuthenticationManager implements ReactiveAuthenticationManager {

    private static final String ROLE_APP = "ROLE_APP";

    private final ClientAppRepository clientApps;

    ApiKeyAuthenticationManager(ClientAppRepository clientApps) {
        this.clientApps = clientApps;
    }

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String apiKey = (String) authentication.getCredentials();
        return this.clientApps.findByApiKey(apiKey)
            .switchIfEmpty(Mono.error(new BadCredentialsException("Invalid API key")))
            .map(clientApp -> ApiKeyAuthentication.authenticated(clientApp.name(), apiKey,
                AuthorityUtils.createAuthorityList(ROLE_APP)));
    }

}
