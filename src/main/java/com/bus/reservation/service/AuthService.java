package com.bus.reservation.service;

import com.bus.reservation.dtos.AuthResponse;
import com.bus.reservation.dtos.LoginRequest;
import com.bus.reservation.dtos.RegisterRequest;
import com.bus.reservation.models.User;
import com.bus.reservation.repository.UserRepository;
import com.bus.reservation.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public void register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already registered");
        }

        String role = request.getRole().toUpperCase();
        User.Role userRole = User.Role.valueOf(role);

        if (userRole == User.Role.USER) {
            if (request.getFirstName() == null || request.getLastName() == null) {
                throw new IllegalArgumentException("First and last name are required");
            }
            if (!request.getFirstName().matches("^[A-Za-z]+$")) {
                throw new IllegalArgumentException("First name must contain only letters");
            }
            if (!request.getLastName().matches("^[A-Za-z]+$")) {
                throw new IllegalArgumentException("Last name must contain only letters");
            }
        } else if (userRole == User.Role.OPERATOR) {
            if (request.getCompanyName() == null) {
                throw new IllegalArgumentException("Company name is required for operator");
            }
            if (!request.getCompanyName().matches("^[A-Za-z ]+$")) {
                throw new IllegalArgumentException("Company name must contain only letters");
            }
        } else {
            throw new IllegalArgumentException("Invalid role");
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .companyName(request.getCompanyName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(User.Role.valueOf(request.getRole().toUpperCase()))
                .isActive(true)
                .build();

        userRepository.save(user);
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String token = jwtUtil.generateToken(userDetails);

        return AuthResponse.builder()
                .token(token)
                .role(userDetails.getRole().name())
                .id(userDetails.getId())
                .email(userDetails.getEmail())
                .build();
    }

}
