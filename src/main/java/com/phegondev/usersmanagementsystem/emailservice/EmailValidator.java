//package com.phegondev.usersmanagementsystem.emailservice;
//
//import org.springframework.stereotype.Component;
//import org.springframework.web.client.RestTemplate;
//import org.springframework.http.ResponseEntity;
//import org.json.JSONObject;
//import java.net.InetAddress;
//import java.util.Properties;
//import jakarta.mail.Session;
//import jakarta.mail.Transport;
//
//@Component
//public class EmailValidator {
//
//    // ✅ API-Based Email Validation (Using Abstract API)
//    public static boolean isEmailValid(String email) {
//        String apiKey = "3a7a2ced187b4979a7765e5e2ddf145e";  // Replace with your Abstract API Key
//        String url = "https://emailvalidation.abstractapi.com/v1/?api_key=" + apiKey + "&email=" + email;
//
//        RestTemplate restTemplate = new RestTemplate();
//        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
//
//        if (response.getStatusCodeValue() == 200) {
//            JSONObject jsonResponse = new JSONObject(response.getBody());
//            return jsonResponse.getBoolean("is_valid_format") && jsonResponse.getBoolean("is_smtp_valid");
//        }
//        return false;
//    }
//
//
//    // ✅ SMTP-Based Email Validation
//    public static boolean isValidEmailSMTP(String email) {
//        try {
//            Properties props = new Properties();
//            props.put("mail.smtp.host", "smtp.gmail.com"); // Change for other providers
//            props.put("mail.smtp.auth", "false");
//            props.put("mail.smtp.port", "25"); // Some providers block this
//            props.put("mail.smtp.timeout", "2000");
//
//            Session session = Session.getInstance(props);
//            Transport transport = session.getTransport("smtp");
//
//            // Resolve domain MX record
//            String domain = email.substring(email.indexOf("@") + 1);
//            InetAddress inetAddress = InetAddress.getByName(domain);
//            return inetAddress.isReachable(2000);  // Returns true if email domain is valid
//
//        } catch (Exception e) {
//            return false;
//        }
//    }
//
//    // ✅ Main Method for Testing
//    public static void main(String[] args) {
//        String testEmail = "test@example.com";
//
//        System.out.println("API Validation: " + isEmailValid(testEmail));
//        System.out.println("SMTP Validation: " + isValidEmailSMTP(testEmail));
//    }
//}
//

package com.phegondev.usersmanagementsystem.emailservice;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.json.JSONObject;
import java.net.InetAddress;
import java.util.Properties;
import jakarta.mail.Session;
import jakarta.mail.Transport;

@Component
public class EmailValidator {

    // API-Based Email Validation (Using Abstract API)
//    public boolean isEmailValid(String email) {
//        String apiKey = "3a7a2ced187b4979a7765e5e2ddf145e";  // Replace with your Abstract API Key
//        String url = "https://emailvalidation.abstractapi.com/v1/?api_key=" + apiKey + "&email=" + email;
//
//        RestTemplate restTemplate = new RestTemplate();
//        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
//
//        if (response.getStatusCodeValue() == 200) {
//            JSONObject jsonResponse = new JSONObject(response.getBody());
//            return jsonResponse.getBoolean("is_valid_format") && jsonResponse.getBoolean("is_smtp_valid");
//        }
//        return false;
//    }

    public static boolean isEmailValid(String email) {
        String apiKey = "3a7a2ced187b4979a7765e5e2ddf145e";  // Replace with your Abstract API Key
        String url = "https://emailvalidation.abstractapi.com/v1/?api_key=" + apiKey + "&email=" + email;

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        if (response.getStatusCodeValue() == 200) {
            // Log the response for debugging
            System.out.println("API Response: " + response.getBody());

            JSONObject jsonResponse = new JSONObject(response.getBody());

            // Access the "value" field of the is_valid_format and is_smtp_valid objects
            boolean isValidFormat = jsonResponse.getJSONObject("is_valid_format").getBoolean("value");
            boolean isSmtpValid = jsonResponse.getJSONObject("is_smtp_valid").getBoolean("value");

            return isValidFormat && isSmtpValid;
        }
        return false;
    }




    // SMTP-Based Email Validation
    public boolean isValidEmailSMTP(String email) {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.host", "smtp.gmail.com"); // Change for other providers
            props.put("mail.smtp.auth", "false");
            props.put("mail.smtp.port", "25"); // Some providers block this
            props.put("mail.smtp.timeout", "2000");

            Session session = Session.getInstance(props);
            Transport transport = session.getTransport("smtp");

            // Resolve domain MX record
            String domain = email.substring(email.indexOf("@") + 1);
            InetAddress inetAddress = InetAddress.getByName(domain);
            return inetAddress.isReachable(2000);  // Returns true if email domain is valid

        } catch (Exception e) {
            return false;
        }
    }
}

