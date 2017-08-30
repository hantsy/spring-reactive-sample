# Reactive programming with Spring 5

[TOC]

From the official website of [Reactive Streams](http://www.reactive-streams.org/):

>Reactive Streams is an initiative to provide a standard for asynchronous stream processing with non-blocking back pressure.This encompasses efforts aimed at runtime environments (JVM and JavaScript) as well as network protocols.

Currently, the JVM specification is completed, it includes a Java API(four simple interface), a textual Specification, a TCK and implementation examples. Check [Reactive Streams for JVM](https://github.com/reactive-streams/reactive-streams-jvm#reactive-streams) for more details.

Reactor and RxJava2 have implemented this specification, and the upcoming Java 9 also adopted in its new Flow API. 

The upcoming Spring 5 embraces [Reactive Streams](http://www.reactive-streams.org/). For Spring developers, it brings a complete new programming model. In this post, we will try to cover all reactive features in the Spring projects.

* Spring core framework added a new `spring-webflux` module, and provided built-in reactive programming support via Reactor and RxJava. 
* Spring Security 5 also added reactive feature. 
* In Spring Data umbrella projects, a new `ReactiveSortingRepository` interface is added in Spring Data Commons. Redis, Mongo, Cassandra subprojects firstly got reactive supports. Unluckily due to the original JDBC is desginated for blocking access, Spring Data JPA can not benefit from this feature. 
* Spring Session also began to add reactive features, an reactive variant for its `SessionRepository` is included in the latest 2.0.0.M3. 

**NOTE: At the moment I was writing this post, some Spring projects are still under active development, I will update the content and sample codes against the final release version when it is released. Please start [Github sample repository](https://github.com/hantsy/spring-reactive-sample) to track and get update for it.**

## Create a Webflux application

An example exceeds thousands of words. Let's begin to write some codes and enjoy the reactive programming brought by Spring 5.

As an example, I will reuse the same concept in my former [Spring Boot sample codes](https://github.com/hantsy/angularjs-springmvc-sample-boot) which is a simple blog application. 

In the following steps we will start with creating RESTful APIs for `Post`. 


### Prerequisites

Before writing some real codes, make sure you have installed the essential software:

* Oracle Java 8, https://java.oracle.com
* Apache Maven, https://maven.apache.org
* Gradle, [http://www.gradle.org](http://www.gradle.org)
* Your favorite IDE, including :
  * NetBeans IDE
  * Eclipse IDE (or base on  Eclipse, eg. Spring ToolSuite is highly recommended) 
  * Intellij IDEA
  * etc

**NOTE**: Do not forget to add path which includes `java` and `mvn` command into your system environment variable **PATH** .

### Generate project skeleton

Execute the following command to create a general web application from Maven archetype. 

```
$ mvn archetype:generate -DgroupId=com.example
	-DartifactId=demo
	-DarchetypeArtifactId=maven-archetype-webapp
	-DinteractiveMode=false
```
You can import the generated codes into your IDEs for further development.

Open *pom.xml* in your IDE editor, add some modifications:

1. Add `spring-boot-starter-parent` as parent POM to manage the versions of all required dependencies for this project.
2. Add `spring-webflux`, `jackson-databind`, `reactor-core` as dependencies to get Spring Web Reactive support
3. Add `logback` as logging framework, `jcl-over-slf4j` is a bridge for Spring jcl and slf4j.
4. Add Lombok to erase the tedious getters, setters, etc for a simple POJO class, check the [Lombok project](http://projectlombok.org) to get more information about Lombok, follow the official installation guide to get Lombok support in your IDE.
5. You have to add spring milestone repositories in `repositories` and `pluginRepositories`, because at the moment, they are still in active development, and not available in the official Maven public repository.

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

Create a new class named `Post`, it includes three fields: `id`, `title`, `content`.

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

In the above codes, `@Data`, `@ToString`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor` are from the Lombok project.

When you compile `Post`, it will utilize Java compiler built-in  *Annotation Processing Tooling* feature to add extra facilities into the final compiled classes, including:

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

Currently we have not connect to any database, here we use a `Map` backed data store instead. When we come to discuss the reactive features provided by Spring Data projects, we will replace it with a real Spring Data reactive implementation.

If you have used Spring Data before, you will find these APIs are every similiar with `Repository` interface provided in Spring Data. 

The main difference is in the current Repository class all methods return a `Flux` or `Mono`.

`Flux` and `Mono` are from Reactor, which powers the reactive support in Spring 5 by default. 

* `Flux` means it could return lots of results in the stream. 
* `Mono` means it could return 0 to 1 result. 

Create a controller class named `PostController` to expose RESTful PAIs for `Post` entity.

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

Create a `@Configuration` class, add an `@EnableWebFlux` annotation to activiate webflux support in this application.

```java
@Configuration
@ComponentScan
@EnableWebFlux
class WebConfig {
    
}
```

Now we almost have done the programming work, let's try to bootstrap the application.

### Bootstrap

According to the official documention, in [WebFlux framework](http://docs.spring.io/spring-framework/docs/5.0.x/spring-framework-reference/web.html#web-reactive) section, there are some options to bootsrap a reactive web application.

>WebFlux can run on Servlet containers with support for the Servlet 3.1 Non-Blocking IO API as well as on other async runtimes such as Netty and Undertow. 

![Spring Boot initializer](https://github.com/hantsy/spring-reactive-sample/blob/master/webflux.png)


#### Apache Tomcat

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
java -jar target/spring-reactive-sample-vanilla-0.0.1-SNAPSHOT-jar-with-dependencies.jar
```

When it is started, try to fetch posts.

```
#curl http://localhost:8080/posts
[{"id":1,"title":"First Post","content":"content of First Post"},{"id":2,"title":"Second Post","content":"content of Second Post"}]
```

#### Jetty

To start a Jetty server, replace the bootstrap codes with the following:

```java
ServletHttpHandlerAdapter servlet = new ServletHttpHandlerAdapter(handler);

Server server = new Server(DEFAULT_PORT);

ServletContextHandler contextHandler = new ServletContextHandler();
contextHandler.setErrorHandler(null);
contextHandler.setContextPath("");
contextHandler.addServlet(new ServletHolder(servlet), "/");

server.setHandler(contextHandler);
server.start();
server.join();
```

Replace `tomcat-embed-core` with the following jetty related dependencies.

```xml
<dependency>
	<groupId>org.eclipse.jetty</groupId>
	<artifactId>jetty-server</artifactId>
</dependency>

<dependency>
	<groupId>org.eclipse.jetty</groupId>
	<artifactId>jetty-servlet</artifactId>
</dependency>
```
Similiarly, you can run the application directly in your IDEs.

Alternatively, you can run the application in Reactor Netty, or JBoss Undertow.

#### Reactor Netty

For Reactor Netty, replace the above bootstraping codes with:

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

#### Undertow

For Undertow, replace the bootstraping codes with:

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

If you are stick on traditional web applications, and want to package it into a **war** file and deploy it into an existing servlet container, Spring 5 provides a `AbstractAnnotationConfigDispatcherHandlerInitializer` to archive this purpose. It is a standard Spring `ApplicationInitializer` implementation which can be scanned by Spring container when servlet container starts up.

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

Next change the project packaging from **jar** to **war** in pom.xml.

```xml
<packaging>war</packaging>
```

And add `serlvet-api` to your project dependencies.

```xml
<dependency>
	<groupId>javax.servlet</groupId>
	<artifactId>javax.servlet-api</artifactId>
	<scope>provided</scope>
</dependency>
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

As you see, in the pom.xml, new Spring Boot strater `spring-boot-starter-webflux`  is added.

```xml
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
```

And the `spring-boot-maven-plugin` is added in the initial pom.xml.

Spring Boot starter `spring-boot-starter-webflux` will handle the `spring-webflux` related dependencies  and enable webflux support automatically. 

Compare to the former vanilla version,

1. No need explicit `WebConfig`, Spring Boot configures it automatically.
2. The former bootstraping class or `ApplicationInitializer` is no use now, the Spring Boot built-in `@SpringBootApplication` annotated class hands over the application bootstrap.

```java
@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
```

By default, Spring Boot will use Reactor Netty to run a webflux application. No need extra configuration for it.

Starts up application via:

```
mvn spring-boot:run
```

### Apache Tomcat

If you want to use Apache Tomcat as target runtime environment, just exclude `spring-boot-starter-reactor-netty` from `spring-boot-starter-webflux`, and add `spring-boot-starter-tomcat` into project dependencies.

```xml
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-webflux</artifactId>
	 <exclusions>
		<exclusion>
			<artifactId>spring-boot-starter-reactor-netty</artifactId>
			<groupId>org.springframework.boot</groupId>
		</exclusion>
	</exclusions>
</dependency>

<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-tomcat</artifactId>
</dependency>
```

### Jetty 

You can use Jetty to replace the default Reactor Netty.

```xml
<dependency>
	<groupId>org.springframework.security</groupId>
	<artifactId>spring-security-webflux</artifactId>
	 <exclusions>
		<exclusion>
			<artifactId>spring-boot-starter-reactor-netty</artifactId>
			<groupId>org.springframework.boot</groupId>
		</exclusion>
	</exclusions>
</dependency>

<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-jetty</artifactId>
</dependency>
```

### Undertow

Similiarly, you can use Undertow as target runtime.

```xml
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-webflux</artifactId>
	<exclusions>
		<exclusion>
			<artifactId>spring-boot-starter-reactor-netty</artifactId>
			<groupId>org.springframework.boot</groupId>
		</exclusion>
	</exclusions>
</dependency>

<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-undertow</artifactId>
</dependency>
```

## Reactive Data Operations

The next generation of Spring Data aslo adds Reactive Streams support.

At the moment, Data Redis, Data MongoDB and Data Cassandra will be the first-class citizen to get basic reactive support.


### Spring Data Mongo

Spring Data Mongo provides reactive variants of `MongoTemplate` and `MongoRepsoitory`, aka `ReactiveMongoTemplate` and `ReactiveMongoRepository` which have reactive capablities.

Add the following into project dependencies.

```xml
<dependency>
	<groupId>org.springframework.data</groupId>
	<artifactId>spring-data-mongodb</artifactId>
</dependency>
<dependency>
	<groupId>org.mongodb</groupId>
	<artifactId>mongodb-driver-reactivestreams</artifactId>
</dependency>
```

Create a `@Configuration` class to configure Mongo and enable Reactive support.

```java
@EnableReactiveMongoRepositories(basePackageClasses = {MongoConfig.class})
public class MongoConfig extends AbstractReactiveMongoConfiguration {

    @Value("${mongo.uri}")
    String mongoUri;

    @Override
    public MongoClient mongoClient() {
        return MongoClients.create(mongoUri);
    }

    @Override
    protected String getDatabaseName() {
        return "blog";
    }

}
```

Create a new `Post` MongoDB document class.

```java
@Document
@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
class Post {

    @Id
    private String id;
    private String title;
    private String content;


}
```

1. `@Document` declares it as a MongoDB document.
2. `@Id` indicates it is the identifier field of `Post` document.

Delcares a `PostRepository` interface to extend Sprign Data MongoDB specific `ReactiveMongoRepository`.

```java
interface PostRepository extends ReactiveMongoRepository<Post, String> {
}
```

Configure MongoDB connection in the *appliation.yml* file.

```yml
spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017/blog
```

Before starting up your application, make sure there is a running MongoDB instance in your local system. 

**NOTE**: If you have not installed it, go to [Mongo download page](ttps://www.mongodb.com/download-center?jmp=nav#community) and get a copy of MongoDB, and install it into your system.

Alternatively, if you are familiar with Docker, it is simple to start a MongoDB instance via Docker Compose file.

```yml
version: '3.3' # specify docker-compose version

# Define the services/containers to be run
services:

  redis:
    image: redis
    ports:
      - "6379:6379"
      
  mongodb: 
    image: mongo 
    volumes:
      - mongodata:/data/db
    ports:
      - "27017:27017"
    command: --smallfiles --rest
#   command: --smallfiles --rest --auth  

volumes:
  mongodata:  
```

Execute the following command to start a Mongo instance in a Docker container.

```
docker-compose up mongodb
```

When the Mongo service is started, it is ready for bootstraping the application.

```
mvn spring-boot:run
```

#### Spring Boot

If you are using Spring Boot, the configuration can be simplified. Just add `spring-boot-starter-data-mongodb-reactive` into the project dependencies.

```
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-data-mongodb-reactive</artifactId>
</dependency>
```

No need extra configuration class, Spring Boot will enable reactive support for MongoDB in this project. `ReactiveMongoTemplate` and `ReactiveMongoRepository` will be configured automatically.

#### Data Auditing Support

Spring Data Mongo supports data auditing as Spring Data JPA, it can set the current user and created/last modified timestamp to a field automatically.

Add `EnableMongoAuditing` to application class to activiate auditing for MongoDB.

```
@EnableMongoAuditing
public class DemoApplication {}
```

In `Post` document, add a new field `createdDate`, annotated it with `@CreatedDate`, it will fill the createdDate with current date when inserting it into MongoDB.

```java
@CreatedDate
private LocalDateTime createdDate;
```

#### Data initialition

Add some test datas into MongoDB when it starts up.

```java
@Component
@Slf4j
class DataInitializr implements CommandLineRunner {

    private final PostRepository posts;

    public DataInitializr(PostRepository posts) {
        this.posts = posts;
    }

    @Override
    public void run(String[] args) {
        log.info("start data initialization  ...");
        this.posts
            .deleteAll()
            .thenMany(
                Flux
                    .just("Post one", "Post two")
                    .flatMap(
                        title -> this.posts.save(Post.builder().title(title).content("content of " + title).build())
                    )
            )
            .log()
            .subscribe(
                null,
                null,
                () -> log.info("done initialization...")
            );

    }

}
```

Use a `CommandLineRunner` to make sure the `run` method is executed after the application is started.

Execute `mvn spring-boot:run` to start up the application now, then we can test if the data is initialized successfully.

```
curl -v http://localhost:8080/posts
* timeout on name lookup is not supported
*   Trying ::1...
* TCP_NODELAY set
* Connected to localhost (::1) port 8080 (#0)
> GET /posts HTTP/1.1
> Host: localhost:8080
> User-Agent: curl/7.54.1
> Accept: */*
>
< HTTP/1.1 200 OK
< transfer-encoding: chunked
< Content-Type: application/json;charset=UTF-8
< Cache-Control: no-cache, no-store, max-age=0, must-revalidate
< Pragma: no-cache
< Expires: 0
< X-Content-Type-Options: nosniff
< X-Frame-Options: DENY
< X-XSS-Protection: 1 ; mode=block
<
[{"id":"599149d53c44062e08c58b86","title":"Post one","content":"content of Post one","createdDate":[2017,8,14,14,57,25,71000000]},{"id":"599149d53c44062e08c58b87","title":"Post two","content":"content of Post two","createdDate":[2017,8,14,14,57,25,173000000]}]* Connection #0 to host localhost left intact
```

As you see, the data is initialized and createdDate is inserted automatically.

### Spring Data Redis

Spring Data Redis provides a reactive variant of `RedisConnectionFactory` aka `ReactiveRedisConnectionFactory` which return a `ReactiveConnection`.

Add the following into your project dependencies.

```xml
<dependency>
	<groupId>org.springframework.data</groupId>
	<artifactId>spring-data-redis</artifactId>
</dependency>
<dependency>
	<groupId>io.lettuce</groupId>
	<artifactId>lettuce-core</artifactId>
</dependency>
<dependency>
	<groupId>org.apache.commons</groupId>
	<artifactId>commons-pool2</artifactId>
</dependency>    
```

**NOTE**: You have to use `lettuce` as redis driver to get reactive support in `spring-data-redis`, and add `commons-pool2` to support Redis connection pool.

Create a `@Configuration` class to configure Mongo and enable Reactive support for Redis.

```java
@EnableRedisRepositories
public class RedisConfig {

    @Autowired
    RedisConnectionFactory factory;

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory();
    }
    
    @Bean
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory connectionFactory){
        RedisTemplate<Object, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);
        return redisTemplate;
    }
    
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory){
        return new StringRedisTemplate(connectionFactory);
    }

    @PreDestroy
    public void flushTestDb() {
        factory.getConnection().flushDb();
    }

}
```

`LettuceConnectionFactory` implements `RedisConnectionFactory` and `ReactiveRedisConnectionFactory` interfaces, when a `LettuceConnectionFactory` is declared, `RedisConnectionFactory` and `ReactiveRedisConnectionFactory` are also registered as beans. 

**NOTE**: Spring Data Redis does not provides a variant for `RedisTemplate` and `Repository`.

In your beans, you can inject a `ReactiveRedisConnectionFactory` and get a reactive connection.

```java
@Inject ReactiveRedisConnectionFactory factory;

ReactiveRedisConnection conn = factory.getReactiveConnection();
```

`ReactiveConnection` provides some reactive methods for redis operations.

For example, create a favorites list for posts.

```java
conn.setCommands()
	.sAdd(
		ByteBuffer.wrap("users:user:favorites".getBytes()),
		this.posts.findAll()
			.stream()
			.map(p -> p.getId().getBytes())
			.map(ByteBuffer::wrap)
			.collect(Collectors.toList())
	)
	.log()
	.subscribe(null, null, ()-> log.info("added favirates..."));
```

And show my favorites in the controller.

```
@RestController()
@RequestMapping(value = "/favorites")
class FavoriteController {

    private final ReactiveRedisConnectionFactory factory;

    public FavoriteController(ReactiveRedisConnectionFactory factory) {
        this.factory = factory;
    }

    @GetMapping("")
    public Mono<List<String>> all() {
        return this.factory.getReactiveConnection()
                .setCommands()
                .sMembers(ByteBuffer.wrap("users:user:favorites".getBytes()))
                .map(FavoriteController::toString)
                .collectList();
    }

    private static String toString(ByteBuffer byteBuffer) {

        byte[] bytes = new byte[byteBuffer.remaining()];
        byteBuffer.get(bytes);
        return new String(bytes);
    }

}
```

#### Spring Boot

For Spring Boot applications, the configuration can be simplified. Just add `spring-boot-starter-data-redis-reactive` into the project dependencies.

```
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-data-redis-reactive</artifactId>
</dependency>
```

Spring boot provides auto-configuration for redis, and registers `ReactiveRedisConnectionFactory` for you automatically.

#### Data Initialition

Declare `Post` as a redis hash data, add `@RedisHash("posts")` to `Post` POJO.

```java
@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RedisHash("posts")
class Post {
    
    @Id
    private String id;
    private String title;
    private String content;
    
}
```

Let's have a look at the `PostRepository`.

```java
interface PostRepository extends KeyValueRepository<Post, String> {

    @Override
    public List<Post> findAll();
}
```

`KeyValueRepository` is from `spring-data-keyvalue`, which is a generic Map based Repository implementation.

```java
private void initPosts() {
	this.posts.deleteAll();
	Stream.of("Post one", "Post two").forEach(
		title -> this.posts.save(Post.builder().id(UUID.randomUUID().toString()).title(title).content("content of " + title).build())
	);
}
```	

### Spring Data Cassandra



## Security for Webflux

Reflect to new webflux feature introduced in Spring 5, Spring Security 5 added new APIs to handle Web Reactive security.

Aligned with Spring 5, Spring Security 5 added a new module named `spring-secuirty-webflux`.

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

1. Use `@EnableWebFluxSecurity` annotation to enable Security for `spring-webflux` based application.
2. `SecurityWebFilterChain` bean is a must to configure the details of Spring Security. `HttpSecurity` is from `spring-secuirty-webflux`, similar with the general version, but handle `WebExhange` instead of Servlet based `WebRequest`.
3. A new `UserDetailsRepository` interface is introduced which is aligned with Reactor APIs. By default, an in-memory `Map` based implementation `MapUserDetailsRepository` is provided, you can customsize yourself by implementing the `UserDetailsRepository` interface.

Starts up the application and verify the Spring Security configuratoin work as expected.

```
mvn spring-boot:run
```

After it is started, try to add a new post without authentication:

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

The server side rejects the client request, and sends back a 401 error(401 Unauthorized).

Use the predefined **user:password** credentials to get authenticated and send the post request again.

```
curl -v  -X POST http://localhost:8080/posts -u "user:password" -H "Content-Type:application/json" -d "{\"title\":\"My Post\",\"content\":\"content of My Post\"}"
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

It is done secussfully, and returns the new created post.


## RouterFunction

`spring-webflux` also provides DSL like syntax to declare route rules for URI paths.



## Client

## Test

## Kotlin 

## Sample codes

The following table lits all sample codes related to this post.

| name          | description                              |
| ------------- | ---------------------------------------- |
| vanilla       | The initial application, includes basic `spring-webflux` feature, use a main class to start up the application |
| war           | Replace the manual bootstrap class in **vanilla** with Spring `ApplicationInitializer`, it can be packaged as a **war** file to be deployed into an external servlet container. |
| boot          | Switch to Spring Boot to get autoconfiguration of `webflux`, added Spring Data Mongo, Spring Secuirty support |
| boot-routes   | Use `RouterFunction` instead of the general `Controller` in **boot** |
| kotlin        | Convert **boot** to use kotlin           |
| kotlin-gradle | Use kotlin functional approach to declare beans and bootstrap the application programatically |
| session       | More features will be added here         |

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
* [From Java To Kotlin - Your Cheat Sheet For Java To Kotlin ](https://github.com/MindorksOpenSource/from-java-to-kotlin)
* [From Java to Kotlin](https://fabiomsr.github.io/from-java-to-kotlin/index.html)
* [Petclinic: Spring 5 reactive version](https://github.com/ssouris/petclinic-spring5-reactive/)
* [Spring Framework 5 Kotlin APIs, the functional way](https://spring.io/blog/2017/08/01/spring-framework-5-kotlin-apis-the-functional-way)
* [Kotlin extensions for MongoOperations and ReactiveMongoOperations ](https://github.com/spring-projects/spring-data-mongodb/commit/2359357977e8734331a78c88e0702f50f3a3c75e)
