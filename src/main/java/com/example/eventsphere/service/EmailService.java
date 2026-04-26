package com.example.eventsphere.service;

import com.example.eventsphere.model.Booking;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromEmail;

    /**
     * Sends a booking confirmation email to the user asynchronously.
     * @Async ensures this never blocks the HTTP response thread.
     */
    @Async
    public void sendBookingConfirmation(Booking booking) {
        try {
            String toEmail   = booking.getUser().getEmail();
            String userName  = booking.getUser().getFirstName() + " " + booking.getUser().getLastName();
            String eventName = booking.getEvent().getTitle();
            String venue     = booking.getEvent().getVenue() + ", " + booking.getEvent().getCity();
            String date      = booking.getEvent().getStartDate()
                                     .format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));
            String ref       = booking.getBookingRef();
            int    qty       = booking.getQuantity();
            String amount    = "\u20b9" + booking.getTotalAmount().toPlainString();

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "EventSphere");
            helper.setTo(toEmail);
            helper.setSubject("\uD83C\uDFAB Booking Confirmed! \u2013 " + eventName + " [" + ref + "]");
            helper.setText(buildHtmlBody(userName, eventName, venue, date, ref, qty, amount), true);

            mailSender.send(message);
            log.info("Booking confirmation email sent to {} for booking {}", toEmail, ref);

        } catch (Exception e) {
            log.error("Failed to send booking confirmation email for booking {}: {}",
                      booking.getBookingRef(), e.getMessage());
        }
    }

    private String buildHtmlBody(String name, String event, String venue,
                                  String date, String ref, int qty, String amount) {
        return "<!DOCTYPE html>" +
               "<html lang='en'><head><meta charset='UTF-8'><style>" +
               "body{margin:0;padding:0;background:#f4f4f8;font-family:'Segoe UI',Arial,sans-serif;}" +
               ".wrap{max-width:600px;margin:40px auto;background:#fff;border-radius:16px;overflow:hidden;box-shadow:0 4px 24px rgba(0,0,0,.10);}" +
               ".header{background:linear-gradient(135deg,#06b6d4,#00e5ff);padding:36px 32px;text-align:center;}" +
               ".header h1{margin:0;color:#07070f;font-size:26px;font-weight:800;}" +
               ".header p{margin:6px 0 0;color:rgba(0,0,0,.6);font-size:14px;}" +
               ".body{padding:36px 32px;}" +
               ".greeting{font-size:17px;color:#1a1a2e;margin-bottom:20px;}" +
               ".card{background:#f0fdff;border:1.5px solid #a5f3ff;border-radius:12px;padding:22px 24px;margin-bottom:24px;}" +
               ".card h2{margin:0 0 16px;font-size:18px;color:#06b6d4;}" +
               ".row{display:flex;justify-content:space-between;margin-bottom:10px;font-size:14px;}" +
               ".label{color:#6b7280;font-weight:500;}" +
               ".value{color:#111827;font-weight:600;text-align:right;}" +
               ".ref-box{background:linear-gradient(135deg,#06b6d4,#00e5ff);border-radius:10px;padding:16px;text-align:center;margin-bottom:24px;}" +
               ".ref-box p{margin:0;color:#07070f;font-size:12px;font-weight:600;letter-spacing:.08em;text-transform:uppercase;}" +
               ".ref-box h3{margin:6px 0 0;color:#07070f;font-size:24px;font-weight:900;letter-spacing:.1em;}" +
               ".footer{background:#f9fafb;border-top:1px solid #e5e7eb;padding:20px 32px;text-align:center;font-size:12px;color:#9ca3af;}" +
               ".footer a{color:#06b6d4;text-decoration:none;}" +
               "</style></head><body>" +
               "<div class='wrap'>" +
               "  <div class='header'><h1>\uD83C\uDFAB Booking Confirmed!</h1><p>Your ticket is ready \u2014 see you there!</p></div>" +
               "  <div class='body'>" +
               "    <p class='greeting'>Hi <strong>" + name + "</strong>,<br>Your booking has been confirmed. Here are your ticket details:</p>" +
               "    <div class='ref-box'><p>Booking Reference</p><h3>" + ref + "</h3></div>" +
               "    <div class='card'>" +
               "      <h2>" + event + "</h2>" +
               "      <div class='row'><span class='label'>\uD83D\uDCCD Venue</span><span class='value'>" + venue + "</span></div>" +
               "      <div class='row'><span class='label'>\uD83D\uDCC5 Date</span><span class='value'>" + date + "</span></div>" +
               "      <div class='row'><span class='label'>\uD83C\uDFAB Tickets</span><span class='value'>" + qty + "</span></div>" +
               "      <div class='row'><span class='label'>\uD83D\uDCB3 Amount Paid</span><span class='value'>" + amount + "</span></div>" +
               "    </div>" +
               "    <p style='color:#6b7280;font-size:13px;line-height:1.6;'>Please carry this email or your booking reference <strong>" + ref + "</strong> at the venue for check-in.<br>" +
               "    For queries, contact <a href='mailto:support@eventsphere.com' style='color:#06b6d4;'>support@eventsphere.com</a></p>" +
               "  </div>" +
               "  <div class='footer'>&copy; 2025 EventSphere &nbsp;|&nbsp; <a href='http://localhost:8080'>Visit Website</a></div>" +
               "</div></body></html>";
    }
}
