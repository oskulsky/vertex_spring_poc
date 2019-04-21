package com.finastra.gateway.controller;

import com.finastra.gateway.common.messages.GetAccountRequestMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("oby")
@Slf4j
public class TestyController {

    @PostMapping("trance")
    public String obyTrance(@RequestParam(value = "param", required = false) String param,
                            @RequestParam(value = "param2", required = false) String param2,
                            @RequestBody GetAccountRequestMessage body) {
        log.info("Param={}, body={}", param, body);
        return "OK: " + param;
    }

}
