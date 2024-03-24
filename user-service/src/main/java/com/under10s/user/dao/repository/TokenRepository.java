package com.under10s.user.dao.repository;


import com.under10s.user.dao.entity.TokenModel;
import com.under10s.user.dao.entity.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TokenRepository extends JpaRepository<TokenModel,Long> {
    TokenModel findByUser(UserModel user);

    TokenModel findByToken(String token);
}
