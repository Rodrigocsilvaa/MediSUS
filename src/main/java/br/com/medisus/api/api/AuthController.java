package br.com.medisus.api.api;

import br.com.medisus.api.api.ApiDtos.*;
import br.com.medisus.api.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService auth;
    public AuthController(AuthService auth) { this.auth = auth; }

    @PostMapping("/register")
    @SecurityRequirements
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequest request) { return auth.register(request); }

    @PostMapping("/login")
    @SecurityRequirements
    public TokenResponse login(@Valid @RequestBody LoginRequest request) { return auth.login(request); }
}
