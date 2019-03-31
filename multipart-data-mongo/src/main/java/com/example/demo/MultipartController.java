package com.example.demo;

import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;
import static org.springframework.http.ResponseEntity.ok;

@RestController()
@RequestMapping(value = "/multipart")
@RequiredArgsConstructor
public class MultipartController {

    private final ReactiveGridFsTemplate gridFsTemplate;

    @PostMapping("")
    public Mono<ResponseEntity> upload(@RequestPart Mono<FilePart> fileParts) {
        return fileParts
            .flatMap(part -> this.gridFsTemplate.store(part.content(), part.filename()))
            .map((id) -> ok().body(Map.of("id", id.toHexString())));
    }


    @GetMapping("{id}")
    public Flux<Void> read(@PathVariable String id, ServerWebExchange exchange) {
        return this.gridFsTemplate.findOne(query(where("_id").is(id)))
            .log()
            .flatMap(gridFsTemplate::getResource)
            .flatMapMany(r -> exchange.getResponse().writeWith(r.getDownloadStream()));
    }

}
