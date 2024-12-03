package com.example.demo;

import org.springframework.data.couchbase.repository.ReactiveCouchbaseRepository;

public interface PostRepository extends ReactiveCouchbaseRepository<Post, String> {
}
