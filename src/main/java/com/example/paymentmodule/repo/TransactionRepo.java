package com.example.paymentmodule.repo;

import com.example.paymentmodule.entity.TransactionHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepo extends JpaRepository<TransactionHistory, Long> {

    TransactionHistory findTransactionHistoryByOrderId(Long orderId);

}
