
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

## A friendly reading path

A natural way through this section is to begin with the general security model and then move into the more concrete configuration pieces:

- start with [An Introduction to Spring Security](start.md) to understand how the reactive model differs from the servlet-based approach
- continue with [Configure Spring Security](config.md) to see the core `ServerHttpSecurity` setup and how it behaves in practice
- then read [Configure Spring Security in Spring Boot](config-boot.md) for the auto-configuration-based variant you will usually reach for in real projects
- move into [Method level constraints](method.md) to protect business logic and endpoints with authorization rules
- finish with [Customize UserDetailsRepository](user-details-repo.md) and [Form based authentication](form.md) for user management and login flows

The chapters are ordered to build from concept to implementation: first the model, then the configuration, then the practical details that make security feel real in a reactive application.
