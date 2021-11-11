package com.example.demo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.springframework.data.domain.Sort.Order.desc;

@Component
@Slf4j
@RequiredArgsConstructor
class DataInitializer implements ApplicationRunner {

    private final PostRepository posts;

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
                .thenMany(
                        this.posts.findAll()
                )
                .subscribe((data) -> log.info("post:" + data),
                        (err) -> log.error("error" + err),
                        () -> log.info("initialization is done...")
                );
    }
}