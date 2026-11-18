package br.com.medisus.api.service;

import br.com.medisus.api.api.ApiDtos.*;
import br.com.medisus.api.api.ConflictException;
import br.com.medisus.api.domain.AppUser;
import br.com.medisus.api.domain.Role;
import br.com.medisus.api.repository.AppUserRepository;
import br.com.medisus.api.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final AppUserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(AppUserRepository users, PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager, JwtService jwtService) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public UserResponse register(RegisterRequest request) {
        if (users.existsByEmailIgnoreCase(request.email())) throw new ConflictException("Email is already registered");
        AppUser user = users.save(new AppUser(request.name().trim(), request.email().trim(),
                passwordEncoder.encode(request.password()), Role.CITIZEN));
        return UserResponse.from(user);
    }

    public TokenResponse login(LoginRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        AppUser user = users.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new IllegalStateException("Authenticated user was not found"));
        return new TokenResponse(jwtService.generate(user), "Bearer", jwtService.getExpirationSeconds(),
                user.getRole(), user.getName());
    }
}
