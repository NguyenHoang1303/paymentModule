package com.example.paymentmodule.service;

import com.example.paymentmodule.dto.TransactionDto;
import com.example.paymentmodule.entity.TransactionHistory;
import com.example.paymentmodule.entity.Wallet;
import com.example.paymentmodule.enums.PaymentType;
import com.example.paymentmodule.enums.Status;
import com.example.paymentmodule.exception.NotFoundException;
import com.example.paymentmodule.repo.TransactionRepo;
import com.example.paymentmodule.repo.WalletRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class WalletServiceImpl implements WalletService {

    @Autowired
    WalletRepo walletRepo;

    @Autowired
    TransactionRepo transactionRepo;


    @Override
    public Wallet save(Wallet wallet) {
        return walletRepo.save(wallet);
    }


    @Transactional
    @Override
    public TransactionDto transfer(TransactionHistory history) {
        TransactionDto dto = new TransactionDto();
        TransactionHistory historySave = TransactionHistory.Builder.aTransactionHistory()
                .withSenderId(history.getSenderId())
                .withReceiverId(history.getReceiverId())
                .withMessage(history.getMessage())
                .withPaymentType(PaymentType.SENDING.name())
                .build();
        try {

            if (history.getAmount().compareTo(BigDecimal.valueOf(0)) <= 0) {
                historySave.setStatus(Status.Transaction.FAIL.name());
                transactionRepo.save(historySave);
                throw new RuntimeException("Số tiền phải lớn hơn 0");
            }
            Wallet walletSender = walletRepo.findBalletByUserId(history.getSenderId());
            Wallet walletReceiver = walletRepo.findBalletByUserId(history.getReceiverId());

            if (walletSender == null) throw new NotFoundException("Tài khoản người gửi không đúng!");
            if (walletReceiver == null) throw new NotFoundException("Tài khoản nhận gửi không đúng!");
            if (walletSender.getBalance().compareTo(history.getAmount()) < 0)
                throw new RuntimeException("Tài khoản không đủ");


            walletSender.setBalance(walletSender.getBalance().subtract(history.getAmount()));
            walletReceiver.setBalance(walletReceiver.getBalance().add(history.getAmount()));
            dto.setSender(walletSender.getName());
            dto.setReceiver(walletReceiver.getName());
            dto.setMessage(history.getMessage());
            dto.setAmount(history.getAmount());

            walletRepo.save(walletSender);
            walletRepo.save(walletReceiver);
            transactionRepo.save(historySave);
        } catch (Exception e) {
            historySave.setStatus(Status.Transaction.FAIL.name());
            transactionRepo.save(historySave);
            throw new RuntimeException(e.getMessage());
        }

        return dto;
    }

    @Override
    public Wallet findBalletByUserId(Long userId) {
        return walletRepo.findBalletByUserId(userId);
    }
}
