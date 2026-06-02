package com.example.demo;

import com.mongodb.MongoClientSettings;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.pojo.Conventions;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.mongodb.autoconfigure.MongoClientSettingsBuilderCustomizer;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }


    @Bean
    MongoClientSettingsBuilderCustomizer mongoClientSettingsBuilderCustomizer() {
        var codecRegistry = CodecRegistries.fromRegistries(
                MongoClientSettings.getDefaultCodecRegistry(),
                CodecRegistries.fromProviders(
                        PojoCodecProvider
                                .builder()
                                .register(Product.class.getPackageName())
                                .conventions(Conventions.DEFAULT_CONVENTIONS)
                                .automatic(true)
                                .build()
                )
        );
        return settings -> settings.codecRegistry(codecRegistry);
    }
}
