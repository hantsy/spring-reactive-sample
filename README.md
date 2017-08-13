# Spring Reactive Sample

[toc]

The upcoming Spring 5 embraces [Reactive Streams](http://www.reactive-streams.org/). From the offcial website of [Reactive Streams](http://www.reactive-streams.org/):

>Reactive Streams is an initiative to provide a standard for asynchronous stream processing with non-blocking back pressure.This encompasses efforts aimed at runtime environments (JVM and JavaScript) as well as network protocols.

Currently, the JVM specification is completed, it includes a Java API(four simple interface), a textual Specification, a TCK and implementation examples. Check [Reactive Streams for JVM](https://github.com/reactive-streams/reactive-streams-jvm#reactive-streams) for more details.


Reactor and RxJava2 implement this specification, and it is also adopted in Java 9 by the new Flow API. 

For Spring developers, it brings a complete new programming model. In this post, we will try to cover all reactive features in the Spring projects.

* Spring core framework added a new `spring-webflux` module, and provided built-in reactive programming support via Reactor and RxJava. 
* Spring Security 5 also added reactive feature. 
* In Spring Data umbrella projects, a new `ReactiveSortingRepository` interface is added in Spring Data Commons. Redis, Mongo, Cassandra subprojects firstly got reactive supports. Unluckily due to the original JDBC is desginated for blocking access, Spring Data JPA can not benefit from this feature. 
* Spring Session also began to add reactive features, an reactive variant for its `SessionRepository` is included in the latest 2.0.0.M3. 

**NOTE: At the moment I am writing this post, some projects are still under active development, I will update the content and the sample codes according to the final release version. Please start [Github sample repository](https://github.com/hantsy/spring-reactive-sample) to track it.**
 
## Create a Webflux application

An example exceeds thousands of words. Let's begin to write some codes and enjoy the reactive programming brought by Spring 5.

Generally, I would like reuse the same concept in my former [Spring Boot sample codes](https://github.com/hantsy/angularjs-springmvc-sample-boot) which is a simple blog application. 

In the following steps we will start with creating RESTful APIs for Post. 


### Prerequisites

Make sure you have installed:

* Java 8, https://java.oracle.com
* Apache Maven, https://maven.apache.org
* Your favorite IDE, including NetBeans IDE, Eclipse, Intellij IDEA.

**NOTE**: Do not forget to add your Java and Maven command into your system environment variable **PATH** .

### Prepare project skeleton

Execute the following command to create a general web application from Maven archetype. 

```
$ mvn archetype:generate -DgroupId=com.example
	-DartifactId=demo
	-DarchetypeArtifactId=maven-archetype-webapp
	-DinteractiveMode=false
```
You can import the generated codes into your IDEs for further development.

Open *pom.xml* in your editor, add some modifications:

1. Add `spring-boot-starter-parent` as parent POM to manage the versions of all required dependencies for this project.
2. Add `spring-webflux`, `jackson-databind`, `reactor-core` as dependencies for Spring Web Reactive support
3. Add `logback` as logging framework, `jcl-over-slf4j` is a bridge for Spring jcl and slf4j.
4. Use Lombok to erase the tedious getters, setters, etc for a simple POJO class, check the [Lombok project](http://projectlombok.org) to get more information if you have Lombok trouble in your IDEs.
5. You have to add spring milestone repositories in `repositories` and `pluginRepositories`, because at the moment, they are still in active development, and not availble in the official Maven public repository.

The final pom.xml looks like:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.example</groupId>
    <artifactId>spring-reactive-sample-vanilla</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <packaging>jar</packaging>

    <name>spring-reactive-sample-vanilla</name>
    <description>Spring Webflux demo(without Spring Boot)</description>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.0.0.M3</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
        <java.version>1.8</java.version>
    </properties>

    <dependencies>     
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-context</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-webflux</artifactId>
        </dependency>
        
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
        </dependency>
        
        <dependency>
            <groupId>io.netty</groupId>
            <artifactId>netty-buffer</artifactId>
        </dependency>
        
        <dependency>
            <groupId>io.projectreactor.ipc</groupId>
            <artifactId>reactor-core</artifactId>
        </dependency>
        
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
        </dependency>
        
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-api</artifactId>
        </dependency>
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>jcl-over-slf4j</artifactId>
        </dependency>
        <dependency>
            <groupId>ch.qos.logback</groupId>
            <artifactId>logback-core</artifactId>
        </dependency>
        <dependency>
            <groupId>ch.qos.logback</groupId>
            <artifactId>logback-classic</artifactId>
        </dependency>
        
    </dependencies>

    <repositories>
        <repository>
            <id>spring-snapshots</id>
            <name>Spring Snapshots</name>
            <url>https://repo.spring.io/snapshot</url>
            <snapshots>
                <enabled>true</enabled>
            </snapshots>
        </repository>
        <repository>
            <id>spring-milestones</id>
            <name>Spring Milestones</name>
            <url>https://repo.spring.io/milestone</url>
            <snapshots>
                <enabled>false</enabled>
            </snapshots>
        </repository>
    </repositories>

    <pluginRepositories>
        <pluginRepository>
            <id>spring-snapshots</id>
            <name>Spring Snapshots</name>
            <url>https://repo.spring.io/snapshot</url>
            <snapshots>
                <enabled>true</enabled>
            </snapshots>
        </pluginRepository>
        <pluginRepository>
            <id>spring-milestones</id>
            <name>Spring Milestones</name>
            <url>https://repo.spring.io/milestone</url>
            <snapshots>
                <enabled>false</enabled>
            </snapshots>
        </pluginRepository>
    </pluginRepositories>

</project>
```

### Getting started

The project skeleton is ready, now let's add some codes to play reactive programming.

Create a new class named `Post`, it includes three fields: id, title, content.

```java
@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
class Post {
    
    private Long id;
    private String title;
    private String content;
    
}
```

`@Data`, `@ToString`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor` are from the Lombok project.

When you compile `Post`, it will utilize Java compiler built-in Annotation Processing tooling to add extra facilities into the final compiled classes, including:

1. Getters and setters of the three fields, and overrides `equals` and `hashCode` methods.
2. Overrides `toString` method.
3. A builder class for creating the Post instance more easily.
4. A constructor with no arguments.
5. A constructor with all fields as arguments.

Create a dummy repository named `PostRepository` to retrieve posts from and save them back to a repository.

```java
@Component
class PostRepository {

    private static final Map<Long, Post> DATA = new HashMap<>();
    private static long ID_COUNTER = 1L;

    static {
        Arrays.asList("First Post", "Second Post")
            .stream()
            .forEach((java.lang.String title) -> {
                long id = ID_COUNTER++;
                DATA.put(Long.valueOf(id), Post.builder().id(id).title(title).content("content of " + title).build());
            }
            );
    }

    Flux<Post> findAll() {
        return Flux.fromIterable(DATA.values());
    }

    Mono<Post> findById(Long id) {
        return Mono.just(DATA.get(id));
    }

    Mono<Post> createPost(Post post) {
        long id = ID_COUNTER++;
        post.setId(id);
        DATA.put(id, post);
        return Mono.just(post);
    }

}
```

Currently we do not connect to any database, use a `Map` backed data store instead. When we talk about the Spring Data reactive feature later, we will replace it with a real Spring Data reactive implementation.

If you have used Spring Data before, you will find these APIs are every similiar with `Repository` interface provided in Spring Data. 

The main difference is in the current Repository class all methods return a `Flux` or `Mono` instead.

`Flux` and `Mono` are from Reactor, which powers the reactive support in Spring 5 by default. 

* `Flux` means it could return lots of results in the stream. 
* `Mono` means it could return 0 to 1 result. 

Create a controller class named `PostController` to expose RESTful PAIs for `Post`.

```java
@RestController
@RequestMapping(value = "/posts")
class PostController {
    
    private final PostRepository posts;

    public PostController(PostRepository posts) {
        this.posts = posts;
    }

    @GetMapping(value = "")
    public Flux<Post> all() {
        return this.posts.findAll();
    }

    @GetMapping(value = "/{id}")
    public Mono<Post> get(@PathVariable(value = "id") Long id) {
        return this.posts.findById(id);
    }
    
    @PostMapping(value = "")
    public Mono<Post> create(Post post) {
        return this.posts.createPost(post);
    }
   
}
```

Create a `@configuration` class, add an `@EnableWebFlux` annotation to activiate webflux in this application.

```java
@Configuration
@ComponentScan
@EnableWebFlux
class WebConfig {
    
}
``` 

Now we almost have done the programming work, let's try to bootstrap the application.

### Bootstrap

Create a general main class to run the application programticially. 

```java
ApplicationContext context = new AnnotationConfigApplicationContext(WebConfig.class, SecurityConfig.class);  

HttpHandler handler = DispatcherHandler.toHttpHandler(context);  

// Tomcat and Jetty (also see notes below)
ServletHttpHandlerAdapter servlet = new ServletHttpHandlerAdapter(handler); 

Tomcat tomcatServer = new Tomcat();
tomcatServer.setHostname(DEFAULT_HOST);
tomcatServer.setPort(DEFAULT_PORT);
Context rootContext = tomcatServer.addContext("", System.getProperty("java.io.tmpdir"));
Tomcat.addServlet(rootContext, "httpHandlerServlet", servlet);
rootContext.addServletMapping("/", "httpHandlerServlet");
tomcatServer.start();
```

The above codes perform some tasks.

1. Create a `HttpHandler` from `ApplicationContext`.
2. Use `ServletHttpHandlerAdapter` to bridge the Servlet APIs to reactive based `HttpHandler`.
3. Start tomcat server. 

Do not forget add the `tomcat-embed-core` to project dependencies.

```xml
 <dependency>
	<groupId>org.apache.tomcat.embed</groupId>
	<artifactId>tomcat-embed-core</artifactId>
</dependency>
```

You can simply run this class in IDEs as others Java application projects. 

If you want to package all dependencies into one jar and run the application in one line command `java -jar filename`, maven-assembly-plugin can help this purpose.

Add `maven-assembly-plugin` configuration into the pom.xml file.

```xml
<!-- Maven Assembly Plugin -->
<plugin>
	<groupId>org.apache.maven.plugins</groupId>
	<artifactId>maven-assembly-plugin</artifactId>
	<configuration>
		<descriptorRefs>
			<descriptorRef>jar-with-dependencies</descriptorRef>
		</descriptorRefs>
		<!-- MainClass in mainfest make a executable jar -->
		<archive>
			<manifest>
				<mainClass>com.example.demo.App</mainClass>
			</manifest>
		</archive>

	</configuration>
	<executions>
		<execution>
			<id>make-assembly</id>
			<phase>package</phase> <!-- bind to the packaging phase -->
			<goals>
				<goal>single</goal>
			</goals>
		</execution>
	</executions>
</plugin>
```

Eneter the project root folder, execute the following command:

```
mvn package
```

When it is done, switch to the *target* folder, besides the general jar, you will find an extra fat jar was generated, which filename is ended with **jar-with-dependencies.jar**.

```
spring-reactive-sample-vanilla-0.0.1-SNAPSHOT-jar-with-dependencies.jar
spring-reactive-sample-vanilla-0.0.1-SNAPSHOT.jar
```  

Run the following command to run this application. 

```
java -jar target/XXXX-jar-with-dependencies.jar 
```

When it is started, try to fetch posts.

```
#curl http://localhost:8080/posts
[{"id":1,"title":"First Post","content":"content of First Post"},{"id":2,"title":"Second Post","content":"content of Second Post"}]
```

Alternatively, you can run the application in Reactor Netty, or JBoss Undertow.

For Reactor Netty, replace the above tomcat bootstraping codes with:

```java
ReactorHttpHandlerAdapter adapter = new ReactorHttpHandlerAdapter(handler);
HttpServer.create(DEFAULT_HOST, DEFAULT_PORT).newHandler(adapter).block();
```

And add `reactor-netty` in your project dependencies.

```xml
<dependency>
	<groupId>io.projectreactor.ipc</groupId>
	<artifactId>reactor-netty</artifactId>
</dependency>
```

For Undertow, replace the above tomcat bootstraping codes with:

```java
UndertowHttpHandlerAdapter undertowAdapter = new UndertowHttpHandlerAdapter(handler);
Undertow server = Undertow.builder().addHttpListener(DEFAULT_PORT, DEFAULT_HOST).setHandler(undertowAdapter).build();
server.start();
```

And add `undertow-core` in your project dependencies.

```xml
<dependency>
	<groupId>io.undertow</groupId>
	<artifactId>undertow-core</artifactId>
</dependency>
```

#### Standalone Servlet Container

If you are stick on traditional web applications, and want to package it into a **war** file and deploy it into an existing servlet container, Spring 5 provides a `AbstractAnnotationConfigDispatcherHandlerInitializer` to fill the gap. It is a standard Spring `ApplicationInitializer` implementation which can be recoginised by Spring when servlet container starts up.

Replace the above bootstraping class with:

```java
public class AppIntializer extends AbstractAnnotationConfigDispatcherHandlerInitializer {

    @Override
    protected Class<?>[] getConfigClasses() {
        return new Class[]{
            WebConfig.class,
            SecurityConfig.class
        };
    }
}
```

And change the project packaging from **jar** to **war** in pom.xml.

```xml
<packaging>war</packaging>
```

Now you can run this application on a IDE managed Servlet 3.1 Container directly. 

Or package the project into a **war** format and deploy it into a servlet 3.1 based container(tomcat, jetty) manually.

Alternatively, if you want to run this application via `mvn` command in the development stage. `cargo-maven2-plugin` can archive this purpose.

```xml
<plugin> 
	<groupId>org.apache.maven.plugins</groupId>
	<artifactId>maven-war-plugin</artifactId>
	<configuration>
		<failOnMissingWebXml>false</failOnMissingWebXml>
	</configuration>
</plugin>
<plugin>
	<groupId>org.codehaus.cargo</groupId>
	<artifactId>cargo-maven2-plugin</artifactId>
	<configuration>
		<container>
			<containerId>tomcat8x</containerId>
			<type>embedded</type>
		</container>

		<configuration>
			<properties>
				<cargo.servlet.port>9000</cargo.servlet.port>
				<cargo.logging>high</cargo.logging>
			</properties>
		</configuration>
	</configuration>
</plugin>
```			

Run the following command to package and deploy it into an embedded tomcat controlled by **cargo**.

```
mvn verify cargo:run
```

## Spring Boot

Currently Spring Boot 2.0 is still in active development. The final Spring Boot 2.0 will target the latest Spring technology stack, including Spring 5, Spring Security 5, Spring Session 2 etc. 

Open browser and navigate to [http://start.spring.io](http://start.spring.io). 

![Spring Boot initializer](https://github.com/hantsy/spring-reactive-sample/blob/master/init.png)

In the Spring Boot Initializer page. 

1. Select Spring Boot version as 2.0.0.M3 or 2.0.0.SNAPSHOT. 
2. In the dependencies box, type **reactive**, it will display all reactive options in a dropdown menu. Select **Ractive Web** to add `spring-webflux` into project dependencies. You can also add other items as you like, such as **Reactive MongoDb**, **Reactive Redis** etc.
3. Click **Generate project** button or hint **ALT+NETER** keys to generate a project skeleton as a zip file for downloading.

Download and extract it into your disc, import the source codes into your IDEs.

A new `spring-boot-starter-webflux` starter is added in the pom.xml.

```xml
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
```

In a Spring Boot application, `spring-boot-starter-webflux` will handle the dependencies  and enable webflux automatically. 

Reuse the former codes,

1. Remove `WebConfig` class.
2. Replace the Bootstrap class with the Spring Boot's `@SpringBootApplication` annotated class as the application entry.

```java
@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
```

By default, Spring Boot will use React Netty to a webflux application. No need extra configuration for it.

Run the following command to run this application.

```
mvn spring-boot:run
```

## Spring Security for Webflux
 
Reflect to new webflux feature introduced in Spring 5, Spring Security 5 added new APIs to handle Reactive Web security.

Similar with Spring 5, Spring Security 5 added a new module named `spring-secuirty-webflux`.

Add it in the project dependencies aside with `spring-boot-starter-security`.

```xml
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
	<groupId>org.springframework.security</groupId>
	<artifactId>spring-security-webflux</artifactId>
</dependency>
```

**NOTE**: Currently you have to add `spring-security-webflux` explicitly, there is no specific starters for `spring-security-webflux`.

Add `@EnableWebFluxSecurity` to a configuration class to enable Spring security for Webflux.

```java
@EnableWebFluxSecurity
class SecurityConfig {

	@Bean
	SecurityWebFilterChain springWebFilterChain(HttpSecurity http) throws Exception {
		return http
			.authorizeExchange()
				.pathMatchers(HttpMethod.GET, "/posts/**").permitAll()
                .pathMatchers(HttpMethod.DELETE, "/posts/**").hasRole("ADMIN")
				//.pathMatchers("/users/{user}/**").access(this::currentUserMatchesPath)
				.anyExchange().authenticated()
				.and()
			.build();
	}

	private Mono<AuthorizationDecision> currentUserMatchesPath(Mono<Authentication> authentication, AuthorizationContext context) {
		return authentication
			.map( a -> context.getVariables().get("user").equals(a.getName()))
			.map( granted -> new AuthorizationDecision(granted));
	}

	@Bean
	public MapUserDetailsRepository userDetailsRepository() {
		UserDetails rob = User.withUsername("test").password("test123").roles("USER").build();
		UserDetails admin = User.withUsername("admin").password("admin123").roles("USER","ADMIN").build();
		return new MapUserDetailsRepository(rob, admin);
	}

}
```

1. `HttpSecurity` is from `spring-secuirty-webflux`, similar with the general version, but handle `WebExhange` instead of Servlet based `WebRequest`.
2. A new `UserDetailsRepository` interface is introduced which is aligned with Reactor APIs. By default, an in-memory `Map` based implementation `MapUserDetailsRepository` is provided, you can customsize yourself by implementing the `UserDetailsRepository` interface.

Start the application by:

```
mvn spring-boot:run
```

Try to add a new post without authentication:

```
#curl -v  -X POST http://localhost:8080/posts -H "Content-Type:application/json" -d "{\"title\":\"My Post\",\"content\":\"content of My Post\"}"
Note: Unnecessary use of -X or --request, POST is already inferred.
* timeout on name lookup is not supported
*   Trying ::1...
* TCP_NODELAY set
* Connected to localhost (::1) port 8080 (#0)
> POST /posts HTTP/1.1
> Host: localhost:8080
> User-Agent: curl/7.54.1
> Accept: */*
> Content-Type:application/json
> Content-Length: 42
>
* upload completely sent off: 42 out of 42 bytes
< HTTP/1.1 401 Unauthorized
< WWW-Authenticate: Basic realm="Realm"
< Cache-Control: no-cache, no-store, max-age=0, must-revalidate
< Pragma: no-cache
< Expires: 0
< X-Content-Type-Options: nosniff
< X-Frame-Options: DENY
< X-XSS-Protection: 1 ; mode=block
< content-length: 0
<
* Connection #0 to host localhost left intact
```

The server rejected the client request, and send back a 401 error(401 Unauthorized).

Use predefined **test:test123** credentials to get authentication and send post request again.

```
curl -v  -X POST http://localhost:8080/posts -u "test:test123" -H "Content-Type:application/json" -d "{\"title\":\"My Post\",\"content\":\"content of My Post\"}"
Note: Unnecessary use of -X or --request, POST is already inferred.
* timeout on name lookup is not supported
*   Trying ::1...
* TCP_NODELAY set
* Connected to localhost (::1) port 8080 (#0)
* Server auth using Basic with user 'test'
> POST /posts HTTP/1.1
> Host: localhost:8080
> Authorization: Basic dGVzdDp0ZXN0MTIz
> User-Agent: curl/7.54.1
> Accept: */*
> Content-Type:application/json
> Content-Length: 50
>
* upload completely sent off: 50 out of 50 bytes
< HTTP/1.1 200 OK
< transfer-encoding: chunked
< Content-Type: application/json;charset=UTF-8
< Cache-Control: no-cache, no-store, max-age=0, must-revalidate
< Pragma: no-cache
< Expires: 0
< X-Content-Type-Options: nosniff
< X-Frame-Options: DENY
< X-XSS-Protection: 1 ; mode=block
< set-cookie: SESSION=b99124f7-c0a0-4507-b9be-34718af3d137; HTTPOnly
<
{"id":"59906f9d3c44060e044fb378","title":"My Post","content":"content of My Post","createdDate":[2017,8,13,23,26,21,392000000]}* Connection #0 to host localhost left intact
```

It is done secussfully, and return the new created post.





































## References

* [Reactive Streams](http://www.reactive-streams.org/), official Reactive Streams website
* [Understanding Reactive types](https://spring.io/blog/2016/04/19/understanding-reactive-types), Spring.IO
* [The WebFlux framework](http://docs.spring.io/spring-framework/docs/5.0.x/spring-framework-reference/web.html#web-reactive), Spring Framework Reference Documentation
* [Reactor Core 3.0 becomes a unified Reactive Foundation on Java 8](https://spring.io/blog/2016/03/11/reactor-core-3-0-becomes-a-unified-reactive-foundation-on-java-8), Spring.IO
* [Reactive Spring](https://spring.io/blog/2016/02/09/reactive-spring), Spring.IO
* Three parts of **Notes on Reactive Programming** by Dave Syer:

   * [Notes on Reactive Programming Part I: The Reactive Landscape](https://spring.io/blog/2016/06/07/notes-on-reactive-programming-part-i-the-reactive-landscape)
   * [Notes on Reactive Programming Part II: Writing Some Code](https://spring.io/blog/2016/06/13/notes-on-reactive-programming-part-ii-writing-some-code)
   * [Notes on Reactive Programming Part III: A Simple HTTP Server Application](https://spring.io/blog/2016/07/20/notes-on-reactive-programming-part-iii-a-simple-http-server-application)

* [Reactive Programming in the Netflix API with RxJava](https://medium.com/netflix-techblog/reactive-programming-in-the-netflix-api-with-rxjava-7811c3a1496a)
* [Reactor by Example](https://www.infoq.com/articles/reactor-by-example)
* [New in Spring 5: Functional Web Framework](https://spring.io/blog/2016/09/22/new-in-spring-5-functional-web-framework)
* [Spring WebFlux: First Steps ](https://dzone.com/articles/spring-webflux-first-steps)
* [Spring-Reactive Example REST Application ](https://dzone.com/articles/spring-reactive-samples)
* [Spring 5 WebFlux and JDBC: To Block or Not to Block ](https://dzone.com/articles/spring-5-webflux-and-jdbc-to-block-or-not-to-block)
* [Reactive Spring 5 and Application Design Impact](https://dzone.com/articles/reactive-spring-5-and-application-design-impact)
