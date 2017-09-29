/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;

import org.springframework.data.couchbase.core.query.ViewIndexed;
import org.springframework.data.couchbase.repository.ReactiveCouchbaseRepository;

@ViewIndexed(designDoc = "post", viewName = "all")
interface PostRepository extends ReactiveCouchbaseRepository<Post, String>{}