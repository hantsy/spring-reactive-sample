/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.thymeleaf.spring6.context.webflux.ReactiveDataDriverContextVariable;
import reactor.core.publisher.Flux;

/**
 * @author hantsy
 */
@Controller
@Slf4j
@RequiredArgsConstructor
public class HomeController {

  private final PostRepository posts;

  @GetMapping("/home")
  public String home(final Model model) {

    Flux<Post> postList = this.posts.findAll();
    model.addAttribute("posts", new ReactiveDataDriverContextVariable(postList, 100));
    return "home";
  }

}
