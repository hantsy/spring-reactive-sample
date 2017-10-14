package com.example.demo;


import com.example.demo.pages.HomePage;
import com.example.demo.pages.HomePage.Attribute;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Eddú Meléndez
 * @author Rob Winch
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = Application.class)
//@TestPropertySource(properties = { "spring.profiles.active=embedded-redis", "server.port=0" })
public class AttributeTests {
	@Value("#{@nettyContext.address().getPort()}")
	int port;

	private WebDriver driver;

	@Before
	public void setup() {
		this.driver = new HtmlUnitDriver();
	}

	@After
	public void tearDown() {
		this.driver.quit();
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
