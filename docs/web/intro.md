---
sort: 1
---

# An introduction to Reactive Web

Unlike the Servlet stack, the Reactive Web Stack is rewritten from scratch using ReactiveStreams and Reactor API.

There are two important concepts in the *Reactive Web*  stack to handle web request.

* `WebHandler` - which is the high-level APIs to assemble the  resources to handle web requests from client.
* `HttpHandler` - which is a low-level API to adapt web handlers to the underlying runtime environment, such as Netty, as well as Servlet 3.1+ container which did provide async capability.

You can review the bootstrap `Application` classes in the [Getting Started](../start) part.

To activate *Reactive Web*, create a `@Configuration`class.

```java
@Configuration
@EnableWebFlux
class WebConfig {}
```
Similiar to the existing WebMVC configuration, to configure the details of web resources, validation, cors,  etc., just create a configuration class to derive  from the  `WebFluxConfigurer` interface.

```java
@Configuration
@EnableWebFlux
class WebConfig implements WebFluxConfigurer {}
````