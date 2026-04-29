package com.loraadova.comeycalla.auth.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/api/test/private")
    public String privateEndpoint() {
        return "Acceso permitido con JWT";
    }
}