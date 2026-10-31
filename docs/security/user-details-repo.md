
# Customize UserDetailsRepository

The authentication manager resolves users through a `ReactiveUserDetailsService`, whose single method returns a `Mono<UserDetails>`. The in-memory `MapReactiveUserDetailsService` covers demos; this page shows two ways to replace it.

## Load users from a properties file

Spring Security provides a `ReactiveUserDetailsServiceResourceFactoryBean` which loads users from a properties file and creates the `ReactiveUserDetailsService` for your applications:

```java
@Bean
public ReactiveUserDetailsServiceResourceFactoryBean userDetailsService() {
    return ReactiveUserDetailsServiceResourceFactoryBean
        .fromResourceLocation("classpath:users.properties");
}
```

The content of the _users.properties_ file is similar to the following:

```
user=password,ROLE_USER
admin=password,ROLE_USER,ROLE_ADMIN
```

The key is the username; the value is the password and its roles.

Source codes: [spring-reactive-sample/security-user-properties](https://github.com/hantsy/spring-reactive-sample/tree/master/security-user-properties)

## Load users from a database

For real applications, implement the `ReactiveUserDetailsService` against your user store. Using Mongo as the backend store, create a `User` document class that implements the Spring Security `UserDetails` interface:

```java
@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document
class User implements UserDetails {

    @Id
    private String id;
    private String username;
    private String password;

    @Builder.Default()
    private boolean active = true;

    @Builder.Default()
    private List<String> roles = new ArrayList<>();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return AuthorityUtils.createAuthorityList(roles.toArray(new String[roles.size()]));
    }

    @Override
    public boolean isAccountNonExpired() {
        return active;
    }

    @Override
    public boolean isAccountNonLocked() {
        return active;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return active;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}
```

Create a `UserRepository` for `User`:

```java
public interface UserRepository extends ReactiveMongoRepository<User, String> {

    Mono<User> findByUsername(String username);
}
```

Replace the in-memory user details service bean with the following, which connects to the real database:

```java
@Bean
public ReactiveUserDetailsService userDetailsService(UserRepository users) {
    return (username) -> users.findByUsername(username).cast(UserDetails.class);
}
```

The [security-data-mongo](https://github.com/hantsy/spring-reactive-sample/tree/master/security-data-mongo) sample goes one step further and registers a `PasswordEncoder`:

```java
@Bean
PasswordEncoder passwordEncoder() {
    return PasswordEncoderFactories.createDelegatingPasswordEncoder();
}
```

Source codes: [spring-reactive-sample/security-data-mongo](https://github.com/hantsy/spring-reactive-sample/tree/master/security-data-mongo).
