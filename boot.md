# Build a Reactive application with Spring Boot 2.0

[TOC]

Nowadays, reactive programming becomes more and more popular. Spring 5 and Spring Boot 2.0 also bring built-in  reactive programming support.

According to the definition in [Wikipedia](https://en.wikipedia.org/wiki/Reactive_programming).

>Reactive programming is an asynchronous programming paradigm concerned with data streams and the propagation of change. This means that it becomes possible to express static (e.g. arrays) or dynamic (e.g. event emitters) data streams with ease via the employed programming language(s) 

[Reactor](http://projectreactor.io) is a [Reactive Streams](http://www.reactive-streams.org/) implementation, and provide fully non-blocking reactive programming model with back-pressure capability.

In this post, we will create a simple Spring Boot application with  the newest Reactive APIs provided in `spring-webflux`, and use  Reactor-Netty as runtime instead of  traditional Servlet based container.

## Prerequisites

Make sure you have already installed the following software.

* [Oracle Java 8](https://java.oracle.com) 
* [Apache Maven](https://maven.apache.org)
* [Gradle](http://www.gradle.org)
* Your favorite IDE, including :
  * [NetBeans IDE](http://www.netbeans.org)
  * [Eclipse IDE](http://www.eclipse.org) (or  Eclipse based IDE,  Spring ToolSuite is highly recommended) 
  * [Intellij IDEA](http://www.jetbrains.com)

## Generate project skeleton 

To kick start a Spring Boot application quickly, the simplest approach is utilizing [Spring Initializer](http://start.spring.io) to generate the project template.

Open your favoriate browser, and go to  [Spring Initializer](http://start.spring.io) page.

![Spring Boot initializer](https://github.com/hantsy/gs-boot-reactive/blob/master/start.png)

Fill the form field as you need.
* You can select Maven or Gradle as build tools.
* And select Kotlin, Groovy,  Java 8 as programming language.
* Then choose the Spring Boot version, be care about this field, only Spring Boot 2.0 support Reactive features, you can select the latest 2.0  milestone 4 or snapshot.
* In the group and artifact, input your desired values. Here we use the default value for demonstration purpose.
* In the **Selected Dependencies** field, type **reactive** in the input box, in the autocomplete dropdown menus, choose **Reactive Web**, you can also add other features as you need. 

Then hint **Generate Project** button or use **ALT+ENTER** keyboard shortcuts to get the generated codes. It will popup a download dialog for you, save it into your local disk.

## Import codes into IDE

Extract the download files, let's  try to import the codes into the popular IDEs, you can use your favorite one, including NetBeans IDE, Eclipse, Intellij IDEA etc,  all of them have excellent Maven support. 

### NetBeans IDE 

NetBeans IDE can recognize Maven project automatically.

1. Start up NetBeans IDE.
2. Click **Open project** icon from toolbar or choose **Open Project** from *File* menu.
3. In the **Open Project** dialog, find and select source codes folder, it should be marked as Mavenzied NetBeans project.
4. When it is selected, click the **Open Project** button.

### Eclipse 

Eclipse Java EE bundle or Spring ToolSuite is highly recommended for building  a Spring application.

1. Starts up Eclipse.
2. Select **Import...** from *File* menu.
3. In the **Import...** dialog, select *Maven/Existing Maven Project*,  click **Next** button.
4. In the **Import Maven Projects...** dialog, click *Browse* button to choose the location of the source codes as **Root Directory**.
5. Select the projects to be imported in the **Projects** list, then click **Finish** button.

### Intellij IDEA

For Java developers, IntelliJ IDEA is the most productive IDEs, it includes a free and open source community version and a commercial version, both have good Java and Maven support.

1. Starts up IDEA.
2. Select *New* / *Project from Existing Sources...* from *File* menu.
3. Choose the source folder in the **Select the File or Directory to import** dialog.
4. Then follow the steps in the  wizard to import the codes.

## Project structure

Let's have a look at the project structure.

```
|   .gitignore
|   mvnw
|   mvnw.cmd
|   pom.xml
|
+---.mvn
|   \---wrapper
|           maven-wrapper.jar
|           maven-wrapper.properties
|
\---src
    +---main
    |   +---java
    |   |   \---com
    |   |       \---example
    |   |           \---demo
    |   |                   DemoApplication.java
    |   |
    |   \---resources
    |           application.properties
    |
    \---test
        \---java
            \---com
                \---example
                    \---demo
                            DemoApplicationTests.java
```

It is a standard Maven project, but includes optional Maven Wrapper files.

`src/main/java/com/example/demo/DemoApplication.java` is the boostrap class of this application.

`src/main/resources/application.properties` is  the application configuration file. Spring Boot also support `YAML` format.

`src/test/java/com/example/demo/DemoApplicationTests.java` is a JUnit test class for this application.

Open the `pom.xml` file. 

There is `spring-boot-stater-webflux`, like the traditional `spring-boot-stater-web`, the new starter will add `spring-webflux` support in this project, and enable `webflux` configuration for this application .

```xml
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
```
By default, it will use `reactor-netty` as runtime.

## Create a Reactive Controller

Similar with traditional `Controller`,  `spring-webfux` also support the same annotation when you declare controllers, but it can use the new  reactive APIs.

```java
@RestController
@RequestMapping
class MessageController {

    @GetMapping
    Flux<Message> allMessages(){
        return Flux.just(
            Message.builder().body("hello Spring 5").build(),
            Message.builder().body("hello Spring Boot 2").build()
        );
    }
}

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
class Message {

    String body;
}
```

We just created a very simple Controller which includes a simple method to return all messages. Note here we return `Flux<Message>` instead of `List<Message>`.

Run the application via IDE run button, or execute `mvn spring-boot:run` in your command line tool.

```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::             (v2.0.0.M4)

...
2017-10-01 21:30:13.856  INFO 14460 --- [ctor-http-nio-1] r.ipc.netty.tcp.BlockingNettyContext     : Started HttpServer on /0:0:0:0:0:0:0:0:8080
2017-10-01 21:30:13.857  INFO 14460 --- [           main] o.s.b.web.embedded.netty.NettyWebServer  : Netty started on port(s): 8080
2017-10-01 21:30:13.889  INFO 14460 --- [           main] com.example.demo.DemoApplication         : Started DemoApplication in 7.001 seconds (JVM running for 28.193)
```
You can use `curl` to taste the APIs.

```
# curl http://localhost:8080
[{"body":"hello Spring 5"},{"body":"hello Spring Boot 2"}]
```

It works well. 

## RouterFunction

Another features provided in `spring-webflux` is its functional `RouterFunction` instead of traditional Controller. For those developers who prefer Lambda, this is a better option.

The above `MessageController` can be replaced with the following `RouterFunction` bean:

```java
    @Bean
    public RouterFunction<ServerResponse> routes() {
        return route(GET("/"), 
            (req)-> ok()
                .body(
                    BodyInserters.fromObject(
                        Arrays.asList(Message.builder().body("hello Spring 5").build(),
                            Message.builder().body("hello Spring Boot 2").build()
                        )
                    )
                )
        );
    }
```

These codes are equivalent with the former controller in functionality.

## Source codes

Check out the [source codes](https://github.com/hantsy/gs-boot-reactive) from my github account.

