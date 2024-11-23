package com.librarysystem.controller;

import com.librarysystem.model.Member;
import com.librarysystem.repository.MemberRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
public class MemberController {

    @Autowired
    private MemberRepository memberRepository;

    /**
     * Add a new member (admin or user).
     *
     * @param member The member details
     * @return Success or error message
     */
    @PostMapping("/addMember")
    public ResponseEntity<String> addMember(@RequestBody Member member) {
        // Validate fields
        if (member.getName() == null || member.getName().trim().isEmpty() ||
            member.getEmail() == null || member.getEmail().trim().isEmpty() ||
            member.getPassword() == null || member.getPassword().trim().isEmpty() ||
            member.getRole() == null || member.getRole().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("All fields are required.");
        }

        // Check if email already exists
        boolean emailExists = memberRepository.findAll().stream()
                .anyMatch(existingMember -> existingMember.getEmail().equalsIgnoreCase(member.getEmail()));

        if (emailExists) {
            return ResponseEntity.badRequest().body("Email already exists.");
        }

        if (memberRepository.findByName(member.getName()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already exists.");
        }

        // Save the new member
        memberRepository.save(member);
        return ResponseEntity.ok("Member added successfully.");
    }

      /**
     * Search for members by name or ID.
     *
     * @param query The search query (name or ID)
     * @return List of matching members
     */
    @GetMapping("/members/search")
    public ResponseEntity<List<Member>> searchMembers(@RequestParam("query") String query) {
        List<Member> members;
        try {
            if (query.matches("\\d+")) { // Check if query is numeric (ID)
                Long id = Long.parseLong(query);
                Optional<Member> member = memberRepository.findById(id);
                members = member.map(List::of).orElseGet(List::of);
            } else { // Otherwise, search by name (ignore case)
                members = memberRepository.findByNameContainingIgnoreCase(query);
            }
            return ResponseEntity.ok(members);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Update a member's details.
     *
     * @param updatedMember The updated member details
     * @return Response message
     */
    @PutMapping("/members/update")
    public ResponseEntity<List<Member>> updateMember(@RequestBody Member updatedMember, 
    @RequestParam(value = "query", required = false) String query) {
    Member existingMember = memberRepository.findById(updatedMember.getId())
    .orElseThrow(() -> new IllegalArgumentException("Member not found"));

    // Update member details
    existingMember.setName(updatedMember.getName());
    existingMember.setEmail(updatedMember.getEmail());
    existingMember.setRole(updatedMember.getRole());
    memberRepository.save(existingMember);

    // Fetch the filtered members if a query is provided
    List<Member> members = (query != null && !query.trim().isEmpty())
    ? memberRepository.findByNameContainingIgnoreCase(query)
    : memberRepository.findAll();

    return ResponseEntity.ok(members);
    }

    /**
     * Delete a member by ID.
     *
     * @param id The member ID
     * @return Response message
     */
    @DeleteMapping("/members/delete/{id}")
    public ResponseEntity<List<Member>> deleteMember(@PathVariable Long id, @RequestParam(value = "query", required = false) String query) {
        if (!memberRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        memberRepository.deleteById(id);

        // Fetch the filtered members if a query is provided
        List<Member> members = (query != null && !query.trim().isEmpty())
            ? memberRepository.findByNameContainingIgnoreCase(query)
            : memberRepository.findAll();

        return ResponseEntity.ok(members);
    }
}
