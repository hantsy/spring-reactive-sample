package com.example.demo;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.cassandra.CassandraContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

	@Bean
	@ServiceConnection
	CassandraContainer cassandraContainer() {
		return new CassandraContainer(DockerImageName.parse("cassandra:latest"))
				.withInitScript("init.cql")
				.withStartupTimeout(Duration.ofMinutes(5));
	}

}
