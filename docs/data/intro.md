
# An introduction to Spring Data (Reactive)

This chapter covers reactive data access in the Spring ecosystem. It explains available store integrations, repository patterns, and pragmatic guidance for choosing the right persistence strategy for reactive applications.

What this chapter covers

- Reactive repositories and common store integrations (MongoDB, Redis, Couchbase, Cassandra)
- R2DBC for reactive relational access and recommended drivers
- Mapping, pagination and query-by-example in reactive repositories
- Interop with Reactor, RxJava, and Mutiny where applicable

What you'll learn

- When to use reactive repositories vs external streaming approaches
- How to model data and perform common operations without blocking
- Practical configuration examples for key stores and drivers

Quick notes

- Traditional JDBC/JPA are blocking and should be avoided in critical reactive paths
- R2DBC provides a reactive relational API; check driver support and maturity for your DB

Chapter roadmap

1. Reactive repository patterns and interfaces
2. Store-specific guides (MongoDB, Redis, Cassandra, Couchbase)
3. Pagination, sorting and query-by-example in a reactive context
4. Migration and testing strategies for reactive data access

See the data/\* files for concrete examples and configuration snippets.
