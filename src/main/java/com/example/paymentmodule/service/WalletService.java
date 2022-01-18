package com.example.paymentmodule.service;

import com.example.paymentmodule.dto.OrderDto;
import com.example.paymentmodule.dto.PaymentDto;
import com.example.paymentmodule.dto.TransactionDto;
import com.example.paymentmodule.entity.TransactionHistory;
import com.example.paymentmodule.entity.Wallet;
import com.example.paymentmodule.enums.PaymentType;
import com.example.paymentmodule.enums.Status;
import com.example.paymentmodule.exception.NotFoundException;
import com.example.paymentmodule.repo.TransactionRepo;
import com.example.paymentmodule.repo.WalletRepo;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.example.paymentmodule.queue.Config.DIRECT_EXCHANGE;
import static com.example.paymentmodule.queue.Config.DIRECT_ROUTING_KEY_PAY;

@Service
public class WalletService {

    @Autowired
    WalletRepo walletRepo;

    @Autowired
    TransactionRepo transactionRepo;

    @Autowired
    RabbitTemplate rabbitTemplate;

    public void handlerPayment(OrderDto orderDto) {
        PaymentDto paymentDto = new PaymentDto(orderDto.getOrderId(),
                orderDto.getUserId(), orderDto.getDevice_token());

        if (orderDto.getCheckout() == null) return;

        if (orderDto.getCheckout().equals(Status.Checkout.REFUND.name())) {
            handlerOrderRefund(orderDto);
            return;
        }

        Wallet wallet = checkWallet(orderDto, paymentDto);
        if (wallet == null) return;

        double totalPrice = orderDto.getTotalPrice();
        double balance = wallet.getBalance();

        if (totalPrice > balance) {
            paymentDto.setMessage("Số dư ví không đủ");
            paymentDto.setCheckout(Status.Checkout.UNPAID.name());
            rabbitTemplate.convertAndSend(DIRECT_EXCHANGE, DIRECT_ROUTING_KEY_PAY, paymentDto);
            return;
        }

        try {
            wallet.setBalance(balance - totalPrice);
            walletRepo.save(wallet);
            TransactionHistory history = TransactionHistory.TransactionHistoryBuilder
                    .aTransactionHistory()
                    .withSenderId(orderDto.getUserId())
                    .withOrderId(orderDto.getOrderId())
                    .withAmount(orderDto.getTotalPrice())
                    .withPaymentType(PaymentType.SENDING.name())
                    .withStatus(Status.Transaction.SUCCESS.name())
                    .build();
            transactionRepo.save(history);

            paymentDto.setCheckout(Status.Checkout.PAID.name());
            paymentDto.setMessage("Thanh toán thành công");
            rabbitTemplate.convertAndSend(DIRECT_EXCHANGE, DIRECT_ROUTING_KEY_PAY, paymentDto);
        } catch (Exception e) {
            rabbitTemplate.convertAndSend(DIRECT_EXCHANGE, DIRECT_ROUTING_KEY_PAY, paymentDto);
            throw new RuntimeException("thanh toán lỗi vui lòng thử lại.");
        }
    }

    private void handlerOrderRefund(OrderDto orderDto) {
        try {
            Wallet wallet = walletRepo.findBalletByUserId(orderDto.getUserId());
            TransactionHistory history = new TransactionHistory(
                    orderDto.getUserId(), orderDto.getOrderId(),
                    PaymentType.REFUND.name(), orderDto.getTotalPrice()
            );
            wallet.setBalance(wallet.getBalance() + orderDto.getTotalPrice());
            history.setStatus(Status.Transaction.SUCCESS.name());
            walletRepo.save(wallet);
            transactionRepo.save(history);
        } catch (Exception e) {
            throw new RuntimeException("refund order fail.");
        }
    }

    private Wallet checkWallet(OrderDto orderDto, PaymentDto paymentDto) {
        if (orderDto.getUserId() == null) {
            paymentDto.setMessage("Tài khoản ví không được trống");
            rabbitTemplate.convertAndSend(DIRECT_EXCHANGE, DIRECT_ROUTING_KEY_PAY, paymentDto);
            return null;
        }

        Wallet wallet = walletRepo.findBalletByUserId(orderDto.getUserId());
        if (wallet == null) {
            paymentDto.setMessage("Tài khoản thanh toán không đúng");
            paymentDto.setCheckout(Status.Checkout.UNPAID.name());
            rabbitTemplate.convertAndSend(DIRECT_EXCHANGE, DIRECT_ROUTING_KEY_PAY, paymentDto);
            return null;
        }
        return wallet;
    }


    @Transactional
    public TransactionDto transfer(TransactionHistory history) {
        TransactionDto dto = new TransactionDto();

        if (history.getAmount() <= 0) throw new RuntimeException("Số tiền phải lớn hơn 0");
        Wallet walletSender = walletRepo.findBalletByUserId(history.getSenderId());
        Wallet walletReceiver = walletRepo.findBalletByUserId(history.getReceiverId());

        if (walletSender == null) throw new NotFoundException("wallet sender not found!");
        if (walletReceiver == null) throw new NotFoundException("wallet receiver not found!");
        if (walletSender.getBalance() < history.getAmount()) throw new RuntimeException("Tài khoản không đủ");

        try {
            walletSender.setBalance(walletSender.getBalance() - history.getAmount());
            walletReceiver.setBalance(walletReceiver.getBalance() + history.getAmount());

            System.out.println(walletReceiver);
            System.out.println(walletSender);

            walletRepo.save(walletSender);
            walletRepo.save(walletReceiver);

            TransactionHistory historySave = TransactionHistory.TransactionHistoryBuilder.aTransactionHistory()
                    .withSenderId(history.getSenderId())
                    .withReceiverId(history.getReceiverId())
                    .withMessage(history.getMessage())
                    .withPaymentType(PaymentType.SENDING.name())
                    .withStatus(Status.Transaction.SUCCESS.name())
                    .build();

            transactionRepo.save(historySave);

            dto.setSender(walletSender.getName());
            dto.setReceiver(walletReceiver.getName());
            dto.setMessage(history.getMessage());
            dto.setAmount(history.getAmount());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

        return dto;
    }
}
