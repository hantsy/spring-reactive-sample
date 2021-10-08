package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@Testcontainers
class IntegrationTests {

  @Autowired WebTestClient webClient;

  static DockerImageName DEFAULT_IMAGE_NAME =
      DockerImageName.parse("mcr.microsoft.com/mssql/server:2019-latest")
          .asCompatibleSubstituteFor("mcr.microsoft.com/mssql/server");

  @Container
  static MSSQLServerContainer mssqlserver =
      new MSSQLServerContainer(DEFAULT_IMAGE_NAME).acceptLicense();

  static {
    mssqlserver.start();
  }

  @DynamicPropertySource
  static void registerDynamicProperties(DynamicPropertyRegistry registry) {
    registry.add(
        "spring.r2dbc.url",
        () ->
            "r2dbc:mssql://"
                + mssqlserver.getHost()
                + ":"
                + mssqlserver.getFirstMappedPort()
                + "/tempdb");
    registry.add("spring.r2dbc.username", () -> mssqlserver.getUsername());
    registry.add("spring.r2dbc.password", () -> mssqlserver.getPassword());
  }

  @Test
  public void willLoadPosts() {
    this.webClient
        .get()
        .uri("/posts")
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBodyList(Post.class)
        .hasSize(1);
  }
}
