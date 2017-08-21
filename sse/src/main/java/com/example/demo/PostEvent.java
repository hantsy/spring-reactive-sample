/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;

import java.util.Date;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author hantsy
 */
@Data
@RequiredArgsConstructor
class PostEvent {
    private final Post post;
    private final Date date;
    
}
