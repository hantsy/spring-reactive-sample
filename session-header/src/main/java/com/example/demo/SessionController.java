/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;

import java.util.HashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.WebSession;

/**
 *
 * @author hantsy
 */
@RestController
public class SessionController {

    
    @GetMapping("/sessionId")
    public Map<String, String> sessionId(WebSession session){
        Map<String, String> map = new HashMap<>();
        map.put("id", session.getId());
       return map ;
    }

}
