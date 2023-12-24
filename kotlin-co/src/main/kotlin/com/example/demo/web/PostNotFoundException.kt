package com.example.demo.web

class PostNotFoundException(id: String) : RuntimeException("Post $id was not found")
