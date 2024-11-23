package com.librarysystem.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.librarysystem.model.Notification;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
   // List<Notification> findByMemberIdAndIsReadFalse(Long memberId);

    @Query("SELECT n FROM Notification n WHERE n.reservation.id = :reservationId")
List<Notification> findByReservationId(@Param("reservationId") Long reservationId);

@Query("SELECT n FROM Notification n WHERE n.member.id = :memberId AND n.isRead = false")
List<Notification> findByMemberIdAndIsReadFalse(@Param("memberId") Long memberId);


@Query("SELECT n FROM Notification n WHERE n.member.id = :memberId AND n.reservation.returnedDate IS NULL")
List<Notification> findUnreadNotificationsForActiveReservations(@Param("memberId") Long memberId);


}

