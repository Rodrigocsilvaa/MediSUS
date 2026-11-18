package br.com.medisus.api.service;

import br.com.medisus.api.domain.AppUser;
import br.com.medisus.api.domain.Role;
import br.com.medisus.api.repository.AppUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BootstrapManager implements ApplicationRunner {
    private final AppUserRepository users;
    private final PasswordEncoder passwords;
    @Value("${medisus.bootstrap.manager-email:}") private String email;
    @Value("${medisus.bootstrap.manager-password:}") private String password;

    public BootstrapManager(AppUserRepository users, PasswordEncoder passwords) {
        this.users = users; this.passwords = passwords;
    }
    @Override public void run(ApplicationArguments args) {
        if (email.isBlank() && password.isBlank()) return;
        if (email.isBlank() || password.length() < 12 || password.length() > 72) {
            throw new IllegalStateException("Configure both bootstrap manager variables; password must be 12-72 characters");
        }
        var existing = users.findByEmailIgnoreCase(email);
        if (existing.isPresent()) {
            if (existing.get().getRole() != Role.MANAGER) {
                throw new IllegalStateException("Bootstrap manager email is already assigned to a non-manager account");
            }
            return;
        }
        users.save(new AppUser("MediSUS Manager", email, passwords.encode(password), Role.MANAGER));
    }
}
