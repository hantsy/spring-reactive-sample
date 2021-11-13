/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.ReactiveRedisMessageListenerContainer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.UUID;

/**
 * @author hantsy
 */
@Configuration
@Slf4j
public class RedisConfig {


    @Bean
    public ReactiveRedisConnectionFactory connectionFactory() {
        return new LettuceConnectionFactory("localhost", 6379);
    }

//    @Bean
//    public ReactiveRedisConnectionFactory lettuceConnectionFactory() {
//
//        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
//            .useSsl().and()
//            .commandTimeout(Duration.ofSeconds(2))
//            .shutdownTimeout(Duration.ZERO)
//            .build();
//
//        return new LettuceConnectionFactory(new RedisStandaloneConfiguration("localhost", 6379), clientConfig);
//    }

    @Bean
    public ReactiveRedisTemplate<String, Post> reactiveRedisTemplate(ReactiveRedisConnectionFactory factory) {
        return new ReactiveRedisTemplate<String, Post>(
                factory,
                RedisSerializationContext.fromSerializer(new Jackson2JsonRedisSerializer(Post.class))
        );
    }

    @Bean
    public ReactiveRedisMessageListenerContainer redisMessageListenerContainer(PostRepository posts, ReactiveRedisConnectionFactory connectionFactory) {
        ReactiveRedisMessageListenerContainer container = new ReactiveRedisMessageListenerContainer(connectionFactory);
        ObjectMapper objectMapper = new ObjectMapper();
        container.receive(ChannelTopic.of("posts"))
                .map(p -> p.getMessage())
                .map(m -> {
                    try {
                        Post post = objectMapper.readValue(m, Post.class);
                        post.setId(UUID.randomUUID().toString());
                        return post;
                    } catch (IOException e) {
                        return null;
                    }
                })
                .switchIfEmpty(Mono.error(new IllegalArgumentException()))
                .flatMap(p -> posts.save(p))
                .subscribe(c -> log.info(" count:" + c), null, () -> log.info("saving post."));
        return container;
    }

}
