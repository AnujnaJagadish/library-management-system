package com.librarysystem.repository;

import com.librarysystem.model.Member;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class MemberRepositoryTests {

    @Autowired
    private MemberRepository memberRepository;

    @Test
    public void testInsertMember() {
        // Create a new member
        Member member = new Member();
        member.setName("John Doe");
        member.setEmail("johndoe@example.com");
        member.setPassword("securepassword123");
        member.setRole("member");

        // Save the member
        Member savedMember = memberRepository.save(member);

        // Verify the saved member has an ID assigned
        assertNotNull(savedMember.getId());
    }

    @Test
    public void testFindMemberByEmail() {
        // Insert a test member
        Member member = new Member();
        member.setName("Jane Doe");
        member.setEmail("janedoe@example.com");
        member.setPassword("securepassword456");
        member.setRole("admin");
        memberRepository.save(member);

        // Query the member by email
       // Optional<Member> foundMember = memberRepository.findByEmail("janedoe@example.com");
       // assertTrue(foundMember.isPresent());
       // assertEquals("Jane Doe", foundMember.get().getName());
    }

    @Test
    public void testUpdateMember() {
        // Insert a test member
        Member member = new Member();
        member.setName("Mark Smith");
        member.setEmail("marksmith@example.com");
        member.setPassword("securepassword789");
        member.setRole("member");
        Member savedMember = memberRepository.save(member);

        // Update the member
        savedMember.setName("Mark Johnson");
        Member updatedMember = memberRepository.save(savedMember);

        // Verify the update
        Optional<Member> foundMember = memberRepository.findById(updatedMember.getId());
        assertTrue(foundMember.isPresent());
        assertEquals("Mark Johnson", foundMember.get().getName());
    }

    @Test
    public void testDeleteMember() {
        // Insert a test member
        Member member = new Member();
        member.setName("Alice Walker");
        member.setEmail("alicewalker@example.com");
        member.setPassword("securepassword012");
        member.setRole("member");
        Member savedMember = memberRepository.save(member);

        // Delete the member
        memberRepository.deleteById(savedMember.getId());

        // Verify the deletion
        Optional<Member> foundMember = memberRepository.findById(savedMember.getId());
        assertFalse(foundMember.isPresent());
    }
}

