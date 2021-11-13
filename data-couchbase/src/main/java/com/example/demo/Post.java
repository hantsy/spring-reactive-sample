/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;


import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.couchbase.core.mapping.Document;
import org.springframework.data.couchbase.core.mapping.Field;
import org.springframework.data.couchbase.core.mapping.id.GeneratedValue;
import org.springframework.data.couchbase.core.mapping.id.GenerationStrategy;

/**
 * @author hantsy
 */
@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document
class Post {

    @Id
    @GeneratedValue(strategy = GenerationStrategy.UNIQUE)
    private String id;
    private String title;

    @Field(value = "content")
    private String content;

}
