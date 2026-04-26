package com.example.eventsphere.service;

import com.example.eventsphere.model.Notification;
import com.example.eventsphere.model.User;
import com.example.eventsphere.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;

    public List<Notification> findForUser(User user) {
        return notificationRepository.findByUserOrUserIsNullOrderByCreatedAtDesc(user);
    }

    public long countUnread(User user) {
        return notificationRepository.countUnreadForUser(user);
    }

    @Transactional
    public Notification save(Notification n) {
        return notificationRepository.save(n);
    }

    @Transactional
    public void markAllRead(User user) {
        notificationRepository.findByUserOrUserIsNullOrderByCreatedAtDesc(user)
                .forEach(n -> {
                    n.setReaded(true);
                    notificationRepository.save(n);
                });
    }

    public List<Notification> findAll() {
        return notificationRepository.findByUserIsNullOrderByCreatedAtDesc();
    }

    @Transactional
    public void broadcast(String title, String message, String type) {
        notificationRepository.save(Notification.builder()
                .title(title).message(message).type(type)
                .icon("bi-megaphone").build());
    }
}
// Note: append is handled separately
