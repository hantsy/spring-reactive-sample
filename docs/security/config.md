---
sort: 2
---

# Configure Spring Security

When you are developing a Spring Web application, try to add the following into your project dependencies.

```xml
<dependency>
	<groupId>org.springframework.security</groupId>
	<artifactId>spring-security-core</artifactId>
</dependency>
<dependency>
	<groupId>org.springframework.security</groupId>
	<artifactId>spring-security-config</artifactId>
</dependency>
<dependency>
	<groupId>org.springframework.security</groupId>
	<artifactId>spring-security-webflux</artifactId>
</dependency>
```

Create a configuration class, add `@EnableWebFluxSecurity` annotation to enable  WebFlux support for Spring security.

```java
@EnableWebFluxSecurity
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

	private Mono<AuthorizationDecision> currentUserMatchesPath(Mono<Authentication> authentication, AuthorizationContext context) {
		return authentication
			.map( a -> context.getVariables().get("user").equals(a.getName()))
			.map( granted -> new AuthorizationDecision(granted));
	}

	@Bean
	public MapUserDetailsRepository userDetailsRepository() {
		UserDetails rob = User.withUsername("test").password("test123").roles("USER").build();
		UserDetails admin = User.withUsername("admin").password("admin123").roles("USER","ADMIN").build();
		return new MapUserDetailsRepository(rob, admin);
	}

}
```

1. Use `@EnableWebFluxSecurity` annotation to enable Security for `spring-webflux` based application.
2. `SecurityWebFilterChain` bean is a must to configure the details of Spring Security. `HttpSecurity` is from `spring-secuirty-webflux`, similar with the general version, but handle `WebExhange` instead of Servlet based `WebRequest`.
3. A new `UserDetailsRepository` interface is introduced which is aligned with Reactor APIs. By default, an in-memory `Map` based implementation `MapUserDetailsRepository` is provided, you can customsize yourself by implementing the `UserDetailsRepository` interface.

Starts up the application and verify the Spring Security configuratoin work as expected.

```
mvn spring-boot:run
```

After it is started, try to add a new post without authentication:

```
#curl -v  -X POST http://localhost:8080/posts -H "Content-Type:application/json" -d "{\"title\":\"My Post\",\"content\":\"content of My Post\"}"
Note: Unnecessary use of -X or --request, POST is already inferred.
* timeout on name lookup is not supported
*   Trying ::1...
* TCP_NODELAY set
* Connected to localhost (::1) port 8080 (#0)
> POST /posts HTTP/1.1
> Host: localhost:8080
> User-Agent: curl/7.54.1
> Accept: */*
> Content-Type:application/json
> Content-Length: 42
>
* upload completely sent off: 42 out of 42 bytes
< HTTP/1.1 401 Unauthorized
< WWW-Authenticate: Basic realm="Realm"
< Cache-Control: no-cache, no-store, max-age=0, must-revalidate
< Pragma: no-cache
< Expires: 0
< X-Content-Type-Options: nosniff
< X-Frame-Options: DENY
< X-XSS-Protection: 1 ; mode=block
< content-length: 0
<
* Connection #0 to host localhost left intact
```

The server side rejects the client request, and sends back a 401 error(401 Unauthorized).

Use the predefined **user:password** credentials to get authenticated and send the post request again.

```
curl -v  -X POST http://localhost:8080/posts -u "user:password" -H "Content-Type:application/json" -d "{\"title\":\"My Post\",\"content\":\"content of My Post\"}"
Note: Unnecessary use of -X or --request, POST is already inferred.
* timeout on name lookup is not supported
*   Trying ::1...
* TCP_NODELAY set
* Connected to localhost (::1) port 8080 (#0)
* Server auth using Basic with user 'test'
> POST /posts HTTP/1.1
> Host: localhost:8080
> Authorization: Basic dGVzdDp0ZXN0MTIz
> User-Agent: curl/7.54.1
> Accept: */*
> Content-Type:application/json
> Content-Length: 50
>
* upload completely sent off: 50 out of 50 bytes
< HTTP/1.1 200 OK
< transfer-encoding: chunked
< Content-Type: application/json;charset=UTF-8
< Cache-Control: no-cache, no-store, max-age=0, must-revalidate
< Pragma: no-cache
< Expires: 0
< X-Content-Type-Options: nosniff
< X-Frame-Options: DENY
< X-XSS-Protection: 1 ; mode=block
< set-cookie: SESSION=b99124f7-c0a0-4507-b9be-34718af3d137; HTTPOnly
<
{"id":"59906f9d3c44060e044fb378","title":"My Post","content":"content of My Post","createdDate":[2017,8,13,23,26,21,392000000]}* Connection #0 to host localhost left intact
```

It is done successfully, and returns the new created post.

