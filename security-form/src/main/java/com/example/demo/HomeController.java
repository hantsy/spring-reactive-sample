/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;

import java.time.Duration;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import reactor.core.publisher.Flux;

/**
 *
 * @author hantsy
 */
@Controller
@Slf4j
public class HomeController {

    private final PostRepository posts;

    HomeController(PostRepository posts) {
        this.posts = posts;
    }

    @GetMapping("/")
    public String home(final Model model) {

        Flux<Post> fluxPost = this.posts.findAll();
        List<Post> postList = fluxPost.collectList().block(Duration.ofDays(1));
        //log.info(" post list chuncked size::" + postList.size());
        model.addAttribute("posts", postList);
        return "home";
    }
}
