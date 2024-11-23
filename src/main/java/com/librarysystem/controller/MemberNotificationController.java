package com.librarysystem.controller;

import com.librarysystem.dto.NotificationRequestDTO;
import com.librarysystem.model.Notification;
import com.librarysystem.repository.NotificationRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class MemberNotificationController {

    @Autowired
    private NotificationRepository notificationRepository;

    /**
     * Fetch all unread notifications for a member where the reservation is active.
     *
     * @param id The ID of the member.
     * @return List of unread notifications.
     */
    @GetMapping("/members/{id}/notifications")
    public ResponseEntity<List<NotificationRequestDTO>> getNotifications(@PathVariable Long id) {
        List<Notification> notifications = notificationRepository.findUnreadNotificationsForActiveReservations(id);

        List<NotificationRequestDTO> notificationDTOs = notifications.stream()
            .map(notification -> new NotificationRequestDTO(
                notification.getId(),
                notification.getMember().getId(),
                notification.getMessage(),
                notification.isRead(),
                notification.getReservation() != null ? notification.getReservation().getId() : null,
                notification.getReservation() != null ? notification.getReservation().getReturnedDate() : null
            ))
            .collect(Collectors.toList());

        return ResponseEntity.ok(notificationDTOs);
    }
}
