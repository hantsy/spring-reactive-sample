# Programmatic Bean Registration with BeanRegistrar

Most applications declare beans with `@Component` scanning or `@Bean` methods, but some wiring is better expressed programmatically — conditional on the environment, assembled from a library's own types, or composed from several independent registration units. Spring Framework 7 introduces `BeanRegistrar` as a dedicated contract for this, replacing the older context-level `registerBean` approach with a bean-definition-stage API that participates in the configuration model and AOT processing.

## Existing alternatives

Before Spring Framework 7, programmatic registration typically went through one of these routes:

- `BeanDefinitionRegistryPostProcessor` or `ImportBeanDefinitionRegistrar` — powerful but low-level: you manipulate raw `BeanDefinition` objects and run at a specific point of the container lifecycle.
- `GenericApplicationContext.registerBean(...)` (since Spring Framework 5) — a friendly context-level API, but the registration happens outside the configuration model, and the common usage pattern constructs the objects eagerly before `refresh()`.

The [register-bean](https://github.com/hantsy/spring-reactive-sample/tree/master/register-bean) sample shows the latter:

```java
AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
PostRepository posts = new PostRepository();
PostHandler postHandler = new PostHandler(posts);
Routes routesBean = new Routes(postHandler);

context.registerBean(PostRepository.class, () -> posts);
context.registerBean(PostHandler.class, () -> postHandler);
context.registerBean(Routes.class, () -> routesBean);
context.registerBean(WebHandler.class, () -> RouterFunctions.toWebHandler(routesBean.routes(), HandlerStrategies.builder().build()));
context.refresh();
```

The objects are built by hand and captured in supplier lambdas; dependency wiring, lifecycle metadata, and profile conditions are all manual.

## BeanRegistrar: the Spring Framework 7 alternative

`BeanRegistrar` (`org.springframework.beans.factory`) is a single-method contract:

```java
public interface BeanRegistrar {
    void register(BeanRegistry registry, Environment env);
}
```

Activate it with `@Import` on any `@Configuration` class (including the `@SpringBootApplication` class) — `ConfigurationClassPostProcessor` picks it up while parsing the configuration model. In a plain Spring application you can also call `GenericApplicationContext.register(BeanRegistrar)` directly.

Compared with `context.registerBean(...)`, it brings several improvements:

- **Bean-definition stage, not context level.** Registrations go through `BeanRegistry` into bean definitions before the context refreshes. Suppliers are deferred — nothing is constructed at registration time — and the registrations participate in the same configuration-model processing as `@Bean` methods.
- **Environment access.** The `Environment` argument lets you branch on profiles and properties directly, e.g. `env.matchesProfiles("h2")`.
- **Typed, fluent bean specs.** `registerBean(Class, Consumer<Spec>)` exposes metadata as chainable options: `supplier`, `primary`, `fallback`, `order`, `lazyInit`, `backgroundInit`, `prototype`, `scope`, `description`, `infrastructure`, and `notAutowirable`.
- **Deferred dependency resolution.** The supplier receives a `SupplierContext`; `ctx.bean(SomeType.class)` (or `beanProvider(...)` for lazy/optional access) resolves collaborators at instantiation time instead of capturing pre-built instances.
- **Composition.** `registry.register(otherRegistrar)` nests registrars, `registerAlias` adds aliases, and `containsBean` guards against duplicates.
- **AOT and GraalVM native support.** Because registrars are processed as part of the configuration model, `ConfigurationClassPostProcessor` generates AOT contributions for them — registrations survive ahead-of-time processing and native-image builds.

**Note:** the `Spec` API has no qualifier support. For resolution conflicts, use `primary()`, `fallback()`, and `order(int)`.

Generic types need a `ParameterizedTypeReference` so the full type is recorded in the bean definition:

```java
registry.registerBean(new ParameterizedTypeReference<RouterFunction<ServerResponse>>() {}, spec -> spec
        .supplier(ctx -> ctx.bean(Routes.class).routes()));
```

## Example: switching repositories by profile

The [boot-bean-registrar](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-bean-registrar) sample registers a complete WebFlux application — repository, handler, routes, and a data initializer — from a single registrar, and switches between an in-memory store and H2 (R2DBC) with a profile:

```java
public class PostBeanRegistrar implements BeanRegistrar {

    @Override
    public void register(BeanRegistry registry, Environment env) {
        if (env.matchesProfiles("h2")) {
            registry.registerBean(PostRepository.class, spec -> spec
                    .supplier(ctx -> new H2PostRepository(ctx.bean(DatabaseClient.class))));
        } else {
            registry.registerBean(PostRepository.class, spec -> spec
                    .supplier(ctx -> new InMemoryPostRepository()));
        }

        registry.registerBean(PostHandler.class, spec -> spec
                .supplier(ctx -> new PostHandler(ctx.bean(PostRepository.class))));

        registry.registerBean(Routes.class, spec -> spec
                .supplier(ctx -> new Routes(ctx.bean(PostHandler.class))));

        registry.registerBean(new ParameterizedTypeReference<RouterFunction<ServerResponse>>() {}, spec -> spec
                .supplier(ctx -> ctx.bean(Routes.class).routes()));

        registry.registerBean(CommandLineRunner.class, spec -> spec
                .supplier(ctx -> args -> {
                    var posts = ctx.bean(PostRepository.class);
                    if (posts instanceof InMemoryPostRepository) {
                        Flux.just(
                                new Post(null, "Spring 7", "Content of Spring 7"),
                                new Post(null, "Bean Registrar", "Content of Bean Registrar")
                        ).flatMap(posts::save).then().block();
                    }
                }));
    }
}
```

Reading the registrar:

- `env.matchesProfiles("h2")` selects the repository implementation at registration time. Under the `h2` profile, `ctx.bean(DatabaseClient.class)` resolves the `DatabaseClient` that Spring Boot's R2DBC auto-configuration provides from `spring.r2dbc.url`.
- The `RouterFunction<ServerResponse>` bean is what Spring Boot's `RouterFunctionMapping` picks up to serve the functional endpoints — no manual `WebHandler`/server bootstrap is needed in a Boot application.
- The `CommandLineRunner` seeds sample data only for the in-memory variant; the H2 variant is populated by `schema.sql`/`data.sql`, which Boot runs automatically for the embedded database.

Import it on the application class:

```java
@SpringBootApplication
@Import(PostBeanRegistrar.class)
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
```

The profile switch is plain Boot configuration — `application.properties` sets the default `spring.r2dbc.url`, and `application-h2.properties` overrides it with a separate in-memory database. Run with the H2 profile via `mvn spring-boot:run -Dspring-boot.run.profiles=h2`.

The sample verifies both variants with `WebTestClient`: `DemoApplicationTests` runs against the default in-memory context, and `H2ApplicationTests` adds `@ActiveProfiles("h2")` to exercise the R2DBC-backed repository.

## Kotlin: BeanRegistrarDsl

Kotlin gets a DSL on top of the same contract — `BeanRegistrarDsl` — with reified `registerBean { }`, `bean<T>()` resolution inside suppliers, and `profile("...") { }` blocks:

```kotlin
class PostBeanRegistrar : BeanRegistrarDsl({
    registerBean { InMemoryPostRepository() }
    registerBean { PostHandler(bean()) }
    registerBean { Routes(bean()) }
    registerBean { bean<Routes>().routes() }
})
```

See [Kotlin DSLs](../kotlin/dsl.md#beanregistrardsl) for the full DSL walkthrough and the [boot-bean-registrar-kotlin-dsl](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-bean-registrar-kotlin-dsl) sample for a complete coroutine-based application built with it.