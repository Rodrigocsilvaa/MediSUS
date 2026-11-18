package br.com.medisus.api.api;

import br.com.medisus.api.security.JwtService;
import br.com.medisus.api.security.SecurityConfiguration;
import br.com.medisus.api.security.UserDetailsServiceImpl;
import br.com.medisus.api.service.AuthService;
import br.com.medisus.api.service.UbsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {AuthController.class, UbsController.class})
@Import(SecurityConfiguration.class)
class ApiSecurityAndValidationTest {
    @Autowired private MockMvc mvc;
    @MockitoBean private AuthService authService;
    @MockitoBean private UbsService ubsService;
    @MockitoBean private JwtService jwtService;
    @MockitoBean private UserDetailsServiceImpl userDetailsService;

    @Test
    @WithMockUser(roles = "CITIZEN")
    void citizenCannotCreateUbs() throws Exception {
        mvc.perform(post("/api/ubs").contentType("application/json")
                        .content("""
                                {"name":"Central","address":"Rua A","city":"São Paulo","state":"SP"}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void invalidRegistrationUsesTheStandardErrorEnvelope() throws Exception {
        mvc.perform(post("/api/auth/register").contentType("application/json")
                        .content("""
                                {"name":"","email":"not-an-email","password":"short"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.fieldErrors.name").exists())
                .andExpect(jsonPath("$.fieldErrors.email").exists())
                .andExpect(jsonPath("$.fieldErrors.password").exists());
    }
}
