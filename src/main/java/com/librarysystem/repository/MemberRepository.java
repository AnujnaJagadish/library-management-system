package com.librarysystem.repository;

import com.librarysystem.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

public interface MemberRepository extends JpaRepository<Member, Long> {

    // Custom query to find a member by email
    Optional<Member> findByName(String name);

    // Custom query to find members by role (admin or member)
    List<Member> findByRole(String role);

    // Custom query to find a member by email
    Optional<Member> findByEmail(String email);

    Optional<Member> findById(Long id);

    @Query("SELECT r.member FROM Reservation r WHERE r.book.id = :bookId AND r.actionType = 'BORROW'")
    List<Member> findBorrowedMembersByBookId(@Param("bookId") Long bookId);

    List<Member> findByNameContainingIgnoreCase(String name);
}
