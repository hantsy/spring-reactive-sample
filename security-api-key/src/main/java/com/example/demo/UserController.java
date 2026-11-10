package com.example.demo;

import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
class UserController {

    @GetMapping("/user")
    Mono<UserView> current(Mono<Authentication> authentication) {
        return authentication.map(auth -> new UserView(
                auth.getName(),
                auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList(),
                auth.isAuthenticated()));
    }

    record UserView(String name, List<String> authorities, boolean authenticated) {
    }

}