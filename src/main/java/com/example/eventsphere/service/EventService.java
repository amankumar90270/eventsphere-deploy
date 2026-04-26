package com.example.eventsphere.service;

import com.example.eventsphere.enums.EventStatus;
import com.example.eventsphere.model.Event;
import com.example.eventsphere.model.User;
import com.example.eventsphere.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    public List<Event> findAll()                            { return eventRepository.findAllByOrderByCreatedAtDesc(); }
    public Optional<Event> findById(Long id)               { return eventRepository.findById(id); }
    public List<Event> findByOrganizer(User organizer)     { return eventRepository.findByOrganizer(organizer); }
    public List<Event> findByStatus(EventStatus status)    { return eventRepository.findByStatus(status); }
    public List<Event> findApproved()                      { return eventRepository.findByStatusIn(List.of(EventStatus.APPROVED, EventStatus.LIVE, EventStatus.UPCOMING)); }
    public List<Event> findLive()                          { return eventRepository.findByStatus(EventStatus.LIVE); }
    public List<Event> findUpcoming()                      { return eventRepository.findByStatusIn(List.of(EventStatus.UPCOMING, EventStatus.APPROVED)); }
    public List<Event> findPending()                       { return eventRepository.findByStatus(EventStatus.PENDING); }
    public List<Event> findFeatured()                      { return eventRepository.findByFeaturedTrueAndStatusIn(List.of(EventStatus.APPROVED, EventStatus.LIVE, EventStatus.UPCOMING)); }
    public long countByStatus(EventStatus status)          { return eventRepository.countByStatus(status); }
    public long countAll()                                 { return eventRepository.count(); }
    public long countByOrganizer(User organizer)           { return eventRepository.countByOrganizer(organizer); }

    @Transactional
    public Event save(Event event)  { return eventRepository.save(event); }

    @Transactional
    public void approve(Long id) {
        eventRepository.findById(id).ifPresent(e -> {
            e.setStatus(EventStatus.APPROVED);
            eventRepository.save(e);
        });
    }

    @Transactional
    public void reject(Long id, String reason) {
        eventRepository.findById(id).ifPresent(e -> {
            e.setStatus(EventStatus.REJECTED);
            e.setRejectionReason(reason);
            eventRepository.save(e);
        });
    }

    @Transactional
    public void delete(Long id) { eventRepository.deleteById(id); }
}
