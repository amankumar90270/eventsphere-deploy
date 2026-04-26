package com.example.eventsphere.service;

import com.example.eventsphere.model.ContactMessage;
import com.example.eventsphere.repository.ContactRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContactService {
    private final ContactRepository contactRepository;

    public void save(String firstName, String lastName, String email,
                     String phone, String subject, String message) {
        contactRepository.save(ContactMessage.builder()
                .firstName(firstName).lastName(lastName)
                .email(email).phone(phone)
                .subject(subject).message(message).build());
    }

    public List<ContactMessage> findAll() { return contactRepository.findAllByOrderByCreatedAtDesc(); }
    public long countUnresolved()          { return contactRepository.countByResolvedFalse(); }

    public void resolve(Long id) {
        contactRepository.findById(id).ifPresent(c -> {
            c.setResolved(true);
            contactRepository.save(c);
        });
    }
}
