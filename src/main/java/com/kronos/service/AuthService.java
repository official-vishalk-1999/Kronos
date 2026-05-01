package com.kronos.service;

import com.kronos.dto.request.RequestDTOs;
import com.kronos.dto.response.ResponseDTOs;
import com.kronos.entity.User;
import com.kronos.exception.ValidationException;
import com.kronos.repository.UserRepository;
import com.kronos.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private AuthenticationManager authenticationManager;

    public ResponseDTOs.AuthResponse signup(RequestDTOs.SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ValidationException("Email already in use");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(User.Role.USER);
        user.setEnabled(true);
        userRepository.save(user);

        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        String token = jwtUtil.generateToken(userDetails, "USER");

        return new ResponseDTOs.AuthResponse(token, "USER", user.getName(), user.getEmail());
    }

    public ResponseDTOs.AuthResponse login(RequestDTOs.LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        UserDetails userDetails = (UserDetails) auth.getPrincipal();

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ValidationException("User not found"));

        String role = user.getRole().name();
        String token = jwtUtil.generateToken(userDetails, role);
        return new ResponseDTOs.AuthResponse(token, role, user.getName(), user.getEmail());
    }

    public ResponseDTOs.AuthResponse adminLogin(RequestDTOs.AdminLoginRequest request) {
        if (!"admin".equals(request.getUsername()) || !"admin".equals(request.getPassword())) {
            throw new ValidationException("Invalid admin credentials");
        }

        org.springframework.security.core.userdetails.UserDetails adminDetails =
                org.springframework.security.core.userdetails.User.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("admin"))
                        .authorities("ROLE_ADMIN")
                        .build();

        String token = jwtUtil.generateToken(adminDetails, "ADMIN");
        return new ResponseDTOs.AuthResponse(token, "ADMIN", "Administrator", "admin");
    }
}
