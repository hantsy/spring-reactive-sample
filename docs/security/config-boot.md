
# Configure Spring Security in Spring Boot applications

For Spring Boot applications, add `spring-boot-starter-security` to the project dependencies.

```xml
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

When WebFlux is detected on the classpath, the starter activates the reactive security stack automatically — HTTP Basic and form login are enabled by default, and every endpoint requires authentication.

Then add a `@Configuration` class to customize your security rules.

```java
@Configuration
class SecurityConfig {

	@Bean
	SecurityWebFilterChain springWebFilterChain(ServerHttpSecurity http) {
		return http
			.authorizeExchange(exchange -> exchange
				.pathMatchers(HttpMethod.GET, "/posts/**").permitAll()
				.pathMatchers(HttpMethod.DELETE, "/posts/**").hasRole("ADMIN")
				//.pathMatchers("/users/{user}/**").access(this::currentUserMatchesPath)
				.anyExchange().authenticated())
			.build();
	}
// ...
}
```

See [Configure Spring Security](config.md) for the full configuration and the user details service.
