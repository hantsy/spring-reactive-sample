package com.example.demo;

import com.example.demo.pages.HomePage;
import com.example.demo.pages.HomePage.Attribute;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Eddú Meléndez
 * @author Rob Winch
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = Application.class)
@Testcontainers
class AttributeTests {

  static DockerImageName redisDockerImageName = DockerImageName.parse("redis");

  @Container
  protected static final RedisContainer REDIS_DB_CONTAINER =
      new RedisContainer(redisDockerImageName).withExposedPorts(6379);

  static {
    REDIS_DB_CONTAINER.start();
  }

  @Value("${server.port:8080}")
  int port;
  @Autowired HttpServer httpServer;
  private DisposableServer disposableServer;
  private WebDriver driver;

  @DynamicPropertySource
  static void setApplicationProperties(DynamicPropertyRegistry propertyRegistry) {
    propertyRegistry.add("spring.redis.host", REDIS_DB_CONTAINER::getHost);
    propertyRegistry.add("spring.redis.port", REDIS_DB_CONTAINER::getFirstMappedPort);
  }

  @BeforeAll
  void setup() {
    this.disposableServer = this.httpServer.bindNow();
    this.driver = new HtmlUnitDriver();
  }

  @AfterAll
  void tearDown() {
    this.disposableServer.dispose();
    this.driver.quit();
  }

  @Test
  void home() {
    HomePage home = HomePage.go(this.driver, this.port);
    home.assertAt();
  }

  @Test
  void createAttribute() {
    HomePage home = HomePage.go(this.driver, this.port);
    // asserting that no attributes are created
    assertThat(home.attributes()).isEmpty();
    // @formatter:off
    home = home.form().attributeName("a").attributeValue("b").submit(HomePage.class);
    // @formatter:on

    List<Attribute> attributes = home.attributes();
    assertThat(attributes).hasSize(1);
    Attribute row = attributes.get(0);
    assertThat(row.getAttributeName()).isEqualTo("a");
    assertThat(row.getAttributeValue()).isEqualTo("b");
  }

  private static class RedisContainer extends GenericContainer<RedisContainer> {

    public RedisContainer(final DockerImageName dockerImageName) {
      super(dockerImageName);
    }
  }
}
