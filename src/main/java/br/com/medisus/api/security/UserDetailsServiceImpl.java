package br.com.medisus.api.security;

import br.com.medisus.api.repository.AppUserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private final AppUserRepository users;
    public UserDetailsServiceImpl(AppUserRepository users) { this.users = users; }
    @Override public UserDetails loadUserByUsername(String username) {
        return users.findByEmailIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
