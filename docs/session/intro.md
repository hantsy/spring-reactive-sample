
# Spring Session Reactive

This chapter describes session management for reactive applications. Unlike traditional servlet sessions, reactive session support requires non-blocking stores and integration points that work with the WebFlux runtime.

What this chapter covers

- Reactive session concepts and how they differ from servlet sessions
- Supported stores (Redis, MongoDB, other distributed stores) and their trade-offs
- Spring Session configuration for reactive applications
- Patterns for stateless authentication, token strategies and when to use server-side sessions

What you'll learn

- How to choose a session strategy for a reactive service
- How to configure Spring Session with reactive data stores
- How to migrate existing session-based apps to a reactive model

Prerequisites and notes

- Familiarity with Spring Security and basic session concepts is helpful
- Prefer external, non-blocking stores (e.g., Redis with Lettuce) for production

Examples and roadmap

1. Configuring reactive session storage
2. Using Spring Session with Spring Security in a WebFlux app
3. Migration notes and testing strategies

This chapter focuses on practical configuration and patterns for production-ready session handling in reactive systems.
