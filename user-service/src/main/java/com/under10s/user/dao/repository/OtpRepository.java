package com.under10s.user.dao.repository;


import com.under10s.user.dao.entity.OtpModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OtpRepository extends JpaRepository<OtpModel,Long> {
    OtpModel findTop1ByEmailIdOrderByExpiryDesc(String emailId);
}

