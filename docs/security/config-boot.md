---
sort: 3
---

# Configure Spring Security in Spring Boot applications



For Spring Boot applications, add it in the project dependencies aside with `spring-boot-starter-security`.

```xml
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

Then add a `@Configuration` class to customize your security rules.

```java
@Configration
class SecurityConfig {

	@Bean
	SecurityWebFilterChain springWebFilterChain(HttpSecurity http) throws Exception {
		return http
			.authorizeExchange()
				.pathMatchers(HttpMethod.GET, "/posts/**").permitAll()
                .pathMatchers(HttpMethod.DELETE, "/posts/**").hasRole("ADMIN")
				//.pathMatchers("/users/{user}/**").access(this::currentUserMatchesPath)
				.anyExchange().authenticated()
				.and()
			.build();
	}
// ...
}
```

