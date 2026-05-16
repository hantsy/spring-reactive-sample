package com.example.demo

import com.mongodb.ConnectionString
import org.springframework.beans.factory.BeanRegistrarDsl
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer
import org.springframework.context.support.ReloadableResourceBundleMessageSource
import org.springframework.core.io.ClassPathResource
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.SimpleReactiveMongoDatabaseFactory
import org.springframework.data.mongodb.repository.support.ReactiveMongoRepositoryFactory
import org.springframework.web.reactive.function.server.HandlerStrategies
import org.springframework.web.reactive.function.server.RouterFunctions
import org.springframework.web.server.WebHandler

fun beans() = BeanRegistrarDsl {

    registerBean {
        PropertySourcesPlaceholderConfigurer().apply {
            val resources = arrayOf(ClassPathResource("application.properties"))
            setLocations(*resources)
            setIgnoreUnresolvablePlaceholders(true)
        }
    }

    registerBean {
        PostHandler(bean())
    }

    registerBean {
        Routes(bean())
    }

    registerBean<WebHandler>("webHandler") {
        val router = bean<Routes>().router()
        val strategies = HandlerStrategies.builder()
            //.webFilter(ref("springSecurityFilterChain"))
            .build()
        //HandlerStrategies.builder().viewResolver(ref()).build()

        RouterFunctions.toWebHandler(router, strategies)
    }

    registerBean("messageSource") {
        ReloadableResourceBundleMessageSource().apply {
            setBasename("messages")
            setDefaultEncoding("UTF-8")
        }
    }

    registerBean {
        DataInitializr(bean())
    }

    registerBean {
        PostRepository(bean())
    }

    registerBean { ReactiveMongoRepositoryFactory(bean()) }

    registerBean {
        ReactiveMongoTemplate(
            SimpleReactiveMongoDatabaseFactory(
                //ConnectionString(env["mongo.uri"])
                ConnectionString("mongodb://localhost:27017/blog")
            )
        )
    }

    profile("foo") {
        registerBean<Foo>()
    }
}

class Foo {}