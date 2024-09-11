package org.ugonna.springboot.keycloak.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.ugonna.springboot.keycloak.dto.RegisterRequest;
import org.ugonna.springboot.keycloak.service.RegisterService;

@RestController
public class RegisterController {

    @Autowired
    private RegisterService registerService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        return registerService.register(request);
    }
}
