package com.playtomic.tests.wallet.api.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Builder
@Getter
public class WalletDto {

    private BigDecimal balance;
    private String id;
}
