package tn.esprit.exam.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping("/")
    public String index() {
        return "redirect:/index.html";
    }

    @GetMapping("/admin")
    public String admin() {
        return "redirect:/backoffice/dashboard.html";
    }
}
