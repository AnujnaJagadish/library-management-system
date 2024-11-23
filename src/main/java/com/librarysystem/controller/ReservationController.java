package com.librarysystem.controller;

import com.librarysystem.model.Book;
import com.librarysystem.model.Member;
import com.librarysystem.model.Reservation;
import com.librarysystem.repository.BookRepository;
import com.librarysystem.repository.MemberRepository;
import com.librarysystem.repository.ReservationRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller for managing reservations.
 */
@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private MemberRepository memberRepository;

    Logger logger = LoggerFactory.getLogger(ReservationController.class);

    /**
     * Handles adding a new reservation.
     *
     * @param request Map containing bookId, memberId, and actionType
     * @return ResponseEntity with success or failure message
     */
    @PostMapping("/add")
    public ResponseEntity<String> reserveBook(@RequestBody Map<String, Object> request) {
        try {
            Long bookId = ((Number) request.get("bookId")).longValue();
            Long memberId = ((Number) request.get("memberId")).longValue();
            String actionTypeString = ((String) request.get("actionType")).toLowerCase(); // Convert to lowercase
            Logger logger = LoggerFactory.getLogger(ReservationController.class);
            logger.info("Action String: {}", actionTypeString);

            // Validate the action type
            if (!actionTypeString.equals("reserve") &&
                !actionTypeString.equals("borrow") &&
                !actionTypeString.equals("return")) {
                return ResponseEntity.badRequest().body("Invalid action type: " + actionTypeString);
            }

            // Fetch the book and member
            Book book = bookRepository.findById(bookId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid book ID: " + bookId));
            Member member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid member ID: " + memberId));

            // Create and save the reservation
            Reservation reservation = new Reservation();
            reservation.setBook(book);
            reservation.setMember(member);
            reservation.setActionType(Reservation.ActionType.valueOf(actionTypeString.toUpperCase())); // Match enum
            reservation.setReservedDate(LocalDateTime.now());

            reservationRepository.save(reservation);
            
            // Decrement book count and increment reserved count
            book.setCount(book.getCount() - 1);
            book.setReservedCount(book.getReservedCount() + 1);

            bookRepository.save(book);
            return ResponseEntity.ok("Reservation added successfully.");
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.badRequest().body("Error occurred while processing reservation: " + ex.getMessage());
        }
    }

     /**
     * Fetch all pending reservations (ACTION_TYPE = 'RESERVE').
     *
     * @return List of pending reservations.
     */
    @GetMapping("/pending")
    public ResponseEntity<List<Reservation>> getPendingReservations() {
        List<Reservation> reservations = reservationRepository.findPendingReservations();
        return ResponseEntity.ok(reservations);
    }

    /**
     * Approve a borrow request.
     *
     * @param reservationId ID of the reservation to approve.
     * @return Success or error response.
     */
    @PostMapping("/approve")
    public ResponseEntity<String> approveBorrow(@RequestParam Long reservationId) {
        Optional<Reservation> reservationOpt = reservationRepository.findById(reservationId);

        if (reservationOpt.isPresent()) {
            Reservation reservation = reservationOpt.get();
            Book book = reservation.getBook();

            // Ensure the book is in reserved state and can be borrowed
            if (book.getReservedCount() > 0) {
                // Update reservation
                reservation.setActionType(Reservation.ActionType.BORROW);
                reservation.setBorrowedDate(LocalDateTime.now());
                reservation.setDueDate(LocalDateTime.now().plusDays(14)); // Example: 14-day due period
                reservationRepository.save(reservation);

                // Update book counts
                book.setReservedCount(book.getReservedCount() - 1);
                book.setBorrowedCount(book.getBorrowedCount() + 1);
                bookRepository.save(book);

                return ResponseEntity.ok("Borrow approved successfully.");
            } else {
                return ResponseEntity.badRequest().body("Book is not available for borrowing.");
            }
        }

        return ResponseEntity.badRequest().body("Reservation not found.");
    }

    /**
     * Reject a reservation request.
     *
     * @param reservationId ID of the reservation to reject.
     * @return Success or error response.
     */
    @DeleteMapping("/cancel")
    public ResponseEntity<String> cancelReservation(@RequestParam("reservationId") Long reservationId) {
        Optional<Reservation> reservationOpt = reservationRepository.findById(reservationId);
    
        if (reservationOpt.isPresent()) {
            Reservation reservation = reservationOpt.get();
            Book book = reservation.getBook();
    
            // Update book counts
            book.setCount(book.getCount() + 1);
            book.setReservedCount(book.getReservedCount() - 1);
            bookRepository.save(book);
    
            // Delete reservation
            reservationRepository.delete(reservation);
    
            return ResponseEntity.ok("Reservation canceled successfully.");
        }
    
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Reservation not found.");
    }
        /**
     * Approve return for a reservation.
     */
    /**
     * Approve return for a reservation.
     */
    @PostMapping("/approveReturn")
    public ResponseEntity<String> approveReturn(@RequestParam("reservationId") Long reservationId) {
        Optional<Reservation> reservationOpt = reservationRepository.findById(reservationId);

        if (reservationOpt.isPresent()) {
            Reservation reservation = reservationOpt.get();
            if (!reservation.getActionType().equals(Reservation.ActionType.BORROW)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Reservation is not in borrow state.");
            }

            Book book = reservation.getBook();

            // Update book counts
            book.setBorrowedCount(book.getBorrowedCount() - 1);
            book.setCount(book.getCount() + 1);
            bookRepository.save(book);

            // Mark reservation as returned
            reservation.setActionType(Reservation.ActionType.RETURN);
            reservation.setReturnedDate(LocalDateTime.now());
            reservationRepository.save(reservation);

            return ResponseEntity.ok("Book return approved successfully.");
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Reservation not found.");
    }

    /**
     * Extend due date for a reservation.
     */
    @PostMapping("/extendDue")
    public ResponseEntity<String> extendDue(@RequestParam("reservationId") Long reservationId) {
        Optional<Reservation> reservationOpt = reservationRepository.findById(reservationId);

        if (reservationOpt.isPresent()) {
            Reservation reservation = reservationOpt.get();
            if (!reservation.getActionType().equals(Reservation.ActionType.BORROW)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Reservation is not in borrow state.");
            }

            if (reservation.getDueDate() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Due date is not set for this reservation.");
            }

            // Extend due date by 15 days
            reservation.setDueDate(reservation.getDueDate().plusDays(15));
            reservationRepository.save(reservation);

            return ResponseEntity.ok("Due date extended successfully.");
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Reservation not found.");
    }

    /**
     * Get all reservations with action type BORROW.
     */
    @GetMapping("/borrowed")
    public ResponseEntity<List<Reservation>> getBorrowedReservations() {
        List<Reservation> reservations = reservationRepository.findApprovalReservations();
        return ResponseEntity.ok(reservations);
    }

    @GetMapping("/mybooks/returned")
    public ResponseEntity<List<Reservation>> getReturnedBooks(@RequestParam("memberId") Long memberId) {
        List<Reservation> reservations = reservationRepository.findByActionTypeAndMemberId(Reservation.ActionType.RETURN, memberId);
        return ResponseEntity.ok(reservations);
    }
    
    @GetMapping("/mybooks/reserved")
    public ResponseEntity<List<Reservation>> getReservedBooks(@RequestParam("memberId") Long memberId) {
        List<Reservation> reservations = reservationRepository.findByActionTypeAndMemberId(Reservation.ActionType.RESERVE, memberId);
        return ResponseEntity.ok(reservations);
    }
    
    @GetMapping("/mybooks/borrowed")
    public ResponseEntity<List<Reservation>> getBorrowedBooks(@RequestParam("memberId") Long memberId) {
        List<Reservation> reservations = reservationRepository.findByActionTypeAndMemberId(Reservation.ActionType.BORROW, memberId);
        return ResponseEntity.ok(reservations);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Reservation> getReservationById(@PathVariable Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found"));
        return ResponseEntity.ok(reservation);
    }

}
