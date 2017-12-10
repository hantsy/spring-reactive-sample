package com.example.demo

import org.slf4j.LoggerFactory
import org.springframework.core.io.ClassPathResource
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.router
import reactor.core.publisher.toMono

class Routes(val postHandler: PostHandler) {
    val log = LoggerFactory.getLogger(Routes::class.java);

    fun router() = router {
        accept(MediaType.TEXT_HTML).nest {
            GET("/") { ServerResponse.ok().render("index") }
            GET("/sse") { ServerResponse.ok().render("sse") }
            //GET("/users", postHandler::findAllView)
        }
        "/api".nest {
            accept(MediaType.APPLICATION_JSON).nest {
                GET("/posts", postHandler::all)
                GET("/posts/{id}", postHandler::get)
            }
            accept(MediaType.TEXT_EVENT_STREAM).nest {
                GET("/posts", postHandler::stream)
            }
            POST("/posts", postHandler::create)
            PUT("/posts/{id}", postHandler::update)
            DELETE("/posts/{id}", postHandler::delete)

        }
        resources("/**", ClassPathResource("static/"))
    }


//            .filter { request, next ->
//        next.handle(request).flatMap {
//            //if (it is RenderingResponse) RenderingResponse.from(it).modelAttributes(attributes(request.locale(), messageSource)).build() else it.toMono()
//        }
//    }

//    private fun attributes(locale: Locale, messageSource: MessageSource) = mutableMapOf<String, Any>(
//            "i18n" to Mustache.Lambda { frag, out ->
//                val tokens = frag.execute().split("|")
//                out.write(messageSource.getMessage(tokens[0], tokens.slice(IntRange(1, tokens.size - 1)).toTypedArray(), locale)) }
//    )


}