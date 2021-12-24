package com.playtomic.tests.wallet.repository.model;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;

@Builder
@Getter
@Document(collection = "wallets")
public class Wallet {
    @Field("balance")
    private BigDecimal balance;
    @Id
    private String id;
}
