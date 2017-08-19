import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    application
    id("com.github.johnrengelman.plugin-shadow") version "2.0.0"
}

application {
    mainClassName = "com.example.demo.ApplicationKt"
}

val kotlinVersion = "1.1.4"
val springBootVersion = "2.0.0.M3"

buildscript {
//    ext {
//        kotlinVersion("1.1.4")
//        springBootVersion("2.0.0.M3")
//    }

    val kotlinVersion = "1.1.4"
    val springBootVersion = "2.0.0.M3"

    repositories {
        mavenCentral()
        maven { setUrl("https://plugins.gradle.org/m2/") }
        maven { setUrl("https://repo.spring.io/snapshot") }
        maven { setUrl("https://repo.spring.io/milestone") }
        maven { setUrl("https://oss.sonatype.org/content/repositories/snapshots") }
    }
    dependencies {
        classpath("io.spring.gradle:dependency-management-plugin:1.0.3.RELEASE")
        classpath("org.springframework.boot:spring-boot-gradle-plugin:$springBootVersion")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
        classpath("org.jetbrains.kotlin:kotlin-allopen:$kotlinVersion")
        //classpath("org.junit.platform:junit-platform-gradle-plugin:1.0.0-SNAPSHOT")
    }
}

apply {
    //plugin("kotlin")
    plugin("kotlin-spring")
    //plugin("eclipse")
    plugin("org.springframework.boot")
    plugin("io.spring.dependency-management")
    //plugin("org.junit.platform.gradle.plugin")
}

//dependencyManagement {
//    imports {
//        mavenBom("org.springframework.boot:spring-boot-parent:$springBootVersion")
//    }
//}

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
            //           freeCompilerArgs = listOf("-Xjsr305-annotations=enable")
        }
    }
}

repositories {
    mavenCentral()
    maven { setUrl("https://repo.spring.io/snapshot") }
    maven { setUrl("https://repo.spring.io/milestone") }
    maven { setUrl("https://oss.sonatype.org/content/repositories/snapshots") }
}


dependencies {
    //spring webflux
    compile("org.springframework:spring-webflux")
    compile("org.springframework:spring-context")
    compile("com.fasterxml.jackson.core:jackson-databind")
    compile("com.fasterxml.jackson.module:jackson-module-kotlin")
    compile("com.fasterxml.jackson.datatype:jackson-datatype-jdk8")

    compile("io.netty:netty-buffer")
    compile("io.projectreactor.ipc:reactor-netty")

    //spring security for webflux
    compile("org.springframework.security:spring-security-core")
    compile("org.springframework.security:spring-security-config")
    compile("org.springframework.security:spring-security-webflux")

    //spring data mongodb reactive
    compile("org.springframework.data:spring-data-mongodb")
    compile("org.mongodb:mongodb-driver-reactivestreams")

    //kotlin
    compile("org.jetbrains.kotlin:kotlin-stdlib-jre8:$kotlinVersion")
    compile("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")

    //slf4j and logback
    compile("org.slf4j:slf4j-api")
    conpile("org.slf4j:jcl-over-slf4j")
    compile("ch.qos.logback:logback-classic")

    //test
    testCompile("org.springframework:spring-test")
    testCompile("io.projectreactor:reactor-test")
}
