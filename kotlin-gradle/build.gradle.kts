import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


plugins {
    kotlin("jvm")
    id("com.github.johnrengelman.plugin-shadow") version "2.0.0"
    application
}

application {
    mainClassName = "com.example.demo.ApplicationKt"
}

buildscript {
    //    ext {
//        kotlinVersion = "1.1.4"
//        springBootVersion = "2.0.0.M3"
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

val kotlinVersion = "1.1.4"
val springBootVersion = "2.0.0.M3"

apply {
    //plugin("kotlin")
    //plugin("kotlin-spring")
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
    compile("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")
    compile("org.springframework.boot:spring-boot-starter-webflux")
    compile("org.jetbrains.kotlin:kotlin-stdlib-jre8:$kotlinVersion")
    compile("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")

    compile("org.slf4j:slf4j-api")
    compile("ch.qos.logback:logback-classic")

    testCompile("org.springframework.boot:spring-boot-starter-test")
    testCompile("io.projectreactor:reactor-test")
}
