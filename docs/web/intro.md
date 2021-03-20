---
sort: 1
---

# An introduction to Reactive Web

Unlike the Servlet stack, the Reactive Web Stack is rewritten from scratch using ReactiveStreams and Reactor API.

There are two important concepts in the *Reactive Web*  stack to handle web request.

* `WebHandler` - which is the high-level APIs to assemble the  resources eg. `WebFilter`, `ExceptionHandler`, etc. to handle web requests from client.
* `HttpHandler` - which is a low-level API to adapt web handlers to the underlying runtime environment, such as Netty, as well as Servlet 3.1+ container which did provide async capability.

You can review the bootstrap `Application` classes in the [Getting Started](../start) part.

To activate *Reactive Web*, create a `@Configuration`class.

```java
@Configuration
@EnableWebFlux
class WebConfig {}
```
Similar to the existing `WebMvcConfigurer` in the Servlet stack.  There is a `WebFluxConfigurer` interface for you  to customize the details of web resources, validation,  CORS,  etc.

You can create a configuration class to derive this interface and override the methods to customize the built-in configuration.

```java
@Configuration
@EnableWebFlux
class WebConfig implements WebFluxConfigurer {}
````