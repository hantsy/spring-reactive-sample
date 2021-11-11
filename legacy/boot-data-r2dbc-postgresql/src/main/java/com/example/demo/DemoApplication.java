package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.r2dbc.ConnectionFactoryOptionsBuilderCustomizer;
import org.springframework.context.annotation.Bean;

import java.util.HashMap;
import java.util.Map;

import static io.r2dbc.postgresql.PostgresqlConnectionFactoryProvider.OPTIONS;

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@Bean
	public ConnectionFactoryOptionsBuilderCustomizer customizer() {
		Map<String, String> options = new HashMap<>();
		options.put("lock_timeout", "30s");
		options.put("statement_timeout", "60s");

		return (builder) -> builder.option(OPTIONS, options);
	}
}

