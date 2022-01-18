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
    private String paymentStatus;
    private String orderStatus;
    private String device_token;
}
