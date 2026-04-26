package com.example.eventsphere.service;

import com.example.eventsphere.enums.*;
import com.example.eventsphere.model.*;
import com.example.eventsphere.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository        userRepo;
    private final CategoryRepository    categoryRepo;
    private final EventRepository       eventRepo;
    private final TicketRepository      ticketRepo;
    private final BookingRepository     bookingRepo;
    private final PaymentRepository     paymentRepo;
    private final CouponRepository      couponRepo;
    private final NotificationRepository notifRepo;
    private final PasswordEncoder       encoder;

    @Override
    public void run(String... args) {
        if (userRepo.count() > 0) { log.info("DB already seeded — skipping."); return; }
        log.info("Seeding EventSphere demo data…");

        // ── USERS ─────────────────────────────────────────────────────
        User admin = userRepo.save(User.builder()
                .firstName("John").lastName("Admin")
                .email("admin@eventsphere.com").password(encoder.encode("admin123"))
                .role(UserRole.ADMIN).phone("9000000001").city("Mumbai").build());

        User org1 = userRepo.save(User.builder()
                .firstName("SoundWave").lastName("Events")
                .email("organizer@eventsphere.com").password(encoder.encode("organizer123"))
                .role(UserRole.ORGANIZER).phone("9000000002").city("Delhi")
                .organizationName("SoundWave Events Co.").build());

        User org2 = userRepo.save(User.builder()
                .firstName("TechConf").lastName("India")
                .email("techconf@eventsphere.com").password(encoder.encode("tech123"))
                .role(UserRole.ORGANIZER).phone("9000000003").city("Bangalore")
                .organizationName("TechConf India").build());

        User user1 = userRepo.save(User.builder()
                .firstName("Riya").lastName("Sharma")
                .email("user@eventsphere.com").password(encoder.encode("user123"))
                .role(UserRole.USER).phone("9000000004").city("Lucknow").build());

        User user2 = userRepo.save(User.builder()
                .firstName("Arjun").lastName("Mehta")
                .email("arjun@eventsphere.com").password(encoder.encode("arjun123"))
                .role(UserRole.USER).phone("9000000005").city("Jaipur").build());

        User user3 = userRepo.save(User.builder()
                .firstName("Kavya").lastName("Patel")
                .email("kavya@eventsphere.com").password(encoder.encode("kavya123"))
                .role(UserRole.USER).phone("9000000006").city("Ahmedabad").build());

        // ── CATEGORIES ────────────────────────────────────────────────
        Category music    = categoryRepo.save(Category.builder().name("Music").icon("bi-music-note-beamed").color("purple").description("Concerts, festivals & live music").build());
        Category tech     = categoryRepo.save(Category.builder().name("Technology").icon("bi-cpu").color("blue").description("Conferences, hackathons & workshops").build());
        Category sports   = categoryRepo.save(Category.builder().name("Sports").icon("bi-trophy").color("green").description("Sports events & marathons").build());
        Category arts     = categoryRepo.save(Category.builder().name("Arts & Culture").icon("bi-palette").color("orange").description("Exhibitions, theatre & cultural events").build());
        Category food     = categoryRepo.save(Category.builder().name("Food & Drink").icon("bi-cup-hot").color("red").description("Food festivals & culinary events").build());
        Category business = categoryRepo.save(Category.builder().name("Business").icon("bi-briefcase").color("teal").description("Networking & business summits").build());

        // ── EVENTS ────────────────────────────────────────────────────
        Event e1 = eventRepo.save(Event.builder()
                .title("Sunburn Festival 2025")
                .description("India's biggest electronic music festival returns to Goa. Three days of non-stop music with 80+ international DJs across 5 stages.")
                .category(music).organizer(org1).venue("Vagator Beach").city("Goa")
                .startDate(LocalDate.now().plusDays(30)).endDate(LocalDate.now().plusDays(32))
                .startTime(LocalTime.of(14, 0)).ticketPrice(new BigDecimal("3500"))
                .totalCapacity(10000).availableSeats(3200).status(EventStatus.APPROVED).featured(true).build());

        Event e2 = eventRepo.save(Event.builder()
                .title("TechSpark India 2025")
                .description("The premier technology conference bringing together 5000+ developers, founders, and tech leaders for two days of innovation.")
                .category(tech).organizer(org2).venue("Bangalore International Exhibition Centre").city("Bangalore")
                .startDate(LocalDate.now().plusDays(15)).endDate(LocalDate.now().plusDays(16))
                .startTime(LocalTime.of(9, 0)).ticketPrice(new BigDecimal("2000"))
                .totalCapacity(5000).availableSeats(1800).status(EventStatus.LIVE).featured(true).build());

        Event e3 = eventRepo.save(Event.builder()
                .title("Mumbai Marathon 2025")
                .description("Join 25,000 runners from across India for the city's biggest annual marathon. 5K, 10K, 21K and 42K categories available.")
                .category(sports).organizer(org1).venue("Chhatrapati Shivaji Maharaj Terminus").city("Mumbai")
                .startDate(LocalDate.now().plusDays(45)).endDate(LocalDate.now().plusDays(45))
                .startTime(LocalTime.of(6, 0)).ticketPrice(new BigDecimal("500"))
                .totalCapacity(25000).availableSeats(8000).status(EventStatus.APPROVED).featured(false).build());

        Event e4 = eventRepo.save(Event.builder()
                .title("Delhi Food Festival")
                .description("A celebration of India's diverse culinary heritage. 200+ stalls, live cooking demos, and tastings from top chefs.")
                .category(food).organizer(org2).venue("India Gate Lawns").city("Delhi")
                .startDate(LocalDate.now().plusDays(10)).endDate(LocalDate.now().plusDays(12))
                .startTime(LocalTime.of(11, 0)).ticketPrice(new BigDecimal("299"))
                .totalCapacity(15000).availableSeats(5000).status(EventStatus.LIVE).featured(true).build());

        Event e5 = eventRepo.save(Event.builder()
                .title("National Art Exhibition – Colours of India")
                .description("An immersive art exhibition featuring 150+ works from 60 acclaimed Indian artists. A journey through India's artistic heritage.")
                .category(arts).organizer(org1).venue("National Gallery of Modern Art").city("Delhi")
                .startDate(LocalDate.now().plusDays(5)).endDate(LocalDate.now().plusDays(35))
                .startTime(LocalTime.of(10, 0)).ticketPrice(BigDecimal.ZERO)
                .totalCapacity(3000).availableSeats(1500).status(EventStatus.APPROVED).featured(false).build());

        Event e6 = eventRepo.save(Event.builder()
                .title("Startup Summit Hyderabad")
                .description("Connect with 200+ investors and 1000+ startups. Pitch competitions, mentoring sessions, and networking opportunities.")
                .category(business).organizer(org2).venue("HITEX Exhibition Centre").city("Hyderabad")
                .startDate(LocalDate.now().plusDays(60)).endDate(LocalDate.now().plusDays(61))
                .startTime(LocalTime.of(9, 30)).ticketPrice(new BigDecimal("1500"))
                .totalCapacity(2000).availableSeats(800).status(EventStatus.UPCOMING).featured(true).build());

        Event e7 = eventRepo.save(Event.builder()
                .title("Kolkata Jazz Night")
                .description("An enchanting evening of smooth jazz featuring top musicians from India and abroad. Dine and dance under the stars.")
                .category(music).organizer(org1).venue("Rabindra Sarobar Amphitheatre").city("Kolkata")
                .startDate(LocalDate.now().plusDays(20)).endDate(LocalDate.now().plusDays(20))
                .startTime(LocalTime.of(19, 0)).ticketPrice(new BigDecimal("800"))
                .totalCapacity(500).availableSeats(120).status(EventStatus.APPROVED).featured(false).build());

        // Pending event for approvals demo
        Event e8 = eventRepo.save(Event.builder()
                .title("AI & Machine Learning Bootcamp")
                .description("An intensive 2-day bootcamp on AI fundamentals, LLMs, and practical ML projects.")
                .category(tech).organizer(org2).venue("IIT Delhi Campus").city("Delhi")
                .startDate(LocalDate.now().plusDays(25)).endDate(LocalDate.now().plusDays(26))
                .startTime(LocalTime.of(9, 0)).ticketPrice(new BigDecimal("1800"))
                .totalCapacity(200).availableSeats(200).status(EventStatus.PENDING).build());

        Event e9 = eventRepo.save(Event.builder()
                .title("Indie Music Night – Pune")
                .description("Discover the best of India's independent music scene. 6 bands, one unforgettable night.")
                .category(music).organizer(org1).venue("Hard Rock Cafe Pune").city("Pune")
                .startDate(LocalDate.now().plusDays(8)).endDate(LocalDate.now().plusDays(8))
                .startTime(LocalTime.of(20, 0)).ticketPrice(new BigDecimal("600"))
                .totalCapacity(300).availableSeats(80).status(EventStatus.PENDING).build());

        // ── TICKETS ───────────────────────────────────────────────────
        ticketRepo.save(Ticket.builder().event(e1).ticketType("General").price(new BigDecimal("3500")).totalQuantity(7000).availableQuantity(3200).benefits("Entry + 1 Meal Voucher").build());
        ticketRepo.save(Ticket.builder().event(e1).ticketType("VIP").price(new BigDecimal("8500")).totalQuantity(2000).availableQuantity(800).benefits("VIP Lounge + 3 Meals + Backstage Pass").build());
        ticketRepo.save(Ticket.builder().event(e1).ticketType("Platinum").price(new BigDecimal("18000")).totalQuantity(1000).availableQuantity(200).benefits("All-inclusive + Meet & Greet + Merchandise").build());

        ticketRepo.save(Ticket.builder().event(e2).ticketType("Standard").price(new BigDecimal("2000")).totalQuantity(4000).availableQuantity(1800).benefits("All Sessions + Lunch").build());
        ticketRepo.save(Ticket.builder().event(e2).ticketType("Premium").price(new BigDecimal("5000")).totalQuantity(1000).availableQuantity(400).benefits("All Sessions + Workshop + Networking Dinner").build());

        ticketRepo.save(Ticket.builder().event(e3).ticketType("5K Run").price(new BigDecimal("500")).totalQuantity(10000).availableQuantity(4000).benefits("T-Shirt + Medal + Certificate").build());
        ticketRepo.save(Ticket.builder().event(e3).ticketType("Full Marathon").price(new BigDecimal("1200")).totalQuantity(5000).availableQuantity(2000).benefits("Premium Kit + Medal + Certificate + Recovery Pack").build());

        ticketRepo.save(Ticket.builder().event(e4).ticketType("Day Pass").price(new BigDecimal("299")).totalQuantity(5000).availableQuantity(1667).benefits("Entry + 2 Tastings").build());
        ticketRepo.save(Ticket.builder().event(e4).ticketType("3-Day Pass").price(new BigDecimal("699")).totalQuantity(5000).availableQuantity(1500).benefits("Entry all 3 days + 6 Tastings + 20% off all stalls").build());

        // ── BOOKINGS ──────────────────────────────────────────────────
        Booking b1 = bookingRepo.save(Booking.builder()
                .user(user1).event(e1).quantity(2).totalAmount(new BigDecimal("7000"))
                .status(BookingStatus.CONFIRMED).build());

        Booking b2 = bookingRepo.save(Booking.builder()
                .user(user1).event(e2).quantity(1).totalAmount(new BigDecimal("2000"))
                .status(BookingStatus.CONFIRMED).build());

        Booking b3 = bookingRepo.save(Booking.builder()
                .user(user1).event(e4).quantity(2).totalAmount(new BigDecimal("598"))
                .status(BookingStatus.CONFIRMED).build());

        Booking b4 = bookingRepo.save(Booking.builder()
                .user(user2).event(e1).quantity(3).totalAmount(new BigDecimal("10500"))
                .status(BookingStatus.CONFIRMED).build());

        Booking b5 = bookingRepo.save(Booking.builder()
                .user(user2).event(e3).quantity(1).totalAmount(new BigDecimal("500"))
                .status(BookingStatus.CONFIRMED).build());

        Booking b6 = bookingRepo.save(Booking.builder()
                .user(user3).event(e7).quantity(2).totalAmount(new BigDecimal("1600"))
                .status(BookingStatus.PENDING).build());

        Booking b7 = bookingRepo.save(Booking.builder()
                .user(user3).event(e6).quantity(1).totalAmount(new BigDecimal("1500"))
                .status(BookingStatus.CANCELLED).build());

        // ── PAYMENTS ──────────────────────────────────────────────────
        paymentRepo.save(Payment.builder().booking(b1).amount(b1.getTotalAmount()).method("UPI").transactionId("UPI202504150001").status(PaymentStatus.SUCCESS).build());
        paymentRepo.save(Payment.builder().booking(b2).amount(b2.getTotalAmount()).method("Credit Card").transactionId("CC202504150002").status(PaymentStatus.SUCCESS).build());
        paymentRepo.save(Payment.builder().booking(b3).amount(b3.getTotalAmount()).method("Net Banking").transactionId("NB202504150003").status(PaymentStatus.SUCCESS).build());
        paymentRepo.save(Payment.builder().booking(b4).amount(b4.getTotalAmount()).method("UPI").transactionId("UPI202504150004").status(PaymentStatus.SUCCESS).build());
        paymentRepo.save(Payment.builder().booking(b5).amount(b5.getTotalAmount()).method("Debit Card").transactionId("DC202504150005").status(PaymentStatus.SUCCESS).build());

        // ── COUPONS ───────────────────────────────────────────────────
        couponRepo.save(Coupon.builder().code("WELCOME20").title("Welcome Offer")
                .description("20% off on your first booking").discountPercent(20)
                .maxDiscount(new BigDecimal("500")).minOrderValue(new BigDecimal("500"))
                .expiryDate(LocalDate.now().plusMonths(3)).active(true).maxUsage(1000).usageCount(234).build());

        couponRepo.save(Coupon.builder().code("FESTIVE15").title("Festive Season")
                .description("15% off on all events this festive season").discountPercent(15)
                .maxDiscount(new BigDecimal("1000")).minOrderValue(new BigDecimal("1000"))
                .expiryDate(LocalDate.now().plusMonths(1)).active(true).maxUsage(500).usageCount(87).build());

        couponRepo.save(Coupon.builder().code("MUSIC10").title("Music Lovers")
                .description("10% off on all music events").discountPercent(10)
                .maxDiscount(new BigDecimal("300")).minOrderValue(new BigDecimal("500"))
                .expiryDate(LocalDate.now().plusMonths(2)).active(true).maxUsage(200).usageCount(45).build());

        couponRepo.save(Coupon.builder().code("TECH500").title("Tech Conference")
                .description("Flat ₹500 off on TechSpark tickets").discountPercent(25)
                .maxDiscount(new BigDecimal("500")).minOrderValue(new BigDecimal("2000"))
                .expiryDate(LocalDate.now().plusDays(20)).active(true).maxUsage(100).usageCount(62).build());

        couponRepo.save(Coupon.builder().code("EARLYBIRD").title("Early Bird")
                .description("25% off for early registrations — limited slots!").discountPercent(25)
                .maxDiscount(new BigDecimal("2000")).minOrderValue(new BigDecimal("1500"))
                .expiryDate(LocalDate.now().plusDays(10)).active(true).maxUsage(50).usageCount(49).build());

        // ── NOTIFICATIONS ─────────────────────────────────────────────
        notifRepo.save(Notification.builder().user(user1).title("Booking Confirmed – Sunburn Festival")
                .message("Your booking for Sunburn Festival 2025 is confirmed. 2 tickets. Booking Ref: " + b1.getBookingRef())
                .type("BOOKING").icon("bi-check-circle").build());

        notifRepo.save(Notification.builder().user(user1).title("Upcoming Event Reminder")
                .message("TechSpark India 2025 starts in 15 days. Don't forget to check the agenda.")
                .type("EVENT").icon("bi-calendar-event").build());

        notifRepo.save(Notification.builder().title("Platform Update")
                .message("EventSphere now supports QR code check-in for all events. Upgrade your experience!")
                .type("SYSTEM").icon("bi-megaphone").build());

        notifRepo.save(Notification.builder().user(user2).title("Your booking for Mumbai Marathon")
                .message("Booking confirmed for Mumbai Marathon 2025. 1 ticket. Start preparing!")
                .type("BOOKING").icon("bi-check-circle").build());

        log.info("✅ Seeded: {} users | {} categories | {} events | {} bookings | {} coupons",
                userRepo.count(), categoryRepo.count(), eventRepo.count(), bookingRepo.count(), couponRepo.count());
        log.info("   admin@eventsphere.com   / admin123");
        log.info("   organizer@eventsphere.com / organizer123");
        log.info("   user@eventsphere.com    / user123");
    }
}
