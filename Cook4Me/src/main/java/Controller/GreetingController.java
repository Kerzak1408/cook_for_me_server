package Controller;

import java.util.concurrent.atomic.AtomicLong;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GreetingController {

    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();

    @RequestMapping("/register")
    public String greeting(@RequestParam(value="name", defaultValue="name") String name, @RequestParam(value="pass", defaultValue="pass") String pass) {
        return name + " " + pass;
    }
}