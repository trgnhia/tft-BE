package org.example.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@Slf4j
public class WsTestController {

    @MessageMapping("/test")
    public void test(Principal principal) {
        log.info("WS principal name = {}", principal.getName());
    }
}