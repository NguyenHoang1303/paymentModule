package com.example.paymentmodule.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class OrderDto {

    private Long orderId;
    private Long userId;
    private Set<OrderDetailDto> orderDetails;
    private BigDecimal totalPrice;
    private String paymentStatus;
    private String inventoryStatus;
    private String orderStatus;
    private String device_token;
    private String message;

    public boolean validationPayment(){
        return this.totalPrice.compareTo(BigDecimal.valueOf(0)) > 0
                && this.orderId != null && this.userId != null && this.paymentStatus != null;
    }
}
