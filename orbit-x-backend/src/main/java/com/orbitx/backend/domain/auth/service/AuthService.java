package com.orbitx.backend.domain.auth.service;

import com.orbitx.backend.domain.auth.dto.*;
import com.orbitx.backend.domain.auth.entity.Company;
import com.orbitx.backend.domain.auth.entity.User;
import com.orbitx.backend.domain.auth.entity.UserRole;
import com.orbitx.backend.domain.auth.repository.CompanyRepository;
import com.orbitx.backend.domain.auth.repository.UserRepository;
import com.orbitx.backend.security.JwtService;
import com.orbitx.backend.shared.exception.OrbitXException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = userRepository.findByEmailWithCompany(request.email())
                .orElseThrow(() -> new OrbitXException("Usuário não encontrado"));

        user.setLastLogin(Instant.now());
        userRepository.save(user);

        String token = jwtService.generateToken(buildClaims(user), user);
        log.info("Usuário autenticado: {}", user.getEmail());
        return AuthResponse.of(token, jwtService.getExpirationMillis(), UserProfileDto.from(user));
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new OrbitXException("E-mail já cadastrado: " + request.email());
        }
        if (companyRepository.existsByTaxId(request.taxId())) {
            throw new OrbitXException("Empresa já cadastrada com o CNPJ: " + request.taxId());
        }

        Company company = companyRepository.save(
                Company.builder()
                        .name(request.companyName())
                        .taxId(request.taxId())
                        .adminEmail(request.email())
                        .build()
        );

        User user = userRepository.save(
                User.builder()
                        .name(request.adminName())
                        .email(request.email())
                        .password(passwordEncoder.encode(request.password()))
                        .role(UserRole.ADMIN)
                        .company(company)
                        .build()
        );

        String token = jwtService.generateToken(buildClaims(user), user);
        log.info("Nova conta empresarial registrada: {} / {}", request.companyName(), request.email());
        return AuthResponse.of(token, jwtService.getExpirationMillis(), UserProfileDto.from(user));
    }

    public Map<String, String> forgotPassword(ForgotPasswordRequest request) {

        boolean exists = userRepository.existsByEmail(request.email());
        log.info("Recuperação de senha solicitada para: {} (existe={})", request.email(), exists);

        return Map.of(
                "message", "Se este e-mail estiver cadastrado, um link de recuperação foi enviado.",
                "requestId", "reset-" + System.currentTimeMillis()
        );
    }

    private Map<String, Object> buildClaims(User user) {
        return Map.of(
                "role", user.getRole().name(),
                "companyId", user.getCompany().getId(),
                "companyName", user.getCompany().getName()
        );
    }
}
