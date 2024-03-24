package com.under10s.user.service;



import com.under10s.user.dao.entity.OtpModel;
import com.under10s.user.dao.entity.UserModel;
import com.under10s.user.dao.repository.OtpRepository;
import com.under10s.user.dao.repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private UserService userService;

    @Autowired
    JwtService jwtService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    OtpRepository otpRepository;

    @Transactional
    public String sendOtpEmail(String toEmail) throws MessagingException, IllegalArgumentException {

        Optional<UserModel> model = Optional.ofNullable(userRepository.findByEmailId(toEmail));
        if (model.isPresent()){
            OtpModel otpModel = generateOtp(toEmail);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom("akbarKhiyani@gmail.com");
            helper.setTo(toEmail);
            helper.setSubject("OTP for password reset");
            helper.setText("Hi," + model.get().getFirstName() + "\n\nYour verification OTP is " + otpModel.getOtp() + "\n\nRegards \nMarketplace Team");
            otpRepository.saveAndFlush(otpModel);
            mailSender.send(message);
            return "OTP sent successfully to " + toEmail;
        }
        return "User Not found!";
    }

    private OtpModel generateOtp(String emailId) {
        OtpModel otpModel = otpRepository.findTop1ByEmailIdOrderByExpiryDesc(emailId);
        int attempts = 1;
        if (otpModel != null) {
            long currentTimeMillis = System.currentTimeMillis();
            long timeDifference = (currentTimeMillis - otpModel.getLastAttemptTime().getTime()) / (1000 * 60);
            if (timeDifference < 10 && otpModel.getAttempts() < 3) {
                attempts = attempts + otpModel.getAttempts();
            } else if (timeDifference < 10) {
                throw new IllegalArgumentException("too many attempts, Please try again later");
            }
        }
        OtpModel newOtp = new OtpModel();
        newOtp.setOtp(String.format("%06d", (int) (Math.random() * 999999)));
        newOtp.setExpiry(System.currentTimeMillis() + 600000);
        newOtp.setEmailId(emailId);
        newOtp.setAttempts(attempts);
        newOtp.setLastAttemptTime(new Date());
        return newOtp;
    }

    @Transactional
    public String validateOtp(String email, String otp) {
        OtpModel otpModel = otpRepository.findTop1ByEmailIdOrderByExpiryDesc(email);
        if (otpModel != null && otpModel.getOtp().equals(otp) && otpModel.getExpiry() >= System.currentTimeMillis()) {
            UserModel user=userRepository.findByEmailId(email);
            if(user.getAttempts() > 0 || user.isLocked()) {
                user.setAttempts(0);
                user.setLocked(false);
                userRepository.saveAndFlush(user);
            }
            Map<String, Object> map = new HashMap<>();
            map.put("userId",user.getUserId());
            String generatedToken=jwtService.generateToken(map,user);
            userService.saveUserToken(user,generatedToken);
            otpRepository.delete(otpModel);
            return generatedToken;
        }else {
            return null;
        }
    }
}