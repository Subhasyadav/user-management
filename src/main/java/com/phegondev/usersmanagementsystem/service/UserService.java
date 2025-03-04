package com.phegondev.usersmanagementsystem.service;

import com.phegondev.usersmanagementsystem.dto.ReqRes;
import com.phegondev.usersmanagementsystem.entity.OurUsers;
import com.phegondev.usersmanagementsystem.repository.UsersRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class UserService {
    @Autowired
    private UsersRepo userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public String generateOtp(String email) {
        Optional<OurUsers> userOptional = userRepository.findByEmail(email);
        if (userOptional.isPresent()) {
            OurUsers user = userOptional.get();
            String otp = String.format("%06d", new Random().nextInt(999999)); // Generate OTP
            user.setOtp(otp);  // Set OTP
            user.setOtpGeneratedTime(LocalDateTime.now()); // Set OTP generation time

            // Save the updated user with OTP in the database
            userRepository.save(user);

            // Send OTP email
            emailService.sendOtpEmail(email, otp);
            return "OTP sent successfully.";
        }
        return "User not found.";
    }


    public String verifyOtp(String email, String otp) {
        Optional<OurUsers> userOptional = userRepository.findByEmail(email);
        if (userOptional.isPresent()) {
            OurUsers user = userOptional.get();

            // Check if OTP exists and matches
            if (user.getOtp() != null && user.getOtp().equals(otp)) {
                // Check OTP expiration (expired if more than 5 minutes have passed)
                if (user.getOtpGeneratedTime().plusMinutes(5).isBefore(LocalDateTime.now())) {
                    return "OTP expired.";  // Return OTP expired message if expired
                }

                // OTP is valid, return success message (do not clear OTP here)
                return "OTP verified.";
            } else {
                return "Invalid OTP.";  // OTP doesn't match
            }
        }
        return "User not found.";  // User not found for the given email
    }




    public String resetPassword(String email, String otp, String password) {
        Optional<OurUsers> userOptional = userRepository.findByEmail(email);
        if (userOptional.isPresent()) {
            OurUsers user = userOptional.get();

            // Check if OTP exists and matches
            if (user.getOtp() != null && user.getOtp().equals(otp)) {
                // Check OTP expiration (expired if more than 5 minutes have passed)
                if (user.getOtpGeneratedTime().plusMinutes(5).isBefore(LocalDateTime.now())) {
                    return "OTP expired.";  // Return OTP expired message if expired
                }

                // Reset password if OTP is valid and not expired
                user.setPassword(passwordEncoder.encode(password));  // Encode the new password
                user.setOtp(null);  // Clear OTP after password reset
                userRepository.save(user);  // Save updated user with new password

                return "Password reset successful.";  // Return success message
            } else {
                return "Invalid OTP.";  // OTP doesn't match
            }
        }
        return "User not found.";  // User not found for the given email
    }


//    public ReqRes register(ReqRes registrationRequest){
//        ReqRes resp = new ReqRes();
//
//        try {
//            // Create a new user object
//            OurUsers ourUser = new OurUsers();
//            ourUser.setEmail(registrationRequest.getEmail());
//            ourUser.setCity(registrationRequest.getCity());
//            ourUser.setRole(registrationRequest.getRole());
//            ourUser.setName(registrationRequest.getName());
//            ourUser.setPassword(passwordEncoder.encode(registrationRequest.getPassword()));
//
////             Save the user to the database (but don’t finalize the registration yet)
//            OurUsers ourUsersResult = userRepository.save(ourUser);
//
//            if (ourUsersResult.getId() > 0) {
//                // Generate OTP for email verification
//                String otpResponse = generateOtp(ourUsersResult.getEmail());
//                if ("OTP sent successfully.".equals(otpResponse)) {
//                    resp.setMessage("OTP sent successfully to your email. Please verify to complete registration.");
//                    resp.setStatusCode(200);
//                } else {
//                    // Handle the case where OTP couldn't be sent (error in OTP generation or sending)
//                    resp.setStatusCode(500);
//                    resp.setError("Failed to send OTP.");
//                }
//            } else {
//                resp.setStatusCode(500);
//                resp.setError("Error saving user.");
//            }
//
//        } catch (Exception e) {
//            resp.setStatusCode(500);
//            resp.setError("An error occurred: " + e.getMessage());
//        }
//        return resp;
//    }
//
//
//    public ReqRes verifyEmailOtp(String email, String otp){
//        ReqRes resp = new ReqRes();
//
//        // Call the verifyOtp method to validate the OTP
//        String otpVerificationResponse = verifyOtp(email, otp);
//
//        if ("OTP verified.".equals(otpVerificationResponse)) {
//            resp.setMessage("Email verified successfully. Registration complete.");
//            resp.setStatusCode(200);
//        } else if ("OTP expired.".equals(otpVerificationResponse)) {
//            resp.setStatusCode(400);
//            resp.setMessage("OTP expired. Please request a new OTP.");
//        } else {
//            resp.setStatusCode(400);
//            resp.setMessage("Invalid OTP. Please try again.");
//        }
//
//        return resp;
//    }
}

