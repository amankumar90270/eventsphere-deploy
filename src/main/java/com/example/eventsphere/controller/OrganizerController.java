package com.example.eventsphere.controller;

import com.example.eventsphere.enums.BookingStatus;
import com.example.eventsphere.enums.EventStatus;
import com.example.eventsphere.model.*;
import com.example.eventsphere.repository.TicketRepository;
import com.example.eventsphere.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/organizer")
@RequiredArgsConstructor
public class OrganizerController {

    private final UserService      userService;
    private final EventService     eventService;
    private final BookingService   bookingService;
    private final CategoryService  categoryService;
    private final TicketRepository ticketRepo;
    private final NotificationService notifService;

    private User currentUser(UserDetails ud) {
        return userService.findByEmail(ud.getUsername()).orElseThrow();
    }

    // ── Dashboard ──────────────────────────────────────────────
    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails ud, Model model) {
        User organizer = currentUser(ud);
        List<Event> myEvents = eventService.findByOrganizer(organizer);
        List<Booking> myBookings = bookingService.findAll().stream()
                .filter(b -> b.getEvent().getOrganizer().getId().equals(organizer.getId())).toList();
        long confirmed = myBookings.stream().filter(b -> b.getStatus() == BookingStatus.CONFIRMED).count();
        BigDecimal revenue = bookingService.revenueForOrganizer(organizer);
        model.addAttribute("organizer",      organizer);
        model.addAttribute("myEvents",       myEvents);
        model.addAttribute("eventCount",     myEvents.size());
        model.addAttribute("bookingCount",   myBookings.size());
        model.addAttribute("confirmedCount", confirmed);
        model.addAttribute("revenue",        revenue);
        model.addAttribute("recentBookings", myBookings.stream().limit(5).toList());
        return "organizer/dashboard";
    }

    // ── My Events ──────────────────────────────────────────────
    @GetMapping("/events")
    public String events(@AuthenticationPrincipal UserDetails ud, Model model) {
        User organizer = currentUser(ud);
        model.addAttribute("organizer", organizer);
        model.addAttribute("events",    eventService.findByOrganizer(organizer));
        return "organizer/events";
    }

    // ── Create Event ───────────────────────────────────────────
    @GetMapping("/createevent")
    public String createEventForm(@AuthenticationPrincipal UserDetails ud, Model model) {
        User organizer = currentUser(ud);
        model.addAttribute("organizer",  organizer);
        model.addAttribute("categories", categoryService.findActive());
        model.addAttribute("event",      new Event());
        return "organizer/createevent";
    }

    @PostMapping("/createevent")
    public String doCreateEvent(@AuthenticationPrincipal UserDetails ud,
                                @RequestParam String title,
                                @RequestParam(required = false) String description,
                                @RequestParam Long categoryId,
                                @RequestParam String venue,
                                @RequestParam String city,
                                @RequestParam(required = false) String address,
                                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                @RequestParam(required = false) String startTime,
                                @RequestParam BigDecimal ticketPrice,
                                @RequestParam int totalCapacity,
                                @RequestParam(required = false) String tags,
                                @RequestParam(required = false) String imageUrl,
                                @RequestParam(required = false) MultipartFile bannerFile,
                                @RequestParam(defaultValue = "submit") String action,
                                RedirectAttributes ra) {

        User organizer = currentUser(ud);
        Category cat = categoryService.findById(categoryId).orElse(null);

        // Determine status
        EventStatus status = "draft".equals(action) ? EventStatus.PENDING : EventStatus.PENDING;

        // Handle banner file upload
        String finalImageUrl = imageUrl;
        if (bannerFile != null && !bannerFile.isEmpty()) {
            try {
                String uploadDir = "src/main/resources/static/uploads/events/";
                Files.createDirectories(Paths.get(uploadDir));
                String filename = UUID.randomUUID() + "_" + bannerFile.getOriginalFilename();
                Files.copy(bannerFile.getInputStream(), Paths.get(uploadDir + filename),
                        StandardCopyOption.REPLACE_EXISTING);
                finalImageUrl = "/uploads/events/" + filename;
            } catch (IOException e) {
                // fallback: keep imageUrl
            }
        }

        LocalTime parsedTime = LocalTime.of(10, 0);
        if (startTime != null && !startTime.isBlank()) {
            try { parsedTime = LocalTime.parse(startTime); } catch (Exception ignored) {}
        }

        Event event = Event.builder()
                .title(title)
                .description(description)
                .category(cat)
                .organizer(organizer)
                .venue(venue)
                .city(city)
                .address(address)
                .startDate(startDate)
                .endDate(endDate != null ? endDate : startDate)
                .startTime(parsedTime)
                .ticketPrice(ticketPrice)
                .totalCapacity(totalCapacity)
                .availableSeats(totalCapacity)
                .tags(tags)
                .imageUrl(finalImageUrl)
                .status(status)
                .build();

        eventService.save(event);

        if ("draft".equals(action)) {
            ra.addFlashAttribute("success", "Event saved as draft.");
        } else {
            ra.addFlashAttribute("success", "Event submitted for admin approval!");
        }
        return "redirect:/organizer/events";
    }

    @PostMapping("/events/delete/{id}")
    public String deleteEvent(@PathVariable Long id, RedirectAttributes ra) {
        eventService.delete(id);
        ra.addFlashAttribute("success", "Event deleted.");
        return "redirect:/organizer/events";
    }

    // ── Tickets ────────────────────────────────────────────────
    @GetMapping("/tickets")
    public String tickets(@AuthenticationPrincipal UserDetails ud, Model model) {
        User organizer = currentUser(ud);
        List<Event> myEvents = eventService.findByOrganizer(organizer);
        List<Ticket> allTickets = myEvents.stream()
                .flatMap(e -> ticketRepo.findByEvent(e).stream()).toList();
        model.addAttribute("organizer", organizer);
        model.addAttribute("tickets",   allTickets);
        model.addAttribute("events",    myEvents);
        return "organizer/tickets";
    }

    // ── Bookings ───────────────────────────────────────────────
    @GetMapping("/bookings")
    public String bookings(@AuthenticationPrincipal UserDetails ud, Model model) {
        User organizer = currentUser(ud);
        List<Booking> bookings = bookingService.findAll().stream()
                .filter(b -> b.getEvent().getOrganizer().getId().equals(organizer.getId())).toList();
        model.addAttribute("organizer", organizer);
        model.addAttribute("bookings",  bookings);
        return "organizer/bookings";
    }

    // ── Check-In ───────────────────────────────────────────────
    @GetMapping("/checkin")
    public String checkin(@AuthenticationPrincipal UserDetails ud,
                          @RequestParam(required = false) String ref,
                          @RequestParam(required = false) String error,
                          @RequestParam(required = false) String success,
                          Model model) {
        User organizer = currentUser(ud);
        model.addAttribute("organizer", organizer);
        model.addAttribute("events",    eventService.findByOrganizer(organizer));
        // Recent check-ins: CONFIRMED bookings for organizer's events (last 20)
        java.util.List<com.example.eventsphere.model.Booking> recent = bookingService.findAll().stream()
            .filter(b -> b.getEvent().getOrganizer().getId().equals(organizer.getId())
                      && b.getStatus() == com.example.eventsphere.enums.BookingStatus.CONFIRMED)
            .limit(20).toList();
        model.addAttribute("recentBookings", recent);
        if (ref  != null) model.addAttribute("searchRef", ref);
        if (error != null) model.addAttribute("checkinError", error);
        if (success != null) model.addAttribute("checkinSuccess", success);
        return "organizer/checkin";
    }

    // Search booking by ref
    @GetMapping("/checkin/search")
    public String searchCheckin(@AuthenticationPrincipal UserDetails ud,
                                @RequestParam String ref,
                                Model model) {
        User organizer = currentUser(ud);
        model.addAttribute("organizer", organizer);
        model.addAttribute("events",    eventService.findByOrganizer(organizer));
        model.addAttribute("searchRef", ref);

        var bookingOpt = bookingService.findByRef(ref.trim().toUpperCase());
        if (bookingOpt.isEmpty()) {
            model.addAttribute("checkinError", "No booking found with reference: " + ref);
        } else {
            com.example.eventsphere.model.Booking b = bookingOpt.get();
            // Verify this booking belongs to organizer's event
            if (!b.getEvent().getOrganizer().getId().equals(organizer.getId())) {
                model.addAttribute("checkinError", "This booking does not belong to your events.");
            } else {
                model.addAttribute("foundBooking", b);
            }
        }

        java.util.List<com.example.eventsphere.model.Booking> recent = bookingService.findAll().stream()
            .filter(bk -> bk.getEvent().getOrganizer().getId().equals(organizer.getId())
                       && bk.getStatus() == com.example.eventsphere.enums.BookingStatus.CONFIRMED)
            .limit(20).toList();
        model.addAttribute("recentBookings", recent);
        return "organizer/checkin";
    }

    // Mark booking as checked in (status -> COMPLETED)
    @PostMapping("/checkin/mark/{bookingId}")
    public String markCheckin(@PathVariable Long bookingId,
                              @AuthenticationPrincipal UserDetails ud,
                              RedirectAttributes ra) {
        User organizer = currentUser(ud);
        bookingService.findById(bookingId).ifPresent(b -> {
            if (b.getEvent().getOrganizer().getId().equals(organizer.getId())) {
                b.setStatus(com.example.eventsphere.enums.BookingStatus.COMPLETED);
                bookingService.save(b);
            }
        });
        ra.addFlashAttribute("checkinSuccess", "Attendee checked in successfully!");
        return "redirect:/organizer/checkin";
    }

    // ── Communication ──────────────────────────────────────────
    @GetMapping("/communication")
    public String communication(@AuthenticationPrincipal UserDetails ud, Model model) {
        User organizer = currentUser(ud);
        model.addAttribute("organizer", organizer);
        model.addAttribute("events",    eventService.findByOrganizer(organizer));
        return "organizer/communication";
    }

    // ── Media ──────────────────────────────────────────────────
    @GetMapping("/media")
    public String media(@AuthenticationPrincipal UserDetails ud, Model model) {
        User organizer = currentUser(ud);
        model.addAttribute("organizer", organizer);
        model.addAttribute("events",    eventService.findByOrganizer(organizer));
        return "organizer/media";
    }

    // ── Analytics ──────────────────────────────────────────────
    @GetMapping("/analytics")
    public String analytics(@AuthenticationPrincipal UserDetails ud, Model model) {
        User organizer = currentUser(ud);
        List<Event> myEvents = eventService.findByOrganizer(organizer);
        BigDecimal revenue = bookingService.revenueForOrganizer(organizer);
        int totalBookedSeats = myEvents.stream()
                .mapToInt(Event::getBookedSeats)
                .sum();
        model.addAttribute("totalBookedSeats",totalBookedSeats);
        model.addAttribute("organizer",  organizer);
        model.addAttribute("events",     myEvents);
        model.addAttribute("eventCount", myEvents.size());
        model.addAttribute("revenue",    revenue);
        return "organizer/analytics";
    }

    // ── Profile ────────────────────────────────────────────────
    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal UserDetails ud, Model model) {
        User organizer = currentUser(ud);
        model.addAttribute("organizer",  organizer);
        model.addAttribute("eventCount", eventService.countByOrganizer(organizer));
        model.addAttribute("revenue",    bookingService.revenueForOrganizer(organizer));
        return "organizer/profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@AuthenticationPrincipal UserDetails ud,
                                @RequestParam String firstName, @RequestParam String lastName,
                                @RequestParam String phone,     @RequestParam String city,
                                @RequestParam(required = false) String bio,
                                @RequestParam(required = false) String organizationName,
                                RedirectAttributes ra) {
        User organizer = currentUser(ud);
        organizer.setFirstName(firstName); organizer.setLastName(lastName);
        organizer.setPhone(phone);         organizer.setCity(city);
        organizer.setBio(bio);             organizer.setOrganizationName(organizationName);
        userService.save(organizer);
        ra.addFlashAttribute("success", "Profile updated.");
        return "redirect:/organizer/profile";
    }
}
