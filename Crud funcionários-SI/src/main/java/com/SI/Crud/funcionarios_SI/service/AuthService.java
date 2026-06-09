package com.SI.Crud.funcionarios_SI.service;

import com.SI.Crud.funcionarios_SI.model.dto.request.LoginRequest;
import com.SI.Crud.funcionarios_SI.model.dto.request.RegisterRequest;
import com.SI.Crud.funcionarios_SI.model.dto.response.AuthResponse;
import com.SI.Crud.funcionarios_SI.model.entity.User;
import com.SI.Crud.funcionarios_SI.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Este email ja esta registado.");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(userRepository.count() == 0 ? "ROLE_ADMIN" : "ROLE_USUARIO")
                .build();

        User savedUser = userRepository.save(user);
        String token = jwtService.generateToken(savedUser);
        return new AuthResponse(token);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Credenciais invalidas"));
        String token = jwtService.generateToken(user);
        return new AuthResponse(token);
    }
}
