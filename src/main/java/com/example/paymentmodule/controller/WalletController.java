package com.example.paymentmodule.controller;

import com.example.paymentmodule.entity.TransactionHistory;
import com.example.paymentmodule.entity.Wallet;
import com.example.paymentmodule.repo.TransactionRepo;
import com.example.paymentmodule.repo.WalletRepo;
import com.example.paymentmodule.response.RESTResponse;
import com.example.paymentmodule.service.WalletServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@CrossOrigin("*")
@RequestMapping("/api/v1/payments/")
public class WalletController {

    @Autowired
    WalletRepo walletRepo;

    @Autowired
    TransactionRepo transactionRepo;

    @Autowired
    WalletServiceImpl walletService;

    @RequestMapping(path = "account/{userId}", method = RequestMethod.GET)
    public ResponseEntity find(@PathVariable int userId) {
        Wallet wallet = walletRepo.findBalletByUserId((long) userId);
        return new ResponseEntity<>(new RESTResponse.Success()
                .addData(wallet)
                .build(), HttpStatus.OK);
    }

    @RequestMapping(path = "transfer", method = RequestMethod.POST)
    public ResponseEntity send(@Valid @RequestBody TransactionHistory history) {
        return new ResponseEntity<>(new RESTResponse.Success()
                .addData(walletService.transfer(history))
                .build(), HttpStatus.OK);
    }


}
