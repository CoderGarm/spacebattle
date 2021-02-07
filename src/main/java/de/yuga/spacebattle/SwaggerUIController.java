package de.yuga.spacebattle;

import org.springframework.web.bind.annotation.RequestMapping;

/**
 * comment in for URL /swagger redirect to not-working-/swagger-ui.html *snarf*
 * possible vaadin conflict?
 */
//@Controller
public class SwaggerUIController {
    @RequestMapping("/swagger")
    public String home() {
        return "redirect:/swagger-ui.html";
    }
}
