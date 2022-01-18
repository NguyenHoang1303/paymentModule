package com.example.paymentmodule.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class TransactionDto {

    private String sender;
    private String receiver;
    private double amount;
    private String message;
}
