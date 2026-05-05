package com.example.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity;

@AutoConfigureWebTestClient
@SpringBootTest(classes = {DemoApplication.class, ContainerConfig.class})
@WithMockUser
public class DemoApplicationTests {

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    PostRepository postRepository;

    WebTestClient client;

    @BeforeEach
    public void setup() {
        client = WebTestClient.bindToApplicationContext(applicationContext)
                .apply(springSecurity())
                .configureClient()
                .build();

        // Clear and seed data for each test to ensure isolation
        postRepository.deleteAll()
                .thenMany(postRepository.saveAll(List.of(
                        Post.builder().title("Post one").content("content of Post one").build(),
                        Post.builder().title("Post two").content("content of Post two").build()
                )))
                .blockLast();
    }

    @Test
    public void getAllPosts_shouldReturnAllPosts() {
        client.get().uri("/posts").exchange()
                .expectStatus().isOk()
                .expectBodyList(Post.class)
                .hasSize(2);
    }

    @Test
    public void createPost_shouldReturnCreatedPost() {
        Post newPost = Post.builder().title("New Post").content("New Content").build();

        client.mutateWith(csrf())
                .post().uri("/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(newPost)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Post.class)
                .value(post -> {
                    assertThat(post.getId()).isNotNull();
                    assertThat(post.getTitle()).isEqualTo("New Post");
                });
    }

    @Test
    public void getPostById_shouldReturnPost() {
        Post post = postRepository.findAll().blockFirst();
        assertThat(post).isNotNull();

        client.get().uri("/posts/" + post.getId()).exchange()
                .expectStatus().isOk()
                .expectBody(Post.class)
                .value(p -> {
                    assertThat(p.getId()).isEqualTo(post.getId());
                    assertThat(p.getTitle()).isEqualTo(post.getTitle());
                });
    }

    @Test
    public void getNonExistentPost_shouldReturn404() {
        client.get().uri("/posts/non-existent-id").exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void updatePost_shouldReturnUpdatedPost() {
        Post post = postRepository.findAll().blockFirst();
        assertThat(post).isNotNull();

        post.setTitle("Updated Title");

        client.mutateWith(csrf())
                .put().uri("/posts/" + post.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(post)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Post.class)
                .value(p -> {
                    assertThat(p.getTitle()).isEqualTo("Updated Title");
                });
    }

    @Test
    public void updateNonExistentPost_shouldReturn404() {
        Post post = Post.builder().title("Title").content("Content").build();

        client.mutateWith(csrf())
                .put().uri("/posts/non-existent-id")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(post)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void deletePost_shouldReturnSuccess() {
        Post post = postRepository.findAll().blockFirst();
        assertThat(post).isNotNull();

        client.mutateWith(csrf())
                .delete().uri("/posts/" + post.getId()).exchange()
                .expectStatus().isOk();

        StepVerifier.create(postRepository.findById(post.getId()))
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    public void deleteNonExistentPost_shouldReturn404() {
        client.mutateWith(csrf())
                .delete().uri("/posts/non-existent-id").exchange()
                .expectStatus().isNotFound();
    }

}
