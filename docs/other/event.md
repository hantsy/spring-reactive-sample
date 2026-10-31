
# Application Events

Spring's `ApplicationEventPublisher`/`@EventListener` mechanism supports reactive return types: an `@EventListener` method may return `Mono<Void>`, and Spring subscribes to the completion.

The [event](https://github.com/hantsy/spring-reactive-sample/tree/master/event) sample demonstrates the pattern end to end: a publisher component publishes a `Greeting` event, and a listener forwards each event into a Reactor `Sinks.Many` so any part of the application can consume the event stream as a `Flux`.

## Publish events

```java
public record Greeting(String message) {
}

@Component
@RequiredArgsConstructor
public class GreetingPublisher {
    private final ApplicationEventPublisher applicationEventPublisher;

    public void publishGreetingEvent(String message) {
        applicationEventPublisher.publishEvent(new Greeting(message));
    }
}
```

## Listen reactively

```java
@Component
@Slf4j
public class GreetingListener {

    public Sinks.Many<Greeting> sinks = Sinks.many().replay().latest();

    @EventListener
    public Mono<Void> onGreetingEvent(Greeting event) {
        log.debug("received event:{}", event);
        sinks.emitNext(event, Sinks.EmitFailureHandler.FAIL_FAST);
        return Mono.empty();
    }
}
```

The `Sinks.Many` bridges the Spring event mechanism to the reactive world: subscribers of `listener.sinks.asFlux()` receive every published event, and `replay().latest()` lets new subscribers catch up on the most recent event.

## Drive it

```java
public static void main(String[] args) {
    var context = new AnnotationConfigApplicationContext(Application.class);

    var publisher = context.getBean(GreetingPublisher.class);
    var listener = context.getBean(GreetingListener.class);
    listener.sinks.asFlux().subscribe(g -> log.debug("subscribed event: {}", g));

    IntStream.range(1, 100)
            .forEach(i -> {
                publisher.publishGreetingEvent("hello world #" + i + " at " + LocalDateTime.now());
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
}
```

## Test

The test verifies both the Spring event and the sink, using `@RecordApplicationEvents` to capture events and Awaitility to bridge the asynchronous delivery:

```java
@SpringJUnitConfig(classes = TypedGreetingEventTest.TestConfig.class)
@RecordApplicationEvents
public class TypedGreetingEventTest {

    @Autowired
    ApplicationEvents applicationEvents;

    @Autowired
    GreetingPublisher publisher;

    @Autowired
    GreetingListener listener;

    @Test
    public void testGreetingEvents() {
        publisher.publishGreetingEvent("hello world");

        Awaitility.await().atMost(Duration.ofMillis(500))
                .untilAsserted(() ->
                        assertThat(applicationEvents.stream(Greeting.class).count()).isEqualTo(1));

        Awaitility.await().atMost(Duration.ofMillis(1000))
                .untilAsserted(() ->
                        StepVerifier.create(listener.sinks.asFlux())
                                .consumeNextWith(greeting -> assertThat(greeting.message()).isEqualTo("hello world"))
                                .thenCancel()
                                .verify());
    }
}
```

For the complete codes, check [spring-reactive-sample/event](https://github.com/hantsy/spring-reactive-sample/tree/master/event).
