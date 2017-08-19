/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;

import java.util.HashMap;
import java.util.Map;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

/**
 *
 * @author hantsy
 */
@Component
public class UserHandler {

    public Mono<ServerResponse> current(ServerRequest req) {
        return req.principal()
            .cast(UserDetails.class)
            .map(
                user -> {
                    Map<Object, Object> map = new HashMap<>();
                    map.put("usrename", user.getUsername());
                    map.put("roles", AuthorityUtils.authorityListToSet(user.getAuthorities()));
                    return map;
                }
            )
            .flatMap((user) -> ServerResponse.ok().body(BodyInserters.fromObject(user)));
    }
}

