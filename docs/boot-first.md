# Create a WebFlux application with Spring Boot

The Spring Boot 2.x targets the latest Spring technology stack, including Spring 5, Spring Security 5, Spring Session 2, etc. 

Spring Boot added a new starter `spring-boot-starter-webflux` for starting a WebFlux application.

Open browser and navigate to [http://start.spring.io](http://start.spring.io). 

![Spring Boot initializer](./init.png)

In the Spring Boot Initializer page. 

1. Select the latest stable Spring Boot version, eg. 2.4.3 at the moment. 
2. In the dependencies box, type **reactive**, it will display all reactive options in a dropdown menu. Select **Ractive Web** to add `spring-boot-starter-webflux` into project dependencies. You can also add other items as you like, such as **Reactive MongoDb**, **Reactive Redis** etc.
3. Click **Generate project** button or hint **ALT+NETER** keys to generate a project skeleton as a zip file for downloading.

Download the archive and extract files into your disc, import the source codes into your IDEs.

As you see, in the pom.xml, the new Spring Boot strater `spring-boot-starter-webflux` is added.

```xml
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
```

And the `spring-boot-maven-plugin` is added in the initial pom.xml.

Spring Boot starter `spring-boot-starter-webflux` will handle the `spring-webflux` related dependencies and enable webflux support automatically. It also add `logback` as default logging handler.

Compare to the former vanilla version,

1. No need explicit `WebConfig`, Spring Boot configures it automatically.
2. The former bootstraping class or `AppInitializer` is no use now, the Spring Boot built-in `@SpringBootApplication` annotated class hands over the application bootstrap.

```java
@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
```

By default, Spring Boot will use Reactor Netty as runtime to run a webflux application. No need extra configuration for it.

To start the application in your terminal, run the following command in the project root folder.

```
mvn spring-boot:run
```

Alternatively, to start Spring Boot applications in your IDEs, run it just like run a general-purpose Java application.

As an exercise, try to add the similar codes with [the former post](./first.md).

1. Add a POJO `Post`.
2. Add a dummy `PostRepository`.
3. Add a simple `PostController`.

Restart the application, and test the endpoints.

```bash
#curl http://localhost:8080/posts
...
```

It is easy to switch to other embedded servers.

Check the complete codes, [spring-reactive-sample/boot](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-start).

## Apache Tomcat

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

Check the sample codes, [spring-reactive-sample/boot-tomcat](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-tomcat).

## Eclipse Jetty 

To use Jetty to replace the default Reactor Netty.

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

Check the sample codes, [spring-reactive-sample/boot-jetty](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-jetty).

## Undertow

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

Check the sample codes, [spring-reactive-sample/boot-undertow](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-undertow).