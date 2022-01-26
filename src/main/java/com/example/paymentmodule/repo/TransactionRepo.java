package com.example.paymentmodule.repo;

import com.example.paymentmodule.entity.TransactionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TransactionRepo extends JpaRepository<TransactionHistory, Long>, JpaSpecificationExecutor<TransactionHistory> {

    TransactionHistory findTransactionHistoryByOrderId(Long orderId);

    @Query(value = "SELECT * FROM transaction_history WHERE transaction_history.sender_id = 451691 ORDER BY transaction_history.id DESC", nativeQuery = true)
    List<TransactionHistory> findTransactionHistoryBySenderId(Long senderId);

}
