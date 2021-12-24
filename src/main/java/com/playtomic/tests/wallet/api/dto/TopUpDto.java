package com.playtomic.tests.wallet.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TopUpDto {

    private BigDecimal amount;
    private String creditCardNumber;
}
