package com.example.demo;


import com.example.demo.pages.HomePage;
import com.example.demo.pages.HomePage.Attribute;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Eddú Meléndez
 * @author Rob Winch
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = Application.class)
class AttributeTests {
    @Value("${server.port:8080}")
    int port;

    private WebDriver driver;

    @Autowired
    HttpServer httpServer;

    private DisposableServer disposableServer;

    @BeforeAll
    public void setup() {
        this.disposableServer = this.httpServer.bindNow();
        this.driver = new HtmlUnitDriver();
    }

    @AfterAll
    public void tearDown() {
        this.driver.quit();
        this.disposableServer.dispose();
    }

    @Test
    void home() {
        HomePage home = HomePage.go(this.driver, this.port);
        home.assertAt();
    }

    @Test
    void createAttribute() {
        HomePage home = HomePage.go(this.driver, this.port);
		// asserting no attributes are present
		assertThat(home.attributes()).isEmpty();
        // @formatter:off
        home = home.form()
                .attributeName("a")
                .attributeValue("b")
                .submit(HomePage.class);
        // @formatter:on

        List<Attribute> attributes = home.attributes();
        assertThat(attributes).hasSize(1);
        Attribute row = attributes.get(0);
        assertThat(row.getAttributeName()).isEqualTo("a");
        assertThat(row.getAttributeValue()).isEqualTo("b");
    }

}
