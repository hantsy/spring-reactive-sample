/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;

import java.util.UUID;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 *
 * @author hantsy
 */
@RestController
@RequestMapping("/uploads")
public class MultipartController {

    private final GridFsTemplate gridFsTemplate;

    MultipartController(GridFsTemplate gridFsTemplate) {
        this.gridFsTemplate = gridFsTemplate;
    }

    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    Mono<String> uploadSingleFile(@RequestBody Mono<FilePart> part) {

        FilePart filePart = part
            .log()
            .block();

        String name = UUID.randomUUID().toString() + "-" + filePart.name();
        String contentType = filePart.headers().getContentType().toString();
        ObjectId id = this.gridFsTemplate.store(filePart.content().blockFirst().asInputStream(), name, contentType);
        
        return Mono.just(id.toString());
    }

}
