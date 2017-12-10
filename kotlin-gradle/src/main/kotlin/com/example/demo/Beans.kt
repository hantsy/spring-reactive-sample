package com.example.demo

import com.example.demo.web.PostHandler
import com.example.demo.web.Routes
import com.example.demo.web.view.MustacheResourceTemplateLoader
import com.example.demo.web.view.MustacheViewResolver
import com.mongodb.ConnectionString
import org.springframework.context.support.BeanDefinitionDsl
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer
import org.springframework.context.support.ReloadableResourceBundleMessageSource
import org.springframework.context.support.beans
import org.springframework.core.io.ClassPathResource
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.SimpleReactiveMongoDatabaseFactory
import org.springframework.data.mongodb.repository.support.ReactiveMongoRepositoryFactory
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.WebFilterChainProxy
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository
import org.springframework.web.reactive.function.server.HandlerStrategies
import org.springframework.web.reactive.function.server.RouterFunctions
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebHandler
import java.util.*
import com.samskivert.mustache.Mustache

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
        RouterFunctions.toWebHandler(
                ref<Routes>().router(),
                HandlerStrategies.builder()
                        .webFilter(ref("springSecurityFilterChain"))
                        .build()
                //HandlerStrategies.builder().viewResolver(ref()).build()
        )
    }

    bean("messageSource") {
        ReloadableResourceBundleMessageSource().apply {
            setBasename("messages")
            setDefaultEncoding("UTF-8")
        }
    }
        bean {
        val prefix = "classpath:/templates/"
        val suffix = ".mustache"
        val loader = MustacheResourceTemplateLoader(prefix, suffix)
        MustacheViewResolver(Mustache.compiler().withLoader(loader)).apply {
            setPrefix(prefix)
            setSuffix(suffix)
        }
    }

    bean {
        DataInitializr(ref(), ref())
    }

    bean {
        UserRepository(ref())
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

    bean<WebFilter>("springSecurityFilterChain") {
        WebFilterChainProxy(Arrays.asList(ref()))
    }

    bean<SecurityWebFilterChain> {
        ref<ServerHttpSecurity>().authorizeExchange()
                .pathMatchers(HttpMethod.GET, "/api/posts/**").permitAll()
                .pathMatchers(HttpMethod.DELETE, "/api/posts/**").hasRole("ADMIN")
                //.pathMatchers("/users/{user}/**").access(this::currentUserMatchesPath)
                .anyExchange().authenticated()
                .and()
                .build()
    }

    bean<ServerHttpSecurity>(scope = BeanDefinitionDsl.Scope.PROTOTYPE) {
        ServerHttpSecurity.http().apply {
            httpBasic()
            authenticationManager(UserDetailsRepositoryReactiveAuthenticationManager(ref()))
            securityContextRepository(WebSessionServerSecurityContextRepository())
        }
    }

    bean {
        ReactiveUserDetailsService { username ->
            ref<UserRepository>()
                    .findByUsername(username)
                    .map { (_, username, password, active, roles) ->
                        org.springframework.security.core.userdetails.User
                                .withDefaultPasswordEncoder()
                                .username(username)
                                .password(password)
                                .accountExpired(!active)
                                .accountLocked(!active)
                                .credentialsExpired(!active)
                                .disabled(!active)
                                .authorities(roles.map(::SimpleGrantedAuthority).toList())
                                .build()
                    }
                    .cast(UserDetails::class.java)
        }
    }

    profile("foo") {
        bean<Foo>()
    }
}

class Foo
