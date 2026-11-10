package com.example.demo

import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.BeanRegistrarDsl
import org.springframework.boot.CommandLineRunner

class PostBeanRegistrar : BeanRegistrarDsl({
    registerBean { InMemoryPostRepository() }
    registerBean { PostHandler(bean()) }
    registerBean { Routes(bean()) }
    registerBean { bean<Routes>().routes() }
    registerBean {
        CommandLineRunner {
            runBlocking { bean<PostRepository>().initializeSampleData() }
        }
    }
    register(FoobarRegistrar())
})

class FoobarRegistrar : BeanRegistrarDsl({
    profile("foo") {
        registerBean<Bar> { Bar(bean<Foo>()) }
        registerBean { Foo() }
    }
})

class Bar(foo: Foo)

class Foo