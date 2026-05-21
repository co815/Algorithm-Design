package rest;

import model.Agency;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AgencySpringRepository agencyRepo;

    public AuthController(AgencySpringRepository agencyRepo) {
        this.agencyRepo = agencyRepo;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        Optional<Agency> agency = agencyRepo.findByUsernameAndPassword(username, password);
        return agency
                .<ResponseEntity<?>>map(a -> ResponseEntity.ok(Map.of("id", a.getId(), "name", a.getName())))
                .orElseGet(() -> ResponseEntity.status(401).body("Invalid credentials"));
    }
}
