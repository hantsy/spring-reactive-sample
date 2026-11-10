package com.example.demo

import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.web.reactive.function.server.coRouter

class Routes(val postHandler: PostHandler) {
    fun routes() = coRouter {
        "/posts".nest {
            "{id}".nest {
                GET(accept(APPLICATION_JSON), postHandler::get)
                PUT(contentType(APPLICATION_JSON), postHandler::update)
                DELETE(accept(APPLICATION_JSON), postHandler::delete)
            }
            GET(accept(APPLICATION_JSON), postHandler::all)
            POST(contentType(APPLICATION_JSON), postHandler::create)
        }
    }
}