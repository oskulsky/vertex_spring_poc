package com.example.springdemo.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class DemoController {

    @PostMapping(value = "/postDemo")
    @ResponseStatus(HttpStatus.CREATED)
    public HttpStatus postDemo(){
        return HttpStatus.CREATED;
    }
}
