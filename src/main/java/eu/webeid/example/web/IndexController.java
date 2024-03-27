package eu.webeid.example.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {
    @GetMapping("/")
    public String welcome(Model model, HttpServletRequest request) {
        model.addAttribute("serverName", request.getServerName());
        return "index";
    }
}
