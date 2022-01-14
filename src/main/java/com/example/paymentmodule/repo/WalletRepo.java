package com.example.paymentmodule.repo;

import com.example.paymentmodule.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletRepo extends JpaRepository<Wallet, Long> {
    Wallet findBalletByUserId(Long id);
}
