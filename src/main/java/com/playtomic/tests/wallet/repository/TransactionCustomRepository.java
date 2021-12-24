package com.playtomic.tests.wallet.repository;

import com.playtomic.tests.wallet.repository.model.Transaction;
import org.springframework.lang.NonNull;

import java.util.List;

public interface TransactionCustomRepository {
    /**
     * Checks if the transaction is already restored.
     *
     * @param walletId
     * @param paymentId
     */
    void checkTransactionRestore(@NonNull String walletId, @NonNull String paymentId);

    /**
     * Search for {@link Transaction} with specific walletId and paymentId.
     *
     * @param walletId
     * @param paymentId
     * @return
     */
    Transaction findTransaction(String walletId, String paymentId);

    /**
     * Lists all transactions by provided walletID.
     *
     * @param walletId
     * @return
     */
    List<Transaction> getAllTransactions(@NonNull String walletId);
}
