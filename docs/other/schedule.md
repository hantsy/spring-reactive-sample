
# Scheduling

Spring's `@Scheduled` supports reactive return types: a scheduled method may return `Mono<Void>` or `Flux<Void>`, and the scheduler subscribes to the completion. The [schedule](https://github.com/hantsy/spring-reactive-sample/tree/master/schedule) sample demonstrates a reactive scheduled task with a plain Spring application.

## Enable scheduling

```java
@Configuration
@EnableAsync
@EnableScheduling
public class ScheduleConfig implements SchedulingConfigurer {

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(taskScheduler());
        taskRegistrar.addFixedDelayTask(
                () -> System.out.println("executing fixed delay task."),
                Duration.ofMillis(1000)
        );
    }

    @Bean(destroyMethod = "shutdown")
    public Executor taskScheduler() {
        return Executors.newScheduledThreadPool(100);
    }
}
```

## A reactive scheduled task

```java
@Component
@Slf4j
public class ReactiveCounter {
    private final AtomicInteger count = new AtomicInteger(0);

    @Scheduled(fixedDelay = 5)
    public Mono<Void> scheduled() {
        return Mono.defer(() -> Mono.just(count.incrementAndGet()))
                .doOnNext(item -> log.debug("current count:{}", item))
                .then();
    }

    public Mono<Integer> getInvocationCount() {
        return Mono.just(this.count.get());
    }
}
```

The `@Scheduled(fixedDelay = 5)` method returns `Mono<Void>`: Spring subscribes on the scheduling thread and waits for completion before scheduling the next execution. `Mono.defer(...)` ensures the counter increments at subscription time, not assembly time.

## Verify

```java
@SpringJUnitConfig(value = ReactiveCounterTest.TestConfig.class)
public class ReactiveCounterTest {

    @Configuration
    @ComponentScan(basePackageClasses = {ScheduleConfig.class})
    static class TestConfig {
    }

    @Autowired
    ReactiveCounter counter;

    @Test
    public void givenSleepBy100ms_whenGetInvocationCount_thenIsGreaterThanZero()
            throws InterruptedException {
        Thread.sleep(100L);
        counter.getInvocationCount()
                .as(StepVerifier::create)
                .consumeNextWith(count -> assertThat(count).isGreaterThan(0))
                .thenCancel()
                .verify();
    }
}
```

For the complete codes, check [spring-reactive-sample/schedule](https://github.com/hantsy/spring-reactive-sample/tree/master/schedule).
