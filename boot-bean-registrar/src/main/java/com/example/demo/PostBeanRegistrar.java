package com.example.demo;

import org.springframework.beans.factory.BeanRegistrar;
import org.springframework.beans.factory.BeanRegistry;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;

public class PostBeanRegistrar implements BeanRegistrar {

    @Override
    public void register(BeanRegistry registry, Environment env) {
        if (env.matchesProfiles("h2")) {
            registry.registerBean(PostRepository.class, spec -> spec
                    .supplier(ctx -> new H2PostRepository(ctx.bean(DatabaseClient.class))));
        } else {
            registry.registerBean(PostRepository.class, spec -> spec
                    .supplier(ctx -> new InMemoryPostRepository()));
        }

        registry.registerBean(PostHandler.class, spec -> spec
                .supplier(ctx -> new PostHandler(ctx.bean(PostRepository.class))));

        registry.registerBean(Routes.class, spec -> spec
                .supplier(ctx -> new Routes(ctx.bean(PostHandler.class))));

        registry.registerBean(new ParameterizedTypeReference<RouterFunction<ServerResponse>>() {}, spec -> spec
                .supplier(ctx -> ctx.bean(Routes.class).routes()));

        registry.registerBean(CommandLineRunner.class, spec -> spec
                .supplier(ctx -> args -> {
                    var posts = ctx.bean(PostRepository.class);
                    if (posts instanceof InMemoryPostRepository) {
                        Flux.just(
                                new Post(null, "Spring 7", "Content of Spring 7"),
                                new Post(null, "Bean Registrar", "Content of Bean Registrar")
                        ).flatMap(posts::save).then().block();
                    }
                }));
    }
}