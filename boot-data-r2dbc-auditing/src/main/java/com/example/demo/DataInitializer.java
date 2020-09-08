package com.example.demo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
class DataInitializer implements ApplicationRunner {

    private final PostRepository posts;

    private final R2dbcEntityTemplate template;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("start data initialization...");
        this.posts
                .saveAll(
                        List.of(
                                Post.builder().title("Post one").content("The content of post one").build(),
                                Post.builder().title("Post tow").content("The content of post tow").build()
                        )
                )
                .then()
                .thenMany(
                        this.posts.findAll()
                )
                .subscribe((data) -> log.info("found post: {}", data),
                        (err) -> log.error("error: {}", err),
                        () -> log.info("initialization is done...")
                );

        var data = Post.builder().title("testtitle by R2dbcEntityTemplate").content("testcontent").build();
        this.template.insert(data).log().then().block(Duration.ofSeconds(5));
    }
}