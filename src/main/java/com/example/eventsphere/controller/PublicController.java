package com.example.eventsphere.controller;

import com.example.eventsphere.enums.EventStatus;
import com.example.eventsphere.model.Event;
import com.example.eventsphere.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/public")
@RequiredArgsConstructor
public class PublicController {

    private final EventService    eventService;
    private final CategoryService categoryService;
    private final ContactService  contactService;

    @GetMapping({"/", "/index"})
    public String index(Model model) {
        model.addAttribute("featuredEvents",  eventService.findFeatured());
        model.addAttribute("liveEvents",      eventService.findLive());
        model.addAttribute("upcomingEvents",  eventService.findApproved().stream().filter(e -> e.getStatus() != com.example.eventsphere.enums.EventStatus.LIVE).limit(8).toList());
        model.addAttribute("categories",      categoryService.findActive());
        model.addAttribute("totalEvents",     eventService.countAll());
        return "public/index";
    }

    @GetMapping("/liveevents")
    public String liveEvents(Model model) {
        model.addAttribute("events",     eventService.findLive());
        model.addAttribute("categories", categoryService.findActive());
        return "public/liveevents";
    }

    @GetMapping("/upcomingevents")
    public String upcomingEvents(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String price,
            Model model) {

        List<Event> events = eventService.findUpcoming();

        // Apply filters
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.toLowerCase();
            events = events.stream()
                    .filter(e -> e.getTitle().toLowerCase().contains(kw)
                            || (e.getDescription() != null && e.getDescription().toLowerCase().contains(kw))
                            || (e.getCity() != null && e.getCity().toLowerCase().contains(kw)))
                    .collect(Collectors.toList());
        }
        if (category != null && !category.isBlank()) {
            events = events.stream()
                    .filter(e -> e.getCategory() != null && e.getCategory().getName().equalsIgnoreCase(category))
                    .collect(Collectors.toList());
        }
        if (city != null && !city.isBlank()) {
            events = events.stream()
                    .filter(e -> e.getCity() != null && e.getCity().equalsIgnoreCase(city))
                    .collect(Collectors.toList());
        }
        if ("free".equals(price)) {
            events = events.stream().filter(Event::isFree).collect(Collectors.toList());
        } else if ("paid".equals(price)) {
            events = events.stream().filter(e -> !e.isFree()).collect(Collectors.toList());
        }

        model.addAttribute("events",     events);
        model.addAttribute("categories", categoryService.findActive());
        return "public/upcomingevents";
    }

    @GetMapping("/eventdetails/{id}")
    public String eventDetails(@PathVariable Long id, Model model) {
        eventService.findById(id).ifPresent(e -> model.addAttribute("event", e));
        return "public/eventdetails";
    }

    @GetMapping("/eventdetails")
    public String eventDetailsFallback(Model model) {
        eventService.findApproved().stream().findFirst()
                .ifPresent(e -> model.addAttribute("event", e));
        return "public/eventdetails";
    }

    @GetMapping("/contact")
    public String contact() { return "public/contact"; }

    @PostMapping("/contact")
    public String submitContact(
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String email,
            @RequestParam(required = false) String phone,
            @RequestParam String subject,
            @RequestParam String message,
            Model model) {
        contactService.save(firstName, lastName, email, phone, subject, message);
        model.addAttribute("success", "Thank you! We'll get back to you within 24 hours.");
        return "public/contact";
    }

    @GetMapping("/about")   public String about()   { return "public/about"; }
    @GetMapping("/faq")     public String faq()     { return "public/faq"; }
}
