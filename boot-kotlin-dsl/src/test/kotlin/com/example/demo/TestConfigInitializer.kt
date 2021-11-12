package com.example.demo

import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.support.GenericApplicationContext

class TestConfigInitializer: ApplicationContextInitializer<GenericApplicationContext>{
    override fun initialize(applicationContext: GenericApplicationContext) {
       beans().initialize(applicationContext)
    }
}