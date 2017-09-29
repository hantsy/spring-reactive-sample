/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;

import com.couchbase.client.java.repository.annotation.Field;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.couchbase.core.mapping.Document;

/**
 *
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
    private String id;
    private String title;

    @Field("content")
    private String content;

}
