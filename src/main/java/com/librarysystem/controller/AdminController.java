package com.librarysystem.controller;

import com.librarysystem.dto.BorrowHistoryDTO;
import com.librarysystem.model.Notification;
import com.librarysystem.model.Reservation;
import com.librarysystem.model.Member;
import com.librarysystem.repository.MemberRepository;
import com.librarysystem.repository.NotificationRepository;
import com.librarysystem.repository.ReservationRepository;
import com.librarysystem.dto.NotificationRequestDTO;
import com.librarysystem.model.Reservation.ActionType;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private NotificationRepository notificationRepository;
    

    /**
     * Fetch borrow history for all members.
     *
     * @param query Optional filter for member name or book title.
     * @return List of BorrowHistoryDTO
     */
    @GetMapping("/borrow-history")
    public ResponseEntity<List<BorrowHistoryDTO>> getBorrowHistory(
            @RequestParam(value = "query", required = false) String query) {
        
        List<Reservation> reservations;

        if (query != null && !query.trim().isEmpty()) {
            reservations = reservationRepository.findByMemberNameOrBookTitle(query.trim());
        } else {
            reservations = reservationRepository.findAll();
        }

        List<BorrowHistoryDTO> history = reservations.stream()
                .map(reservation -> new BorrowHistoryDTO(
                        reservation.getMember().getName(),
                        reservation.getBook().getTitle(),
                        reservation.getBook().getAuthor(),
                        reservation.getActionType().toString(),
                        reservation.getActionType() == Reservation.ActionType.BORROW
                                ? reservation.getBorrowedDate() != null ? reservation.getBorrowedDate().toLocalDate().toString() : null
                                : reservation.getActionType() == Reservation.ActionType.RESERVE
                                ? reservation.getReservedDate() != null ? reservation.getReservedDate().toLocalDate().toString() : null
                                : reservation.getReturnedDate() != null ? reservation.getReturnedDate().toLocalDate().toString() : null
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(history);
    }

    @PostMapping("/notifications/send")
    public ResponseEntity<String> sendNotification(@RequestBody NotificationRequestDTO request) {
        if (request.getReservationId() == null) {
            throw new IllegalArgumentException("Reservation ID must not be null");
        }
    
        Reservation reservation = reservationRepository.findById(request.getReservationId())
            .orElseThrow(() -> new IllegalArgumentException("Reservation not found"));
    
        Notification notification = new Notification();
        notification.setReservation(reservation); // Associate the reservation
        notification.setMember(reservation.getMember());
        notification.setMessage(request.getMessage());
        notification.setRead(false);
    
        notificationRepository.save(notification);
    
        return ResponseEntity.ok("Notification sent successfully!");
    }
    
    

    @GetMapping("/reservations/past-due")
    public ResponseEntity<List<Reservation>> getPastDueReservations() {
        List<Reservation> pastDueReservations = reservationRepository.findAll()
            .stream()
            .filter(reservation -> {
                // Convert LocalDateTime to LocalDate for comparison
                LocalDate dueDate = reservation.getDueDate() != null ? reservation.getDueDate().toLocalDate() : null;
                return dueDate != null 
                        && dueDate.isBefore(LocalDate.now())
                        && reservation.getActionType() == Reservation.ActionType.BORROW;
            })
            .collect(Collectors.toList());
    
        return ResponseEntity.ok(pastDueReservations);
    }
    
    

    @PutMapping("/reservations/return/{id}")
    public ResponseEntity<String> returnBook(@PathVariable Long id) {
        Reservation reservation = reservationRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Reservation not found"));

        if (reservation.getActionType() == Reservation.ActionType.BORROW) {
            // Update action type to RETURN
            reservation.setActionType(Reservation.ActionType.RETURN);
            reservation.setReturnedDate(LocalDateTime.now());
            reservationRepository.save(reservation);

            // Mark related notifications as read
            List<Notification> notifications = notificationRepository.findByReservationId(reservation.getId());
            for (Notification notification : notifications) {
                notification.setRead(true);
                notificationRepository.save(notification);
            }
        }

        return ResponseEntity.ok("Book returned successfully!");
    }

  
        
}
