/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 *
 * @author hantsy
 */
@RestController
public class UserController {

    private final UserRepository users;

    UserController(UserRepository users) {
        this.users = users;
    }

    @GetMapping("/user")
    public Mono<User> current(@AuthenticationPrincipal Mono<User> principal) {
        return principal;
    }

    @GetMapping("/users/{username}")
    public Mono<User> get(@PathVariable() String username) {
        return this.users.findByUsername(username);
    }
}
