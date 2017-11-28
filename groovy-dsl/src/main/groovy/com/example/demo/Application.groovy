package com.example.demo

import org.springframework.context.support.GenericGroovyApplicationContext
import reactor.ipc.netty.NettyContext

class Application {

    static void main(String[] args) {

        GenericGroovyApplicationContext context
        try {
            context = new GenericGroovyApplicationContext("classpath:application.groovy")
            context.getBean(NettyContext.class).onClose().block()
        } finally {
            if (context) {
                context.close()
            }
        }
    }

}

