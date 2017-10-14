/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

/**
 *
 * @author hantsy
 */
@Configuration
//@EnableRedisRepositories
public class RedisConfig {

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory();
    }

//    @Bean
//    public ReactiveRedisTemplate<Object, Object> reactiveRedisTemplate(ReactiveRedisConnectionFactory connectionFactory) {
//        ReactiveRedisTemplate<Object, Object> redisTemplate = new ReactiveRedisTemplate(
//            connectionFactory,
//            RedisSerializationContext.newSerializationContext().build()
//        );
//        return redisTemplate;
//    }

}
