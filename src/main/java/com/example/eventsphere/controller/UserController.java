package com.example.eventsphere.controller;

import com.example.eventsphere.enums.BookingStatus;
import com.example.eventsphere.model.*;
import com.example.eventsphere.repository.TicketRepository;
import com.example.eventsphere.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.*;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final EventService eventService;
    private final BookingService bookingService;
    private final CategoryService categoryService;
    private final CouponService couponService;
    private final NotificationService notifService;
    private final TicketRepository ticketRepo;
    private final PaymentService paymentService;
    private final RazorpayService razorpayService;
    private final EmailService emailService;

    private User currentUser(UserDetails ud) {
        return userService.findByEmail(ud.getUsername()).orElseThrow();
    }

    // ── Dashboard ──────────────────────────────────────────────
    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails ud, Model model) {
        User user = currentUser(ud);
        List<Booking> myBookings = bookingService.findByUser(user);
        long upcoming = myBookings.stream().filter(b -> b.getStatus() == BookingStatus.CONFIRMED).count();
        long cancelled = myBookings.stream().filter(b -> b.getStatus() == BookingStatus.CANCELLED).count();
        model.addAttribute("user", user);
        model.addAttribute("myBookings", myBookings.stream().limit(5).toList());
        model.addAttribute("upcomingCount", upcoming);
        model.addAttribute("cancelledCount", cancelled);
        model.addAttribute("totalBookings", myBookings.size());
        model.addAttribute("suggestedEvents", eventService.findApproved().stream().limit(4).toList());
        model.addAttribute("unreadCount", notifService.countUnread(user));
        model.addAttribute("totalNotifications", notifService.findForUser(user).size());
        return "user/dashboard";
    }

    // ── Browse Events ──────────────────────────────────────────
    @GetMapping("/browseevents")
    public String browseEvents(@AuthenticationPrincipal UserDetails ud, Model model) {
        model.addAttribute("user", currentUser(ud));
        model.addAttribute("events", eventService.findApproved());
        model.addAttribute("categories", categoryService.findActive());
        return "user/browseevents";
    }

    // ── Event Details ──────────────────────────────────────────
    @GetMapping("/eventdetails/{id}")
    public String eventDetails(@PathVariable Long id,
            @AuthenticationPrincipal UserDetails ud, Model model) {
        User user = currentUser(ud);
        eventService.findById(id).ifPresent(e -> {
            model.addAttribute("event", e);
            model.addAttribute("tickets", ticketRepo.findByEventAndActiveTrue(e));
        });
        model.addAttribute("user", user);
        model.addAttribute("unreadCount", notifService.countUnread(user));
        return "user/eventdetails";
    }

    @GetMapping("/eventdetails")
    public String eventDetailsFallback(@AuthenticationPrincipal UserDetails ud, Model model) {
        User user = currentUser(ud);
        eventService.findApproved().stream().findFirst().ifPresent(e -> {
            model.addAttribute("event", e);
            model.addAttribute("tickets", ticketRepo.findByEventAndActiveTrue(e));
        });
        model.addAttribute("user", user);
        model.addAttribute("unreadCount", notifService.countUnread(user));
        return "user/eventdetails";
    }

    // ── My Bookings ────────────────────────────────────────────
    @GetMapping("/booking")
    public String myBookings(@AuthenticationPrincipal UserDetails ud, Model model) {
        User user = currentUser(ud);
        model.addAttribute("user", user);
        model.addAttribute("bookings", bookingService.findByUser(user));
        return "user/booking";
    }

    @PostMapping("/booking/cancel/{id}")
    public String cancelBooking(@PathVariable Long id, RedirectAttributes ra) {
        bookingService.cancel(id);
        ra.addFlashAttribute("success", "Booking cancelled successfully.");
        return "redirect:/user/booking";
    }

    // ── Checkout ───────────────────────────────────────────────
    @GetMapping("/checkout")
    public String checkout(@AuthenticationPrincipal UserDetails ud,
            @RequestParam(required = false) Long eventId,
            @RequestParam(defaultValue = "1") int qty,
            Model model) {
        User user = currentUser(ud);
        model.addAttribute("user", user);
        model.addAttribute("coupons", couponService.findActive());
        model.addAttribute("razorpayKey", razorpayService.getKeyId());
        if (eventId != null) {
            eventService.findById(eventId).ifPresent(e -> {
                model.addAttribute("event", e);
                model.addAttribute("tickets", ticketRepo.findByEventAndActiveTrue(e));
                model.addAttribute("subtotal", e.getTicketPrice().multiply(BigDecimal.valueOf(qty)));
                model.addAttribute("qty", qty);
            });
        }
        return "user/checkout";
    }

    // ── AJAX: Apply Coupon ─────────────────────────────────────
    @PostMapping("/checkout/applyCoupon")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> applyCoupon(
            @RequestParam String couponCode,
            @RequestParam BigDecimal amount) {
        Map<String, Object> result = new HashMap<>();
        var coupon = couponService.findByCode(couponCode);
        if (coupon.isEmpty() || !coupon.get().isValid()) {
            result.put("success", false);
            result.put("message", "Invalid or expired coupon code.");
        } else {
            BigDecimal discounted = couponService.apply(couponCode, amount);
            BigDecimal saved = amount.subtract(discounted);
            result.put("success", true);
            result.put("newAmount", discounted);
            result.put("saved", saved);
            result.put("discount", coupon.get().getDiscountPercent() + "% off");
            result.put("message", "Coupon applied! You save ₹" + saved.intValue());
        }
        return ResponseEntity.ok(result);
    }

    // ── AJAX: Create Razorpay Order ────────────────────────────
    @PostMapping("/checkout/createRazorpayOrder")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createRazorpayOrder(
            @RequestParam Long eventId,
            @RequestParam int qty,
            @RequestParam(required = false) String couponCode,
            @AuthenticationPrincipal UserDetails ud) {
        Map<String, Object> result = new HashMap<>();
        try {
            Event event = eventService.findById(eventId).orElseThrow();
            BigDecimal amount = event.getTicketPrice().multiply(BigDecimal.valueOf(qty));
            if (couponCode != null && !couponCode.isBlank()) {
                amount = couponService.apply(couponCode, amount);
            }
            String receipt = "ES-" + System.currentTimeMillis();
            var order = razorpayService.createOrder(amount, receipt);
            result.put("success", true);
            result.put("orderId", order.getString("id"));
            result.put("amount", order.getInt("amount"));
            result.put("currency", order.getString("currency"));
            result.put("keyId", razorpayService.getKeyId());
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Payment gateway error: " + e.getMessage());
        }
        return ResponseEntity.ok(result);
    }

    // ── Razorpay Payment Verification + Booking Confirm ───────
    @PostMapping("/checkout/verifyPayment")
    public String verifyPayment(@AuthenticationPrincipal UserDetails ud,
            @RequestParam Long eventId,
            @RequestParam int qty,
            @RequestParam(required = false) String couponCode,
            @RequestParam(required = false) String razorpayOrderId,
            @RequestParam(required = false) String razorpayPaymentId,
            @RequestParam(required = false) String razorpaySignature,
            @RequestParam(defaultValue = "RAZORPAY") String paymentMethod,
            RedirectAttributes ra) {
        User user = currentUser(ud);
        Event event = eventService.findById(eventId).orElseThrow();

        // Verify signature
        boolean paymentVerified = true;
        if (razorpayOrderId != null && razorpayPaymentId != null && razorpaySignature != null) {
            paymentVerified = razorpayService.verifySignature(razorpayOrderId, razorpayPaymentId, razorpaySignature);
        }

        if (!paymentVerified) {
            ra.addFlashAttribute("error", "Payment verification failed. Please try again.");
            return "redirect:/user/checkout?eventId=" + eventId + "&qty=" + qty;
        }

        BigDecimal total = event.getTicketPrice().multiply(BigDecimal.valueOf(qty));
        if (couponCode != null && !couponCode.isBlank()) {
            total = couponService.apply(couponCode, total);
        }

        Booking booking = bookingService.save(Booking.builder()
                .user(user).event(event).quantity(qty)
                .totalAmount(total).status(BookingStatus.CONFIRMED).build());

        paymentService.save(Payment.builder()
                .booking(booking).amount(total)
                .method(razorpayPaymentId != null ? "RAZORPAY" : paymentMethod)
                .transactionId(razorpayPaymentId != null ? razorpayPaymentId : "ES-" + System.currentTimeMillis())
                .build());

        event.setAvailableSeats(event.getAvailableSeats() - qty);
        eventService.save(event);

        // Send confirmation email asynchronously
        emailService.sendBookingConfirmation(booking);

        ra.addFlashAttribute("success", "🎉 Booking confirmed & Mail Sent ! Ref: " + booking.getBookingRef());
        return "redirect:/user/booking";
    }

    // Fallback confirm (non-Razorpay / test mode)
    @PostMapping("/checkout/confirm")
    public String confirmBooking(@AuthenticationPrincipal UserDetails ud,
            @RequestParam Long eventId,
            @RequestParam(defaultValue = "1") int qty,
            @RequestParam(required = false) String couponCode,
            @RequestParam(defaultValue = "CARD") String paymentMethod,
            RedirectAttributes ra) {
        User user = currentUser(ud);
        Event event = eventService.findById(eventId).orElseThrow();
        BigDecimal total = event.getTicketPrice().multiply(BigDecimal.valueOf(qty));
        if (couponCode != null && !couponCode.isBlank()) {
            total = couponService.apply(couponCode, total);
        }
        Booking booking = bookingService.save(Booking.builder()
                .user(user).event(event).quantity(qty)
                .totalAmount(total).status(BookingStatus.CONFIRMED).build());
        paymentService.save(Payment.builder()
                .booking(booking).amount(total).method(paymentMethod)
                .transactionId("ES-TXN-" + System.currentTimeMillis())
                .build());
        event.setAvailableSeats(event.getAvailableSeats() - qty);
        eventService.save(event);

        // Send confirmation email asynchronously
        emailService.sendBookingConfirmation(booking);

        ra.addFlashAttribute("success", "Booking confirmed! Ref: " + booking.getBookingRef());
        return "redirect:/user/booking";
    }

    // ── Ticket Download ────────────────────────────────────────
    @GetMapping("/ticketdownload")
    public String ticketDownload(@AuthenticationPrincipal UserDetails ud,
            @RequestParam(required = false) Long bookingId, Model model) {
        User user = currentUser(ud);
        model.addAttribute("user", user);
        if (bookingId != null) {
            bookingService.findById(bookingId).ifPresent(b -> model.addAttribute("booking", b));
        } else {
            bookingService.findByUser(user).stream().findFirst()
                    .ifPresent(b -> model.addAttribute("booking", b));
        }
        return "user/ticketdownload";
    }

    // ── Coupons ────────────────────────────────────────────────
    @GetMapping("/coupons")
    public String coupons(@AuthenticationPrincipal UserDetails ud, Model model) {
        model.addAttribute("user", currentUser(ud));
        model.addAttribute("coupons", couponService.findActive());
        return "user/coupons";
    }

    // ── Notifications ──────────────────────────────────────────
    @GetMapping("/notifications")
    public String notifications(@AuthenticationPrincipal UserDetails ud, Model model) {
        User user = currentUser(ud);
        model.addAttribute("user", user);
        model.addAttribute("notifications", notifService.findForUser(user));
        model.addAttribute("unreadCount", notifService.countUnread(user));
        notifService.markAllRead(user);
        return "user/notifications";
    }

    // ── Profile ────────────────────────────────────────────────
    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal UserDetails ud, Model model) {
        User user = currentUser(ud);
        model.addAttribute("user", user);
        model.addAttribute("totalBookings", bookingService.findByUser(user).size());
        return "user/profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@AuthenticationPrincipal UserDetails ud,
            @RequestParam String firstName, @RequestParam String lastName,
            @RequestParam String phone, @RequestParam String city,
            @RequestParam(required = false) String bio,
            RedirectAttributes ra) {
        User user = currentUser(ud);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPhone(phone);
        user.setCity(city);
        user.setBio(bio);
        userService.save(user);
        ra.addFlashAttribute("success", "Profile updated.");
        return "redirect:/user/profile";
    }
}
