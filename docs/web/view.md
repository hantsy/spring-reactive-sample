---
sort: 9
---

# View and View Resolvers

Like the traditional Spring MVC, Spring WebFlux also includes the capability to rendering views by the integrated template engine.

## ThymeLeaf

Thymeleaf  is the most popular template engine in Spring ecosystem.  

Add thymeleaf related dependencies.

```xml
<dependency>
    <groupId>org.thymeleaf.extras</groupId>
    <artifactId>thymeleaf-extras-java8time</artifactId>
</dependency>
<dependency>
    <groupId>org.thymeleaf</groupId>
    <artifactId>thymeleaf-spring5</artifactId>
</dependency>     
```

Configure Thymeleaf in a Spring web application. 

```java
@Configuration
@EnableWebFlux
class WebConfig implements ApplicationContextAware, WebFluxConfigurer {

    private ApplicationContext ctx;

    @Override
    public void setApplicationContext(ApplicationContext context) {
        this.ctx = context;
    }

    @Bean
    public SpringResourceTemplateResolver thymeleafTemplateResolver() {

        final SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver();
        resolver.setApplicationContext(this.ctx);
        resolver.setPrefix("classpath:/templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCacheable(false);
        resolver.setCheckExistence(false);
        return resolver;

    }

    @Bean
    public ISpringWebFluxTemplateEngine thymeleafTemplateEngine() {
        // We override here the SpringTemplateEngine instance that would otherwise be
        // instantiated by
        // Spring Boot because we want to apply the SpringWebFlux-specific context
        // factory, link builder...
        final SpringWebFluxTemplateEngine templateEngine = new SpringWebFluxTemplateEngine();
        templateEngine.setTemplateResolver(thymeleafTemplateResolver());
        return templateEngine;
    }

    @Bean
    public ThymeleafReactiveViewResolver thymeleafChunkedAndDataDrivenViewResolver() {
        final ThymeleafReactiveViewResolver viewResolver = new ThymeleafReactiveViewResolver();
        viewResolver.setTemplateEngine(thymeleafTemplateEngine());
//        viewResolver.setOrder(1);
//        viewResolver.setViewNames(new String[]{"home"});
        viewResolver.setResponseMaxChunkSizeBytes(8192); // OUTPUT BUFFER size limit
        return viewResolver;
    }

    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
        registry.viewResolver(thymeleafChunkedAndDataDrivenViewResolver());
    }

}

```

Thymeleaf provides reactive support in the template engine and allow you render a Flux stream dynamically.

An example of Thymeleaf template.

```html
<!DOCTYPE html>
<html>
    <head>
        <title>Simple Blog Posts</title>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
    </head>
    <body>
        <h1>All posts</h1>
        <div>
            <table>
                <thead>
                    <tr>
                        <th> ID</th>
                        <th>Title </th>
                        <th>Content</th>
                    </tr>
                </thead>
                <tbody>
                    <tr th:each="e : ${posts}">
                        <td th:text="${e.id}"></td>
                        <td th:text="${e.title}"></td>
                        <td th:text="${e.content}"></td>
                    </tr>
                </tbody>
            </table>  

        </div>
    </body>
</html>
```



Source codes:   [spring-reactive-sample/mvc-thymeleaf](https://github.com/hantsy/spring-reactive-sample/blob/master/mvc-thymeleaf) and  [spring-reactive-sample/boot-mvc-thymeleaf](https://github.com/hantsy/spring-reactive-sample/blob/master/boot-mvc-thymeleaf) .

## Freemarker

Spring 5 provides built-in support to Freemarker template engine.

Add Freemarker dependencies.

```xml
<dependency>
    <groupId>org.freemarker</groupId>
    <artifactId>freemarker</artifactId>
</dependency>       
```

Add Freemarker configuration.

```java
@Configuration
@EnableWebFlux
class WebConfig implements ApplicationContextAware, WebFluxConfigurer {

    private ApplicationContext ctx;

    @Override
    public void setApplicationContext(ApplicationContext context) {
        this.ctx = context;
    }

    @Bean
    public FreeMarkerConfigurer freeMarkerConfig() {
        Map<String, Object> variables = new HashMap<>();
        variables.put("xml_escape", new XmlEscape());

        FreeMarkerConfigurer configurer = new FreeMarkerConfigurer();
        configurer.setPreferFileSystemAccess(false);
        configurer.setTemplateLoaderPath("classpath:/templates");
        configurer.setResourceLoader(this.ctx);
        configurer.setFreemarkerVariables(variables);

        return configurer;
    }

    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
        registry.freeMarker();
    }

}
```

An example of Freemarker template.

```html
<!DOCTYPE html>
<html>
    <head>
        <title>Simple Blog Posts</title>
        <meta charset="UTF-8"/>
        <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    </head>
    <body>
        <h1>All posts</h1>
        <div>
            <table>
                <thead>
                    <tr>
                        <th> ID</th>
                        <th>Title </th>
                        <th>Content</th>
                    </tr>
                </thead>
                <tbody>

                        <#list posts as post>        
                        <tr>
                            <td>${post.id}</td>
                            <td>${post.title}</td>
                            <td>${post.content}</td>
                        </tr>
                        <#else>
                        nothing
                        </#list>

                </tbody>
            </table>  
        </div>
    </body>
</html>
```

Source codes:   [spring-reactive-sample/mvc-freemarker](https://github.com/hantsy/spring-reactive-sample/blob/master/mvc-freemarker) and  [spring-reactive-sample/boot-mvc-freemarker](https://github.com/hantsy/spring-reactive-sample/blob/master/boot-mvc-freemarker) .

## Mustache

[Mustache](https://mustache.github.io/) is  a popular template engine in the Ruby and  NodeJS world, it also provides Java implementation.

Spring framework does not integrate Mustache as Freemarker. But you can create your own `Veiw` and `ViewResolver` to adapt the Mustache compiler to the Spring world.

Add Mustache java into dependencies, we use `jmustache` here.

```xml
<dependency>
    <groupId>com.samskivert</groupId>
    <artifactId>jmustache</artifactId>
</dependency>
```

Then create a `MustacheView` to render the tempalte.

```java
public class MustacheView extends AbstractUrlBasedView {

    private Compiler compiler;

    private String charset;

    /**
     * Set the JMustache compiler to be used by this view. Typically this property is not
     * set directly. Instead a single {@link Compiler} is expected in the Spring
     * application context which is used to compile Mustache templates.
     * @param compiler the Mustache compiler
     */
    public void setCompiler(Compiler compiler) {
        this.compiler = compiler;
    }

    /**
     * Set the charset used for reading Mustache template files.
     * @param charset the charset to use for reading template files
     */
    public void setCharset(String charset) {
        this.charset = charset;
    }

    @Override
    public boolean checkResourceExists(Locale locale) throws Exception {
        return resolveResource() != null;
    }

    @Override
    protected Mono<Void> renderInternal(Map<String, Object> model, MediaType contentType,
                                        ServerWebExchange exchange) {
        Resource resource = resolveResource();
        if (resource == null) {
            return Mono.error(new IllegalStateException(
                    "Could not find Mustache template with URL [" + getUrl() + "]"));
        }
        DataBuffer dataBuffer = exchange.getResponse().bufferFactory().allocateBuffer();
        try (Reader reader = getReader(resource)) {
            Template template = this.compiler.compile(reader);
            Charset charset = getCharset(contentType).orElse(getDefaultCharset());
            try (Writer writer = new OutputStreamWriter(dataBuffer.asOutputStream(),
                    charset)) {
                template.execute(model, writer);
                writer.flush();
            }
        }
        catch (Exception ex) {
            DataBufferUtils.release(dataBuffer);
            return Mono.error(ex);
        }
        return exchange.getResponse().writeWith(Flux.just(dataBuffer));
    }

    private Resource resolveResource() {
        Resource resource = getApplicationContext().getResource(getUrl());
        if (resource == null || !resource.exists()) {
            return null;
        }
        return resource;
    }

    private Reader getReader(Resource resource) throws IOException {
        if (this.charset != null) {
            return new InputStreamReader(resource.getInputStream(), this.charset);
        }
        return new InputStreamReader(resource.getInputStream());
    }

    private Optional<Charset> getCharset(MediaType mediaType) {
        return Optional.ofNullable(mediaType != null ? mediaType.getCharset() : null);
    }

}
```

Create a `MustacheViewResolver` to resolve a view.

```java
public class MustacheViewResolver extends UrlBasedViewResolver {

    private final Compiler compiler;

    private String charset;

    /**
     * Create a {@code MustacheViewResolver} backed by a default instance of a
     * {@link Compiler}.
     */
    public MustacheViewResolver() {
        this.compiler = Mustache.compiler();
        setViewClass(requiredViewClass());
    }

    /**
     * Create a {@code MustacheViewResolver} backed by a custom instance of a
     * {@link Compiler}.
     *
     * @param compiler the Mustache compiler used to compile templates
     */
    public MustacheViewResolver(Compiler compiler) {
        this.compiler = compiler;
        setViewClass(requiredViewClass());
    }

    /**
     * Set the charset.
     *
     * @param charset the charset
     */
    public void setCharset(String charset) {
        this.charset = charset;
    }

    @Override
    protected Class<?> requiredViewClass() {
        return MustacheView.class;
    }

    @Override
    protected AbstractUrlBasedView createView(String viewName) {
        MustacheView view = (MustacheView) super.createView(viewName);
        view.setCompiler(this.compiler);
        view.setCharset(this.charset);
        return view;
    }

}
```

Configure `MustacheViewResolver` .

```java
@Configuration
@EnableWebFlux
class WebConfig implements ApplicationContextAware, WebFluxConfigurer {

    private ApplicationContext ctx;

    @Override
    public void setApplicationContext(ApplicationContext context) {
        this.ctx = context;
    }

    @Bean
    public ViewResolver mustacheViewResolver() {
        String prefix = "classpath:/templates/";
        String suffix = ".mustache";
        Mustache.TemplateLoader loader = new MustacheResourceTemplateLoader(prefix, suffix);
        MustacheViewResolver mustacheViewResolver = new MustacheViewResolver(Mustache.compiler().withLoader(loader));
        mustacheViewResolver.setPrefix(prefix);
        mustacheViewResolver.setSuffix(suffix);
        return mustacheViewResolver;
    }

    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
        registry.viewResolver(mustacheViewResolver());
    }

}
```

The `MustacheResourceTemplateLoader` is a Spring aware template resource loader.

```java
public class MustacheResourceTemplateLoader implements TemplateLoader, ResourceLoaderAware {

    private String suffix = "classpath:templates/";
    private String prefix = ".mustache";
    private String charset = "UTF-8";

    private ResourceLoader resourceLoader = new DefaultResourceLoader();

    public MustacheResourceTemplateLoader(String prefix, String suffix) {
        this.prefix = prefix;
        this.suffix = suffix;
    }

    /**
     * Set the charset.
     *
     * @param charset the charset
     */
    public void setCharset(String charset) {
        this.charset = charset;
    }

    /**
     * Set the resource loader.
     *
     * @param resourceLoader the resource loader
     */
    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public Reader getTemplate(String name) throws IOException {
        return new InputStreamReader(this.resourceLoader
                        .getResource(this.prefix + name + this.suffix).getInputStream(),
                this.charset);
    }

}
```

An example of Mustache template file.


{% highlight html %}{% raw %}
{{>header}}
	<section class="container">
        <div class="row">
			{{#posts}}
            <div class="col-md-12">
			    <div class="card">
				  <div class="card-body">
					<h4 class="card-title">{{title}}</h4>
					<h6 class="card-subtitle mb-2 text-muted">{{createdDate}}</h6>
					<p class="card-text">{{content}}</p>
					<a href="/posts/{{id}}" class="card-link">View Details</a>
				  </div>
				</div>
			</div>
			{{/posts}}
        </div>
    </section>
{{>footer}}
{% endraw %}{% endhighlight %}


Source codes:   [spring-reactive-sample/mvc-mustache](https://github.com/hantsy/spring-reactive-sample/blob/master/mvc-mustache) and  [spring-reactive-sample/boot-mvc-mustache](https://github.com/hantsy/spring-reactive-sample/blob/master/boot-mvc-mustache) .
