package com.example.paymentmodule.service;

import com.example.paymentmodule.dto.TransactionDto;
import com.example.paymentmodule.entity.TransactionHistory;
import com.example.paymentmodule.entity.Wallet;
import org.springframework.transaction.annotation.Transactional;

public interface WalletService {

    Wallet save(Wallet wallet);

    @Transactional
    TransactionDto transfer(TransactionHistory history);

    Wallet findBalletByUserId(Long userId);
}
