package com.under10s.user.dao.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "otp_tbl")
@Data
public class OtpModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long otpId;

    @Column(nullable = false)
    private String otp;

    @Column(nullable = false)
    private long expiry;

    @Column
    private String emailId;

    @Column
    private int attempts;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastAttemptTime;
}
