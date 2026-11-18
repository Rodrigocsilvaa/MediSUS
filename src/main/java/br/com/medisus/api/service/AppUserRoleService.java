package br.com.medisus.api.service;

import br.com.medisus.api.api.ResourceNotFoundException;
import br.com.medisus.api.domain.AppUser;
import br.com.medisus.api.domain.Role;
import br.com.medisus.api.repository.AppUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AppUserRoleService {
    private final AppUserRepository users;
    public AppUserRoleService(AppUserRepository users) { this.users = users; }
    @Transactional
    public AppUser setRole(long id, Role role) {
        AppUser user = users.findById(id).orElseThrow(() -> new ResourceNotFoundException("User", id));
        user.setRole(role);
        return users.save(user);
    }
}
