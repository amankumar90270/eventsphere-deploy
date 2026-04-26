package com.example.eventsphere.controller;

import com.example.eventsphere.enums.BookingStatus;
import com.example.eventsphere.enums.EventStatus;
import com.example.eventsphere.enums.UserRole;
import com.example.eventsphere.model.Category;
import com.example.eventsphere.model.Notification;
import com.example.eventsphere.model.User;
import com.example.eventsphere.service.*;
import com.example.eventsphere.model.ContactMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService         userService;
    private final EventService        eventService;
    private final BookingService      bookingService;
    private final CategoryService     categoryService;
    private final CouponService       couponService;
    private final NotificationService notifService;
    private final PaymentService      paymentService;
    private final ContactService       contactService;

    // ── Dashboard ──────────────────────────────────────────────
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalUsers",     userService.countAll());
        model.addAttribute("totalEvents",    eventService.countAll());
        model.addAttribute("totalBookings",  bookingService.countAll());
        model.addAttribute("totalRevenue",   bookingService.totalRevenue());
        model.addAttribute("pendingEvents",  eventService.countByStatus(EventStatus.PENDING));
        model.addAttribute("recentBookings", bookingService.findAll().stream().limit(8).toList());
        model.addAttribute("recentEvents",   eventService.findAll().stream().limit(5).toList());
        return "admin/dashboard";
    }

    // ── User Management ────────────────────────────────────────
    @GetMapping("/usermanagement")
    public String users(Model model) {
        model.addAttribute("users",      userService.findAll());
        model.addAttribute("totalUsers", userService.countAll());
        model.addAttribute("organizers", userService.countByRole(UserRole.ORGANIZER));
        model.addAttribute("customers",  userService.countByRole(UserRole.USER));
        return "admin/usermanagement";
    }

    @PostMapping("/usermanagement/add")
    public String addUser(@RequestParam String firstName,
                          @RequestParam String lastName,
                          @RequestParam String email,
                          @RequestParam String password,
                          @RequestParam(required = false) String phone,
                          @RequestParam(defaultValue = "USER") String role,
                          @RequestParam(required = false) String organizationName,
                          RedirectAttributes ra) {
        if (userService.emailExists(email)) {
            ra.addFlashAttribute("error", "Email already registered.");
            return "redirect:/admin/usermanagement";
        }
        User u = User.builder()
                .firstName(firstName).lastName(lastName)
                .email(email).password(password)
                .phone(phone)
                .role(UserRole.valueOf(role))
                .organizationName(organizationName)
                .active(true).build();
        userService.register(u);
        ra.addFlashAttribute("success", firstName + " " + lastName + " added as " + role + ".");
        return "redirect:/admin/usermanagement";
    }

    @PostMapping("/usermanagement/toggle/{id}")
    public String toggleUser(@PathVariable Long id, RedirectAttributes ra) {
        userService.toggleActive(id);
        ra.addFlashAttribute("success", "User status updated.");
        return "redirect:/admin/usermanagement";
    }

    // ── Categories ─────────────────────────────────────────────
    @GetMapping("/categories")
    public String categories(Model model) {
        model.addAttribute("categories",  categoryService.findAll());
        model.addAttribute("newCategory", new Category());
        return "admin/categories";
    }

    @PostMapping("/categories/save")
    public String saveCategory(@RequestParam(required = false) Long id,
                               @RequestParam String name,
                               @RequestParam(required = false) String icon,
                               @RequestParam(required = false) String color,
                               @RequestParam(required = false) String description,
                               RedirectAttributes ra) {
        Category cat = (id != null)
                ? categoryService.findById(id).orElse(new Category())
                : new Category();
        cat.setName(name);
        cat.setIcon(icon  != null && !icon.isBlank()  ? icon  : "bi-tag");
        cat.setColor(color != null && !color.isBlank() ? color : "blue");
        cat.setDescription(description);
        categoryService.save(cat);
        ra.addFlashAttribute("success", "Category '" + name + "' saved.");
        return "redirect:/admin/categories";
    }

    @PostMapping("/categories/delete/{id}")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes ra) {
        categoryService.delete(id);
        ra.addFlashAttribute("success", "Category deleted.");
        return "redirect:/admin/categories";
    }

    // ── Event Approvals ────────────────────────────────────────
    @GetMapping("/approvals")
    public String approvals(Model model) {
        model.addAttribute("pendingEvents",  eventService.findPending());
        model.addAttribute("allEvents",      eventService.findAll());
        model.addAttribute("pendingCount",   eventService.countByStatus(EventStatus.PENDING));
        model.addAttribute("approvedCount",  eventService.countByStatus(EventStatus.APPROVED));
        model.addAttribute("rejectedCount",  eventService.countByStatus(EventStatus.REJECTED));
        return "admin/approvals";
    }

    @PostMapping("/approvals/approve/{id}")
    public String approveEvent(@PathVariable Long id, RedirectAttributes ra) {
        eventService.approve(id);
        ra.addFlashAttribute("success", "Event approved.");
        return "redirect:/admin/approvals";
    }

    @PostMapping("/approvals/reject/{id}")
    public String rejectEvent(@PathVariable Long id,
                              @RequestParam(defaultValue = "Does not meet guidelines") String reason,
                              RedirectAttributes ra) {
        eventService.reject(id, reason);
        ra.addFlashAttribute("success", "Event rejected.");
        return "redirect:/admin/approvals";
    }

    // ── Bookings ───────────────────────────────────────────────
    @GetMapping("/bookings")
    public String bookings(Model model) {
        model.addAttribute("bookings",     bookingService.findAll());
        model.addAttribute("confirmed",    bookingService.countByStatus(BookingStatus.CONFIRMED));
        model.addAttribute("pending",      bookingService.countByStatus(BookingStatus.PENDING));
        model.addAttribute("cancelled",    bookingService.countByStatus(BookingStatus.CANCELLED));
        model.addAttribute("totalRevenue", bookingService.totalRevenue());
        return "admin/bookings";
    }

    @PostMapping("/bookings/cancel/{id}")
    public String cancelBooking(@PathVariable Long id, RedirectAttributes ra) {
        bookingService.cancel(id);
        ra.addFlashAttribute("success", "Booking cancelled.");
        return "redirect:/admin/bookings";
    }

    // ── Payments ───────────────────────────────────────────────
    @GetMapping("/payments")
    public String payments(Model model) {
        model.addAttribute("payments",     paymentService.findAll());
        model.addAttribute("totalRevenue", bookingService.totalRevenue());
        return "admin/payments";
    }

    // ── Notifications ──────────────────────────────────────────
    @GetMapping("/notifications")
    public String notifications(Model model) {
        model.addAttribute("notifications", notifService.findAll());
        model.addAttribute("newNotif",      new Notification());
        return "admin/notifications";
    }

    @PostMapping("/notifications/broadcast")
    public String broadcast(@RequestParam String title, @RequestParam String message,
                            RedirectAttributes ra) {
        notifService.broadcast(title, message, "SYSTEM");
        ra.addFlashAttribute("success", "Notification broadcast to all users.");
        return "redirect:/admin/notifications";
    }

    // ── Reports ────────────────────────────────────────────────
    @GetMapping("/reports")
    public String reports(Model model) {
        model.addAttribute("totalRevenue",  bookingService.totalRevenue());
        model.addAttribute("totalBookings", bookingService.countAll());
        model.addAttribute("totalEvents",   eventService.countAll());
        model.addAttribute("totalUsers",    userService.countAll());
        model.addAttribute("liveEvents",    eventService.countByStatus(EventStatus.LIVE));
        return "admin/reports";
    }


    // ── Contact Messages ───────────────────────────────────────
    @GetMapping("/contacts")
    public String contacts(Model model) {
        model.addAttribute("messages",    contactService.findAll());
        model.addAttribute("unresolved",  contactService.countUnresolved());
        return "admin/contacts";
    }

    @PostMapping("/contacts/resolve/{id}")
    public String resolveContact(@PathVariable Long id, RedirectAttributes ra) {
        contactService.resolve(id);
        ra.addFlashAttribute("success", "Message marked as resolved.");
        return "redirect:/admin/contacts";
    }

    @GetMapping("/login")
    public String adminLogin() { return "redirect:/login"; }
}
