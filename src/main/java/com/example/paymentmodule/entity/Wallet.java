package com.example.paymentmodule.entity;


import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "wallets")
@ToString
public class Wallet {
    @Id
    @Column(name = "id", nullable = false)
    private Long userId;
    private BigDecimal balance;
    private String name;

}
