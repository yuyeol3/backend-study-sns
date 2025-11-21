package com.example.devSns.repository;

import com.example.devSns.domain.RefreshTokens;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokensRepository extends JpaRepository<RefreshTokens, byte[]> {


}
