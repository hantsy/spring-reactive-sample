import com.mongodb.ConnectionString
import com.mongodb.reactivestreams.client.MongoClients
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.SimpleReactiveMongoDatabaseFactory
import org.springframework.http.server.reactive.HttpHandler
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.server.adapter.WebHttpHandlerBuilder
import reactor.ipc.netty.NettyContext
import reactor.ipc.netty.http.server.HttpServer

import static org.springframework.web.reactive.function.server.RequestPredicates.DELETE
import static org.springframework.web.reactive.function.server.RequestPredicates.GET
import static org.springframework.web.reactive.function.server.RequestPredicates.POST
import static org.springframework.web.reactive.function.server.RequestPredicates.PUT
import static org.springframework.web.reactive.function.server.RouterFunctions.route

def mongoUri = "mongodb://localhost:27017/blog"
def serverPort = 8080

beans {

    xmlns context:"http://www.springframework.org/schema/context"
    context.'component-scan'('base-package': 'com.example.demo')

    nettyContext(NettyContext) {
        HttpHandler handler = WebHttpHandlerBuilder.applicationContext(context).build()
        ReactorHttpHandlerAdapter adapter = new ReactorHttpHandlerAdapter(handler)
        HttpServer httpServer = HttpServer.create("localhost", serverPort)
        httpServer.newHandler(adapter).block()
    }

    routes(RouterFunction) {
        def postHandler= ref("postHandler")
        route(GET("/posts"), postHandler.&all)
                .andRoute(POST("/posts"), postHandler.&create)
                .andRoute(GET("/posts/{id}"), postHandler.&get)
                .andRoute(PUT("/posts/{id}"), postHandler.&update)
                .andRoute(DELETE("/posts/{id}"), postHandler.&delete)
    }

    postHandler(PostHandler){
        posts = ref("postRepository")
    }

    dataInitializer(DataInitialzer){
        posts = ref("postRepository")
    }

    postRepository(PostRepository){
        template = ref("reactiveMonogTemplate")
    }

    reactiveMonogTemplate(ReactiveMongoTemplate){
        new ReactiveMongoTemplate(new SimpleReactiveMongoDatabaseFactory(new ConnectionString(mongoUri)))
    }


}
