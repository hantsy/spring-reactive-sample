# Preface

When Spring introduced its reactive programming stack in the Spring 5 era, I followed it closely and wrote an early article, [Reactive Programming with Spring 5](https://hantsy.medium.com/reactive-programming-with-spring-5-3bfc5d324ba0), to explain the new ideas in practical terms. This site is the evolution of that work: it takes the original material, refreshes it for **Spring Framework 7** and **Spring Boot 4**, and turns it into a more structured, hands-on guide.

The examples remain the core of the learning experience. Instead of long theoretical explanations, the book shows how reactive programming looks in real Spring applications — from a minimal HTTP service to database access, security, testing, and Kotlin integrations.

## Who this book is for

This is a practical guide to the Spring reactive stack, especially **Spring WebFlux**.

It is not a full reference for the entire Spring ecosystem. If you are learning Spring for the first time, it helps to have some background in Spring MVC, dependency injection, and Java application development. A basic understanding of modern Java and Maven or Gradle will make the journey smoother.

## Why the examples matter

At the heart of this book is a simple idea: _one runnable example is worth more than a thousand words_.

Reactive features in Spring span many layers — WebFlux, Spring Data, Spring Security, Spring Session, Kotlin coroutines, and the wider reactive ecosystem. Rather than explaining every concept in isolation, the book uses small, real examples to show how they behave in context.

The source code for every chapter lives in the GitHub repository: [hantsy/spring-reactive-sample](https://github.com/hantsy/spring-reactive-sample). The README lists each sample and gives you a quick description, so you can jump directly to the module that matches your interest.

## How to read this book

You do not need to read every chapter in a single sitting. A good path is:

1. read the core concepts in the Getting Started chapters
2. try one WebFlux example locally
3. move to data access, security, or testing when the use case matches your project
4. return to the Kotlin and ecosystem chapters when you need a broader view

The goal is not to memorize APIs; it is to understand how the pieces fit together in a reactive application.

## Contributing

Feedback is welcome.

If you find an error in the code samples or a gap in the documentation, please [open an issue](https://github.com/hantsy/spring-reactive-sample/issues). If a concept is missing or a sample would be useful, a pull request or issue report is always appreciated.

If this book has been useful to you, sharing it with others or supporting the project is a great way to keep the work going.

## About the author

I am a self-employed software engineer and consultant based in China, with more than 20 years of experience building Java and Spring applications. If you need technical consulting or development help, you can reach me at <hantsy@gmail.com>.

Enjoy the reading, and happy coding.
