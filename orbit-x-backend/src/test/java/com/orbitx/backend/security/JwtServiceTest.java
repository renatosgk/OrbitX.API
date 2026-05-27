package com.orbitx.backend.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private static final String SECRET =
            "4F72626974582D4A57542D5365637265742D4B65792D537570657253656375726521";

    private JwtService jwtService;
    private UserDetails activeUser;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", SECRET);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 86400000L);

        activeUser = User.builder()
                .username("pilot@orbitx.io")
                .password("encoded")
                .authorities(Collections.emptyList())
                .build();
    }

    @Test
    void generateToken_returnsNonBlankJwt() {
        assertThat(jwtService.generateToken(activeUser)).isNotBlank();
    }

    @Test
    void extractUsername_returnsSubjectEmbeddedInToken() {
        String token = jwtService.generateToken(activeUser);
        assertThat(jwtService.extractUsername(token)).isEqualTo("pilot@orbitx.io");
    }

    @Test
    void isTokenValid_returnsTrueForMatchingUserAndFreshToken() {
        String token = jwtService.generateToken(activeUser);
        assertThat(jwtService.isTokenValid(token, activeUser)).isTrue();
    }

    @Test
    void isTokenValid_returnsFalseWhenUsernameDoesNotMatch() {
        String token = jwtService.generateToken(activeUser);
        UserDetails other = User.builder()
                .username("impostor@orbitx.io")
                .password("x")
                .authorities(Collections.emptyList())
                .build();
        assertThat(jwtService.isTokenValid(token, other)).isFalse();
    }

    @Test
    void isTokenValid_returnsFalseForExpiredToken() {
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", -1000L);
        String token = jwtService.generateToken(activeUser);
        assertThat(jwtService.isTokenValid(token, activeUser)).isFalse();
    }
}
