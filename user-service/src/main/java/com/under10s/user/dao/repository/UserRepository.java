package com.under10s.user.dao.repository;


import com.under10s.user.dao.entity.UserModel;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository

public interface UserRepository extends JpaRepository<UserModel, Long> {

    UserModel findByEmailId(String email);
}
