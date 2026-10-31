
# Form based authentication

Form based authentication is the simple approach to protect web pages.

By default, Spring Security detects the request content type and decides if it should be authenticated by a login form.

For example, there is a `HomeController` class.

```java
@Controller
@Slf4j
public class HomeController {
    private final PostRepository posts;

    HomeController(PostRepository posts) {
        this.posts = posts;
    }

    @GetMapping("/")
    public String home(final Model model) {

        Flux<Post> postsAll = this.posts.findAll();
        model.addAttribute("posts", postsAll);
        return "home";
    }
}
```

And the content of the _home_ template file.

```html
<!-- /WEB-INF/templates/home.ftl -->
<!DOCTYPE html>
<html>
    <head>
        <title>Simple Blog Posts</title>
        <meta charset="UTF-8"/>
        <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    </head>
    <body>
        <h1>All posts</h1>
        <div>
            <table>
                <thead>
                    <tr>
                        <th> ID</th>
                        <th>Title </th>
                        <th>Content</th>
                    </tr>
                </thead>
                <tbody>

                        <#list posts as post>
                        <tr>
                            <td>${post.id}</td>
                            <td>${post.title}</td>
                            <td>${post.content}</td>
                        </tr>
                        <#else>
                        nothing
                        </#list>

                </tbody>
            </table>

        </div>
    </body>
</html>
```

Add a simple configuration to protect the home page.

```java
@Configuration
@EnableWebFluxSecurity
class SecurityConfig {

    @Bean
    SecurityWebFilterChain springWebFilterChain(ServerHttpSecurity http) throws Exception {
        return http.authorizeExchange()
                    .anyExchange().authenticated()
//                .and()
//                    .formLogin()
                .and()
                .build();
    }
    //...
}
```

By default, when accessing the home URL _http://localhost:8080/_, the `HomeController` handles the request and renders the _home.ftl_ template into an HTML page, and Spring Security invokes _form_ based authentication to protect _web pages_.

By default, Spring Security provides a simple login form page. Alternatively you can customize the login form attributes or specify a new login page in the Spring Security configuration:

```java
.formLogin(formLogin -> formLogin
        .loginPage("/login"))
```

Source codes: [spring-reactive-sample/security-form](https://github.com/hantsy/spring-reactive-sample/tree/master/security-form).
