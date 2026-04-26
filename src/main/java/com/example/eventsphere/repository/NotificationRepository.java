package com.example.eventsphere.repository;

import com.example.eventsphere.model.Notification;
import com.example.eventsphere.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserOrUserIsNullOrderByCreatedAtDesc(User user);
    List<Notification> findByUserIsNullOrderByCreatedAtDesc();
    long countByUserAndReadedFalse(User user);
    @org.springframework.data.jpa.repository.Query("SELECT COUNT(n) FROM Notification n WHERE (n.user = :user OR n.user IS NULL) AND n.readed = false")
    long countUnreadForUser(User user);
}
