---
sort: 5
---

# Customize UserDetailsRepository



## Load users from a properties file

Spring Security provides a `UserDetailsRepositoryResourceFactoryBean` which allow you load users from a properties file to create the `UserDetailsRepository` for your applications.

```java
@Bean
public UserDetailsRepositoryResourceFactoryBean userDetailsService() {
	return UserDetailsRepositoryResourceFactoryBean
		.fromResourceLocation("classpath:users.properties");
}
```

The content of the *users.properties* is similar to the following.

```
user=password,ROLE_USER
admin=password,ROLE_USER,ROLE_ADMIN
```

The key is username, the value is password, and it's roles.

Source codes: [spring-reactive-sample/security-user-properties]( https://github.com/hantsy/spring-reactive-sample/tree/master/security-user-properties)

## Customize UserDetailsRepository

As said before, you can easily implement your own `UserDetailsRepository`.

Here let's use Mongo as backend store, create a `User` document class which implements spring security specific `UserDetails` interface.

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

Create a generic purpose `Repository` for `User`, named `UserRepository`.

```java
public interface UserRepository extends ReactiveMongoRepository<User, String> {

    Mono<User> findByUsername(String username);
}
```

Replace the `UserDetailsRepository` bean declaration with the following, which connect to the real database.

```java
@Bean
public UserDetailsRepository userDetailsRepository(UserRepository users) {
	return (username) -> {
		return users.findByUsername(username).cast(UserDetails.class);
	};
}
```

Source codes: [spring-reactive-sample/security-data-mongo](https://github.com/hantsy/spring-reactive-sample/tree/master/security-data-mongo).
