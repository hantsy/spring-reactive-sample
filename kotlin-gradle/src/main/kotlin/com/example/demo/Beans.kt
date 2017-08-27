package com.example.demo

import com.mongodb.ConnectionString
import org.springframework.beans.factory.config.PropertiesFactoryBean
import org.springframework.context.support.BeanDefinitionDsl
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer
import org.springframework.context.support.ReloadableResourceBundleMessageSource
import org.springframework.context.support.beans
import org.springframework.core.env.MutablePropertySources
import org.springframework.core.env.PropertySource
import org.springframework.core.env.PropertySources
import org.springframework.core.env.PropertySourcesPropertyResolver
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.support.DefaultPropertySourceFactory
import org.springframework.core.io.support.EncodedResource
import org.springframework.core.io.support.PropertySourceFactory
import org.springframework.core.io.support.ResourcePropertySource
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.SimpleReactiveMongoDatabaseFactory
import org.springframework.data.mongodb.repository.support.ReactiveMongoRepositoryFactory
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.UserDetailsRepositoryAuthenticationManager
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.HttpSecurity
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.MapUserDetailsRepository
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsRepository
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.WebFilterChainFilter
import org.springframework.security.web.server.context.WebSessionSecurityContextRepository
import org.springframework.web.reactive.function.server.HandlerStrategies
import org.springframework.web.reactive.function.server.RouterFunctions
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebHandler
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

fun beans() = beans {

    bean<ResourcePropertySource> {
        ResourcePropertySource(EncodedResource(ClassPathResource("application.properties")))
    }

    bean {
        PostHandler(it.ref())
    }

    bean {
        Routes(it.ref())
    }

    bean<WebHandler>("webHandler") {
        RouterFunctions.toWebHandler(
                it.ref<Routes>().router(),
                HandlerStrategies.builder().build()
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
        DataInitializr(it.ref(), it.ref())
    }

    bean {
        PostRepository(it.ref())
    }

    bean { ReactiveMongoRepositoryFactory(it.ref()) }

    bean {
        ReactiveMongoTemplate(
                SimpleReactiveMongoDatabaseFactory(
                        //ConnectionString(it.env.getProperty("mongo.uri"))
                        ConnectionString("mongodb://localhost:27017/blog")
                )
        )
    }

    bean<WebFilter>("springSecurityFilterChain") {
        WebFilterChainFilter(Flux.just(it.ref()))
    }

    bean<SecurityWebFilterChain> {
        it.ref<HttpSecurity>().authorizeExchange()
                .pathMatchers(HttpMethod.GET, "/api/posts/**").permitAll()
                .pathMatchers(HttpMethod.DELETE, "/api/posts/**").hasRole("ADMIN")
                //.pathMatchers("/users/{user}/**").access(this::currentUserMatchesPath)
                .anyExchange().authenticated()
                .and()
                .build()
    }

    bean<HttpSecurity>(scope = BeanDefinitionDsl.Scope.PROTOTYPE) {
        HttpSecurity.http().apply {
            httpBasic()
            authenticationManager(UserDetailsRepositoryAuthenticationManager(it.ref()))
            securityContextRepository(WebSessionSecurityContextRepository())
        }
    }

    bean {
        UserDetailsRepository { username -> it.ref<UserRepository>()
                .findByUsername(username)
                .map { (_, username, password, active, roles) ->
                    org.springframework.security.core.userdetails.User
                            .withUsername(username)
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

    bean {
//        val user = User.withUsername("user").password("test123").roles("USER").build()
//        val admin = User.withUsername("admin").password("test123").roles("USER", "ADMIN").build()
//        MapUserDetailsRepository(user, admin)
        UserRepository(it.ref())
    }

    profile("foo") {
        bean<Foo>()
    }
}

class Foo

//@EnableReactiveMongoRepositories
//@EnableMongoAuditing
//class DataConfig(val environment: Environment) : AbstractReactiveMongoConfiguration() {
//
//    @Bean
//    fun mongoEventListener(): LoggingEventListener {
//        return LoggingEventListener()
//    }
//
//    @Bean
//    override fun mongoClient() = MongoClients.create(ConnectionString(environment.getProperty("mongo.uri")))
//
//    @Bean
//    override fun getDatabaseName(): String = "blog"
//}

//@EnableWebFluxSecurity
//class SecurityConfig {
//    @Bean
//    @Throws(Exception::class)
//    fun springWebFilterChain(http: HttpSecurity): SecurityWebFilterChain {
//        return http
//                .authorizeExchange()
//                .pathMatchers(HttpMethod.GET, "/posts/**").permitAll()
//                .pathMatchers(HttpMethod.DELETE, "/posts/**").hasRole("ADMIN")
//                //.pathMatchers("/users/{user}/**").access(this::currentUserMatchesPath)
//                .anyExchange().authenticated()
//                .and()
//                .build()
//    }
//
//    private fun currentUserMatchesPath(authentication: Mono<Authentication>, context: AuthorizationContext): Mono<AuthorizationDecision> {
//        return authentication
//                .map { context.variables?.get("user")?.equals(it.name) }
//                .map { AuthorizationDecision(it ?: false) }
//    }
//
//    @Bean
//    fun userDetailsRepository(): MapUserDetailsRepository {
//        val rob = User.withUsername("test").password("test123").roles("USER").build()
//        val admin = User.withUsername("admin").password("admin123").roles("USER", "ADMIN").build()
//        return MapUserDetailsRepository(rob, admin)
//    }
//}