/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;

import java.util.List;
import java.util.UUID;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
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
    Mono<List<String>> requestBodyFlux(@RequestBody Flux<FilePart> parts) {

        return parts
                .log()
                .flatMap(
                        part -> {
                            String name = UUID.randomUUID().toString() + "-" + part.name();
                            String contentType = part.headers().getContentType().toString();
                            return part.content().map(data -> this.gridFsTemplate.store(data.asInputStream(), name, contentType));
                        }
                )
                .map(id -> id.toString())
                .collectList();

    }

}
