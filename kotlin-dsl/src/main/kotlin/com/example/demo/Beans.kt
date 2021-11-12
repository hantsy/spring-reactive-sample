package com.example.demo

import com.mongodb.ConnectionString
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer
import org.springframework.context.support.ReloadableResourceBundleMessageSource
import org.springframework.context.support.beans
import org.springframework.core.io.ClassPathResource
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.SimpleReactiveMongoDatabaseFactory
import org.springframework.data.mongodb.repository.support.ReactiveMongoRepositoryFactory
import org.springframework.web.reactive.function.server.HandlerStrategies
import org.springframework.web.reactive.function.server.RouterFunctions
import org.springframework.web.server.WebHandler

fun beans() = beans {

    bean {
        PropertySourcesPlaceholderConfigurer().apply {
            val resources = arrayOf(ClassPathResource("application.properties"))
            setLocations(* resources)
            setIgnoreUnresolvablePlaceholders(true)
        }
    }

    bean {
        PostHandler(ref())
    }

    bean {
        Routes(ref())
    }

    bean<WebHandler>("webHandler") {
        val router = ref<Routes>().router()
        val strategies = HandlerStrategies.builder()
            //.webFilter(ref("springSecurityFilterChain"))
            .build()
        //HandlerStrategies.builder().viewResolver(ref()).build()

        RouterFunctions.toWebHandler(router, strategies)
    }

    bean("messageSource") {
        ReloadableResourceBundleMessageSource().apply {
            setBasename("messages")
            setDefaultEncoding("UTF-8")
        }
    }

    bean {
        DataInitializr(ref())
    }

    bean {
        PostRepository(ref())
    }

    bean { ReactiveMongoRepositoryFactory(ref()) }

    bean {
        ReactiveMongoTemplate(
            SimpleReactiveMongoDatabaseFactory(
                //ConnectionString(env["mongo.uri"])
                ConnectionString("mongodb://localhost:27017/blog")
            )
        )
    }

    profile("foo") {
        bean<Foo>()
    }
}

class Foo {}