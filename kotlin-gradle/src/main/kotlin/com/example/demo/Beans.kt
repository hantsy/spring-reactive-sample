package com.example.demo

import org.springframework.context.support.ReloadableResourceBundleMessageSource
import org.springframework.context.support.beans
import org.springframework.web.reactive.function.server.HandlerStrategies
import org.springframework.web.reactive.function.server.RouterFunctions
import org.springframework.web.server.WebHandler

fun beans() = beans {

//    bean{
//        PropertySourcesPlaceholderConfigurer().apply {
//            setLocation(ClassPathResource("/application.properties"))
//        }
//    }
    bean {
        PostHandler(it.ref())
    }
    bean {
        Routes(it.ref())
    }
    bean<WebHandler>("webHandler") {
        RouterFunctions.toWebHandler(
                it.ref<Routes>().router(),
                HandlerStrategies.empty().build()
                //HandlerStrategies.builder().viewResolver(it.ref()).build()
        )
    }
    bean("messageSource") {
        ReloadableResourceBundleMessageSource().apply {
            setBasename("messages")
            setDefaultEncoding("UTF-8")
        }
    }
    //    bean {
//        val prefix = "classpath:/templates/"
//        val suffix = ".mustache"
//        val loader = MustacheResourceTemplateLoader(prefix, suffix)
//        MustacheViewResolver(Mustache.compiler().withLoader(loader)).apply {
//            setPrefix(prefix)
//            setSuffix(suffix)
//        }
//    }
    bean {
        PostRepository(it.ref())
    }
    bean {
        DataInitializr(it.ref())
    }
//    bean { ReactiveMongoRepositoryFactory(it.ref()) }
//    bean {
//        ReactiveMongoTemplate(
//                SimpleReactiveMongoDatabaseFactory(
//                //ConnectionString(it.env.getProperty("mongo.uri"))
//                        ConnectionString("mongodb://localhost:27017/blog")
//                )
//        )
//    }

    profile("foo") {
        bean<Foo>()
    }
}

class Foo