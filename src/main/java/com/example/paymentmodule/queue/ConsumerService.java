package com.example.paymentmodule.queue;

import com.example.paymentmodule.dto.OrderDto;
import com.example.paymentmodule.entity.TransactionHistory;
import com.example.paymentmodule.entity.Wallet;
import com.example.paymentmodule.enums.PaymentType;
import com.example.paymentmodule.enums.Status;
import com.example.paymentmodule.repo.TransactionRepo;
import com.example.paymentmodule.service.WalletService;
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
    public void handlerPayment(OrderDto orderDto) {
        if (!orderDto.validationPayment()){
            orderDto.setMessage("Kiểm tra thông tin đơn hàng");
            rabbitTemplate.convertAndSend(DIRECT_EXCHANGE, DIRECT_ROUTING_KEY_ORDER_PAY, orderDto);
            return;
        }
        if (orderDto.getPaymentStatus().equals(Status.Payment.REFUND.name())) {
            handlerOrderRefund(orderDto);
            return;
        }
        if (orderDto.getPaymentStatus().equals(Status.Payment.UNPAID.name())) {
            handlerOrderUnpaid(orderDto);
        }
    }

    @Transactional
    void handlerOrderRefund(OrderDto orderDto) {
        Wallet wallet = checkWalletExist(orderDto);
        if (wallet == null) return;
        TransactionHistory history = TransactionHistory.Builder.aTransactionHistory()
                .withSenderId(orderDto.getUserId())
                .withOrderId(orderDto.getOrderId())
                .withAmount(orderDto.getTotalPrice())
                .withPaymentType(PaymentType.REFUND.name())
                .build();

        try {
            wallet.setBalance(wallet.getBalance().add(orderDto.getTotalPrice()));
            history.setStatus(Status.Transaction.SUCCESS.name());
            orderDto.setPaymentStatus(Status.Payment.REFUNDED.name());
            walletService.save(wallet);
             transactionRepo.save(history);
            rabbitTemplate.convertAndSend(DIRECT_EXCHANGE, DIRECT_ROUTING_KEY_ORDER_PAY, orderDto);
        } catch (Exception e) {
            history.setStatus(Status.Transaction.FAIL.name());
            transactionRepo.save(history);
            rabbitTemplate.convertAndSend(DIRECT_EXCHANGE, DIRECT_ROUTING_KEY_PAY, orderDto);
            throw new RuntimeException("refund order fail.");
        }
    }

    private void handlerOrderUnpaid(OrderDto orderDto) {
        Wallet wallet = checkWalletExist(orderDto);
        if (wallet == null) return;

        BigDecimal totalPrice = orderDto.getTotalPrice();
        BigDecimal balance = wallet.getBalance();

        if (totalPrice.compareTo(balance) > 0) {
            orderDto.setMessage("Số dư ví không đủ");
            orderDto.setPaymentStatus(Status.Payment.FAIL.name());
            rabbitTemplate.convertAndSend(DIRECT_EXCHANGE, DIRECT_ROUTING_KEY_ORDER_PAY, orderDto);
            return;
        }

        TransactionHistory history = TransactionHistory.Builder
                .aTransactionHistory()
                .withSenderId(orderDto.getUserId())
                .withOrderId(orderDto.getOrderId())
                .withAmount(orderDto.getTotalPrice())
                .withPaymentType(PaymentType.SENDING.name())
                .build();

        try {
            wallet.setBalance(balance.subtract(totalPrice));
            history.setStatus(Status.Transaction.SUCCESS.name());
            orderDto.setPaymentStatus(Status.Payment.PAID.name());
            orderDto.setMessage("Thanh toán thành công");
            walletService.save(wallet);
            transactionRepo.save(history);
            rabbitTemplate.convertAndSend(DIRECT_EXCHANGE, DIRECT_ROUTING_KEY_ORDER_PAY, orderDto);
        } catch (Exception e) {
            history.setStatus(Status.Transaction.FAIL.name());
            transactionRepo.save(history);
            rabbitTemplate.convertAndSend(DIRECT_EXCHANGE, DIRECT_ROUTING_KEY_PAY, orderDto);
            throw new RuntimeException("thanh toán lỗi vui lòng thử lại.");
        }
    }

    private Wallet checkWalletExist(OrderDto orderDto) {
        Wallet wallet = walletService.findBalletByUserId(orderDto.getUserId());
        if (wallet == null) {
            orderDto.setMessage("Tài khoản thanh toán không đúng");
            orderDto.setPaymentStatus(Status.Payment.UNPAID.name());
            rabbitTemplate.convertAndSend(DIRECT_EXCHANGE, DIRECT_ROUTING_KEY_ORDER_PAY, orderDto);
            return null;
        }
        return wallet;
    }

}
