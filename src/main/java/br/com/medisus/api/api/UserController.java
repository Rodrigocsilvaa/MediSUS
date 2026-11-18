package br.com.medisus.api.api;

import br.com.medisus.api.api.ApiDtos.*;
import br.com.medisus.api.repository.AppUserRepository;
import br.com.medisus.api.service.AppUserRoleService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('MANAGER')")
public class UserController {
    private final AppUserRepository users;
    private final AppUserRoleService roleService;
    public UserController(AppUserRepository users, AppUserRoleService roleService) {
        this.users = users; this.roleService = roleService;
    }
    @GetMapping public Page<UserResponse> list(Pageable pageable) {
        return users.findAll(pageable).map(UserResponse::from);
    }
    @PutMapping("/{id}/role") public UserResponse setRole(@PathVariable long id, @Valid @RequestBody RoleRequest request) {
        return UserResponse.from(roleService.setRole(id, request.role()));
    }
}
