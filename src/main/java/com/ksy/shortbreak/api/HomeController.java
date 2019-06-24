package com.ksy.shortbreak.api;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

public @Controller class HomeController {
    @GetMapping({"/", "/index", "/home"})
    public String index(Model model) {
        return "home";
    }
}