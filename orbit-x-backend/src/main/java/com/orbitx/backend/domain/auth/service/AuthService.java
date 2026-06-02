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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
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
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

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

    @Transactional
    public Map<String, String> forgotPassword(ForgotPasswordRequest request) {
        userRepository.findByEmail(request.email()).ifPresent(user -> {
            String tempPassword = generateTempPassword();
            user.setPassword(passwordEncoder.encode(tempPassword));
            userRepository.save(user);
            sendTempPasswordEmail(user.getEmail(), user.getName(), tempPassword);
            log.info("Senha temporária gerada e enviada para: {}", user.getEmail());
        });

        return Map.of(
                "message", "Se este e-mail estiver cadastrado, uma senha temporária foi enviada.",
                "requestId", "reset-" + System.currentTimeMillis()
        );
    }

    private String generateTempPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$!";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(10);
        for (int i = 0; i < 10; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private void sendTempPasswordEmail(String to, String name, String tempPassword) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Orbit X — Sua senha temporária");
            message.setText(
                    "Olá, " + name + "!\n\n" +
                    "Recebemos uma solicitação de redefinição de senha para sua conta Orbit X.\n\n" +
                    "Sua senha temporária é: " + tempPassword + "\n\n" +
                    "Use esta senha para fazer login e altere-a em seguida.\n\n" +
                    "Se você não solicitou esta redefinição, ignore este e-mail.\n\n" +
                    "— Equipe Orbit X"
            );
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Falha ao enviar e-mail de recuperação para {}: {}", to, e.getMessage());
        }
    }

    private Map<String, Object> buildClaims(User user) {
        return Map.of(
                "role", user.getRole().name(),
                "companyId", user.getCompany().getId(),
                "companyName", user.getCompany().getName()
        );
    }
}
