package com.phegondev.usersmanagementsystem.controller;

//import com.phegondev.usersmanagementsystem.model.ReqRes;
import com.phegondev.usersmanagementsystem.dto.ReqRes;
import com.phegondev.usersmanagementsystem.emailservice.EmailValidator;
import com.phegondev.usersmanagementsystem.service.RegisterService;
import com.phegondev.usersmanagementsystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class RegisterController {

//    @Autowired
//    private UserService userService;

    @Autowired
    private EmailValidator emailValidator;

    @Autowired
    private RegisterService registerService;

    // Register endpoint: Starts the registration process and sends OTP to the user's email.
    @PostMapping("/register")
    public ReqRes register(@RequestBody ReqRes registrationRequest) {
        return registerService.register(registrationRequest);
    }

    // Verify OTP endpoint: Verifies the OTP entered by the user for registration completion.
//    @PostMapping("/verify-otp")
//    public ReqRes verifyOtp(@RequestParam String email, @RequestParam String otp) {
//        return registerService.verifyOtp(email, otp);
//    }
//    @PostMapping("/verify-otp")
//    public ReqRes verifyOtp(@RequestBody ReqRes otpRequest) {
//        return registerService.verifyOtp(otpRequest.getEmail(), otpRequest.getOtp());
//    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ReqRes> verifyOtp(@RequestBody ReqRes request) {
        ReqRes response = registerService.verifyOtp(request.getEmail(), request.getOtp());
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getStatusCode()));
    }



    // Optional: A test endpoint to check if email is valid (you can delete this later if not needed)
    @GetMapping("/check-email")
    public boolean checkEmailValidity(@RequestParam String email) {
        return emailValidator.isEmailValid(email);
    }
}

