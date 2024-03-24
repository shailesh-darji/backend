package com.under10s.user.rest;


import com.under10s.user.api.UserServiceURI;
import com.under10s.user.dto.UserDetailsDTO;
import com.under10s.user.service.EmailService;
import com.under10s.user.service.JwtService;
import com.under10s.user.service.UserService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(UserServiceURI.URI_USER_SERVICE)
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private EmailService emailService;

    @PostMapping(value = UserServiceURI.URI_REGISER_USER, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> register(@Valid @RequestBody UserDetailsDTO userDetailsDto) {
        try {
            UserDetailsDTO userDetailsDTO = userService.register(userDetailsDto);
            return ResponseEntity.ok().headers(userService.setAuthTokenInHeaders(userDetailsDTO.getToken())).body(userDetailsDTO);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping(UserServiceURI.URI_LOGIN)
    public ResponseEntity<?> login(@Valid  @NotEmpty @RequestBody Map<String, String> dataMap) {
        try {
            UserDetailsDTO userDetailsDTO = userService.login(dataMap.get("email"), dataMap.get("password"));
            return ResponseEntity.ok().headers(userService.setAuthTokenInHeaders(userDetailsDTO.getToken())).body(userDetailsDTO);
        } catch (BadCredentialsException e) {
            userService.updateLoginAttempts(null, dataMap.get("email"), false);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (LockedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @GetMapping(UserServiceURI.URI_FORGOT_PASSWORD_OTP)
    public ResponseEntity<String> sendOtpEmail(@Valid @NotBlank @Email(message = "Invalid email provided!") @RequestParam String emailId) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(emailService.sendOtpEmail(emailId));
        } catch (MessagingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
    }

    @GetMapping(UserServiceURI.URI_VALIDATE_OTP)
    public ResponseEntity<String> isValidOtp(@Valid @NotBlank @Email(message = "Invalid email provided!") @RequestParam String toEmail,@NotBlank @RequestParam String otp) {
        String token = emailService.validateOtp(toEmail, otp);
        if (token != null) {
            return ResponseEntity.status(HttpStatus.OK).headers(userService.setAuthTokenInHeaders(token)).body("OTP verified!");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("OTP invalid!");
        }
    }

    @PostMapping(UserServiceURI.URI_UPDATE_PASSWORD)
    public ResponseEntity<String> updatePassword(@Valid @NotEmpty @RequestBody Map<String, String> dataMap) {
        String result = userService.updatePassword(dataMap);
        return ResponseEntity.ok(result);
    }
}
