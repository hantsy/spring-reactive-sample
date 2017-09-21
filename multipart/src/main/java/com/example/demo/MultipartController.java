/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 *
 * @author hantsy
 * 
 * //sample code: https://github.com/spring-projects/spring-framework/blob/master/spring-webflux/src/test/java/org/springframework/web/reactive/result/method/annotation/MultipartIntegrationTests.java
 */
@RestController
@RequestMapping("/uploads")
public class MultipartController {

    //			@RequestPart("fileParts") FilePart fileParts,
//				@RequestPart("fileParts") Mono<FilePart> filePartsMono,
//				@RequestPart("fileParts") Flux<FilePart> filePartsFlux,
//    @PostMapping("/requestBodyMap")
//    Mono<String> requestBodyMap(@RequestBody Mono<MultiValueMap<String, Part>> partsMono) {
//        return partsMono.map(this::partMapDescription);
//    }

    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    Mono<String> requestBodyFlux(@RequestBody Flux<Part> parts) {
        return partFluxDescription(parts);
    }

    private static String partMapDescription(MultiValueMap<String, Part> partsMap) {
        return partsMap.keySet().stream().sorted()
            .map(key -> partListDescription(partsMap.get(key)))
            .collect(Collectors.joining(",", "Map[", "]"));
    }

    private static Mono<String> partFluxDescription(Flux<? extends Part> partsFlux) {
        return partsFlux.log().collectList().map(MultipartController::partListDescription);
    }

    private static String partListDescription(List<? extends Part> parts) {
        return parts.stream().map(MultipartController::partDescription)
            .collect(Collectors.joining(",", "[", "]"));
    }

    private static String partDescription(Part part) {
        return part instanceof FilePart ? part.name() + ":" + ((FilePart) part).filename() : part.name();
    }

}
