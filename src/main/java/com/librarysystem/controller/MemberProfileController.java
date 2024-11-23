package com.librarysystem.controller;

import com.librarysystem.model.Member;
import com.librarysystem.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * Controller for managing member-specific profile operations.
 */
@RestController
@RequestMapping("/api/member")
public class MemberProfileController {

    @Autowired
    private MemberRepository memberRepository;

    /**
     * Fetches the profile details of a member by ID.
     *
     * @param id The ID of the member
     * @return The member details
     */
    @GetMapping("/profile/id/{id}")
    public ResponseEntity<Member> getProfileById(@PathVariable Long id) {
        Optional<Member> member = memberRepository.findById(id);
        return member.map(ResponseEntity::ok)
                     .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
    }

    /**
     * Fetches the profile details of a member by name.
     *
     * @param name The name of the member
     * @return The member details
     */
    @GetMapping("/profile/name/{name}")
    public ResponseEntity<Member> getProfileByName(@PathVariable String name) {
        Optional<Member> member = memberRepository.findByName(name);
        return member.map(ResponseEntity::ok)
                     .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
    }

    /**
     * Updates the profile of a member.
     *
     * @param member The updated member details
     * @return Success or error response
     */
    @PutMapping("/update")
    public ResponseEntity<String> updateProfile(@RequestBody Member member) {
        Optional<Member> existingMember = memberRepository.findById(member.getId());
        if (existingMember.isPresent()) {
            Member foundMember = existingMember.get();
            foundMember.setName(member.getName());
            foundMember.setEmail(member.getEmail());
            foundMember.setPassword(member.getPassword());
            memberRepository.save(foundMember);
            return ResponseEntity.ok("Profile updated successfully.");
        } else {
            return ResponseEntity.badRequest().body("Member not found.");
        }
    }

}
