package com.playtomic.tests.wallet.repository;

import org.springframework.lang.NonNull;

import java.math.BigDecimal;

public interface WalletCustomRepository {

    /**
     * Reduces wallets balance by certain amount.
     *
     * @param id
     * @param amount
     */
    void emptyWallet(@NonNull String id, @NonNull BigDecimal amount);

    /**
     * Increases wallets balance by certain amount.
     *
     * @param id
     * @param amount
     */
    void topUpWallet(@NonNull String id, @NonNull BigDecimal amount);

}
