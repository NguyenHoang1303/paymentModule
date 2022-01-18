package com.example.paymentmodule.dto;

import lombok.*;

import java.util.HashMap;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class OrderDto {

    private Long orderId;
    private Long userId;
    private HashMap<Long, Integer> productAndQuantity;
    private double totalPrice;
    private String checkout;
    private String status;
    private String device_token;
}
