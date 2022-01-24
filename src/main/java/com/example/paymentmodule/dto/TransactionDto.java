package com.example.paymentmodule.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class TransactionDto {

    private String sender;
    private String receiver;
    private BigDecimal amount;
    private String message;
}
