package com.example.notification_service.repository;

import com.example.notification_service.model.Notification;
import com.example.notification_service.model.NotificationStatus;
import com.example.notification_service.model.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    List<Notification> findByOrderId(UUID orderId);

    List<Notification> findByStatus(NotificationStatus status);

    List<Notification> findByTypeAndStatus(NotificationType type, NotificationStatus status);

    @Query("SELECT n FROM Notification n WHERE n.status = 'FAILED' AND n.retryCount < 3")
    List<Notification> findFailedNotificationsForRetry();

    @Query("SELECT n FROM Notification n WHERE n.orderId = :orderId ORDER BY n.createdAt DESC")
    List<Notification> findByOrderIdOrderByCreatedAtDesc(@Param("orderId") UUID orderId);

    @Query("SELECT n FROM Notification n WHERE n.createdAt >= :startDate AND n.createdAt <= :endDate")
    List<Notification> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                     @Param("endDate") LocalDateTime endDate);
}