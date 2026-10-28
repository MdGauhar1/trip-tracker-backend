
package com.triptracker.auth;

import com.triptracker.auth.dto.RegisterRequest;
import com.triptracker.auth.dto.LoginRequest;
import com.triptracker.auth.dto.LoginResponse;
import com.triptracker.user.User;
import com.triptracker.user.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    // REGISTER AND AUTOMATICALLY AUTHENTICATE USER
    public LoginResponse register(RegisterRequest request) {

        if (request.getName() == null || request.getName().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Name is required"
            );
        }

        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Email is required"
            );
        }

        if (request.getPassword() == null
                || request.getPassword().length() < 8) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Password must contain at least 8 characters"
            );
        }

        String email = request.getEmail()
                .trim()
                .toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email is already registered"
            );
        }

        User user = new User();

        user.setName(request.getName().trim());
        user.setEmail(email);

        // Never store the raw password
        user.setPasswordHash(
                passwordEncoder.encode(request.getPassword())
        );

        // Save the user first so the database ID is available
        User savedUser = userRepository.save(user);

        // Generate JWT for the newly registered user
        String token = jwtService.generateToken(
                savedUser.getEmail(),
                savedUser.getId()
        );

        // Return the same response structure as login
        return new LoginResponse(
                token,
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail()
        );
    }

    // LOGIN
    public LoginResponse login(LoginRequest request) {

        if (request.getEmail() == null
                || request.getEmail().isBlank()
                || request.getPassword() == null
                || request.getPassword().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Email and password are required"
            );
        }

        String email = request.getEmail()
                .trim()
                .toLowerCase();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Invalid email or password"
                ));

        boolean passwordMatches = passwordEncoder.matches(
                request.getPassword(),
                user.getPasswordHash()
        );

        if (!passwordMatches) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid email or password"
            );
        }

        String token = jwtService.generateToken(
                user.getEmail(),
                user.getId()
        );

        return new LoginResponse(
                token,
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }
}









//package com.triptracker.auth;
//
//import com.triptracker.auth.dto.RegisterRequest;
//import com.triptracker.auth.dto.RegisterResponse;
//import com.triptracker.auth.dto.LoginRequest;
//import com.triptracker.auth.dto.LoginResponse;
//import com.triptracker.user.User;
//import com.triptracker.user.UserRepository;
//
//import org.springframework.http.HttpStatus;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//import org.springframework.web.server.ResponseStatusException;
//
//@Service
//public class AuthService {
//
//    private final UserRepository userRepository;
//    private final PasswordEncoder passwordEncoder;
//    private final JwtService jwtService;
//
//    public AuthService(
//            UserRepository userRepository,
//            PasswordEncoder passwordEncoder,
//            JwtService jwtService) {
//
//        this.userRepository = userRepository;
//        this.passwordEncoder = passwordEncoder;
//        this.jwtService = jwtService;
//    }
//
//    // register method service
//    public RegisterResponse register(RegisterRequest request) {
//
//        if (request.getName() == null || request.getName().isBlank()) {
//            throw new ResponseStatusException(
//                    HttpStatus.BAD_REQUEST,
//                    "Name is required"
//            );
//        }
//
//        if (request.getEmail() == null || request.getEmail().isBlank()) {
//            throw new ResponseStatusException(
//                    HttpStatus.BAD_REQUEST,
//                    "Email is required"
//            );
//        }
//
//        if (request.getPassword() == null || request.getPassword().length() < 8) {
//            throw new ResponseStatusException(
//                    HttpStatus.BAD_REQUEST,
//                    "Password must contain at least 8 characters"
//            );
//        }
//
//        String email = request.getEmail().trim().toLowerCase();
//
//        if (userRepository.existsByEmail(email)) {
//            throw new ResponseStatusException(
//                    HttpStatus.CONFLICT,
//                    "Email is already registered"
//            );
//        }
//
//        User user = new User();
//
//        user.setName(request.getName().trim());
//        user.setEmail(email);
//        user.setPasswordHash(
//                passwordEncoder.encode(request.getPassword())
//        );
//
//        User savedUser = userRepository.save(user);
//
//        return new RegisterResponse(
//                savedUser.getId(),
//                savedUser.getName(),
//                savedUser.getEmail(),
//                "User registered successfully"
//        );
//    }
//
//
//    // login method service
//    public LoginResponse login(LoginRequest request) {
//
//        if (request.getEmail() == null || request.getEmail().isBlank()
//                || request.getPassword() == null
//                || request.getPassword().isBlank()) {
//
//            throw new ResponseStatusException(
//                    HttpStatus.BAD_REQUEST,
//                    "Email and password are required"
//            );
//        }
//
//        String email = request.getEmail().trim().toLowerCase();
//
//        User user = userRepository.findByEmail(email)
//                .orElseThrow(() -> new ResponseStatusException(
//                        HttpStatus.UNAUTHORIZED,
//                        "Invalid email or password"
//                ));
//
//        boolean passwordMatches = passwordEncoder.matches(
//                request.getPassword(),
//                user.getPasswordHash()
//        );
//
//        if (!passwordMatches) {
//            throw new ResponseStatusException(
//                    HttpStatus.UNAUTHORIZED,
//                    "Invalid email or password"
//            );
//        }
//
//        String token = jwtService.generateToken(
//                user.getEmail(),
//                user.getId()
//        );
//
//        return new LoginResponse(
//                token,
//                user.getId(),
//                user.getName(),
//                user.getEmail()
//        );
//    }
//
//}