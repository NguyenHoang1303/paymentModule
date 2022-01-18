package com.example.paymentmodule.service;

import com.example.paymentmodule.dto.OrderDto;
import com.example.paymentmodule.dto.TransactionDto;
import com.example.paymentmodule.entity.TransactionHistory;
import org.springframework.transaction.annotation.Transactional;

public interface WalletService {
    @Transactional
    void handlerPayment(OrderDto orderDto);

    @Transactional
    TransactionDto transfer(TransactionHistory history);
}
