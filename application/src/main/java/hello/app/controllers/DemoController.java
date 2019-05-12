package hello.app.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoController {

    @RequestMapping(path = "/demo")
    public String demo() {
        return "Greetings from Spring Boot!";
    }
}
