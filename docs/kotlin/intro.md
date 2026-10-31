
# Kotlin

Kotlin is a first-class citizen in the Spring ecosystem. Spring Framework provides dedicated Kotlin DSLs for bean definitions and functional routing (`BeanRegistrarDsl`, `coRouter`), Spring Data supports coroutine-based repositories (`CoroutineCrudRepository`), and the web layer accepts `suspend` functions and `Flow` wherever a `Publisher` is expected.

This chapter covers Kotlin support end to end, based on the runnable samples in this repository and the companion [spring7-sandbox](https://github.com/hantsy/spring7-sandbox) / [spring6-sandbox](https://github.com/hantsy/spring6-sandbox) repositories.

## Chapter roadmap

1. [Annotation-Driven Kotlin and RouterFunctions](kotlin.md) — setup and the two classic programming models in Kotlin.
2. [Coroutines](coroutines.md) — `suspend` controllers, `Flow`, `CoroutineCrudRepository`, `CoWebFilter`, and testing with MockK.
3. [Kotlin DSLs](dsl.md) — the beans DSL, `router { }`/`coRouter { }` routes, and `BeanRegistrarDsl`.
4. [Events, caching, and scheduling](misc.md) — coroutine-friendly events, caching, scheduling, and kotlinx.serialization.

## What's next

- [Annotation-Driven Kotlin and RouterFunctions](kotlin.md) — the Kotlin spelling of the two Java programming models.
- [Coroutines](coroutines.md) — `suspend` controllers, `Flow`, `CoroutineCrudRepository`, `CoWebFilter`, and testing with MockK.
- [Kotlin DSLs](dsl.md) — the beans DSL and `router { }`/`coRouter { }` route DSLs, plus `BeanRegistrarDsl` in Spring Framework 7.
- [Events, Caching, and Scheduling](misc.md) — coroutine-friendly events, caching, scheduling, and kotlinx.serialization.
