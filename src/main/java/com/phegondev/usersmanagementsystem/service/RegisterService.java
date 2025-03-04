package com.phegondev.usersmanagementsystem.service;

import com.phegondev.usersmanagementsystem.dto.ReqRes;
import com.phegondev.usersmanagementsystem.emailservice.EmailValidator;
import com.phegondev.usersmanagementsystem.entity.OurUsers;
import com.phegondev.usersmanagementsystem.repository.UsersRepo;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class RegisterService {

    @Autowired
    private UsersRepo userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JavaMailSender emailSender;

    @Autowired
    private EmailValidator emailValidator;

    private static final int OTP_EXPIRATION_MINUTES = 10;

    public ReqRes register(ReqRes registrationRequest) {
        ReqRes resp = new ReqRes();

        try {
            // Validate email format
            if (!emailValidator.isEmailValid(registrationRequest.getEmail())) {
                resp.setStatusCode(400);
                resp.setError("Invalid email format or non-existent email address. Please enter a valid email.");
                return resp;
            }

            // Validate email SMTP
            if (!emailValidator.isValidEmailSMTP(registrationRequest.getEmail())) {
                resp.setStatusCode(400);
                resp.setError("Email domain is not reachable. Please enter a valid email.");
                return resp;
            }

            // Check if user already exists
            Optional<OurUsers> existingUser = userRepository.findByEmail(registrationRequest.getEmail());

            if (existingUser.isPresent()) {
                OurUsers user = existingUser.get();

                // If user is unverified and OTP has expired, delete the user and allow re-registration
                if (!user.isVerified() && user.getOtpGeneratedTime().plusMinutes(OTP_EXPIRATION_MINUTES).isBefore(LocalDateTime.now())) {
                    userRepository.delete(user);
                } else {
                    resp.setStatusCode(400);
                    resp.setError("Email is already registered. Please log in.");
                    resp.setVerified(user.isVerified());
                    return resp;
                }
            }

            // Create new user with OTP
            OurUsers tempUser = new OurUsers();
            tempUser.setEmail(registrationRequest.getEmail());
            tempUser.setName(registrationRequest.getName());
            tempUser.setCity(registrationRequest.getCity());
            tempUser.setRole(registrationRequest.getRole());
            tempUser.setPassword(passwordEncoder.encode(registrationRequest.getPassword()));
            tempUser.setOtp(generateOtp(registrationRequest.getEmail()));
            tempUser.setOtpGeneratedTime(LocalDateTime.now());
            tempUser.setVerified(false);

            // Save user with OTP
            userRepository.save(tempUser);

            resp.setMessage("OTP sent successfully to your email. Please verify to complete registration.");
            resp.setStatusCode(200);

        } catch (Exception e) {
            resp.setStatusCode(500);
            resp.setError("An error occurred: " + e.getMessage());
        }

        return resp;
    }


    // Generate OTP and send it to email
    private String generateOtp(String email) {
        try {
            String otp = String.format("%06d", new Random().nextInt(999999));
            sendOtpToEmail(email, otp);
            return otp;
        } catch (Exception e) {
            return null;
        }
    }

    // Send OTP to the provided email address

    private void sendOtpToEmail(String email, String otp) {
        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(email);
            helper.setSubject("Your OTP Code");
            helper.setText("Your OTP code is: " + otp + "\n\nThis OTP is valid for " + OTP_EXPIRATION_MINUTES + " minutes.", true); // Supports HTML

            // ✅ Correctly set a custom sender name with an email
            helper.setFrom("Yadav Group Of Industry <sy715050@gmail.com>");

            emailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Error sending OTP to email: " + e.getMessage());
        }
    }


    // OTP verification method


    private void sendRegistrationSuccessEmail(String email, String name, String password) {
        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(email);
            helper.setSubject("Welcome to Yadav Group Of Industry!");

            // ✅ Use HTML for better formatting (Username as heading, Password below)
            String emailContent = "<h2>Welcome, " + name + "!</h2>" +
                    "<p>Your registration is successful. You can now log in to your account.</p>" +
                    "<p><a href='https://yourwebsite.com/login'>Click here to log in</a></p>" +
                    "<p>If you did not register, please contact our support team.</p>";

            helper.setText(emailContent, true); // ✅ Enable HTML formatting

            // ✅ Set the sender name correctly
            helper.setFrom("Yadav Group Of Industry <sy715050@gmail.com>");

            emailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Error sending registration success email: " + e.getMessage());
        }
    }

    public ReqRes verifyOtp(String email, String otp) {
        ReqRes resp = new ReqRes();

        Optional<OurUsers> optionalUser = userRepository.findByEmail(email);
        if (!optionalUser.isPresent()) {
            resp.setStatusCode(400);
            resp.setError("OTP has expired or is invalid. Please request a new OTP.");
            return resp;
        }

        OurUsers user = optionalUser.get();

        // Check if user is already verified
        if (user.isVerified()) {
            resp.setStatusCode(400);
            resp.setError("User is already registered and verified.");
            resp.setVerified(true);
            return resp;
        }

        // Check if OTP has expired
        if (user.getOtpGeneratedTime().plusMinutes(OTP_EXPIRATION_MINUTES).isBefore(LocalDateTime.now())) {
            userRepository.delete(user);  // Delete unverified user
            resp.setStatusCode(400);
            resp.setError("OTP has expired. Please request a new OTP.");
            return resp;
        }

        // Check if OTP matches
        if (user.getOtp().equals(otp)) {
            user.setVerified(true);  // ✅ Mark user as verified
            user.setOtp(null);  // ✅ Clear OTP
            user.setOtpGeneratedTime(null);
            userRepository.save(user);  // ✅ Update in database

            // ✅ Send email with username and password after successful registration
            sendRegistrationSuccessEmail(user.getEmail(), user.getName(), user.getPassword());

            resp.setMessage("Registration complete. Your login details have been sent to your email.");
            resp.setStatusCode(200);
            resp.setVerified(true);
        } else {
            resp.setStatusCode(400);
            resp.setError("Invalid OTP. Please try again.");
        }

        return resp;
    }


}

