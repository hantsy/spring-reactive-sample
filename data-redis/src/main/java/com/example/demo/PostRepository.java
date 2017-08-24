/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;

import java.util.List;
import org.springframework.data.keyvalue.repository.KeyValueRepository;

interface PostRepository extends KeyValueRepository<Post, String> {

    @Override
    public List<Post> findAll();
}
