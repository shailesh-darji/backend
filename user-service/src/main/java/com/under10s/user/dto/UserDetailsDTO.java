package com.under10s.user.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDetailsDTO implements Serializable {

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters long")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters long")
    private String lastName;

    @NotBlank(message = "Mobile number is required")
    @Size(min = 10, max = 15, message = "Mobile number must be between 10 and 15 digits long")
    private String mobileNumber;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email provided!")
    private String emailId;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 20, message = "Password  must be between 10 and 15 digits long")
    private String password;

    private int roleId = 1;

    @JsonIgnore
    private String token;

    private Long userId;
}