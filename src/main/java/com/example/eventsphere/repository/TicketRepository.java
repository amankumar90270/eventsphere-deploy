package com.example.eventsphere.repository;

import com.example.eventsphere.model.Event;
import com.example.eventsphere.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByEvent(Event event);
    List<Ticket> findByEventAndActiveTrue(Event event);
}
