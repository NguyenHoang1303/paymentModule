package com.example.paymentmodule.queue;

import com.example.paymentmodule.entity.TransactionHistory;
import com.example.paymentmodule.entity.Wallet;
import com.example.paymentmodule.enums.PaymentStatus;
import com.example.paymentmodule.enums.PaymentType;
import com.example.paymentmodule.enums.Status;
import com.example.paymentmodule.repo.TransactionRepo;
import com.example.paymentmodule.service.WalletService;
import common.event.OrderEvent;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static com.example.paymentmodule.queue.Config.*;

@Component
@Log4j2
public class ConsumerService {

    @Autowired
    WalletService walletService;

    @Autowired
    TransactionRepo transactionRepo;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Transactional
    public void handlerPayment(OrderEvent orderEvent) {
        orderEvent.setQueueName(QUEUE_PAY);
        if (!orderEvent.validationPayment()) {
            orderEvent.setMessage("Kiểm tra thông tin đơn hàng");
            rabbitTemplate.convertAndSend(DIRECT_EXCHANGE, DIRECT_ROUTING_KEY_ORDER, orderEvent);
            return;
        }
        if (orderEvent.getPaymentStatus().equals(PaymentStatus.REFUND.name())) {
            handlerOrderRefund(orderEvent);
            return;
        }
        if (orderEvent.getPaymentStatus().equals(PaymentStatus.UNPAID.name())) {
            handlerOrderUnpaid(orderEvent);
        }
    }

    @Transactional
    void handlerOrderRefund(OrderEvent orderEvent) {
        Wallet wallet = checkWalletExist(orderEvent);
        if (wallet == null) return;
        TransactionHistory history = TransactionHistory.Builder.aTransactionHistory()
                .withSenderId(orderEvent.getUserId())
                .withOrderId(orderEvent.getOrderId())
                .withAmount(orderEvent.getTotalPrice())
                .withPaymentType(PaymentType.REFUND.name())
                .build();

        try {
            wallet.setBalance(wallet.getBalance().add(orderEvent.getTotalPrice()));
            history.setStatus(Status.Transaction.SUCCESS.name());
            orderEvent.setPaymentStatus(Status.Payment.REFUNDED.name());
            walletService.save(wallet);
            transactionRepo.save(history);

            rabbitTemplate.convertAndSend(DIRECT_EXCHANGE, DIRECT_ROUTING_KEY_ORDER, orderEvent);
        } catch (Exception e) {
            history.setStatus(Status.Transaction.FAIL.name());
            transactionRepo.save(history);
            rabbitTemplate.convertAndSend(DIRECT_EXCHANGE, DIRECT_ROUTING_KEY_PAY, orderEvent);
            throw new RuntimeException("refund order fail.");
        }
    }

    void handlerOrderUnpaid(OrderEvent orderEvent) {
        Wallet wallet = checkWalletExist(orderEvent);
        if (wallet == null) return;

        BigDecimal totalPrice = orderEvent.getTotalPrice();
        BigDecimal balance = wallet.getBalance();

        if (totalPrice.compareTo(balance) > 0) {
            orderEvent.setMessage("Số dư ví không đủ");
            orderEvent.setPaymentStatus(PaymentStatus.FAIL.name());
            rabbitTemplate.convertAndSend(DIRECT_EXCHANGE, DIRECT_ROUTING_KEY_ORDER, orderEvent);
            return;
        }
        TransactionHistory history = TransactionHistory.Builder
                .aTransactionHistory()
                .withSenderId(orderEvent.getUserId())
                .withOrderId(orderEvent.getOrderId())
                .withAmount(orderEvent.getTotalPrice())
                .withPaymentType(PaymentType.SENDING.name())
                .build();

        try {
            wallet.setBalance(balance.subtract(totalPrice));
            history.setStatus(Status.Transaction.SUCCESS.name());
            orderEvent.setPaymentStatus(Status.Payment.PAID.name());
            orderEvent.setMessage("Thanh toán thành công");
            walletService.save(wallet);
            transactionRepo.save(history);
            rabbitTemplate.convertAndSend(DIRECT_EXCHANGE, DIRECT_ROUTING_KEY_ORDER, orderEvent);
        } catch (Exception e) {
            history.setStatus(Status.Transaction.FAIL.name());
            transactionRepo.save(history);
            rabbitTemplate.convertAndSend(DIRECT_EXCHANGE, DIRECT_ROUTING_KEY_PAY, orderEvent);
            throw new RuntimeException("thanh toán lỗi vui lòng thử lại.");
        }
    }

    private Wallet checkWalletExist(OrderEvent orderEvent) {
        Wallet wallet = walletService.findBalletByUserId(orderEvent.getUserId());
        if (wallet == null) {
            orderEvent.setMessage("Tài khoản thanh toán không đúng");
            orderEvent.setPaymentStatus(Status.Payment.FAIL.name());
            rabbitTemplate.convertAndSend(DIRECT_EXCHANGE, DIRECT_ROUTING_KEY_ORDER, orderEvent);
            return null;
        }
        return wallet;
    }

}
