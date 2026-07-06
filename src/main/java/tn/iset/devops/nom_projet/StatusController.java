package tn.iset.devops.nom_projet;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StatusController {

    @GetMapping("/")
    public String status() {
        return "Mon application Spring Boot conteneurisee avec Docker";
    }
}