package egovframework.example.common.web;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** Shared application shell; private data is fetched only through authenticated domain APIs. */
@Controller
public class WorkbenchController {
    private final Environment environment;
    public WorkbenchController(Environment environment) { this.environment = environment; }
    @GetMapping("/")
    public String index(Model model, HttpServletResponse response) {
        model.addAttribute("mock", environment.acceptsProfiles(Profiles.of("test")));
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Content-Security-Policy", "default-src 'self'; script-src 'self'; style-src 'self'; connect-src 'self'; img-src 'none'; object-src 'none'; base-uri 'none'; frame-ancestors 'none'; form-action 'self'");
        response.setHeader("Referrer-Policy", "no-referrer");
        return "workbench";
    }
}
