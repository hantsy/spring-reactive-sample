/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;

import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;

interface PostRepository extends ReactiveCassandraRepository<Post, String>{}