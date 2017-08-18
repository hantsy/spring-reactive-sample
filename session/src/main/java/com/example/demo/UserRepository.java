/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.select;
import org.springframework.data.cassandra.core.ReactiveCassandraTemplate;
import org.springframework.data.cassandra.core.query.Query;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 *
 * @author hantsy
 */
@Component
public class UserRepository implements UserDetailsRepository {

    private final ReactiveCassandraTemplate template;

    public UserRepository(ReactiveCassandraTemplate template) {
        this.template = template;
    }

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return this.template
            .selectOne(
                select()
                    .from("users")
                    .where(eq("username", username)),
                User.class
            )
            .map(user -> org.springframework.security.core.userdetails.User.withUserDetails(user))
            .cast(UserDetails.class);
    }
    
    public Mono<User> save(User  user){
        return this.template.insert(user);
    }
    
    public Mono<Boolean> deleteAll(){
        return this.template.delete(Query.empty(), User.class);
    }

}
