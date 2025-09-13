package com.alimmit.golf.example;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/hello")
class HelloController {

    @GetMapping
    String hello() {
        return "hello";
    }
}
