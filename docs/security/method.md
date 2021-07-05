---
sort: 4
---

# Method level constraints

Like traditional Spring WebMvc applications, you can use a `@PreAuthorize("hasRole('ADMIN')")` annotation on your methods to prevent the execution of this method if the evaluation of the expression defined in the `PreAuthorize` is false.

To enable the method level security, add an extra `@EnableReactiveMethodSecurity` to your configuration class.

```java
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
class SecurityConfig {
}
```

In your business codes, add `@PreAuthorize("hasRole('ADMIN')")` annotation to your method.

```java
@PreAuthorize("hasRole('ADMIN')")
Mono<Post> delete(Long id) {
	Post deleted = data.get(id);
	data.remove(id);
	return Mono.just(deleted);
}
```

If you want to Java EE/Jakarta EE compatible annotations, such `RolesAllowed` , etc.   Add an attribute to the `@EnableGlobalMethodSecurity` annotation.

```java
@EnableGlobalMethodSecurity(jsr250Enabled = true)
```

 

