package com.playtomic.tests.wallet.repository.model;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Builder
@Getter
@Document(collection = "transactions")
public class Transaction {

    /**
     * Amount of money to update wallets balance
     */
    private BigDecimal amount;
    /**
     * Number of credit card. Used only when performing top up action.
     */
    private String creditCardNumber;
    @Id
    private String id;
    /**
     * Id of payment. Can be id of purchase or id of transaction for wallet top up.
     */
    private String paymentId;
    /**
     * {@link TransactionType}
     */
    private String transactionType;
    @Version
    private Integer version;
    private String walletId;
}
