package com.example.telemetry.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/orders")
    public String orders() {
        return "order";
    }

    @GetMapping("/faults")
    public String faults() {
        return "fault";
    }
}
