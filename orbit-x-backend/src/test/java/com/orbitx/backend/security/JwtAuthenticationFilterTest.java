package com.orbitx.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock JwtService          jwtService;
    @Mock UserDetailsService  userDetailsService;
    @Mock HttpServletRequest  request;
    @Mock HttpServletResponse response;
    @Mock FilterChain         filterChain;

    @InjectMocks JwtAuthenticationFilter filter;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void requestWithoutAuthorizationHeader_passesWithNoAuthentication() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void requestWithValidToken_setsAuthenticationInSecurityContext() throws Exception {
        UserDetails user = activeUser("active@orbitx.io", true);
        when(request.getHeader("Authorization")).thenReturn("Bearer valid.jwt.token");
        when(jwtService.extractUsername("valid.jwt.token")).thenReturn("active@orbitx.io");
        when(userDetailsService.loadUserByUsername("active@orbitx.io")).thenReturn(user);
        when(jwtService.isTokenValid("valid.jwt.token", user)).thenReturn(true);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName())
                .isEqualTo("active@orbitx.io");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void requestWithValidTokenButInactiveUser_doesNotSetAuthentication() throws Exception {
        UserDetails inactive = activeUser("inactive@orbitx.io", false);
        when(request.getHeader("Authorization")).thenReturn("Bearer valid.jwt.token");
        when(jwtService.extractUsername("valid.jwt.token")).thenReturn("inactive@orbitx.io");
        when(userDetailsService.loadUserByUsername("inactive@orbitx.io")).thenReturn(inactive);
        when(jwtService.isTokenValid("valid.jwt.token", inactive)).thenReturn(true);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void requestWithExpiredToken_doesNotSetAuthentication() throws Exception {
        UserDetails user = activeUser("active@orbitx.io", true);
        when(request.getHeader("Authorization")).thenReturn("Bearer expired.token");
        when(jwtService.extractUsername("expired.token")).thenReturn("active@orbitx.io");
        when(userDetailsService.loadUserByUsername("active@orbitx.io")).thenReturn(user);
        when(jwtService.isTokenValid("expired.token", user)).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    private UserDetails activeUser(String username, boolean enabled) {
        return User.builder()
                .username(username)
                .password("encoded")
                .disabled(!enabled)
                .authorities(Collections.emptyList())
                .build();
    }
}
