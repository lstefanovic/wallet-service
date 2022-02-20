package com.playtomic.tests.wallet.repository;

import org.springframework.lang.NonNull;

import java.math.BigDecimal;

public interface WalletCustomRepository {

    /**
     * Updates wallets balance by certain amount.
     *
     * @param id
     * @param amount
     */
    void updateWallet(@NonNull String id, @NonNull BigDecimal amount);

}
