package com.example.notification_service.repository;

import com.example.notification_service.model.NotificationTemplate;
import com.example.notification_service.model.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, UUID> {

    Optional<NotificationTemplate> findByTemplateNameAndActive(String templateName, Boolean active);

    Optional<NotificationTemplate> findByEventTypeAndTypeAndActive(String eventType, NotificationType type, Boolean active);

    List<NotificationTemplate> findByActiveTrue();

    List<NotificationTemplate> findByTypeAndActiveTrue(NotificationType type);
}