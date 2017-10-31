import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.noarg.gradle.NoArgExtension


plugins {
    kotlin("jvm") version "1.1.51"
    application
}

buildscript {
    extra["kotlinVersion"] = "1.1.51"
    extra["springBootVersion"] = "2.0.0.M5"

    val kotlinVersion: String by extra
    //val springBootVersion: String by extra

    repositories {
        mavenCentral()
        maven { setUrl("https://plugins.gradle.org/m2/") }
        maven { setUrl("https://repo.spring.io/snapshot") }
        maven { setUrl("https://repo.spring.io/milestone") }
        maven { setUrl("https://oss.sonatype.org/content/repositories/snapshots") }
    }
    dependencies {
        classpath("io.spring.gradle:dependency-management-plugin:1.0.3.RELEASE")
        // classpath("org.springframework.boot:spring-boot-gradle-plugin:$springBootVersion")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
        classpath("org.jetbrains.kotlin:kotlin-allopen:$kotlinVersion")
        classpath("org.jetbrains.kotlin:kotlin-noarg:$kotlinVersion")
        classpath("org.junit.platform:junit-platform-gradle-plugin:1.0.0")
    }
}


apply {
    //plugin("kotlin")
    //plugin("application")
    //plugin("kotlin-spring")
    //plugin("eclipse")
    //plugin("org.springframework.boot")
    plugin("kotlin-noarg")
    plugin("io.spring.dependency-management")
    //plugin("com.github.johnrengelman.plugin-shadow") version "2.0.0"
    plugin("org.junit.platform.gradle.plugin")
}

application {
    mainClassName = "com.example.demo.ApplicationKt"
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

version = "1.0.0-SNAPSHOT"

val kotlinVersion: String by extra
val springBootVersion: String by extra

// https://spring.io/blog/2016/12/16/dependency-management-plugin-1-0-0-rc1
configure<DependencyManagementExtension> {
    imports {
        mavenBom("org.springframework.boot:spring-boot-parent:$springBootVersion")
    }
}

configure<NoArgExtension> {
    annotation("org.springframework.data.mongodb.core.mapping.Document")
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
            freeCompilerArgs = listOf("-Xjsr305=strict")
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
    compile("org.springframework:spring-context") {
        exclude(module = "spring-aop")
    }
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
    compile("org.slf4j:jcl-over-slf4j")
    compile("ch.qos.logback:logback-classic")

    compile("com.google.code.findbugs:jsr305:3.0.2") // Needed for now, could be removed when KT-19419 will be fixed

    //test
    testCompile("org.springframework:spring-test")
    testCompile("io.projectreactor:reactor-test")
    testCompile("org.junit.jupiter:junit-jupiter-api:5.0.0")
    testRuntime("org.junit.jupiter:junit-jupiter-engine:5.0.0")
    testRuntime("org.junit.platform:junit-platform-launcher:1.0.0")
}
