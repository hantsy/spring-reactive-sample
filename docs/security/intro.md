
# Spring Security

Security is not a separate concern you add at the end of a project. In reactive applications, it is part of the request pipeline from the beginning: authentication, authorization, CSRF handling, and route protection all sit in front of the business logic.

This chapter focuses on Spring Security's WebFlux support and shows how it fits into a non-blocking architecture.

## What this chapter covers

- the difference between the reactive and servlet security models
- configuration of `ServerHttpSecurity` and endpoint protection
- reactive user details services and authentication providers
- method-level security and form login in a reactive application

## What you will learn

- how to secure routes declaratively and programmatically
- how to configure custom user services in a reactive stack
- how to reason about security in a non-blocking request pipeline
- how to test protected APIs without turning the app into a blocking prototype

## Prerequisites

It helps if you already know the basics of Spring Security and have a working WebFlux app from the [Getting Started](../start/intro.md) chapter.

## Chapter roadmap

1. [An Introduction to Spring Security](start.md) — how the reactive security model differs from the servlet version
2. [Configure Spring Security](config.md) — `@EnableWebFluxSecurity` and `ServerHttpSecurity`, verified with `curl`
3. [Configure Spring Security in Spring Boot](config-boot.md) — the starter-based auto-configuration path
4. [Method level constraints](method.md) — `@PreAuthorize` on reactive methods
5. [Customize UserDetailsRepository](user-details-repo.md) — load users from properties or a database
6. [Form based authentication](form.md) — secure server-rendered pages and login flows
