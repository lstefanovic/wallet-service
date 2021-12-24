package com.playtomic.tests.wallet.api.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class TransactionDto {

    private BigDecimal amount;
    private String creditCardNumber;
    private String paymentId;
    private String transactionType;
}
