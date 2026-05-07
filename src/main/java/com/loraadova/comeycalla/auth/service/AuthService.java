package com.loraadova.comeycalla.auth.service;

import com.loraadova.comeycalla.auth.dto.AuthResponse;
import com.loraadova.comeycalla.auth.dto.LoginRequest;
import com.loraadova.comeycalla.auth.dto.RegisterRequest;
import com.loraadova.comeycalla.auth.mapper.AuthMapper;
import com.loraadova.comeycalla.auth.security.JwtService;
import com.loraadova.comeycalla.user.entity.UserEntity;
import com.loraadova.comeycalla.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthMapper authMapper;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("El email ya está registrado");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("El nombre de usuario ya existe");
        }

        UserEntity user = authMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        UserEntity savedUser = userRepository.save(user);
        String token = jwtService.generateToken(savedUser.getEmail());

        return authMapper.toAuthResponse(savedUser, token);
    }

    public AuthResponse login(LoginRequest request) {
        UserEntity user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Credenciales incorrectas"));

        boolean passwordMatches = passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        );

        if (!passwordMatches) {
            throw new BadCredentialsException("Credenciales incorrectas");
        }

        String token = jwtService.generateToken(user.getEmail());


        return authMapper.toAuthResponse(user, token);
    }

}
