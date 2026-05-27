package com.orbitx.backend.domain.auth.service;

import com.orbitx.backend.domain.auth.dto.ForgotPasswordRequest;
import com.orbitx.backend.domain.auth.dto.LoginRequest;
import com.orbitx.backend.domain.auth.dto.RegisterRequest;
import com.orbitx.backend.domain.auth.entity.Company;
import com.orbitx.backend.domain.auth.entity.User;
import com.orbitx.backend.domain.auth.entity.UserRole;
import com.orbitx.backend.domain.auth.repository.CompanyRepository;
import com.orbitx.backend.domain.auth.repository.UserRepository;
import com.orbitx.backend.security.JwtService;
import com.orbitx.backend.shared.exception.OrbitXException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository       userRepository;
    @Mock CompanyRepository    companyRepository;
    @Mock PasswordEncoder      passwordEncoder;
    @Mock JwtService           jwtService;
    @Mock AuthenticationManager authenticationManager;

    @InjectMocks AuthService authService;

    @Test
    void login_returnsTokenAndUpdatesLastLogin() {
        Company company = Company.builder().id(1L).name("Orbit Corp").taxId("00.000.000/0001-00")
                .adminEmail("cto@orbitx.io").build();
        User user = User.builder().id(1L).name("CTO").email("cto@orbitx.io")
                .password("encoded").role(UserRole.ADMIN).company(company).active(true).build();

        when(userRepository.findByEmailWithCompany("cto@orbitx.io")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(anyMap(), eq(user))).thenReturn("signed.jwt.token");
        when(jwtService.getExpirationMillis()).thenReturn(86400000L);

        var response = authService.login(new LoginRequest("cto@orbitx.io", "secret123"));

        assertThat(response.accessToken()).isEqualTo("signed.jwt.token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        verify(userRepository).save(user);
    }

    @Test
    void login_propagatesBadCredentialsFromAuthenticationManager() {
        doThrow(new BadCredentialsException("Bad credentials"))
                .when(authenticationManager).authenticate(any());

        assertThatThrownBy(() -> authService.login(new LoginRequest("x@x.com", "wrong")))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void register_throwsWhenEmailAlreadyRegistered() {
        when(userRepository.existsByEmail("taken@orbitx.io")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(
                new RegisterRequest("Orbit Corp", "00.000.000/0001-00", "Admin", "taken@orbitx.io", "Pass1234!")))
                .isInstanceOf(OrbitXException.class)
                .hasMessageContaining("Email already registered");
    }

    @Test
    void register_throwsWhenTaxIdAlreadyRegistered() {
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(companyRepository.existsByTaxId("DUP-CNPJ")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(
                new RegisterRequest("Orbit Corp", "DUP-CNPJ", "Admin", "new@orbitx.io", "Pass1234!")))
                .isInstanceOf(OrbitXException.class)
                .hasMessageContaining("Tax ID");
    }

    @Test
    void forgotPassword_alwaysReturnsGenericMessageRegardlessOfEmailExistence() {
        when(userRepository.existsByEmail("ghost@orbitx.io")).thenReturn(false);

        var result = authService.forgotPassword(new ForgotPasswordRequest("ghost@orbitx.io"));

        assertThat(result).containsKey("message");
        assertThat(result.get("message")).contains("recovery link");
    }
}
