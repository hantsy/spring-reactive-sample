package com.example.demo

import org.slf4j.LoggerFactory
import org.springframework.core.io.ClassPathResource
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.router

class Routes(val postHandler: PostHandler) {
    val log = LoggerFactory.getLogger(Routes::class.java);

    fun router() = router {
//        accept(MediaType.TEXT_HTML).nest {
//            GET("/") { ServerResponse.ok().render("index") }
//            GET("/sse") { ServerResponse.ok().render("sse") }
//            GET("/users", postHandler::findAllView)
//        }
        "/api".nest {
            accept(MediaType.APPLICATION_JSON).nest {
                GET("/posts", postHandler::all)
                POST("/posts", postHandler::create)
                GET("/posts/{id}", postHandler::get)
                PUT("/posts/{id}", postHandler::update)
                DELETE("/posts/{id}", postHandler::delete)
            }
            accept(MediaType.TEXT_EVENT_STREAM).nest {
                GET("/posts", postHandler::stream)
            }

        }
        resources("/**", ClassPathResource("static/"))
    }

//            .filter { request, next ->
//        next.handle(request).flatMap {
//
//        }
//    }


}