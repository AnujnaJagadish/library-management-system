package com.librarysystem.controller;

import com.librarysystem.dto.RegisterRequest;
import com.librarysystem.dto.SuccessResponse;
import com.librarysystem.model.Member;
import com.librarysystem.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Handles user registration, including validation and saving user data.
 */

@RestController
@RequestMapping("/api")
public class RegistrationController {

    @Autowired
    private MemberRepository memberRepository;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {
    try {
        String name = registerRequest.getName();
        String email = registerRequest.getEmail();
        String password = registerRequest.getPassword();

        // Validate input
        if (name == null || name.trim().isEmpty() || 
            email == null || email.trim().isEmpty() || 
            password == null || password.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("All fields are required.");
        }

        // Check if the name already exists
        if (memberRepository.findByName(name).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already exists.");
        }

        // Check if the email already exists
        if (memberRepository.findByEmail(email).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already exists.");
        }

        // Create and save new member
        Member newMember = new Member();
        newMember.setName(name);
        newMember.setEmail(email);
        newMember.setPassword(password);
        newMember.setRole("member");

        memberRepository.save(newMember);

        return ResponseEntity.ok(new SuccessResponse("Registration successful."));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred during registration.");
        }
    }   
    
}
