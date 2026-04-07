package org.example.repositories;
import org.example.entities.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findAllByOrderByCreatedAtDesc ();
    List<Notification> findTop10ByOrderByCreatedAtDesc();
}
