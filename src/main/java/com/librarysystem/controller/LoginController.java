package com.librarysystem.controller;

import com.librarysystem.dto.LoginRequest;
import com.librarysystem.model.Member;
import com.librarysystem.repository.MemberRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

/**
 * Manages user authentication, including login and role-based access.
 **/

@RestController
@RequestMapping("/api/auth")
public class LoginController {

    @Autowired
    private MemberRepository memberRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();

        // Validate input
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Please provide both username and password.");
        }

        // Find the user by name
        Optional<Member> member = memberRepository.findByName(username);
        if (member.isPresent()) {
            Member foundMember = member.get();
            // Validate the password
            if (foundMember.getPassword().equals(password)) {
                // Include the member's name and role in the response
                return ResponseEntity.ok(Map.of(
                    "role", foundMember.getRole(),
                    "name", foundMember.getName(),
                    "Id", foundMember.getId() // Add member's name here
                ));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                     .body(Map.of("message", "Invalid password."));
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                 .body(Map.of("message", "Invalid username."));
        }
    }
}
