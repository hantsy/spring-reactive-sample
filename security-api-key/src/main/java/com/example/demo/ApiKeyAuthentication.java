package com.example.demo;

import java.util.Collection;
import java.util.List;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

public class ApiKeyAuthentication extends AbstractAuthenticationToken {

    private final String principal;
    private String apiKey;

    private ApiKeyAuthentication(String principal, String apiKey, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        this.apiKey = apiKey;
    }

    public static ApiKeyAuthentication unauthenticated(String apiKey) {
        return new ApiKeyAuthentication(null, apiKey, List.of());
    }

    public static ApiKeyAuthentication authenticated(String principal, String apiKey, Collection<? extends GrantedAuthority> authorities) {
        ApiKeyAuthentication authentication = new ApiKeyAuthentication(principal, apiKey, authorities);
        authentication.setAuthenticated(true);
        return authentication;
    }

    @Override
    public Object getCredentials() {
        return this.apiKey;
    }

    @Override
    public Object getPrincipal() {
        return this.principal;
    }

    @Override
    public void eraseCredentials() {
        super.eraseCredentials();
        this.apiKey = null;
    }

}