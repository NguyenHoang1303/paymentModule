package com.example.paymentmodule.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "transaction_history")
public class TransactionHistory {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Sender is required")
    private Long senderId;

    @Column(columnDefinition = "bigint default 1")
    @NotNull(message = "Receiver is required")
    private Long receiverId = 1L;

    private Long orderId;
    private String paymentType;  // gửi tiền, refund
    @NotNull(message = "Amount is required")
    private double amount;
    private String status;

    private String message;
    private LocalDate createdAt;
    private LocalDate updatedAt;



    public TransactionHistory(Long senderId, Long orderId, String paymentType, double amount) {
        this.senderId = senderId;
        this.orderId = orderId;
        this.paymentType = paymentType;
        this.amount = amount;
        this.createdAt = LocalDate.now();
    }

    public static final class TransactionHistoryBuilder {
        private Long id;
        private Long senderId;
        private Long receiverId = 1L;
        private Long orderId;
        private String paymentType;  // gửi tiền, refund
        private double amount;
        private String status;
        private String message;
        private LocalDate updatedAt;

        private TransactionHistoryBuilder() {
        }

        public static TransactionHistoryBuilder aTransactionHistory() {
            return new TransactionHistoryBuilder();
        }

        public TransactionHistoryBuilder withId(Long id) {
            this.id = id;
            return this;
        }

        public TransactionHistoryBuilder withSenderId(Long senderId) {
            this.senderId = senderId;
            return this;
        }

        public TransactionHistoryBuilder withReceiverId(Long receiverId) {
            this.receiverId = receiverId;
            return this;
        }

        public TransactionHistoryBuilder withOrderId(Long orderId) {
            this.orderId = orderId;
            return this;
        }

        public TransactionHistoryBuilder withPaymentType(String paymentType) {
            this.paymentType = paymentType;
            return this;
        }

        public TransactionHistoryBuilder withAmount(double amount) {
            this.amount = amount;
            return this;
        }

        public TransactionHistoryBuilder withStatus(String status) {
            this.status = status;
            return this;
        }

        public TransactionHistoryBuilder withMessage(String message) {
            this.message = message;
            return this;
        }

        public TransactionHistoryBuilder withUpdatedAt(LocalDate updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public TransactionHistory build() {
            TransactionHistory transactionHistory = new TransactionHistory();
            transactionHistory.setId(id);
            transactionHistory.setSenderId(senderId);
            transactionHistory.setReceiverId(receiverId);
            transactionHistory.setOrderId(orderId);
            transactionHistory.setPaymentType(paymentType);
            transactionHistory.setAmount(amount);
            transactionHistory.setStatus(status);
            transactionHistory.setMessage(message);
            transactionHistory.setCreatedAt(LocalDate.now());
            transactionHistory.setUpdatedAt(updatedAt);
            return transactionHistory;
        }
    }
}
