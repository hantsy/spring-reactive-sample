/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo.repository;

import com.example.demo.domain.Post;
import org.springframework.data.elasticsearch.repository.ReactiveElasticsearchRepository;

public interface PostRepository extends ReactiveElasticsearchRepository<Post, String> {
}