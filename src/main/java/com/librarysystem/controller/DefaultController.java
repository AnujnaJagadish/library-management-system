package com.librarysystem.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Provides default routes or fallback endpoints for the application.
 */

@RestController
public class DefaultController {

    @GetMapping("/")
    public String home() {
        return "Welcome to the Library Management System!";
    }
}

