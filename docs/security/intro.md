
# Spring Security

This chapter explains how to secure reactive applications using Spring Security's WebFlux support. It covers authentication, authorization, and integration with reactive data stores.

## What this chapter covers

- Core differences between the reactive and servlet security stacks
- Configuring `ServerHttpSecurity` and securing routes
- Reactive user details services and authentication mechanisms
- Method-level security and form-based login

## What you'll learn

- How to secure endpoints with declarative and programmatic approaches
- How to configure reactive authentication providers and custom user services
- Patterns for secure error handling and CSRF considerations in reactive apps

## Prerequisites

- Familiarity with Spring Security concepts (filters, authentication manager)
- A working WebFlux application from the [Getting Started](../start/intro.md) chapter

## Chapter roadmap

1. [An Introduction to Spring Security](start.md) — how the reactive security stack differs from the servlet stack
2. [Configure Spring Security](config.md) — `@EnableWebFluxSecurity` and `ServerHttpSecurity`, verified with curl
3. [Configure Spring Security in Spring Boot](config-boot.md) — the starter and auto-configuration path
4. [Method level constraints](method.md) — `@PreAuthorize` on reactive methods
5. [Customize UserDetailsRepository](user-details-repo.md) — load users from a properties file or a database
6. [Form based authentication](form.md) — protecting server-rendered pages
