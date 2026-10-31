
# In-memory sessions

The simplest `ReactiveSessionRepository` is a `ConcurrentHashMap` behind `ReactiveMapSessionRepository`. It demonstrates the full Spring Session setup without any external infrastructure.

## Step 1: add the dependency

```xml
<dependency>
    <groupId>org.springframework.session</groupId>
    <artifactId>spring-session-core</artifactId>
</dependency>
```

## Step 2: enable Spring Web Session

Create a configuration class, annotate it with `@EnableSpringWebSession`, and declare the repository bean:

```java
@Configuration
@EnableSpringWebSession
public class SessionConfig {

    @Bean
    public ReactiveSessionRepository<?> sessionRepository() {
        return new ReactiveMapSessionRepository(new ConcurrentHashMap<>());
    }
}
```

`@EnableSpringWebSession` installs the `WebSessionManager` that routes every `WebSession` interaction through the repository.

## Step 3: use WebSession in a controller

```java
@Controller
public class SessionController {

    @PostMapping("/session")
    public String setAttribute(@ModelAttribute SessionAttributeForm sessionAttributeForm, WebSession session) {
        session.getAttributes().put(sessionAttributeForm.getAttributeName(), sessionAttributeForm.getAttributeValue());
        return "redirect:/";
    }

    @GetMapping("/")
    public String index(Model model, WebSession webSession) {
        model.addAttribute("webSession", webSession);
        return "index";
    }
}
```

A Thymeleaf form posts attribute name/value pairs, and the index page renders the current session's attributes:

```html
<form class="form-inline" role="form" action="./session" method="post">
    <label for="attributeName">Attribute Name</label>
    <input id="attributeName" type="text" name="attributeName"/>
    <label for="attributeValue">Attribute Value</label>
    <input id="attributeValue" type="text" name="attributeValue"/>
    <input type="submit" value="Set Attribute"/>
</form>

<table class="table table-striped">
    <thead>
    <tr>
        <th>Attribute Name</th>
        <th>Attribute Value</th>
    </tr>
    </thead>
    <tbody>
    <tr th:each="attr : ${webSession.attributes}">
        <td th:text="${attr.key}"></td>
        <td th:text="${attr.value}"></td>
    </tr>
    </tbody>
</table>
```

## Verify

Start the application and open http://localhost:8080. Submit the form — the attribute appears in the table, and the browser stores a `SESSION` cookie (not `JSESSIONID`).

## Test

The sample verifies the flow with an HtmlUnit-driven test that boots the real server:

```java
@Test
public void createAttribute() {
    HomePage home = HomePage.go(this.driver, this.port);

    home = home.form()
            .attributeName("a")
            .attributeValue("b")
            .submit(HomePage.class);

    List<Attribute> attributes = home.attributes();
    assertThat(attributes).hasSize(1);
    Attribute row = attributes.get(0);
    assertThat(row.getAttributeName()).isEqualTo("a");
    assertThat(row.getAttributeValue()).isEqualTo("b");
}
```

For the complete codes, check [spring-reactive-sample/session](https://github.com/hantsy/spring-reactive-sample/tree/master/session).

## What's next

The in-memory map does not survive restarts and is not shared between instances. For production, use [Redis](redis.md) or [Mongo](mongo.md) as the session store.
