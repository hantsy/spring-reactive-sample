package com.example.demo;


import com.example.demo.pages.HomePage;
import com.example.demo.pages.HomePage.Attribute;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig(classes = Application.class)
public class AttributeTests {
    @Value("${server.port:8080}")
    int port;

    private WebDriver driver;

    @Autowired
    HttpServer httpServer;

    private DisposableServer disposableServer;

    @BeforeEach
    public void setup() {
        this.disposableServer = this.httpServer.bindNow();
        this.driver = new HtmlUnitDriver();
    }

    @AfterEach
    public void tearDown() {
        this.driver.quit();
        this.disposableServer.dispose();
    }

    @Test
    public void home() {
        HomePage home = HomePage.go(this.driver, this.port);
        home.assertAt();
    }

    @Test
    public void noAttributes() {
        HomePage home = HomePage.go(this.driver, this.port);
        assertThat(home.attributes()).isEmpty();
    }

    @Test
    public void createAttribute() {
        HomePage home = HomePage.go(this.driver, this.port);
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
