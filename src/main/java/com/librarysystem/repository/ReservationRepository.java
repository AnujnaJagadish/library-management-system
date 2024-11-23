package com.librarysystem.repository;

import com.librarysystem.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // Custom query to find reservations by member ID
    List<Reservation> findByMemberId(Long memberId);

    // Custom query to find reservations by book ID
    List<Reservation> findByBookId(Long bookId);

    // Custom query to find reservations by action type (reserve, borrow, return)
    List<Reservation> findByActionType(String actionType);

    Optional<Reservation> findById(Long id);

        /**
     * Find all reservations with ACTION_TYPE = 'RESERVE'.
     */
    @Query("SELECT r FROM Reservation r WHERE r.actionType = 'RESERVE'")
    List<Reservation> findPendingReservations();


    @Query("SELECT r FROM Reservation r WHERE r.actionType = 'BORROW'")
    List<Reservation> findApprovalReservations();

    /**
     * Find reservations by action type and member ID.
     */
    @Query("SELECT r FROM Reservation r WHERE r.actionType = :actionType AND r.member.id = :memberId")
    List<Reservation> findByActionTypeAndMemberId(Reservation.ActionType actionType, Long memberId);
    

        @Query("SELECT r FROM Reservation r WHERE " +
           "LOWER(r.member.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(r.book.title) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Reservation> findByMemberNameOrBookTitle(@Param("query") String query);

        
    @Query("SELECT r FROM Reservation r WHERE r.book.id = :bookId")
    List<Reservation> findByBooksId(@Param("bookId") Long bookId);
    
}
