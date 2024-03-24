package com.under10s.user.service;



import com.under10s.user.dao.entity.RoleModel;
import com.under10s.user.dao.entity.TokenModel;
import com.under10s.user.dao.entity.UserModel;
import com.under10s.user.dao.repository.RoleRepository;
import com.under10s.user.dao.repository.TokenRepository;
import com.under10s.user.dao.repository.UserRepository;
import com.under10s.user.dto.UserDetailsDTO;
import com.under10s.user.helper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    JwtService jwtService;

    @Autowired
    TokenRepository tokenRepository;

    @Transactional
    public UserDetailsDTO register(UserDetailsDTO userDetailsDTO) {
        LOGGER.debug("UserService.register: {}", userDetailsDTO.toString());
        if (userRepository.findByEmailId(userDetailsDTO.getEmailId()) != null) {
            throw new IllegalArgumentException("User Already Exits");
        }
        UserModel userModel = ModelMapper.getUserModelFromRegisterDTO(userDetailsDTO);
        Optional<RoleModel> userRole = roleRepository.findById((long) userDetailsDTO.getRoleId());
        userRole.ifPresent(userModel::setRoleModel);
        userModel.setPassword(passwordEncoder.encode(userDetailsDTO.getPassword()));
        userRepository.saveAndFlush(userModel);
        return login(userModel.getEmailId(), userDetailsDTO.getPassword());
    }

    @Transactional
    public UserDetailsDTO login(String email, String password) {
        LOGGER.debug("UserService.login : {} , {}", email, password);
        UserModel user = null;
        Authentication authenticate = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
        user = (UserModel) authenticate.getPrincipal();
        if (user.getAttempts() > 0) {
            updateLoginAttempts(user, null, true);
        }
        Map<String, Object> map = new HashMap<>();
        map.put("userId", user.getUserId());
        String generatedToken = jwtService.generateToken(map, user);
        saveUserToken(user, generatedToken);
        UserDetailsDTO responseUserDetailsDTO = ModelMapper.getResponseUserDetailsDtoFromUserModel(user);
        responseUserDetailsDTO.setToken(generatedToken);
        return responseUserDetailsDTO;
    }

    @Transactional
    public void updateLoginAttempts(UserModel userModel, String emailId, boolean isSuccess) {
        if (userModel == null) {
            userModel = userRepository.findByEmailId(emailId);
        }
        LOGGER.debug("UserService.updateLoginAttempts : {} , {}, {}",userModel.toString(), emailId, isSuccess);
        if (isSuccess) {
            userModel.setAttempts(0);
        } else {
            if (userModel.getAttempts() >= 2) {
                userModel.setLocked(true);
            }
            userModel.setAttempts(userModel.getAttempts() + 1);
        }
        userRepository.saveAndFlush(userModel);
    }

    public void saveUserToken(UserModel user, String jwtToken) {
        LOGGER.debug("UserService.saveUserToken : {} , {}",user.toString(), jwtToken);
        TokenModel existingToken = tokenRepository.findByUser(user);
        if (existingToken != null) {
            existingToken.setToken(jwtToken);
            tokenRepository.saveAndFlush(existingToken);
        } else {
            TokenModel token = TokenModel.builder()
                    .user(user)
                    .token(jwtToken)
                    .build();
            tokenRepository.saveAndFlush(token);
        }
    }

    public HttpHeaders setAuthTokenInHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + token);
        headers.add("Expires", String.valueOf(jwtService.extractExpiration(token)));
        return headers;
    }

    @Transactional
    public String updatePassword(Map<String, String> dataMap) {
        LOGGER.debug("UserService.updatePassword : {}", dataMap.toString());
        String newPassword, currentPassword, confirmPassword;
        newPassword = dataMap.get("newPassword");
        currentPassword = dataMap.get("currentPassword");
        confirmPassword = dataMap.get("confirmPassword");
        if (!newPassword.equals(confirmPassword)) {
            return "New password and confirm password should match!";
        }
        UserModel userModel = (UserModel) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!passwordEncoder.matches(currentPassword, userModel.getPassword())) {
            updateLoginAttempts(userModel, null, false);
            return "Invalid current password!";
        }
        userModel.setPassword(passwordEncoder.encode(newPassword));
        userRepository.saveAndFlush(userModel);
        return "Password Updated!";
    }
}
