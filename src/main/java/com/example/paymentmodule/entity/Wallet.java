package com.example.paymentmodule.entity;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "wallets")
public class Wallet {
    @Id
    @Column(name = "id", nullable = false)
    private Long userId;
    private double balance;
    private String name;

}
